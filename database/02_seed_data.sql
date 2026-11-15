-- =====================================================================
-- SEED DATA: Smart Inventory System
-- Dữ liệu mẫu cho testing và development
-- =====================================================================

-- ─── USERS ───────────────────────────────────────────────────────────

-- Password hash cho "Admin@123" và "Staff@123" (BCrypt)
-- Lưu ý: Backend cần hash password khi insert, đây chỉ là ví dụ
-- Nếu backend chưa có API seed, cần hash trước khi insert

-- Admin account
-- Password: admin123 (đơn giản để test)
-- Hash được tạo với BCrypt cost=10
INSERT INTO users (email, password_hash, full_name, phone, role, status, created_at, updated_at)
VALUES (
    'admin@warehouse.vn',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'Nguyễn Văn Admin',
    '0901234567',
    'ADMIN',
    'active',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Warehouse Staff account  
-- Password: staff123 (đơn giản để test)
-- Hash được tạo với BCrypt cost=10
INSERT INTO users (email, password_hash, full_name, phone, role, status, created_at, updated_at)
VALUES (
    'staff@warehouse.vn',
    '$2a$10$Ie0B15M3gZxIAi49kxmmC.RAuxyJ31tdA4XQ1cFtAH9M60NfVLeW.',
    'Trần Thị Nhân Viên',
    '0907654321',
    'WAREHOUSE_STAFF',
    'active',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- ─── CATEGORIES ──────────────────────────────────────────────────────

INSERT INTO categories (category_name, description, is_active, created_at, updated_at, created_by)
VALUES
    ('Điện tử', 'Thiết bị điện tử, máy tính, phụ kiện', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    ('Văn phòng phẩm', 'Đồ dùng văn phòng, giấy tờ, bút viết', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    ('Thực phẩm', 'Thực phẩm khô, đóng hộp, bảo quản lâu', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    ('Dược phẩm', 'Thuốc, dược liệu, vật tư y tế', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    ('Hóa chất', 'Hóa chất công nghiệp, vệ sinh', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1);

-- ─── SUPPLIERS ───────────────────────────────────────────────────────

INSERT INTO suppliers (supplier_name, contact_person, email, phone, address, tax_code, is_active, created_at, updated_at, created_by)
VALUES
    ('Công ty TNHH Điện Tử ABC', 'Nguyễn Văn A', 'contact@abc-electronics.vn', '0281234567', '123 Nguyễn Huệ, Q1, TP.HCM', '0123456789', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    ('Công ty Cổ Phần Văn Phòng Phẩm XYZ', 'Trần Thị B', 'info@xyz-office.vn', '0287654321', '456 Lê Lợi, Q3, TP.HCM', '9876543210', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    ('Tổng Công Ty Thực Phẩm Việt Nam', 'Lê Văn C', 'sales@vietnamfood.vn', '0289876543', '789 Trần Hưng Đạo, Q5, TP.HCM', '1122334455', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    ('Công ty Dược Phẩm Sài Gòn', 'Phạm Thị D', 'info@sgpharma.vn', '0283456789', '321 Võ Văn Tần, Q3, TP.HCM', '5544332211', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1);

-- ─── PRODUCTS ────────────────────────────────────────────────────────

INSERT INTO products (sku, product_name, description, category_id, unit, quantity, min_stock_level, max_stock_level, unit_price, barcode, supplier_id, location, is_active, created_at, updated_at, created_by)
VALUES
    -- Điện tử
    ('LAPTOP-001', 'Laptop Dell Latitude 5420', 'Laptop Dell i5-1135G7, 8GB RAM, 256GB SSD', 1, 'Cái', 50, 10, 100, 15000000, '8901234567890', 1, 'KHO-A-01', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    ('MOUSE-001', 'Chuột Logitech M331', 'Chuột không dây Logitech', 1, 'Cái', 200, 50, 500, 250000, '8901234567891', 1, 'KHO-A-02', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    ('KEYBOARD-001', 'Bàn phím Logitech K380', 'Bàn phím Bluetooth đa nền tảng', 1, 'Cái', 150, 30, 300, 750000, '8901234567892', 1, 'KHO-A-03', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    
    -- Văn phòng phẩm
    ('PAPER-001', 'Giấy A4 Double A', 'Giấy A4 70gsm, 500 tờ/ream', 2, 'Ream', 500, 100, 1000, 85000, '8901234567893', 2, 'KHO-B-01', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    ('PEN-001', 'Bút bi Thiên Long TL-027', 'Bút bi xanh 0.7mm', 2, 'Cây', 1000, 200, 2000, 3000, '8901234567894', 2, 'KHO-B-02', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    ('FOLDER-001', 'Bìa còng A4', 'Bìa còng nhựa 2 lỗ', 2, 'Cái', 300, 50, 500, 12000, '8901234567895', 2, 'KHO-B-03', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    
    -- Thực phẩm
    ('RICE-001', 'Gạo ST25', 'Gạo ST25 cao cấp', 3, 'Kg', 1000, 200, 2000, 35000, '8901234567896', 3, 'KHO-C-01', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    ('OIL-001', 'Dầu ăn Simply', 'Dầu ăn thực vật 1L', 3, 'Chai', 500, 100, 1000, 45000, '8901234567897', 3, 'KHO-C-02', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    
    -- Dược phẩm
    ('MED-001', 'Paracetamol 500mg', 'Thuốc hạ sốt, giảm đau', 4, 'Hộp', 300, 50, 500, 15000, '8901234567898', 4, 'KHO-D-01', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    ('MED-002', 'Vitamin C 1000mg', 'Viên sủi Vitamin C', 4, 'Hộp', 200, 40, 400, 120000, '8901234567899', 4, 'KHO-D-02', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1);

-- ─── STOCK MOVEMENTS (Mẫu) ──────────────────────────────────────────

INSERT INTO stock_movements (product_id, movement_type, quantity, unit_price, reference_number, notes, created_at, created_by)
VALUES
    (1, 'IMPORT', 50, 15000000, 'PO-2024-001', 'Nhập kho đợt 1', CURRENT_TIMESTAMP, 1),
    (2, 'IMPORT', 200, 250000, 'PO-2024-002', 'Nhập kho đợt 1', CURRENT_TIMESTAMP, 1),
    (3, 'IMPORT', 150, 750000, 'PO-2024-003', 'Nhập kho đợt 1', CURRENT_TIMESTAMP, 1),
    (4, 'IMPORT', 500, 85000, 'PO-2024-004', 'Nhập kho đợt 1', CURRENT_TIMESTAMP, 1),
    (5, 'IMPORT', 1000, 3000, 'PO-2024-005', 'Nhập kho đợt 1', CURRENT_TIMESTAMP, 1);

COMMIT;

-- =====================================================================
-- LƯU Ý QUAN TRỌNG:
-- =====================================================================
-- 1. Password hash ở trên là GIỐNG MẪU, cần thay bằng hash thực từ BCrypt
-- 2. Để tạo password hash, sử dụng:
--    - Online tool: https://bcrypt-generator.com/ (cost=10)
--    - Hoặc gọi API backend: POST /api/auth/hash-password
-- 3. Sau khi chạy script này, test login với:
--    Admin: admin@warehouse.vn / Admin@123
--    Staff: staff@warehouse.vn / Staff@123
-- =====================================================================
