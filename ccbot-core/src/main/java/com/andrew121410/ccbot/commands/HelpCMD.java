package com.andrew121410.ccbot.commands;

import com.andrew121410.ccbot.CCBotCore;
import com.andrew121410.ccbot.commands.manager.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;

public class HelpCMD implements ICommand {

    private CCBotCore ccBotCore;

    public HelpCMD(CCBotCore ccBotCore) {
        this.ccBotCore = ccBotCore;
        this.ccBotCore.getCommandManager().register(this, "help");
    }

    @Override
    public boolean onMessage(MessageReceivedEvent event, String[] args) {
        helpStyle(event.getTextChannel());
        return true;
    }

    private void helpStyle(TextChannel textChannel) {
        String prefix = this.ccBotCore.getConfigManager().getMainConfig().getPrefix();

        String basicHelp = prefix + "help"
                + "\r\n" + " " + prefix + "tag";

        String adminHelp = prefix + "ban"
                + "\r\n" + " " + prefix + "kick"
                + "\r\n" + " " + prefix + "purge"
                + "\r\n" + " " + prefix + "config"
                + "\r\n" + " " + prefix + "tags";

//        String addonhelp = API.PREFIX + "ticket"
//                + "\r\n" + " " + API.PREFIX + "suggest";

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setAuthor("CCBot Help")
                .setColor(Color.BLUE)
                .setDescription("This is the list of commands.")
                .addField("Basic Commands!", basicHelp, false)
                .addField("Admin Commands!", adminHelp, false)
//                .addField("Addon Commands!", addonhelp, false)
                .setFooter("CCBot | Version: " + CCBotCore.VERSION + " | Developed by Andrew121410#2035");

        textChannel.sendMessage(embedBuilder.build()).queue();
    }
}
