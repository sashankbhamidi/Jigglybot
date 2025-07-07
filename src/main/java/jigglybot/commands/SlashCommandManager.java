package jigglybot.commands;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.rest.RestClient;
import jigglybot.Bot;
import jigglybot.ChannelWrapper;
import jigglybot.UserWrapper;
import jigglybot.battle.Battle;
import jigglybot.battle.BattleUtils;
import jigglybot.battle.Trainer;
import jigglybot.dialog.DialogEraseAllData;
import jigglybot.location.Location;
import jigglybot.monster.Monster;
import jigglybot.monster.Species;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

public class SlashCommandManager {
    
    private static final Logger logger = LoggerFactory.getLogger(SlashCommandManager.class);
    
    private final GatewayDiscordClient gateway;
    private final RestClient restClient;
    private final long applicationId;

    public SlashCommandManager(GatewayDiscordClient gateway) {
        this.gateway = gateway;
        this.restClient = gateway.getRestClient();
        this.applicationId = gateway.getRestClient().getApplicationId().block();
    }

    public void registerCommands() {
        List<ApplicationCommandRequest> commands = new ArrayList<>();

        // Basic commands
        commands.add(ApplicationCommandRequest.builder()
            .name("help")
            .description("Get help with using JigglyBot")
            .build());

        commands.add(ApplicationCommandRequest.builder()
            .name("start")
            .description("Start your Pokemon journey")
            .addOption(ApplicationCommandOptionData.builder()
                .name("pokemon")
                .description("Choose your starter Pokemon")
                .type(3) // STRING type
                .required(false)
                .addChoice(ApplicationCommandOptionChoiceData.builder().name("Bulbasaur").value("bulbasaur").build())
                .addChoice(ApplicationCommandOptionChoiceData.builder().name("Charmander").value("charmander").build())
                .addChoice(ApplicationCommandOptionChoiceData.builder().name("Squirtle").value("squirtle").build())
                .addChoice(ApplicationCommandOptionChoiceData.builder().name("Pikachu").value("pikachu").build())
                .build())
            .build());

        commands.add(ApplicationCommandRequest.builder()
            .name("location")
            .description("Show your current location and available actions")
            .build());

        commands.add(ApplicationCommandRequest.builder()
            .name("pokemon")
            .description("View your Pokemon")
            .addOption(ApplicationCommandOptionData.builder()
                .name("number")
                .description("Pokemon number to view details")
                .type(4) // INTEGER type
                .required(false)
                .build())
            .build());

        commands.add(ApplicationCommandRequest.builder()
            .name("stats")
            .description("View Pokemon stats")
            .addOption(ApplicationCommandOptionData.builder()
                .name("number")
                .description("Pokemon number to view stats")
                .type(4) // INTEGER type
                .required(true)
                .build())
            .build());

        commands.add(ApplicationCommandRequest.builder()
            .name("swap")
            .description("Swap two Pokemon positions")
            .addOption(ApplicationCommandOptionData.builder()
                .name("first")
                .description("First Pokemon position")
                .type(4) // INTEGER type
                .required(true)
                .build())
            .addOption(ApplicationCommandOptionData.builder()
                .name("second")
                .description("Second Pokemon position")
                .type(4) // INTEGER type
                .required(true)
                .build())
            .build());

        commands.add(ApplicationCommandRequest.builder()
            .name("move")
            .description("Move to a different location")
            .addOption(ApplicationCommandOptionData.builder()
                .name("location")
                .description("Location number to move to")
                .type(4) // INTEGER type
                .required(true)
                .build())
            .build());

        commands.add(ApplicationCommandRequest.builder()
            .name("spawn")
            .description("Encounter a wild Pokemon")
            .build());

        commands.add(ApplicationCommandRequest.builder()
            .name("battle")
            .description("Start a trainer battle")
            .build());

        commands.add(ApplicationCommandRequest.builder()
            .name("heal")
            .description("Heal your Pokemon at a Pokemon Center")
            .build());

        commands.add(ApplicationCommandRequest.builder()
            .name("dex")
            .description("View Pokedex entries")
            .addOption(ApplicationCommandOptionData.builder()
                .name("query")
                .description("Pokemon name, number, or 'all'")
                .type(3) // STRING type
                .required(true)
                .build())
            .build());

        commands.add(ApplicationCommandRequest.builder()
            .name("save")
            .description("Save your game progress")
            .build());

        commands.add(ApplicationCommandRequest.builder()
            .name("reset")
            .description("Reset your game data")
            .addOption(ApplicationCommandOptionData.builder()
                .name("confirm")
                .description("Type 'yes' to confirm deletion of all data")
                .type(3) // STRING type
                .required(false)
                .addChoice(ApplicationCommandOptionChoiceData.builder().name("Yes, delete everything").value("yes").build())
                .addChoice(ApplicationCommandOptionChoiceData.builder().name("No, cancel").value("no").build())
                .build())
            .build());

        // PC Commands
        commands.add(ApplicationCommandRequest.builder()
            .name("deposit")
            .description("Deposit a Pokemon to PC storage")
            .addOption(ApplicationCommandOptionData.builder()
                .name("pokemon")
                .description("Pokemon number to deposit")
                .type(4) // INTEGER type
                .required(true)
                .build())
            .build());

        commands.add(ApplicationCommandRequest.builder()
            .name("withdraw")
            .description("Withdraw a Pokemon from PC storage")
            .addOption(ApplicationCommandOptionData.builder()
                .name("pokemon")
                .description("Pokemon number to withdraw")
                .type(4) // INTEGER type
                .required(true)
                .build())
            .build());

        commands.add(ApplicationCommandRequest.builder()
            .name("release")
            .description("Release Pokemon from PC storage (PERMANENT!)")
            .addOption(ApplicationCommandOptionData.builder()
                .name("pokemon")
                .description("Pokemon numbers to release (space separated)")
                .type(3) // STRING type
                .required(true)
                .build())
            .addOption(ApplicationCommandOptionData.builder()
                .name("confirm")
                .description("Type 'CONFIRM' to actually release the Pokemon")
                .type(3) // STRING type
                .required(false)
                .addChoice(ApplicationCommandOptionChoiceData.builder().name("CONFIRM - Release Pokemon").value("CONFIRM").build())
                .addChoice(ApplicationCommandOptionChoiceData.builder().name("Cancel").value("CANCEL").build())
                .build())
            .build());

        commands.add(ApplicationCommandRequest.builder()
            .name("page")
            .description("Change PC storage page")
            .addOption(ApplicationCommandOptionData.builder()
                .name("number")
                .description("Page number")
                .type(4) // INTEGER type
                .required(true)
                .build())
            .build());

        // Battle Commands
        commands.add(ApplicationCommandRequest.builder()
            .name("join")
            .description("Join a battle")
            .addOption(ApplicationCommandOptionData.builder()
                .name("pokemon")
                .description("Pokemon to use in battle")
                .type(3) // STRING type
                .required(false)
                .build())
            .build());

        commands.add(ApplicationCommandRequest.builder()
            .name("fight")
            .description("Use a move in battle")
            .addOption(ApplicationCommandOptionData.builder()
                .name("move")
                .description("Move to use")
                .type(3) // STRING type
                .required(false)
                .build())
            .build());

        commands.add(ApplicationCommandRequest.builder()
            .name("switch")
            .description("Switch Pokemon in battle")
            .addOption(ApplicationCommandOptionData.builder()
                .name("pokemon")
                .description("Pokemon to switch to")
                .type(3) // STRING type
                .required(false)
                .build())
            .build());

        commands.add(ApplicationCommandRequest.builder()
            .name("catch")
            .description("Try to catch a wild Pokemon")
            .build());

        commands.add(ApplicationCommandRequest.builder()
            .name("run")
            .description("Run from battle")
            .build());

        commands.add(ApplicationCommandRequest.builder()
            .name("next")
            .description("Advance to next message")
            .build());

        // Register all commands
        for (ApplicationCommandRequest command : commands) {
            restClient.getApplicationService()
                .createGlobalApplicationCommand(applicationId, command)
                .doOnError(error -> System.err.println("Failed to register command: " + command.name() + " - " + error.getMessage()))
                .doOnSuccess(cmd -> System.out.println("Registered command: " + cmd.name()))
                .subscribe();
        }
    }

