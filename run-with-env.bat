@echo off
REM JigglyBot Startup Script with Environment Variables for Windows
REM This script will start the bot using environment variables

echo Starting JigglyBot with environment variables...

REM Check if .env file exists
if not exist ".env" (
    echo ❌ Error: .env file not found!
    echo Please create a .env file with your Discord token:
    echo copy .env.example .env
    echo Then edit .env and add your DISCORD_TOKEN
    pause
    exit /b 1
)

REM Check if jar file exists
if not exist "target\jigglybot-2.0.0.jar" (
    echo Building JigglyBot...
    call mvn clean package -q
    if errorlevel 1 (
        echo ❌ Build failed!
        pause
        exit /b 1
    )
)

REM Start the bot
echo 🚀 Starting JigglyBot...
java -jar target\jigglybot-2.0.0.jar

echo JigglyBot stopped.
pause