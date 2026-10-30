package com.sis.backend.mapper

import com.sis.backend.dto.product.ProductInventoryResponse
import java.sql.ResultSet
import java.sql.Date
import java.sql.Timestamp

/**
 * Chuyển đổi dữ liệu tồn kho từ ResultSet sang DTO.
 */
object ProductInventoryMapper {

    /**
     * Chuyển một dòng kết quả truy vấn thành thông tin tồn kho.
     *
     * @param resultSet Dòng dữ liệu truy vấn.
     * @return Thông tin tồn kho của sản phẩm.
     */
    fun toResponse(resultSet: ResultSet): ProductInventoryResponse {
        return ProductInventoryResponse(
            inventoryId = resultSet.getLong("inventory_id"),
            warehouseId = resultSet.getLong("warehouse_id"),
            warehouseName = resultSet.getString("warehouse_name"),
            batchCode = resultSet.getString("batch_code"),
            manufactureDate = resultSet.getDate("manufacture_date")
                ?.toLocalDate()
                ?.toString(),
            expiryDate = resultSet.getDate("expiry_date")
                ?.toLocalDate()
                ?.toString(),
            stockQuantity = resultSet.getInt("stock_quantity"),
            reservedQuantity = resultSet.getInt("reserved_quantity"),
            availableQuantity = resultSet.getInt("available_quantity"),
            updatedAt = resultSet.getTimestamp("updated_at")
                ?.toLocalDateTime()
                ?.toString()
                ?: ""
        )
    }
}
