# SPEC: Backend Tồn kho, Cảnh báo, Giá trị, Sổ kho và Truy vết (UC29 – UC39)

Quy ước chung, stack, cấu trúc thư mục và quy tắc code: xem `CLAUDE.md`. Schema: xem `docs/schema.sql` (là nguồn sự thật, không chép lại trong file này).

## 1. Mục tiêu và phạm vi

Làm API backend cho 11 use case UC29–UC39. Tất cả API là **chỉ đọc**, trừ hai thao tác ghi:
- `reserved_quantity` của `inventory` (UC31).
- `min_stock`, `max_stock` của `products` (UC33).

**Không có API nào ghi vào `stock_quantity`.**

Ngoài phạm vi: API dropdown lọc (kho, danh mục, nhà cung cấp, người dùng), CRUD kho, sửa/xóa phiếu giao dịch.

## 2. Danh sách API

| UC | Endpoint | Quyền (mặc định) |
|---|---|---|
| 29 | `GET /api/inventory` | ADMIN, STAFF |
| 30 | `GET /api/inventory/{id}` | ADMIN, STAFF |
| 30, 34, 35 | `GET /api/inventory/by-product/{productId}` | ADMIN, STAFF |
| 31 | `PATCH /api/inventory/{id}/reserved` | ADMIN, STAFF |
| 32 | (dùng cột sinh `available_quantity`, không có endpoint riêng) | — |
| 33 | `GET /api/products/thresholds` | ADMIN, STAFF |
| 33 | `PATCH /api/products/{id}/thresholds` | **ADMIN** |
| 34 | `GET /api/inventory/alerts/low-stock` | ADMIN, STAFF |
| 35 | `GET /api/inventory/alerts/over-stock` | ADMIN, STAFF |
| 34, 35 | `GET /api/inventory/alerts/summary` | ADMIN, STAFF |
| 36 | `GET /api/stock-ledger` | ADMIN, STAFF |
| 37 | `GET /api/transactions/movements` | ADMIN, STAFF |
| 38 | `GET /api/transactions/{id}` | ADMIN, STAFF |
| 39 | `GET /api/inventory/valuation` và `/valuation/summary` | **ADMIN** |

Chưa đăng nhập → 401. Thiếu quyền → 403. Không có bảng phân kho cho nhân viên, nên staff thấy mọi kho đang hoạt động.

`routes/ApiRoutes.kt` chứa hằng số đường dẫn. Parse `id` bằng `toIntOrNull()`, sai → 400. Kiểm tra `/api/products/thresholds` không xung đột với `/api/products/{id}` của Sprint 1.

## 3. Thứ tự làm việc

Mỗi đợt xong phải **viết file test theo mục 11** và build, test xanh mới sang đợt sau. Đợt nào không có file test đi kèm thì coi như chưa xong.

| Đợt | UC | Ghi chú |
|---|---|---|
| 1 | 29, 30, 31, 32 | Nền: entity bảng, enum, `PageResponse`, `ApiRoutes`, `StatusPages`, `RoleAuthorization` (nếu chưa có) |
| 2 | 33, 34, 35 | Ngưỡng và cảnh báo (gom nhóm theo sản phẩm) |
| 3 | 39 | Giá trị tồn kho |
| 4 | 36, 37, 38 | Phụ thuộc quyết định ở mục 9; dùng chung hàm tính `delta` |

Phụ thuộc: đợt 4 dùng lại bảng `inventory`, `products`, `warehouses` đã khai báo ở đợt 1.

## 4. Quy ước dữ liệu chung

- Chỉ tính kho `warehouses.is_active = TRUE`. Với cảnh báo và ngưỡng, chỉ tính sản phẩm `products.is_active = TRUE`.
- `batch_code = ''` nghĩa là "không theo lô". Response có cờ `noBatch`.
- Tìm kiếm `keyword`: `sku`, `product_name` (và `barcode` ở UC29), LIKE `%kw%`, escape ký tự đặc biệt.
- Phân trang: `{ items, page, size, totalElements, totalPages }`, mặc định `size = 10`, tối đa `100`. Mọi danh sách có query `COUNT` cùng điều kiện.
- Sort chỉ qua whitelist, mặc định ghi ở từng UC.
- Tổng cộng (ledger, valuation) phải lấy từ query tổng riêng, **không cộng theo trang**.
- Ngưỡng cận hạn: `inventory.nearExpiryDays = 30` trong `application.conf`.

