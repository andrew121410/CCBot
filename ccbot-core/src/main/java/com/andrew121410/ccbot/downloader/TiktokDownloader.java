package com.andrew121410.ccbot.downloader;

import com.andrew121410.ccbot.CCBotCore;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

// https://github.com/krypton-byte/tiktok-downloader
public class TiktokDownloader implements IDownloader {

    private CCBotCore ccBotCore;

    private final File tiktokFolder;

    public TiktokDownloader(CCBotCore ccBotCore, File videosFolder) {
        this.ccBotCore = ccBotCore;

        this.tiktokFolder = new File(videosFolder, "tiktoks");
        if (!tiktokFolder.exists()) {
            tiktokFolder.mkdir();
            tiktokFolder.deleteOnExit();
        }
    }

    public CompletableFuture<File> download(String url, TextChannel textChannel) {
        return CompletableFuture.supplyAsync(() -> {
            SnaptikJSON[] snaptikJSONS = getSnaptikJSON(url);

            // Most likely not a tiktok video link
            if (snaptikJSONS == null) {
                return null;
            }

            // Most likely a slideshow type video
            if (snaptikJSONS.length >= 2) {
                textChannel.sendMessage("This is a slideshow type video, I can't download it unfortunately.").queue(message -> message.delete().queueAfter(10, TimeUnit.SECONDS));
                return null;
            }

            long time = System.currentTimeMillis() - 1;
            String fileName = time + ".mp4";
            try {
                Process process = Runtime.getRuntime().exec("sudo python3 -m tiktok_downloader --snaptik --save " + fileName + " --url " + url, null, tiktokFolder);
                process.waitFor();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new File(tiktokFolder, fileName);
        }, VideoDownloader.VIDEO_DOWNLOADER_EXECUTOR_SERVICE);
    }

    public SnaptikJSON[] getSnaptikJSON(String url) {
        try {
            Process process = Runtime.getRuntime().exec("sudo python3 -m tiktok_downloader --snaptik --json --url " + url, null, tiktokFolder);

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

            process.waitFor();

            String result = builder.toString();
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(result, SnaptikJSON[].class);
        } catch (Exception ignored) {
        }
        return null;
    }
}

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
class SnaptikJSON {
    private String type;
    private String url;
    private boolean watermark;
}
