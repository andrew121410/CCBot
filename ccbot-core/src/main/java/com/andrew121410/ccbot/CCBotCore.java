package com.andrew121410.ccbot;

import com.andrew121410.ccbot.commands.manager.CommandManager;
import com.andrew121410.ccbot.config.ConfigManager;
import com.andrew121410.ccbot.events.CEvents;
import com.andrew121410.ccbot.utils.CTimer;
import com.andrew121410.ccbot.utils.MessageHistoryManager;
import com.andrew121410.ccbot.utils.SetListMap;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class CCBotCore {

    public static final String VERSION = "2.3";

    private static CCBotCore instance;

    private JDA jda;

    private SetListMap setListMap;
    private ConfigManager configManager;
    private CommandManager commandManager;
    private CTimer cTimer;
    private MessageHistoryManager messageHistoryManager;

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
            System.exit(0);
            return;
        }

        this.commandManager = new CommandManager(this);

        this.jda = JDABuilder.createDefault(this.configManager.getMainConfig().getToken())
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES, GatewayIntent.MESSAGE_CONTENT)
                .setEventManager(new AnnotatedEventManager())
                .addEventListeners(commandManager)
                .addEventListeners(new CEvents(this))
                .build()
                .awaitReady();

        this.cTimer = new CTimer(this);
        this.messageHistoryManager = new MessageHistoryManager(this);
        this.messageHistoryManager.cacheEverythingMissing();
    }

    public void exit() {
        this.getConfigManager().getMainConfig().setLastOn(String.valueOf(System.currentTimeMillis()));
        this.cTimer.getSaveService().shutdown();
        this.cTimer.getPresenceService().shutdown();
        this.configManager.saveAll(false);
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

    public MessageHistoryManager getMessageHistoryManager() {
        return messageHistoryManager;
    }
}
