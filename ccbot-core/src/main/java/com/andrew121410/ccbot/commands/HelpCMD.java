package com.andrew121410.ccbot.commands;

import com.andrew121410.ccbot.CCBotCore;
import com.andrew121410.ccbot.commands.manager.ACommand;
import com.andrew121410.ccbot.commands.manager.AbstractCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;

@ACommand(command = "help", description = "shows all the commands for the bot")
public class HelpCMD extends AbstractCommand {

    private CCBotCore ccBotCore;

    public HelpCMD(CCBotCore ccBotCore) {
        super(ccBotCore);
        this.ccBotCore = ccBotCore;
    }

    @Override
    public boolean onMessage(MessageReceivedEvent event, String[] args) {
        helpStyle(event.getChannel().asTextChannel());
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

        textChannel.sendMessageEmbeds(embedBuilder.build()).queue();
    }
}
