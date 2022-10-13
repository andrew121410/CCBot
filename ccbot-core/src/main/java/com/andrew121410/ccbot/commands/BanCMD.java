package com.andrew121410.ccbot.commands;

import com.andrew121410.ccbot.CCBotCore;
import com.andrew121410.ccbot.commands.manager.ACommand;
import com.andrew121410.ccbot.commands.manager.AbstractCommand;
import com.andrew121410.ccbot.commands.manager.CommandManager;
import com.andrew121410.ccutils.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Mentions;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.concurrent.TimeUnit;

@ACommand(command = "ban", description = "Bans the player")
public class BanCMD extends AbstractCommand {

    private final CCBotCore ccBotCore;

    public BanCMD(CCBotCore ccBotCore) {
        super(ccBotCore);
        this.ccBotCore = ccBotCore;
    }

    @Override
    public boolean onMessage(MessageReceivedEvent event, String[] args) {
        TextChannel textChannel = event.getChannel().asTextChannel();

        if (!CommandManager.hasPermission(event.getMember(), textChannel, Permission.BAN_MEMBERS)) {
            return true;
        }

        if (args.length >= 1) {
            Mentions mentions = event.getMessage().getMentions();
            if (mentions.getMembers().isEmpty()) {
                textChannel.sendMessage("You need to mention a member!").queue(a -> a.delete().queueAfter(10, TimeUnit.SECONDS));
                return true;
            }

            Integer time = 0;
            if (args.length == 2) {
                time = Utils.asIntegerOrElse(args[1], 0);
                if (time > 7) {
                    textChannel.sendMessage("Message deletion days must not be higher then 7.").queue(a -> a.delete().queueAfter(10, TimeUnit.SECONDS));
                    return true;
                }
            }

            Member member = mentions.getMembers().get(0);
            try {
                event.getGuild().ban(member, time, TimeUnit.DAYS).queue();
            } catch (Exception e) {
                textChannel.sendMessage("I can't ban that member!").queue(a -> a.delete().queueAfter(10, TimeUnit.SECONDS));
                return true;
            }
            event.getChannel().sendMessage(member.getAsMention() + " **has been banned from the server!** \\uD83D\\uDD34").queue();
            return true;
        } else {
            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setAuthor("CCBot Ban Usage!")
                    .setColor(Color.RED)
                    .addField("1.", this.ccBotCore.getConfigManager().getMainConfig().getPrefix() + "ban <Member>", false)
                    .addField("2.", this.ccBotCore.getConfigManager().getMainConfig().getPrefix() + "ban <Member> <MessageDeletionDays>", false);
            textChannel.sendMessageEmbeds(embedBuilder.build()).queue(a -> a.delete().queueAfter(10, TimeUnit.SECONDS));
        }
        return true;
    }
}