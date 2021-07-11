package com.andrew121410.ccbot.commands;

import com.andrew121410.ccbot.CCBotCore;
import com.andrew121410.ccbot.commands.manager.CommandManager;
import com.andrew121410.ccbot.commands.manager.ICommand;
import com.andrew121410.ccbot.objects.CGuild;
import com.andrew121410.ccbot.objects.CReaction;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;

public class ConfigCMD implements ICommand {

    private CCBotCore ccBotCore;

    public ConfigCMD(CCBotCore ccBotCore) {
        this.ccBotCore = ccBotCore;
        this.ccBotCore.getCommandManager().register(this, "config");
    }

    @Override
    public boolean onMessage(MessageReceivedEvent event, String[] args) {
        if (!CommandManager.hasPermission(event.getMember(), event.getTextChannel(), Permission.MANAGE_PERMISSIONS)) {
            return true;
        }
        CGuild cGuild = this.ccBotCore.getConfigManager().getGuildConfigManager().getOrElseAdd(event.getGuild());

        if (args.length == 0) {
            EmbedBuilder embedBuilder = makeEmbed(cGuild);

            event.getTextChannel().sendMessage(embedBuilder.build()).queue(a -> cGuild.getReactions().putIfAbsent(a.getTextChannel().getId() + a.getId(), new CReaction(a, Collections.singletonList(Permission.MANAGE_SERVER), Arrays.asList("\uD83D\uDC4B", "\uD83D\uDCF0"), (onCReaction, onEvent) -> {
                switch (onEvent.getReactionEmote().getEmoji()) {
                    case "\uD83D\uDC4B":
                        cGuild.getSettings().setWelcomeMessages(!cGuild.getSettings().getWelcomeMessages());
                        break;
                    case "\uD83D\uDCF0":
                        cGuild.getSettings().setLogs(!cGuild.getSettings().getLogs());
                        break;
                    default:
                        break;
                }
            }, ((cReaction, event1) -> makeEmbed(cGuild)), true)));
        }
        return false;
    }

    private EmbedBuilder makeEmbed(CGuild cGuild) {
        String configSec = "WelcomeMessages: \uD83D\uDC4B " + cGuild.getSettings().getWelcomeMessages()
                + "\r\n" + "Logs: \uD83D\uDCF0 " + cGuild.getSettings().getLogs();
        return new EmbedBuilder()
                .setAuthor("CCBot")
                .setTitle("Guild configuration!")
                .setDescription("Allows you to edit the configuration of the guild.")
                .addField("Settings:", configSec, false)
                .setColor(new Color(48, 2, 11));
    }
}
