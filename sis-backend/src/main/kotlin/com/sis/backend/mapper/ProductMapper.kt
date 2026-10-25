package com.sis.backend.mapper

import com.sis.backend.entity.Product
import java.sql.ResultSet

/**
 * Chuyển bản ghi JDBC thành đối tượng Product.
 *
 * @param resultSet Bản ghi kết quả truy vấn sản phẩm.
 * @return Đối tượng Product tương ứng với bản ghi.
 */
fun mapProduct(resultSet: ResultSet): Product {
    val categoryIdValue = resultSet.getLong("category_id")
    val categoryId = if (resultSet.wasNull()) null else categoryIdValue

    val createdByValue = resultSet.getLong("created_by")
    val createdBy = if (resultSet.wasNull()) null else createdByValue

    return Product(
        productId = resultSet.getLong("product_id"),
        sku = resultSet.getString("sku"),
        barcode = resultSet.getString("barcode"),
        productName = resultSet.getString("product_name"),
        description = resultSet.getString("description"),
        unit = resultSet.getString("unit"),
        minStock = resultSet.getBigDecimal("min_stock").toDouble(),
        categoryId = categoryId,
        status = resultSet.getString("status"),
        createdBy = createdBy,
        createdAt = resultSet.getTimestamp("created_at").toString(),
        updatedAt = resultSet.getTimestamp("updated_at").toString()
    )
}