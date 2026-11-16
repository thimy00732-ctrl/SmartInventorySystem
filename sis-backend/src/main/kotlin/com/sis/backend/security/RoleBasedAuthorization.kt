package com.sis.backend.security

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*

/**
 * RoleBasedAuthorization: Kiểm tra quyền truy cập theo vai trò
 * 
 * Theo tài liệu phân quyền 111 UC:
 * - ADMIN: Toàn quyền quản lý hệ thống
 * - WAREHOUSE_STAFF: Quyền hạn chế, chỉ thao tác nghiệp vụ hàng ngày
 * 
 * Trả về 403 khi sai quyền, 401 khi token không hợp lệ
 */

/**
 * AttributeKey cho RoleBasedAuthorizationPlugin
 */
private val RoleBasedAuthorizationPluginKey = AttributeKey<RoleBasedAuthorizationPlugin>("RoleBasedAuthorization")

/**
 * Plugin kiểm tra role
 */
class RoleBasedAuthorizationPlugin(private val requiredRoles: Set<String>) {
    
    class Configuration {
        val roles = mutableSetOf<String>()
        
        fun role(vararg roles: String) {
            this.roles.addAll(roles)
        }
    }

    companion object Plugin : BaseRouteScopedPlugin<Configuration, RoleBasedAuthorizationPlugin> {
        override val key = RoleBasedAuthorizationPluginKey

        override fun install(
            pipeline: ApplicationCallPipeline,
            configure: Configuration.() -> Unit
        ): RoleBasedAuthorizationPlugin {
            val config = Configuration().apply(configure)
            val plugin = RoleBasedAuthorizationPlugin(config.roles)

            pipeline.intercept(ApplicationCallPipeline.Call) {
                val principal = call.principal<JwtPrincipal>()
                
                if (principal == null) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        mapOf("error" to "Thiếu hoặc token không hợp lệ")
                    )
                    finish()
                    return@intercept
                }

                if (principal.role !in plugin.requiredRoles) {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        mapOf("error" to "Bạn không có quyền truy cập chức năng này")
                    )
                    finish()
                    return@intercept
                }
            }

            return plugin
        }
    }
}

/**
 * Extension function để yêu cầu ADMIN role
 * 
 * Theo tài liệu phân quyền:
 * - UC08: Thêm sản phẩm - ADMIN only
 * - UC11: Cập nhật sản phẩm - ADMIN only
 * - UC12: Ngừng kinh doanh sản phẩm - ADMIN only
 * - UC18-UC22: Quản lý danh mục - ADMIN only
 * - UC23-UC27: Quản lý nhà cung cấp - ADMIN only
 * - UC33: Thiết lập ngưỡng tồn kho - ADMIN only
 * - UC39, UC47, UC91: Xem giá trị tồn kho (giá vốn) - ADMIN only
 * - UC45, UC55, UC64: Phê duyệt phiếu - ADMIN only
 * - UC65, UC66: Điều chỉnh tồn kho - ADMIN only
 */
fun Route.requireAdmin(build: Route.() -> Unit): Route {
    return authenticate("auth-jwt") {
        install(RoleBasedAuthorizationPlugin) {
            role("ADMIN")
        }
        build()
    }
}

/**
 * Extension function để yêu cầu WAREHOUSE_STAFF hoặc ADMIN
 * 
 * Theo tài liệu phân quyền:
 * - UC09, UC10, UC13, UC14: Xem, tìm kiếm, lọc sản phẩm - Cả hai role
 * - UC19, UC22: Xem, tìm kiếm danh mục - Cả hai role
 * - UC24, UC25, UC28: Xem, tìm kiếm nhà cung cấp - Cả hai role
 * - UC29-UC38: Xem tồn kho, sổ kho - Cả hai role
 * - UC40-UC44, UC49-UC54, UC59-UC63: Lập phiếu nhập, xuất, kiểm kê - Cả hai role
 * - UC68-UC88: Quét mã, lô hàng, chatbot - Cả hai role
 * - UC89-UC105: Báo cáo và thống kê - Cả hai role (trừ giá trị tồn kho)
 */
