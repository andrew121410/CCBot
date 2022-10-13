package com.andrew121410.ccbot.commands.manager;

import com.andrew121410.ccbot.CCBotCore;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public abstract class AbstractCommand {

    private CCBotCore ccBotCore;

    public AbstractCommand(CCBotCore ccBotCore) {
        this.ccBotCore = ccBotCore;
    }

    public abstract boolean onMessage(MessageReceivedEvent event, String[] args);

    public boolean onSlashCommand(SlashCommandInteractionEvent slashCommandInteractionEvent) {
        return false;
    }
}
