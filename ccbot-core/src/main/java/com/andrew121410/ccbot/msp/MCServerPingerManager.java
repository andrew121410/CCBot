package com.andrew121410.ccbot.msp;

import br.com.azalim.mcserverping.MCPing;
import br.com.azalim.mcserverping.MCPingOptions;
import br.com.azalim.mcserverping.MCPingResponse;
import com.andrew121410.ccbot.CCBotCore;
import com.andrew121410.ccbot.objects.CGuild;
import com.andrew121410.ccutils.utils.TimeUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.FileUpload;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class MCServerPingerManager {

    private final CCBotCore ccBotCore;
    @Getter
    private AtomicLong lastRan = new AtomicLong(0L);

    public static final ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();

    public MCServerPingerManager(CCBotCore ccBotCore) {
        this.ccBotCore = ccBotCore;

        // Delete all the MC Server icons.
        deleteAllIcons();

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
                    MinecraftServerStatus serverStatus = !aMinecraftServer.isUseStatusWebsiteApi() ? getServerStatus(aMinecraftServer) : getServerStatusFromOnline(aMinecraftServer);

                    if (serverStatus == null) {
                        System.out.println("Failed to get server status for " + aMinecraftServer.getIp() + ":" + aMinecraftServer.getPort() + " useStatusWebsiteApi: " + aMinecraftServer.isUseStatusWebsiteApi());
                        return;
                    }

                    if (!serverStatus.getOnline()) { // If the server is offline.
                        aMinecraftServer.setAttempts(aMinecraftServer.getAttempts() + 1);
                        if (aMinecraftServer.getAttempts() >= 100) aMinecraftServer.setAttempts(5);

                        if (aMinecraftServer.getAttempts() >= aMinecraftServer.getMaxAttempts() && !aMinecraftServer.isSentMessage()) {
                            TextChannel textChannel = guild.getTextChannelById(aMinecraftServer.getChannelId());
                            if (textChannel == null) return;

                            // Set the time of offline
                            aMinecraftServer.setTimeOfOffline(System.currentTimeMillis());

                            EmbedBuilder embedBuilder = new EmbedBuilder();
                            embedBuilder.setColor(Color.RED);
                            embedBuilder.setTitle(aMinecraftServer.getName() + " is offline!");
                            embedBuilder.setDescription("The Minecraft Server `" + aMinecraftServer.getIp() + ":" + aMinecraftServer.getPort() + "` is offline!");

                            // Send the message
                            if (doesIconExist(aMinecraftServer)) { // Send the message with the icon.
                                embedBuilder.setThumbnail("attachment://server.png");
                                textChannel.sendFiles(FileUpload.fromData(getIconFile(aMinecraftServer), "server.png")).setEmbeds(embedBuilder.build()).queue();
                            } else { // Send the message without the icon.
                                textChannel.sendMessageEmbeds(embedBuilder.build()).queue();
                            }

                            aMinecraftServer.setSentMessage(true);
                        }
                    } else { // If the server is online.
                        aMinecraftServer.setAttempts(0);

                        if (aMinecraftServer.isSentMessage()) {
                            TextChannel textChannel = guild.getTextChannelById(aMinecraftServer.getChannelId());
                            if (textChannel == null) return;

                            EmbedBuilder embedBuilder = new EmbedBuilder();
                            embedBuilder.setColor(Color.GREEN);
                            embedBuilder.setTitle(aMinecraftServer.getName() + " is online!");

                            if (aMinecraftServer.getTimeOfOffline() != 0L) {
                                embedBuilder.setDescription("The Minecraft Server `" + aMinecraftServer.getIp() + ":" + aMinecraftServer.getPort() + "` is online!"
                                        + "\n\rThe server was offline for " + TimeUtils.makeIntoEnglishWords(aMinecraftServer.getTimeOfOffline(), System.currentTimeMillis(), true, false));
                            } else {
                                embedBuilder.setDescription("The Minecraft Server `" + aMinecraftServer.getIp() + ":" + aMinecraftServer.getPort() + "` is online!");
                            }

                            // Send the message
                            if (doesIconExist(aMinecraftServer)) { // Send the message with the icon.
                                embedBuilder.setThumbnail("attachment://server.png");
                                textChannel.sendFiles(FileUpload.fromData(getIconFile(aMinecraftServer), "server.png")).setEmbeds(embedBuilder.build()).queue();
                            } else { // Send the message without the icon.
                                textChannel.sendMessageEmbeds(embedBuilder.build()).queue();
                            }

                            aMinecraftServer.setSentMessage(false);
                        }
                    }
                }
            }
            lastRan.set(System.currentTimeMillis());
        };

        SCHEDULED_EXECUTOR_SERVICE.scheduleAtFixedRate(runnable, 0, 1, TimeUnit.MINUTES);
    }

    private MinecraftServerStatus getServerStatus(AMinecraftServer aMinecraftServer) {
        MCPingOptions options = MCPingOptions.builder()
                .hostname(aMinecraftServer.getIp())
                .port(aMinecraftServer.getPort())
                .build();

        try {
            MCPingResponse response = MCPing.getPing(options);

            // Save the icon
            saveIcon(aMinecraftServer, response.getFavicon());

            return new MinecraftServerStatus(true, null); // Icon is null, because we already saved it.
        } catch (Exception e) {
            return new MinecraftServerStatus(false, null);
        }
    }

    private MinecraftServerStatus getServerStatusFromOnline(AMinecraftServer aMinecraftServer) {
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

            MinecraftServerStatus minecraftServerStatus = objectMapper.readValue(response.body(), MinecraftServerStatus.class);

            // Save the icon
            saveIcon(aMinecraftServer, minecraftServerStatus.getIcon());

            return minecraftServerStatus;
        } catch (Exception e) {
            return null;
        }
    }

    private boolean doesIconExist(AMinecraftServer aMinecraftServer) {
        return getIconFile(aMinecraftServer) != null;
    }

    private void saveIcon(AMinecraftServer aMinecraftServer, String base64) {
        // Don't save the icon if it's already saved
        if (doesIconExist(aMinecraftServer)) return;

        // Don't save the icon if it's null lol
        if (base64 == null) return;

        // Make directory if it doesn't exist
        File directory = new File(this.ccBotCore.getWorkingDirectory(), "mc-server-icons");
        if (!directory.exists()) {
            directory.mkdir();
        }

        try {
            String partSeparator = ",";
            if (base64.contains(partSeparator)) base64 = base64.split(partSeparator)[1];

            byte[] bytes = Base64.getDecoder().decode(base64.getBytes(StandardCharsets.UTF_8));
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(bytes));
            File file = new File(directory, aMinecraftServer.getIp() + "-" + aMinecraftServer.getPort() + ".png");
            ImageIO.write(bufferedImage, "png", file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private File getIconFile(AMinecraftServer aMinecraftServer) {
        File file = new File(this.ccBotCore.getWorkingDirectory(), "mc-server-icons/" + aMinecraftServer.getIp() + "-" + aMinecraftServer.getPort() + ".png");
        if (!file.exists()) return null;
        return file;
    }

    private void deleteAllIcons() {
        File file = new File(this.ccBotCore.getWorkingDirectory(), "mc-server-icons");
        if (!file.exists()) return;
        for (File file1 : Objects.requireNonNull(file.listFiles())) {
            file1.delete();
        }
    }
}
