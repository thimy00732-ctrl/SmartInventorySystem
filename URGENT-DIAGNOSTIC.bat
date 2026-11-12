@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ╔════════════════════════════════════════════════════════════╗
echo ║  🚨 URGENT DIAGNOSTIC - Category Toggle Issue           ║
echo ╔════════════════════════════════════════════════════════════╗
echo.
echo This script will collect diagnostic information.
echo Please send the output file to developer.
echo.
echo Press any key to start...
pause >nul
cls

set OUTPUT_FILE=diagnostic-report-%date:~10,4%%date:~4,2%%date:~7,2%-%time:~0,2%%time:~3,2%%time:~6,2%.txt
set OUTPUT_FILE=%OUTPUT_FILE: =0%

echo Generating diagnostic report: %OUTPUT_FILE%
echo.

(
echo ════════════════════════════════════════════════════════════
echo   DIAGNOSTIC REPORT - Category Toggle Issue
echo   Generated: %date% %time%
echo ════════════════════════════════════════════════════════════
echo.
echo.

echo [1] JAVA PROCESSES
echo ════════════════════════════════════════════════════════════
tasklist /FI "IMAGENAME eq java.exe" /FO TABLE /V
if errorlevel 1 (
    echo No Java processes found.
) 
echo.
echo.

echo [2] PORT 8080 STATUS
echo ════════════════════════════════════════════════════════════
netstat -ano | findstr :8080
if errorlevel 1 (
    echo Port 8080 is NOT in use - Backend NOT running!
) else (
    echo Port 8080 is in use - Backend appears to be running
)
echo.
echo.

echo [3] BACKEND API TEST - Health Check
echo ════════════════════════════════════════════════════════════
curl -s http://localhost:8080/health 2>nul
if errorlevel 1 (
    echo [FAIL] Cannot connect to backend
) else (
    echo [OK] Backend responded
)
echo.
echo.

echo [4] BACKEND API TEST - Get Categories
echo ════════════════════════════════════════════════════════════
curl -s http://localhost:8080/api/categories 2>nul > temp-categories.json
if errorlevel 1 (
    echo [FAIL] Cannot get categories from backend
) else (
    echo [OK] Categories API responded
    echo.
    echo First 500 characters of response:
    type temp-categories.json | findstr /C:"categoryId" /C:"categoryName" /C:"isActive"
)
echo.
echo.

echo [5] BACKEND API TEST - Reactivate Category 16
echo ════════════════════════════════════════════════════════════
curl -X PATCH -s -w "HTTP Status: %%{http_code}\n" http://localhost:8080/api/categories/16/reactivate 2>nul
echo.
echo.

echo [6] BUILD FILES TIMESTAMP
echo ════════════════════════════════════════════════════════════
echo CategoryRepository.class:
if exist "sis-backend\build\classes\kotlin\main\com\sis\backend\repository\CategoryRepository.class" (
    dir "sis-backend\build\classes\kotlin\main\com\sis\backend\repository\CategoryRepository.class" | findstr /C:"CategoryRepository"
) else (
    echo File not found - Backend not built?
)
echo.
echo JAR file:
if exist "sis-backend\build\libs\*.jar" (
    dir "sis-backend\build\libs\*.jar" | findstr /C:".jar"
) else (
    echo No JAR files found
)
echo.
echo.

echo [7] RECENT BUILD LOG
echo ════════════════════════════════════════════════════════════
if exist "sis-backend\build-log.txt" (
    echo Last 20 lines of build log:
    powershell -Command "Get-Content 'sis-backend\build-log.txt' -Tail 20"
) else (
    echo No build log found
)
echo.
echo.

echo [8] MYSQL DATABASE CHECK
echo ════════════════════════════════════════════════════════════
echo Checking MySQL connection...
mysql -uroot -e "SELECT 'MySQL Connected' as status;" 2>nul
if errorlevel 1 (
    echo [FAIL] Cannot connect to MySQL
    echo Trying with password...
    mysql -uroot -proot -e "SELECT 'MySQL Connected' as status;" 2>nul
    if errorlevel 1 (
        echo [FAIL] Cannot connect to MySQL with common passwords
        echo Please check manually in phpMyAdmin
    ) else (
        echo [OK] MySQL Connected with password
        echo.
        echo Categories in database:
        mysql -uroot -proot warehouse_db -e "SELECT category_id, category_name, is_active FROM categories ORDER BY category_id LIMIT 5;"
    )
) else (
    echo [OK] MySQL Connected
    echo.
    echo Categories in database:
    mysql -uroot warehouse_db -e "SELECT category_id, category_name, is_active FROM categories ORDER BY category_id LIMIT 5;"
)
echo.
echo.

echo [9] GRADLE WRAPPER VERSION
echo ════════════════════════════════════════════════════════════
cd sis-backend
call gradlew.bat --version 2>nul
cd ..
echo.
echo.

echo [10] SYSTEM INFORMATION
echo ════════════════════════════════════════════════════════════
echo Computer: %COMPUTERNAME%
echo User: %USERNAME%
echo Date: %date%
echo Time: %time%
systeminfo | findstr /C:"OS Name" /C:"OS Version" /C:"System Type"
echo.
echo.

echo ════════════════════════════════════════════════════════════
echo   END OF DIAGNOSTIC REPORT
echo ════════════════════════════════════════════════════════════

) > %OUTPUT_FILE% 2>&1

del temp-categories.json 2>nul

echo.
echo ════════════════════════════════════════════════════════════
echo   ✅ DIAGNOSTIC COMPLETE
echo ════════════════════════════════════════════════════════════
echo.
echo Report saved to: %OUTPUT_FILE%
echo.
echo ⚠️  IMPORTANT: Please do the following:
echo.
echo 1. Open file: %OUTPUT_FILE%
echo 2. Copy ALL content
echo 3. Send to developer
echo.
echo 4. Also check phpMyAdmin:
echo    - Open: http://localhost/phpmyadmin
echo    - Database: warehouse_db
echo    - Table: categories
echo    - Take screenshot of first 5 rows
echo.
echo Press any key to open the report file...
pause >nul
notepad %OUTPUT_FILE%
