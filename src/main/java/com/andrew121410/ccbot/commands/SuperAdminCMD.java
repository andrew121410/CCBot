package com.andrew121410.ccbot.commands;

import com.andrew121410.ccbot.CCBot;
import com.andrew121410.ccbot.commands.manager.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.concurrent.TimeUnit;

public class SuperAdminCMD implements ICommand {

    private CCBot ccBot;

    public SuperAdminCMD(CCBot ccBot) {
        this.ccBot = ccBot;
        this.ccBot.getCommandManager().register(this, "superadmin");
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
                for (Guild guild : this.ccBot.getJda().getGuilds()) {
                    stringBuilder.append(guild.getName()).append("(").append(guild.getId()).append(")").append("\r\n");
                }
                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setAuthor("CCBot Guilds List")
                        .setDescription("Show's all of the guilds that the bot is in.")
                        .addField("Guilds:", stringBuilder.toString(), false)
                        .addField("More Guild information:", "Total Guilds: " + this.ccBot.getJda().getGuilds().size(), false);
                event.getTextChannel().sendMessage(embedBuilder.build()).queue();
            }

            if (args.length == 1) {
                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setAuthor("CCBot SuperAdmin Guilds Usage!")
                        .addField("1.", this.ccBot.getConfigManager().getMainConfig().getPrefix() + "superadmin guilds list", false);
                event.getTextChannel().sendMessage(embedBuilder.build()).queue(a -> a.delete().queueAfter(10, TimeUnit.SECONDS));
            }
        }

        return false;
    }
}
