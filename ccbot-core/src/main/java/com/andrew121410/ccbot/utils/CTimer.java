package com.andrew121410.ccbot.utils;

import com.andrew121410.ccbot.CCBotCore;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CTimer {

    private final CCBotCore ccBotCore;

    private ScheduledExecutorService saveService;

    public CTimer(CCBotCore ccBotCore) {
        this.ccBotCore = ccBotCore;
        setupSaveManager();
    }

    private void setupSaveManager() {
        Runnable runnable = () -> ccBotCore.getConfigManager().saveAll(true);
        this.saveService = Executors.newSingleThreadScheduledExecutor();
        this.saveService.scheduleAtFixedRate(runnable, 0, 30, TimeUnit.SECONDS);
    }

    public ScheduledExecutorService getSaveService() {
        return saveService;
    }
}
