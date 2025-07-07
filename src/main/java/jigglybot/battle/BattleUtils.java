package jigglybot.battle;

import jigglybot.ChannelWrapper;
import jigglybot.UserWrapper;
import jigglybot.monster.Monster;

/**
 * Utility class for battle operations that work with slash commands
 * These methods don't block on Discord API calls
 */
public class BattleUtils {
    
    /**
     * Check if a user can join a battle (returns error message or null if OK)
     */
    public static String validateJoinBattle(Battle battle, UserWrapper user) {
        if (!user.initialized) {
            return "❌ You need to start your journey first! Use `/start`";
        }
        
        if (battle.p1Mon != null) {
            return "❌ You can only join when a Pokemon faints!";
        }
        
        if (user.inBattle) {
            return "❌ You're already in a battle!";
        }
        
        // Check if user has any Pokemon that can battle
        boolean canJoin = false;
        for (int i = 0; i < user.squad.length; i++) {
            if (user.squad[i] != null && user.squad[i].hp > 0) {
                canJoin = true;
                break;
            }
        }
        
        if (!canJoin) {
            return "❌ All your Pokemon have fainted! You need to heal at a Pokemon Center!";
        }
        
        return null; // No error - can join
    }
    
    /**
     * Check if a user can use a move in battle
     */
    public static String validateFightAction(Battle battle, UserWrapper user) {
        if (user != battle.player1 && user != battle.player2) {
            return "❌ You must join the battle first! Use `/join`";
        }
        
        if (battle.p1Mon == null || battle.p2Mon == null) {
            return "❌ Battle is not properly initialized!";
        }
        
        return null; // No error - can fight
    }
    
    /**
     * Check if a user can run from battle
     */
    public static String validateRunAction(Battle battle, UserWrapper user) {
        if (user != battle.player1 && user != battle.player2) {
            return "❌ You must join the battle first!";
        }
        
        return null; // No error - can run
    }
    
    /**
     * Check if a user can catch in battle
     */
    public static String validateCatchAction(Battle battle, UserWrapper user) {
        if (user != battle.player1 && user != battle.player2) {
            return "❌ You must join the battle first!";
        }
        
        if (!battle.joinable) {
            return "❌ You can't catch trainer Pokemon!";
        }
        
        return null; // No error - can catch
    }
    
    /**
     * Find a Pokemon by name or number in user's squad
     */
    public static Monster findPokemon(UserWrapper user, String identifier) {
        if (identifier == null) {
            return null;
        }
        
        try {
            // Try to parse as number
            int index = Integer.parseInt(identifier) - 1;
            if (index >= 0 && index < user.squad.length && user.squad[index] != null) {
                return user.squad[index];
            }
        } catch (NumberFormatException e) {
            // Try to find by name
            for (int i = 0; i < user.squad.length; i++) {
                if (user.squad[i] != null && user.squad[i].name.equalsIgnoreCase(identifier)) {
                    return user.squad[i];
                }
            }
        }
        
        return null;
    }
    
    /**
     * Find a move by name or number for a Pokemon
     */
    public static int findMoveIndex(Monster pokemon, String identifier) {
        if (identifier == null || pokemon == null) {
            return -1;
        }
        
        try {
            // Try to parse as number
            int index = Integer.parseInt(identifier) - 1;
            if (index >= 0 && index < pokemon.moves.length && pokemon.moves[index] != null) {
                return index;
            }
        } catch (NumberFormatException e) {
            // Try to find by name
            for (int i = 0; i < pokemon.moves.length; i++) {
                if (pokemon.moves[i] != null && pokemon.moves[i].name.equalsIgnoreCase(identifier)) {
                    return i;
                }
            }
        }
        
        return -1;
    }
}