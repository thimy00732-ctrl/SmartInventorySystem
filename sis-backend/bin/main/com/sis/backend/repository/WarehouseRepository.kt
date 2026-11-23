package com.sis.backend.repository

import com.sis.backend.entity.Warehouse
import java.sql.ResultSet
import javax.sql.DataSource

/**
 * Repository xử lý truy vấn và thao tác dữ liệu kho hàng.
 *
 * @property dataSource Nguồn kết nối cơ sở dữ liệu.
 */
class WarehouseRepository(
    private val dataSource: DataSource
) {

    /**
     * Lấy tất cả kho hàng.
     *
     * @param activeOnly Chỉ lấy kho đang hoạt động nếu là true.
     * @return Danh sách kho hàng.
     */
    fun findAll(activeOnly: Boolean = false): List<Warehouse> {
        val sql = if (activeOnly) {
            """
            SELECT warehouse_id, warehouse_name, location,
                   is_active, created_at, updated_at
            FROM warehouses
            WHERE is_active = TRUE
            ORDER BY warehouse_name ASC
            """.trimIndent()
        } else {
            """
            SELECT warehouse_id, warehouse_name, location,
                   is_active, created_at, updated_at
            FROM warehouses
            ORDER BY warehouse_name ASC
            """.trimIndent()
        }

        return dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.executeQuery().use { resultSet ->
                    buildList {
                        while (resultSet.next()) {
                            add(mapWarehouse(resultSet))
                        }
                    }
                }
            }
        }
    }

    /**
     * Tìm kho theo ID.
     *
     * @param warehouseId ID kho cần tìm.
     * @return Warehouse nếu tồn tại, ngược lại null.
     */
    fun findById(warehouseId: Long): Warehouse? {
        val sql = """
            SELECT warehouse_id, warehouse_name, location,
                   is_active, created_at, updated_at
            FROM warehouses
            WHERE warehouse_id = ?
        """.trimIndent()

        return dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setLong(1, warehouseId)

                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) mapWarehouse(resultSet) else null
                }
            }
        }
    }

    /**
     * Kiểm tra kho có tồn tại và đang hoạt động hay không.
     *
     * @param warehouseId ID kho cần kiểm tra.
     * @return true nếu kho tồn tại và đang hoạt động.
     */
    fun existsActiveById(warehouseId: Long): Boolean {
        val sql = """
            SELECT EXISTS (
                SELECT 1
                FROM warehouses
                WHERE warehouse_id = ?
                  AND is_active = TRUE
            )
        """.trimIndent()

        return dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setLong(1, warehouseId)

                statement.executeQuery().use { result ->
                    result.next() && result.getBoolean(1)
                }
            }
        }
    }

    /**
     * Lấy danh sách warehouse options cho dropdown (chỉ active warehouses).
     *
     * @return Danh sách WarehouseOption gồm id, name, location.
     */
    fun findAllActiveForDropdown(): List<com.sis.backend.dto.WarehouseOption> {
        val sql = """
            SELECT warehouse_id, warehouse_name, location
            FROM warehouses
            WHERE is_active = TRUE
            ORDER BY warehouse_name ASC
        """.trimIndent()

        return dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.executeQuery().use { resultSet ->
                    buildList {
                        while (resultSet.next()) {
                            add(
                                com.sis.backend.dto.WarehouseOption(
                                    warehouseId = resultSet.getLong("warehouse_id"),
                                    warehouseName = resultSet.getString("warehouse_name"),
                                    location = resultSet.getString("location")
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Map ResultSet thành Warehouse entity.
     *
     * @param resultSet ResultSet từ query.
     * @return Warehouse object.
     */
    private fun mapWarehouse(resultSet: ResultSet): Warehouse {
        return Warehouse(
            warehouseId = resultSet.getLong("warehouse_id"),
            warehouseName = resultSet.getString("warehouse_name"),
            location = resultSet.getString("location"),
            isActive = resultSet.getBoolean("is_active"),
            createdAt = resultSet.getTimestamp("created_at").toLocalDateTime(),
            updatedAt = resultSet.getTimestamp("updated_at").toLocalDateTime()
        )
    }
}
