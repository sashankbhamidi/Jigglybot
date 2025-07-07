package jigglybot;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import reactor.core.publisher.Mono;

/**
 * Base class for dialogs that work with slash commands
 * Unlike the legacy Dialog system, this responds immediately and handles follow-up interactions
 */
public abstract class SlashDialog {
    
    protected ChannelWrapper channelWrapper;
    protected UserWrapper user;
    protected ChatInputInteractionEvent originalEvent;
    
    public SlashDialog(ChannelWrapper cw, UserWrapper uw, ChatInputInteractionEvent event) {
        this.channelWrapper = cw;
        this.user = uw;
        this.originalEvent = event;
    }
    
    /**
     * Execute the dialog - should return a Mono<Void> for immediate response
     */
    public abstract Mono<Void> execute();
    
    /**
     * Handle follow-up slash command input (if needed)
     */
    public abstract Mono<Void> handleFollowUp(String command, ChatInputInteractionEvent event);
    
    /**
     * Check if this dialog is still active
     */
    public abstract boolean isActive();
    
    /**
     * Complete the dialog and clean up
     */
    public void complete() {
        if (channelWrapper.currentSlashDialog == this) {
            channelWrapper.currentSlashDialog = null;
        }
    }
}