    public void setupEventHandlers() {
        gateway.on(ChatInputInteractionEvent.class, this::handleSlashCommand).subscribe();
    }

    private Mono<Void> handleSlashCommand(ChatInputInteractionEvent event) {
        String commandName = event.getCommandName();
        
        // Get user and channel wrappers with error handling
        try {
            if (event.getInteraction().getMember().isEmpty()) {
                return event.reply("❌ **Error:** Could not identify user!").withEphemeral(true);
            }
            
            UserWrapper user = UserWrapper.get(event.getInteraction().getMember().orElse(null));
            ChannelWrapper channel = ChannelWrapper.get(event.getInteraction().getChannel().block());
            
            if (user == null || channel == null) {
                return event.reply("❌ **Error:** Could not initialize user or channel data!").withEphemeral(true);
            }

        switch (commandName) {
            case "help":
                return handleHelp(event, user);
            case "start":
                return handleStart(event, user, channel);
            case "location":
                return handleLocation(event, user, channel);
            case "pokemon":
                return handlePokemon(event, user, channel);
            case "stats":
                return handleStats(event, user, channel);
            case "swap":
                return handleSwap(event, user, channel);
            case "move":
                return handleMove(event, user, channel);
            case "spawn":
                return handleSpawn(event, user, channel);
            case "battle":
                return handleBattle(event, user, channel);
            case "heal":
                return handleHeal(event, user, channel);
            case "dex":
                return handleDex(event, user, channel);
            case "save":
                return handleSave(event, user);
            case "reset":
                return handleReset(event, user, channel);
            case "deposit":
                return handleDeposit(event, user, channel);
            case "withdraw":
                return handleWithdraw(event, user, channel);
            case "release":
                return handleRelease(event, user, channel);
            case "page":
                return handlePage(event, user, channel);
            case "join":
                return handleJoin(event, user, channel);
            case "fight":
                return handleFight(event, user, channel);
            case "switch":
                return handleSwitch(event, user, channel);
            case "catch":
                return handleCatch(event, user, channel);
            case "run":
                return handleRun(event, user, channel);
            case "next":
                return handleNext(event, user, channel);
            default:
                return event.reply("Unknown command!").withEphemeral(true);
        }
        } catch (Exception e) {
            logger.error("Error handling slash command: " + commandName, e);
            return event.reply("❌ **An error occurred!** Please try again or contact support.").withEphemeral(true);
        }
    }

