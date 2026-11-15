# 🔐 TÀI KHOẢN TEST

## Tài khoản có sẵn trong database

### 1. Admin
- **Email**: `admin@warehouse.vn`
- **Password**: `admin123`
- **Role**: ADMIN
- **Quyền**: Toàn bộ chức năng (111 UC)

### 2. Minh Châu Admin
- **Email**: `minhchauadmin@warehouse.vn`
- **Password**: `admin123`
- **Tên**: Minh Châu
- **Role**: ADMIN
- **Quyền**: Toàn bộ chức năng (111 UC)

### 3. Staff
- **Email**: `staff@warehouse.vn`
- **Password**: `staff123`
- **Role**: WAREHOUSE_STAFF
- **Quyền**: Hạn chế (xem docs/PHAN_QUYEN.md)

---

## Cách chạy seed data

```bash
# Chạy file SQL trong phpMyAdmin hoặc:
mysql -u root -p warehouse_db < database/01_schema.sql
mysql -u root -p warehouse_db < database/02_seed_data.sql
```

---

## Tạo tài khoản mới

### Cách 1: Qua phpMyAdmin

1. Tạo BCrypt hash tại: https://bcrypt-generator.com/
   - Password: `your_password`
   - Cost: `10`
   - Click Hash

2. Chạy SQL:
```sql
INSERT INTO users (email, password_hash, full_name, phone, role, status, created_at, updated_at)
VALUES (
    'email@warehouse.vn',
    '$2a$10$...hash_từ_step_1...',
    'Họ Tên',
    '0900000000',
    'ADMIN', -- hoặc 'WAREHOUSE_STAFF'
    'active',
    NOW(),
    NOW()
);
```

### Cách 2: Qua API (nếu đã có admin token)

```bash
POST http://localhost:8080/api/auth/users
Authorization: Bearer <admin_token>

{
  "email": "newuser@warehouse.vn",
  "password": "password123",
  "full_name": "Họ Tên",
  "phone": "0900000000",
  "role": "WAREHOUSE_STAFF"
}
```

---

## Lưu ý

- Password hash phải là BCrypt với cost=10
- Password tối thiểu 8 ký tự
- Email phải unique
- Role chỉ có: `ADMIN` hoặc `WAREHOUSE_STAFF`
- Status: `active` hoặc `inactive`
