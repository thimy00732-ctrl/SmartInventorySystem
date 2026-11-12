@echo off
echo Testing backend API...
echo.
curl -s http://localhost:8080/api/categories/1
echo.
echo.
pause
