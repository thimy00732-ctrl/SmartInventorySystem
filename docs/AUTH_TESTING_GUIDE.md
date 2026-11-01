# Auth Testing Guide (UC01-UC07)

## Chuẩn Bị

### 1. Khởi Động Backend
```bash
cd sis-backend
./gradlew run
# Backend chạy tại: http://localhost:8080
```

### 2. Mở Postman
- Tạo collection mới: "Auth Testing"
- Tạo environment mới: "Local Dev"
  - base_url: http://localhost:8080
  - access_token: (sẽ được set sau)
  - refresh_token: (sẽ được set sau)

---

## 🧪 Test Scenarios

### Scenario 1: UC01 - Đăng Nhập Thành Công

**Setup**: Tạo 1 tài khoản admin trong database (nếu chưa có):
```sql
-- Chạy trong MySQL
INSERT INTO users (email, password_hash, role, status, created_at, updated_at)
VALUES ('admin@example.com', 'any_hash', 'ADMIN', 'ACTIVE', NOW(), NOW());

-- Hoặc dùng UI: trực tiếp test API
```

**Test Case**:
```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "admin@example.com",
  "password": "Password123"
}

Expected (200 OK):
{
  "user_id": 1,
  "email": "admin@example.com",
  "role": "ADMIN",
  "access_token": "eyJ0eXAiOiJKV1QiLCJhbGc...",
  "refresh_token": "eyJ0eXAiOiJKV1QiLCJhbGc...",
  "expires_in": 3600
}

Steps:
1. Copy access_token → Environment variable: {{access_token}}
2. Copy refresh_token → Environment variable: {{refresh_token}}
```

**Validation**:
- Status = 200
- response.access_token exists
- response.refresh_token exists
- response.user_id = 1

---

### Scenario 2: UC01 - Đăng Nhập Thất Bại (Email Sai)

**Test Case**:
```
POST http://localhost:8080/api/auth/login
{
  "email": "nonexistent@example.com",
  "password": "Password123"
}

Expected (401 Unauthorized):
{
  "error": "Email hoặc mật khẩu không chính xác"
}
```

---

### Scenario 3: UC01 - Đăng Nhập Thất Bại (Mật Khẩu Sai)

**Test Case**:
```
POST http://localhost:8080/api/auth/login
{
  "email": "admin@example.com",
  "password": "WrongPassword"
}

Expected (401 Unauthorized):
{
  "error": "Email hoặc mật khẩu không chính xác"
}
```

---

### Scenario 4: UC04 - Kiểm Soát Truy Cập (Validate Token)

**Test Case**:
```
GET http://localhost:8080/api/auth/validate
Authorization: Bearer {{access_token}}

Expected (200 OK):
{
  "valid": true,
  "user_id": 1,
  "email": "admin@example.com",
  "role": "ADMIN",
  "message": null
}
```

**Validation**:
- Status = 200
- response.valid = true
- response.user_id = 1

---

### Scenario 5: UC03 - Tạo Người Dùng Mới (Chỉ Admin)

**Test Case**:
```
POST http://localhost:8080/api/auth/users
Authorization: Bearer {{access_token}}
Content-Type: application/json

{
  "email": "staff@example.com",
  "password": "Staff@123",
  "role": "WAREHOUSE_STAFF"
}

Expected (201 Created):
{
  "user_id": 2,
  "email": "staff@example.com",
  "role": "WAREHOUSE_STAFF",
  "status": "ACTIVE",
  "last_login_at": null,
  "created_at": "2024-01-15T10:30:00"
}
```

**Validation**:
- Status = 201
- response.user_id = 2
- response.status = "ACTIVE"

---

### Scenario 6: UC03 - Tạo User Thất Bại (Email Duplicate)

**Test Case**:
```
POST http://localhost:8080/api/auth/users
Authorization: Bearer {{access_token}}

{
  "email": "admin@example.com",  // Email này đã tồn tại
  "password": "NewPass@123",
  "role": "WAREHOUSE_STAFF"
}

Expected (400 Bad Request):
{
  "error": "Email này đã được sử dụng"
}
```

---

### Scenario 7: UC03 - Tạo User Thất Bại (Mật Khẩu Yếu)

**Test Case**:
```
POST http://localhost:8080/api/auth/users
Authorization: Bearer {{access_token}}

{
  "email": "weak@example.com",
  "password": "weak",  // Tưới yếu: < 8 ký tự, không có chữ hoa/số
  "role": "WAREHOUSE_STAFF"
}

Expected (400 Bad Request):
{
  "error": "Mật khẩu tối thiểu 8 ký tự"
}
```

