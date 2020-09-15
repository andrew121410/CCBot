package com.andrew121410.ccbot.objects.server;

import com.andrew121410.ccbot.CCBot;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.dv8tion.jda.api.entities.Guild;

import java.util.HashMap;
import java.util.Map;

@ToString
@EqualsAndHashCode
@NoArgsConstructor
public class CGuild {

    private final CCBot ccBot = CCBot.getInstance();
    private String guildId;
    private Map<String, CReaction> cReactionMap;
    private CGuildSettings cGuildSettings;

    private CGuild(String guildId, Map<String, CReaction> cReactionMap, CGuildSettings cGuildSettings) {
        this.guildId = guildId;
        this.cReactionMap = cReactionMap;
        this.cGuildSettings = cGuildSettings;
    }

    public CGuild(Guild guild) {
        this(guild.getId(), new HashMap<>(), new CGuildSettings(guild.getId()));
    }

    public CGuild(CGuildSettings cGuildSettings) {
        this(cGuildSettings.getGuildId(), new HashMap<>(), cGuildSettings);
    }

    public String getGuildId() {
        return guildId;
    }

    public Map<String, CReaction> getCReactionMap() {
        return cReactionMap;
    }

    public CGuildSettings getCGuildSettings() {
        return cGuildSettings;
    }
}
