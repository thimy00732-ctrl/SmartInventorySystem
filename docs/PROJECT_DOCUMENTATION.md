<!--
  Tài liệu tổng quan toàn bộ dự án Smart Inventory System.
  Dùng để ghi chép kiến trúc hệ thống, module chính, dữ liệu và phạm vi nghiệp vụ
  trong giai đoạn phát triển đầu của dự án.
-->
# Smart Inventory System — Tài liệu tổng quan dự án

## 1. Mục tiêu dự án

Smart Inventory System là hệ thống quản lý kho theo mô hình backend + mobile client. Mục tiêu chính là giúp doanh nghiệp theo dõi hàng tồn kho, chứng từ nhập/xuất/điều chỉnh, cảnh báo mức tồn, tính giá trị kho, và truy vết biến động hàng hóa trên một nền tảng thống nhất.

Dự án hiện có hai thành phần chính:

- Backend: Kotlin + Ktor + Exposed-style repository/service pattern, chạy trên MySQL
- Mobile client: Android Jetpack Compose, gọi API từ backend

Cấu trúc tài liệu này dựa trên code hiện có trong repo và nguồn sự thật của database tại [schema.sql](./schema.sql), đồng thời tham khảo đặc tả nghiệp vụ tại [SPEC_inventory.md](./SPEC_inventory.md).

---

## 2. Tổng quan kiến trúc

### 2.1 Kiến trúc hệ thống

| Thành phần | Mô tả | Công nghệ |
|---|---|---|
| Backend API | Cung cấp API nghiệp vụ, xác thực, phân quyền, truy vấn dữ liệu và xử lý nghiệp vụ kho | Kotlin, Ktor, Gradle Kotlin DSL |
| Database | Lưu trữ dữ liệu giao dịch, sản phẩm, kho, phiếu, người dùng và AI knowledge | MySQL 8 |
| Android client | Giao diện thao tác danh mục, sản phẩm, tồn kho và phiếu giao dịch | Kotlin, Jetpack Compose, Retrofit |
| Docs & Spec | Quy định nghiệp vụ, schema, quy ước API và luồng phát triển | Markdown |

### 2.2 Kiến trúc ứng dụng backend

Backend theo mô hình layer rõ ràng:

- `routes`: định nghĩa endpoint và gắn quyền truy cập
- `controller`: nhận request, parse tham số, gọi service
- `service`: xử lý nghiệp vụ và validation
- `repository`: truy vấn DB và thao tác dữ liệu
- `mapper`: chuyển `ResultRow` / model thành DTO
- `entity`: định nghĩa bảng và enum Exposed
- `dto`: request/response model JSON
- `exception`: định nghĩa lỗi và mã lỗi
- `security`: JWT, claim, role authorization

Đây là kiến trúc chuẩn hóa trong dự án, và được thể hiện rõ ở file [Application.kt](../sis-backend/src/main/kotlin/com/sis/backend/Application.kt).

---

## 3. Cấu trúc thư mục dự án

```text
SmartInventorySystem/
├─ docs/
│  ├─ API_CONVENTIONS.md
│  ├─ ARCHITECTURE.md
│  ├─ GIT_WORKFLOW.md
│  ├─ SPEC_inventory.md
│  ├─ schema.sql
│  └─ PROJECT_DOCUMENTATION.md
├─ database/
│  ├─ 01_schema.sql
│  ├─ 02_seed_data.sql
│  └─ 03_test_queries.sql
├─ sis-backend/
│  ├─ src/main/kotlin/com/sis/backend/
│  ├─ src/test/kotlin/com/sis/backend/
│  ├─ build.gradle.kts
│  └─ settings.gradle.kts
├─ sis-android/
│  ├─ app/src/main/java/com/sis/android/
│  ├─ app/build.gradle.kts
│  └─ settings.gradle.kts
├─ README.md
├─ CLAUDE.md
└─ .gitignore
```

