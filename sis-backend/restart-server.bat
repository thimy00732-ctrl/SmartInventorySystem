@echo off
echo ================================================
echo    RESTART BACKEND SERVER
echo ================================================
echo.
echo [1/3] Stopping existing server...
taskkill /F /IM java.exe /FI "WINDOWTITLE eq *sis-backend*" 2>nul
timeout /t 2 /nobreak >nul

echo.
echo [2/3] Building latest code...
call gradlew.bat build -x test
if errorlevel 1 (
    echo.
    echo ERROR: Build failed!
    pause
    exit /b 1
)

echo.
echo [3/3] Starting server...
echo Server will start in new window...
start "SIS Backend Server" cmd /k "gradlew.bat run"

echo.
echo ================================================
echo    Backend server started successfully!
echo    Check the new window for server logs
echo ================================================
echo.
pause
