package com.andrew121410.ccbot.events;

import com.andrew121410.ccbot.CCBot;
import com.andrew121410.ccbot.config.GuildConfigManager;
import com.andrew121410.ccbot.objects.server.CGuild;
import com.andrew121410.ccbot.utils.CUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.channel.text.update.TextChannelUpdateNSFWEvent;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CEvents {

    private Map<String, CGuild> guildMap;

    private CCBot ccBot;
    private JDA jda;
    private GuildConfigManager guildConfigManager;
    private CUtils cUtils;

    public CEvents(CCBot ccBot) {
        this.ccBot = ccBot;
        this.jda = this.ccBot.getJda();
        this.guildConfigManager = this.ccBot.getConfigManager().getGuildConfigManager();
        this.cUtils = new CUtils();
        this.guildMap = this.ccBot.getSetListMap().getGuildMap();
    }

    @SubscribeEvent
    public void onGuildJoin(GuildJoinEvent event) {
        System.out.println("New Guild Join: " + event.getGuild().getName());
        this.guildConfigManager.add(event.getGuild());
    }

    @SubscribeEvent
    public void OnGuildLeave(GuildLeaveEvent event) {
        System.out.println("Guild LEAVE: " + event.getGuild().getName());
        this.guildConfigManager.remove(event.getGuild().getId());
    }

    @SubscribeEvent
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        CGuild cGuild = this.guildConfigManager.getOrElseAdd(event.getGuild());
        if (!cGuild.getCGuildSettings().getWelcomeMessages()) {
            return;
        }
        if (event.getGuild().getSystemChannel() == null) return;

        EmbedBuilder embedBuilder;

        TextChannel textChannel = cUtils.findTextChannel(event.getGuild().getTextChannels(), "rules", "chat-rules", "rules-and-info");

        if (textChannel != null) {
            embedBuilder = new EmbedBuilder()
                    .setAuthor("Welcome!", event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl())
                    .setTitle("Welcome! to the " + event.getGuild().getName())
                    .setDescription(event.getUser().getAsMention() + " Please check the " + textChannel.getAsMention() + ", and have fun!")
                    .setColor(Color.GREEN);
        } else {
            embedBuilder = new EmbedBuilder()
                    .setAuthor("Welcome!", event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl())
                    .setTitle("Welcome! to the " + event.getGuild().getName())
                    .setDescription(event.getUser().getAsMention() + " Please check the rules, and have fun!")
                    .setColor(Color.GREEN);
        }

        event.getGuild().getSystemChannel().sendMessage(embedBuilder.build()).queue();

        TextChannel textChannel2 = cUtils.findTextChannel(event.getGuild().getTextChannels(), cUtils.getLogsStringArray());

        EmbedBuilder embedBuilder1 = new EmbedBuilder()
                .setAuthor("Member Joined:", event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl())
                .setDescription(event.getUser().getAsMention() + " " + event.getUser().getAsTag())
                .setColor(Color.GREEN)
                .setThumbnail(event.getUser().getAvatarUrl());

        textChannel2.sendMessage(embedBuilder1.build()).queue();
    }

    @SubscribeEvent
    public void onGuildMemberLeave(GuildMemberRemoveEvent event) {
        CGuild cGuild = this.guildConfigManager.getOrElseAdd(event.getGuild());
        if (!cGuild.getCGuildSettings().getLogs()) {
            return;
        }
        List<String> channelList = new ArrayList<>();
        channelList.add("leaves");
        channelList.add("lefts");
        channelList.addAll(Arrays.asList(cUtils.getLogsStringArray()));
        TextChannel textChannel = cUtils.findTextChannel(event.getGuild().getTextChannels(), channelList);

        if (textChannel == null) return;

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setAuthor("Member Left:", event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl())
                .setDescription(event.getUser().getAsTag() + " Has left the server \uD83D\uDE22")
                .setColor(Color.YELLOW)
                .setThumbnail(event.getUser().getAvatarUrl());

        textChannel.sendMessage(embedBuilder.build()).queue();
    }

    @SubscribeEvent
    public void onRoleAddEvent(GuildMemberRoleAddEvent event) {
        CGuild cGuild = this.guildConfigManager.getOrElseAdd(event.getGuild());
        if (!cGuild.getCGuildSettings().getLogs()) {
            return;
        }
        List<Role> roleList = event.getRoles();
        String roleString = roleList.stream().map(Role::getName).collect(Collectors.joining(", "));

        List<String> channelList = new ArrayList<>(Arrays.asList(cUtils.getLogsStringArray()));
        TextChannel textChannel = cUtils.findTextChannel(event.getGuild().getTextChannels(), channelList);

        if (textChannel == null) return;

        EmbedBuilder embedBuilder;

        //Only 1 role.
        if (roleList.size() == 1) {
            embedBuilder = new EmbedBuilder()
                    .setAuthor(event.getUser().getAsTag(), event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl())
                    .setDescription(event.getUser().getAsMention() + " **was given the** " + roleString.toUpperCase() + " **role**")
                    .setColor(Color.YELLOW);
        } else embedBuilder = new EmbedBuilder()
                .setAuthor(event.getUser().getAsTag(), event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl())
                .setDescription(event.getUser().getAsMention() + " **was given the roles:** " + roleString)
                .setColor(Color.YELLOW);

        textChannel.sendMessage(embedBuilder.build()).queue();
    }

    @SubscribeEvent
    public void onRoleRemoveEvent(GuildMemberRoleRemoveEvent event) {
        CGuild cGuild = this.guildConfigManager.getOrElseAdd(event.getGuild());
        if (!cGuild.getCGuildSettings().getLogs()) {
            return;
        }
        List<Role> roleList = event.getRoles();
        String roleString = roleList.stream().map(Role::getName).collect(Collectors.joining(", "));

        List<String> channelList = new ArrayList<>(Arrays.asList(cUtils.getLogsStringArray()));
        TextChannel textChannel = cUtils.findTextChannel(event.getGuild().getTextChannels(), channelList);

        if (textChannel == null) return;

        EmbedBuilder embedBuilder;

        if (roleList.size() == 1) {
            embedBuilder = new EmbedBuilder()
                    .setAuthor(event.getUser().getAsTag(), event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl())
                    .setDescription("**Role:** " + roleString + " **for** " + event.getUser().getAsMention() + " **has been removed!**")
                    .setColor(Color.ORANGE);
        } else embedBuilder = new EmbedBuilder()
                .setAuthor(event.getUser().getAsTag(), event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl())
                .setDescription("**Roles:** " + roleString + " **for** " + event.getUser().getAsMention() + " **has been removed!**")
                .setColor(Color.ORANGE);

        textChannel.sendMessage(embedBuilder.build()).queue();
    }

    @SubscribeEvent
    public void onGuildBan(GuildBanEvent event) {
        CGuild cGuild = this.guildConfigManager.getOrElseAdd(event.getGuild());
        if (!cGuild.getCGuildSettings().getLogs()) {
            return;
        }
        List<String> channelList = new ArrayList<>(Arrays.asList(cUtils.getLogsStringArray()));
        TextChannel textChannel = cUtils.findTextChannel(event.getGuild().getTextChannels(), channelList);

        if (textChannel == null) return;

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setAuthor("Member Banned:", event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl())
                .setDescription(event.getUser().getAsMention() + " Has been BANNED \uD83D\uDE08")
                .setColor(Color.RED)
                .setThumbnail(event.getUser().getAvatarUrl());

        textChannel.sendMessage(embedBuilder.build()).queue();
    }

    @SubscribeEvent
    public void onGuildUnban(GuildUnbanEvent event) {
        CGuild cGuild = this.guildConfigManager.getOrElseAdd(event.getGuild());
        if (!cGuild.getCGuildSettings().getLogs()) {
            return;
        }
        List<String> channelList = new ArrayList<>(Arrays.asList(cUtils.getLogsStringArray()));
        TextChannel textChannel = cUtils.findTextChannel(event.getGuild().getTextChannels(), channelList);

        if (textChannel == null) return;

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setAuthor("Member Unbanned:", event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl())
                .setDescription(event.getUser().getAsMention() + " " + event.getUser().getAsTag())
                .setColor(Color.MAGENTA)
                .setThumbnail(event.getUser().getAvatarUrl());

        textChannel.sendMessage(embedBuilder.build()).queue();
    }

    //Extra not needed but oh well.

    @SubscribeEvent
    public void onPrivateMessages(PrivateMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        event.getChannel().sendMessage("Why are you private messaging me? you weirdo....").queue();
        event.getChannel().close().queue();
    }

    @SubscribeEvent
    public void onTextChannelUpdateNSFWEvent(TextChannelUpdateNSFWEvent event) {
        if (!event.getOldNSFW()) {
            event.getChannel().sendMessage("Aye NSFW channel my favorite.").queue();
        }
    }
}
