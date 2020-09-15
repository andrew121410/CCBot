package com.andrew121410.ccbot.objects.server;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class CReaction {

    private Message message;
    private List<Permission> permissions;
    private List<String> emojis;

    private BiConsumer<CReaction, MessageReactionAddEvent> biConsumer;
    private BiFunction<CReaction, MessageReactionAddEvent, EmbedBuilder> biFunction;
    private boolean deleteOnShutdown;

    public CReaction(Message message, List<Permission> permissions, List<String> emojis, BiConsumer<CReaction, MessageReactionAddEvent> biConsumer, BiFunction<CReaction, MessageReactionAddEvent, EmbedBuilder> biFunction, boolean deleteOnShutdown) {
        this.message = message;
        this.permissions = permissions;
        this.emojis = emojis;
        this.biConsumer = biConsumer;
        this.biFunction = biFunction;
        this.deleteOnShutdown = deleteOnShutdown;

        for (String emoji : this.getEmojis()) {
            message.addReaction(emoji).queue();
        }
    }

    public Message getMessage() {
        return message;
    }

    public List<Permission> getPermissions() {
        return permissions;
    }

    public List<String> getEmojis() {
        return emojis;
    }

    public BiConsumer<CReaction, MessageReactionAddEvent> getBiConsumer() {
        return biConsumer;
    }

    public BiFunction<CReaction, MessageReactionAddEvent, EmbedBuilder> getBiFunction() {
        return biFunction;
    }

    public boolean isDeleteOnShutdown() {
        return deleteOnShutdown;
    }
}
