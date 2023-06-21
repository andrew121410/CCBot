package com.andrew121410.ccbot.guilds;

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

    @JsonProperty("WelcomeMessages")
    private Boolean welcomeMessages = false;

    @JsonProperty("Logs")
    private Boolean logs = false;

    public Boolean getWelcomeMessages() {
        return welcomeMessages;
    }

    public void setWelcomeMessages(Boolean welcomeMessages) {
        this.welcomeMessages = welcomeMessages;
    }

    public Boolean isLoggingEnabled() {
        return logs;
    }

    public void setLogs(Boolean logs) {
        this.logs = logs;
    }
}
