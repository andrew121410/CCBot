package com.andrew121410.ccbot.utils;

import com.andrew121410.ccbot.commands.manager.AbstractCommand;
import com.andrew121410.ccbot.objects.CGuild;

import java.util.HashMap;
import java.util.Map;

public class SetListMap {

    private Map<String, AbstractCommand> commandMap;
    private Map<String, CGuild> guildMap;

    public SetListMap() {
        this.commandMap = new HashMap<>();
        this.guildMap = new HashMap<>();
    }

    public Map<String, AbstractCommand> getCommandMap() {
        return commandMap;
    }

    public Map<String, CGuild> getGuildMap() {
        return guildMap;
    }
}
