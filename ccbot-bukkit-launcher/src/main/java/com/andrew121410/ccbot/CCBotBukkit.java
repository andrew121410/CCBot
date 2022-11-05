package com.andrew121410.ccbot;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Level;

public class CCBotBukkit extends JavaPlugin {

    private CCBotCore ccBotCore;

    @Override
    public void onEnable() {
        File pluginFolder = this.getDataFolder();

        // I'm not sure if this is necessary, but I'm going to do it anyway.
        if (!pluginFolder.exists()) {
            if (!pluginFolder.mkdir()) {
                this.getLogger().log(Level.SEVERE, "Failed to create plugin folder!");
            }
        }

        this.ccBotCore = new CCBotCore(pluginFolder);
    }

    @Override
    public void onDisable() {
        this.ccBotCore.exit();
    }
}
