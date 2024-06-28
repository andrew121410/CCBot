package com.andrew121410.ccbot.commands.extra;

import com.andrew121410.ccbot.CCBotCore;
import com.andrew121410.ccbot.commands.manager.ACommand;
import com.andrew121410.ccbot.commands.manager.AbstractCommand;
import com.andrew121410.ccbot.commands.manager.CommandManager;
import com.andrew121410.ccbot.guilds.CGuild;
import com.andrew121410.ccbot.msp.AMinecraftServer;
import com.andrew121410.ccbot.msp.MCServerPingerManager;
import com.andrew121410.ccutils.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

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
//                    prefix + "msp useStatusWebsiteAPI <ip> <true/false> -> (default: false) - This will use https://mcsrvstat.us/ to get the status of the server."
                    //+ "\r\n" +
                    prefix + "msp maxAttempts <ip> <int> -> (default: 3) - The max attempts until we send a message that the server is offline."
                            + "\r\n" + prefix + "msp channel <ip> -> Change where the message is sent when the server goes online or offline. (Changes to this text channel)"
//                            + "\r\n" + prefix + "msp beMoreDescriptive <ip> <true/false> -> (default: false) - This will make the message more descriptive."
                    , false);

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
        } else if (args.length >= 3 && args[0].equalsIgnoreCase("add")) {
            String ip = args[1];
            Integer port;
            String name;

            if (args.length >= 4) {
                port = Utils.asIntegerOrElse(args[2], 0); // default Minecraft port
                name = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
            } else {
                port = 25565; // default Minecraft port
                name = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
            }

            if (port == 0) {
                textChannel.sendMessage("Invalid port!").queue();
                return true;
            }

            this.mcServerPingerManager.add(event.getGuild(), new AMinecraftServer(textChannel.getIdLong(), name, ip, port));
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
        } else if (args[0].equalsIgnoreCase("channel")) {
            if (args.length == 1) { // Send usage
                textChannel.sendMessage(prefix + "msp channel <ip>").queue();
                return true;
            } else if (args.length == 2) {
                String ip = args[1];

                cGuild.getaMinecraftServers().stream().filter(aMinecraftServer -> aMinecraftServer.getIp().equalsIgnoreCase(ip)).findFirst().ifPresent(aMinecraftServer -> {
                    aMinecraftServer.setChannelId(textChannel.getIdLong());
                    textChannel.sendMessage("Set " + aMinecraftServer.getIp() + " channel to -> " + aMinecraftServer.getChannelId()).queue();
                });
            }
            return true;
//        } else if (args[0].equalsIgnoreCase("beMoreDescriptive")) {
//            if (args.length == 1) { // Send usage
//                textChannel.sendMessage(prefix + "msp beMoreDescriptive <ip> <true/false>").queue();
//                return true;
//            } else if (args.length == 3) {
//                String ip = args[1];
//                boolean beMoreDescriptive = Utils.asBooleanOrElse(args[2], false);
//
//                cGuild.getaMinecraftServers().stream().filter(aMinecraftServer -> aMinecraftServer.getIp().equalsIgnoreCase(ip)).findFirst().ifPresent(aMinecraftServer -> {
//                    aMinecraftServer.setBeMoreDescriptive(beMoreDescriptive);
//                    textChannel.sendMessage("Set " + aMinecraftServer.getIp() + " beMoreDescriptive to -> " + aMinecraftServer.isBeMoreDescriptive()).queue();
//                });
//            }
//            return true;
        } else if (args[0].equalsIgnoreCase("isThreadFrozen")) {
            AtomicLong seeIfFrozen = this.ccBotCore.getMcServerPingerManager().getLastRan();

            // Check if more than 3 mins
            if (System.currentTimeMillis() - seeIfFrozen.get() > 180000) {
                event.getChannel().sendMessage("It is frozen!!!").queue();
            } else {
                event.getChannel().sendMessage("It is not frozen!!!").queue();
            }
        }
        return true;
    }
}