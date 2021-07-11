package com.andrew121410.ccbot.config;

import com.andrew121410.ccbot.CCBotCore;
import com.andrew121410.ccbot.objects.CGuild;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.entities.Guild;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class GuildConfigManager {

    private Map<String, CGuild> guildMap;

    private CCBotCore ccBotCore;
    private ConfigManager configManager;

    public GuildConfigManager(CCBotCore ccBotCore, ConfigManager configManager) {
        this.ccBotCore = ccBotCore;
        this.configManager = configManager;
        this.guildMap = this.ccBotCore.getSetListMap().getGuildMap();
    }

    @SneakyThrows
    public void loadAllGuilds() {
        ObjectMapper objectMapper = configManager.createDefaultMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        File guildsFolder = getGuildFolder();
        List<File> ymlFiles = Arrays.stream(guildsFolder.listFiles()).filter(file -> file.getName().endsWith(".yml")).collect(Collectors.toList());
        for (File ymlFile : ymlFiles) {
            CGuild cGuild = objectMapper.readValue(ymlFile, CGuild.class);
            this.guildMap.putIfAbsent(cGuild.getGuildId(), cGuild);
        }
    }

    @SneakyThrows
    public void saveAllGuilds() {
        ObjectMapper objectMapper = configManager.createDefaultMapper();
        File guildsFolder = getGuildFolder();
        Instant start = Instant.now();
        for (Map.Entry<String, CGuild> entry : this.guildMap.entrySet()) {
            String k = entry.getKey();
            CGuild v = entry.getValue();
            File file = new File(guildsFolder, k + ".yml");
            objectMapper.writeValue(file, v);
        }
        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        System.out.println("Saved all guilds to file took: " + timeElapsed + " milliseconds");
    }

    public void add(Guild guild) {
        Objects.requireNonNull(guild, "Guild is null");
        if (this.guildMap.containsKey(guild.getId())) return;
        CGuild cGuild = new CGuild(guild);
        this.guildMap.putIfAbsent(guild.getId(), cGuild);
    }

    public void add(String key) {
        add(this.ccBotCore.getJda().getGuildById(key));
    }

    public void remove(String key) {
        CGuild cGuild = this.guildMap.get(key);
        if (cGuild == null) return;
        File ymlFile = new File(getGuildFolder(), cGuild.getGuildId() + ".yml");
        if (ymlFile.exists()) {
            ymlFile.delete();
        }
        this.guildMap.remove(cGuild.getGuildId());
    }

    public CGuild getOrElseAdd(Guild guild) {
        if (!this.guildMap.containsKey(guild.getId())) {
            add(guild);
        }
        return this.guildMap.get(guild.getId());
    }

    public CGuild getOrElseAdd(String key) {
        if (!this.guildMap.containsKey(key)) {
            add(key);
        }
        return this.guildMap.get(key);
    }

    public File getGuildFolder() {
        File guildFolder = new File(configManager.getConfigFolder(), "guilds");
        if (!guildFolder.exists()) {
            guildFolder.mkdir();
        }
        return guildFolder;
    }
}
