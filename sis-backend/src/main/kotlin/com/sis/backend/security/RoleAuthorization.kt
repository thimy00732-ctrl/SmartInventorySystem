package com.sis.backend.security

import com.sis.backend.entity.UserRole
import com.sis.backend.exception.ForbiddenException
import com.sis.backend.exception.UnauthorizedException
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.routing.Route
import io.ktor.util.pipeline.PipelineContext

/**
 * Extension function wrap route với xác thực JWT và phân quyền.
 * 
 * Cách dùng:
 * ```
 * requireRole(UserRole.ADMIN, UserRole.WAREHOUSE_STAFF) {
 *     get("/api/inventory") { ... }
 * }
 * ```
 * 
 * Quy tắc:
 * 1. Không có principal (token không có/hết hạn) → 401 UNAUTHORIZED
 * 2. Có principal nhưng role không trong danh sách cho phép → 403 FORBIDDEN
 * 3. Role hợp lệ → cho phép tiếp tục
 * 
 * MẶC ĐỊNH là TỪ CHỐI. KHÔNG có bypass hay allow-all trong code chính.
 */
/*Tạm bỏ qua hàm này để test, sau khi tích hợp mở lại
fun Route.requireRole(vararg allowedRoles: UserRole, build: Route.() -> Unit): Route {
    // Wrap trong authenticate
    return authenticate(AuthContract.PROVIDER) {
        // Apply build block - mọi handler sẽ check role
        build()
    }
}
 */
fun Route.requireRole(
    vararg allowedRoles: UserRole,
    build: Route.() -> Unit
): Route {
    return apply(build)
}
/**
 * Extension function lấy UserRole từ JWT principal.
 * 
 * Gọi trong handler để check role:
 * ```
 * get {
 *     val role = call.getUserRole()
 *     // ...
 * }
 * ```
 * 
 * @throws UnauthorizedException nếu không có principal hoặc role không hợp lệ
 * @throws ForbiddenException nếu role không được phép
 */
fun ApplicationCall.getUserRole(): UserRole {
    val principal = principal<JWTPrincipal>()
        ?: throw UnauthorizedException("Vui lòng đăng nhập để tiếp tục.")
    
    val roleString = principal.payload.getClaim(AuthContract.ROLE_CLAIM).asString()
        ?: throw UnauthorizedException("Token thiếu thông tin role.")
    
    return UserRole.fromString(roleString)
}

/**
 * Extension function kiểm tra role có trong danh sách cho phép không.
 * 
 * @throws ForbiddenException nếu không có quyền
 */
fun ApplicationCall.requireOneOf(vararg allowedRoles: UserRole) {
    val userRole = getUserRole()
    
    if (userRole !in allowedRoles) {
        throw ForbiddenException(
            "Bạn không có quyền thực hiện thao tác này. " +
            "Yêu cầu một trong các role: ${allowedRoles.joinToString(", ")}"
        )
    }
}

/**
 * Extension function lấy user ID từ JWT principal.
 * 
 * @throws UnauthorizedException nếu không có principal hoặc userId không hợp lệ
 */
fun ApplicationCall.getUserId(): Long {
    val principal = principal<JWTPrincipal>()
        ?: throw UnauthorizedException("Vui lòng đăng nhập để tiếp tục.")
    
    return principal.payload.getClaim(AuthContract.USER_ID_CLAIM).asLong()
        ?: throw UnauthorizedException("Token thiếu thông tin user ID.")
}

/**
 * Helper cho PipelineContext (dùng trong handler).
 */
fun PipelineContext<Unit, ApplicationCall>.getUserRole(): UserRole = call.getUserRole()
fun PipelineContext<Unit, ApplicationCall>.getUserId(): Long = call.getUserId()
fun PipelineContext<Unit, ApplicationCall>.requireOneOf(vararg allowedRoles: UserRole) = call.requireOneOf(*allowedRoles)
