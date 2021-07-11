package com.andrew121410.ccbot.objects;

import com.andrew121410.ccbot.CCBotCore;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.dv8tion.jda.api.entities.Guild;

import java.util.HashMap;
import java.util.Map;

@ToString
@EqualsAndHashCode
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CGuild {

    @JsonIgnore
    private final CCBotCore ccBotCore = CCBotCore.getInstance();

    @JsonProperty("Guild-ID")
    private String guildId;

    @JsonIgnore
    private Map<String, CReaction> reactions = new HashMap<>();

    @JsonProperty("Tags")
    private Map<String, String> tags = new HashMap<>();

    @JsonProperty("Settings")
    private CGuildSettings settings = new CGuildSettings();

    public CGuild(Guild guild) {
        this.guildId = guild.getId();
    }

    public String getGuildId() {
        return guildId;
    }

    public Map<String, CReaction> getReactions() {
        return reactions;
    }

    public CGuildSettings getSettings() {
        return settings;
    }

    public Map<String, String> getTags() {
        return tags;
    }
}
