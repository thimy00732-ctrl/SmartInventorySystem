@echo off
chcp 65001 >nul
echo ========================================
echo FORCE RESTART - Smart Inventory Backend
echo ========================================
echo.

echo [1/5] Dừng tất cả tiến trình Java backend...
taskkill /F /IM java.exe 2>nul
if %errorlevel% equ 0 (
    echo ✓ Đã dừng tiến trình Java
    timeout /t 2 /nobreak >nul
) else (
    echo ✓ Không có tiến trình Java nào đang chạy
)
echo.

echo [2/5] Xóa thư mục build cũ...
if exist build (
    rmdir /S /Q build
    echo ✓ Đã xóa thư mục build
) else (
    echo ✓ Không có thư mục build
)
echo.

echo [3/5] Clean project...
call gradlew.bat clean
echo.

echo [4/5] Build lại project (không chạy test)...
call gradlew.bat build -x test
if %errorlevel% neq 0 (
    echo ✗ BUILD THẤT BẠI!
    pause
    exit /b 1
)
echo.

echo [5/5] Khởi động backend...
echo Backend sẽ chạy tại: http://localhost:8080
echo.
echo Để dừng backend: Nhấn Ctrl+C
echo ========================================
echo.

call gradlew.bat run
