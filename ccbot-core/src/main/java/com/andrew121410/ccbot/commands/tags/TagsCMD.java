package com.andrew121410.ccbot.commands.tags;

import com.andrew121410.ccbot.CCBotCore;
import com.andrew121410.ccbot.commands.manager.ACommand;
import com.andrew121410.ccbot.commands.manager.AbstractCommand;
import com.andrew121410.ccbot.commands.manager.CommandManager;
import com.andrew121410.ccbot.objects.CGuild;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
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
        TextChannel textChannel = event.getChannel().asTextChannel();

        CGuild cGuild = this.ccBotCore.getConfigManager().getGuildConfigManager().addOrGet(event.getGuild());

        String[] tags = cGuild.getTags().keySet().toArray(new String[0]);
        StringBuilder stringBuilder = new StringBuilder();
        int i = 1;
        for (String tag : tags) {
            stringBuilder.append("\r\n");
            stringBuilder.append(i).append(". ");
            stringBuilder.append(tag);
            i++;
        }

        if (!CommandManager.hasPermission(event.getMember(), textChannel, Permission.MANAGE_SERVER)) {
            if (tags.length == 0) {
                textChannel.sendMessage("There are no tags on this guild.").queue();
                return true;
            }

            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle("List of all tags!")
                    .setDescription(stringBuilder.toString())
                    .setColor(Color.MAGENTA);
            textChannel.sendMessageEmbeds(embedBuilder.build()).queue();
            return true;
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("add")) {
            String tag = args[1].toLowerCase();
            //tag add test START
            String[] wordArray = Arrays.copyOfRange(args, 2, args.length);

            if (cGuild.getTags().containsKey(tag)) {
                textChannel.sendMessage("The tag " + tag + " already exists.").queue();
                return true;
            }
            cGuild.getTags().put(tag, String.join(" ", wordArray));
            textChannel.sendMessage("**Tag added!**").queue();
            return true;
        } else if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
            String tag = args[1].toLowerCase();

            if (!cGuild.getTags().containsKey(tag)) {
                textChannel.sendMessage("That tag doesn't even exist.").queue();
                return true;
            }
            cGuild.getTags().remove(tag);
            textChannel.sendMessage("**Tag was removed!**").queue();
            return true;
        } else {
            String prefix = this.ccBotCore.getConfigManager().getMainConfig().getPrefix();
            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setAuthor("Tags Help!")
                    .setColor(Color.RED)
                    .addField("Usage:", prefix + "tags add test this is a test tag \r\n"
                            + prefix + "tags remove test", false);

            if (tags.length != 0) {
                embedBuilder.addField("Tags:", stringBuilder.toString(), false);
            }

            textChannel.sendMessageEmbeds(embedBuilder.build()).queue();
        }
        return true;
    }
}
