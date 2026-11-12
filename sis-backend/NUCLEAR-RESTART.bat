@echo off
chcp 65001 >nul
color 0C
echo ========================================
echo ⚠️  NUCLEAR RESTART - XÓA TOÀN BỘ
echo ========================================
echo.
echo Script này sẽ:
echo [1] Kill TẤT CẢ java.exe
echo [2] Xóa thư mục .gradle cache
echo [3] Xóa thư mục build
echo [4] Clean project
echo [5] Build lại từ đầu
echo [6] Khởi động backend
echo.
pause
echo.

echo [1/7] Kill tất cả Java processes...
taskkill /F /IM java.exe 2>nul
if %errorlevel% equ 0 (
    echo ✓ Đã kill Java
    timeout /t 3 /nobreak >nul
) else (
    echo ✓ Không có Java process nào
)
echo.

echo [2/7] Xóa .gradle cache...
if exist .gradle (
    rmdir /S /Q .gradle
    echo ✓ Đã xóa .gradle
) else (
    echo ✓ Không có .gradle
)
echo.

echo [3/7] Xóa thư mục build...
if exist build (
    rmdir /S /Q build
    echo ✓ Đã xóa build
) else (
    echo ✓ Không có build
)
echo.

echo [4/7] Xóa các file .class cũ...
for /r %%i in (*.class) do del "%%i" 2>nul
echo ✓ Hoàn tất
echo.

echo [5/7] Clean project...
call gradlew.bat clean --no-daemon
echo.

echo [6/7] Build project (không chạy test)...
call gradlew.bat build -x test --no-daemon
if %errorlevel% neq 0 (
    color 0C
    echo.
    echo ========================================
    echo ❌ BUILD THẤT BẠI!
    echo ========================================
    pause
    exit /b 1
)
echo.

color 0A
echo ========================================
echo ✅ BUILD THÀNH CÔNG!
echo ========================================
echo.

echo [7/7] Khởi động backend...
echo Backend sẽ chạy tại: http://localhost:8080
echo.
echo ⚠️  ĐỢI ĐẾN KHI THẤY: "Application started"
echo.
echo Để dừng: Nhấn Ctrl+C
echo ========================================
echo.

call gradlew.bat run --no-daemon
