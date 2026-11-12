-- ==================================================
-- FIX DATABASE - Set all categories to active
-- ==================================================

-- Step 1: Check current status
SELECT 
    '=== BEFORE FIX ===' as step,
    category_id,
    category_name,
    is_active as current_status
FROM categories
ORDER BY category_id;

-- Step 2: Update all to active
UPDATE categories 
SET is_active = 1,
    updated_at = CURRENT_TIMESTAMP
WHERE is_active = 0;

-- Step 3: Verify fix
SELECT 
    '=== AFTER FIX ===' as step,
    category_id,
    category_name,
    is_active as new_status
FROM categories
ORDER BY category_id;

-- Step 4: Show count
SELECT 
    'SUMMARY' as step,
    COUNT(*) as total_categories,
    SUM(is_active) as active_count,
    SUM(CASE WHEN is_active = 0 THEN 1 ELSE 0 END) as inactive_count
FROM categories;
