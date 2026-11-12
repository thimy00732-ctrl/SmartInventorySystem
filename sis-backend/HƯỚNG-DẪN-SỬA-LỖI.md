# HƯỚNG DẪN SỬA LỖI CATEGORY

## Vấn đề đã phát hiện

1. **StatusPages plugin chưa được cài đặt** 
   - File `StatusPagesConfig.kt` đã tồn tại nhưng chưa được gọi trong `Application.kt`
   - Dẫn đến tất cả ngoại lệ `CategoryException` trả về HTTP 500 thay vì 400/404/409

2. **Backend có thể đang chạy code cũ**
   - Mặc dù đã rebuild nhiều lần, tiến trình Java có thể vẫn chạy bytecode cũ
   - Code cũ sử dụng `getBoolean("is_active")` không tương thích với MySQL tinyint(1)

3. **Database có thể có giá trị is_active = 0**
   - Mặc dù SQL dump cho thấy is_active = 1, giá trị thực tế trong database có thể khác

## Đã sửa

✅ Đã thêm `configureStatusPages()` vào `Application.kt`
✅ Đã sửa `CategoryRepository.mapRow()`: `getInt("is_active") == 1`
✅ Đã sửa `reactivate()` SQL: sử dụng `TRUE`/`FALSE` thay vì `0`/`1`

## Cách khắc phục - 3 BƯỚC

### BƯỚC 1: Sửa Database (nếu cần)

```bash
# Mở MySQL Workbench hoặc DBeaver
# Chạy file: verify-and-fix-database.sql
```

Hoặc chạy trực tiếp:
```sql
-- Kiểm tra giá trị hiện tại
SELECT category_id, category_name, is_active FROM categories;

-- Nếu tất cả is_active = 0, sửa lại:
UPDATE categories SET is_active = 1;
```

### BƯỚC 2: Restart Backend (QUAN TRỌNG)

**Cách 1: Sử dụng script tự động (KHUYẾN NGHỊ)**
```bash
cd d:\SmartInventorySystem\sis-backend
FORCE-RESTART.bat
```

Script này sẽ:
- Dừng tất cả tiến trình Java
- Xóa thư mục build cũ
- Clean project
- Build lại từ đầu
- Khởi động backend mới

**Cách 2: Thủ công**
```bash
# 1. Mở Task Manager → Tìm java.exe → End Task (TẤT CẢ)
# 2. Mở CMD:
cd d:\SmartInventorySystem\sis-backend
gradlew.bat clean
rmdir /S /Q build
gradlew.bat build -x test
gradlew.bat run
```

### BƯỚC 3: Test API

**Cách 1: Sử dụng script PowerShell**
```bash
# Mở PowerShell mới (KHÔNG phải cửa sổ đang chạy backend)
cd d:\SmartInventorySystem\sis-backend
.\test-category-api.ps1
```

**Cách 2: Test thủ công bằng trình duyệt**
```
1. Mở: http://localhost:8080/api/categories
2. Kiểm tra: "isActive" có giá trị true không?
3. Test Android app: thử toggle trạng thái category
```

## Kỳ vọng sau khi sửa

✅ GET /api/categories → Tất cả categories có `"isActive": true`
✅ PATCH /api/categories/{id}/deactivate → HTTP 200 (không còn 500)
✅ PATCH /api/categories/{id}/reactivate → HTTP 200 hoặc 400 (nếu đã active)
✅ Android app có thể toggle trạng thái category

## Nếu vẫn lỗi

1. **Kiểm tra backend có chạy không:**
   ```
   http://localhost:8080/health
   ```

2. **Kiểm tra log backend khi toggle:**
   - Xem cửa sổ CMD đang chạy backend
   - Tìm dòng lỗi màu đỏ khi nhấn toggle

3. **Kiểm tra database thực tế:**
   ```sql
   SELECT * FROM categories WHERE category_id = 1;
   ```

4. **Chạy diagnostics đầy đủ:**
   ```bash
   URGENT-DIAGNOSTIC.bat
   ```
   Gửi output cho developer để phân tích

## Giải thích kỹ thuật

### Tại sao cần cài StatusPages?

Trước đây:
```kotlin
fun Application.module() {
    install(ContentNegotiation) { ... }
    // StatusPages KHÔNG được cài đặt
}
```

Khi xảy ra `InvalidCategoryException`, không có handler → HTTP 500

Sau khi sửa:
```kotlin
fun Application.module() {
    configureStatusPages()  // ← Thêm dòng này
    install(ContentNegotiation) { ... }
}
```

Bây giờ `InvalidCategoryException` → HTTP 400 với message rõ ràng

### Tại sao phải dừng hẳn Java?

IntelliJ/IDE có thể cache compiled classes. Khi rebuild, nó compile mới nhưng JVM vẫn load classes cũ từ memory. Cách duy nhất chắc chắn là kill process và start lại.

### Tại sao dùng getInt() thay vì getBoolean()?

MySQL JDBC driver không tự động convert `tinyint(1)` thành Boolean. Phải đọc dưới dạng Int rồi so sánh với 1.

```kotlin
// SAI - không hoạt động với MySQL
isActive = result.getBoolean("is_active")

// ĐÚNG - hoạt động với MySQL tinyint(1)
isActive = result.getInt("is_active") == 1
```
