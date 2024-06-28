package com.andrew121410.ccbot.msp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@ToString
@EqualsAndHashCode
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AMinecraftServer {

    @JsonProperty("name")
    private String name;

    @JsonProperty("Server-IP")
    private String ip;
    @JsonProperty("Server-Port")
    private int port;

    @JsonProperty("Channel-ID")
    private long channelId;
    @JsonIgnore
    private int attempts;
    @JsonProperty("Did-we-send-a-message")
    private boolean sentMessage = false;
    @JsonProperty("Time-of-offline")
    private long timeOfOffline = 0L;

    // User Settings
    @JsonProperty("max-attempts")
    private int maxAttempts = 3;
//    @JsonProperty("beMoreDescriptive")
//    private boolean beMoreDescriptive = false;

    public AMinecraftServer(long channelId, String name, String ip, int port) {
        this.channelId = channelId;
        this.name = name;
        this.ip = ip;
        this.port = port;
    }
}
