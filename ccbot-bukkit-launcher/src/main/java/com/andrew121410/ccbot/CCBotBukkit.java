package com.andrew121410.ccbot;

import org.bukkit.plugin.java.JavaPlugin;

public class CCBotBukkit extends JavaPlugin {

    private CCBotCore ccBotCore;

    @Override
    public void onEnable() {
        this.ccBotCore = new CCBotCore("plugins/CCBot/");
    }

    @Override
    public void onDisable() {
        this.ccBotCore.exit();
    }
}