---

### Scenario 8: UC03 - Tạo User Thất Bại (Không Phải Admin)

**Trước**: Đăng nhập với staff account:
```
POST http://localhost:8080/api/auth/login
{
  "email": "staff@example.com",
  "password": "Staff@123"
}
Response → {{staff_access_token}}
```

**Test Case**:
```
POST http://localhost:8080/api/auth/users
Authorization: Bearer {{staff_access_token}}

{
  "email": "another@example.com",
  "password": "Pass@123",
  "role": "WAREHOUSE_STAFF"
}

Expected (403 Forbidden):
{
  "error": "Chỉ admin mới có quyền tạo người dùng"
}
```

---

### Scenario 9: UC03 - Cập Nhật Người Dùng

**Test Case**:
```
PUT http://localhost:8080/api/auth/users/2
Authorization: Bearer {{access_token}}
Content-Type: application/json

{
  "role": "ADMIN",
  "status": "INACTIVE"
}

Expected (200 OK):
{
  "user_id": 2,
  "email": "staff@example.com",
  "role": "ADMIN",
  "status": "INACTIVE",
  "created_at": "2024-01-15T10:30:00"
}
```

---

### Scenario 10: UC05 - Cập Nhật Hồ Sơ

**Test Case**:
```
PUT http://localhost:8080/api/auth/profile
Authorization: Bearer {{access_token}}

{
  "email": "admin.new@example.com"
}

Expected (200 OK):
{
  "user_id": 1,
  "email": "admin.new@example.com",
  "role": "ADMIN",
  "status": "ACTIVE"
}

Lưu ý: Chỉ có thể cập nhật email của chính mình
```

---

### Scenario 11: UC06 - Thay Đổi Mật Khẩu

**Setup**: Đăng nhập với admin account:
```
POST http://localhost:8080/api/auth/login
{
  "email": "admin@example.com",
  "password": "Password123"
}
Response → {{admin_access_token}}
```

**Test Case**:
```
POST http://localhost:8080/api/auth/change-password
Authorization: Bearer {{admin_access_token}}

{
  "old_password": "Password123",
  "new_password": "NewPassword456"
}

Expected (200 OK):
{
  "message": "Mật khẩu đã được thay đổi thành công",
  "success": true
}
```

**Validation**:
- Thử login với old password → phải fail
- Thử login với new password → phải thành công

---

### Scenario 12: UC06 - Thay Đổi Mật Khẩu Thất Bại (Old Password Sai)

**Test Case**:
```
POST http://localhost:8080/api/auth/change-password
Authorization: Bearer {{access_token}}

{
  "old_password": "WrongOldPassword",
  "new_password": "NewPassword456"
}

Expected (400 Bad Request):
{
  "error": "Mật khẩu cũ không chính xác"
}
```

---

### Scenario 13: UC06 - Thay Đổi Mật Khẩu Thất Bại (Mật Khẩu Mới = Mật Khẩu Cũ)

**Test Case**:
```
POST http://localhost:8080/api/auth/change-password
Authorization: Bearer {{access_token}}

{
  "old_password": "CurrentPassword",
  "new_password": "CurrentPassword"  // Cùng mật khẩu
}

Expected (400 Bad Request):
{
  "error": "Mật khẩu mới phải khác mật khẩu cũ"
}
```

---

### Scenario 14: UC07 - Làm Mới Access Token

**Setup**: Sau khi login thành công, có {{refresh_token}}

**Test Case**:
```
POST http://localhost:8080/api/auth/refresh-token
Content-Type: application/json

{
  "refresh_token": "{{refresh_token}}"
}

Expected (200 OK):
{
  "access_token": "eyJ0eXAiOiJKV1QiLCJhbGc...",  // Token mới
  "refresh_token": "eyJ0eXAiOiJKV1QiLCJhbGc...",  // Token mới
  "expires_in": 3600
}

Steps:
1. Update Environment: {{access_token}} = new access_token
2. Update Environment: {{refresh_token}} = new refresh_token
```

**Validation**:
- Status = 200
- response.access_token !== old_access_token (token khác)
- response.refresh_token !== old_refresh_token (token khác)

---

### Scenario 15: UC07 - Làm Mới Token Thất Bại (Token Hết Hạn)

