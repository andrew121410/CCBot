package com.andrew121410.ccbot.commands;

import com.andrew121410.ccbot.CCBot;
import com.andrew121410.ccbot.commands.manager.CommandManager;
import com.andrew121410.ccbot.commands.manager.ICommand;
import com.andrew121410.ccutils.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PurgeCMD implements ICommand {

    private CCBot ccBot;

    public PurgeCMD(CCBot ccBot) {
        this.ccBot = ccBot;
        this.ccBot.getCommandManager().register(this, "purge");
    }

    @Override
    public boolean onMessage(MessageReceivedEvent event, String[] args) {
        if (!CommandManager.hasPermission(event.getMember(), event.getTextChannel(), Permission.MESSAGE_MANAGE)) {
            return true;
        }

        if (args.length == 0) {
            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setAuthor("CCBot Purge Usage!")
                    .setColor(Color.RED)
                    .addField("Usage:", this.ccBot.getConfigManager().getMainConfig().getPrefix() + "purge <Number>", false);
            event.getTextChannel().sendMessage(embedBuilder.build()).queue(a -> a.delete().queueAfter(10, TimeUnit.SECONDS));
            return true;
        } else if (args.length == 1) {
            Integer integer = Utils.asIntegerOrElse(args[0], null);
            if (integer == null) {
                event.getTextChannel().sendMessage("That's not a number.").queue(a -> a.delete().queueAfter(10, TimeUnit.SECONDS));
                return true;
            }
            if (integer <= 1) {
                event.getTextChannel().sendMessage("Number must be two or more.").queue(a -> a.delete().queueAfter(10, TimeUnit.SECONDS));
                return true;
            }
            if (integer >= 100) {
                event.getTextChannel().sendMessage("The max you can purge at once is 99.").queue(a -> a.delete().queueAfter(10, TimeUnit.SECONDS));
                return true;
            }
            purge(event.getTextChannel(), integer);
            event.getTextChannel().sendMessage("**Successfully purged " + integer + " messages!**").queue();
        }
        return false;
    }

    private void purge(TextChannel textChannel, int num) {
        num++;
        MessageHistory history = new MessageHistory(textChannel);
        List<Message> messagesList = history.retrievePast(num).complete();

        try {
            textChannel.deleteMessages(messagesList).queue();
        } catch (IllegalArgumentException ex) {
            textChannel.sendMessage(ex.getMessage()).queue();
        }
    }
}
