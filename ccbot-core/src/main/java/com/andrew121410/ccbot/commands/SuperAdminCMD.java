package com.andrew121410.ccbot.commands;

import com.andrew121410.ccbot.CCBotCore;
import com.andrew121410.ccbot.commands.manager.ACommand;
import com.andrew121410.ccbot.commands.manager.AbstractCommand;
import com.andrew121410.ccutils.utils.HashBasedUpdater;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@ACommand(command = "superadmin", description = "null")
public class SuperAdminCMD extends AbstractCommand {

    /**
     * You might think this is very suspicious but this just has basic commands like leaving a guild or seeing the guilds the bot is in.
     * I might remove this in the future but for now it's here.
     * <p>
     * Don't worry I'm not going to steal your server or anything lol, sure that's against the TOS
     */

    private final CCBotCore ccBotCore;

    public SuperAdminCMD(CCBotCore ccBotCore) {
        super(ccBotCore);
        this.ccBotCore = ccBotCore;
    }

    @Override
    public boolean onMessage(MessageReceivedEvent event, String[] args) {
        TextChannel textChannel = event.getGuildChannel().asTextChannel();
        if (event.getMember() == null) return false;

        if (event.getMember().getUser().getIdLong() != 122772122790526976L) {
            return false;
        }

        if (args.length == 0) {
            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setAuthor("CCBot SuperAdmin Usage!")
                    .addField("1.", this.ccBotCore.getConfigManager().getMainConfig().getPrefix() + "superadmin guilds", false)
                    .addField("2.", this.ccBotCore.getConfigManager().getMainConfig().getPrefix() + "superadmin invite <guildId>", false)
                    .addField("3.", this.ccBotCore.getConfigManager().getMainConfig().getPrefix() + "superadmin name-testing", false);
            textChannel.sendMessageEmbeds(embedBuilder.build()).queue(a -> a.delete().queueAfter(10, TimeUnit.SECONDS));
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
                textChannel.sendMessageEmbeds(embedBuilder.build()).queue();
                return true;
            } else if (args.length == 3 && args[1].equalsIgnoreCase("leave")) {
                Guild guild = this.ccBotCore.getJda().getGuildById(args[2]);
                if (guild == null) {
                    textChannel.sendMessage("Guild not found.").queue();
                    return false;
                }
                guild.leave().queue();
                textChannel.sendMessage("Left guild: " + guild.getName()).queue();
                return true;
            } else {
                if (args.length == 1) {
                    EmbedBuilder embedBuilder = new EmbedBuilder()
                            .setAuthor("CCBot SuperAdmin Guilds Usage!")
                            .addField("1.", this.ccBotCore.getConfigManager().getMainConfig().getPrefix() + "superadmin guilds list", false)
                            .addField("2.", this.ccBotCore.getConfigManager().getMainConfig().getPrefix() + "superadmin guilds leave <guildId>", false);
                    textChannel.sendMessageEmbeds(embedBuilder.build()).queue(a -> a.delete().queueAfter(10, TimeUnit.SECONDS));
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("invite")) {
            String guildId = args[1];

            Guild guild = this.ccBotCore.getJda().getGuildById(guildId);
            if (guild == null) {
                textChannel.sendMessage("The bot isn't in that guild **FAILED**").queue(message -> message.delete().queueAfter(5, TimeUnit.SECONDS));
                return true;
            }
            guild.retrieveInvites().queue(list -> {
                if (list == null || list.isEmpty()) return;
                Random rand = new Random();
                Invite randomInvite = list.get(rand.nextInt(list.size()));
                textChannel.sendMessage(randomInvite.getUrl()).queue();
            });
            return true;
        } else if (args.length == 1 && args[0].equalsIgnoreCase("name-testing")) {
            String name = event.getMember().getUser().getName();
            String effectiveName = event.getMember().getEffectiveName();
            String globalName = event.getMember().getUser().getGlobalName();
            String nickname = event.getMember().getNickname();

            // Make a embed
            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setAuthor("Name Testing")
                    .addField("Name:", name, false)
                    .addField("Effective Name:", effectiveName, false);
            if (globalName != null) {
                embedBuilder.addField("Global Name:", globalName, false);
            }
            if (nickname != null) {
                embedBuilder.addField("Nickname:", nickname, false);
            }
            textChannel.sendMessageEmbeds(embedBuilder.build()).queue();
        } else if (args.length == 1 && args[0].equalsIgnoreCase("update")) {
            String latestJar = "https://github.com/andrew121410/CCBot/releases/download/latest/CCBot.jar";
            String latestHash = "https://github.com/andrew121410/CCBot/releases/download/latest/hash.txt";

            HashBasedUpdater hashBasedUpdater = new HashBasedUpdater(this.ccBotCore.getClass(), latestJar, latestHash);

            if (!hashBasedUpdater.shouldUpdate()) {
                textChannel.sendMessage("The bot is already up to date!").queue();
                return false;
            }

            // Run in a new thread, so we don't block the main thread.
            Runnable runnable = () -> {
                textChannel.sendMessage("Updating the bot!").queue();
                hashBasedUpdater.update();

                textChannel.sendMessage("The bot has been updated! Now restarting...").queue((message) -> {
                    // Shutdown the bot.
                    this.ccBotCore.exit();
                });
            };

            new Thread(runnable).start();
        }
        return false;
    }
}
