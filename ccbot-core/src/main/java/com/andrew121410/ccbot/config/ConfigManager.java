package com.andrew121410.ccbot.config;

import com.andrew121410.ccbot.CCBotCore;
import lombok.SneakyThrows;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLFactory;
import tools.jackson.dataformat.yaml.YAMLFactoryBuilder;
import tools.jackson.dataformat.yaml.YAMLWriteFeature;

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
        YAMLFactoryBuilder builder = YAMLFactory.builder();
        builder.disable(YAMLWriteFeature.WRITE_DOC_START_MARKER);
        // So with Jackson 3.0 FAIL_ON_UNKNOWN_PROPERTIES is disabled by default.
        // From the docs: "Feature is disabled by default as of Jackson 3.0 (in 2.x it was enabled)."
        YAMLFactory yamlFactory = builder.build();
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
