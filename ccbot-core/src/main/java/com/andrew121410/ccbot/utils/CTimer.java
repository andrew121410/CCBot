package com.andrew121410.ccbot.utils;

import com.andrew121410.ccbot.CCBotCore;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CTimer {

    private CCBotCore ccBotCore;

    private int number;

    private ScheduledExecutorService saveService;
    private ScheduledExecutorService presenceService;

    public CTimer(CCBotCore ccBotCore) {
        this.ccBotCore = ccBotCore;
        setupSaveManager();
        setupRichPressure();
    }

    private void setupSaveManager() {
        Runnable runnable = () -> ccBotCore.getConfigManager().saveAll();
        this.saveService = Executors.newSingleThreadScheduledExecutor();
        this.saveService.scheduleAtFixedRate(runnable, 0, 30, TimeUnit.SECONDS);
    }

    public void setupRichPressure() {
        //The Default Rate is 13
        //But the recommended rate is 15
        //But i'm just gonna do 20 seconds just to be safe.
        Runnable runnable = () -> ccBotCore.getJda().getPresence().setPresence(onlineStatus(), Activity.of(statusType(), Objects.requireNonNull(statusMessage())));
//        Runnable runnable = () -> this.main.getJda().getPresence().setPresence(OnlineStatus.ONLINE, Activity.of(Activity.ActivityType.DEFAULT, "CCBot | //help"));
        this.presenceService = Executors.newSingleThreadScheduledExecutor();
        this.presenceService.scheduleAtFixedRate(runnable, 0, 20, TimeUnit.SECONDS);
    }


    private String statusMessage() {
        if (number == 0) {
            number++;
            return "CCBot | //help";
        }
        if (number == 1) {
            number++;
            return this.ccBotCore.getJda().getGuilds().size() + " servers!";
        }
        if (number == 2) {
            number++;
            return "Zzz";
        }
        if (number == 3) {
            number++;
            return "Ok?";
        }
        if (number == 4) {
            number = 0;
            return "dead";
        }
        return null;
    }

    private OnlineStatus onlineStatus() {
        switch (number) {
            case 0:
            case 1:
                return OnlineStatus.ONLINE;
            case 2:
                return OnlineStatus.IDLE;
            case 3:
            case 4:
                return OnlineStatus.DO_NOT_DISTURB;
            default:
                return OnlineStatus.ONLINE;
        }
    }

    private Activity.ActivityType statusType() {
        switch (number) {
            case 0:
            case 1:
            case 2:
            case 4:
                return Activity.ActivityType.DEFAULT;
            case 3:
                return Activity.ActivityType.WATCHING;
            default:
                return Activity.ActivityType.DEFAULT;
        }
    }

    public ScheduledExecutorService getSaveService() {
        return saveService;
    }

    public ScheduledExecutorService getPresenceService() {
        return presenceService;
    }
}