    private Mono<Void> handleHelp(ChatInputInteractionEvent event, UserWrapper user) {
        String helpText = "🎮 **JigglyBot - Discord Pokemon Adventure**\n\n" +
            "**✨ Getting Started:**\n" +
            "• `/start` - Begin your Pokemon journey\n" +
            "• `/help` - Show this help message\n\n" +
            "**🗺️ Exploration:**\n" +
            "• `/location` - Show current location and actions\n" +
            "• `/move location:<number>` - Travel to a new location\n" +
            "• `/spawn` - Encounter wild Pokemon\n\n" +
            "**👾 Pokemon Management:**\n" +
            "• `/pokemon` - View your Pokemon team\n" +
            "• `/stats number:<#>` - View detailed Pokemon stats\n" +
            "• `/swap first:<#> second:<#>` - Rearrange team order\n" +
            "• `/dex query:<name/number/all>` - Browse Pokedex\n\n" +
            "**⚔️ Battle System:**\n" +
            "• `/battle` - Challenge a trainer\n" +
            "• `/join` - Enter an active battle\n" +
            "• `/fight move:<name>` - Use a Pokemon move\n" +
            "• `/switch pokemon:<name>` - Change active Pokemon\n" +
            "• `/catch` - Attempt to capture wild Pokemon\n" +
            "• `/run` - Flee from battle\n\n" +
            "**🏥 Pokemon Center:**\n" +
            "• `/heal` - Restore Pokemon to full health\n" +
            "• `/deposit pokemon:<#>` - Store Pokemon in PC\n" +
            "• `/withdraw pokemon:<#>` - Retrieve from PC\n" +
            "• `/release pokemon:<numbers>` - Release Pokemon\n" +
            "• `/page number:<#>` - Navigate PC storage\n\n" +
            "**💾 Game Management:**\n" +
            "• `/save` - Save your progress\n" +
            "• `/reset` - Reset all game data\n" +
            "• `/next` - Continue message sequences";

        if (!user.initialized) {
            helpText += "\n\n**⚠️ You haven't started yet! Use `/start` to begin your adventure!**";
        }

        return event.reply(helpText).withEphemeral(true);
    }

    private Mono<Void> handleStart(ChatInputInteractionEvent event, UserWrapper user, ChannelWrapper channel) {
        if (user.initialized) {
            return event.reply("You have already started your journey!").withEphemeral(true);
        }

        String pokemonChoice = event.getOption("pokemon")
            .flatMap(option -> option.getValue())
            .map(value -> value.asString())
            .orElse(null);

        if (pokemonChoice == null) {
            return event.reply("**Welcome to the world of Pokemon!**\n\n" +
                "Please choose your starter Pokemon:\n" +
                "• Use `/start pokemon:Bulbasaur`\n" +
                "• Use `/start pokemon:Charmander`\n" +
                "• Use `/start pokemon:Squirtle`\n" +
                "• Use `/start pokemon:Pikachu`");
        }

        Species starter = Species.by_name.get(pokemonChoice.toLowerCase());
        if (starter == null) {
            return event.reply("Invalid Pokemon choice! Please choose Bulbasaur, Charmander, Squirtle, or Pikachu.");
        }

        // Setup starter Pokemon immediately without blocking operations
        user.pickStarterAsync(channel, starter);
        
        return event.reply("**🎉 Congratulations!** You chose " + starter.name.toUpperCase() + " as your starter Pokemon!\n" +
            "**Professor Oak:** Welcome to the world of Pokemon! Your adventure begins now!\n\n" +
            "Use `/location` to see where you are and start exploring!");
    }

    private Mono<Void> handleLocation(ChatInputInteractionEvent event, UserWrapper user, ChannelWrapper channel) {
        if (!user.initialized) {
            return event.reply("You need to start your journey first! Use `/start`").withEphemeral(true);
        }

        Location location = channel.location;
        StringBuilder response = new StringBuilder();
        response.append("**📍 Current Location: ").append(location.name).append("**\n\n");

        response.append("**Things to do here:**\n");
        if (location.hasCenter) {
            response.append("🏥 **Pokemon Center** - Heal your Pokemon with `/heal`\n");
            response.append("💾 **Storage PC** - Manage Pokemon with `/deposit`, `/withdraw`, `/release`\n");
        }

        if (!location.spawnEntries.isEmpty()) {
            response.append("🌱 **Wild Pokemon** - Encounter Pokemon with `/spawn`\n");
        }

        response.append("⚔️ **Trainer Battles** - Challenge trainers with `/battle`\n\n");

        response.append("**Available destinations:**\n");
        for (int i = 0; i < location.neighbors.length; i++) {
            response.append((i + 1)).append(". ").append(location.neighbors[i].name).append("\n");
        }

        response.append("\nUse `/move <number>` to travel to a destination!");

        return event.reply(response.toString());
    }

