package com.andrew121410.ccbot.msp;

import com.andrew121410.ccbot.CCBotCore;
import com.andrew121410.ccbot.objects.CGuild;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.awt.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MCServerPingerManager {

    private final CCBotCore ccBotCore;

    public static final ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();

    public MCServerPingerManager(CCBotCore ccBotCore) {
        this.ccBotCore = ccBotCore;

        setupTimer();
    }

    public void add(Guild guild, AMinecraftServer aMinecraftServer) {
        CGuild cGuild = this.ccBotCore.getGuildConfigManager().addOrGet(guild);
        cGuild.getaMinecraftServers().add(aMinecraftServer);
    }

    public void remove(Guild guild, String ip) {
        CGuild cGuild = this.ccBotCore.getGuildConfigManager().addOrGet(guild);
        cGuild.getaMinecraftServers().removeIf(aMinecraftServer -> aMinecraftServer.getIp().equalsIgnoreCase(ip));
    }

    public void removeAll(Guild guild) {
        CGuild cGuild = this.ccBotCore.getGuildConfigManager().addOrGet(guild);
        cGuild.getaMinecraftServers().clear();
    }

    @SneakyThrows
    public void setupTimer() {
        Runnable runnable = () -> {
            for (CGuild cGuild : this.ccBotCore.getGuildMap().values()) {
                Guild guild = this.ccBotCore.getJda().getGuildById(cGuild.getGuildId());
                List<AMinecraftServer> aMinecraftServers = cGuild.getaMinecraftServers();

                for (AMinecraftServer aMinecraftServer : aMinecraftServers) {
                    MinecraftServerStatus serverStatus = getServerStatus(aMinecraftServer);

                    if (serverStatus == null) {
                        System.out.println("Failed to get server status for " + aMinecraftServer.getIp() + ":" + aMinecraftServer.getPort());
                        return;
                    }

                    if (!serverStatus.getOnline()) {
                        aMinecraftServer.setAttempts(aMinecraftServer.getAttempts() + 1);
                        if (aMinecraftServer.getAttempts() >= 50) aMinecraftServer.setAttempts(5);

                        if (aMinecraftServer.getAttempts() >= 3 && !aMinecraftServer.isSentMessage()) {
                            TextChannel textChannel = guild.getTextChannelById(aMinecraftServer.getChannelId());
                            if (textChannel == null) return;

                            EmbedBuilder embedBuilder = new EmbedBuilder();
                            if (aMinecraftServer.getName() != null) {
                                embedBuilder.setTitle(aMinecraftServer.getName() + " is offline!");
                            } else {
                                embedBuilder.setTitle("Server is offline!");
                            }
                            embedBuilder.setDescription("The Minecraft Server `" + aMinecraftServer.getIp() + ":" + aMinecraftServer.getPort() + "` is offline!");
                            embedBuilder.setColor(Color.RED);

                            textChannel.sendMessageEmbeds(embedBuilder.build()).queue();

                            aMinecraftServer.setSentMessage(true);
                        }
                    } else {
                        aMinecraftServer.setAttempts(0);

                        if (aMinecraftServer.isSentMessage()) {
                            TextChannel textChannel = guild.getTextChannelById(aMinecraftServer.getChannelId());
                            if (textChannel == null) return;

                            EmbedBuilder embedBuilder = new EmbedBuilder();
                            if (aMinecraftServer.getName() != null) {
                                embedBuilder.setTitle(aMinecraftServer.getName() + " is online!");
                            } else {
                                embedBuilder.setTitle("Server is online!");
                            }
                            embedBuilder.setDescription("The Minecraft Server `" + aMinecraftServer.getIp() + ":" + aMinecraftServer.getPort() + "` is online!");
                            embedBuilder.setColor(Color.GREEN);

                            textChannel.sendMessageEmbeds(embedBuilder.build()).queue();

                            aMinecraftServer.setSentMessage(false);
                        }
                    }
                }
            }
        };

        SCHEDULED_EXECUTOR_SERVICE.scheduleAtFixedRate(runnable, 0, 30, TimeUnit.SECONDS);
    }

    private MinecraftServerStatus getServerStatus(AMinecraftServer aMinecraftServer) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.mcsrvstat.us/2/" + aMinecraftServer.getIp() + ":" + aMinecraftServer.getPort()))
                    .timeout(Duration.ofMinutes(1))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(response.body(), MinecraftServerStatus.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
