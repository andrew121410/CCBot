package com.andrew121410.ccbot.events;

import com.andrew121410.ccbot.CCBotCore;
import com.andrew121410.ccbot.config.GuildConfigManager;
import com.andrew121410.ccbot.objects.CGuild;
import com.andrew121410.ccbot.objects.button.CButton;
import com.andrew121410.ccbot.objects.button.CButtonManager;
import com.andrew121410.ccbot.utils.CUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateNSFWEvent;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class CEvents {

    private final Map<String, CGuild> guildMap;

    private final CCBotCore ccBotCore;
    private final JDA jda;
    private final GuildConfigManager guildConfigManager;
    private final CUtils cUtils;

    public CEvents(CCBotCore ccBotCore) {
        this.ccBotCore = ccBotCore;
        this.jda = this.ccBotCore.getJda();
        this.guildConfigManager = this.ccBotCore.getConfigManager().getGuildConfigManager();
        this.cUtils = new CUtils();
        this.guildMap = this.ccBotCore.getSetListMap().getGuildMap();
    }

    @SubscribeEvent
    public void onReady(ReadyEvent event) {
        for (Guild guild : event.getJDA().getGuilds()) {
            this.guildConfigManager.add(guild);
        }
    }

    @SubscribeEvent
    public void onGuildJoin(GuildJoinEvent event) {
        System.out.println("New Guild Join: " + event.getGuild().getName());
        this.guildConfigManager.add(event.getGuild());
    }

    @SubscribeEvent
    public void onGuildLeave(GuildLeaveEvent event) {
        System.out.println("Guild LEAVE: " + event.getGuild().getName());
        this.guildConfigManager.remove(event.getGuild().getId());
    }

    @SubscribeEvent
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        CGuild cGuild = this.guildConfigManager.getOrElseAdd(event.getGuild());
        if (!cGuild.getSettings().getWelcomeMessages()) {
            return;
        }
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

    @SubscribeEvent
    public void onGuildMemberLeave(GuildMemberRemoveEvent event) {
        CGuild cGuild = this.guildConfigManager.getOrElseAdd(event.getGuild());
        if (!cGuild.getSettings().getLogs()) {
            return;
        }
        List<String> channelList = new ArrayList<>();
        channelList.add("leaves");
        channelList.add("lefts");
        channelList.addAll(Arrays.asList(cUtils.getLogsStringArray()));
        TextChannel textChannel = cUtils.findTextChannel(event.getGuild().getTextChannels(), channelList);

        if (textChannel == null) return;

        EmbedBuilder embedBuilder = new EmbedBuilder().setAuthor("Member Left:", event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl()).setDescription(event.getUser().getAsTag() + " Has left the server \uD83D\uDE22").setColor(Color.YELLOW).setThumbnail(event.getUser().getAvatarUrl());

        textChannel.sendMessageEmbeds(embedBuilder.build()).queue();
    }

    @SubscribeEvent
    public void onRoleAddEvent(GuildMemberRoleAddEvent event) {
        CGuild cGuild = this.guildConfigManager.getOrElseAdd(event.getGuild());
        if (!cGuild.getSettings().getLogs()) {
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
            embedBuilder = new EmbedBuilder().setAuthor(event.getUser().getAsTag(), event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl()).setDescription(event.getUser().getAsMention() + " **was given the** " + roleString.toUpperCase() + " **role**").setColor(Color.YELLOW);
        } else
            embedBuilder = new EmbedBuilder().setAuthor(event.getUser().getAsTag(), event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl()).setDescription(event.getUser().getAsMention() + " **was given the roles:** " + roleString).setColor(Color.YELLOW);

        textChannel.sendMessageEmbeds(embedBuilder.build()).queue();
    }

    @SubscribeEvent
    public void onRoleRemoveEvent(GuildMemberRoleRemoveEvent event) {
        CGuild cGuild = this.guildConfigManager.getOrElseAdd(event.getGuild());
        if (!cGuild.getSettings().getLogs()) {
            return;
        }
        List<Role> roleList = event.getRoles();
        String roleString = roleList.stream().map(Role::getName).collect(Collectors.joining(", "));

        TextChannel textChannel = cUtils.findTextChannel(event.getGuild().getTextChannels(), Arrays.asList(cUtils.getLogsStringArray()));

        if (textChannel == null) return;

        EmbedBuilder embedBuilder;

        if (roleList.size() == 1) {
            embedBuilder = new EmbedBuilder().setAuthor(event.getUser().getAsTag(), event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl()).setDescription("**Role:** " + roleString + " **for** " + event.getUser().getAsMention() + " **has been removed!**").setColor(Color.ORANGE);
        } else
            embedBuilder = new EmbedBuilder().setAuthor(event.getUser().getAsTag(), event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl()).setDescription("**Roles:** " + roleString + " **for** " + event.getUser().getAsMention() + " **has been removed!**").setColor(Color.ORANGE);

        textChannel.sendMessageEmbeds(embedBuilder.build()).queue();
    }

    @SubscribeEvent
    public void onGuildBan(GuildBanEvent event) {
        CGuild cGuild = this.guildConfigManager.getOrElseAdd(event.getGuild());
        if (!cGuild.getSettings().getLogs()) {
            return;
        }
        List<String> channelList = new ArrayList<>(Arrays.asList(cUtils.getLogsStringArray()));
        TextChannel textChannel = cUtils.findTextChannel(event.getGuild().getTextChannels(), channelList);

        if (textChannel == null) return;

        EmbedBuilder embedBuilder = new EmbedBuilder().setAuthor("Member Banned:", event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl()).setDescription(event.getUser().getAsMention() + " Has been BANNED \uD83D\uDE08").setColor(Color.RED).setThumbnail(event.getUser().getAvatarUrl());

        textChannel.sendMessageEmbeds(embedBuilder.build()).queue();
    }

    @SubscribeEvent
    public void onGuildUnban(GuildUnbanEvent event) {
        CGuild cGuild = this.guildConfigManager.getOrElseAdd(event.getGuild());
        if (!cGuild.getSettings().getLogs()) {
            return;
        }
        List<String> channelList = new ArrayList<>(Arrays.asList(cUtils.getLogsStringArray()));
        TextChannel textChannel = cUtils.findTextChannel(event.getGuild().getTextChannels(), channelList);

        if (textChannel == null) return;

        EmbedBuilder embedBuilder = new EmbedBuilder().setAuthor("Member Unbanned:", event.getUser().getAvatarUrl(), event.getUser().getAvatarUrl()).setDescription(event.getUser().getAsMention() + " " + event.getUser().getAsTag()).setColor(Color.MAGENTA).setThumbnail(event.getUser().getAvatarUrl());

        textChannel.sendMessageEmbeds(embedBuilder.build()).queue();
    }

    @SubscribeEvent
    public void onButtonClickEvent(ButtonInteractionEvent event) {
        if (event.getMember() == null) return;
        if (event.getMember().getUser().isBot()) return; //No bots

        if (event.getGuild() == null) {
            throw new NullPointerException("Guild was null for some reason");
        }

        CGuild cGuild = this.guildConfigManager.getOrElseAdd(event.getGuild());
        String theGoldenKey = event.getChannel().asTextChannel().getId() + event.getMessageId();
        CButtonManager cButtonManager = cGuild.getButtonManager().get(theGoldenKey);
        if (cButtonManager == null) {
            event.getMessage().delete().queue();
            return;
        }

        Optional<CButton> cButton = cButtonManager.getCButtons().stream().filter(cButton1 -> Objects.equals(cButton1.getComponent().getId(), event.getComponentId())).findFirst();

        if (cButton.isPresent()) {
            //Check if the member has the right permissions to use this button
            for (Permission permission : cButton.get().getPermissions()) {
                if (!event.getMember().hasPermission(permission)) {
                    event.reply("You don't have the right permissions to do this.").queue();
                    return;
                }
            }
            cButtonManager.getOnButtonClick().accept(cButtonManager, event);
        } else {
            throw new NullPointerException("Button is not present. THIS SHOULD NOT HAPPEN");
        }
    }

    //Extra not needed

    @SubscribeEvent
    public void onChannelUpdateNSFWEvent(ChannelUpdateNSFWEvent event) {
        if (Boolean.FALSE.equals(event.getOldValue()) && event.getChannelType() == ChannelType.TEXT) {
            TextChannel textChannel = event.getJDA().getTextChannelById(event.getChannel().getId());
            if (textChannel == null) return;
            textChannel.sendMessage("NSFW channel is my favorite :wink").queue();
        }
    }

    @SubscribeEvent
    public void onPrivateTextEvent(MessageReceivedEvent event) {
        if (!(event.getChannelType() == ChannelType.PRIVATE)) return;
        if (event.getAuthor().isBot()) return;

        PrivateChannel privateChannel = event.getChannel().asPrivateChannel();

        privateChannel.sendMessage("Why are you private messaging me? you weirdo....").queue(a -> a.delete().queueAfter(10, TimeUnit.SECONDS));
    }
}
