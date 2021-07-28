package com.andrew121410.ccbot.commands;

import com.andrew121410.ccbot.CCBotCore;
import com.andrew121410.ccbot.commands.manager.ACommand;
import com.andrew121410.ccbot.commands.manager.AbstractCommand;
import com.andrew121410.ccbot.commands.manager.CommandManager;
import com.andrew121410.ccutils.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@ACommand(command = "purge", description = "Purges messages")
public class PurgeCMD extends AbstractCommand {

    private CCBotCore ccBotCore;

    public PurgeCMD(CCBotCore ccBotCore) {
        super(ccBotCore);
        this.ccBotCore = ccBotCore;
    }

    @Override
    public boolean onMessage(MessageReceivedEvent event, String[] args) {
        if (!CommandManager.hasPermission(event.getMember(), event.getTextChannel(), Permission.MESSAGE_MANAGE)) {
            return true;
        }

        String prefix = this.ccBotCore.getConfigManager().getMainConfig().getPrefix();

        if (args.length == 0) {
            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setAuthor("CCBot Purge Usage!")
                    .setColor(Color.RED)
                    .addField("Usage:", prefix + "purge <Number>" + "\r\n " + prefix + "purge words badword", false);
            event.getTextChannel().sendMessageEmbeds(embedBuilder.build()).queue(a -> a.delete().queueAfter(10, TimeUnit.SECONDS));
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
            return true;
        } else if (args.length >= 2 && args[0].contains("word")) {
            String[] wordArray = Arrays.copyOfRange(args, 1, args.length);
            purgeWords(event.getTextChannel(), Arrays.asList(wordArray));
            event.getTextChannel().sendMessage("**We are now deleting messages with the word/s " + Arrays.toString(wordArray) + " in them!**").queue();
            return true;
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

    private void purgeWords(TextChannel textChannel, List<String> strings) {
        AtomicInteger atomicInteger = new AtomicInteger(0);
        textChannel.getIterableHistory().forEachAsync(message -> {
            for (String string : strings)
                if (message.getContentRaw().contains(string)) {
                    atomicInteger.getAndIncrement();
                    message.delete().queue();
                }
            return true;
        }).thenRunAsync(() -> textChannel.sendMessage("Completed! The count was: " + atomicInteger.get()).queue());
    }
}
