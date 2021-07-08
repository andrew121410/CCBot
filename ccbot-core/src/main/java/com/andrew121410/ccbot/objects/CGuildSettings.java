package com.andrew121410.ccbot.objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CGuildSettings {

    public CGuildSettings(String guildId) {
        this.guildId = guildId;
    }

    @JsonProperty("GuildID")
    private String guildId;

    @JsonProperty("WelcomeMessages")
    private Boolean welcomeMessages = true;

    @JsonProperty("Logs")
    private Boolean logs = true;

    @JsonIgnore
    public String getGuildId() {
        return guildId;
    }

    @JsonIgnore
    public Boolean getWelcomeMessages() {
        return welcomeMessages;
    }

    @JsonIgnore
    public Boolean getLogs() {
        return logs;
    }

    @JsonIgnore
    public void setGuildId(String guildId) {
        this.guildId = guildId;
    }

    @JsonIgnore
    public void setWelcomeMessages(Boolean welcomeMessages) {
        this.welcomeMessages = welcomeMessages;
    }

    @JsonIgnore
    public void setLogs(Boolean logs) {
        this.logs = logs;
    }
}
