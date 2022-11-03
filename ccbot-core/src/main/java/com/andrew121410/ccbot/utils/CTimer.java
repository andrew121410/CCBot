package com.andrew121410.ccbot.utils;

import com.andrew121410.ccbot.CCBotCore;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CTimer {

    private final CCBotCore ccBotCore;

    private int number;

    private ScheduledExecutorService saveService;
    private ScheduledExecutorService presenceService;

    public CTimer(CCBotCore ccBotCore) {
        this.ccBotCore = ccBotCore;
        setupSaveManager();
        setupRichPressure();
    }

    private void setupSaveManager() {
        Runnable runnable = () -> ccBotCore.getConfigManager().saveAll(true);
        this.saveService = Executors.newSingleThreadScheduledExecutor();
        this.saveService.scheduleAtFixedRate(runnable, 0, 30, TimeUnit.SECONDS);
    }

    public void setupRichPressure() {
        //Runnable runnable = () -> ccBotCore.getJda().getPresence().setPresence(onlineStatus(), Activity.of(statusType(), Objects.requireNonNull(statusMessage())));
        Runnable runnable = () -> ccBotCore.getJda().getPresence().setPresence(OnlineStatus.ONLINE, Activity.of(Activity.ActivityType.WATCHING, ccBotCore.getJda().getGuilds().size() + " servers!"));
        this.presenceService = Executors.newSingleThreadScheduledExecutor();
        this.presenceService.scheduleAtFixedRate(runnable, 0, 15, TimeUnit.SECONDS);
    }


//    private String statusMessage() {
//        if (number == 0) {
//            number++;
//            return "CCBot | //help";
//        }
//        if (number == 1) {
//            number++;
//            return this.ccBotCore.getJda().getGuilds().size() + " servers!";
//        }
//        return null;
//    }
//
//    private OnlineStatus onlineStatus() {
//        return switch (number) {
//            default -> OnlineStatus.ONLINE;
//        };
//    }
//
//    private Activity.ActivityType statusType() {
//        return switch (number) {
//            case 1 -> Activity.ActivityType.WATCHING;
//            default -> Activity.ActivityType.PLAYING;
//        };
//    }

    public ScheduledExecutorService getSaveService() {
        return saveService;
    }

    public ScheduledExecutorService getPresenceService() {
        return presenceService;
    }
}
