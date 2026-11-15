package com.sis.backend.controller

import com.sis.backend.dto.CreateSupplierRequest
import com.sis.backend.dto.UpdateSupplierRequest
import com.sis.backend.exception.DuplicateSupplierException
import com.sis.backend.exception.InvalidSupplierException
import com.sis.backend.exception.SupplierNotFoundException
import com.sis.backend.service.SupplierService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Controller tiếp nhận và xử lý request liên quan đến nhà cung cấp.
 *
 * @property supplierService Service xử lý nghiệp vụ nhà cung cấp.
 */
class SupplierController(
    private val supplierService: SupplierService
) {

    /**
     * Logger ghi nhận lỗi phát sinh trong SupplierController.
     */
    private val logger = Logger.getLogger(
        SupplierController::class.java.name
    )

    /**
     * Tạo nhà cung cấp.
     */
    suspend fun createSupplier(call: ApplicationCall) {
        try {
            val request = call.receive<CreateSupplierRequest>()
            val result = supplierService.createSupplier(request)

            call.respond(HttpStatusCode.Created, result)
        } catch (exception: Exception) {
            handleException(call, exception)
        }
    }

    /**
     * Lấy danh sách nhà cung cấp.
     *
     * Query parameter:
     * - keyword: Từ khóa tìm kiếm.
     * - activeOnly: Chỉ lấy nhà cung cấp đang hoạt động.
     */
    suspend fun getSuppliers(call: ApplicationCall) {
        try {
            val keyword = call.request.queryParameters["keyword"]

            val activeOnly = call.request.queryParameters["activeOnly"]
                ?.toBooleanStrictOrNull() ?: true

            val result = supplierService.getSuppliers(
                keyword = keyword,
                activeOnly = activeOnly
            )

            call.respond(HttpStatusCode.OK, result)
        } catch (exception: Exception) {
            handleException(call, exception)
        }
    }

    /**
     * Xem chi tiết nhà cung cấp.
     *
     * Bao gồm thông tin nhà cung cấp, sản phẩm liên quan
     * và lịch sử giao dịch nhập hàng.
     */
    suspend fun getSupplierDetail(call: ApplicationCall) {
        try {
            val supplierId = getSupplierId(call)
            val result = supplierService.getSupplierDetail(supplierId)

            call.respond(HttpStatusCode.OK, result)
        } catch (exception: Exception) {
            handleException(call, exception)
        }
    }

    /**
     * Cập nhật thông tin nhà cung cấp.
     */
    suspend fun updateSupplier(call: ApplicationCall) {
        try {
            val supplierId = getSupplierId(call)
            val request = call.receive<UpdateSupplierRequest>()

            val result = supplierService.updateSupplier(
                supplierId = supplierId,
                request = request
            )

            call.respond(HttpStatusCode.OK, result)
        } catch (exception: Exception) {
            handleException(call, exception)
        }
    }

    /**
     * Ngừng hoạt động nhà cung cấp.
     */
    suspend fun deactivateSupplier(call: ApplicationCall) {
        try {
            val supplierId = getSupplierId(call)

            supplierService.deactivateSupplier(supplierId)

            call.respond(
                HttpStatusCode.OK,
                mapOf(
                    "message" to "Ngừng hoạt động nhà cung cấp thành công."
                )
            )
        } catch (exception: Exception) {
            handleException(call, exception)
        }
    }

    /**
     * Tìm kiếm nhà cung cấp.
     *
     * Tìm kiếm theo tên, số điện thoại hoặc email.
     */
    suspend fun searchSuppliers(call: ApplicationCall) {
        try {
            val keyword = call.request.queryParameters["keyword"]
                ?: throw InvalidSupplierException(
                    "Vui lòng cung cấp từ khóa tìm kiếm."
                )

            val result = supplierService.searchSuppliers(keyword)

            call.respond(HttpStatusCode.OK, result)
        } catch (exception: Exception) {
            handleException(call, exception)
        }
    }

    /**
     * Hợp tác lại nhà cung cấp.
     *
     * Chuyển trạng thái nhà cung cấp từ ngừng hoạt động
     * sang đang hoạt động.
     */
    suspend fun reactivateSupplier(call: ApplicationCall) {
        try {
            val supplierId = getSupplierId(call)

            supplierService.reactivateSupplier(supplierId)

            call.respond(
                HttpStatusCode.OK,
                mapOf(
                    "message" to "Hợp tác lại nhà cung cấp thành công."
                )
            )
        } catch (exception: Exception) {
            handleException(call, exception)
        }
    }

    /**
     * Lấy và kiểm tra supplierId từ URL.
     *
     * @param call Request hiện tại.
     * @return Mã nhà cung cấp hợp lệ.
     */
    private fun getSupplierId(call: ApplicationCall): Long {
        val supplierId = call.parameters["id"]?.toLongOrNull()
            ?: throw InvalidSupplierException(
                "Mã nhà cung cấp không hợp lệ."
            )

        if (supplierId <= 0) {
            throw InvalidSupplierException(
                "Mã nhà cung cấp phải lớn hơn 0."
            )
        }

        return supplierId
    }

    /**
     * Chuyển exception thành HTTP response.
     *
     * @param call Request hiện tại.
     * @param exception Exception phát sinh.
     */
    private suspend fun handleException(
        call: ApplicationCall,
        exception: Exception
    ) {
        when (exception) {
            is SupplierNotFoundException -> {
                call.respond(
                    HttpStatusCode.NotFound,
                    mapOf(
                        "message" to (
                                exception.message
                                    ?: "Không tìm thấy nhà cung cấp."
                                )
                    )
                )
            }

            is DuplicateSupplierException -> {
                call.respond(
                    HttpStatusCode.Conflict,
                    mapOf(
                        "message" to (
                                exception.message
                                    ?: "Tên nhà cung cấp đã tồn tại."
                                )
                    )
                )
            }

            is InvalidSupplierException -> {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf(
                        "message" to (
                                exception.message
                                    ?: "Dữ liệu không hợp lệ."
                                )
                    )
                )
            }

            else -> {
                logger.log(
                    Level.SEVERE,
                    "Supplier API error",
                    exception
                )

                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf(
                        "message" to "Đã xảy ra lỗi trong quá trình xử lý."
                    )
                )
            }
        }
    }
}
