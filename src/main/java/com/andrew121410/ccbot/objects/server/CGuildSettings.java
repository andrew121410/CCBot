package com.andrew121410.ccbot.objects.server;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CGuildSettings {

    @JsonProperty("WelcomeMessages")
    private Boolean welcomeMessages = true;

    @JsonProperty("Logs")
    private Boolean logs = true;
}
