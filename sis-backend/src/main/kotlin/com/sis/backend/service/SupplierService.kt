package com.sis.backend.service

import com.sis.backend.dto.CreateSupplierRequest
import com.sis.backend.dto.SupplierDetailResponse
import com.sis.backend.dto.SupplierProductResponse
import com.sis.backend.dto.SupplierResponse
import com.sis.backend.dto.SupplierTransactionDetailResponse
import com.sis.backend.dto.SupplierTransactionResponse
import com.sis.backend.dto.UpdateSupplierRequest
import com.sis.backend.exception.DuplicateSupplierException
import com.sis.backend.exception.InvalidSupplierException
import com.sis.backend.exception.SupplierNotFoundException
import com.sis.backend.mapper.SupplierMapper
import com.sis.backend.repository.SupplierRepository

/**
 * Service xử lý nghiệp vụ nhà cung cấp UC23–UC28.
 */
class SupplierService(
    private val supplierRepository: SupplierRepository
) {

    /**
     * Thêm nhà cung cấp mới.
     */
    fun createSupplier(
        request: CreateSupplierRequest
    ): SupplierResponse {
        val supplierName = validateSupplierName(request.supplierName)
        val phone = normalizeOptional(request.phone)
        val email = validateEmail(request.email)
        val address = normalizeOptional(request.address)

        validatePhone(phone)

        if (supplierRepository.existsByName(supplierName)) {
            throw DuplicateSupplierException()
        }

        val supplierId = supplierRepository.create(
            supplierName = supplierName,
            phone = phone,
            email = email,
            address = address
        )

        val supplier = supplierRepository.findById(supplierId)
            ?: throw IllegalStateException("Không thể lấy thông tin nhà cung cấp vừa tạo.")

        return SupplierMapper.toResponse(supplier)
    }

    /**
     * Lấy danh sách nhà cung cấp.
     */
    fun getSuppliers(
        keyword: String?,
        activeOnly: Boolean = true
    ): List<SupplierResponse> {
        return SupplierMapper.toResponseList(
            supplierRepository.findAll(keyword, activeOnly)
        )
    }

    /**
     * Xem chi tiết nhà cung cấp.
     *
     * Bao gồm thông tin nhà cung cấp, sản phẩm liên quan
     * và lịch sử giao dịch nhập hàng.
     */
    fun getSupplierDetail(
        supplierId: Long
    ): SupplierDetailResponse {
        val supplier = supplierRepository.findById(supplierId)
            ?: throw SupplierNotFoundException()

        val products = supplierRepository
            .findProductsBySupplierId(supplierId)
            .map { row ->
                SupplierProductResponse(
                    productId = row["productId"] as Long,
                    sku = row["sku"] as String,
                    barcode = row["barcode"] as String?,
                    productName = row["productName"] as String,
                    imageUrl = row["imageUrl"] as String?,
                    categoryId = row["categoryId"] as Long?,
                    supplierId = row["supplierId"] as Long?,
                    unit = row["unit"] as String,
                    costPrice = row["costPrice"] as Double,
                    salePrice = row["salePrice"] as Double,
                    minStock = row["minStock"] as Int,
                    maxStock = row["maxStock"] as Int,
                    isActive = row["isActive"] as Boolean,
                    createdAt = row["createdAt"] as String,
                    updatedAt = row["updatedAt"] as String
                )
            }

        val transactions = supplierRepository
            .findImportTransactionsBySupplierId(supplierId)
            .map { transaction ->
                val transactionId = transaction["transactionId"] as Long

                val details = supplierRepository
                    .findTransactionDetails(transactionId)
                    .map { row ->
                        SupplierTransactionDetailResponse(
                            detailId = row["detailId"] as Long,
                            transactionId = row["transactionId"] as Long,
                            productId = row["productId"] as Long,
                            sku = row["sku"] as String,
                            productName = row["productName"] as String,
                            batchCode = row["batchCode"] as String,
                            manufactureDate = row["manufactureDate"] as String?,
                            expiryDate = row["expiryDate"] as String?,
                            quantity = row["quantity"] as Int,
                            unitPrice = row["unitPrice"] as Double,
                            systemQuantity = row["systemQuantity"] as Int?,
                            actualQuantity = row["actualQuantity"] as Int?,
                            differenceQuantity = row["differenceQuantity"] as Int?,
                            note = row["note"] as String?
                        )
                    }

                SupplierTransactionResponse(
                    transactionId = transactionId,
                    warehouseId = transaction["warehouseId"] as Long,
                    targetWarehouseId = transaction["targetWarehouseId"] as Long?,
                    supplierId = transaction["supplierId"] as Long?,
                    userId = transaction["userId"] as Long,
                    approvedBy = transaction["approvedBy"] as Long?,
                    transactionType = transaction["transactionType"] as String,
                    transactionSubtype = transaction["transactionSubtype"] as String?,
                    status = transaction["status"] as String,
                    note = transaction["note"] as String?,
                    rejectionReason = transaction["rejectionReason"] as String?,
                    createdAt = transaction["createdAt"] as String,
                    submittedAt = transaction["submittedAt"] as String?,
                    approvedAt = transaction["approvedAt"] as String?,
                    completedAt = transaction["completedAt"] as String?,
                    cancelledAt = transaction["cancelledAt"] as String?,
                    details = details
                )
            }

        return SupplierDetailResponse(
            supplierId = supplier.supplierId,
            supplierName = supplier.supplierName,
            phone = supplier.phone,
            email = supplier.email,
            address = supplier.address,
            isActive = supplier.isActive,
            createdAt = supplier.createdAt.toString(),
            updatedAt = supplier.updatedAt.toString(),
            products = products,
            transactions = transactions
        )
    }

    /**
     * Cập nhật thông tin nhà cung cấp.
     */
    fun updateSupplier(
        supplierId: Long,
        request: UpdateSupplierRequest
    ): SupplierResponse {
        supplierRepository.findById(supplierId)
            ?: throw SupplierNotFoundException()

        val supplierName = validateSupplierName(request.supplierName)
        val phone = normalizeOptional(request.phone)
        val email = validateEmail(request.email)
        val address = normalizeOptional(request.address)

        validatePhone(phone)

        if (supplierRepository.existsByName(supplierName, supplierId)) {
            throw DuplicateSupplierException()
        }

        supplierRepository.update(
            supplierId = supplierId,
            supplierName = supplierName,
            phone = phone,
            email = email,
            address = address
        )

        val updatedSupplier = supplierRepository.findById(supplierId)
            ?: throw SupplierNotFoundException()

        return SupplierMapper.toResponse(updatedSupplier)
    }

    /**
     * Ngừng hoạt động nhà cung cấp.
     *
     * Dữ liệu nhà cung cấp và lịch sử giao dịch vẫn được giữ lại.
     */
    fun deactivateSupplier(
        supplierId: Long
    ) {
        val supplier = supplierRepository.findById(supplierId)
            ?: throw SupplierNotFoundException()

        if (!supplier.isActive) {
            throw InvalidSupplierException("Nhà cung cấp đã ngừng hoạt động.")
        }

        supplierRepository.deactivate(supplierId)
    }

    /**
     * Tìm kiếm nhà cung cấp.
     *
     * Tìm theo tên, số điện thoại hoặc email.
     */
    fun searchSuppliers(
        keyword: String
    ): List<SupplierResponse> {
        if (keyword.isBlank()) {
            throw InvalidSupplierException("Từ khóa tìm kiếm không được để trống.")
        }

        return SupplierMapper.toResponseList(
            supplierRepository.findAll(
                keyword = keyword.trim(),
                activeOnly = false
            )
        )
    }

    /**
     * Kiểm tra và chuẩn hóa tên nhà cung cấp.
     */
    private fun validateSupplierName(name: String): String {
        val value = name.trim()

        if (value.isBlank()) {
            throw InvalidSupplierException("Tên nhà cung cấp không được để trống.")
        }

        if (value.length > 150) {
            throw InvalidSupplierException("Tên nhà cung cấp không được vượt quá 150 ký tự.")
        }

        return value
    }

    /**
     * Chuẩn hóa các trường dữ liệu tùy chọn.
     */
    private fun normalizeOptional(value: String?): String? {
        return value?.trim()?.takeIf { it.isNotEmpty() }
    }

    /**
     * Kiểm tra định dạng email và giới hạn độ dài.
     */
    private fun validateEmail(email: String?): String? {
        val value = normalizeOptional(email) ?: return null

        if (value.length > 100) {
            throw InvalidSupplierException("Email không được vượt quá 100 ký tự.")
        }

        val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

        if (!emailRegex.matches(value)) {
            throw InvalidSupplierException("Email không đúng định dạng.")
        }

        return value
    }

    /**
     * Kiểm tra số điện thoại và giới hạn độ dài.
     */
    private fun validatePhone(phone: String?) {
        if (phone == null) return

        if (phone.length > 20) {
            throw InvalidSupplierException("Số điện thoại không được vượt quá 20 ký tự.")
        }

        val phoneRegex = Regex("^[0-9+() .-]+$")

        if (!phoneRegex.matches(phone)) {
            throw InvalidSupplierException("Số điện thoại chứa ký tự không hợp lệ.")
        }
    }

    /**
     * Hợp tác lại nhà cung cấp.
     *
     * Chuyển trạng thái nhà cung cấp từ ngừng hoạt động
     * sang đang hoạt động.
     *
     * @param supplierId Mã nhà cung cấp cần kích hoạt.
     */
    fun reactivateSupplier(supplierId: Long) {
        val supplier = supplierRepository.findById(supplierId)
            ?: throw SupplierNotFoundException()

        if (supplier.isActive) {
            throw InvalidSupplierException(
                "Nhà cung cấp hiện đang hoạt động."
            )
        }

        val updated = supplierRepository.reactivate(supplierId)

        if (!updated) {
            throw InvalidSupplierException(
                "Không thể kích hoạt lại nhà cung cấp."
            )
        }
    }
}