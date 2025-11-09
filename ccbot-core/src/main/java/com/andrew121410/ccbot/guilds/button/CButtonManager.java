package com.andrew121410.ccbot.guilds.button;

import lombok.Getter;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.actionrow.ActionRowChildComponent;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@Getter
public class CButtonManager {

    private List<CButton> cButtons;
    private BiConsumer<CButtonManager, ButtonInteractionEvent> onButtonClick;
    private boolean deleteOnShutdown;

    public CButtonManager(Message message, List<CButton> cButtons, BiConsumer<CButtonManager, ButtonInteractionEvent> onButtonClick, boolean deleteOnShutdown) {
        this.cButtons = cButtons;
        this.onButtonClick = onButtonClick;
        this.deleteOnShutdown = deleteOnShutdown;

        List<ActionRowChildComponent> components = this.cButtons.stream().map(CButton::getJdaButton).collect(Collectors.toList());
        ActionRow actionRow = ActionRow.of(components);
        message.editMessageComponents(actionRow).queue();
    }
}
