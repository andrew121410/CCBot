package com.andrew121410.ccbot.utils;

import com.andrew121410.ccbot.CCBotCore;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CTimer {

    private final CCBotCore ccBotCore;

    private ScheduledExecutorService saveService;
    @Nullable
    private ScheduledExecutorService autoRestarter = null;

    public CTimer(CCBotCore ccBotCore) {
        this.ccBotCore = ccBotCore;
        setupSaveManager();
    }

    private void setupSaveManager() {
        Runnable runnable = () -> ccBotCore.getConfigManager().saveAll(true);
        this.saveService = Executors.newSingleThreadScheduledExecutor();
        this.saveService.scheduleAtFixedRate(runnable, 0, 30, TimeUnit.SECONDS);
    }

    public void setupAutoRestarter() {
        Runnable runnable = this.ccBotCore::exit;

        this.autoRestarter = Executors.newSingleThreadScheduledExecutor();
        this.autoRestarter.schedule(runnable, 1, TimeUnit.DAYS);

        System.out.println("Auto restarter will restart the bot in 24 hours.");
    }

    public ScheduledExecutorService getSaveService() {
        return saveService;
    }

    public ScheduledExecutorService getAutoRestarter() {
        return autoRestarter;
    }
}