## 5. Đặc tả và tiêu chí nghiệm thu từng UC

### UC29: Danh sách tồn kho
`GET /api/inventory?keyword&warehouseId&categoryId&stockStatus&sortBy&sortDir&page&size`

- `stockStatus`: `ALL` (mặc định) | `IN_STOCK` (`stock_quantity > 0`) | `OUT` (`= 0`) | `RESERVED` (`reserved_quantity > 0`).
- Mỗi dòng là một tổ hợp kho/sản phẩm/lô: `inventoryId, productId, imageUrl, sku, productName, unit, warehouseId, warehouseName, batchCode, noBatch, expiryDate, expiryStatus, stockQuantity, reservedQuantity, availableQuantity`.
- `expiryStatus`: `NONE` (không có hạn) | `EXPIRED` | `NEAR_EXPIRY` | `OK`, tính ở BE.
- Whitelist sort: `productName`, `stockQuantity`, `expiryDate`. Mặc định `product_name ASC`.
- JOIN: `inventory` INNER `products`, `warehouses`; LEFT `categories`.

Nghiệm thu:
- [ ] Mỗi bộ lọc hoạt động riêng và kết hợp được.
- [ ] Kho `is_active = FALSE` không xuất hiện.
- [ ] `sortBy` ngoài whitelist không gây lỗi SQL (dùng mặc định hoặc 400).
- [ ] `keyword` chứa `%` hoặc `_` không khớp lung tung.
- [ ] `totalElements` và `totalPages` đúng khi lọc.
- [ ] Bộ lọc `OUT` chỉ trả dòng inventory có tồn 0 (không trả sản phẩm chưa từng nhập).

### UC30: Chi tiết tồn kho
`GET /api/inventory/{id}`: dòng tồn (`batchCode, manufactureDate, expiryDate, stockQuantity, reservedQuantity, availableQuantity, updatedAt`), kho, sản phẩm (`productId, sku, barcode, productName, imageUrl, unit, minStock, maxStock, isActive`), `categoryName`, `supplierName` (LEFT JOIN).

`GET /api/inventory/by-product/{productId}`: các dòng inventory của sản phẩm ở kho đang hoạt động, sắp `stock_quantity DESC`. Response có `items` và dòng tổng (`totalStock, totalReserved, totalAvailable`) **tính ở service**.

Nghiệm thu:
- [ ] `id` không tồn tại → 404.
- [ ] `productId` không tồn tại → 404.
- [ ] Sản phẩm không có dòng tồn: `items` rỗng, tổng bằng 0.
- [ ] Dòng tổng bằng tổng các dòng trả về.
- [ ] Có `manufactureDate` và `expiryDate`; `categoryName`/`supplierName` null khi sản phẩm không có.

### UC31: Giữ chỗ
`PATCH /api/inventory/{id}/reserved`, body `{ "reservedQuantity": n }`.

Trong **một transaction**:
1. Khóa dòng bằng `forUpdate()` (tương đương `SELECT ... FOR UPDATE`).
2. Không tồn tại → 404.
3. Kiểm tra `0 <= n <= stock_quantity`; vi phạm → 400 với thông báo nêu rõ lý do (âm, hoặc vượt tồn hiện tại là bao nhiêu).
4. `UPDATE inventory SET reserved_quantity = n WHERE inventory_id = :id`.
5. Đọc lại và trả `inventoryId, stockQuantity, reservedQuantity, availableQuantity, updatedAt`.

Không có trường "ghi chú lý do": body có trường lạ thì bỏ qua, không lưu. Không ghi `available_quantity`. Nếu DB vẫn báo vi phạm CHECK thì bắt và trả 400.

Nghiệm thu:
- [ ] `n = -1` → 400.
- [ ] `n = stock + 1` → 400, thông báo có số tồn hiện tại.
- [ ] `n = 0` và `n = stock` đều hợp lệ.
- [ ] `available_quantity` trong response = `stock - n`.
- [ ] Hai request song song trên cùng dòng không làm hỏng dữ liệu.
- [ ] Không có câu lệnh nào ghi cột `available_quantity` (test hoặc kiểm tra code).

### UC32: Số lượng khả dụng
Dùng cột sinh `available_quantity = stock_quantity - reserved_quantity`, chỉ SELECT.

