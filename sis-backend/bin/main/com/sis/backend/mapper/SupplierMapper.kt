package com.sis.backend.mapper

import com.sis.backend.dto.SupplierResponse
import com.sis.backend.entity.Supplier

/**
 * Mapper chuyển đổi dữ liệu Supplier Entity sang DTO.
 */
object SupplierMapper {

    /**
     * Chuyển Supplier Entity thành SupplierResponse.
     */
    fun toResponse(supplier: Supplier): SupplierResponse {
        return SupplierResponse(
            supplierId = supplier.supplierId,
            supplierName = supplier.supplierName,
            phone = supplier.phone,
            email = supplier.email,
            address = supplier.address,
            isActive = supplier.isActive,
            createdAt = supplier.createdAt.toString(),
            updatedAt = supplier.updatedAt.toString()
        )
    }

    /**
     * Chuyển danh sách Supplier Entity thành danh sách DTO.
     */
    fun toResponseList(
        suppliers: List<Supplier>
    ): List<SupplierResponse> {
        return suppliers.map(::toResponse)
    }
}