### 3.1 Module backend

Module backend nằm trong [sis-backend](../sis-backend). Đây là lõi của hệ thống, chuyên xử lý:

- Authentication / authorization
- CRUD danh mục và sản phẩm
- Quản lý vật tư và nhà cung cấp
- Theo dõi tồn kho theo kho / theo lô
- Tính ngưỡng và cảnh báo
- Sổ kho và giao dịch
- Định giá tồn kho

### 3.2 Module Android

Module Android nằm trong [sis-android](../sis-android). Đây là client UI, tập trung vào:

- Đăng nhập
- Quản lý sản phẩm
- Quản lý danh mục
- Quản lý tồn kho
- Gửi dữ liệu qua Retrofit
- Scan mã vạch / trợ lý AI

### 3.3 Database scripts

Thư mục [database](../database) chứa các script SQL mẫu, bao gồm:

- schema cơ sở dữ liệu
- dữ liệu mẫu
- truy vấn kiểm thử

Nguồn dữ liệu thật của hệ thống là [docs/schema.sql](./schema.sql), được ghi rõ trong comment đầu file.

---

## 4. Hệ thống nghiệp vụ

### 4.1 Tài khoản và phân quyền

Theo schema, hệ thống quản lý người dùng với 2 vai trò chính:

- `ADMIN`
- `WAREHOUSE_STAFF`

Bảng `users` lưu thông tin người dùng, mật khẩu hash, trạng thái, thời gian đăng nhập và quyền. Bảng `refresh_tokens` quản lý phiên đăng nhập theo refresh token.

Trong backend, security layer hỗ trợ xác thực JWT và kiểm tra role. Từ code, các route dùng `RoleAuthorization` và `AuthContract` để xác định người dùng, claim và quyền truy cập.

### 4.2 Quản lý danh mục

Hệ thống hỗ trợ các danh mục nghiệp vụ chính:

- `categories`: danh mục sản phẩm
- `suppliers`: nhà cung cấp
- `warehouses`: kho hàng
- `products`: sản phẩm

Đây là nền tảng cho các nghiệp vụ thực thi sau đó như tồn kho, giao dịch và cảnh báo.

### 4.3 Quản lý sản phẩm và kho

Cấu trúc dữ liệu chính:

- `products`: thông tin sản phẩm, giá vốn, giá bán, ngưỡng tối thiểu/tối đa, trạng thái hoạt động
- `warehouses`: thông tin kho, trạng thái hoạt động
- `inventory`: tồn kho theo kho-sản phẩm-lô hàng

Điểm đặc biệt:

- `inventory.stock_quantity` là số lượng tồn thực tế
- `inventory.reserved_quantity` là số lượng đang giữ chỗ
- `available_quantity` là cột sinh: `stock_quantity - reserved_quantity`
- Không ghi trực tiếp vào cột này qua nghiệp vụ, mà chỉ đọc từ DB

### 4.4 Phiếu giao dịch và sổ kho

Hệ thống lưu và xử lý các loại giao dịch:

- `IMPORT`
- `EXPORT`
- `TRANSFER`
- `STOCKTAKE`
- `ADJUSTMENT`

Mỗi phiếu có `transaction_details`, chứa chi tiết sản phẩm, số lượng, giá và các trường kiểm kê như `system_quantity`, `actual_quantity`, `difference_quantity`.

Dữ liệu này là nền tảng cho:

- sổ kho (stock ledger)
- truy vết biến động
- báo cáo tồn kho theo thời gian
- định giá và cảnh báo

### 4.5 Cảnh báo và ngưỡng

Các nghiệp vụ warning được triển khai theo các use case Sprint 2:

- ngưỡng tồn tối thiểu / tối đa của sản phẩm
- cảnh báo hết hàng / tồn thấp
- cảnh báo quá mức
- tổng hợp cảnh báo

Logic này được định nghĩa rõ trong [SPEC_inventory.md](./SPEC_inventory.md), đặc biệt các API từ UC29 đến UC39.

