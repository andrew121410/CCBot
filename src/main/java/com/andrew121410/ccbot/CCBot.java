package com.andrew121410.ccbot;

import com.andrew121410.ccbot.commands.*;
import com.andrew121410.ccbot.commands.manager.CommandManager;
import com.andrew121410.ccbot.config.ConfigManager;
import com.andrew121410.ccbot.events.CEvents;
import com.andrew121410.ccbot.utils.CCPresence;
import com.andrew121410.ccbot.utils.SetListMap;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;

import java.util.Scanner;

public class CCBot {

    public static final String VERSION = "1.0";

    private static CCBot instance;
    private Scanner scanner;

    private JDA jda;

    private SetListMap setListMap;
    private ConfigManager configManager;
    private CommandManager commandManager;

    public static void main(String[] args) {
        new CCBot(args);
    }

    public CCBot(String[] args) {
        instance = this;
        this.setListMap = new SetListMap();
        this.configManager = new ConfigManager(this);
        this.configManager.loadConfig(); //Loads default config
        setupJDA();
        setupScanner();
    }

    @SneakyThrows
    public void setupJDA() {
        this.commandManager = new CommandManager(this);

        if (this.configManager.getMainConfig() == null) {
            System.out.println("Please add discord token to config.yml.");
            exit();
        }

        this.jda = JDABuilder.createDefault(this.configManager.getMainConfig().getToken())
                .setEventManager(new AnnotatedEventManager())
                .addEventListeners(commandManager)
                .addEventListeners(new CEvents(this))
                .build()
                .awaitReady();

        new CCPresence(this);
        setupCommands();
    }

    private void setupCommands() {
        new HelpCMD(this);
        new PurgeCMD(this);
        new KickCMD(this);
        new BanCMD(this);
        new ConfigCMD(this);
    }

    private void setupScanner() {
        this.scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String s = scanner.nextLine();
            switch (s.toLowerCase()) {
                case "stop":
                case "end":
                case "exit":
                    exit();
                    break;
                default:
                    System.out.println("Sorry don't understand: " + s);
            }
        }
    }

    public void exit() {
        this.configManager.saveAll();
        System.exit(0);
    }

    public static CCBot getInstance() {
        return instance;
    }

    public Scanner getScanner() {
        return scanner;
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
}