    private Mono<Void> handlePokemon(ChatInputInteractionEvent event, UserWrapper user, ChannelWrapper channel) {
        if (!user.initialized) {
            return event.reply("You need to start your journey first! Use `/start`").withEphemeral(true);
        }

        Long pokemonNumber = event.getOption("number")
            .flatMap(option -> option.getValue())
            .map(value -> value.asLong())
            .orElse(null);

        if (pokemonNumber != null) {
            try {
                int index = pokemonNumber.intValue() - 1;
                if (index >= 0 && index < user.squad.length && user.squad[index] != null) {
                    return event.reply("**Pokemon #" + pokemonNumber + " Stats:**\n" + user.squad[index].getStatsString());
                } else {
                    return event.reply("Invalid Pokemon number!").withEphemeral(true);
                }
            } catch (Exception e) {
                return event.reply("Invalid Pokemon number!").withEphemeral(true);
            }
        } else {
            return event.reply(user.getMonstersString(channel));
        }
    }

    private Mono<Void> handleStats(ChatInputInteractionEvent event, UserWrapper user, ChannelWrapper channel) {
        if (!user.initialized) {
            return event.reply("You need to start your journey first! Use `/start`").withEphemeral(true);
        }

        long pokemonNumber = event.getOption("number")
            .flatMap(option -> option.getValue())
            .map(value -> value.asLong())
            .orElse(0L);

        try {
            int index = (int) pokemonNumber - 1;
            if (index >= 0 && index < user.squad.length && user.squad[index] != null) {
                return event.reply("**Detailed Stats for Pokemon #" + pokemonNumber + ":**\n" + user.squad[index].getStatsString());
            } else {
                return event.reply("Invalid Pokemon number!").withEphemeral(true);
            }
        } catch (Exception e) {
            return event.reply("Invalid Pokemon number!").withEphemeral(true);
        }
    }

    private Mono<Void> handleSwap(ChatInputInteractionEvent event, UserWrapper user, ChannelWrapper channel) {
        if (!user.initialized) {
            return event.reply("You need to start your journey first! Use `/start`").withEphemeral(true);
        }

        if (user.inBattle) {
            return event.reply("Cannot swap Pokemon during battle!").withEphemeral(true);
        }

        long first = event.getOption("first")
            .flatMap(option -> option.getValue())
            .map(value -> value.asLong())
            .orElse(0L);

        long second = event.getOption("second")
            .flatMap(option -> option.getValue())
            .map(value -> value.asLong())
            .orElse(0L);

        try {
            int firstIndex = (int) first - 1;
            int secondIndex = (int) second - 1;

            if (firstIndex >= 0 && secondIndex >= 0 && firstIndex < user.squad.length && secondIndex < user.squad.length &&
                user.squad[firstIndex] != null && user.squad[secondIndex] != null) {
                
                Monster temp = user.squad[firstIndex];
                user.squad[firstIndex] = user.squad[secondIndex];
                user.squad[secondIndex] = temp;
                
                return event.reply("✅ Swapped " + user.squad[firstIndex].getName() + " and " + user.squad[secondIndex].getName() + "!\n\n" + user.getMonstersString(channel));
            } else {
                return event.reply("Invalid Pokemon numbers!").withEphemeral(true);
            }
        } catch (Exception e) {
            return event.reply("Invalid Pokemon numbers!").withEphemeral(true);
        }
    }

    private Mono<Void> handleMove(ChatInputInteractionEvent event, UserWrapper user, ChannelWrapper channel) {
        if (!user.initialized) {
            return event.reply("You need to start your journey first! Use `/start`").withEphemeral(true);
        }

        if (channel.currentBattle != null) {
            return event.reply("Finish the battle first!").withEphemeral(true);
        }

        long locationNumber = event.getOption("location")
            .flatMap(option -> option.getValue())
            .map(value -> value.asLong())
            .orElse(0L);

        try {
            int index = (int) locationNumber - 1;
            if (index >= 0 && index < channel.location.neighbors.length) {
                Location newLocation = channel.location.neighbors[index];
                channel.location = newLocation;
                return event.reply("🚶 **Moved to " + newLocation.name + "!**\n\n" + Bot.getLocationString(newLocation, true));
            } else {
                return event.reply("Invalid location number!").withEphemeral(true);
            }
        } catch (Exception e) {
            return event.reply("Invalid location number!").withEphemeral(true);
        }
    }

