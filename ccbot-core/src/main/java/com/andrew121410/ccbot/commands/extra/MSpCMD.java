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
            textChannel.sendMessage(prefix + "msp add <ip> <port>" + "\r\n" + prefix + "msp remove <ip>").queue();
            return true;
        } else if (args.length >= 2 && args[0].equalsIgnoreCase("add")) {
            String ip = args[1];
            String port = args.length == 2 ? "25565" : args[2];
            this.mcServerPingerManager.add(event.getGuild(), new AMinecraftServer(textChannel.getIdLong(), ip, Integer.parseInt(port)));
            textChannel.sendMessage("Added the server -> " + ip + ":" + port).queue();
        } else if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
            String ip = args[1];

            if (ip.equalsIgnoreCase("all")) {
                this.mcServerPingerManager.removeAll(event.getGuild());
                textChannel.sendMessage("Removed all servers!").queue();
                return true;
            }

            this.mcServerPingerManager.remove(event.getGuild(), ip);
            textChannel.sendMessage("Removed the server -> " + ip).queue();
        }
        return true;
    }
}