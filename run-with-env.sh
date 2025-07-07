#!/bin/bash

# JigglyBot Startup Script with Environment Variables
# This script will load environment variables from .env file and start the bot

echo "Starting JigglyBot with environment variables..."

# Check if .env file exists
if [ ! -f ".env" ]; then
    echo "❌ Error: .env file not found!"
    echo "Please create a .env file with your Discord token:"
    echo "cp .env.example .env"
    echo "Then edit .env and add your DISCORD_TOKEN"
    exit 1
fi

# Check if jar file exists
if [ ! -f "target/jigglybot-2.0.0.jar" ]; then
    echo "Building JigglyBot..."
    mvn clean package -q
    if [ $? -ne 0 ]; then
        echo "❌ Build failed!"
        exit 1
    fi
fi

# Start the bot
echo "🚀 Starting JigglyBot..."
java -jar target/jigglybot-2.0.0.jar

echo "JigglyBot stopped."