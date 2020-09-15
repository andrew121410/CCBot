package com.andrew121410.ccbot.objects.server;

import com.andrew121410.ccbot.CCBot;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CGuild {

    private CCBot ccBot = CCBot.getInstance();

    public CGuild(Guild guild) {
        this.guildId = guild.getId();
        this.cGuildSettings = new CGuildSettings();
    }

    private Map<String, BiConsumer<String, MessageReactionAddEvent>> reactionsMap = new HashMap<>();

    @JsonProperty("Guild-ID")
    private String guildId = null;

    @JsonProperty("Guild-Settings")
    private CGuildSettings cGuildSettings = null;

    public Guild getGuild() {
        return this.ccBot.getJda().getGuildById(guildId);
    }
}
