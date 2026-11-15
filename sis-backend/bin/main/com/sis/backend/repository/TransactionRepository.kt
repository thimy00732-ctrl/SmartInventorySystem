package com.sis.backend.repository

import com.sis.backend.dto.*
import com.sis.backend.util.DeltaCalculator
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.sql.DataSource

/**
 * Repository xử lý truy vấn transactions (UC37, UC38).
 * 
 * @property dataSource Nguồn kết nối cơ sở dữ liệu.
 */
class TransactionRepository(
    private val dataSource: DataSource
) {
    
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    
    /**
     * Lấy lịch sử biến động với phân trang (UC37).
     * 
     * Mỗi dòng = 1 transaction_detail.
     * Không ép COMPLETED, mặc định mọi trạng thái.
     * 
     * @param productId Lọc theo sản phẩm.
     * @param warehouseId Lọc theo kho (source hoặc target).
     * @param type Transaction type (IMPORT, EXPORT, TRANSFER, STOCKTAKE, ADJUSTMENT).
     * @param status Transaction status (6 giá trị).
     * @param userId Lọc theo người tạo hoặc người duyệt.
     * @param fromDate Ngày bắt đầu (không bắt buộc).
     * @param toDate Ngày kết thúc (không bắt buộc).
     * @param sortBy Cột sắp xếp (whitelist: txTime, productName, quantity, transactionType).
     * @param sortDir ASC hoặc DESC.
     * @param page Trang hiện tại.
     * @param size Số dòng mỗi trang.
     * @return Danh sách movements và tổng số dòng.
     */
    fun getTransactionMovements(
        productId: Long? = null,
        warehouseId: Long? = null,
        type: String? = null,
        status: String? = null,
        userId: Long? = null,
        fromDate: LocalDate? = null,
        toDate: LocalDate? = null,
        sortBy: String = "txTime",
        sortDir: String = "DESC",
        page: Int = 1,
        size: Int = 10
    ): Pair<List<TransactionMovementResponse>, Long> {
        
        require(page >= 1) { "Page phải >= 1" }
        require(size in 1..100) { "Size phải từ 1-100" }
        
        val baseSql = """
            SELECT 
                t.transaction_id,
                t.transaction_type,
                t.transaction_subtype,
                t.status,
                COALESCE(t.completed_at, t.created_at) AS tx_time,
                d.product_id,
                p.sku,
                p.product_name,
                p.unit,
                d.batch_code,
                d.quantity,
                d.difference_quantity,
                t.warehouse_id,
                w.warehouse_name,
                t.target_warehouse_id,
                tw.warehouse_name AS target_warehouse_name,
                t.user_id AS created_by_id,
                u.full_name AS created_by_name
            FROM transaction_details d
            INNER JOIN transactions t ON d.transaction_id = t.transaction_id
            INNER JOIN products p ON d.product_id = p.product_id
            INNER JOIN warehouses w ON t.warehouse_id = w.warehouse_id
            LEFT JOIN warehouses tw ON t.target_warehouse_id = tw.warehouse_id
            LEFT JOIN users u ON t.user_id = u.user_id
        """.trimIndent()
        
        val conditions = mutableListOf<String>()
        val parameters = mutableListOf<Any>()
        
        productId?.let {
            conditions.add("d.product_id = ?")
            parameters.add(it)
        }
        
        warehouseId?.let {
            conditions.add("(t.warehouse_id = ? OR t.target_warehouse_id = ?)")
            parameters.add(it)
            parameters.add(it)
        }
        
        type?.let {
            conditions.add("t.transaction_type = ?")
            parameters.add(it)
        }
        
        status?.let {
            conditions.add("t.status = ?")
            parameters.add(it)
        }
        
        userId?.let {
            conditions.add("(t.user_id = ? OR t.approved_by = ?)")
            parameters.add(it)
            parameters.add(it)
        }
        
        fromDate?.let {
            conditions.add("COALESCE(t.completed_at, t.created_at) >= ?")
            parameters.add(it.toString())
        }
        
        toDate?.let {
            // Inclusive, không exclusive như UC36
            conditions.add("COALESCE(t.completed_at, t.created_at) <= ?")
            parameters.add(it.toString())
        }
        
        val whereClause = if (conditions.isEmpty()) "" 
            else "WHERE ${conditions.joinToString(" AND ")}"
        
        // Whitelist sort
        val allowedSortColumns = mapOf(
            "txTime" to "tx_time",
            "productName" to "p.product_name",
            "quantity" to "d.quantity",
            "transactionType" to "t.transaction_type"
        )
        
        val sortColumn = allowedSortColumns[sortBy] ?: "tx_time"
        val sortDirection = if (sortDir.uppercase() == "ASC") "ASC" else "DESC"
        
        // Count query
        val countSql = """
            SELECT COUNT(*) FROM (
                $baseSql
                $whereClause
            ) AS subquery
        """.trimIndent()
        
        // Data query
        val offset = (page - 1) * size
        val dataSql = """
            $baseSql
            $whereClause
            ORDER BY $sortColumn $sortDirection, t.transaction_id DESC
            LIMIT ? OFFSET ?
        """.trimIndent()
        
        return dataSource.connection.use { conn ->
            // Count
            val totalItems = conn.prepareStatement(countSql).use { stmt ->
                bindParameters(stmt, parameters)
                stmt.executeQuery().use { rs ->
                    if (rs.next()) rs.getLong(1) else 0L
                }
            }
            
            // Data
            val items = conn.prepareStatement(dataSql).use { stmt ->
                bindParameters(stmt, parameters)
                stmt.setInt(parameters.size + 1, size)
                stmt.setInt(parameters.size + 2, offset)
                
                stmt.executeQuery().use { rs ->
                    buildList {
                        while (rs.next()) {
                            val transactionType = rs.getString("transaction_type")
                            val transactionSubtype = rs.getString("transaction_subtype")
                            val quantity = rs.getBigDecimal("quantity")
                            val differenceQuantity = rs.getBigDecimal("difference_quantity")
                            val statusValue = rs.getString("status")
                            val warehouseName = rs.getString("warehouse_name")
                            val targetWarehouseName = rs.getString("target_warehouse_name")
                            
                            // Tính delta bằng DeltaCalculator
                            val delta = DeltaCalculator.calculate(
                                transactionType = transactionType,
                                adjustmentReason = transactionSubtype,
                                quantity = quantity,
                                differenceQuantity = differenceQuantity,
                                isSource = true
                            )
                            
                            // Movement label
                            val movementLabel = DeltaCalculator.getMovementLabel(
                                transactionType = transactionType,
                                adjustmentReason = transactionSubtype
                            )
                            
                            // affectsStock = true if COMPLETED
                            val affectsStock = (statusValue == "COMPLETED")
                            
                            val txTime = rs.getTimestamp("tx_time")?.toLocalDateTime()
                                ?.format(dateTimeFormatter) ?: ""
                            
                            add(
                                TransactionMovementResponse(
                                    transactionId = rs.getLong("transaction_id"),
                                    transactionType = transactionType,
                                    transactionSubtype = transactionSubtype,
                                    status = statusValue,
                                    txTime = txTime,
                                    productId = rs.getLong("product_id"),
                                    sku = rs.getString("sku"),
                                    productName = rs.getString("product_name"),
                                    unit = rs.getString("unit"),
                                    batchCode = rs.getString("batch_code"),
                                    quantity = quantity,
                                    differenceQuantity = differenceQuantity,
                                    warehouseId = rs.getLong("warehouse_id"),
                                    warehouseName = warehouseName,
                                    targetWarehouseId = rs.getLong("target_warehouse_id").let {
                                        if (rs.wasNull()) null else it
                                    },
                                    targetWarehouseName = targetWarehouseName,
                                    createdById = rs.getLong("created_by_id").let {
                                        if (rs.wasNull()) null else it
                                    },
                                    createdByName = rs.getString("created_by_name"),
                                    delta = delta,
                                    movementLabel = movementLabel,
                                    affectsStock = affectsStock
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
     * Lấy chi tiết giao dịch theo ID (UC38).
     * 
     * @param transactionId ID giao dịch.
     * @return Chi tiết giao dịch hoặc null nếu không tồn tại.
     */
    fun getTransactionDetail(transactionId: Long): TransactionDetailResponse? {
        // Query header
        val headerSql = """
            SELECT 
                t.transaction_id,
                t.transaction_type,
                t.transaction_subtype,
                t.status,
                t.note,
                t.rejection_reason,
                t.created_at,
                t.submitted_at,
                t.approved_at,
                t.completed_at,
                t.cancelled_at,
                t.warehouse_id,
                w.warehouse_name,
                t.target_warehouse_id,
                tw.warehouse_name AS target_warehouse_name,
                t.supplier_id,
                s.supplier_name,
                t.user_id AS created_by_id,
                u1.full_name AS created_by_name,
                t.approved_by AS approved_by_id,
                u2.full_name AS approved_by_name
            FROM transactions t
            INNER JOIN warehouses w ON t.warehouse_id = w.warehouse_id
            LEFT JOIN warehouses tw ON t.target_warehouse_id = tw.warehouse_id
            LEFT JOIN suppliers s ON t.supplier_id = s.supplier_id
            LEFT JOIN users u1 ON t.user_id = u1.user_id
            LEFT JOIN users u2 ON t.approved_by = u2.user_id
            WHERE t.transaction_id = ?
        """.trimIndent()
        
        // Query details
        val detailsSql = """
            SELECT 
                d.detail_id,
                d.product_id,
                p.sku,
                p.product_name,
                p.unit,
                d.batch_code,
                d.manufacture_date,
                d.expiry_date,
                d.quantity,
                d.unit_price,
                d.system_quantity,
                d.actual_quantity,
                d.difference_quantity,
                d.note
            FROM transaction_details d
            INNER JOIN products p ON d.product_id = p.product_id
            WHERE d.transaction_id = ?
            ORDER BY d.detail_id
        """.trimIndent()
        
        return dataSource.connection.use { conn ->
            // Get header
            val header = conn.prepareStatement(headerSql).use { stmt ->
                stmt.setLong(1, transactionId)
                stmt.executeQuery().use { rs ->
                    if (!rs.next()) return@use null
                    
                    mapOf(
                        "transactionId" to rs.getLong("transaction_id"),
                        "transactionType" to rs.getString("transaction_type"),
                        "transactionSubtype" to rs.getString("transaction_subtype"),
                        "status" to rs.getString("status"),
                        "note" to rs.getString("note"),
                        "rejectionReason" to rs.getString("rejection_reason"),
                        "createdAt" to rs.getTimestamp("created_at")?.toLocalDateTime()?.format(dateTimeFormatter),
                        "submittedAt" to rs.getTimestamp("submitted_at")?.toLocalDateTime()?.format(dateTimeFormatter),
                        "approvedAt" to rs.getTimestamp("approved_at")?.toLocalDateTime()?.format(dateTimeFormatter),
                        "completedAt" to rs.getTimestamp("completed_at")?.toLocalDateTime()?.format(dateTimeFormatter),
                        "cancelledAt" to rs.getTimestamp("cancelled_at")?.toLocalDateTime()?.format(dateTimeFormatter),
                        "warehouseId" to rs.getLong("warehouse_id"),
                        "warehouseName" to rs.getString("warehouse_name"),
                        "targetWarehouseId" to rs.getLong("target_warehouse_id").let { if (rs.wasNull()) null else it },
                        "targetWarehouseName" to rs.getString("target_warehouse_name"),
                        "supplierId" to rs.getLong("supplier_id").let { if (rs.wasNull()) null else it },
                        "supplierName" to rs.getString("supplier_name"),
                        "createdById" to rs.getLong("created_by_id").let { if (rs.wasNull()) null else it },
                        "createdByName" to rs.getString("created_by_name"),
                        "approvedById" to rs.getLong("approved_by_id").let { if (rs.wasNull()) null else it },
                        "approvedByName" to rs.getString("approved_by_name")
                    )
                }
            } ?: return@use null
            
            // Get details
            val details = conn.prepareStatement(detailsSql).use { stmt ->
                stmt.setLong(1, transactionId)
                stmt.executeQuery().use { rs ->
                    buildList {
                        while (rs.next()) {
                            val unitPrice = rs.getBigDecimal("unit_price")
                            val quantity = rs.getBigDecimal("quantity")
                            val lineValue = if (unitPrice != null && quantity != null) {
                                unitPrice.multiply(quantity)
                            } else {
                                null
                            }
                            
                            add(
                                TransactionDetailItemResponse(
                                    detailId = rs.getLong("detail_id"),
                                    productId = rs.getLong("product_id"),
                                    sku = rs.getString("sku"),
                                    productName = rs.getString("product_name"),
                                    unit = rs.getString("unit"),
                                    batchCode = rs.getString("batch_code"),
                                    manufactureDate = rs.getDate("manufacture_date")?.toLocalDate()?.toString(),
                                    expiryDate = rs.getDate("expiry_date")?.toLocalDate()?.toString(),
                                    warehouseId = rs.getLong("warehouse_id"),
                                    warehouseName = rs.getString("warehouse_name"),
                                    targetWarehouseId = rs.getLong("target_warehouse_id").let { if (rs.wasNull()) null else it },
                                    targetWarehouseName = rs.getString("target_warehouse_name"),
                                    quantity = quantity,
                                    unitPrice = unitPrice,
                                    lineValue = lineValue,
                                    systemQuantity = rs.getBigDecimal("system_quantity"),
                                    actualQuantity = rs.getBigDecimal("actual_quantity"),
                                    differenceQuantity = rs.getBigDecimal("difference_quantity"),
                                    note = rs.getString("note")
                                )
                            )
                        }
                    }
                }
            }
            
            // Calculate summary
            val totalLines = details.size
            val totalQuantity = details.mapNotNull { it.quantity }.fold(BigDecimal.ZERO) { acc, qty -> acc.add(qty) }
            
            val transactionType = header["transactionType"] as String
            val totalValue = if (transactionType in listOf("IMPORT", "EXPORT")) {
                details.mapNotNull { it.lineValue }.fold(BigDecimal.ZERO) { acc, value -> acc.add(value) }
            } else {
                null
            }
            
            val summary = TransactionSummaryResponse(
                totalLines = totalLines,
                totalQuantity = totalQuantity,
                totalValue = totalValue
            )
            
            // Chỉ trả rejectionReason nếu status = REJECTED
            val rejectionReason = if (header["status"] == "REJECTED") {
                header["rejectionReason"] as String?
            } else {
                null
            }
            
            TransactionDetailResponse(
                transactionId = header["transactionId"] as Long,
                transactionType = header["transactionType"] as String,
                transactionSubtype = header["transactionSubtype"] as String?,
                status = header["status"] as String,
                note = header["note"] as String?,
                rejectionReason = rejectionReason,
                createdAt = header["createdAt"] as String? ?: "",
                submittedAt = header["submittedAt"] as String?,
                approvedAt = header["approvedAt"] as String?,
                completedAt = header["completedAt"] as String?,
                cancelledAt = header["cancelledAt"] as String?,
                warehouseId = header["warehouseId"] as Long,
                warehouseName = header["warehouseName"] as String,
                targetWarehouseId = header["targetWarehouseId"] as Long?,
                targetWarehouseName = header["targetWarehouseName"] as String?,
                supplierId = header["supplierId"] as Long?,
                supplierName = header["supplierName"] as String?,
                createdById = header["createdById"] as Long?,
                createdByName = header["createdByName"] as String?,
                approvedById = header["approvedById"] as Long?,
                approvedByName = header["approvedByName"] as String?,
                details = details,
                summary = summary
            )
        }
    }
    
    private fun bindParameters(stmt: java.sql.PreparedStatement, params: List<Any>) {
        params.forEachIndexed { index, value ->
            when (value) {
                is Long -> stmt.setLong(index + 1, value)
                is String -> stmt.setString(index + 1, value)
            }
        }
    }
}
