@echo off
setlocal enabledelayedexpansion

REM Script to manually build and push all Docker images to GitHub Container Registry
REM Usage: push-to-ghcr.bat [username] [tag]

echo ========================================
echo Building and Pushing to GHCR
echo ========================================
echo.

REM Get GitHub username from argument or prompt
set GITHUB_USERNAME=%1
if "%GITHUB_USERNAME%"=="" (
    set /p GITHUB_USERNAME="Enter your GitHub username: "
)

REM Get tag from argument or use latest
set TAG=%2
if "%TAG%"=="" set TAG=latest

set REGISTRY=ghcr.io
set IMAGE_PREFIX=%GITHUB_USERNAME%/quizzler-backend

echo Registry: %REGISTRY%
echo Image Prefix: %IMAGE_PREFIX%
echo Tag: %TAG%
echo.

REM Check if Docker is running
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Docker is not running. Please start Docker Desktop.
    pause
    exit /b 1
)

REM Login to GHCR
echo [INFO] Logging in to GitHub Container Registry...
echo You need a Personal Access Token (PAT) with 'write:packages' permission
echo Create one at: https://github.com/settings/tokens
echo.
set /p GITHUB_TOKEN="Enter your GitHub Personal Access Token: "

echo %GITHUB_TOKEN% | docker login ghcr.io -u %GITHUB_USERNAME% --password-stdin

if %errorlevel% neq 0 (
    echo [ERROR] Failed to login to GHCR
    pause
    exit /b 1
)

echo [OK] Logged in successfully
echo.

REM Services to build and push
set SERVICES[0]=service-registry:./service-registry
set SERVICES[1]=api-gateway:./api-gateway
set SERVICES[2]=user-auth:./user-auth
set SERVICES[3]=quiz-service:./Quiz
set SERVICES[4]=question-service:./question-service
set SERVICES[5]=participation-service:./participation-service
set SERVICES[6]=leaderboard:./LeaderBoard
set SERVICES[7]=java-judge:./JavaJudge
set SERVICES[8]=bugreport-service:./BugReport-service

REM Build and push each service
for /L %%i in (0,1,8) do (
    set SERVICE_INFO=!SERVICES[%%i]!
    for /f "tokens=1,2 delims=:" %%a in ("!SERVICE_INFO!") do (
        set SERVICE_NAME=%%a
        set SERVICE_PATH=%%b
        set IMAGE_NAME=%REGISTRY%/%IMAGE_PREFIX%/!SERVICE_NAME!:%TAG%
        
        echo [INFO] Building !SERVICE_NAME!...
        docker build -t !IMAGE_NAME! -f !SERVICE_PATH!/Dockerfile !SERVICE_PATH!
        
        if !errorlevel! equ 0 (
            echo [OK] Built !SERVICE_NAME!
            echo [INFO] Pushing !SERVICE_NAME!...
            docker push !IMAGE_NAME!
            
            if !errorlevel! equ 0 (
                echo [OK] Pushed !SERVICE_NAME!
            ) else (
                echo [ERROR] Failed to push !SERVICE_NAME!
            )
        ) else (
            echo [ERROR] Failed to build !SERVICE_NAME!
        )
        echo.
    )
)

echo ========================================
echo All images pushed successfully!
echo ========================================
echo.
echo View your images at: https://github.com/%GITHUB_USERNAME%?tab=packages
echo.

pause




