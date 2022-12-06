package com.andrew121410.ccbot.msp;

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

    @JsonProperty("Server-IP")
    private String ip;
    @JsonProperty("Server-Port")
    private int port;

    @JsonProperty("Channel-ID")
    private long channelId;
    private int attempts;
    @JsonProperty("Did-we-send-a-message")
    private boolean sentMessage = false;

    public AMinecraftServer(long channelId, String ip, int port) {
        this.channelId = channelId;
        this.ip = ip;
        this.port = port;
    }
}
