package com.andrew121410.ccbot.commands;

import com.andrew121410.ccbot.CCBot;
import com.andrew121410.ccbot.commands.manager.CommandManager;
import com.andrew121410.ccbot.commands.manager.ICommand;
import com.andrew121410.ccbot.objects.server.CGuild;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

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
            EmbedBuilder embedBuilder = makeEmbed(cGuild, null, event.getTextChannel());

            event.getTextChannel().sendMessage(embedBuilder.build()).queue(a -> {
                a.addReaction("\\uD83D\\uDC4B").queue();
                a.addReaction("\\uD83D\\uDCF0").queue();

                cGuild.getReactionsMap().put(a.getId(), (c, d) -> {
                    switch (d.getReactionEmote().getEmoji()) {
                        case "\\uD83D\\uDC4B":
                            cGuild.getCGuildSettings().setWelcomeMessages(!cGuild.getCGuildSettings().getWelcomeMessages());
                            makeEmbed(cGuild, c, d.getTextChannel());
                            break;
                        case "\\uD83D\\uDCF0":
                            cGuild.getCGuildSettings().setLogs(!cGuild.getCGuildSettings().getLogs());
                            makeEmbed(cGuild, c, d.getTextChannel());
                            break;
                    }
                });
            });
            return true;
        }
        return false;
    }

    private EmbedBuilder makeEmbed(CGuild cGuild, String edit, TextChannel textChannel) {
        String configSec = "WelcomeMessages: \\uD83D\\uDC4B " + cGuild.getCGuildSettings().getWelcomeMessages()
                + "\r\n" + "Logs: \\uD83D\\uDCF0 " + cGuild.getCGuildSettings().getLogs();

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setAuthor("CCBot")
                .setTitle("Guild configuration!")
                .setDescription("Allows you to edit the configuration of the guild.")
                .addField("Settings:", configSec, false);

        if (edit != null) {
            textChannel.editMessageById(edit, embedBuilder.build()).queue(a -> {
                a.clearReactions().queue();
                a.addReaction("\\uD83D\\uDC4B").queue();
                a.addReaction("\\uD83D\\uDCF0").queue();
            });
        }
        return embedBuilder;
    }
}
