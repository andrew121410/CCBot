package com.andrew121410.ccbot;

import com.andrew121410.ccbot.commands.manager.AbstractCommand;
import com.andrew121410.ccbot.commands.manager.CommandManager;
import com.andrew121410.ccbot.config.ConfigManager;
import com.andrew121410.ccbot.events.CEvents;
import com.andrew121410.ccbot.objects.CGuild;
import com.andrew121410.ccbot.utils.CTimer;
import com.andrew121410.ccbot.utils.TiktokDownloader;
import lombok.Getter;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.HashMap;
import java.util.Map;

public class CCBotCore {

    @Getter
    private final Map<String, AbstractCommand> commandMap;
    @Getter
    private final Map<String, CGuild> guildMap;

    public static final String VERSION = "2.5";

    private static CCBotCore instance;

    private JDA jda;

    private ConfigManager configManager;
    private CommandManager commandManager;
    private CTimer cTimer;

    public CCBotCore(String folderPath) {
        instance = this;

        // Initialize the maps
        this.commandMap = new HashMap<>();
        this.guildMap = new HashMap<>();

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
                .build()
                .awaitReady();

        // Try to init the cGuilds
        for (Guild guild : this.jda.getGuilds()) {
            CGuild cGuild = this.configManager.getGuildConfigManager().addOrGet(guild);
            cGuild.init(guild); // Try to init the guild if it's not already
        }

        this.jda.addEventListener(new CEvents(this));
        this.jda.addEventListener(commandManager);

        this.cTimer = new CTimer(this);
    }

    public void exit() {
        this.getConfigManager().getMainConfig().setLastOn(String.valueOf(System.currentTimeMillis()));
        this.cTimer.getSaveService().shutdown();
        this.cTimer.getPresenceService().shutdown();
        TiktokDownloader.TIKTOK_EXECUTOR_SERVICE.shutdown();
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
