package com.andrew121410.ccbot.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CCBotJacksonConfig {

    @JsonProperty("Discord-Token")
    private String token = "bot-token";

    @JsonProperty("Command-Prefix")
    private String prefix = "//";

    @JsonProperty("LastOn-do-not-touch")
    private String lastOn = "0";
}
