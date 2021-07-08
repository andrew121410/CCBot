package com.andrew121410.ccbot.commands.manager;

import com.andrew121410.ccbot.CCBotCore;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CommandManager {

    private Map<String, ICommand> commandMap;

    private CCBotCore ccBotCore;
    private String prefix;

    public CommandManager(CCBotCore ccBotCore) {
        this.ccBotCore = ccBotCore;
        this.commandMap = this.ccBotCore.getSetListMap().getCommandMap();
        this.prefix = this.ccBotCore.getConfigManager().getMainConfig().getPrefix();
    }

    @SubscribeEvent
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.getMessage().getContentRaw().startsWith(prefix)) {
            return;
        }
        String arg = event.getMessage().getContentRaw();
        String[] oldArgs = arg.split(" ");
        oldArgs[0] = oldArgs[0].replace(prefix, "");
        String command = oldArgs[0].toLowerCase();
        String[] modifiedArray = Arrays.copyOfRange(oldArgs, 1, oldArgs.length);

        if (this.commandMap.containsKey(command)) {
            ICommand iCommand = this.commandMap.get(command);
            iCommand.onMessage(event, modifiedArray);
        }
    }

    public void register(ICommand iCommandManager, String command) {
        this.commandMap.putIfAbsent(command.toLowerCase(), iCommandManager);
        System.out.println("CommandManager Registered: " + iCommandManager.getClass() + " ? CMD: " + command);
    }

    public static boolean hasPermission(Member member, TextChannel textChannel, Permission permission) {
        if (!member.hasPermission(permission)) {
            textChannel.sendMessage("You don't have permission to do this.").queue(a -> a.delete().queueAfter(10, TimeUnit.SECONDS));
            return false;
        }
        return true;
    }
}
