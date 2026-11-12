# Test Category API - PowerShell Script
# Kiểm tra API categories sau khi sửa lỗi

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "TEST CATEGORY API" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:8080/api"

# Test 1: Health check
Write-Host "[TEST 1] Health check..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/../health" -Method Get
    Write-Host "✓ Backend đang chạy" -ForegroundColor Green
    Write-Host "  Response: $($response | ConvertTo-Json -Compress)" -ForegroundColor Gray
} catch {
    Write-Host "✗ Backend không chạy hoặc lỗi" -ForegroundColor Red
    Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Test 2: Get all categories
Write-Host "[TEST 2] Lấy danh sách categories..." -ForegroundColor Yellow
try {
    $categories = Invoke-RestMethod -Uri "$baseUrl/categories" -Method Get
    $activeCount = ($categories.data | Where-Object { $_.isActive -eq $true }).Count
    $inactiveCount = ($categories.data | Where-Object { $_.isActive -eq $false }).Count
    
    Write-Host "✓ Lấy được $($categories.data.Count) categories" -ForegroundColor Green
    Write-Host "  - Đang hoạt động: $activeCount" -ForegroundColor $(if ($activeCount -gt 0) { "Green" } else { "Red" })
    Write-Host "  - Không hoạt động: $inactiveCount" -ForegroundColor Gray
    
    if ($activeCount -eq 0) {
        Write-Host "  ⚠ CẢNH BÁO: Tất cả categories đều inactive!" -ForegroundColor Red
        Write-Host "  → Chạy SQL script: verify-and-fix-database.sql" -ForegroundColor Yellow
    }
    
    # Lưu ID của category đầu tiên để test
    $testCategoryId = $categories.data[0].categoryId
    $testCategoryActive = $categories.data[0].isActive
    
    Write-Host "  Test category: ID=$testCategoryId, isActive=$testCategoryActive" -ForegroundColor Gray
} catch {
    Write-Host "✗ Lỗi khi lấy categories" -ForegroundColor Red
    Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Test 3: Toggle category status
Write-Host "[TEST 3] Thay đổi trạng thái category (ID=$testCategoryId)..." -ForegroundColor Yellow

if ($testCategoryActive) {
    # Deactivate
    Write-Host "  → Đang hoạt động, thử DEACTIVATE..." -ForegroundColor Gray
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/categories/$testCategoryId/deactivate" -Method Patch
        Write-Host "  ✓ Deactivate thành công" -ForegroundColor Green
        Write-Host "  Response: $($response | ConvertTo-Json -Compress)" -ForegroundColor Gray
    } catch {
        Write-Host "  ✗ Deactivate thất bại" -ForegroundColor Red
        Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
        Write-Host "  Status: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    }
    Write-Host ""
    
    # Reactivate
    Write-Host "  → Thử REACTIVATE lại..." -ForegroundColor Gray
    Start-Sleep -Seconds 1
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/categories/$testCategoryId/reactivate" -Method Patch
        Write-Host "  ✓ Reactivate thành công" -ForegroundColor Green
        Write-Host "  Response: $($response | ConvertTo-Json -Compress)" -ForegroundColor Gray
    } catch {
        Write-Host "  ✗ Reactivate thất bại" -ForegroundColor Red
        Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
        Write-Host "  Status: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    }
} else {
    # Reactivate first
    Write-Host "  → Không hoạt động, thử REACTIVATE..." -ForegroundColor Gray
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/categories/$testCategoryId/reactivate" -Method Patch
        Write-Host "  ✓ Reactivate thành công" -ForegroundColor Green
        Write-Host "  Response: $($response | ConvertTo-Json -Compress)" -ForegroundColor Gray
    } catch {
        Write-Host "  ✗ Reactivate thất bại" -ForegroundColor Red
        Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
        Write-Host "  Status: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    }
    Write-Host ""
    
    # Deactivate
    Write-Host "  → Thử DEACTIVATE..." -ForegroundColor Gray
    Start-Sleep -Seconds 1
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/categories/$testCategoryId/deactivate" -Method Patch
        Write-Host "  ✓ Deactivate thành công" -ForegroundColor Green
        Write-Host "  Response: $($response | ConvertTo-Json -Compress)" -ForegroundColor Gray
    } catch {
        Write-Host "  ✗ Deactivate thất bại" -ForegroundColor Red
        Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
        Write-Host "  Status: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    }
}
Write-Host ""

# Test 4: Verify final state
Write-Host "[TEST 4] Kiểm tra trạng thái cuối cùng..." -ForegroundColor Yellow
try {
    $category = Invoke-RestMethod -Uri "$baseUrl/categories/$testCategoryId" -Method Get
    Write-Host "✓ Category ID=$testCategoryId" -ForegroundColor Green
    Write-Host "  - Name: $($category.categoryName)" -ForegroundColor Gray
    Write-Host "  - isActive: $($category.isActive)" -ForegroundColor Gray
} catch {
    Write-Host "✗ Không lấy được thông tin category" -ForegroundColor Red
}
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "TEST HOÀN TẤT" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
