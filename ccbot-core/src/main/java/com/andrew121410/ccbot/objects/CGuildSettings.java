package com.andrew121410.ccbot.objects;

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
    private Boolean welcomeMessages = true;

    @JsonProperty("Logs")
    private Boolean logs = true;

    public Boolean getWelcomeMessages() {
        return welcomeMessages;
    }

    public void setWelcomeMessages(Boolean welcomeMessages) {
        this.welcomeMessages = welcomeMessages;
    }

    public Boolean getLogs() {
        return logs;
    }

    public void setLogs(Boolean logs) {
        this.logs = logs;
    }
}
