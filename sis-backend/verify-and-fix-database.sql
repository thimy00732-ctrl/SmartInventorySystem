-- ========================================
-- KIỂM TRA VÀ SỬA DATABASE
-- ========================================

-- Bước 1: Kiểm tra giá trị hiện tại
SELECT 
    'TRƯỚC KHI SỬA' as phase,
    category_id, 
    category_name, 
    is_active,
    CASE 
        WHEN is_active = 1 THEN 'TRUE' 
        WHEN is_active = 0 THEN 'FALSE' 
        ELSE 'UNKNOWN' 
    END as is_active_readable
FROM categories
ORDER BY category_id;

-- Bước 2: Sửa tất cả category về trạng thái hoạt động
UPDATE categories 
SET is_active = 1 
WHERE is_active = 0 OR is_active IS NULL;

-- Bước 3: Kiểm tra sau khi sửa
SELECT 
    'SAU KHI SỬA' as phase,
    category_id, 
    category_name, 
    is_active,
    CASE 
        WHEN is_active = 1 THEN 'TRUE' 
        WHEN is_active = 0 THEN 'FALSE' 
        ELSE 'UNKNOWN' 
    END as is_active_readable
FROM categories
ORDER BY category_id;

-- Bước 4: Thống kê
SELECT 
    'THỐNG KÊ' as info,
    COUNT(*) as total_categories,
    SUM(CASE WHEN is_active = 1 THEN 1 ELSE 0 END) as active_count,
    SUM(CASE WHEN is_active = 0 THEN 1 ELSE 0 END) as inactive_count
FROM categories;
