package com.andrew121410.ccbot.msp;

import br.com.azalim.mcserverping.MCPing;
import br.com.azalim.mcserverping.MCPingOptions;
import br.com.azalim.mcserverping.MCPingResponse;
import com.andrew121410.ccbot.CCBotCore;
import com.andrew121410.ccbot.guilds.CGuild;
import com.andrew121410.ccutils.utils.TimeUtils;
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
import java.nio.charset.StandardCharsets;
import java.time.Instant;
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
                    MinecraftServerStatus serverStatus = getServerStatus(aMinecraftServer);
                    if (serverStatus == null) return;

                    if (!serverStatus.getOnline()) { // If the server is offline.
                        // Add 1 to the attempts.
                        aMinecraftServer.setAttempts(aMinecraftServer.getAttempts() + 1);

                        // Don't let the attempts go over 100.
                        if (aMinecraftServer.getAttempts() >= 100) aMinecraftServer.setAttempts(5);

                        // Send the message if the attempts are over the max attempts, and if the message hasn't been sent.
                        if (aMinecraftServer.getAttempts() >= aMinecraftServer.getMaxAttempts() && !aMinecraftServer.isSentMessage()) {
                            TextChannel textChannel = guild.getTextChannelById(aMinecraftServer.getChannelId());
                            if (textChannel == null) return;

                            // Set the time of offline
                            aMinecraftServer.setTimeOfOffline(System.currentTimeMillis());

                            EmbedBuilder embedBuilder = new EmbedBuilder();
                            embedBuilder.setColor(new Color(255, 100, 100)); // Custom color
                            embedBuilder.setTitle("**" + aMinecraftServer.getName() + "** is offline!"); // Bold title
                            embedBuilder.setDescription("The Minecraft Server is offline!");

                            embedBuilder.addField("IP", "`" + aMinecraftServer.getIp() + "`", true);

                            // If the port is not the default port (25565)
                            if (aMinecraftServer.getPort() != 25565) {
                                embedBuilder.addField("Port", "`" + aMinecraftServer.getPort() + "`", true);
                            }

                            if (doesIconExist(aMinecraftServer)) { // Send the message with the icon.
                                embedBuilder.setThumbnail("attachment://server.png");
                                textChannel.sendFiles(FileUpload.fromData(getIconFile(aMinecraftServer), "server.png")).setEmbeds(embedBuilder.build()).queue();
                            } else { // Send the message without the icon.
                                textChannel.sendMessageEmbeds(embedBuilder.build()).queue();
                            }

                            embedBuilder.setFooter("Server Status", null);
                            embedBuilder.setTimestamp(Instant.now()); // Current timestamp

                            aMinecraftServer.setSentMessage(true);
                        }
                    } else { // If the server is online.
                        aMinecraftServer.setAttempts(0);

                        if (aMinecraftServer.isSentMessage()) {
                            TextChannel textChannel = guild.getTextChannelById(aMinecraftServer.getChannelId());
                            if (textChannel == null) return;

                            EmbedBuilder embedBuilder = new EmbedBuilder();
                            embedBuilder.setColor(new Color(100, 255, 100)); // Custom color
                            embedBuilder.setTitle("**" + aMinecraftServer.getName() + "** is online!"); // Bold title

                            if (aMinecraftServer.getTimeOfOffline() != 0L) {
                                embedBuilder.setDescription("The Minecraft Server is online!"
                                        + "\n\rThe server was offline for " + TimeUtils.makeIntoEnglishWords(aMinecraftServer.getTimeOfOffline(), System.currentTimeMillis(), false, false));
                            } else {
                                embedBuilder.setDescription("The Minecraft Server is online!");
                            }

                            // Add fields
                            embedBuilder.addField("IP", "`" + aMinecraftServer.getIp() + "`", true);

                            // If the port is not the default port (25565)
                            if (aMinecraftServer.getPort() != 25565) {
                                embedBuilder.addField("Port", "`" + aMinecraftServer.getPort() + "`", true);
                            }

                            // Send the message
                            if (doesIconExist(aMinecraftServer)) { // Send the message with the icon.
                                embedBuilder.setThumbnail("attachment://server.png");
                                textChannel.sendFiles(FileUpload.fromData(getIconFile(aMinecraftServer), "server.png")).setEmbeds(embedBuilder.build()).queue();
                            } else { // Send the message without the icon.
                                textChannel.sendMessageEmbeds(embedBuilder.build()).queue();
                            }

                            // Add footer and timestamp
                            embedBuilder.setFooter("Server Status", null); // You can replace null with a URL of an icon for the footer
                            embedBuilder.setTimestamp(Instant.now()); // Current timestamp

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
