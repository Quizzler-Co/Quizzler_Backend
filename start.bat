@echo off
echo ==========================================
echo Starting Quizzler Backend Services
echo ==========================================
echo.

REM Check if Docker is running
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Docker is not running. Please start Docker Desktop.
    pause
    exit /b 1
)

echo [OK] Docker is running
echo.

REM Start all services
echo [INFO] Building and starting all services...
echo [INFO] This may take a few minutes on first run...
echo.

docker-compose up --build

pause

