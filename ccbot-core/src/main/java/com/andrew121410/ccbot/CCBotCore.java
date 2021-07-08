package com.andrew121410.ccbot;

import com.andrew121410.ccbot.commands.*;
import com.andrew121410.ccbot.commands.manager.CommandManager;
import com.andrew121410.ccbot.config.ConfigManager;
import com.andrew121410.ccbot.events.CEvents;
import com.andrew121410.ccbot.objects.CGuild;
import com.andrew121410.ccbot.utils.CTimer;
import com.andrew121410.ccbot.utils.SetListMap;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

public class CCBotCore {

    public static final String VERSION = "2.0";

    private static CCBotCore instance;

    private JDA jda;

    private SetListMap setListMap;
    private ConfigManager configManager;
    private CommandManager commandManager;
    private CTimer cTimer;

    public CCBotCore(String folderPath) {
        instance = this;
        this.setListMap = new SetListMap();
        this.configManager = new ConfigManager(this, folderPath);
        this.configManager.loadAll(); //Loads default config
        setupJDA();
    }

    @SneakyThrows
    public void setupJDA() {
        if (this.configManager.getMainConfig() == null) {
            System.out.println("Please add discord token to config.yml.");
            exit();
        }

        this.commandManager = new CommandManager(this);

        this.jda = JDABuilder.createDefault(this.configManager.getMainConfig().getToken())
                .setEventManager(new AnnotatedEventManager())
                .addEventListeners(commandManager)
                .addEventListeners(new CEvents(this))
                .build()
                .awaitReady();

        this.cTimer = new CTimer(this);
        setupCommands();
    }

    private void setupCommands() {
        new HelpCMD(this);
        new PurgeCMD(this);
        new KickCMD(this);
        new BanCMD(this);
        new ConfigCMD(this);
        new SuperAdminCMD(this);
    }

    public void exit() {
        //Reactions
        for (Map.Entry<String, CGuild> entry : this.setListMap.getGuildMap().entrySet()) {
            entry.getValue().getCReactionMap().entrySet().stream().filter((a -> a.getValue().isDeleteOnShutdown())).forEach(b -> b.getValue().getMessage().delete().queue());
        }

        this.cTimer.getSaveService().shutdown();
        this.cTimer.getPresenceService().shutdown();
        this.configManager.saveAll();
        System.out.println("Exited Successfully!");
        this.jda.shutdown();
        System.exit(0);
    }

    public static CCBotCore getInstance() {
        return instance;
    }

    public JDA getJda() {
        return jda;
    }

    public SetListMap getSetListMap() {
        return setListMap;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public CTimer getCTimer() {
        return cTimer;
    }
}
