package com.andrew121410.ccbot.config;

import com.andrew121410.ccbot.CCBot;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import lombok.SneakyThrows;

import java.io.File;

public class ConfigManager {

    private CCBot ccBot;

    private CCBotJacksonConfig ccBotJacksonConfig;
    private GuildConfigManager guildConfigManager;

    public ConfigManager(CCBot ccBot) {
        this.ccBot = ccBot;
        this.guildConfigManager = new GuildConfigManager(this.ccBot, this);
    }

    public void loadAll() {
        loadConfig();
        this.guildConfigManager.loadAllGuilds();
    }

    public void saveAll() {
        this.guildConfigManager.saveAllGuilds();
    }

    @SneakyThrows
    public CCBotJacksonConfig loadConfig() {
        ObjectMapper objectMapper = createDefaultMapper();
        File configFile = new File(getConfigFolder(), "config.yml");
        if (configFile.exists()) {
            this.ccBotJacksonConfig = objectMapper.readValue(configFile, CCBotJacksonConfig.class);
            return this.ccBotJacksonConfig;
        } else {
            this.ccBotJacksonConfig = null;
            objectMapper.writeValue(configFile, new CCBotJacksonConfig());
            return null;
        }
    }

    public ObjectMapper createDefaultMapper() {
        YAMLFactory yamlFactory = new YAMLFactory();
        yamlFactory.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER);
        return new ObjectMapper(yamlFactory);
    }

    public File getConfigFolder() {
        File configFolder = new File("config");
        if (!configFolder.exists()) {
            configFolder.mkdir();
        }
        return configFolder;
    }

    public CCBotJacksonConfig getMainConfig() {
        return ccBotJacksonConfig;
    }

    public GuildConfigManager getGuildConfigManager() {
        return guildConfigManager;
    }
}
