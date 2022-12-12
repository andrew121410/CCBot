package com.andrew121410.ccbot.commands.extra;

import com.andrew121410.ccbot.CCBotCore;
import com.andrew121410.ccbot.commands.manager.ACommand;
import com.andrew121410.ccbot.commands.manager.AbstractCommand;
import com.andrew121410.ccbot.commands.manager.CommandManager;
import com.andrew121410.ccbot.msp.AMinecraftServer;
import com.andrew121410.ccbot.msp.MCServerPingerManager;
import com.andrew121410.ccbot.objects.CGuild;
import com.andrew121410.ccutils.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

@ACommand(command = "msp", description = "Allows you to edit the configuration for the bot!")
public class MSpCMD extends AbstractCommand {

    private final CCBotCore ccBotCore;

    private MCServerPingerManager mcServerPingerManager;

    public MSpCMD(CCBotCore ccBotCore) {
        super(ccBotCore);
        this.ccBotCore = ccBotCore;

        this.mcServerPingerManager = this.ccBotCore.getMcServerPingerManager();
    }

    @Override
    public boolean onMessage(MessageReceivedEvent event, String[] args) {
        TextChannel textChannel = event.getGuildChannel().asTextChannel();

        if (!CommandManager.hasPermission(event.getMember(), textChannel, Permission.ADMINISTRATOR)) {
            return true;
        }
        CGuild cGuild = this.ccBotCore.getConfigManager().getGuildConfigManager().addOrGet(event.getGuild());

        String prefix = ccBotCore.getConfigManager().getMainConfig().getPrefix();

        if (args.length == 0) {
            EmbedBuilder embedBuilder = new EmbedBuilder();

            embedBuilder.setTitle("Minecraft Server Pinger");
            embedBuilder.setDescription("Minecraft Server Pinger allows you to ping a minecraft server and send a message to a channel when the server turns on or off.");
            embedBuilder.setColor(new Color(0, 128, 0)); // Green

            // Usage of commands
            embedBuilder.addField("Usage",
                    prefix + "msp add <ip> <port> <name>"
                            + "\r\n" + prefix + "msp remove <ip>"
                            + "\r\n" + prefix + "msp list", false);
            embedBuilder.addField("Settings",
                    prefix + "msp useStatusWebsiteAPI <ip> <true/false> -> (default: false) - This will use https://mcsrvstat.us/ to get the status of the server."
                            + "\r\n" + prefix + "msp maxAttempts <ip> <int> -> (default: 3) - The max attempts until we send a message that the server is offline.", false);

            // Show the list of servers if there are any
            if (cGuild.getaMinecraftServers().size() > 0) {
                StringBuilder stringBuilder = new StringBuilder();
                for (AMinecraftServer aMinecraftServer : cGuild.getaMinecraftServers()) {
                    stringBuilder.append(aMinecraftServer.getName()).append(" -> ").append(aMinecraftServer.getIp()).append(":").append(aMinecraftServer.getPort()).append("\r\n");
                }
                embedBuilder.addField("Your servers", stringBuilder.toString(), false);
            }

            textChannel.sendMessageEmbeds(embedBuilder.build()).queue();
            return true;
        } else if (args.length >= 4 && args[0].equalsIgnoreCase("add")) {
            String ip = args[1];
            String port = args[2];
            String name = String.join(" ", Arrays.copyOfRange(args, 3, args.length));

            this.mcServerPingerManager.add(event.getGuild(), new AMinecraftServer(textChannel.getIdLong(), name, ip, Integer.parseInt(port), false));
            textChannel.sendMessage("Adding " + ip + ":" + port + " with the name " + name).queue();
            return true;
        } else if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
            String ip = args[1];

            if (ip.equalsIgnoreCase("all")) {
                this.mcServerPingerManager.removeAll(event.getGuild());
                textChannel.sendMessage("Removed all servers!").queue();
                return true;
            }

            this.mcServerPingerManager.remove(event.getGuild(), ip);
            textChannel.sendMessage("Removed the server -> " + ip).queue();
            return true;
        } else if (args[0].equalsIgnoreCase("list")) {
            List<AMinecraftServer> aMinecraftServers = cGuild.getaMinecraftServers();

            if (aMinecraftServers.isEmpty()) {
                textChannel.sendMessage("There are no servers to list!").queue();
                return true;
            }

            StringBuilder stringBuilder = new StringBuilder();

            // Debug
            if (args.length == 2 && args[1].equalsIgnoreCase("@debug")) {
                for (AMinecraftServer aMinecraftServer : aMinecraftServers) {
                    stringBuilder.append(aMinecraftServer.toString()).append("\r\n");
                }
                textChannel.sendMessage(stringBuilder.toString()).queue();
                return true;
            }


            for (AMinecraftServer aMinecraftServer : aMinecraftServers) {
                stringBuilder.append(aMinecraftServer.getName()).append(" -> ").append(aMinecraftServer.getIp()).append(":").append(aMinecraftServer.getPort()).append("\r\n");
            }

            textChannel.sendMessage(stringBuilder.toString()).queue();
        } else if (args[0].equalsIgnoreCase("useStatusWebsiteAPI")) {
            if (args.length == 1) { // Send usage
                textChannel.sendMessage(prefix + "msp useStatusWebsiteAPI <true/false>").queue();
                return true;
            } else if (args.length == 3) {
                String ip = args[1];
                boolean bool = Utils.asBooleanOrElse(args[2], false);

                cGuild.getaMinecraftServers().stream().filter(aMinecraftServer -> aMinecraftServer.getIp().equalsIgnoreCase(ip)).findFirst().ifPresent(aMinecraftServer -> {
                    aMinecraftServer.setUseStatusWebsiteApi(bool);
                    textChannel.sendMessage("Set " + aMinecraftServer.getIp() + " useStatusWebsiteApi to -> " + aMinecraftServer.isUseStatusWebsiteApi()).queue();
                });
            }
            return true;
        } else if (args[0].equalsIgnoreCase("maxAttempts")) {
            if (args.length == 1) { // Send usage
                textChannel.sendMessage(prefix + "msp maxAttempts <ip> <int>").queue();
                return true;
            } else if (args.length == 3) {
                String ip = args[1];
                int maxAttempts = Utils.asIntegerOrElse(args[2], 3);

                cGuild.getaMinecraftServers().stream().filter(aMinecraftServer -> aMinecraftServer.getIp().equalsIgnoreCase(ip)).findFirst().ifPresent(aMinecraftServer -> {
                    aMinecraftServer.setMaxAttempts(maxAttempts);
                    textChannel.sendMessage("Set " + aMinecraftServer.getIp() + " maxAttempts to -> " + aMinecraftServer.getMaxAttempts()).queue();
                });
            }
            return true;
        }
        return true;
    }
}