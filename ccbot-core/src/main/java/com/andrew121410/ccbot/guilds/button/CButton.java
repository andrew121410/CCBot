package com.andrew121410.ccbot.guilds.button;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.buttons.Button;

import java.util.List;

@ToString
@EqualsAndHashCode
@Getter
@AllArgsConstructor
public class CButton {
    private List<Permission> permissions;
    private Button jdaButton;
}
