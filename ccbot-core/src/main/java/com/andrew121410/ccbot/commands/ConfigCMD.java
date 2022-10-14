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
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;

@ACommand(command = "config", description = "Allows you to edit the configuration for the bot!")
public class ConfigCMD extends AbstractCommand {

    private CCBotCore ccBotCore;

    //    https://github.com/DV8FromTheWorld/JDA/wiki/Interactions#buttons
    public ConfigCMD(CCBotCore ccBotCore) {
        super(ccBotCore);
        this.ccBotCore = ccBotCore;
    }

    @Override
    public boolean onMessage(MessageReceivedEvent event, String[] args) {
        TextChannel textChannel = event.getGuildChannel().asTextChannel();

        if (!CommandManager.hasPermission(event.getMember(), textChannel, Permission.MANAGE_PERMISSIONS)) {
            return true;
        }
        CGuild cGuild = this.ccBotCore.getConfigManager().getGuildConfigManager().addOrGet(event.getGuild());

        if (args.length == 0) {
            EmbedBuilder embedBuilder = makeEmbed(cGuild);

            textChannel.sendMessageEmbeds(embedBuilder.build()).queue(message ->
                    cGuild.createButtonManager(
                            message.getChannel().asTextChannel().getId() + message.getId(),
                            new CButtonManager(message, Arrays.asList(
                                    new CButton(Collections.singletonList(Permission.MANAGE_SERVER), Button.primary("welcome", "WelcomeMessages - \uD83D\uDC4B")),
                                    new CButton(Collections.singletonList(Permission.MANAGE_SERVER), Button.primary("log", "Logs - \uD83D\uDCF0"))),
                                    (cButtonManager, buttonClickEvent) -> {
                                        switch (buttonClickEvent.getComponentId()) {
                                            case "welcome" ->
                                                    cGuild.getSettings().setWelcomeMessages(!cGuild.getSettings().getWelcomeMessages());
                                            case "log" -> cGuild.getSettings().setLogs(!cGuild.getSettings().isLoggingEnabled());
                                        }
                                        if (buttonClickEvent.getMessage() != null)
                                            buttonClickEvent.deferEdit().setEmbeds(makeEmbed(cGuild).build()).queue();
                                    }, true)));
        }
        return true;
    }

    private EmbedBuilder makeEmbed(CGuild cGuild) {
        String configSec = "WelcomeMessages: \uD83D\uDC4B " + cGuild.getSettings().getWelcomeMessages()
                + "\r\n" + "Logs: \uD83D\uDCF0 " + cGuild.getSettings().isLoggingEnabled();
        return new EmbedBuilder()
                .setAuthor("CCBot")
                .setTitle("Guild configuration!")
                .setDescription("Allows you to edit the configuration of the guild.")
                .addField("Settings:", configSec, false)
                .setColor(new Color(48, 2, 11));
    }
}
