package com.andrew121410.ccbot.config;

import com.andrew121410.ccbot.CCBotCore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import lombok.SneakyThrows;

import java.io.File;

public class ConfigManager {

    private CCBotCore ccBotCore;

    private CCBotJacksonConfig ccBotJacksonConfig;
    private GuildConfigManager guildConfigManager;


    public ConfigManager(CCBotCore ccBotCore) {
        this.ccBotCore = ccBotCore;
        this.guildConfigManager = new GuildConfigManager(this.ccBotCore, this);
    }

    public void loadAll() {
        loadConfig();
        this.guildConfigManager.loadAllGuilds();
    }

    public void saveAll(boolean silent) {
        this.saveConfig();
        this.guildConfigManager.saveAllGuilds(silent);
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

    @SneakyThrows
    public void saveConfig() {
        ObjectMapper objectMapper = createDefaultMapper();
        File configFile = new File(getConfigFolder(), "config.yml");
        objectMapper.writeValue(configFile, this.ccBotJacksonConfig);
    }

    public ObjectMapper createDefaultMapper() {
        YAMLFactory yamlFactory = new YAMLFactory();
        yamlFactory.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER);
        return new ObjectMapper(yamlFactory);
    }

    public File getConfigFolder() {
        File configFolder = new File(this.ccBotCore.getWorkingDirectory(), "config");
        if (!configFolder.exists()) {
            if (!configFolder.mkdirs()) {
                System.out.println("Failed to create config folder.");
            }
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
