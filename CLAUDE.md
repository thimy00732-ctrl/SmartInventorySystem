# CLAUDE.md — Quy ước chung cho agent (backend Kotlin + Ktor)

File này agent đọc mỗi phiên. Giữ ngắn. Đặc tả nghiệp vụ nằm ở `docs/SPEC_inventory.md`, schema ở `docs/schema.sql`.

## Dự án

Hệ thống quản lý kho. Backend Kotlin + Ktor + Exposed + MySQL 8. Đang làm Sprint 2: module Tồn kho, Cảnh báo, Giá trị, Sổ kho, Truy vết (UC29–UC39).

## Việc đầu tiên mỗi phiên

1. Đọc `docs/SPEC_inventory.md` và `docs/schema.sql`.
2. **Đọc code Sprint 1 trước khi viết code mới** để theo đúng quy ước đang có:
   - `page` bắt đầu từ 0 hay 1, dạng `PageResponse`.
   - Cách serialize `BigDecimal`, `LocalDate`, `LocalDateTime`.
   - Cách xác thực JWT và kiểm tra role.
   - Dùng Koin hay truyền dependency thủ công.
   - `StatusPages` và `ErrorResponse` đã có chưa.
3. Nếu spec mâu thuẫn với code Sprint 1: **giữ code Sprint 1**, nêu rõ chỗ đã điều chỉnh trong báo cáo.

## Stack

- Kotlin, JDK 17+, Ktor (Netty), Gradle Kotlin DSL.
- Exposed (DSL) + HikariCP + MySQL Connector/J.
- kotlinx.serialization, StatusPages, Authentication (JWT).
- Test: JUnit 5 (hoặc Kotest), MockK, `testApplication` của Ktor, Testcontainers MySQL nếu có sẵn.

## Cấu trúc thư mục

Package gốc lấy theo Sprint 1 (spec ký hiệu `com.example.warehouse`). Mỗi tính năng phải có file ở đủ 8 thư mục:

`controller`, `dto`, `entity`, `exception`, `mapper`, `repository`, `routes`, `service`.

| Thư mục | Vai trò | Không được |
|---|---|---|
| `routes` | Khai báo đường dẫn (hằng số trong `ApiRoutes`), gắn quyền, gọi controller | Chứa logic |
| `controller` | Đọc và parse tham số, gọi service, `call.respond` | Chứa nghiệp vụ, truy cập DB |
| `service` | Nghiệp vụ, validation, quản lý transaction | Phụ thuộc `ApplicationCall` |
| `repository` | Truy vấn Exposed hoặc SQL thô, trả model trung gian | Chứa nghiệp vụ |
| `mapper` | Chuyển `ResultRow`/model → DTO (extension function) | Truy vấn DB |
| `entity` | Bảng Exposed và enum, khớp tuyệt đối schema | Tạo/sửa schema |
| `dto` | `@Serializable data class` cho request/response | Lộ `password_hash` hay `ResultRow` |
| `exception` | `ErrorCode`, `AppException` và các lớp con, cấu hình `StatusPages` | — |

## Quy tắc code (bắt buộc)

- Mọi truy vấn DB chạy trong `newSuspendedTransaction(Dispatchers.IO)`.
- Tiền tệ dùng `BigDecimal` (scale 2, `HALF_UP`), không dùng `Double/Float`.
- SQL thô dùng **tham số ràng buộc**, không nối chuỗi. Sắp xếp chỉ qua **whitelist cột**.
- LIKE phải escape `%`, `_`, `\`.
- Không ghi vào cột sinh tự động: `inventory.available_quantity`, `transaction_details.difference_quantity`.
- Không dùng `SchemaUtils.create*`. Schema do `docs/schema.sql` quản lý.
- Lỗi vi phạm CHECK/ràng buộc của DB phải trả 400, không để lọt 500.
- Controller và route mỏng, không dùng `!!` bừa bãi, ưu tiên null-safety.
- Thông báo lỗi trả về client bằng tiếng Việt, rõ ràng.
- Không làm các API dropdown (`/api/warehouses`, `/categories`, `/suppliers`, `/users/options`), đã có sẵn.
- **Mỗi UC phải kèm file test thật** trong `src/test/kotlin/...` theo mục 11 của spec (service, repository, route). Không chỉ chạy test có sẵn. Không sửa test cho khớp code sai.

## Lệnh

Điền lệnh thật của repo vào đây (agent sẽ dùng):

```
Build:  ./gradlew build
Test:   ./gradlew test
Chạy:   ./gradlew run
```

## Cách làm việc

- Làm **theo từng đợt** trong spec (mục 3). Mỗi đợt phải build và test xanh mới sang đợt sau.
- Không tự ý đổi các quyết định ở bảng "Quyết định mặc định" trong spec. Mọi mặc định nằm ở **một chỗ** (`RolePolicy`, `application.conf`, hàm tính delta dùng chung).
- Khi xong một đợt, báo cáo: file đã tạo/sửa, kết quả build và test, các chỗ lệch so với spec, câu hỏi còn lại.
- Không sửa API và code đã có của Sprint 1 trừ khi cần bổ sung nhỏ (ví dụ thêm handler vào `StatusPages`); nêu rõ khi có sửa.
