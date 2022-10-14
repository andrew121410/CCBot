package com.andrew121410.ccbot.utils;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CUtils {

    public static TextChannel findTextChannel(List<TextChannel> textChannels, List<String> list) {
        for (String string : list) {
            for (TextChannel textChannel : textChannels) {
                if (string.equalsIgnoreCase(textChannel.getName())) {
                    return textChannel;
                }
            }
        }
        return null;
    }

    public static TextChannel findTextChannel(List<TextChannel> textChannels, String... list) {
        return findTextChannel(textChannels, new ArrayList<>(Arrays.asList(list)));
    }

    public static TextChannel findLogChannel(Guild guild) {
        return findTextChannel(guild.getTextChannels(), getLogsStringArray());
    }

    public static List<String> getLogsList() {
        return Arrays.stream(getLogsStringArray()).toList();
    }

    public static String[] getLogsStringArray() {
        return new String[]{"logs", "log", "bot-logs", "bots-log", "bots-logs", "ccbot-logs", "ccbot-log", "discord-log", "discord-logs", "owner-log", "owners-log"};

    }
}
