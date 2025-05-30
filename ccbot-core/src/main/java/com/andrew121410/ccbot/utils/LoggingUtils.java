package com.andrew121410.ccbot.utils;

import com.andrew121410.ccbot.CCBotCore;
import com.andrew121410.ccbot.config.GuildConfigManager;
import com.andrew121410.ccbot.guilds.AMessage;
import com.andrew121410.ccbot.guilds.CGuild;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.events.role.RoleCreateEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.events.role.update.RoleUpdateNameEvent;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LoggingUtils {

    private final CCBotCore ccBotCore;

    private final GuildConfigManager guildConfigManager;

    public LoggingUtils(CCBotCore ccBotCore) {
        this.ccBotCore = ccBotCore;
        this.guildConfigManager = this.ccBotCore.getConfigManager().getGuildConfigManager();
    }

    public void handle(GuildMemberJoinEvent event) {
        CGuild cGuild = this.guildConfigManager.addOrGet(event.getGuild());
        long time = Instant.now().getEpochSecond();

        if (cGuild.getSettings().getWelcomeMessages() && event.getGuild().getSystemChannel() != null) {
            TextChannel welcomeChannel = CUtils.findTextChannel(event.getGuild().getTextChannels(), "rules", "chat-rules", "rules-and-info");

            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setAuthor("🎉 Welcome!", event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl())
                    .setTitle("Welcome to " + event.getGuild().getName() + "!")
                    .setDescription(event.getUser().getAsMention() + " just joined the server. " +
                            "Please check the " + (welcomeChannel != null ? welcomeChannel.getAsMention() : "rules") + " and have fun!")
                    .addField("Account Created", "<t:" + event.getUser().getTimeCreated().toEpochSecond() + ":D>", true)
                    .addField("Joined", "<t:" + time + ":R>", true)
                    .setColor(new Color(0x57F287)) // Discord green
                    .setThumbnail(event.getUser().getAvatarUrl())
                    .setFooter("User ID: " + event.getUser().getId())
                    .setTimestamp(Instant.now());

            event.getGuild().getSystemChannel().sendMessageEmbeds(embedBuilder.build()).queue();
        }

        if (cGuild.getSettings().isLoggingEnabled()) {
            TextChannel logChannel = CUtils.findLogChannel(event.getGuild());
            if (logChannel == null) return;
            EmbedBuilder logEmbedBuilder = new EmbedBuilder()
                    .setAuthor("🎉 Member Joined!", event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl())
                    .setDescription(event.getUser().getAsMention() + " has joined the server.")
                    .addField("Account Created", "<t:" + event.getUser().getTimeCreated().toEpochSecond() + ":D>", true)
                    .addField("Joined", "<t:" + time + ":R>", true)
                    .setColor(new Color(0x5865F2)) // Discord blurple
                    .setThumbnail(event.getUser().getAvatarUrl())
                    .setFooter("User ID: " + event.getUser().getId())
                    .setTimestamp(Instant.now());
            logChannel.sendMessageEmbeds(logEmbedBuilder.build()).queue();
        }
    }

    public void handle(GuildMemberRemoveEvent event) {
        CGuild cGuild = this.guildConfigManager.addOrGet(event.getGuild());
        if (!cGuild.getSettings().isLoggingEnabled()) return;

        List<String> channelList = new ArrayList<>();
        channelList.add("leaves");
        channelList.add("lefts");
        channelList.addAll(Arrays.asList(CUtils.getLogsStringArray()));
        TextChannel textChannel = CUtils.findTextChannel(event.getGuild().getTextChannels(), channelList);

        if (textChannel == null) return;

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setAuthor("Member Left:", event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl())
                .setDescription(event.getUser().getName() + " has left the server \uD83D\uDE22")
                .setColor(Color.YELLOW)
                .setThumbnail(event.getUser().getAvatarUrl());

        // If the user doesn't have the same name as their effective name, then add the effective name to the embed message
        if (!event.getUser().getName().equals(event.getUser().getEffectiveName())) {
            embedBuilder.addField("Effective Name", event.getUser().getEffectiveName(), false);
        }

        textChannel.sendMessageEmbeds(embedBuilder.build()).queue();
    }

    public void handle(RoleCreateEvent event) {
        CGuild cGuild = this.guildConfigManager.addOrGet(event.getGuild());
        if (!cGuild.getSettings().isLoggingEnabled()) return;

        TextChannel logChannel = CUtils.findLogChannel(event.getGuild());
        if (logChannel == null) return;

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setAuthor("Role Created:", event.getRole().getGuild().getIconUrl(), event.getRole().getGuild().getIconUrl())
                .setDescription("**The role** " + event.getRole().getAsMention() + " **has been created!**")
                .setColor(Color.GREEN);

        logChannel.sendMessageEmbeds(embedBuilder.build()).queue();
    }

    public void handle(RoleDeleteEvent event) {
        CGuild cGuild = this.guildConfigManager.addOrGet(event.getGuild());
        if (!cGuild.getSettings().isLoggingEnabled()) return;

        TextChannel logChannel = CUtils.findLogChannel(event.getGuild());
        if (logChannel == null) return;

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setAuthor("Role Deleted:", event.getRole().getGuild().getIconUrl(), event.getRole().getGuild().getIconUrl())
                .setDescription("**The role** " + event.getRole().getName() + " **has been deleted!**")
                .setColor(Color.RED);

        logChannel.sendMessageEmbeds(embedBuilder.build()).queue();
    }

    public void handle(RoleUpdateNameEvent event) {
        CGuild cGuild = this.guildConfigManager.addOrGet(event.getGuild());
        if (!cGuild.getSettings().isLoggingEnabled()) return;

        TextChannel logChannel = CUtils.findLogChannel(event.getGuild());
        if (logChannel == null) return;

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setAuthor("Role Name Changed:", event.getRole().getGuild().getIconUrl(), event.getRole().getGuild().getIconUrl())
                .setDescription("**The role** " + event.getRole().getAsMention() + " **has had its name changed from** " + event.getOldName() + " **to** " + event.getNewName())
                .setColor(Color.YELLOW);

        logChannel.sendMessageEmbeds(embedBuilder.build()).queue();
    }

    public void handle(GuildMemberRoleAddEvent event) {
        CGuild cGuild = this.guildConfigManager.addOrGet(event.getGuild());
        if (!cGuild.getSettings().isLoggingEnabled()) return;

        List<Role> roleList = event.getRoles();
        String roleString = roleList.stream().map(Role::getName).collect(Collectors.joining(", "));

        TextChannel logChannel = CUtils.findLogChannel(event.getGuild());
        if (logChannel == null) return;

        EmbedBuilder embedBuilder;
        if (roleList.size() == 1) {
            embedBuilder = new EmbedBuilder()
                    .setAuthor(event.getUser().getName(), event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl())
                    .setDescription(event.getUser().getAsMention() + " **was given the** " + roleString.toUpperCase() + " **role**")
                    .setColor(Color.orange);
        } else {
            embedBuilder = new EmbedBuilder()
                    .setAuthor(event.getUser().getName(), event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl())
                    .setDescription(event.getUser().getAsMention() + " **was given the roles:** " + roleString)
                    .setColor(Color.orange);
        }

        logChannel.sendMessageEmbeds(embedBuilder.build()).queue();
    }

    public void handle(GuildMemberRoleRemoveEvent event) {
        CGuild cGuild = this.guildConfigManager.addOrGet(event.getGuild());
        if (!cGuild.getSettings().isLoggingEnabled()) return;

        List<Role> roleList = event.getRoles();
        String roleString = roleList.stream().map(Role::getName).collect(Collectors.joining(", "));

        TextChannel logChannel = CUtils.findLogChannel(event.getGuild());
        if (logChannel == null) return;

        EmbedBuilder embedBuilder;
        if (roleList.size() == 1) {
            embedBuilder = new EmbedBuilder()
                    .setAuthor(event.getUser().getName(), event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl())
                    .setDescription("**The role** " + roleString.toUpperCase() + " **for** " + event.getUser().getAsMention() + " **has been removed!**")
                    .setColor(Color.ORANGE);
        } else
            embedBuilder = new EmbedBuilder()
                    .setAuthor(event.getUser().getName(), event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl())
                    .setDescription("**The roles** " + roleString.toUpperCase() + " **for** " + event.getUser().getAsMention() + " **has been removed!**")
                    .setColor(Color.ORANGE);

        logChannel.sendMessageEmbeds(embedBuilder.build()).queue();
    }

    public void handle(GuildBanEvent event) {
        CGuild cGuild = this.guildConfigManager.addOrGet(event.getGuild());
        if (!cGuild.getSettings().isLoggingEnabled()) return;

        TextChannel logChannel = CUtils.findLogChannel(event.getGuild());
        if (logChannel == null) return;

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setAuthor("Member Banned:", event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl())
                .setDescription(event.getUser().getAsMention() + " has been banned!")
                .setColor(Color.RED).setThumbnail(event.getUser().getAvatarUrl());

        logChannel.sendMessageEmbeds(embedBuilder.build()).queue();
    }

    public void handle(GuildUnbanEvent event) {
        CGuild cGuild = this.guildConfigManager.addOrGet(event.getGuild());
        if (!cGuild.getSettings().isLoggingEnabled()) return;

        TextChannel logChannel = CUtils.findLogChannel(event.getGuild());
        if (logChannel == null) return;

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setAuthor("Member Unbanned:", event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl())
                .setDescription(event.getUser().getAsMention() + " has been unbanned!")
                .setColor(Color.MAGENTA)
                .setThumbnail(event.getUser().getAvatarUrl());

        logChannel.sendMessageEmbeds(embedBuilder.build()).queue();
    }

    public void handle(MessageDeleteEvent event) {
        CGuild cGuild = this.guildConfigManager.addOrGet(event.getGuild());
        if (!cGuild.getSettings().isLoggingEnabled()) return;

        TextChannel logChannel = CUtils.findLogChannel(event.getGuild());
        if (logChannel == null) return;

        AMessage message = cGuild.getMessageHistoryManager().getMessage(event.getChannel().asTextChannel(), event.getMessageId());
        if (message == null) return;

        User user = this.ccBotCore.getJda().getUserById(message.getAuthorId());
        if (user == null) return;
        if (user.isBot()) return;

        // This prevents empty messages from being logged (most likely a video or photo was deleted)
        if (message.getMessageRawContent().isEmpty() || message.getMessageRawContent().isBlank()) return;

        EmbedBuilder embedBuilder = new EmbedBuilder().setAuthor(user.getName(), null, user.getAvatarUrl())
                .setDescription("**Message sent by** " + user.getAsMention() + " **has been deleted in** " + event.getChannel().getAsMention())
                .setColor(Color.RED)
                .addField("Message Content", message.getMessageRawContent(), false);

        logChannel.sendMessageEmbeds(embedBuilder.build()).queue();
    }

    public void handle(MessageUpdateEvent event) {
        CGuild cGuild = this.guildConfigManager.addOrGet(event.getGuild());
        if (!cGuild.getSettings().isLoggingEnabled()) return;

        TextChannel logChannel = CUtils.findLogChannel(event.getGuild());
        if (logChannel == null) return;

        AMessage message = cGuild.getMessageHistoryManager().getMessage(event.getChannel().asTextChannel(), event.getMessageId());
        if (message == null) return;

        User user = this.ccBotCore.getJda().getUserById(message.getAuthorId());
        if (user == null) return;
        if (user.isBot()) return;

        // This prevents messages that are the same from being logged (this happens when a message is pinned for some reason...)
        if (event.getMessage().getContentRaw().equals(message.getMessageRawContent())) return;

        EmbedBuilder embedBuilder = new EmbedBuilder().setAuthor(user.getName(), null, user.getAvatarUrl())
                .setDescription("**Message sent by** " + user.getAsMention() + " **has been edited in** " + event.getChannel().getAsMention())
                .setColor(Color.YELLOW)
                .addField("Old Message Content", message.getMessageRawContent(), false)
                .addField("New Message Content", event.getMessage().getContentRaw(), false);

        logChannel.sendMessageEmbeds(embedBuilder.build()).queue();
    }
}