package com.sis.backend.mapper

import com.sis.backend.entity.Category
import java.sql.ResultSet

/**
 * Ánh xạ bản ghi từ ResultSet sang đối tượng Category.
 *
 * @param rs ResultSet chứa dữ liệu bản ghi từ cơ sở dữ liệu.
 * @return Đối tượng Category tương ứng.
 */
fun mapCategory(rs: ResultSet): Category {
    return Category(
        categoryId = rs.getLong("category_id"),
        categoryName = rs.getString("category_name"),
        description = rs.getString("description"),
        status = rs.getString("status"),
        createdAt = rs.getTimestamp("created_at").toString(),
        updatedAt = rs.getTimestamp("updated_at").toString()
    )
}