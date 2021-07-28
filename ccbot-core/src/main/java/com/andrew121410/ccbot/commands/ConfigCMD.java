package com.andrew121410.ccbot.commands;

import com.andrew121410.ccbot.CCBotCore;
import com.andrew121410.ccbot.commands.manager.ACommand;
import com.andrew121410.ccbot.commands.manager.AbstractCommand;
import com.andrew121410.ccbot.commands.manager.CommandManager;
import com.andrew121410.ccbot.objects.CGuild;
import com.andrew121410.ccbot.objects.button.CButton;
import com.andrew121410.ccbot.objects.button.CButtonManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.Button;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;

@ACommand(command = "config", description = "Allows you to edit the configuration for the bot!")
public class ConfigCMD extends AbstractCommand {

    private CCBotCore ccBotCore;

    public ConfigCMD(CCBotCore ccBotCore) {
        super(ccBotCore);
        this.ccBotCore = ccBotCore;
    }

    @Override
    public boolean onMessage(MessageReceivedEvent event, String[] args) {
        if (!CommandManager.hasPermission(event.getMember(), event.getTextChannel(), Permission.MANAGE_PERMISSIONS)) {
            return true;
        }
        CGuild cGuild = this.ccBotCore.getConfigManager().getGuildConfigManager().getOrElseAdd(event.getGuild());

        if (args.length == 0) {
            EmbedBuilder embedBuilder = makeEmbed(cGuild);

            event.getTextChannel().sendMessageEmbeds(embedBuilder.build()).queue(message ->
                    cGuild.createButtonManager(
                            message.getTextChannel().getId() + message.getId(), //Key
                            new CButtonManager(message, Arrays.asList(
                                    new CButton(Collections.singletonList(Permission.MANAGE_SERVER), Button.primary("welcome", "WelcomeMessages")),
                                    new CButton(Collections.singletonList(Permission.MANAGE_SERVER), Button.primary("log", "Logs"))),
                                    (cButtonManager, buttonClickEvent) -> {
                                        switch (buttonClickEvent.getComponentId()) {
                                            case "welcome" -> cGuild.getSettings().setWelcomeMessages(!cGuild.getSettings().getWelcomeMessages());
                                            case "log" -> cGuild.getSettings().setLogs(!cGuild.getSettings().getLogs());
                                        }
                                        if (buttonClickEvent.getMessage() != null)
//                                            https://github.com/DV8FromTheWorld/JDA/wiki/Interactions#buttons
                                            buttonClickEvent.deferEdit().setEmbeds(makeEmbed(cGuild).build()).queue();
                                    }, true)));
        }
        return true;
    }

    private EmbedBuilder makeEmbed(CGuild cGuild) {
        String configSec = "WelcomeMessages: \uD83D\uDC4B " + cGuild.getSettings().getWelcomeMessages()
                + "\r\n" + "Logs: \uD83D\uDCF0 " + cGuild.getSettings().getLogs();
        return new EmbedBuilder()
                .setAuthor("CCBot")
                .setTitle("Guild configuration!")
                .setDescription("Allows you to edit the configuration of the guild.")
                .addField("Settings:", configSec, false)
                .setColor(new Color(48, 2, 11));
    }
}