Nghiệm thu:
- [ ] Mọi response liên quan (UC29, 30, 31) có `availableQuantity` lấy từ cột này.
- [ ] Không tự tính lại ở service, không ghi vào cột này.

### UC33: Thiết lập ngưỡng
`GET /api/products/thresholds?keyword&categoryId&page&size`: sản phẩm `is_active = TRUE`, trả `productId, sku, productName, unit, minStock, maxStock, totalStock, alertLevel`. `totalStock` chỉ cộng kho đang hoạt động (LEFT JOIN, sản phẩm chưa có tồn vẫn xuất hiện với 0).

`alertLevel`: `totalStock = 0` và `< min` → `OUT_OF_STOCK`; `< min` → `LOW`; `> max` → `OVER`; còn lại `NORMAL`.

`PATCH /api/products/{id}/thresholds`, body `{ "minStock", "maxStock" }`: chỉ cập nhật `min_stock`, `max_stock`. Response `productId, minStock, maxStock, totalStock, alertLevel`. Không sửa API cập nhật sản phẩm (UC11) của Sprint 1.

Nghiệm thu:
- [ ] `minStock < 0` → 400.
- [ ] `maxStock < minStock` → 400, thông báo rõ.
- [ ] `minStock = maxStock` hợp lệ.
- [ ] Sản phẩm không tồn tại → 404; sản phẩm `is_active = FALSE` → 400.
- [ ] Response trả đúng `alertLevel` mới.
- [ ] STAFF gọi PATCH → 403.

### UC34: Cảnh báo tồn thấp
`GET /api/inventory/alerts/low-stock?categoryId&supplierId&keyword&page&size`

- Gom theo sản phẩm, `total_stock = COALESCE(SUM(stock_quantity), 0)` từ kho đang hoạt động, `HAVING total_stock < min_stock`.
- Trả: `productId, sku, productName, unit, supplierId, supplierName, minStock, totalStock, missingQty (= min - total), level`. `level = OUT_OF_STOCK` khi `total_stock = 0`, ngược lại `LOW`.
- Sắp `missingQty DESC`.
- "Phạm vi kho đã quy định" = tất cả kho đang hoạt động. Phân bổ theo kho lấy từ `by-product`.

Nghiệm thu:
- [ ] Sản phẩm chưa có dòng inventory nào vẫn xuất hiện (tổng 0, `OUT_OF_STOCK`).
- [ ] Sản phẩm bằng đúng ngưỡng (`total = min`) **không** xuất hiện.
- [ ] Tồn ở kho không hoạt động không được cộng.
- [ ] Lọc `supplierId`, `categoryId`, `keyword` đúng.
- [ ] Thứ tự `missingQty` giảm dần.

### UC35: Cảnh báo vượt mức
`GET /api/inventory/alerts/over-stock?categoryId&keyword&page&size`

- Gom theo sản phẩm, `HAVING SUM(stock_quantity) > max_stock`.
- Trả: `productId, sku, productName, unit, maxStock, totalStock, excessQty, excessPercent`.
- `excessPercent = ROUND((SUM(stock) / NULLIF(max_stock, 0) - 1) * 100, 1)`. Khi `max_stock = 0` trả `null`, không lỗi chia cho 0.
- Sắp `excessQty DESC`. Kho bị ảnh hưởng xem qua `by-product`.

Nghiệm thu:
- [ ] `max_stock = 0` không gây lỗi, `excessPercent = null`.
- [ ] Sản phẩm bằng đúng `max_stock` không xuất hiện.
- [ ] `excessQty` và `excessPercent` đúng với ví dụ tính tay.

### Tổng quan cảnh báo (UC34, 35)
`GET /api/inventory/alerts/summary` trả `lowCount`, `outOfStockCount`, `overCount` bằng **một query**:

```sql
WITH totals AS (
    SELECT p.product_id, p.min_stock, p.max_stock,
           COALESCE(SUM(i.stock_quantity), 0) AS total_stock
    FROM products p
    LEFT JOIN (inventory i INNER JOIN warehouses w
               ON w.warehouse_id = i.warehouse_id AND w.is_active = TRUE)
           ON i.product_id = p.product_id
    WHERE p.is_active = TRUE
    GROUP BY p.product_id, p.min_stock, p.max_stock
)
SELECT COALESCE(SUM(total_stock < min_stock), 0)                      AS low_count,
       COALESCE(SUM(total_stock < min_stock AND total_stock = 0), 0)  AS out_of_stock_count,
       COALESCE(SUM(total_stock > max_stock), 0)                      AS over_count
FROM totals;
```

