package com.andrew121410.ccbot.utils;

import com.andrew121410.ccbot.CCBotCore;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
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

        this.folder = new File(this.ccBotCore.getWorkingDirectory(), "tiktoks");
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
            sendVideo(textChannel, file, true);
        });
    }

    public void sendVideo(TextChannel textChannel, File file, boolean shouldTryToCompressIfTooLarge) {
        FileUpload fileUpload = FileUpload.fromData(file);

        // Empty file...
        if (file.length() == 0) {
            if (!file.delete()) System.out.println("Failed to delete after failing " + file.getName());
            return;
        }

        textChannel.sendFiles(fileUpload).queue((message -> {
            if (!file.delete()) System.out.println("Failed to delete after posting video " + file.getName());
        }), (exe) -> {
            if (shouldTryToCompressIfTooLarge && exe.getMessage().contains("too large")) {
                // Let's try to compress the video
                textChannel.sendMessage("*The video is too large, trying to compress it...*").queue(message -> message.delete().queueAfter(10, TimeUnit.SECONDS));
                sendVideo(textChannel, compressVideo(file), false);
            } else {
                if (!file.delete()) System.out.println("Failed to delete after failing " + file.getName());
                textChannel.sendMessage("*Even after trying to compress the video the size was somehow still too large...*").queue(message -> message.delete().queueAfter(10, TimeUnit.SECONDS));
            }
        });
    }

    public File compressVideo(File video) {
        File compressedVideos = new File(this.folder, "compressedVideos");
        if (!compressedVideos.exists()) compressedVideos.mkdir();

        try {
            Process process = Runtime.getRuntime().exec("sudo ../../discordify-new.sh 8 ../" + video.getName() + " zero-h-medium", null, compressedVideos);
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String videoWithoutExtension = video.getName().replaceFirst("[.][^.]+$", "");
        return new File(compressedVideos, videoWithoutExtension + "-8.mp4");
    }

    public static boolean isTiktok(String url) {
        return url.contains("tiktok.com/t/");
    }
}