fun Route.requireStaffOrAdmin(build: Route.() -> Unit): Route {
    return authenticate("auth-jwt") {
        install(RoleBasedAuthorizationPlugin) {
            role("ADMIN", "WAREHOUSE_STAFF")
        }
        build()
    }
}

/**
 * Extension function để yêu cầu xác thực nhưng không giới hạn role
 * Dùng cho các endpoint cần kiểm tra thêm logic phức tạp bên trong
 */
fun Route.requireAuthentication(build: Route.() -> Unit): Route {
    return authenticate("auth-jwt") {
        build()
    }
}

/**
 * Helper function để kiểm tra quyền sở hữu phiếu
 * Dùng cho các trường hợp "Giới hạn" trong ma trận phân quyền
 * 
 * Ví dụ:
 * - UC42: Nhân viên chỉ sửa phiếu do mình lập, khi còn DRAFT
 * - UC44: Nhân viên chỉ gửi duyệt phiếu do mình lập
 * - UC53, UC54, UC57: Tương tự với phiếu xuất
 * 
 * @param createdByUserId - user_id của người tạo phiếu
 * @param currentUserId - user_id từ JWT token
 * @param currentUserRole - role từ JWT token
 * @param allowAdmin - Admin có được phép không (mặc định: true)
 * @return true nếu có quyền, false nếu không
 */
fun checkOwnership(
    createdByUserId: Int,
    currentUserId: Int,
    currentUserRole: String,
    allowAdmin: Boolean = true
): Boolean {
    // Admin được toàn quyền (nếu allowAdmin = true)
    if (allowAdmin && currentUserRole == "ADMIN") {
        return true
    }
    
    // Người dùng chỉ được thao tác với phiếu của chính mình
    return createdByUserId == currentUserId
}

/**
 * Helper function để kiểm tra phiếu có thể chỉnh sửa không
 * Theo tài liệu: chỉ sửa được phiếu DRAFT do mình lập
 * 
 * @param transactionStatus - trạng thái phiếu (DRAFT, PENDING, ...)
 * @param createdByUserId - user_id của người tạo phiếu
 * @param currentUserId - user_id từ JWT token
 * @param currentUserRole - role từ JWT token
 * @return true nếu có thể sửa, false nếu không
 */
fun canEditTransaction(
    transactionStatus: String,
    createdByUserId: Int,
    currentUserId: Int,
    currentUserRole: String
): Boolean {
    // Chỉ sửa được phiếu DRAFT
    if (transactionStatus != "DRAFT") {
        return false
    }
    
    // Kiểm tra quyền sở hữu
    return checkOwnership(createdByUserId, currentUserId, currentUserRole, allowAdmin = true)
}

/**
 * Helper function để kiểm tra có thể duyệt phiếu không
 * Theo tài liệu UC45, UC55, UC64:
 * - Chỉ Admin duyệt
 * - Admin không tự duyệt phiếu của chính mình (người duyệt khác người lập)
 * 
 * @param createdByUserId - user_id của người tạo phiếu
 * @param currentUserId - user_id từ JWT token
 * @param currentUserRole - role từ JWT token
 * @return true nếu có thể duyệt, false nếu không
 */
fun canApproveTransaction(
    createdByUserId: Int,
    currentUserId: Int,
    currentUserRole: String
): Boolean {
    // Chỉ Admin mới được duyệt
    if (currentUserRole != "ADMIN") {
        return false
    }
    
    // Admin không được tự duyệt phiếu của chính mình
    return createdByUserId != currentUserId
}

/**
 * Helper function để kiểm tra quyền xem giá trị tồn kho (giá vốn)
 * Theo tài liệu UC39, UC47, UC91: chỉ ADMIN
 * 
 * @param currentUserRole - role từ JWT token
 * @return true nếu được xem, false nếu không
 */
fun canViewInventoryValue(currentUserRole: String): Boolean {
    return currentUserRole == "ADMIN"
}

/**
 * Helper function để kiểm tra quyền điều chỉnh tồn kho trực tiếp
 * Theo tài liệu UC65, UC66: chỉ ADMIN
 * 
 * @param currentUserRole - role từ JWT token
 * @return true nếu được điều chỉnh, false nếu không
 */
fun canAdjustInventoryDirectly(currentUserRole: String): Boolean {
    return currentUserRole == "ADMIN"
}