---

## 5. Cơ sở dữ liệu chính

### 5.1 Bảng chính

Các bảng chính trong schema:

- `users`
- `refresh_tokens`
- `categories`
- `suppliers`
- `products`
- `warehouses`
- `inventory`
- `transactions`
- `transaction_details`
- `knowledge_documents`
- `vector_embeddings`

### 5.2 Các ràng buộc quan trọng

- `products.min_stock >= 0`
- `products.max_stock >= min_stock`
- `inventory.stock_quantity >= 0`
- `inventory.reserved_quantity >= 0`
- `inventory.reserved_quantity <= stock_quantity`
- `transaction_details.quantity > 0`
- `difference_quantity` là cột sinh từ `actual_quantity - system_quantity`
- `available_quantity` là cột sinh từ `stock_quantity - reserved_quantity`

### 5.3 Ghi chú quan trọng về business rule

Theo quy ước dự án, không được tự ý cập nhật cột sinh tự động:

- `inventory.available_quantity`
- `transaction_details.difference_quantity`

Điều này là cơ sở của các quy tắc nghiệp vụ và kiểm thử trong spec.

---

## 6. Api và functional scope

### 6.1 Các API hiện có trong backend

File [ApiRoutes.kt](../sis-backend/src/main/kotlin/com/sis/backend/routes/ApiRoutes.kt) định nghĩa các route chính của backend:

- Inventory
  - `/api/inventory`
  - `/api/inventory/{id}`
  - `/api/inventory/by-product/{productId}`
  - `/api/inventory/{id}/reserved`
- Products thresholds
  - `/api/products/thresholds`
  - `/api/products/{id}/thresholds`
- Alerts
  - `/api/inventory/alerts/low-stock`
  - `/api/inventory/alerts/over-stock`
  - `/api/inventory/alerts/summary`
- Stock ledger
  - `/api/stock-ledger`
- Transactions
  - `/api/transactions/movements`
  - `/api/transactions/{id}`
- Valuation
  - `/api/inventory/valuation`
  - `/api/inventory/valuation/summary`

### 6.2 Tập trung nghiệp vụ theo Sprint 2

Theo [SPEC_inventory.md](./SPEC_inventory.md), dự án đang phát triển theo các use case sau:

- UC29: Danh sách tồn kho
- UC30: Chi tiết tồn kho
- UC31: Giữ chỗ hàng
- UC32: Số lượng khả dụng
- UC33: Thiết lập ngưỡng
- UC34: Cảnh báo tồn thấp
- UC35: Cảnh báo vượt mức
- UC36: Sổ kho
- UC37: Di chuyển / biến động giao dịch
- UC38: Chi tiết giao dịch
- UC39: Định giá tồn kho

Các use case này là nền tảng của module `sprint 2` hiện có trong backend.

---

## 7. Tích hợp auth và phân quyền

Chi tiết thực tế từ code dựa trên [Application.kt](../sis-backend/src/main/kotlin/com/sis/backend/Application.kt):

- `configureStatusPages()` được gọi sớm trong module
- `ContentNegotiation` được cài đặt với JSON serialization
- JWT được chuẩn bị nhưng chưa được kích hoạt hoàn toàn ở time of implementation
- `AuthContract` lưu định nghĩa provider và claim
- `RoleAuthorization` là chỉnh sửa quan trọng cho route-level security

Nghiệp vụ phân quyền cố định theo role:

- `ADMIN`: có quyền thao tác ngưỡng và các chức năng quản trị
- `WAREHOUSE_STAFF`: xem/điều hành theo phạm vi kho được phép, phụ thuộc nghiệp vụ / policy

---

## 8. Stack kỹ thuật

### 8.1 Backend

- Kotlin 2.2.20
- JDK 24 / toolchain 25 (trong build file)
- Ktor 3.3.1
- HikariCP + MySQL Connector/J
- kotlinx.serialization
- JWT (Auth0)
- JUnit 5 + MockK + Ktor test host