    private Mono<Void> handleSpawn(ChatInputInteractionEvent event, UserWrapper user, ChannelWrapper channel) {
        if (!user.initialized) {
            return event.reply("You need to start your journey first! Use `/start`").withEphemeral(true);
        }

        if (channel.location.spawnEntries.isEmpty()) {
            return event.reply("No Pokemon spawn in " + channel.location.name + "!").withEphemeral(true);
        }

        if (channel.currentBattle != null) {
            return event.reply("Finish the current battle first!").withEphemeral(true);
        }

        Monster monster = channel.location.spawn();
        channel.currentBattle = new Battle(channel, monster);
        
        return event.reply("🌟 **Wild L" + monster.level + " " + monster.getName().toUpperCase() + " appeared!**\n" +
            "Use `/join` to start the battle!");
    }

    private Mono<Void> handleBattle(ChatInputInteractionEvent event, UserWrapper user, ChannelWrapper channel) {
        if (!user.initialized) {
            return event.reply("You need to start your journey first! Use `/start`").withEphemeral(true);
        }

        if (channel.currentBattle != null) {
            return event.reply("Finish the current battle first!").withEphemeral(true);
        }

        Trainer trainer = new Trainer();
        channel.currentBattle = new Battle(channel, trainer);
        
        return event.reply("⚔️ **" + trainer.name + " wants to fight!**\n" +
            "Use `/join` to accept the challenge!");
    }

    private Mono<Void> handleHeal(ChatInputInteractionEvent event, UserWrapper user, ChannelWrapper channel) {
        if (!user.initialized) {
            return event.reply("You need to start your journey first! Use `/start`").withEphemeral(true);
        }

        if (user.inBattle) {
            return event.reply("Finish the battle first!").withEphemeral(true);
        }

        if (!channel.location.hasCenter) {
            return event.reply("There is no Pokemon Center in " + channel.location.name + "!").withEphemeral(true);
        }

        // Heal all Pokemon
        for (int i = 0; i < user.squad.length; i++) {
            if (user.squad[i] != null) {
                user.squad[i].hp = user.squad[i].maxHp;
                user.squad[i].status = 0;
                
                for (int j = 0; j < user.squad[i].moves.length; j++) {
                    if (user.squad[i].moves[j] != null) {
                        user.squad[i].movePP[j] = user.squad[i].moves[j].maxPP;
                    }
                }
            }
        }

        user.save();
        return event.reply("🏥 **Welcome to the Pokemon Center!**\n" +
            "Your Pokemon have been healed to perfect health!\n" +
            "We hope to see you again!");
    }

    private Mono<Void> handleDex(ChatInputInteractionEvent event, UserWrapper user, ChannelWrapper channel) {
        if (!user.initialized) {
            return event.reply("You need to start your journey first! Use `/start`").withEphemeral(true);
        }

        String query = event.getOption("query")
            .flatMap(option -> option.getValue())
            .map(value -> value.asString())
            .orElse("");

        if (query.equals("all")) {
            StringBuilder response = new StringBuilder("**📚 Pokedex Entries:**\n\n");
            
            int lastEntry = 0;
            for (int i : Species.by_num.keySet()) {
                if (user.dex[i] > 0) {
                    lastEntry = i;
                }
            }

            if (lastEntry == 0) {
                return event.reply("You haven't discovered any Pokemon yet! Go explore and catch some Pokemon first!");
            }

            int count = 0;
            for (int i : Species.by_num.keySet()) {
                if (i > lastEntry) break;
                
                response.append(String.format("%03d. ", i));
                if (user.dex[i] <= 0) {
                    response.append("??? - Not discovered\n");
                } else {
                    Species species = Species.by_num.get(i);
                    String status = user.dex[i] >= 2 ? "🟢" : "🟡";
                    response.append(status).append(" ").append(species.name.toUpperCase()).append("\n");
                }
                
                count++;
                if (count >= 20) {
                    response.append("\n*Use `/dex <number>` or `/dex <name>` for detailed info*");
                    break;
                }
            }

            return event.reply(response.toString());
        } else {
            try {
                int num = Integer.parseInt(query);
                Species species = Species.by_num.get(num);
                if (species == null) {
                    return event.reply("No Pokemon found with that number!").withEphemeral(true);
                }
                species.printDexEntry(channel, user, false);
                return event.reply("Showing Pokedex entry for #" + num);
            } catch (NumberFormatException e) {
                Species species = Species.by_name.get(query.toLowerCase());
                if (species == null) {
                    return event.reply("No Pokemon found with that name!").withEphemeral(true);
                }
                species.printDexEntry(channel, user, false);
                return event.reply("Showing Pokedex entry for " + species.name.toUpperCase());
            }
        }
    }

    private Mono<Void> handleSave(ChatInputInteractionEvent event, UserWrapper user) {
        if (!user.initialized) {
            return event.reply("You need to start your journey first! Use `/start`").withEphemeral(true);
        }

        if (user.save()) {
            return event.reply("💾 **Game saved successfully!**");
        } else {
            return event.reply("❌ **Failed to save game!**").withEphemeral(true);
        }
    }

