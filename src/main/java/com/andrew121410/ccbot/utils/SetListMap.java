package com.andrew121410.ccbot.utils;

import com.andrew121410.ccbot.commands.manager.ICommand;
import com.andrew121410.ccbot.objects.server.CGuild;

import java.util.HashMap;
import java.util.Map;

public class SetListMap {

    private Map<String, ICommand> commandMap;
    private Map<String, CGuild> guildMap;

    public SetListMap() {
        this.commandMap = new HashMap<>();
        this.guildMap = new HashMap<>();
    }

    public Map<String, ICommand> getCommandMap() {
        return commandMap;
    }

    public Map<String, CGuild> getGuildMap() {
        return guildMap;
    }
}
