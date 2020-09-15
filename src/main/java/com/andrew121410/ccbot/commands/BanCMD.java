package com.andrew121410.ccbot.commands;

import com.andrew121410.ccbot.CCBot;
import com.andrew121410.ccbot.commands.manager.CommandManager;
import com.andrew121410.ccbot.commands.manager.ICommand;
import com.andrew121410.ccutils.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class BanCMD implements ICommand {

    private CCBot ccBot;

    public BanCMD(CCBot ccBot) {
        this.ccBot = ccBot;
        this.ccBot.getCommandManager().register(this, "ban");
    }

    @Override
    public boolean onMessage(MessageReceivedEvent event, String[] args) {
        if (!CommandManager.hasPermission(event.getMember(), event.getTextChannel(), Permission.BAN_MEMBERS)) {
            return true;
        }

        if (args.length == 0) {
            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setAuthor("CCBot Ban Usage!")
                    .setColor(Color.RED)
                    .addField("1.", this.ccBot.getConfigManager().getMainConfig().getPrefix() + "ban <Member>", false)
                    .addField("2.", this.ccBot.getConfigManager().getMainConfig().getPrefix() + "ban <Member> <MessageDeletionDays>", false);
            event.getTextChannel().sendMessage(embedBuilder.build()).queue(a -> a.delete().queueAfter(10, TimeUnit.SECONDS));
        } else {
            boolean memberMaybe = event.getMessage().getMentionedMembers().isEmpty();
            Integer integer = Utils.asIntegerOrElse(args[1], 0);
            if (!memberMaybe) {
                event.getTextChannel().sendMessage("You didn't mention a user.").queue(a -> a.delete().queueAfter(10, TimeUnit.SECONDS));
                return true;
            }
            if (integer > 7) {
                event.getTextChannel().sendMessage("Message deletion days must not be higher then 7.").queue(a -> a.delete().queueAfter(10, TimeUnit.SECONDS));
                return true;
            }
            Member member = event.getMessage().getMentionedMembers().get(0);
            event.getGuild().ban(member, integer).queue();
            event.getChannel().sendMessage(member.getAsMention() + " **has been banned from the server!** \\uD83D\\uDD34").queue();
        }
        return true;
    }
}