**Test Case**:
```
POST http://localhost:8080/api/auth/refresh-token

{
  "refresh_token": "expired_token_here"
}

Expected (401 Unauthorized):
{
  "error": "Refresh token không hợp lệ hoặc đã hết hạn"
}
```

---

### Scenario 16: UC02 - Đăng Xuất

**Test Case**:
```
POST http://localhost:8080/api/auth/logout

{
  "refresh_token": "{{refresh_token}}"
}

Expected (200 OK):
{
  "message": "Đăng xuất thành công"
}

Validation:
- Thử dùng refresh_token này để làm mới → phải fail (token bị revoke)
```

**Sau khi logout**:
```
POST http://localhost:8080/api/auth/refresh-token

{
  "refresh_token": "{{refresh_token}}"  // Token này đã bị revoke
}

Expected (401 Unauthorized):
{
  "error": "Refresh token đã bị vô hiệu hóa"
}
```

---

## 📊 Test Coverage Matrix

| UC | Scenario | Endpoint | Method | Expected Status | Notes |
|----|----------|----------|--------|-----------------|-------|
| UC01 | Login thành công | /api/auth/login | POST | 200 | Trả JWT + Refresh token |
| UC01 | Login fail - Email sai | /api/auth/login | POST | 401 | Error message rõ ràng |
| UC01 | Login fail - Password sai | /api/auth/login | POST | 401 | Error message rõ ràng |
| UC02 | Logout | /api/auth/logout | POST | 200 | Revoke refresh token |
| UC03 | Tạo user (Admin) | /api/auth/users | POST | 201 | Chỉ admin mới được |
| UC03 | Tạo user fail - Email dup | /api/auth/users | POST | 400 | Email unique constraint |
| UC03 | Tạo user fail - Pass yếu | /api/auth/users | POST | 400 | Validate password strength |
| UC03 | Tạo user fail - Không admin | /api/auth/users | POST | 403 | Role-based access control |
| UC03 | Update user | /api/auth/users/{id} | PUT | 200 | Chỉ admin mới được |
| UC04 | Validate token | /api/auth/validate | GET | 200 | Token hợp lệ |
| UC05 | Update profile | /api/auth/profile | PUT | 200 | Cập nhật info cá nhân |
| UC06 | Change password | /api/auth/change-password | POST | 200 | Old + new password |
| UC06 | Change password fail | /api/auth/change-password | POST | 400 | Old password sai |
| UC07 | Refresh token | /api/auth/refresh-token | POST | 200 | Token rotation |
| UC07 | Refresh fail - Expired | /api/auth/refresh-token | POST | 401 | Token hết hạn |

---

## 🚀 Automation với Newman

### Tạo Postman Collection

1. Export collection từ Postman
2. Lưu vào: `postman/collections/Auth-UC01-UC07.postman_collection.json`
3. Tạo environment file: `postman/environments/local.json`

```json
// local.json
{
  "name": "Local Dev",
  "values": [
    {"key": "base_url", "value": "http://localhost:8080", "enabled": true},
    {"key": "access_token", "value": "", "enabled": true},
    {"key": "refresh_token", "value": "", "enabled": true},
    {"key": "admin_email", "value": "admin@example.com", "enabled": true},
    {"key": "admin_password", "value": "Password123", "enabled": true}
  ]
}
```

### Chạy Tests

```bash
# Install Newman
npm install -g newman

# Chạy collection
newman run postman/collections/Auth-UC01-UC07.postman_collection.json \
  --environment postman/environments/local.json \
  --reporters cli,json,html \
  --reporter-html-export newman-report.html

# Xem report
open newman-report.html
```

---

## 🐛 Troubleshooting

### Error: "Token không hợp lệ"
- Kiểm tra xem token có được copy đúng không
- Token có hết hạn không? (access token: 1 giờ)
- Authorization header có format: `Bearer <token>`

### Error: "Email này đã được sử dụng"
- Dùng email mới hoặc xóa user cũ từ database

### Error: "Chỉ admin mới có quyền"
- Kiểm tra role của user: phải là "ADMIN"
- Đảm bảo token từ admin account

### Error: "Mật khẩu phải chứa..."
- Password phải có: 8+ ký tự, chữ hoa, chữ thường, số
- Ví dụ: `Password123`, `MyPass@456`

---

## 📝 Notes

- Access token: hợp lệ 1 giờ
- Refresh token: hợp lệ 7 ngày
- Soft delete: status = INACTIVE (không hard delete)
- Token rotation: khi refresh, refresh_token cũ bị revoke
- Password: BCrypt hashing với cost 12

