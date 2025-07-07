# JigglyBot 🎮

A Discord Pokemon bot with modern slash commands, built using Discord4J and Java 11.

> **Note:** This repository includes modern slash command implementation and Discord4J 3.2.6 migration. 
> Based on the original JigglyBot by [aehmttw](https://github.com/aehmttw/Jigglybot).

## Features

- 🎯 **Modern Slash Commands** - All commands use Discord's latest slash command system
- ⚔️ **Pokemon Battles** - Real-time turn-based battles with move selection
- 📦 **Pokemon Collection** - Catch, store, and manage your Pokemon team
- 🏥 **Pokemon Centers** - Heal your Pokemon and manage PC storage
- 🗺️ **Location System** - Travel between different areas and encounter wild Pokemon
- 📊 **Pokemon Stats** - View detailed stats, moves, and level progression
- 🔒 **Secure Configuration** - Environment variable support for tokens

## Quick Start

1. **Clone the repository**
   ```bash
   git clone https://github.com/aehmttw/Jigglybot.git
   cd Jigglybot
   ```

2. **Set up your Discord token**
   ```bash
   cp .env.example .env
   # Edit .env and add your DISCORD_TOKEN
   ```

3. **Build and run**
   ```bash
   mvn clean package
   ./run-with-env.sh
   ```

For detailed setup instructions, see [SETUP.md](SETUP.md).

## Slash Commands

- `/start` - Begin your Pokemon journey
- `/pokemon` - View your Pokemon team
- `/battle` - Start a battle with wild Pokemon
- `/join` - Join an ongoing battle
- `/fight` - Use a move in battle
- `/catch` - Attempt to catch a wild Pokemon
- `/heal` - Heal your Pokemon at a Pokemon Center
- `/location` - View current location and travel options
- `/move` - Travel to a different location
- `/dex` - View your Pokedex
- And many more...

## Requirements

- Java 11 or higher
- Maven 3.6 or higher
- Discord Bot Token

## Architecture

- **Discord4J 3.2.6** - Modern Discord API wrapper
- **Slash Commands** - All interactions use Discord's slash command system
- **Reactive Programming** - Non-blocking operations with Reactor
- **Environment Variables** - Secure token management
- **Maven** - Dependency management and building