    private Mono<Void> handleReset(ChatInputInteractionEvent event, UserWrapper user, ChannelWrapper channel) {
        if (!user.initialized) {
            return event.reply("You need to start your journey first! Use `/start`").withEphemeral(true);
        }

        String confirmation = event.getOption("confirm")
            .flatMap(option -> option.getValue())
            .map(value -> value.asString())
            .orElse(null);

        if (confirmation == null) {
            return event.reply("⚠️ **DANGER: This will DELETE ALL your Pokemon and progress!**\n\n" +
                "Are you absolutely sure? This action cannot be undone!\n\n" +
                "Use `/reset confirm:yes` if you really want to delete everything.\n" +
                "Use `/reset confirm:no` to cancel.")
                .withEphemeral(true);
        }

        if ("yes".equals(confirmation)) {
            // Perform the reset
            user.reset();
            return event.reply("💥 **Game data has been completely reset!**\n\n" +
                "All your Pokemon, progress, and data have been deleted.\n" +
                "Use `/start` to begin a new adventure!");
        } else {
            return event.reply("✅ **Reset cancelled!** Your data is safe.").withEphemeral(true);
        }
    }

    private Mono<Void> handleDeposit(ChatInputInteractionEvent event, UserWrapper user, ChannelWrapper channel) {
        if (!user.initialized) {
            return event.reply("You need to start your journey first! Use `/start`").withEphemeral(true);
        }

        if (user.inBattle) {
            return event.reply("Finish the battle first!").withEphemeral(true);
        }

        if (!channel.location.hasCenter) {
            return event.reply("There is no Pokemon Center in " + channel.location.name + "!").withEphemeral(true);
        }

        long pokemonNumber = event.getOption("pokemon")
            .flatMap(option -> option.getValue())
            .map(value -> value.asLong())
            .orElse(0L);

        try {
            int index = (int) pokemonNumber - 1;
            if (index >= 0 && index < user.squad.length && user.squad[index] != null) {
                if (index == 0 && user.squad[1] == null) {
                    return event.reply("You can't deposit your last Pokemon!").withEphemeral(true);
                }

                Monster pokemon = user.squad[index];
                user.storage.add(pokemon);

                // Shift Pokemon down
                for (int i = index + 1; i < user.squad.length; i++) {
                    user.squad[i - 1] = user.squad[i];
                    user.squad[i] = null;
                }

                return event.reply("💾 **Sent " + pokemon.getName() + " to Storage PC!**\n\n" + user.getMonstersString(channel));
            } else {
                return event.reply("Invalid Pokemon number!").withEphemeral(true);
            }
        } catch (Exception e) {
            return event.reply("Invalid Pokemon number!").withEphemeral(true);
        }
    }

    private Mono<Void> handleWithdraw(ChatInputInteractionEvent event, UserWrapper user, ChannelWrapper channel) {
        if (!user.initialized) {
            return event.reply("You need to start your journey first! Use `/start`").withEphemeral(true);
        }

        if (user.inBattle) {
            return event.reply("Finish the battle first!").withEphemeral(true);
        }

        if (!channel.location.hasCenter) {
            return event.reply("There is no Pokemon Center in " + channel.location.name + "!").withEphemeral(true);
        }

        long pokemonNumber = event.getOption("pokemon")
            .flatMap(option -> option.getValue())
            .map(value -> value.asLong())
            .orElse(0L);

        try {
            int index = (int) pokemonNumber - 1;
            if (index >= 0 && index < user.storage.size()) {
                // Find free slot
                int freeSlot = -1;
                for (int i = 0; i < user.squad.length; i++) {
                    if (user.squad[i] == null) {
                        freeSlot = i;
                        break;
                    }
                }

                if (freeSlot == -1) {
                    return event.reply("Your team is full! Deposit a Pokemon first.").withEphemeral(true);
                }

                Monster pokemon = user.storage.remove(index);
                user.squad[freeSlot] = pokemon;
                return event.reply("✅ **Took " + pokemon.getName() + " from Storage PC!**\n\n" + user.getMonstersString(channel));
            } else {
                return event.reply("Invalid Pokemon number!").withEphemeral(true);
            }
        } catch (Exception e) {
            return event.reply("Invalid Pokemon number!").withEphemeral(true);
        }
    }

