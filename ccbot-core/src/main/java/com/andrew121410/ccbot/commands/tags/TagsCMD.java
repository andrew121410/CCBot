package com.andrew121410.ccbot.commands.tags;

import com.andrew121410.ccbot.CCBotCore;
import com.andrew121410.ccbot.commands.manager.ACommand;
import com.andrew121410.ccbot.commands.manager.AbstractCommand;
import com.andrew121410.ccbot.commands.manager.CommandManager;
import com.andrew121410.ccbot.objects.CGuild;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.Arrays;

@ACommand(command = "tags", description = "Show's all the tags on the guild")
public class TagsCMD extends AbstractCommand {

    private CCBotCore ccBotCore;

    public TagsCMD(CCBotCore ccBotCore) {
        super(ccBotCore);
        this.ccBotCore = ccBotCore;
    }

    @Override
    public boolean onMessage(MessageReceivedEvent event, String[] args) {
        CGuild cGuild = this.ccBotCore.getSetListMap().getGuildMap().get(event.getGuild().getId());
        if (cGuild == null) {
            event.getTextChannel().sendMessage("Something went wrong.").queue();
            throw new NullPointerException("CGuild is null...");
        }

        String[] tags = cGuild.getTags().keySet().toArray(new String[0]);
        StringBuilder stringBuilder = new StringBuilder();
        int i = 1;
        for (String tag : tags) {
            stringBuilder.append("\r\n");
            stringBuilder.append(i).append(". ");
            stringBuilder.append(tag);
            i++;
        }

        if (!CommandManager.hasPermission(event.getMember(), event.getTextChannel(), Permission.MANAGE_SERVER)) {
            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle("List of all tags!")
                    .setDescription(stringBuilder.toString())
                    .setColor(Color.MAGENTA);
            event.getTextChannel().sendMessageEmbeds(embedBuilder.build()).queue();
            return true;
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("add")) {
            String tag = args[1].toLowerCase();
            //tag add test START
            String[] wordArray = Arrays.copyOfRange(args, 2, args.length);

            if (cGuild.getTags().containsKey(tag)) {
                event.getTextChannel().sendMessage("The tag " + tag + " already exists.").queue();
                return true;
            }
            cGuild.getTags().put(tag, String.join(" ", wordArray));
            event.getTextChannel().sendMessage("**Tag added!**").queue();
            return true;
        } else if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
            String tag = args[1].toLowerCase();

            if (!cGuild.getTags().containsKey(tag)) {
                event.getTextChannel().sendMessage("That tag doesn't even exist.").queue();
                return true;
            }
            cGuild.getTags().remove(tag);
            event.getTextChannel().sendMessage("**Tag was removed!**").queue();
            return true;
        } else {
            String prefix = this.ccBotCore.getConfigManager().getMainConfig().getPrefix();
            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setAuthor("Tags Help!")
                    .setColor(Color.RED)
                    .addField("Usage:", prefix + "tags add test this is a test tag \r\n"
                            + prefix + "tags remove test", false)
                    .addField("Tags:", stringBuilder.toString(), false);
            event.getTextChannel().sendMessageEmbeds(embedBuilder.build()).queue();
        }
        return true;
    }
}