Nghiệm thu:
- [ ] `lowCount` đã bao gồm `outOfStockCount`.
- [ ] Số liệu khớp với số dòng của hai API danh sách cảnh báo (cùng dữ liệu mẫu).
- [ ] Bảng rỗng trả 0, không null.

### UC36: Sổ kho
`GET /api/stock-ledger?fromDate&toDate&warehouseId&categoryId&keyword&page&size`

- `fromDate`, `toDate` bắt buộc (`yyyy-MM-dd`). `fromDate > toDate` hoặc khoảng > 366 ngày → 400. BE đổi `toExclusive = toDate + 1 ngày`.
- Chỉ phiếu `status = 'COMPLETED'`, mốc thời gian `completed_at`.
- Gom theo sản phẩm và kho (không tách lô).
- Tồn đầu kỳ **tính ngược từ tồn hiện tại**: `opening = current_qty - tổng delta từ fromDate đến nay`; `closing = opening + tổng delta trong kỳ`.

Quy tắc `delta` (dùng chung với UC37, viết **một hàm duy nhất**):

| Loại | Kho tác động | delta | movement_kind |
|---|---|---|---|
| IMPORT | `warehouse_id` | `+quantity` | IMPORT |
| EXPORT | `warehouse_id` | `-quantity` | EXPORT |
| TRANSFER | `warehouse_id` (nguồn) | `-quantity` | TRANSFER_OUT |
| TRANSFER | `target_warehouse_id` (đích) | `+quantity` | TRANSFER_IN |
| STOCKTAKE | `warehouse_id` | `COALESCE(difference_quantity, 0)` | ADJUST |
| ADJUSTMENT + SURPLUS | `warehouse_id` | `+quantity` | ADJUST |
| ADJUSTMENT + SHORTAGE hoặc DAMAGE | `warehouse_id` | `-quantity` | ADJUST |
| ADJUSTMENT + OTHER | `warehouse_id` | `COALESCE(difference_quantity, 0)` | ADJUST |

SQL chuẩn (chạy bằng `exec(sql, args)` với tham số ràng buộc; đổi `:param` sang `?` theo thứ tự hoặc dùng helper đặt tên tham số của dự án):

```sql
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
    FROM transactions t JOIN transaction_details d ON d.transaction_id = t.transaction_id
    WHERE t.status = 'COMPLETED'
    UNION ALL
    SELECT t.transaction_id, t.completed_at, t.target_warehouse_id, d.product_id,
           'TRANSFER_IN', d.quantity
    FROM transactions t JOIN transaction_details d ON d.transaction_id = t.transaction_id
    WHERE t.status='COMPLETED' AND t.transaction_type='TRANSFER'
      AND t.target_warehouse_id IS NOT NULL
),
stock_now AS (
    SELECT product_id, warehouse_id, SUM(stock_quantity) AS current_qty
    FROM inventory GROUP BY product_id, warehouse_id
),
agg AS (
    SELECT m.product_id, m.warehouse_id,
      SUM(CASE WHEN m.tx_time >= :fromDate THEN m.delta ELSE 0 END) AS net_since_from,
      SUM(CASE WHEN m.tx_time >= :fromDate AND m.tx_time < :toExclusive AND m.movement_kind='IMPORT'       THEN m.delta  ELSE 0 END) AS import_qty,
      SUM(CASE WHEN m.tx_time >= :fromDate AND m.tx_time < :toExclusive AND m.movement_kind='EXPORT'       THEN -m.delta ELSE 0 END) AS export_qty,
      SUM(CASE WHEN m.tx_time >= :fromDate AND m.tx_time < :toExclusive AND m.movement_kind='TRANSFER_IN'  THEN m.delta  ELSE 0 END) AS transfer_in_qty,
      SUM(CASE WHEN m.tx_time >= :fromDate AND m.tx_time < :toExclusive AND m.movement_kind='TRANSFER_OUT' THEN -m.delta ELSE 0 END) AS transfer_out_qty,
      SUM(CASE WHEN m.tx_time >= :fromDate AND m.tx_time < :toExclusive AND m.movement_kind='ADJUST'       THEN m.delta  ELSE 0 END) AS adjust_net
    FROM movements m GROUP BY m.product_id, m.warehouse_id
),
pairs AS (
    SELECT product_id, warehouse_id FROM stock_now
    UNION
    SELECT product_id, warehouse_id FROM agg
)
SELECT w.warehouse_id, w.warehouse_name, p.product_id, p.sku, p.product_name, p.unit,
       COALESCE(s.current_qty,0) - COALESCE(a.net_since_from,0) AS opening_qty,
       COALESCE(a.import_qty,0) AS import_qty, COALESCE(a.export_qty,0) AS export_qty,
       COALESCE(a.transfer_in_qty,0) AS transfer_in_qty, COALESCE(a.transfer_out_qty,0) AS transfer_out_qty,
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
WHERE (:warehouseId IS NULL OR w.warehouse_id = :warehouseId)
  AND (:categoryId  IS NULL OR p.category_id  = :categoryId)
  AND (:keyword IS NULL OR p.sku LIKE CONCAT('%',:keyword,'%') OR p.product_name LIKE CONCAT('%',:keyword,'%'))
ORDER BY p.product_name, w.warehouse_name
LIMIT :size OFFSET :offset;
```

