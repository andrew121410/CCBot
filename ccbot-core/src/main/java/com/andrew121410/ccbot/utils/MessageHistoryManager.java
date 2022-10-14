package com.andrew121410.ccbot.utils;

import com.andrew121410.ccbot.CCBotCore;
import com.andrew121410.ccbot.objects.AMessage;
import com.andrew121410.ccbot.objects.CGuild;
import com.andrew121410.ccutils.storage.ISQL;
import com.andrew121410.ccutils.storage.SQLite;
import com.andrew121410.ccutils.storage.easy.EasySQL;
import com.andrew121410.ccutils.storage.easy.SQLDataStore;
import com.google.common.collect.Multimap;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageHistoryManager {

    private CCBotCore ccBotCore;

    private final ISQL isql;
    private final EasySQL easySQL;

    public MessageHistoryManager(CCBotCore ccBotCore) {
        this.ccBotCore = ccBotCore;

        this.isql = new SQLite(this.ccBotCore.getConfigManager().getConfigFolder(), "messageHistory");
        this.easySQL = new EasySQL(this.isql, "messageHistory");

        List<String> columns = new ArrayList<>();
        columns.add("guildId");
        columns.add("channelId");
        columns.add("messageId");
        columns.add("senderId");
        columns.add("message");

        this.easySQL.create(columns, false);
    }

    public void saveMessage(Guild guild, TextChannel textChannel, User user, Message message) {
        Map<String, String> map = new HashMap<>();
        map.put("guildId", guild.getId());
        map.put("channelId", textChannel.getId());
        map.put("messageId", message.getId());
        map.put("senderId", user.getId());
        map.put("message", message.getContentRaw());

        try {
            this.easySQL.save(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveMessage(Message message) {
        saveMessage(message.getGuild(), message.getChannel().asTextChannel(), message.getAuthor(), message);
    }

    public AMessage getMessage(Guild guild, TextChannel textChannel, String messageId) {
        Map<String, String> map = new HashMap<>();

        map.put("guildId", guild.getId());
        map.put("channelId", textChannel.getId());
        map.put("messageId", messageId);

        Multimap<String, SQLDataStore> bigMap = this.easySQL.get(map);

        SQLDataStore sqlDataStore = bigMap.values().stream().findFirst().orElse(null);
        if (sqlDataStore == null) return null;

        return new AMessage(sqlDataStore.get("message"), sqlDataStore.get("senderId"));
    }

    public void deleteMessage(String guildId, String channelId, String messageId) {
        Map<String, String> map = new HashMap<>();

        map.put("guildId", guildId);
        map.put("channelId", channelId);
        map.put("messageId", messageId);

        this.easySQL.delete(map);
    }

    public void deleteGuild(String guildId) {
        Map<String, String> map = new HashMap<>();
        map.put("guildId", guildId);
        this.easySQL.delete(map);
    }

    public boolean isRunning = false;

    public void cacheEverythingMissing() {
        if (this.isRunning) return;
        this.isRunning = true;

        System.out.println("Caching missing messages...");

        String lastOnline = this.ccBotCore.getConfigManager().getMainConfig().getLastOn();
        long lastOnlineLong = Long.parseLong(lastOnline);

        if (lastOnlineLong != 0) {
            for (Guild guild : this.ccBotCore.getJda().getGuilds()) {
                CGuild cGuild = this.ccBotCore.getSetListMap().getGuildMap().get(guild.getId());

                // If the guild doesn't have the log feature enabled then skip it.
                if (!cGuild.getSettings().getLogs()) continue;

                for (TextChannel textChannel : guild.getTextChannels()) {
                    textChannel.getIterableHistory().takeUntilAsync(message -> message.getTimeCreated().toInstant().toEpochMilli() <= lastOnlineLong).thenApply(messages -> {
                        int size = messages.size();
                        for (Message message : messages) {
                            if (size <= 30) {
                                System.out.println("Caching message: " + message.getId() + " Content: " + message.getContentRaw());
                            }
                            this.saveMessage(guild, textChannel, message.getAuthor(), message);
                        }
                        return null;
                    });
                }
            }
        } else {
            // Cache everything
            Runnable runnable = () -> {
                for (Guild guild : this.ccBotCore.getJda().getGuilds()) {
                    CGuild cGuild = this.ccBotCore.getSetListMap().getGuildMap().get(guild.getId());

                    // If the guild doesn't have the log feature enabled then skip it.
                    if (!cGuild.getSettings().getLogs()) continue;

                    cacheAllMessagesOfGuild(guild);
                }
                System.out.println("Done caching ALL messages!");
            };
            new Thread(runnable).start();
        }
        System.out.println("Done in cacheEverythingMissing!");
        this.isRunning = false;
    }

    private void cacheAllMessagesOfGuild(Guild guild) {
        for (TextChannel textChannel : guild.getTextChannels()) {
            for (Message message : textChannel.getIterableHistory()) {
                saveMessage(guild, textChannel, message.getAuthor(), message);
            }
        }
    }

    public void cacheAllMessagesOfGuildAsync(Guild guild) {
        Runnable runnable = () -> cacheAllMessagesOfGuild(guild);
        new Thread(runnable).start();
    }
}

