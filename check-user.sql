-- Kiểm tra tài khoản Minh Châu trong database
SELECT 
    user_id,
    full_name,
    email,
    phone,
    role,
    status,
    LEFT(password_hash, 30) as pwd_start,
    LENGTH(password_hash) as pwd_length,
    must_change_password,
    created_at,
    updated_at
FROM users 
WHERE email = 'minhchauadmin@warehouse.vn';

-- Kết quả cần thấy:
-- pwd_start: $2a$10$92IXUNpkjO0rOQ5byMi.Ye
-- pwd_length: 60
-- status: ACTIVE
-- role: ADMIN
