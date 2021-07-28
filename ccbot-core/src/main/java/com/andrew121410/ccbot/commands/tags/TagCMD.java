package com.andrew121410.ccbot.commands.tags;

import com.andrew121410.ccbot.CCBotCore;
import com.andrew121410.ccbot.commands.manager.ACommand;
import com.andrew121410.ccbot.commands.manager.AbstractCommand;
import com.andrew121410.ccbot.objects.CGuild;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;

@ACommand(command = "tag", description = "Shows the tag?")
public class TagCMD extends AbstractCommand {

    private CCBotCore ccBotCore;

    public TagCMD(CCBotCore ccBotCore) {
        super(ccBotCore);
        this.ccBotCore = ccBotCore;
    }

    @Override
    public boolean onMessage(MessageReceivedEvent event, String[] args) {
        CGuild cGuild = this.ccBotCore.getSetListMap().getGuildMap().get(event.getGuild().getId());

        if (args.length == 0) {
            String[] tags = cGuild.getTags().keySet().toArray(new String[0]);
            StringBuilder stringBuilder = new StringBuilder();
            int i = 1;
            for (String tag : tags) {
                stringBuilder.append("\r\n");
                stringBuilder.append(i).append(". ");
                stringBuilder.append(tag);
                i++;
            }

            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle("List of all tags!")
                    .setDescription(stringBuilder.toString())
                    .setColor(Color.magenta);
            event.getTextChannel().sendMessageEmbeds(embedBuilder.build()).queue();
            return true;
        } else if (args.length == 1) {
            String tag = args[0].toLowerCase();
            String tagMessage = cGuild.getTags().get(tag);
            if (tagMessage == null) {
                event.getTextChannel().sendMessage("That's not a tag.").queue();
                return true;
            }
            event.getTextChannel().sendMessage(tagMessage).queue();
        }
        return true;
    }
}
