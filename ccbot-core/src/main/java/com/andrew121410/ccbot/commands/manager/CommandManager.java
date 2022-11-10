package com.andrew121410.ccbot.commands.manager;

import com.andrew121410.ccbot.CCBotCore;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import org.reflections.Reflections;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CommandManager {

    private Map<String, AbstractCommand> commandMap;

    private CCBotCore ccBotCore;
    private String prefix;

    public CommandManager(CCBotCore ccBotCore) {
        this.ccBotCore = ccBotCore;
        this.commandMap = this.ccBotCore.getCommandMap();
        this.prefix = this.ccBotCore.getConfigManager().getMainConfig().getPrefix();

        // Find all the commands and register them
        new Reflections("com.andrew121410.ccbot.commands").getTypesAnnotatedWith(ACommand.class).stream()
                .filter(AbstractCommand.class::isAssignableFrom)
                .forEach(aClass -> {
                    ACommand aCommand = aClass.getAnnotation(ACommand.class);
                    try {
                        this.commandMap.put(aCommand.command(), (AbstractCommand) aClass.getConstructor(CCBotCore.class).newInstance(ccBotCore));
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                });
    }

    @SubscribeEvent
    public void onMessageReceived(MessageReceivedEvent event) {
        // Check the prefix
        if (!event.getMessage().getContentRaw().startsWith(this.prefix)) return;

        // Ignore if member is null, and ignore if a bot
        if (event.getMember() == null || event.getAuthor().isBot()) return;

        String arg = event.getMessage().getContentRaw();
        String[] oldArgs = arg.split(" ");
        oldArgs[0] = oldArgs[0].replace(prefix, "");
        String command = oldArgs[0].toLowerCase();
        String[] modifiedArray = Arrays.copyOfRange(oldArgs, 1, oldArgs.length);

        if (this.commandMap.containsKey(command)) {
            AbstractCommand abstractCommand = this.commandMap.get(command);
            abstractCommand.onMessage(event, modifiedArray);
        }
    }

    public static boolean hasPermission(Member member, TextChannel textChannel, Permission permission) {
        if (!member.hasPermission(permission)) {
            textChannel.sendMessage("You don't have permission to do this.").queue(a -> a.delete().queueAfter(10, TimeUnit.SECONDS));
            return false;
        }
        return true;
    }
}

