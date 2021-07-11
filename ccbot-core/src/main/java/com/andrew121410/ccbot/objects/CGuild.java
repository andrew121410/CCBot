package com.andrew121410.ccbot.objects;

import com.andrew121410.ccbot.CCBotCore;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CGuild {

    private final CCBotCore ccBotCore = CCBotCore.getInstance();

    @JsonProperty("Guild-ID")
    private String guildId;

    private Map<String, CReaction> cReactionMap = new HashMap<>();
    @JsonProperty("Tags")
    private Map<String, String> tags = new HashMap<>();
    @JsonProperty("Settings")
    private CGuildSettings cGuildSettings = new CGuildSettings();

    public CGuild(Guild guild) {
        this.guildId = guild.getId();
    }

    public String getGuildId() {
        return guildId;
    }

    public Map<String, CReaction> getReactions() {
        return cReactionMap;
    }

    public CGuildSettings getSettings() {
        return cGuildSettings;
    }

    public Map<String, String> getTags() {
        return tags;
    }
}