Viết thêm query `COUNT(*)` và query **tổng toàn bộ kết quả lọc** (opening, nhập, xuất, chuyển vào, chuyển ra, điều chỉnh, closing). Response: `items`, `total`, phân trang, `note = "Tính từ các phiếu đã hoàn tất; tồn đầu kỳ suy ngược từ tồn hiện tại"`.

Nghiệm thu:
- [ ] Khi `toDate` là hôm nay, `closing_qty` bằng tồn hiện tại của cặp sản phẩm/kho.
- [ ] Phiếu `TRANSFER` sinh đúng 2 dòng: trừ ở kho nguồn, cộng ở kho đích.
- [ ] Phiếu không `COMPLETED` (DRAFT, PENDING, APPROVED, REJECTED, CANCELLED) không được tính.
- [ ] `STOCKTAKE` và `ADJUSTMENT` (SURPLUS, SHORTAGE, DAMAGE, OTHER) cộng vào `adjust_net` đúng dấu.
- [ ] `fromDate > toDate` và khoảng > 366 ngày → 400; thiếu `fromDate`/`toDate` → 400.
- [ ] Dòng tổng không phụ thuộc trang đang xem.
- [ ] Cặp sản phẩm/kho có biến động nhưng không có dòng inventory vẫn xuất hiện.

### UC37: Lịch sử biến động
`GET /api/transactions/movements?productId&warehouseId&type&status&userId&fromDate&toDate&sortBy&sortDir&page&size`

- **Không ép `COMPLETED`**, mặc định mọi trạng thái. `type` thuộc 5 giá trị `TransactionType`, `status` thuộc 6 giá trị `TransactionStatus`; sai → 400.
- Mỗi dòng kết quả là **một dòng sản phẩm trong phiếu**.
- `tx_time = COALESCE(completed_at, created_at)` dùng cho lọc và sắp xếp.
- Lọc kho: `warehouse_id = :w OR target_warehouse_id = :w`. Lọc người: `user_id = :u OR approved_by = :u`.
- `fromDate`/`toDate` không bắt buộc; `fromDate > toDate` → 400.
- Trả: `transactionId, transactionType, transactionSubtype, status, txTime, productId, sku, productName, unit, batchCode, quantity, differenceQuantity, warehouseId, warehouseName, targetWarehouseId, targetWarehouseName, createdById, createdByName, delta, movementLabel, affectsStock`.
- `delta` tính bằng **cùng hàm** của UC36. `movementLabel` của chuyển kho: "kho nguồn → kho đích". `affectsStock = (status == COMPLETED)`.
- Whitelist sort: `txTime`, `productName`, `quantity`, `transactionType`. Mặc định `txTime DESC`.
- JOIN: `transaction_details` INNER `transactions`, `products`, `warehouses`; LEFT `warehouses` (alias kho đích), LEFT `users` (người lập).

Nghiệm thu:
- [ ] Phiếu nhiều sản phẩm hiện nhiều dòng.
- [ ] Lọc theo từng tham số và kết hợp đúng.
- [ ] Phiếu chuyển kho hiện ở cả kho nguồn lẫn kho đích khi lọc theo kho.
- [ ] `type`/`status` sai giá trị → 400.
- [ ] Phiếu chưa `COMPLETED` có `affectsStock = false`.
- [ ] `sortBy` ngoài whitelist không gây lỗi SQL.
- [ ] `delta` của UC37 khớp với UC36 cho cùng phiếu.