### 8.2 Android

- Kotlin Android
- Jetpack Compose
- Retrofit + Gson
- CameraX
- ML Kit Barcode Scanning
- Lifecycle ViewModel / Compose

### 8.3 Database

- MySQL 8
- UTF-8 MB4
- SQL schema quản lý bởi file [schema.sql](./schema.sql)

---

## 9. Quy ước phát triển và coding rules

Dự án có các quy ước rõ ràng, thể hiện ở [CLAUDE.md](../CLAUDE.md) và [SPEC_inventory.md](./SPEC_inventory.md):

- Mọi query DB phải chạy trong `newSuspendedTransaction(Dispatchers.IO)`
- Tiền tệ dùng `BigDecimal` với scale 2, format `HALF_UP`
- SQL raw phải dùng parameter binding, không nối chuỗi
- `LIKE` phải escape `%`, `_`, `\`
- Không update cột sinh tự động
- Không dùng `SchemaUtils.create*`; schema do SQL file quản lý
- Lỗi DB constraint phải trả `400` thay vì `500`
- API trả về thông báo lỗi bằng tiếng Việt, rõ ràng
- Không được làm API dropdown không có trong spec

---

## 10. Cách chạy dự án

### 10.1 Backend

1. Mở terminal trong thư mục [sis-backend](../sis-backend)
2. Đảm bảo MySQL đang chạy
3. Tạo / đồng bộ schema theo [schema.sql](./schema.sql)
4. Chạy:

```bash
./gradlew build
./gradlew test
./gradlew run
```

### 10.2 Android

1. Mở project [sis-android](../sis-android) trong Android Studio
2. Cài đặt SDK phù hợp
3. Chạy app trên emulator hoặc thiết bị thật
4. Cấu hình base URL backend nếu cần (thường là `http://10.0.2.2:8080` trên emulator)

---

## 11. Trạng thái phát triển hiện tại

Theo các file hiện có, dự án đang tập trung vào giai đoạn Sprint 2 về:

- inventory & stock control
- alerting & threshold management
- valuation
- stock ledger and movement traceability

Một số module đã có code và test thực tế như:

- `InventoryController`, `InventoryService`, `InventoryRepository`
- `AlertService`, `AlertRepository`
- `ValuationService`, `ValuationRepository`
- test classes trong [sis-backend/src/test/kotlin/com/sis/backend](../sis-backend/src/test/kotlin/com/sis/backend)

Điều này cho thấy dự án đang đi đúng hướng theo đặc tả được định nghĩa trong [SPEC_inventory.md](./SPEC_inventory.md).

---

## 12. Tài liệu liên quan

- [README.md](../README.md)
- [CLAUDE.md](../CLAUDE.md)
- [SPEC_inventory.md](./SPEC_inventory.md)
- [schema.sql](./schema.sql)
- [API_CONVENTIONS.md](./API_CONVENTIONS.md)
- [ARCHITECTURE.md](./ARCHITECTURE.md)
- [GIT_WORKFLOW.md](./GIT_WORKFLOW.md)

---

## 13. Kết luận

Smart Inventory System là một dự án warehouse management system hiện đại, kết hợp backend API và mobile client, tập trung vào kiểm soát hàng tồn kho, cảnh báo ngưỡng, sổ kho, giao dịch và định giá. Nó có kiến trúc phân lớp rõ ràng, quy tắc nghiệp vụ chặt chẽ, và tài liệu mô tả hệ thống đầy đủ trong các file docs của repo.

Nếu cần mở rộng trong tương lai, điểm nên ưu tiên là:

1. hoàn thiện JWT và auth policy
2. kiểm thử toàn bộ API theo spec UC29–UC39
3. mở rộng module AI / knowledge document
4. đồng bộ contract backend-mobile để tránh sai lệch dữ liệu
