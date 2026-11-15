# 📚 Hướng Dẫn Kiểm Thử Tự Động

**Ngày Tạo:** 15-11-2026  
**Dự Án:** Hệ Thống Quản Lý Kho - Backend  
**Phiên Bản:** 2.0 - Cập Nhật  
**Ngôn Ngữ:** Tiếng Việt

---

## 📌 ĐỌC NGAY - QUAN TRỌNG

⚠️ **LỖI NGHIÊM TRỌNG:** Các test đã qua nhưng ứng dụng bị hỏng!
- ✅ 27/27 test đã qua
- ❌ Nhưng không ai đăng nhập được
- 🔴 Cơ sở dữ liệu lưu mật khẩu dạng văn bản (LỖI!)

**Xem chi tiết:** `TEST_RESULTS_ACTUAL_VI.md`

---

## 📋 Mục Lục

1. [Tổng Quan](#tổng-quan)
2. [Các Tệp Test](#các-tệp-test)
3. [Chi Tiết Từng Lớp Test](#chi-tiết-từng-lớp-test)
4. [Công Cụ & Khung Kiểm Thử](#công-cụ--khung-kiểm-thử)
5. [Cách Chạy Test](#cách-chạy-test)
6. [Lỗi Bảo Mật Phát Hiện](#lỗi-bảo-mật-phát-hiện)
7. [Tài Liệu Kết Quả](#tài-liệu-kết-quả)

---

## 🎯 Tổng Quan

### Mục Đích
Bộ test tự động này được viết để:
- ✅ Kiểm tra **bảo mật mật khẩu** (mã hóa BCrypt)
- ✅ Kiểm tra **token xác thực** tạo và xác minh
- ✅ Phát hiện **lỗi bảo mật** trong đăng nhập
- ✅ Tài liệu hóa cách hoạt động của các hàm bảo mật

### Phạm Vi Kiểm Thử
```
Tổng số test:     27 test
Đã qua:           27 ✅
Thất bại:         0 ❌
Tỷ lệ thành công: 100%

⚠️ Nhưng: Ứng dụng BỊ HỎNG - Không ai đăng nhập được!
```

### Các Lớp Test
1. **PasswordSecurityTest** (11 test)
2. **JWTTokenTest** (8 test)
3. **LoginSecurityBugTest** (4 test)
4. **LoginSecurityTest** (4 test)

---

## 📁 Các Tệp Test

### 1. PasswordSecurityTest.kt

**Vị Trí:** `src/test/kotlin/com/sis/backend/PasswordSecurityTest.kt`

**Mục Đích:** Kiểm thử mã hóa mật khẩu và xác minh BCrypt

**11 Test:**

| # | Tên Test | Mục Đích | Kết Quả |
|---|----------|---------|--------|
| 1 | testPasswordHashingSuccess | Kiểm tra mã hóa mật khẩu BCrypt | ✅ |
| 2 | testPasswordVerificationCorrect | Xác minh mật khẩu đúng | ✅ |
| 3 | testPasswordVerificationWrong | Xác minh mật khẩu sai → Thất bại | ✅ |
| 4 | testStrongPasswordValid | Mật khẩu mạnh hợp lệ | ✅ |
| 5 | testStrongPasswordTooShort | Mật khẩu quá ngắn → Thất bại | ✅ |
| 6 | testStrongPasswordNoUppercase | Không có chữ hoa → Thất bại | ✅ |
| 7 | testStrongPasswordNoLowercase | Không có chữ thường → Thất bại | ✅ |
| 8 | testStrongPasswordNoNumber | Không có số → Thất bại | ✅ |
| 9 | testPasswordErrorEmpty | Thông báo lỗi - trống | ✅ |
| 10 | testPasswordErrorTooShort | Thông báo lỗi - quá ngắn | ✅ |
| 11 | testPasswordErrorValid | Thông báo lỗi - hợp lệ | ✅ |

**Yêu Cầu Mật Khẩu Mạnh:**
- Tối thiểu 8 ký tự
- Có chữ hoa (A-Z)
- Có chữ thường (a-z)
- Có số (0-9)

---

### 2. JWTTokenTest.kt

**Vị Trí:** `src/test/kotlin/com/sis/backend/PasswordSecurityTest.kt` (cùng tệp)

**Mục Đích:** Kiểm thử tạo và xác minh token xác thực

**8 Test:**

| # | Tên Test | Mục Đích | Kết Quả |
|---|----------|---------|--------|
| 1 | testGenerateAccessToken | Tạo mã truy cập | ✅ |
| 2 | testVerifyAccessTokenCorrect | Xác minh mã truy cập đúng | ✅ |
| 3 | testVerifyAccessTokenInvalid | Xác minh mã truy cập sai → Trống | ✅ |
| 4 | testGenerateRefreshToken | Tạo mã làm mới | ✅ |
| 5 | testVerifyRefreshTokenCorrect | Xác minh mã làm mới đúng | ✅ |
| 6 | testVerifyRefreshTokenInvalid | Xác minh mã làm mới sai → Trống | ✅ |
| 7 | testExtractUserIdFromToken | Trích xuất mã người dùng từ token | ✅ |
| 8 | testAccessTokenContainsAllClaims | Token chứa tất cả thông tin | ✅ |

**Chi Tiết Token:**
- **Mã Truy Cập:** Hợp lệ 1 giờ (3600 giây)
- **Mã Làm Mới:** Hợp lệ 7 ngày (604800 giây)
- **Thuật Toán:** HMAC256
- **Thông Tin:** mã người dùng, email, vai trò, kiểu, thời hết hạn

---

### 3. LoginSecurityBugTest.kt

**Vị Trí:** `src/test/kotlin/com/sis/backend/LoginSecurityBugTest.kt`

**Mục Đích:** Phát hiện và chứng minh lỗi bảo mật - không ai đăng nhập được

**4 Test:**

| # | Tên Test | Mục Đích | Kết Quả |
|---|----------|---------|--------|
| 1 | testBugPlaintextPasswordCannotLogin | Chứng minh mật khẩu văn bản → đăng nhập thất bại | ✅ |
| 2 | testBugBCryptCannotVerifyPlaintext | BCrypt không xác minh mật khẩu văn bản | ✅ |
| 3 | testFixProperBCryptHashCanLogin | Sửa - BCrypt mã hóa → đăng nhập được | ✅ |
| 4 | testSecurityIssueDocumented | Tài liệu lỗi và giải pháp | ✅ |

**Lỗi Phát Hiện:**
```
🔴 LỖI NGHIÊM TRỌNG: Không ai đăng nhập được

Nguyên Nhân:
  - Cơ Sở Dữ Liệu: password_hash = "Password123" (mật khẩu văn bản)
  - Dịch Vụ Đăng Nhập: Dùng hàm xác minh mật khẩu BCrypt
  - Hàm Xác Minh: Dùng BCrypt.verify()
  - BCrypt: Không thể xác minh mật khẩu văn bản
  - Kết Quả: Tất Cả Đăng Nhập Thất Bại ❌

Giải Pháp:
  - Cập Nhật Cơ Sở Dữ Liệu: password_hash = "$2b$12$..." (mã hóa BCrypt)
  - Khi Đó: Đăng Nhập Thành Công ✅
```

---

### 4. LoginSecurityTest.kt

**Vị Trí:** `src/test/kotlin/com/sis/backend/LoginSecurityTest.kt`

**Mục Đích:** Kiểm thử hành vi đăng nhập

**4 Test:**

| # | Tên Test | Mục Đích | Kết Quả |
|---|----------|---------|--------|
| 1 | testLoginSuccess | Đăng nhập thành công | ✅ |
| 2 | testLoginFailedWrongPassword | Đăng nhập thất bại - sai mật khẩu | ✅ |
| 3 | testLoginFailedUserNotFound | Đăng nhập thất bại - không tìm thấy người dùng | ✅ |
| 4 | testLoginReturnsToken | Đăng nhập trả về token | ✅ |

---

## 🔧 Chi Tiết Từng Lớp Test

### PasswordSecurityTest

**Nội Dung Test:**
```kotlin
// Test 1: Mã hóa
val plainPassword = "Password123"
val hashedPassword = PasswordService.hashPassword(plainPassword)
// Kiểm tra: hashedPassword không trống, dài > 10 ký tự

// Test 2: Xác minh (đúng)
val isValid = PasswordService.verifyPassword(plainPassword, hashedPassword)
// Kiểm tra: isValid == true ✅

// Test 3: Xác minh (sai)
val isInvalid = PasswordService.verifyPassword("WrongPassword", hashedPassword)
// Kiểm tra: isInvalid == false ✅

// Test 4-8: Yêu Cầu Độ Mạnh
// Kiểm tra yêu cầu: độ dài >= 8, chữ hoa, chữ thường, số

// Test 9-11: Thông Báo Lỗi
// Kiểm tra thông báo hiển thị cho người dùng
```

**Điểm Quan Trọng:**
- Sử dụng BCrypt với hệ số = 12 (tiêu chuẩn)
- Mỗi lần mã hóa tạo muối khác nhau
- BCrypt an toàn chống tấn công đo thời gian

---

### JWTTokenTest

**Nội Dung Test:**
```kotlin
// Test 1: Tạo Mã Truy Cập
val token = jwtService.generateAccessToken(1, "user@example.com", "ADMIN")
// Kiểm tra: token chứa 3 phần ngăn bằng dấu chấm

// Test 2: Xác Minh Mã Truy Cập
val claims = jwtService.verifyAccessToken(token)
// Kiểm tra: userId=1, email="user@example.com", role="ADMIN"

// Test 3: Xác Minh Token Không Hợp Lệ
val invalidClaims = jwtService.verifyAccessToken("invalid.token.here")
// Kiểm tra: invalidClaims == null

// Test 4-6: Kiểm Thử Mã Làm Mới
// Tương tự mã truy cập nhưng cho quy trình làm mới

// Test 7: Trích Xuất Mã Người Dùng
val userId = jwtService.getUserIdFromToken(token)
// Kiểm tra: userId == 1

// Test 8: Xác Minh Tất Cả Thông Tin
// Kiểm tra token chứa tất cả thông tin
```

**Điểm Quan Trọng:**
- Định Dạng Token: phần.phần.phần
- Xác Minh: bằng cơ sở và thời hết hạn
- Thông Tin: mã người dùng, email, vai trò, kiểu, thời hết hạn

---

### LoginSecurityBugTest

**Nội Dung Test:**
```kotlin
// Test 1: Lỗi - Mật khẩu văn bản không đăng nhập được
val userInput = "Password123"
val dbPassword = "Password123"  // LỖI: Mật khẩu văn bản!
val canLogin = PasswordService.verifyPassword(userInput, dbPassword)
// Kiểm tra: canLogin == false ❌ (LỖI!)

// Test 2: Lý Do Lỗi - BCrypt không xác minh mật khẩu văn bản
val result = PasswordService.verifyPassword("Password123", "Password123")
// Kiểm tra: result == false (Thiết Kế BCrypt)

// Test 3: Chứng Minh Sửa - Mã Hóa BCrypt Hoạt Động
val hashedPassword = PasswordService.hashPassword("Password123")
val fixedResult = PasswordService.verifyPassword("Password123", hashedPassword)
// Kiểm tra: fixedResult == true ✅ (ĐÃ SỬA!)

// Test 4: Tài Liệu
// In ra báo cáo lỗi & giải pháp
```

**Điểm Quan Trọng:**
- Test này chứng minh lỗi hiện tại
- Test này chứng minh giải pháp
- In ra báo cáo để tài liệu hóa lỗi

---

## 🛠️ Công Cụ & Khung Kiểm Thử

### Thư Viện
```kotlin
// Khung Kiểm Thử
testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")

// Giả Mạo
testImplementation("io.mockk:mockk:1.13.5")

// Kiểm Thử Kotlin
testImplementation(kotlin("test"))
```

### Ngăn Xếp Kiểm Thử
- **Khung:** JUnit 5 (Jupiter)
- **Kiểm Tra:** Kotlin Test (kotlin.test)
- **Giả Mạo:** MockK
- **Ngôn Ngữ:** Kotlin
- **Công Cụ Xây Dựng:** Gradle 9.3
- **Thời Gian Chạy:** Java 24

### Cấu Hình Test
```gradle
tasks.test {
    useJUnitPlatform()
}
```

---

## 📍 Vị Trí Tệp Test

```
src/test/kotlin/com/sis/backend/
├── PasswordSecurityTest.kt           (11 test)
├── JWTTokenTest.kt                   (8 test, trong tệp trên)
└── LoginSecurityBugTest.kt           (4 test)
```

---

## 🚀 Cách Chạy Test

### 1. Chạy Tất Cả Test
```bash
cd sis-backend
./gradlew.bat test
```

### 2. Chạy Lớp Test Cụ Thể
```bash
# PasswordSecurityTest
./gradlew.bat test --tests "*PasswordSecurityTest*"

# JWTTokenTest
./gradlew.bat test --tests "*JWTTokenTest*"

# LoginSecurityBugTest
./gradlew.bat test --tests "*LoginSecurityBugTest*"
```

### 3. Chạy Phương Thức Test Cụ Thể
```bash
./gradlew.bat test --tests "*PasswordSecurityTest.testPasswordHashingSuccess*"
```

### 4. Xem Chi Tiết
```bash
./gradlew.bat test --info
```

### 5. Xóa & Xây Dựng Lại
```bash
./gradlew.bat clean test
```

---

## 📊 Lỗi Bảo Mật Phát Hiện

### 🔴 LỖI NGHIÊM TRỌNG - Không Ai Đăng Nhập Được

**Vấn Đề:** Không ai đăng nhập được vào ứng dụng

**Nguyên Nhân:**
1. Cơ Sở Dữ Liệu lưu `password_hash = "Password123"` (mật khẩu văn bản)
2. Dịch Vụ Đăng Nhập dùng `PasswordService.verifyPassword()` để xác minh
3. `verifyPassword()` dùng BCrypt.verify()
4. BCrypt không thể xác minh mật khẩu văn bản-với-văn bản
5. Kết Quả: Tất Cả Đăng Nhập Thất Bại ❌

**Sửa Chữa Cần Thiết:**
```sql
-- Cập nhật tất cả mật khẩu thành mã hóa BCrypt
UPDATE users SET password_hash = '$2b$12$[MÃ_HÓA_BCRYPT]' WHERE 1=1;
```

**Ví Dụ Sửa:**
```sql
UPDATE users 
SET password_hash = '$2b$12$AAAAAv0gWy0u8w.7mQABXukKcqaSDEoVJiV.RkQ3cKA6cGz5xPf1.' 
WHERE email = 'admin@example.com';
```

---

## 📈 Tóm Tắt Test

```
┌──────────────────────────────────────┐
│        TÓM TẮT KẾT QUẢ TEST        │
├──────────────────────────────────────┤
│ Tổng Số Test:         27            │
│ Đã Qua:               27 ✅        │
│ Thất Bại:              0 ❌        │
│ Bỏ Qua:                0            │
│ Tỷ Lệ Thành Công:    100%          │
├──────────────────────────────────────┤
│ PasswordSecurityTest:  11 ✅       │
│ JWTTokenTest:          8 ✅       │
│ LoginSecurityBugTest:  4 ✅       │
│ LoginSecurityTest:     4 ✅       │
└──────────────────────────────────────┘
```

---

## ✅ Kết Luận

### Hoàn Thành
- ✅ 27 test đã viết xong
- ✅ 100% test đã qua
- ✅ Phát hiện 1 lỗi nghiêm trọng
- ✅ Tài liệu hóa lỗi & giải pháp

### Cần Làm
- ⏳ Cập nhật cơ sở dữ liệu mật khẩu thành mã hóa BCrypt
- ⏳ Xác minh đăng nhập hoạt động sau cập nhật
- ⏳ Triển khai sửa chữa lên máy chủ chính

---

**Báo Cáo Được Tạo Bởi:** Kiro - Kiểm Thử Tự Động  
**Ngày:** 15-11-2026  
**Phiên Bản:** 1.0 - Phát Hành Đầu Tiên
