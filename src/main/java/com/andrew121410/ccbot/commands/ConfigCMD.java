package com.andrew121410.ccbot.commands;

import com.andrew121410.ccbot.CCBot;
import com.andrew121410.ccbot.commands.manager.CommandManager;
import com.andrew121410.ccbot.commands.manager.ICommand;
import com.andrew121410.ccbot.objects.server.CGuild;
import com.andrew121410.ccbot.objects.server.CReaction;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;

public class ConfigCMD implements ICommand {

    private CCBot ccBot;

    public ConfigCMD(CCBot ccBot) {
        this.ccBot = ccBot;
        this.ccBot.getCommandManager().register(this, "config");
    }

    @Override
    public boolean onMessage(MessageReceivedEvent event, String[] args) {
        if (!CommandManager.hasPermission(event.getMember(), event.getTextChannel(), Permission.MANAGE_PERMISSIONS)) {
            return true;
        }
        CGuild cGuild = this.ccBot.getConfigManager().getGuildConfigManager().getOrElseAdd(event.getGuild());

        if (args.length == 0) {
            EmbedBuilder embedBuilder = makeEmbed(cGuild);

            event.getTextChannel().sendMessage(embedBuilder.build()).queue(a -> cGuild.getCReactionMap().putIfAbsent(a.getTextChannel().getId() + a.getId(), new CReaction(a, Collections.singletonList(Permission.MANAGE_SERVER), Arrays.asList("\uD83D\uDC4B", "\uD83D\uDCF0"), (onCReaction, onEvent) -> {
                switch (onEvent.getReactionEmote().getEmoji()) {
                    case "\uD83D\uDC4B":
                        cGuild.getCGuildSettings().setWelcomeMessages(!cGuild.getCGuildSettings().getWelcomeMessages());
                        break;
                    case "\uD83D\uDCF0":
                        cGuild.getCGuildSettings().setLogs(!cGuild.getCGuildSettings().getLogs());
                        break;
                    default:
                        break;
                }
            }, ((cReaction, event1) -> makeEmbed(cGuild)), true)));
        }
        return false;
    }

    private EmbedBuilder makeEmbed(CGuild cGuild) {
        String configSec = "WelcomeMessages: \uD83D\uDC4B " + cGuild.getCGuildSettings().getWelcomeMessages()
                + "\r\n" + "Logs: \uD83D\uDCF0 " + cGuild.getCGuildSettings().getLogs();
        return new EmbedBuilder()
                .setAuthor("CCBot")
                .setTitle("Guild configuration!")
                .setDescription("Allows you to edit the configuration of the guild.")
                .addField("Settings:", configSec, false)
                .setColor(new Color(48, 2, 11));
    }
}
