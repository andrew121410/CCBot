package com.andrew121410.ccbot;

import com.andrew121410.ccbot.commands.manager.AbstractCommand;
import com.andrew121410.ccbot.commands.manager.CommandManager;
import com.andrew121410.ccbot.config.ConfigManager;
import com.andrew121410.ccbot.config.GuildConfigManager;
import com.andrew121410.ccbot.downloader.VideoDownloader;
import com.andrew121410.ccbot.events.CEvents;
import com.andrew121410.ccbot.msp.MCServerPingerManager;
import com.andrew121410.ccbot.objects.CGuild;
import com.andrew121410.ccbot.utils.CTimer;
import lombok.Getter;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class CCBotCore {

    @Getter
    private final Map<String, AbstractCommand> commandMap;
    @Getter
    private final Map<String, CGuild> guildMap;

    public static final String VERSION = "2.5";

    private static CCBotCore instance;
    private final File workingDirectory;

    private JDA jda;

    private ConfigManager configManager;
    private CommandManager commandManager;
    private CTimer cTimer;

    @Getter
    private VideoDownloader videoDownloader;
    private MCServerPingerManager mcServerPingerManager;

    @Getter
    private long uptime;

    public CCBotCore(File workingDirectory) {
        instance = this;
        this.workingDirectory = workingDirectory;

        // Initialize the maps
        this.commandMap = new HashMap<>();
        this.guildMap = new HashMap<>();

        this.configManager = new ConfigManager(this);
        this.configManager.loadAll();

        setupJDA();
    }

    @SneakyThrows
    public void setupJDA() {
        if (this.configManager.getMainConfig() == null) {
            System.out.println("Please add discord token to config.yml.");
            System.exit(0);
            return;
        }

        this.jda = JDABuilder.createDefault(this.configManager.getMainConfig().getToken())
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES, GatewayIntent.MESSAGE_CONTENT)
                .setChunkingFilter(ChunkingFilter.ALL)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableCache(CacheFlag.ACTIVITY)
                .enableCache(CacheFlag.ROLE_TAGS)
                .setEventManager(new AnnotatedEventManager())
                .setStatus(OnlineStatus.ONLINE)
                .build()
                .awaitReady();

        // Try to init the cGuilds
        for (Guild guild : this.jda.getGuilds()) {
            CGuild cGuild = this.configManager.getGuildConfigManager().addOrGet(guild);
            cGuild.init(guild); // Try to init the guild if it's not already
        }

        // Other stuff
        this.cTimer = new CTimer(this);
        this.mcServerPingerManager = new MCServerPingerManager(this);

        // Register the events
        this.jda.addEventListener(new CEvents(this));

        // Register all the commands (should be the last thing we do)
        this.commandManager = new CommandManager(this);
        this.jda.addEventListener(commandManager);

        // Video downloader
        this.videoDownloader = new VideoDownloader(this);

        // Set the uptime
        this.uptime = System.currentTimeMillis();
    }

    public void exit() {
        this.getConfigManager().getMainConfig().setLastOn(String.valueOf(System.currentTimeMillis()));

        // Shutdown executors
        this.cTimer.getSaveService().shutdown();
        VideoDownloader.VIDEO_DOWNLOADER_EXECUTOR_SERVICE.shutdown();
        MCServerPingerManager.SCHEDULED_EXECUTOR_SERVICE.shutdown();

        // Save all the configs
        this.configManager.saveAll(false);

        // Shutdown the bot
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

    public GuildConfigManager getGuildConfigManager() {
        return this.configManager.getGuildConfigManager();
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public CTimer getCTimer() {
        return cTimer;
    }

    public File getWorkingDirectory() {
        return this.workingDirectory;
    }

    public MCServerPingerManager getMcServerPingerManager() {
        return mcServerPingerManager;
    }
}
