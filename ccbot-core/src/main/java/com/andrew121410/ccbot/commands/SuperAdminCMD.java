package com.andrew121410.ccbot.commands;

import com.andrew121410.ccbot.CCBotCore;
import com.andrew121410.ccbot.commands.manager.ACommand;
import com.andrew121410.ccbot.commands.manager.AbstractCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@ACommand(command = "superadmin", description = "null")
public class SuperAdminCMD extends AbstractCommand {

    private CCBotCore ccBotCore;

    public SuperAdminCMD(CCBotCore ccBotCore) {
        super(ccBotCore);
        this.ccBotCore = ccBotCore;
    }

    @Override
    public boolean onMessage(MessageReceivedEvent event, String[] args) {
        if (!event.getMember().getUser().getName().equals("Andrew121410") && !event.getMember().getUser().getDiscriminator().equals("#2035")) {
            return false;
        }

        if (args.length == 0) {
            event.getTextChannel().sendMessage("You're honestly not smart.").queue(a -> a.delete().queueAfter(5, TimeUnit.SECONDS));
        } else if (args[0].equalsIgnoreCase("guilds")) {
            if (args.length == 2 && args[1].equalsIgnoreCase("list")) {
                StringBuilder stringBuilder = new StringBuilder();
                for (Guild guild : this.ccBotCore.getJda().getGuilds()) {
                    stringBuilder.append(guild.getName()).append("(").append(guild.getId()).append(")").append("\r\n");
                }
                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setAuthor("CCBot Guilds List")
                        .setDescription("Show's all of the guilds that the bot is in.")
                        .addField("Guilds:", stringBuilder.toString(), false)
                        .addField("More Guild information:", "Total Guilds: " + this.ccBotCore.getJda().getGuilds().size(), false);
                event.getTextChannel().sendMessageEmbeds(embedBuilder.build()).queue();
            }

            if (args.length == 1) {
                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setAuthor("CCBot SuperAdmin Guilds Usage!")
                        .addField("1.", this.ccBotCore.getConfigManager().getMainConfig().getPrefix() + "superadmin guilds list", false);
                event.getTextChannel().sendMessageEmbeds(embedBuilder.build()).queue(a -> a.delete().queueAfter(10, TimeUnit.SECONDS));
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("invite")) {
            String guildId = args[1];

            Guild guild = this.ccBotCore.getJda().getGuildById(guildId);
            if (guild == null) {
                event.getTextChannel().sendMessage("The bot isn't in that guild **FAILED**").queue(message -> message.delete().queueAfter(5, TimeUnit.SECONDS));
                return true;
            }
            guild.retrieveInvites().queue(list -> {
                if (list == null || list.isEmpty()) return;
                Random rand = new Random();
                Invite randomInvite = list.get(rand.nextInt(list.size()));
                event.getTextChannel().sendMessage(randomInvite.getUrl()).queue();
            });
            return true;
        }
        return false;
    }
}
