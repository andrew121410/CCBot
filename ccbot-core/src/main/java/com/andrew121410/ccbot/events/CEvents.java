package com.andrew121410.ccbot.events;

import com.andrew121410.ccbot.CCBotCore;
import com.andrew121410.ccbot.config.GuildConfigManager;
import com.andrew121410.ccbot.objects.CGuild;
import com.andrew121410.ccbot.objects.button.CButton;
import com.andrew121410.ccbot.objects.button.CButtonManager;
import com.andrew121410.ccbot.utils.CUtils;
import com.andrew121410.ccbot.utils.LoggingUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
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
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class CEvents {

    private final Map<String, CGuild> guildMap;

    private final LoggingUtils loggingUtils;

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

        this.loggingUtils = new LoggingUtils(this.ccBotCore);
    }

    @SubscribeEvent
    public void onReady(ReadyEvent event) {
    }

    @SubscribeEvent
    public void onGuildJoin(GuildJoinEvent event) {
        System.out.println("New Guild Join: " + event.getGuild().getName());
        this.guildConfigManager.addOrGet(event.getGuild());
    }

    @SubscribeEvent
    public void onGuildLeave(GuildLeaveEvent event) {
        System.out.println("Guild LEAVE: " + event.getGuild().getName());

        CGuild cGuild = this.guildMap.get(event.getGuild().getId());
        if (cGuild == null) return;
        cGuild.getMessageHistoryManager().delete();

        this.guildConfigManager.remove(event.getGuild().getId());
    }

    @SubscribeEvent
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        this.loggingUtils.handle(event);
    }

    @SubscribeEvent
    public void onGuildMemberLeave(GuildMemberRemoveEvent event) {
        this.loggingUtils.handle(event);
    }

    @SubscribeEvent
    public void onRoleAddEvent(GuildMemberRoleAddEvent event) {
        this.loggingUtils.handle(event);
    }

    @SubscribeEvent
    public void onRoleRemoveEvent(GuildMemberRoleRemoveEvent event) {
        this.loggingUtils.handle(event);
    }

    @SubscribeEvent
    public void onGuildBan(GuildBanEvent event) {
        this.loggingUtils.handle(event);
    }

    @SubscribeEvent
    public void onGuildUnban(GuildUnbanEvent event) {
        this.loggingUtils.handle(event);
    }

    @SubscribeEvent
    public void onButtonClickEvent(ButtonInteractionEvent event) {
        if (event.getMember() == null) return;
        if (event.getMember().getUser().isBot()) return; //No bots

        if (event.getGuild() == null) {
            throw new NullPointerException("Guild was null for some reason");
        }

        CGuild cGuild = this.guildConfigManager.addOrGet(event.getGuild());
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

    @SubscribeEvent
    private void onMessageReceivedEvent(MessageReceivedEvent event) {
        CGuild cGuild = this.guildConfigManager.addOrGet(event.getGuild());
        if (cGuild == null) return;
        cGuild.getMessageHistoryManager().saveMessage(event.getMessage());
    }

    @SubscribeEvent
    private void onMessageDeleteEvent(MessageDeleteEvent event) {
        CGuild cGuild = this.guildConfigManager.addOrGet(event.getGuild());
        this.loggingUtils.handle(event);
        cGuild.getMessageHistoryManager().deleteMessage(event.getChannel().getId(), event.getMessageId());
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
