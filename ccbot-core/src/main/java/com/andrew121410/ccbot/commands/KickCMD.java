package com.andrew121410.ccbot.commands;

import com.andrew121410.ccbot.CCBotCore;
import com.andrew121410.ccbot.commands.manager.ACommand;
import com.andrew121410.ccbot.commands.manager.AbstractCommand;
import com.andrew121410.ccbot.commands.manager.CommandManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Mentions;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.concurrent.TimeUnit;

@ACommand(command = "kick", description = "Kicks a player from the guild")
public class KickCMD extends AbstractCommand {

    private CCBotCore ccBotCore;

    public KickCMD(CCBotCore ccBotCore) {
        super(ccBotCore);
        this.ccBotCore = ccBotCore;
    }

    @Override
    public boolean onMessage(MessageReceivedEvent event, String[] args) {
        TextChannel textChannel = event.getChannel().asTextChannel();

        if (!CommandManager.hasPermission(event.getMember(), textChannel, Permission.KICK_MEMBERS)) {
            return true;
        }

        if (args.length == 0) {
            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setAuthor("CCBot Kick Usage!")
                    .setColor(Color.RED)
                    .addField("Usage:", this.ccBotCore.getConfigManager().getMainConfig().getPrefix() + "kick <Member>", false);
            textChannel.sendMessageEmbeds(embedBuilder.build()).queue(a -> a.delete().queueAfter(10, TimeUnit.SECONDS));
            return true;
        } else if (args.length == 1) {
            Mentions mentions = event.getMessage().getMentions();
            if (mentions.getMembers().isEmpty()) {
                textChannel.sendMessage("You didn't mention a user.").queue(a -> a.delete().queueAfter(10, TimeUnit.SECONDS));
                return true;
            }
            Member member = mentions.getMembers().get(0);
            event.getGuild().kick(member).queue();
            event.getChannel().sendMessage(member.getAsMention() + " **has been kicked from the server!**").queue();
            return true;
        }
        return false;
    }
}
