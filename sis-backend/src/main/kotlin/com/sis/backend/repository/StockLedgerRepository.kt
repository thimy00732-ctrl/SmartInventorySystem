package com.sis.backend.repository

import com.sis.backend.dto.StockLedgerItemResponse
import com.sis.backend.dto.StockLedgerTotalResponse
import java.math.BigDecimal
import java.time.LocalDate
import javax.sql.DataSource

/**
 * Repository xử lý truy vấn sổ kho (UC36).
 * 
 * Dùng CTE phức tạp với movements, stock_now, agg, pairs.
 * Tồn đầu kỳ tính ngược từ tồn hiện tại.
 * 
 * @property dataSource Nguồn kết nối cơ sở dữ liệu.
 */
class StockLedgerRepository(
    private val dataSource: DataSource
) {
    
    /**
     * Lấy sổ kho với phân trang (UC36).
     * 
     * @param fromDate Ngày bắt đầu (bắt buộc).
     * @param toDate Ngày kết thúc (bắt buộc, exclusive).
     * @param warehouseId Lọc theo kho (null = tất cả).
     * @param categoryId Lọc theo danh mục (null = tất cả).
     * @param keyword Tìm kiếm theo SKU hoặc tên sản phẩm.
     * @param page Trang hiện tại (từ 1).
     * @param size Số dòng mỗi trang.
     * @return Danh sách items và tổng số dòng.
     */
    fun getStockLedger(
        fromDate: LocalDate,
        toDate: LocalDate,
        warehouseId: Long? = null,
        categoryId: Long? = null,
        keyword: String? = null,
        page: Int = 1,
        size: Int = 10
    ): Pair<List<StockLedgerItemResponse>, Long> {
        
        require(page >= 1) { "Page phải >= 1" }
        require(size in 1..100) { "Size phải từ 1-100" }
        
        val toExclusive = toDate.plusDays(1)
        
        // SQL CTE từ spec (converted từ :param sang ? positional)
        val baseSql = """
            WITH movements AS (
                SELECT t.transaction_id, t.completed_at AS tx_time, t.warehouse_id, d.product_id,
                       CASE WHEN t.transaction_type='IMPORT' THEN 'IMPORT'
                            WHEN t.transaction_type='EXPORT' THEN 'EXPORT'
                            WHEN t.transaction_type='TRANSFER' THEN 'TRANSFER_OUT'
                            ELSE 'ADJUST' END AS movement_kind,
                       CASE WHEN t.transaction_type='IMPORT' THEN d.quantity
                            WHEN t.transaction_type IN ('EXPORT','TRANSFER') THEN -d.quantity
                            WHEN t.transaction_type='STOCKTAKE' THEN COALESCE(d.difference_quantity,0)
                            WHEN t.transaction_type='ADJUSTMENT' THEN
                                 CASE WHEN t.transaction_subtype='SURPLUS' THEN d.quantity
                                      WHEN t.transaction_subtype IN ('SHORTAGE','DAMAGE') THEN -d.quantity
                                      ELSE COALESCE(d.difference_quantity,0) END
                       END AS delta
                FROM transactions t 
                JOIN transaction_details d ON d.transaction_id = t.transaction_id
                WHERE t.status = 'COMPLETED'
                UNION ALL
                SELECT t.transaction_id, t.completed_at, t.target_warehouse_id, d.product_id,
                       'TRANSFER_IN', d.quantity
                FROM transactions t 
                JOIN transaction_details d ON d.transaction_id = t.transaction_id
                WHERE t.status='COMPLETED' AND t.transaction_type='TRANSFER'
                  AND t.target_warehouse_id IS NOT NULL
            ),
            stock_now AS (
                SELECT product_id, warehouse_id, SUM(stock_quantity) AS current_qty
                FROM inventory 
                GROUP BY product_id, warehouse_id
            ),
            agg AS (
                SELECT m.product_id, m.warehouse_id,
                  SUM(CASE WHEN m.tx_time >= ? THEN m.delta ELSE 0 END) AS net_since_from,
                  SUM(CASE WHEN m.tx_time >= ? AND m.tx_time < ? AND m.movement_kind='IMPORT'       THEN m.delta  ELSE 0 END) AS import_qty,
                  SUM(CASE WHEN m.tx_time >= ? AND m.tx_time < ? AND m.movement_kind='EXPORT'       THEN -m.delta ELSE 0 END) AS export_qty,
                  SUM(CASE WHEN m.tx_time >= ? AND m.tx_time < ? AND m.movement_kind='TRANSFER_IN'  THEN m.delta  ELSE 0 END) AS transfer_in_qty,
                  SUM(CASE WHEN m.tx_time >= ? AND m.tx_time < ? AND m.movement_kind='TRANSFER_OUT' THEN -m.delta ELSE 0 END) AS transfer_out_qty,
                  SUM(CASE WHEN m.tx_time >= ? AND m.tx_time < ? AND m.movement_kind='ADJUST'       THEN m.delta  ELSE 0 END) AS adjust_net
                FROM movements m 
                GROUP BY m.product_id, m.warehouse_id
            ),
            pairs AS (
                SELECT product_id, warehouse_id FROM stock_now
                UNION
                SELECT product_id, warehouse_id FROM agg
            )
        """.trimIndent()
        
        val selectColumns = """
            SELECT w.warehouse_id, w.warehouse_name, p.product_id, p.sku, p.product_name, p.unit,
                   COALESCE(s.current_qty,0) - COALESCE(a.net_since_from,0) AS opening_qty,
                   COALESCE(a.import_qty,0) AS import_qty, 
                   COALESCE(a.export_qty,0) AS export_qty,
                   COALESCE(a.transfer_in_qty,0) AS transfer_in_qty, 
                   COALESCE(a.transfer_out_qty,0) AS transfer_out_qty,
                   COALESCE(a.adjust_net,0) AS adjust_net,
                   COALESCE(s.current_qty,0) - COALESCE(a.net_since_from,0)
                     + COALESCE(a.import_qty,0) - COALESCE(a.export_qty,0)
                     + COALESCE(a.transfer_in_qty,0) - COALESCE(a.transfer_out_qty,0)
                     + COALESCE(a.adjust_net,0) AS closing_qty
            FROM pairs k
            LEFT JOIN stock_now s ON s.product_id=k.product_id AND s.warehouse_id=k.warehouse_id
            LEFT JOIN agg a       ON a.product_id=k.product_id AND a.warehouse_id=k.warehouse_id
            JOIN products p   ON p.product_id = k.product_id
            JOIN warehouses w ON w.warehouse_id = k.warehouse_id AND w.is_active = TRUE
        """.trimIndent()
        
        val conditions = mutableListOf<String>()
        val filterParams = mutableListOf<Any>()
        
        warehouseId?.let {
            conditions.add("w.warehouse_id = ?")
            filterParams.add(it)
        }
        
        categoryId?.let {
            conditions.add("p.category_id = ?")
            filterParams.add(it)
        }
        
        keyword?.trim()?.takeIf { it.isNotBlank() }?.let {
            conditions.add("(p.sku LIKE ? OR p.product_name LIKE ?)")
            val searchValue = "%$it%"
            filterParams.add(searchValue)
            filterParams.add(searchValue)
        }
        
        val whereClause = if (conditions.isEmpty()) "" 
            else "WHERE ${conditions.joinToString(" AND ")}"
        
        // Count query
        val countSql = """
            $baseSql
            SELECT COUNT(*) FROM (
                $selectColumns
                $whereClause
            ) AS subquery
        """.trimIndent()
        
        // Data query
        val offset = (page - 1) * size
        val dataSql = """
            $baseSql
            $selectColumns
            $whereClause
            ORDER BY p.product_name, w.warehouse_name
            LIMIT ? OFFSET ?
        """.trimIndent()
        
        return dataSource.connection.use { conn ->
            // Tính total items
            val totalItems = conn.prepareStatement(countSql).use { stmt ->
                bindDateParameters(stmt, fromDate, toExclusive)
                bindFilterParameters(stmt, 11, filterParams)
                
                stmt.executeQuery().use { rs ->
                    if (rs.next()) rs.getLong(1) else 0L
                }
            }
            
            // Lấy items
            val items = conn.prepareStatement(dataSql).use { stmt ->
                bindDateParameters(stmt, fromDate, toExclusive)
                bindFilterParameters(stmt, 11, filterParams)
                stmt.setInt(11 + filterParams.size, size)
                stmt.setInt(11 + filterParams.size + 1, offset)
                
                stmt.executeQuery().use { rs ->
                    buildList {
                        while (rs.next()) {
                            add(
                                StockLedgerItemResponse(
                                    warehouseId = rs.getLong("warehouse_id"),
                                    warehouseName = rs.getString("warehouse_name"),
                                    productId = rs.getLong("product_id"),
                                    sku = rs.getString("sku"),
                                    productName = rs.getString("product_name"),
                                    unit = rs.getString("unit"),
                                    openingQty = rs.getBigDecimal("opening_qty"),
                                    importQty = rs.getBigDecimal("import_qty"),
                                    exportQty = rs.getBigDecimal("export_qty"),
                                    transferInQty = rs.getBigDecimal("transfer_in_qty"),
                                    transferOutQty = rs.getBigDecimal("transfer_out_qty"),
                                    adjustNet = rs.getBigDecimal("adjust_net"),
                                    closingQty = rs.getBigDecimal("closing_qty")
                                )
                            )
                        }
                    }
                }
            }
            
            Pair(items, totalItems)
        }
    }
    
    /**
     * Lấy tổng toàn bộ (không phân trang) cho UC36.
     */
    fun getStockLedgerTotal(
        fromDate: LocalDate,
        toDate: LocalDate,
        warehouseId: Long? = null,
        categoryId: Long? = null,
        keyword: String? = null
    ): StockLedgerTotalResponse {
        
        val toExclusive = toDate.plusDays(1)
        
        val baseSql = """
            WITH movements AS (
                SELECT t.transaction_id, t.completed_at AS tx_time, t.warehouse_id, d.product_id,
                       CASE WHEN t.transaction_type='IMPORT' THEN 'IMPORT'
                            WHEN t.transaction_type='EXPORT' THEN 'EXPORT'
                            WHEN t.transaction_type='TRANSFER' THEN 'TRANSFER_OUT'
                            ELSE 'ADJUST' END AS movement_kind,
                       CASE WHEN t.transaction_type='IMPORT' THEN d.quantity
                            WHEN t.transaction_type IN ('EXPORT','TRANSFER') THEN -d.quantity
                            WHEN t.transaction_type='STOCKTAKE' THEN COALESCE(d.difference_quantity,0)
                            WHEN t.transaction_type='ADJUSTMENT' THEN
                                 CASE WHEN t.transaction_subtype='SURPLUS' THEN d.quantity
                                      WHEN t.transaction_subtype IN ('SHORTAGE','DAMAGE') THEN -d.quantity
                                      ELSE COALESCE(d.difference_quantity,0) END
                       END AS delta
                FROM transactions t 
                JOIN transaction_details d ON d.transaction_id = t.transaction_id
                WHERE t.status = 'COMPLETED'
                UNION ALL
                SELECT t.transaction_id, t.completed_at, t.target_warehouse_id, d.product_id,
                       'TRANSFER_IN', d.quantity
                FROM transactions t 
                JOIN transaction_details d ON d.transaction_id = t.transaction_id
                WHERE t.status='COMPLETED' AND t.transaction_type='TRANSFER'
                  AND t.target_warehouse_id IS NOT NULL
            ),
            stock_now AS (
                SELECT product_id, warehouse_id, SUM(stock_quantity) AS current_qty
                FROM inventory 
                GROUP BY product_id, warehouse_id
            ),
            agg AS (
                SELECT m.product_id, m.warehouse_id,
                  SUM(CASE WHEN m.tx_time >= ? THEN m.delta ELSE 0 END) AS net_since_from,
                  SUM(CASE WHEN m.tx_time >= ? AND m.tx_time < ? AND m.movement_kind='IMPORT'       THEN m.delta  ELSE 0 END) AS import_qty,
                  SUM(CASE WHEN m.tx_time >= ? AND m.tx_time < ? AND m.movement_kind='EXPORT'       THEN -m.delta ELSE 0 END) AS export_qty,
                  SUM(CASE WHEN m.tx_time >= ? AND m.tx_time < ? AND m.movement_kind='TRANSFER_IN'  THEN m.delta  ELSE 0 END) AS transfer_in_qty,
                  SUM(CASE WHEN m.tx_time >= ? AND m.tx_time < ? AND m.movement_kind='TRANSFER_OUT' THEN -m.delta ELSE 0 END) AS transfer_out_qty,
                  SUM(CASE WHEN m.tx_time >= ? AND m.tx_time < ? AND m.movement_kind='ADJUST'       THEN m.delta  ELSE 0 END) AS adjust_net
                FROM movements m 
                GROUP BY m.product_id, m.warehouse_id
            ),
            pairs AS (
                SELECT product_id, warehouse_id FROM stock_now
                UNION
                SELECT product_id, warehouse_id FROM agg
            )
            SELECT 
                COALESCE(SUM(COALESCE(s.current_qty,0) - COALESCE(a.net_since_from,0)), 0) AS opening_qty,
                COALESCE(SUM(COALESCE(a.import_qty,0)), 0) AS import_qty,
                COALESCE(SUM(COALESCE(a.export_qty,0)), 0) AS export_qty,
                COALESCE(SUM(COALESCE(a.transfer_in_qty,0)), 0) AS transfer_in_qty,
                COALESCE(SUM(COALESCE(a.transfer_out_qty,0)), 0) AS transfer_out_qty,
                COALESCE(SUM(COALESCE(a.adjust_net,0)), 0) AS adjust_net,
                COALESCE(SUM(
                    COALESCE(s.current_qty,0) - COALESCE(a.net_since_from,0)
                    + COALESCE(a.import_qty,0) - COALESCE(a.export_qty,0)
                    + COALESCE(a.transfer_in_qty,0) - COALESCE(a.transfer_out_qty,0)
                    + COALESCE(a.adjust_net,0)
                ), 0) AS closing_qty
            FROM pairs k
            LEFT JOIN stock_now s ON s.product_id=k.product_id AND s.warehouse_id=k.warehouse_id
            LEFT JOIN agg a       ON a.product_id=k.product_id AND a.warehouse_id=k.warehouse_id
            JOIN products p   ON p.product_id = k.product_id
            JOIN warehouses w ON w.warehouse_id = k.warehouse_id AND w.is_active = TRUE
        """.trimIndent()
        
        val conditions = mutableListOf<String>()
        val filterParams = mutableListOf<Any>()
        
        warehouseId?.let {
            conditions.add("w.warehouse_id = ?")
            filterParams.add(it)
        }
        
        categoryId?.let {
            conditions.add("p.category_id = ?")
            filterParams.add(it)
        }
        
        keyword?.trim()?.takeIf { it.isNotBlank() }?.let {
            conditions.add("(p.sku LIKE ? OR p.product_name LIKE ?)")
            val searchValue = "%$it%"
            filterParams.add(searchValue)
            filterParams.add(searchValue)
        }
        
        val whereClause = if (conditions.isEmpty()) "" 
            else "WHERE ${conditions.joinToString(" AND ")}"
        
        val sql = "$baseSql $whereClause"
        
        return dataSource.connection.use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                bindDateParameters(stmt, fromDate, toExclusive)
                bindFilterParameters(stmt, 11, filterParams)
                
                stmt.executeQuery().use { rs ->
                    if (rs.next()) {
                        StockLedgerTotalResponse(
                            openingQty = rs.getBigDecimal("opening_qty"),
                            importQty = rs.getBigDecimal("import_qty"),
                            exportQty = rs.getBigDecimal("export_qty"),
                            transferInQty = rs.getBigDecimal("transfer_in_qty"),
                            transferOutQty = rs.getBigDecimal("transfer_out_qty"),
                            adjustNet = rs.getBigDecimal("adjust_net"),
                            closingQty = rs.getBigDecimal("closing_qty")
                        )
                    } else {
                        StockLedgerTotalResponse(
                            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
                        )
                    }
                }
            }
        }
    }
    
    /**
     * Bind date parameters (10 lần: fromDate và toExclusive).
     */
    private fun bindDateParameters(
        stmt: java.sql.PreparedStatement,
        fromDate: LocalDate,
        toExclusive: LocalDate
    ) {
        // CTE có 10 tham số date: fromDate (x5), toExclusive (x5)
        val fromStr = fromDate.toString()
        val toStr = toExclusive.toString()
        
        stmt.setString(1, fromStr)   // net_since_from
        stmt.setString(2, fromStr)   // import_qty check 1
        stmt.setString(3, toStr)     // import_qty check 2
        stmt.setString(4, fromStr)   // export_qty check 1
        stmt.setString(5, toStr)     // export_qty check 2
        stmt.setString(6, fromStr)   // transfer_in check 1
        stmt.setString(7, toStr)     // transfer_in check 2
        stmt.setString(8, fromStr)   // transfer_out check 1
        stmt.setString(9, toStr)     // transfer_out check 2
        stmt.setString(10, fromStr)  // adjust check 1
        stmt.setString(11, toStr)    // adjust check 2
    }
    
    /**
     * Bind filter parameters (warehouse, category, keyword).
     */
    private fun bindFilterParameters(
        stmt: java.sql.PreparedStatement,
        startIndex: Int,
        params: List<Any>
    ) {
        params.forEachIndexed { index, value ->
            when (value) {
                is Long -> stmt.setLong(startIndex + index, value)
                is String -> stmt.setString(startIndex + index, value)
            }
        }
    }
}