    private Mono<Void> handleRelease(ChatInputInteractionEvent event, UserWrapper user, ChannelWrapper channel) {
        if (!user.initialized) {
            return event.reply("You need to start your journey first! Use `/start`").withEphemeral(true);
        }

        if (user.inBattle) {
            return event.reply("Finish the battle first!").withEphemeral(true);
        }

        if (!channel.location.hasCenter) {
            return event.reply("There is no Pokemon Center in " + channel.location.name + "!").withEphemeral(true);
        }

        String pokemonNumbers = event.getOption("pokemon")
            .flatMap(option -> option.getValue())
            .map(value -> value.asString())
            .orElse("");

        String confirmation = event.getOption("confirm")
            .flatMap(option -> option.getValue())
            .map(value -> value.asString())
            .orElse("");

        if (pokemonNumbers.isEmpty()) {
            return event.reply("Please specify Pokemon numbers to release!").withEphemeral(true);
        }

        ArrayList<Monster> pokemonToRelease = new ArrayList<>();
        String[] numbers = pokemonNumbers.split("\\s+");

        try {
            for (String numberStr : numbers) {
                int index = Integer.parseInt(numberStr) - 1;
                if (index >= 0 && index < user.storage.size()) {
                    Monster pokemon = user.storage.get(index);
                    if (!pokemonToRelease.contains(pokemon)) {
                        pokemonToRelease.add(pokemon);
                    }
                    // Skip duplicates silently - this is user-friendly behavior
                } else {
                    return event.reply("❌ Invalid Pokemon number: " + numberStr).withEphemeral(true);
                }
            }
            
            if (pokemonToRelease.isEmpty()) {
                return event.reply("❌ No valid Pokemon to release!").withEphemeral(true);
            }

            // Build Pokemon names for messages
            StringBuilder mons = new StringBuilder();
            StringBuilder releaseText = new StringBuilder();
            
            if (pokemonToRelease.size() == 1) {
                mons.append(pokemonToRelease.get(0).getName()).append(" is");
                releaseText.append(pokemonToRelease.get(0).getName()).append(" was");
            } else if (pokemonToRelease.size() == 2) {
                mons.append(pokemonToRelease.get(0).getName()).append(" and ")
                    .append(pokemonToRelease.get(1).getName()).append(" are");
                releaseText.append(pokemonToRelease.get(0).getName()).append(" and ")
                    .append(pokemonToRelease.get(1).getName()).append(" were");
            } else {
                for (int i = 0; i < pokemonToRelease.size(); i++) {
                    mons.append(pokemonToRelease.get(i).getName());
                    releaseText.append(pokemonToRelease.get(i).getName());
                    if (i < pokemonToRelease.size() - 1) {
                        mons.append(", ");
                        releaseText.append(", ");
                    }
                    if (i == pokemonToRelease.size() - 2) {
                        mons.append("and ");
                        releaseText.append("and ");
                    }
                }
                mons.append(" are");
                releaseText.append(" were");
            }

            // Check if user confirmed
            if ("CONFIRM".equals(confirmation)) {
                // Actually release the Pokemon
                for (Monster m : pokemonToRelease) {
                    user.storage.remove(m);
                }
                
                // Save the user data
                user.save();
                
                return event.reply("✅ " + releaseText + " released outside. Bye!\n\n" +
                    "Your Pokemon storage has been updated.");
            } else {
                // Show warning and ask for confirmation
                StringBuilder response = new StringBuilder();
                response.append("⚠️ **WARNING: PERMANENT ACTION!**\n\n");
                response.append("Once released, ").append(mons).append(" **gone forever!**\n\n");
                response.append("**Pokemon to be released:**\n");
                for (Monster m : pokemonToRelease) {
                    response.append("• ").append(m.getName()).append(" (Level ").append(m.level).append(")\n");
                }
                response.append("\n**To confirm:** Run the same command again with `confirm: CONFIRM - Release Pokemon`");
                
                return event.reply(response.toString()).withEphemeral(true);
            }
            
        } catch (Exception e) {
            return event.reply("Invalid Pokemon numbers!").withEphemeral(true);
        }
    }

    private Mono<Void> handlePage(ChatInputInteractionEvent event, UserWrapper user, ChannelWrapper channel) {
        if (!user.initialized) {
            return event.reply("You need to start your journey first! Use `/start`").withEphemeral(true);
        }

        if (user.inBattle) {
            return event.reply("Finish the battle first!").withEphemeral(true);
        }

        if (!channel.location.hasCenter) {
            return event.reply("There is no Pokemon Center in " + channel.location.name + "!").withEphemeral(true);
        }

        long pageNumber = event.getOption("number")
            .flatMap(option -> option.getValue())
            .map(value -> value.asLong())
            .orElse(0L);

        try {
            int page = (int) pageNumber - 1;
            
            if (user.storage.size() == 0) {
                return event.reply("❌ Your Pokemon storage is empty! No pages to display.").withEphemeral(true);
            }
            
            int maxPage = (user.storage.size() - 1) / UserWrapper.entries_per_page;

            if (page >= 0 && page <= maxPage) {
                user.page = page;
                return event.reply("📄 **Set page to " + pageNumber + "!**\n\n" + user.getMonstersString(channel));
            } else {
                return event.reply("❌ Maximum page is " + (maxPage + 1) + "!").withEphemeral(true);
            }
        } catch (Exception e) {
            return event.reply("Invalid page number!").withEphemeral(true);
        }
    }

