# JigglyBot Setup Guide

## Prerequisites

- **Java 11 or higher**
- **Maven 3.6 or higher**
- **Discord Bot Token** (from Discord Developer Portal)

## Environment Variables Setup

JigglyBot supports multiple ways to configure your Discord token:

### Option 1: Using .env File (Recommended)

1. **Copy the example file:**
   ```bash
   cp .env.example .env
   ```

2. **Edit the .env file:**
   ```bash
   nano .env  # or use your preferred editor
   ```

3. **Add your Discord token:**
   ```env
   DISCORD_TOKEN=your_actual_discord_token_here
   DISCORD_APP_ID=your_application_id_here
   ENVIRONMENT=development
   ```

4. **Run the bot:**
   ```bash
   ./run-with-env.sh    # Linux/Mac
   run-with-env.bat     # Windows
   ```

### Option 2: System Environment Variables

**Linux/Mac:**
```bash
export DISCORD_TOKEN="your_token_here"
java -jar target/jigglybot-2.0.0.jar
```

**Windows (Command Prompt):**
```cmd
set DISCORD_TOKEN=your_token_here
java -jar target/jigglybot-2.0.0.jar
```

**Windows (PowerShell):**
```powershell
$env:DISCORD_TOKEN="your_token_here"
java -jar target/jigglybot-2.0.0.jar
```

### Option 3: Command Line Argument (Legacy)

```bash
java -jar target/jigglybot-2.0.0.jar YOUR_TOKEN_HERE
```

## Building the Bot

### Manual Build
```bash
mvn clean package
```

This creates `target/jigglybot-2.0.0.jar`

### Using Run Scripts
The run scripts automatically build if needed:
```bash
./run-with-env.sh    # Builds automatically if JAR is missing
```

## Deployment Options

### Local Development

1. **Clone the main repository:**
   ```bash
   git clone https://github.com/aehmttw/Jigglybot.git
   cd Jigglybot
   ```

2. **Set up environment:**
   ```bash
   cp .env.example .env
   # Edit .env with your token
   ```

3. **Run:**
   ```bash
   ./run-with-env.sh
   ```

### Pterodactyl Panel Deployment

1. **Upload files:**
   - Upload the entire project or just the JAR file
   - Ensure `userdata/` directory exists (created automatically)

2. **Set environment variables in Pterodactyl:**
   - Go to your server settings
   - Add environment variable: `DISCORD_TOKEN` with your token

3. **Startup command:**
   ```bash
   java -jar jigglybot-2.0.0.jar
   ```

4. **Alternative with build:**
   ```bash
   mvn clean package && java -jar target/jigglybot-2.0.0.jar
   ```

### VPS/Dedicated Server

1. **Install dependencies:**
   ```bash
   # Ubuntu/Debian
   sudo apt update
   sudo apt install openjdk-11-jdk maven

   # CentOS/RHEL
   sudo yum install java-11-openjdk maven
   ```

2. **Deploy:**
   ```bash
   git clone https://github.com/aehmttw/Jigglybot.git
   cd Jigglybot
   cp .env.example .env
   # Edit .env
   mvn clean package
   java -jar target/jigglybot-2.0.0.jar
   ```

3. **Run as service (optional):**
   ```bash
   # Create systemd service
   sudo nano /etc/systemd/system/jigglybot.service
   ```
   
   Service file content:
   ```ini
   [Unit]
   Description=JigglyBot Discord Pokemon Bot
   After=network.target
   
   [Service]
   Type=simple
   User=your-user
   WorkingDirectory=/path/to/Jigglybot
   ExecStart=/usr/bin/java -jar target/jigglybot-2.0.0.jar
   EnvironmentFile=/path/to/Jigglybot/.env
   Restart=always
   RestartSec=10
   
   [Install]
   WantedBy=multi-user.target
   ```

## Getting Your Discord Token

1. **Go to Discord Developer Portal:**
   https://discord.com/developers/applications

2. **Create a new application** or select existing one

3. **Go to "Bot" section**

4. **Copy the token** (keep it secret!)

5. **Set bot permissions:**
   - Go to "Installation" tab in Discord Developer Portal
   - Under "Default Install Settings", select:
     - Send Messages
     - Use Slash Commands
     - Add Reactions
     - Read Message History
     - Attach Files

6. **Invite your bot:**
   - Use the simplified invite URL: `https://discord.com/oauth2/authorize?client_id=YOUR_APP_ID`
   - Replace `YOUR_APP_ID` with your actual Application ID
   - Example: `https://discord.com/oauth2/authorize?client_id=1391433586187436135`
   - The permissions are automatically applied from your Developer Portal settings

## Troubleshooting

### Common Issues

**"Invalid token" error:**
- Double-check your token in .env file
- Make sure there are no extra spaces
- Verify the token in Discord Developer Portal

**"No such file or directory" error:**
- Ensure `userdata/` directory exists
- Check file permissions
- The bot creates this automatically on first run

**Build failures:**
- Check Java version: `java -version` (need 11+)
- Check Maven version: `mvn -version` (need 3.6+)
- Clear Maven cache: `mvn clean`

**Permission errors:**
- Make sure run scripts are executable: `chmod +x run-with-env.sh`
- Check bot permissions in Discord server

### Logs and Debugging

**Log files location:**
- `logs/jigglybot.log` - Current log
- `logs/jigglybot.YYYY-MM-DD.log` - Daily logs

**Enable debug logging:**
Edit `src/main/resources/logback.xml` and change level to `DEBUG`

**Check bot status:**
```bash
# View recent logs
tail -f logs/jigglybot.log

# Check if process is running
ps aux | grep jigglybot
```

## Security Notes

- ✅ Never commit your `.env` file to version control
- ✅ Keep your Discord token private
- ✅ Use environment variables in production
- ✅ Regularly rotate your bot token
- ⚠️ Don't share your token in screenshots or logs

## Performance Tips

- **Memory allocation:** `-Xmx512m` for most servers
- **Garbage collection:** `-XX:+UseG1GC` for better performance
- **Startup example:**
  ```bash
  java -Xmx512m -XX:+UseG1GC -jar target/jigglybot-2.0.0.jar
  ```

## Support

- **Issues:** Open an issue on GitHub
- **Documentation:** Check README.md for command list
- **Discord:** Test bot functionality with `/help` command