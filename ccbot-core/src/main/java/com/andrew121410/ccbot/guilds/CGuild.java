package com.andrew121410.ccbot.guilds;

import com.andrew121410.ccbot.CCBotCore;
import com.andrew121410.ccbot.msp.AMinecraftServer;
import com.andrew121410.ccbot.guilds.button.CButtonManager;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.dv8tion.jda.api.entities.Guild;

import java.util.*;

@ToString
@EqualsAndHashCode
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CGuild {

    @JsonIgnore
    private boolean initialized = false;

    @JsonIgnore
    private final CCBotCore ccBotCore = CCBotCore.getInstance();

    @JsonProperty("Guild-ID")
    private String guildId;

    @JsonIgnore
    private Map<String, CButtonManager> buttonManager = new HashMap<>();

    @JsonProperty("Tags")
    private Map<String, String> tags = new HashMap<>();

    @JsonProperty("Minecraft-Server-Pinger")
    private List<AMinecraftServer> aMinecraftServers = Collections.synchronizedList(new ArrayList<>());

    @JsonProperty("Settings")
    private CGuildSettings settings = new CGuildSettings();

    @JsonIgnore
    private MessageHistoryManager messageHistoryManager;

    public CGuild(Guild guild) {
        this.guildId = guild.getId();
    }

    public void init(Guild guild) {
        if (this.initialized) return;
        this.initialized = true;

        this.messageHistoryManager = new MessageHistoryManager(this.ccBotCore, guild.getId());
        this.messageHistoryManager.cacheEverythingMissing();

        // Make sure it's actually a sync list.
        this.aMinecraftServers = Collections.synchronizedList(this.aMinecraftServers);
    }

    @JsonIgnore
    public CButtonManager createButtonManager(String key, CButtonManager cButtonManager) {
        return this.buttonManager.putIfAbsent(key, cButtonManager);
    }

    public String getGuildId() {
        return guildId;
    }

    public Map<String, CButtonManager> getButtonManager() {
        return buttonManager;
    }

    public CGuildSettings getSettings() {
        return settings;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public MessageHistoryManager getMessageHistoryManager() {
        return messageHistoryManager;
    }

    public List<AMinecraftServer> getaMinecraftServers() {
        return aMinecraftServers;
    }
}