### UC38: Truy vết giao dịch
`GET /api/transactions/{id}`

- Phiếu: `transactionId, transactionType, transactionSubtype, status, note, rejectionReason, createdAt, submittedAt, approvedAt, completedAt, cancelledAt`, kho, kho đích, nhà cung cấp, người lập (`users.user_id`), người duyệt (`approved_by`). Các trường duyệt/hủy có thể null → LEFT JOIN; `users` join hai lần (alias).
- Các dòng: `detailId, productId, sku, productName, unit, batchCode, manufactureDate, expiryDate, quantity, unitPrice, lineValue (= quantity * unit_price), systemQuantity, actualQuantity, differenceQuantity, note`, sắp `detail_id`.
- Tóm tắt (service, `BigDecimal`): tổng số dòng, tổng số lượng, tổng giá trị (**chỉ IMPORT và EXPORT**, loại khác `null`).
- `rejectionReason` chỉ trả khi `status = REJECTED`.
- `unit_price` là giá tại thời điểm lập phiếu, không lấy `products.cost_price` hiện tại.

Nghiệm thu:
- [ ] `id` không tồn tại → 404.
- [ ] Phiếu chưa duyệt: `approvedBy*` và `approvedAt` null, không lỗi.
- [ ] Phiếu `REJECTED` có `rejectionReason`; phiếu khác thì null.
- [ ] Phiếu `STOCKTAKE` trả `systemQuantity, actualQuantity, differenceQuantity`.
- [ ] Tổng giá trị bằng tổng `lineValue` với IMPORT/EXPORT, null với loại khác.
- [ ] Phiếu `TRANSFER` có kho đích; phiếu `IMPORT` có nhà cung cấp (nếu có).

### UC39: Giá trị tồn kho
Chỉ ADMIN. Giá trị = `stock_quantity * cost_price` hiện tại, chỉ kho đang hoạt động.

- `GET /api/inventory/valuation/summary?warehouseId&categoryId` → `totalValue, totalQty, productCount` (đếm `DISTINCT product_id` có `stock_quantity > 0`) và danh sách `warehouseValue` theo từng kho (sắp giảm dần; chỉ lọc `categoryId`, không lọc `warehouseId`).
- `GET /api/inventory/valuation?warehouseId&categoryId&keyword&page&size` → `items` (`warehouseId, warehouseName, productId, sku, productName, totalQty, currentCostPrice, stockValue`), gom theo kho + sản phẩm, `HAVING SUM(stock_quantity) > 0`, sắp `stockValue DESC`; kèm `total` lấy từ query summary và `note = "Tính theo giá vốn hiện tại, không phản ánh giá vốn lịch sử"`.
- Tiền: `BigDecimal`, scale 2, `HALF_UP`. Tổng rỗng trả `BigDecimal.ZERO`.

Nghiệm thu:
- [ ] STAFF gọi → 403.
- [ ] `totalValue` bằng tổng các dòng của bảng chi tiết khi không phân trang.
- [ ] Kho `is_active = FALSE` không tính.
- [ ] Dòng tổng không đổi khi chuyển trang.
- [ ] Không có sai số làm tròn kiểu `double` (kiểm tra bằng số liệu dễ lệch như 0.1 + 0.2).
- [ ] Dòng tồn 0 không xuất hiện trong bảng chi tiết.

## 6. Xử lý lỗi

- `ErrorCode` (enum, mỗi mã kèm HTTP status): `RESOURCE_NOT_FOUND` (404), `INVALID_RESERVED_QUANTITY` (400), `INVALID_THRESHOLD` (400), `INVALID_DATE_RANGE` (400), `VALIDATION_ERROR` (400), `DATA_INTEGRITY` (400), `UNAUTHORIZED` (401), `FORBIDDEN` (403), `INTERNAL_ERROR` (500).
- `ErrorResponse`: `timestamp, status, code, message, path, details`.
- `StatusPages` xử lý: `AppException`, body sai định dạng (400), tham số sai kiểu hoặc sai enum (400), vi phạm ràng buộc DB (SQLState `23000` hoặc mã 3819 → 400), `Throwable` chung (500, log chi tiết nhưng không lộ ra client).
- Nếu Sprint 1 đã cài `StatusPages` thì bổ sung handler vào đó, không cài lần hai.

