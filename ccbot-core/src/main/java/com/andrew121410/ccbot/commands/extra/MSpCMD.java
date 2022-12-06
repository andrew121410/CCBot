package com.andrew121410.ccbot.commands.extra;

import com.andrew121410.ccbot.CCBotCore;
import com.andrew121410.ccbot.commands.manager.ACommand;
import com.andrew121410.ccbot.commands.manager.AbstractCommand;
import com.andrew121410.ccbot.commands.manager.CommandManager;
import com.andrew121410.ccbot.msp.AMinecraftServer;
import com.andrew121410.ccbot.msp.MCServerPingerManager;
import com.andrew121410.ccbot.objects.CGuild;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

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
            textChannel.sendMessage(prefix + "msp add <ip> <port>" + "\r\n" + prefix + "msp remove <ip>" + "\r\n" + prefix + "msp list").queue();
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
        } else if (args.length == 2 && args[0].equalsIgnoreCase("useStatusWebsiteAPI")) {
            String ip = args[1];

            cGuild.getaMinecraftServers().stream().filter(aMinecraftServer -> aMinecraftServer.getIp().equalsIgnoreCase(ip)).findFirst().ifPresent(aMinecraftServer -> {
                aMinecraftServer.setUseStatusWebsiteApi(true);
                textChannel.sendMessage("Set " + aMinecraftServer.getIp() + " to use the status website API -> " + aMinecraftServer.isUseStatusWebsiteApi()).queue();
            });
        }
        return true;
    }
}