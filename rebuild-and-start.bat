@echo off
echo ========================================
echo REBUILDING FITSET BACKEND WITH GEMINI AI
echo ========================================
echo.

cd /d "%~dp0backend"

echo [1/3] Cleaning previous build...
call mvn clean
if errorlevel 1 (
    echo ERROR: Maven clean failed!
    pause
    exit /b 1
)

echo.
echo [2/3] Installing dependencies and building...
call mvn install -DskipTests
if errorlevel 1 (
    echo ERROR: Maven install failed!
    pause
    exit /b 1
)

echo.
echo [3/3] Starting backend server...
echo Backend will start at: http://localhost:8080
echo Press Ctrl+C to stop the server
echo.

call mvn spring-boot:run

pause
