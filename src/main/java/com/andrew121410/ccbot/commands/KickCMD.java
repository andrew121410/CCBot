package com.andrew121410.ccbot.commands;

import com.andrew121410.ccbot.CCBot;
import com.andrew121410.ccbot.commands.manager.CommandManager;
import com.andrew121410.ccbot.commands.manager.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class KickCMD implements ICommand {

    private CCBot ccBot;

    public KickCMD(CCBot ccBot) {
        this.ccBot = ccBot;
        this.ccBot.getCommandManager().register(this, "kick");
    }

    @Override
    public boolean onMessage(MessageReceivedEvent event, String[] args) {
        if (!CommandManager.hasPermission(event.getMember(), event.getTextChannel(), Permission.KICK_MEMBERS)) {
            return true;
        }

        if (args.length == 0) {
            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setAuthor("CCBot Kick Usage!")
                    .setColor(Color.RED)
                    .addField("Usage:", this.ccBot.getConfigManager().getMainConfig().getPrefix() + "kick <Member>", false);
            event.getTextChannel().sendMessage(embedBuilder.build()).queue(a -> a.delete().queueAfter(10, TimeUnit.SECONDS));
            return true;
        } else if (args.length == 1) {
            boolean memberMaybe = event.getMessage().getMentionedMembers().isEmpty();
            if (!memberMaybe) {
                event.getTextChannel().sendMessage("You didn't mention a user.").queue(a -> a.delete().queueAfter(10, TimeUnit.SECONDS));
                return true;
            }
            Member member = event.getMessage().getMentionedMembers().get(0);
            event.getGuild().kick(member).queue();
            event.getChannel().sendMessage(member.getAsMention() + " **has been kicked from the server!**").queue();
            return true;
        }
        return false;
    }
}
