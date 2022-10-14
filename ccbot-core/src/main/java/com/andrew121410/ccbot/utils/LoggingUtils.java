package com.andrew121410.ccbot.utils;

import com.andrew121410.ccbot.CCBotCore;
import com.andrew121410.ccbot.config.GuildConfigManager;
import com.andrew121410.ccbot.objects.AMessage;
import com.andrew121410.ccbot.objects.CGuild;
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

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LoggingUtils {

    private final CCBotCore ccBotCore;
    private final CUtils cUtils;

    private GuildConfigManager guildConfigManager;

    public LoggingUtils(CCBotCore ccBotCore) {
        this.ccBotCore = ccBotCore;
        this.cUtils = new CUtils();
        this.guildConfigManager = this.ccBotCore.getConfigManager().getGuildConfigManager();
    }

    public void handle(GuildMemberJoinEvent event) {
        CGuild cGuild = this.guildConfigManager.addOrGet(event.getGuild());
        if (!cGuild.getSettings().getWelcomeMessages()) return;

        if (event.getGuild().getSystemChannel() == null) return;

        EmbedBuilder embedBuilder;

        TextChannel textChannel = cUtils.findTextChannel(event.getGuild().getTextChannels(), "rules", "chat-rules", "rules-and-info");

        if (textChannel != null) {
            embedBuilder = new EmbedBuilder().setAuthor("Welcome!", event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl()).setTitle("Welcome! to the " + event.getGuild().getName()).setDescription(event.getUser().getAsMention() + " Please check the " + textChannel.getAsMention() + ", and have fun!").setColor(Color.GREEN);
        } else {
            embedBuilder = new EmbedBuilder().setAuthor("Welcome!", event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl()).setTitle("Welcome! to the " + event.getGuild().getName()).setDescription(event.getUser().getAsMention() + " Please check the rules, and have fun!").setColor(Color.GREEN);
        }

        event.getGuild().getSystemChannel().sendMessageEmbeds(embedBuilder.build()).queue();

        TextChannel textChannel2 = cUtils.findTextChannel(event.getGuild().getTextChannels(), cUtils.getLogsStringArray());

        if (textChannel2 == null) return; //Could not find log channel.

        EmbedBuilder embedBuilder1 = new EmbedBuilder().setAuthor("Member Joined:", event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl()).setDescription(event.getUser().getAsMention() + " " + event.getUser().getAsTag()).setColor(Color.GREEN).setThumbnail(event.getUser().getAvatarUrl());

        textChannel2.sendMessageEmbeds(embedBuilder1.build()).queue();
    }

    public void handle(GuildMemberRemoveEvent event) {
        CGuild cGuild = this.guildConfigManager.addOrGet(event.getGuild());
        if (!cGuild.getSettings().isLoggingEnabled()) return;

        List<String> channelList = new ArrayList<>();
        channelList.add("leaves");
        channelList.add("lefts");
        channelList.addAll(Arrays.asList(cUtils.getLogsStringArray()));
        TextChannel textChannel = cUtils.findTextChannel(event.getGuild().getTextChannels(), channelList);

        if (textChannel == null) return;

        EmbedBuilder embedBuilder = new EmbedBuilder().setAuthor("Member Left:", event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl()).setDescription(event.getUser().getAsTag() + " Has left the server \uD83D\uDE22").setColor(Color.YELLOW).setThumbnail(event.getUser().getAvatarUrl());

        textChannel.sendMessageEmbeds(embedBuilder.build()).queue();
    }

    public void handle(GuildMemberRoleAddEvent event) {
        CGuild cGuild = this.guildConfigManager.addOrGet(event.getGuild());
        if (!cGuild.getSettings().isLoggingEnabled()) return;

        List<Role> roleList = event.getRoles();
        String roleString = roleList.stream().map(Role::getName).collect(Collectors.joining(", "));

        List<String> channelList = new ArrayList<>(Arrays.asList(cUtils.getLogsStringArray()));
        TextChannel textChannel = cUtils.findTextChannel(event.getGuild().getTextChannels(), channelList);

        if (textChannel == null) return;

        EmbedBuilder embedBuilder;

        //Only 1 role.
        if (roleList.size() == 1) {
            embedBuilder = new EmbedBuilder().setAuthor(event.getUser().getAsTag(), event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl()).setDescription(event.getUser().getAsMention() + " **was given the** " + roleString.toUpperCase() + " **role**").setColor(Color.YELLOW);
        } else
            embedBuilder = new EmbedBuilder().setAuthor(event.getUser().getAsTag(), event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl()).setDescription(event.getUser().getAsMention() + " **was given the roles:** " + roleString).setColor(Color.YELLOW);

        textChannel.sendMessageEmbeds(embedBuilder.build()).queue();
    }

    public void handle(GuildMemberRoleRemoveEvent event) {
        CGuild cGuild = this.guildConfigManager.addOrGet(event.getGuild());
        if (!cGuild.getSettings().isLoggingEnabled()) return;

        List<Role> roleList = event.getRoles();
        String roleString = roleList.stream().map(Role::getName).collect(Collectors.joining(", "));

        TextChannel textChannel = cUtils.findTextChannel(event.getGuild().getTextChannels(), Arrays.asList(cUtils.getLogsStringArray()));

        if (textChannel == null) return;

        EmbedBuilder embedBuilder;

        if (roleList.size() == 1) {
            embedBuilder = new EmbedBuilder().setAuthor(event.getUser().getAsTag(), event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl()).setDescription("**The role** " + roleString.toUpperCase() + " **for** " + event.getUser().getAsMention() + " **has been removed!**").setColor(Color.ORANGE);
        } else
            embedBuilder = new EmbedBuilder().setAuthor(event.getUser().getAsTag(), event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl()).setDescription("**The roles** " + roleString.toUpperCase() + " **for** " + event.getUser().getAsMention() + " **has been removed!**").setColor(Color.ORANGE);

        textChannel.sendMessageEmbeds(embedBuilder.build()).queue();
    }

    public void handle(GuildBanEvent event) {
        CGuild cGuild = this.guildConfigManager.addOrGet(event.getGuild());
        if (!cGuild.getSettings().isLoggingEnabled()) return;

        List<String> channelList = new ArrayList<>(Arrays.asList(cUtils.getLogsStringArray()));
        TextChannel textChannel = cUtils.findTextChannel(event.getGuild().getTextChannels(), channelList);

        if (textChannel == null) return;

        EmbedBuilder embedBuilder = new EmbedBuilder().setAuthor("Member Banned:", event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl()).setDescription(event.getUser().getAsMention() + " Has been BANNED \uD83D\uDE08").setColor(Color.RED).setThumbnail(event.getUser().getAvatarUrl());

        textChannel.sendMessageEmbeds(embedBuilder.build()).queue();
    }

    public void handle(GuildUnbanEvent event) {
        CGuild cGuild = this.guildConfigManager.addOrGet(event.getGuild());
        if (!cGuild.getSettings().isLoggingEnabled()) return;

        List<String> channelList = new ArrayList<>(Arrays.asList(cUtils.getLogsStringArray()));
        TextChannel textChannel = cUtils.findTextChannel(event.getGuild().getTextChannels(), channelList);

        if (textChannel == null) return;

        EmbedBuilder embedBuilder = new EmbedBuilder().setAuthor("Member Unbanned:", event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl()).setDescription(event.getUser().getAsMention() + " " + event.getUser().getAsTag()).setColor(Color.MAGENTA).setThumbnail(event.getUser().getAvatarUrl());

        textChannel.sendMessageEmbeds(embedBuilder.build()).queue();
    }

    public void handle(MessageDeleteEvent event) {
        CGuild cGuild = this.guildConfigManager.addOrGet(event.getGuild());
        if (!cGuild.getSettings().isLoggingEnabled()) return;

        TextChannel textChannel = cUtils.findTextChannel(event.getGuild().getTextChannels(), cUtils.getLogsList());
        if (textChannel == null) return;

        AMessage message = cGuild.getMessageHistoryManager().getMessage(event.getChannel().asTextChannel(), event.getMessageId());
        if (message == null) return;

        User user = this.ccBotCore.getJda().getUserById(message.getAuthorId());
        if (user == null) return;
        if (user.isBot()) return;

        EmbedBuilder embedBuilder = new EmbedBuilder().setAuthor(user.getAsTag(), user.getEffectiveAvatarUrl())
                .setDescription("**Message sent by** " + user.getAsMention() + " **has been deleted in** " + event.getChannel().getAsMention())
                .setColor(Color.RED)
                .addField("Message Content", message.getMessageRawContent(), false);

        textChannel.sendMessageEmbeds(embedBuilder.build()).queue();
    }
}
