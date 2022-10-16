package com.andrew121410.ccbot.utils;

import com.andrew121410.ccbot.CCBotCore;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TiktokDownloader {

    public static final ExecutorService TIKTOK_EXECUTOR_SERVICE = Executors.newFixedThreadPool(1);

    public CCBotCore ccBotCore;

    private final File folder;

    public TiktokDownloader(CCBotCore ccBotCore) {
        this.ccBotCore = ccBotCore;

        this.folder = new File(this.ccBotCore.getConfigManager().getConfigFolder(), "tiktoks");
        if (!folder.exists()) {
            folder.mkdir();
            folder.deleteOnExit();
        }
    }

    public void download(String url, TextChannel textChannel) {
        CompletableFuture.supplyAsync(() -> {
            long time = System.currentTimeMillis() - 1;
            String fileName = time + ".mp4";
            try {
                Process process = Runtime.getRuntime().exec("sudo python3 -m tiktok_downloader --snaptik --save " + fileName + " --url " + url, null, folder);
                process.waitFor();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new File(folder, fileName);
        }, TIKTOK_EXECUTOR_SERVICE).thenAccept(file -> {
            FileUpload fileUpload = FileUpload.fromData(file);

            // Empty file...
            if (file.length() == 0) {
                if (!file.delete()) System.out.println("Failed to delete after failing " + file.getName());
                return;
            }

            textChannel.sendFiles(fileUpload).queue(message -> {
                if (!file.delete()) System.out.println("Failed to delete after posting video " + file.getName());
            }, new ErrorHandler()
                    .ignore(ErrorResponse.FILE_UPLOAD_MAX_SIZE_EXCEEDED)
                    .handle(
                            ErrorResponse.FILE_UPLOAD_MAX_SIZE_EXCEEDED,
                            (e) -> textChannel.sendMessage("Tiktok video was too big...").queue(message -> message.delete().queueAfter(10, TimeUnit.SECONDS))));

        });
    }

    public static boolean isTiktok(String url) {
        return url.contains("tiktok.com/t/");
    }
}