## 7. Index

Đã có sẵn trong schema: `uq_inventory_batch`, `idx_inventory_product`, `idx_inventory_expiry`, `idx_transaction_type_status`, `idx_transaction_created`, `idx_transaction_warehouse`, `idx_detail_product`. Cần thêm (đã có trong `docs/schema.sql`, phần patch cuối file):

```sql
CREATE INDEX idx_transaction_status_completed ON transactions (status, completed_at);
```

Sinh vài nghìn dòng dữ liệu mẫu và kiểm tra thời gian phản hồi của alerts, valuation, ledger.

## 8. Dữ liệu mẫu cho test

Cần có: nhiều kho (có một kho `is_active = FALSE`), sản phẩm có và không theo lô, sản phẩm tồn thấp, hết hàng, vượt mức, sản phẩm có `max_stock = 0`, đủ 5 loại giao dịch, đủ 6 trạng thái, ít nhất một phiếu chuyển kho, một phiếu kiểm kê có chênh lệch, một phiếu `REJECTED` có lý do, một phiếu chưa duyệt.

## 9. Quyết định mặc định (chờ PO chốt)

**⚠️ CẬP NHẬT: Các quyết định sau đã được PO chốt cho Đợt 1 (UC29-32)**

Code theo mặc định dưới đây. Mỗi quyết định nằm ở một chỗ duy nhất (`RolePolicy`, `application.conf`, hoặc hàm tính delta) để đổi mà không sửa nhiều file.

| # | Nội dung | Quyết định | Trạng thái |
|---|---|---|---|
| 1 | `ADJUSTMENT` + `OTHER` tăng hay giảm | Dùng `difference_quantity`, không có thì 0 | Chờ PO |
| 2 | `STOCKTAKE` khi hoàn tất, tồn đổi thế nào | Tồn đổi đúng bằng `difference_quantity` | Chờ PO |
| 3 | Tồn đổi ở `APPROVED` hay `COMPLETED` | Chỉ `COMPLETED` | Chờ PO |
| 4 | Mốc ghi sổ | `completed_at` | Chờ PO |
| 5 | STAFF được `PATCH reserved`? | **Được** (ADMIN và STAFF) | **✅ ĐÃ CHỐT** |
| 6 | STAFF được `PATCH thresholds`? | **Không** (chỉ ADMIN) | **✅ ĐÃ CHỐT** |
| 7 | STAFF được xem valuation? | **Không** (chỉ ADMIN) | **✅ ĐÃ CHỐT** |
| 8 | Ô "ghi chú lý do" khi cập nhật giữ chỗ | Bỏ, không lưu | **✅ ĐÃ CHỐT** |
| 9 | Thiết lập ngưỡng nhúng form sửa sản phẩm (UC11) hay màn riêng | API riêng, không sửa UC11 | **✅ ĐÃ CHỐT** |
| 10 | Ngưỡng cận hạn | **30 ngày** (trong `application.conf`) | **✅ ĐÃ CHỐT** |
| 11 | `noBatch` flag | `batch_code == ""` | **✅ ĐÃ CHỐT** |
| 12 | GET `/api/inventory/{id}` trả kho inactive? | **Trả** (vì đã có ID rồi) | **✅ ĐÃ CHỐT** |
| 13 | Danh sách (UC29, by-product) lọc kho? | Chỉ `is_active = TRUE` | **✅ ĐÃ CHỐT** |

**Ghi chú về cài đặt:**
- Quyết định 5, 6, 7: gom trong `RolePolicy.kt` (security/)
- Quyết định 10: trong `application.conf` → `inventory.nearExpiryDays = 30`
- Quyết định 11-13: trong mapper và service
- Quyết định 1-4: sẽ cài đặt ở Đợt 4 (UC36-38)

**Thay đổi so với spec gốc (do chốt với Sprint 1):**
- Spec gốc nhắc `newSuspendedTransaction(Dispatchers.IO)` của Exposed
- **Thực tế**: Sprint 1 dùng **JDBC thuần**, không dùng Exposed DSL
- **Cài đặt**: UC31 dùng `connection.setAutoCommit(false)` + `SELECT ... FOR UPDATE` + manual commit/rollback
- Tất cả repository methods chạy trong `withContext(Dispatchers.IO)` ở service layer

