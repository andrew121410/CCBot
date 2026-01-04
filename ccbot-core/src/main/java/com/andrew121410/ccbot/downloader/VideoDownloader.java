package com.andrew121410.ccbot.downloader;

import com.andrew121410.ccbot.CCBotCore;
import lombok.Getter;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class VideoDownloader {

    public static final ExecutorService VIDEO_DOWNLOADER_EXECUTOR_SERVICE = Executors.newFixedThreadPool(2);

    private final CCBotCore ccBotCore;
    @Getter
    private final File videosFolder;

    private TiktokDownloader tiktokDownloader;

    public VideoDownloader(CCBotCore ccBotCore) {
        this.ccBotCore = ccBotCore;

        this.videosFolder = new File(this.ccBotCore.getWorkingDirectory(), "videos");
        if (!videosFolder.exists()) {
            videosFolder.mkdir();
        }

        this.tiktokDownloader = new TiktokDownloader(this.ccBotCore, videosFolder);
    }

    public void handle(TextChannel textChannel, String possibleUrl) {
        DPlatform dPlatform = identifyPlatform(possibleUrl);

        // Could not identify the platform
        if (dPlatform == null) return;

        IDownloader iDownloader = getDownloader(dPlatform);
        if (iDownloader == null) return;

        CompletableFuture<File> fileCompletableFuture = iDownloader.download(possibleUrl, textChannel);
        if (fileCompletableFuture == null) return;

        fileCompletableFuture.thenAccept(file -> {
            if (file == null) return;
            sendVideo(textChannel, file, true);
        });
    }

    public void sendVideo(TextChannel textChannel, File videoFile, boolean shouldTryToCompressIfTooLarge) {
        FileUpload fileUpload = FileUpload.fromData(videoFile);

        // Empty file...
        if (videoFile.length() == 0) {
            if (!videoFile.delete()) System.out.println("Failed to delete after failing " + videoFile.getName());
            return;
        }

        textChannel.sendFiles(fileUpload).queue((message -> {
            if (!videoFile.delete()) System.out.println("Failed to delete after posting video " + videoFile.getName());
        }), (exe) -> {
            if (shouldTryToCompressIfTooLarge && exe.getMessage().contains("too large")) {
                // Let's try to compress the video
                textChannel.sendMessage("*The video is too large, trying to compress it...*").queue(message -> message.delete().queueAfter(10, TimeUnit.SECONDS));
                sendVideo(textChannel, compressVideo(videoFile), false);
            } else {
                if (!videoFile.delete()) System.out.println("Failed to delete after failing " + videoFile.getName());
                textChannel.sendMessage("*Even after trying to compress the video the size was somehow still too large...*").queue(message -> message.delete().queueAfter(10, TimeUnit.SECONDS));
            }
        });
    }

    public File compressVideo(File video) {
        File compressedVideos = new File(this.videosFolder, "compressedVideos");
        if (!compressedVideos.exists()) compressedVideos.mkdir();

        try {
            Process process = Runtime.getRuntime().exec("sudo ../../discordify-new.sh 25 ../" + video.getName() + " zero-h-medium", null, compressedVideos);
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String videoWithoutExtension = video.getName().replaceFirst("[.][^.]+$", "");
        return new File(compressedVideos, videoWithoutExtension + "-25.mp4");
    }

    public IDownloader getDownloader(DPlatform dPlatform) {
        switch (dPlatform) {
            case TIKTOK:
                return this.tiktokDownloader;
        }
        return null;
    }

    public static DPlatform identifyPlatform(String url) {
//        if (url.contains("tiktok.com/t/")) {
//            return DPlatform.TIKTOK;
//        }
        return null;
    }
}