    // Battle command implementations
    private Mono<Void> handleJoin(ChatInputInteractionEvent event, UserWrapper user, ChannelWrapper channel) {
        if (!user.initialized) {
            return event.reply("You need to start your journey first! Use `/start`").withEphemeral(true);
        }

        if (channel.currentBattle == null) {
            return event.reply("❌ No battle to join!").withEphemeral(true);
        }

        // Validate join action
        String error = BattleUtils.validateJoinBattle(channel.currentBattle, user);
        if (error != null) {
            return event.reply(error).withEphemeral(true);
        }

        String pokemonChoice = event.getOption("pokemon")
            .flatMap(option -> option.getValue())
            .map(value -> value.asString())
            .orElse(null);

        return event.reply("⚔️ **Joining the battle...**")
            .then(Mono.fromRunnable(() -> channel.currentBattle.join(user, pokemonChoice)));
    }

    private Mono<Void> handleFight(ChatInputInteractionEvent event, UserWrapper user, ChannelWrapper channel) {
        if (!user.initialized) {
            return event.reply("You need to start your journey first! Use `/start`").withEphemeral(true);
        }

        if (channel.currentBattle == null) {
            return event.reply("❌ No battle in progress!").withEphemeral(true);
        }

        // Validate fight action
        String error = BattleUtils.validateFightAction(channel.currentBattle, user);
        if (error != null) {
            return event.reply(error).withEphemeral(true);
        }

        String move = event.getOption("move")
            .flatMap(option -> option.getValue())
            .map(value -> value.asString())
            .orElse(null);

        if (move == null) {
            return event.reply("❌ Please specify a move! Use `/fight move:1` or `/fight move:Tackle`").withEphemeral(true);
        }

        // Validate move before responding
        Monster playerMon = (user == channel.currentBattle.player1) ? channel.currentBattle.p1Mon : channel.currentBattle.p2Mon;
        if (playerMon == null) {
            return event.reply("❌ You don't have a Pokemon in battle!").withEphemeral(true);
        }
        
        // Check if move exists and has PP
        int moveIndex = BattleUtils.findMoveIndex(playerMon, move);
        if (moveIndex == -1) {
            return event.reply("❌ Invalid move! Use move number (1-4) or exact move name.").withEphemeral(true);
        }
        
        if (playerMon.movePP[moveIndex] <= 0) {
            return event.reply("❌ " + playerMon.moves[moveIndex].name + " has no PP left!").withEphemeral(true);
        }
        
        // Respond immediately, then process battle action
        return event.reply("⚔️ **Using " + playerMon.moves[moveIndex].name + "...**")
            .then(Mono.fromRunnable(() -> channel.currentBattle.inputFight(user, move)));
    }

    private Mono<Void> handleSwitch(ChatInputInteractionEvent event, UserWrapper user, ChannelWrapper channel) {
        if (!user.initialized) {
            return event.reply("You need to start your journey first! Use `/start`").withEphemeral(true);
        }

        if (channel.currentBattle == null) {
            return event.reply("No battle in progress!").withEphemeral(true);
        }

        String pokemon = event.getOption("pokemon")
            .flatMap(option -> option.getValue())
            .map(value -> value.asString())
            .orElse(null);

        if (channel.currentBattle.player1 == null) {
            if (channel.currentBattle.prevP1 != user) {
                return event.reply("You must join the battle first!").withEphemeral(true);
            }
            return event.reply("🔄 **Joining battle with Pokemon...**")
                .then(Mono.fromRunnable(() -> channel.currentBattle.join(user, pokemon)));
        } else {
            return event.reply("🔄 **Switching Pokemon...**")
                .then(Mono.fromRunnable(() -> channel.currentBattle.inputSwitch(user, pokemon)));
        }
    }

    private Mono<Void> handleCatch(ChatInputInteractionEvent event, UserWrapper user, ChannelWrapper channel) {
        if (!user.initialized) {
            return event.reply("You need to start your journey first! Use `/start`").withEphemeral(true);
        }

        if (channel.currentBattle == null) {
            return event.reply("❌ No battle in progress!").withEphemeral(true);
        }

        // Validate catch action
        String error = BattleUtils.validateCatchAction(channel.currentBattle, user);
        if (error != null) {
            return event.reply(error).withEphemeral(true);
        }

        return event.reply("🎯 **Throwing Pokeball...**")
            .then(Mono.fromRunnable(() -> channel.currentBattle.inputCapture(user)));
    }

    private Mono<Void> handleRun(ChatInputInteractionEvent event, UserWrapper user, ChannelWrapper channel) {
        if (!user.initialized) {
            return event.reply("You need to start your journey first! Use `/start`").withEphemeral(true);
        }

        if (channel.currentBattle == null) {
            return event.reply("❌ No battle in progress!").withEphemeral(true);
        }

        // Validate run action
        String error = BattleUtils.validateRunAction(channel.currentBattle, user);
        if (error != null) {
            return event.reply(error).withEphemeral(true);
        }

        return event.reply("🏃 **Attempting to run...**")
            .then(Mono.fromRunnable(() -> channel.currentBattle.inputRun(user)));
    }

    private Mono<Void> handleNext(ChatInputInteractionEvent event, UserWrapper user, ChannelWrapper channel) {
        if (!user.initialized) {
            return event.reply("You need to start your journey first! Use `/start`").withEphemeral(true);
        }

        channel.advance();
        return event.reply("➡️ **Advanced to next message!**");
    }
}