Quyết định 1, 2, 3, 4 ảnh hưởng trực tiếp đến kết quả sổ kho (UC36). Nếu PO đổi, sửa hàm tính delta và điều kiện `WHERE` của CTE `movements`, rồi chạy lại test UC36.

## 10. Báo cáo sau mỗi đợt

Agent báo cáo: danh sách file đã tạo/sửa (theo 8 thư mục), kết quả build và test, các tiêu chí nghiệm thu chưa đạt (nếu có), chỗ lệch so với spec hoặc code Sprint 1, câu hỏi còn lại. Cuối đợt 4: bảng endpoint ↔ UC ↔ quyền và lệnh curl mẫu (kèm `Authorization: Bearer ...`).

## 11. Yêu cầu viết test (bắt buộc)

Agent phải **tạo file test thật** trong `src/test/kotlin/...` (cùng package gốc), không chỉ chạy test có sẵn. Mỗi mục `[ ]` trong "Nghiệm thu" ở mục 5 phải có ít nhất một test tương ứng. Tên test nêu rõ hành vi (ví dụ `reserved vuot ton tra ve 400`).

Ba tầng test, mỗi UC có đủ các tầng áp dụng được:

| Tầng | Công cụ | Kiểm tra | Đặt ở |
|---|---|---|---|
| Service | JUnit 5 (hoặc Kotest) + MockK | Nghiệp vụ, validation, tính `alertLevel`, `delta`, tổng, `BigDecimal` | `src/test/kotlin/.../service/` |
| Repository | Testcontainers MySQL 8 (hoặc DB test của Sprint 1) chạy `docs/schema.sql` | SQL thật: lọc, sort whitelist, gom nhóm, CTE sổ kho, `forUpdate`, cột sinh | `src/test/kotlin/.../repository/` |
| Route (API) | `testApplication { }` của Ktor | Status code, JSON trả về, 401/403, 400 khi sai tham số, 404 | `src/test/kotlin/.../routes/` |

Quy tắc:
- Test repository bắt buộc cho các SQL phức tạp: UC34, 35, alerts summary, UC36, UC37, UC39. Mock DB không đủ cho các truy vấn này.
- Mỗi test tự dựng dữ liệu mẫu (fixture dùng chung trong `src/test/.../fixture/`), theo mục 8, và dọn sau khi chạy. Không phụ thuộc thứ tự chạy giữa các test.
- Test phân quyền cho mọi endpoint: không token → 401, sai role → 403, đúng role → thành công.
- Test **bắt buộc** (không được bỏ):
  - UC31: `n = -1`, `n = stock + 1`, `n = 0`, `n = stock`; hai request song song trên cùng dòng; không ghi `available_quantity`.
  - UC33: `min < 0`, `max < min`, `min = max`; STAFF gọi PATCH → 403.
  - UC34, 35: sản phẩm chưa có dòng inventory; sản phẩm bằng đúng ngưỡng không xuất hiện; `max_stock = 0` không lỗi chia cho 0; tồn ở kho không hoạt động không được cộng.
  - Alerts summary: số liệu khớp với số dòng của hai API danh sách.
  - UC36: `closing_qty` bằng tồn hiện tại khi `toDate` là hôm nay; `TRANSFER` sinh 2 dòng; phiếu không `COMPLETED` không được tính; từng loại `STOCKTAKE`/`ADJUSTMENT` đúng dấu; khoảng ngày sai → 400.
  - UC37: từng bộ lọc; phiếu chuyển hiện ở cả hai kho; `delta` khớp UC36 cho cùng phiếu; sort ngoài whitelist.
  - UC38: phiếu chưa duyệt, `REJECTED`, `STOCKTAKE`, `TRANSFER`, id không tồn tại.
  - UC39: STAFF → 403; kho không hoạt động không tính; tổng bằng tổng các dòng; tiền dùng `BigDecimal` (ví dụ 0.1 + 0.2 ra đúng 0.30).
- Báo cáo cuối mỗi đợt phải kèm: danh sách file test đã tạo, số test, số đạt/không đạt, và mục nghiệm thu nào chưa có test (nếu có).
- Không sửa test cho khớp với code sai. Nếu test lộ lỗi hoặc spec mâu thuẫn, báo lại thay vì chỉnh test để qua.
