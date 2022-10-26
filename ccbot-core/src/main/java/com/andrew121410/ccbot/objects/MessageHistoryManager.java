package com.andrew121410.ccbot.objects;

import com.andrew121410.ccbot.CCBotCore;
import com.andrew121410.ccbot.objects.AMessage;
import com.andrew121410.ccutils.storage.ISQL;
import com.andrew121410.ccutils.storage.SQLite;
import com.andrew121410.ccutils.storage.easy.EasySQL;
import com.andrew121410.ccutils.storage.easy.SQLDataStore;
import com.google.common.collect.Multimap;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageHistoryManager {

    private boolean isFirstTime = true;

    private final CCBotCore ccBotCore;
    private final String guildId;

    private final ISQL isql;
    private final EasySQL easySQL;

    private boolean interrupt = false;
    private boolean isRunning = false;

    public MessageHistoryManager(CCBotCore ccBotCore, String guildId) {
        this.ccBotCore = ccBotCore;
        this.guildId = guildId;

        File db = new File(this.ccBotCore.getConfigManager().getGuildConfigManager().getGuildFolder(), "mh-" + guildId + ".db");
        if (db.exists()) isFirstTime = false;

        this.isql = new SQLite(this.ccBotCore.getConfigManager().getGuildConfigManager().getGuildFolder(), "mh-" + guildId);
        this.easySQL = new EasySQL(this.isql, "messageHistory");

        List<String> columns = new ArrayList<>();
        columns.add("channelId");
        columns.add("messageId");
        columns.add("senderId");
        columns.add("message");

        this.easySQL.create(columns, false);
    }

    public void saveMessage(TextChannel textChannel, User user, Message message) {
        if (interrupt) return;

        Map<String, String> map = new HashMap<>();
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
        saveMessage(message.getChannel().asTextChannel(), message.getAuthor(), message);
    }

    public AMessage getMessage(TextChannel textChannel, String messageId) {
        if (interrupt) return null;

        Map<String, String> map = new HashMap<>();

        map.put("channelId", textChannel.getId());
        map.put("messageId", messageId);

        Multimap<String, SQLDataStore> bigMap = this.easySQL.get(map);

        SQLDataStore sqlDataStore = bigMap.values().stream().findFirst().orElse(null);
        if (sqlDataStore == null) return null;

        return new AMessage(sqlDataStore.get("message"), sqlDataStore.get("senderId"));
    }

    public void deleteMessage(String channelId, String messageId) {
        if (interrupt) return;

        Map<String, String> map = new HashMap<>();

        map.put("channelId", channelId);
        map.put("messageId", messageId);

        this.easySQL.delete(map);
    }

    public void delete() {
        this.interrupt = true;
        this.isql.disconnect();
        File file = new File(this.ccBotCore.getConfigManager().getGuildConfigManager().getGuildFolder(), "mh-" + guildId);
        if (file.delete()) System.out.println("Deleted " + file.getName());
    }

    public void cacheEverythingMissing() {
        if (this.isRunning) return;
        this.isRunning = true;

        System.out.println("Caching missing messages for " + guildId);

        String lastOnline = this.ccBotCore.getConfigManager().getMainConfig().getLastOn();
        long lastOnlineLong = Long.parseLong(lastOnline);

        if (!this.isFirstTime) {
            Guild guild = this.ccBotCore.getJda().getGuildById(guildId);
            if (guild == null) return;

            for (TextChannel textChannel : guild.getTextChannels()) {
                if (!guild.getSelfMember().getPermissions(textChannel).contains(Permission.VIEW_CHANNEL)) continue;

                textChannel.getIterableHistory().takeUntilAsync(message -> message.getTimeCreated().toInstant().toEpochMilli() <= lastOnlineLong).thenApply(messages -> {
                    int size = messages.size();
                    for (Message message : messages) {
                        if (size <= 30) {
                            System.out.println("Caching message: " + message.getId() + " Content: " + message.getContentRaw());
                        }
                        this.saveMessage(textChannel, message.getAuthor(), message);
                    }
                    return null;
                }).thenApply(aVoid -> {
                    this.isRunning = false;
                    return null;
                });
            }
        } else {
            // Cache everything
            Runnable runnable = () -> {
                Guild guild = this.ccBotCore.getJda().getGuildById(guildId);
                if (guild == null) return;

                cacheAllMessagesOfGuild(guild);

                System.out.println("Done caching missing messages for " + guildId);
                this.isRunning = false;
            };
            new Thread(runnable).start();
        }
    }

    private void cacheAllMessagesOfGuild(Guild guild) {
        for (TextChannel textChannel : guild.getTextChannels()) {
            if (!guild.getSelfMember().getPermissions(textChannel).contains(Permission.VIEW_CHANNEL)) continue;
            for (Message message : textChannel.getIterableHistory()) {
                saveMessage(textChannel, message.getAuthor(), message);
            }
        }
    }

    public void cacheAllMessagesOfGuildAsync(Guild guild) {
        Runnable runnable = () -> cacheAllMessagesOfGuild(guild);
        new Thread(runnable).start();
    }
}
