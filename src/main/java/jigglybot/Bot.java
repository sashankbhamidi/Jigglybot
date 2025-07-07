package jigglybot;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.gateway.intent.Intent;
import discord4j.gateway.intent.IntentSet;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.reaction.Reaction;
import jigglybot.battle.action.MoveList;
import jigglybot.commands.SlashCommandManager;
import jigglybot.location.Location;
import jigglybot.monster.Dex;
import jigglybot.monster.Species;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.File;

public class Bot
{
    private static final Logger logger = LoggerFactory.getLogger(Bot.class);
    
    public static String token;
    public static DiscordClient client;
    public static GatewayDiscordClient gateway;
    private static SlashCommandManager slashCommandManager;

    public static void main(final String[] args)
    {
        logger.info("Starting JigglyBot...");
        
        // Load environment variables from .env file (if present)
        Dotenv dotenv = null;
        try {
            dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();
            logger.info("Loaded .env file configuration");
        } catch (Exception e) {
            logger.info("No .env file found, using system environment variables");
        }
        
        // Get Discord token from environment variables or command line args
        token = getToken(dotenv, args);
        if (token == null || token.trim().isEmpty()) {
            logger.error("Discord token not found!");
            logger.error("Please either:");
            logger.error("1. Create a .env file with DISCORD_TOKEN=your_token_here");
            logger.error("2. Set the DISCORD_TOKEN environment variable");
            logger.error("3. Pass the token as a command line argument");
            System.exit(1);
        }
        
        logger.info("Discord token loaded successfully");
        
        // Initialize game data
        Species.setup();
        Location.setup();
        Dex.setup();
        MoveList.setup();

        // Ensure userdata directory exists
        File userdataDir = new File(UserWrapper.save_dir);
        if (!userdataDir.exists()) {
            boolean created = userdataDir.mkdirs();
            if (created) {
                logger.info("Created userdata directory: {}", userdataDir.getAbsolutePath());
            } else {
                logger.error("Failed to create userdata directory: {}", userdataDir.getAbsolutePath());
            }
        } else {
            logger.info("Userdata directory exists: {}", userdataDir.getAbsolutePath());
        }

        client = DiscordClient.create(token);
        
        // Configure gateway intents - minimal intents for slash commands only
        IntentSet intents = IntentSet.of(
            Intent.GUILDS,                    // Basic guild information (required for slash commands)
            Intent.GUILD_MESSAGE_REACTIONS    // Reaction events (for message navigation arrows)
        );
        
        gateway = client.gateway()
            .setEnabledIntents(intents)
            .login()
            .block();

        logger.info("Connected to Discord!");

        // Initialize slash command manager
        slashCommandManager = new SlashCommandManager(gateway);
        
        // Register slash commands
        logger.info("Registering slash commands...");
        slashCommandManager.registerCommands();
        
        // Setup event handlers
        slashCommandManager.setupEventHandlers();
        
        // Setup reaction event handler (for message navigation)
        setupReactionHandler();

        logger.info("JigglyBot is ready!");
        gateway.onDisconnect().block();
    }
    
    /**
     * Get Discord token from multiple sources in priority order:
     * 1. Command line argument
     * 2. .env file DISCORD_TOKEN
     * 3. System environment variable DISCORD_TOKEN
     */
    private static String getToken(Dotenv dotenv, String[] args) {
        // Priority 1: Command line argument (for backwards compatibility)
        if (args.length > 0 && !args[0].trim().isEmpty()) {
            logger.info("Using token from command line argument");
            return args[0].trim();
        }
        
        // Priority 2: .env file
        if (dotenv != null) {
            String envToken = dotenv.get("DISCORD_TOKEN");
            if (envToken != null && !envToken.trim().isEmpty()) {
                logger.info("Using token from .env file");
                return envToken.trim();
            }
        }
        
        // Priority 3: System environment variable
        String sysToken = System.getenv("DISCORD_TOKEN");
        if (sysToken != null && !sysToken.trim().isEmpty()) {
            logger.info("Using token from system environment variable");
            return sysToken.trim();
        }
        
        return null;
    }

    private static void setupReactionHandler() {
        gateway.on(ReactionAddEvent.class).subscribe(event ->
        {
            Message message = event.getMessage().block();
            MessageChannel channel = message.getChannel().block();
            ChannelWrapper cw = ChannelWrapper.get(channel);

            if (cw.activeMessage == message.getId().asLong())
            {
                for (Reaction r: message.getReactions())
                {
                    if (r.getEmoji().asUnicodeEmoji().get().getRaw().equals("\uD83D\uDD3D") && r.getCount() > 1)
                    {
                        cw.advance();
                        break;
                    }
                }
            }
        });
    }

    // Keep the printLocation method as it's used by both legacy and slash commands
    public static String getLocationString(Location l, boolean showCurrent)
    {
        StringBuilder s = new StringBuilder("```");

        if (showCurrent)
            s.append("Currently in ").append(l.name).append("\n\n");

        s.append("Things to do here:\n");

        if (l.hasCenter)
        {
            s.append("HEAL your POKéMON at the POKéMON CENTER!\n");
            s.append("DEPOSIT, WITHDRAW, or RELEASE POKéMON with the POKéMON CENTER's STORAGE PC!\n");
        }

        if (!l.spawnEntries.isEmpty())
            s.append("Encounter wild POKéMON!\n");

        s.append("\nMOVE to: \n");

        for (int i = 0; i < l.neighbors.length; i++)
            s.append(i + 1).append(". ").append(l.neighbors[i].name).append("\n");

        return s.toString().substring(0, s.length() - 1) + "```";
    }

    public static void printLocation(MessageChannel channel, Member m, boolean showCurrent)
    {
        Location l = ChannelWrapper.get(channel).location;
        channel.createMessage(getLocationString(l, showCurrent)).block();
    }
}
