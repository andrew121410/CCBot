package com.andrew121410.ccbot.downloader;

import com.andrew121410.ccbot.CCBotCore;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
            List<SnaptikJSON> list = getSnaptikJSON(url);

            // Most likely not a tiktok video link
            if (list == null) {
                return null;
            }

            long time = System.currentTimeMillis() - 1;
            String fileName = time + ".noIdea";
            try {
                Process process = Runtime.getRuntime().exec("sudo python3.11 -m tiktok_downloader --snaptik --save " + fileName + " --url " + url, null, tiktokFolder);
                process.waitFor();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Check if it's a mp4 file
            File file = new File(tiktokFolder, fileName);
            try {
                if (isMP4File(file)) {
                    // Rename the file to the correct name
                    String newName = time + ".mp4";
                    File newFile = new File(tiktokFolder, newName);
                    if (!file.renameTo(newFile)) {
                        throw new IOException("Failed to rename file: " + file.getAbsolutePath());
                    }

                    return newFile;
                } else {
                    textChannel.sendMessage("Not a valid video?").queue(message -> message.delete().queueAfter(10, TimeUnit.SECONDS));
                    System.out.println("Not a mp4 file: " + file.getAbsolutePath());
                    if (!file.delete()) throw new IOException("Failed to delete file: " + file.getAbsolutePath());
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }, VideoDownloader.VIDEO_DOWNLOADER_EXECUTOR_SERVICE);
    }

    public List<SnaptikJSON> getSnaptikJSON(String url) {
        try {
            Process process = Runtime.getRuntime().exec("sudo python3.11 -m tiktok_downloader --snaptik --json --url " + url, null, tiktokFolder);

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

            process.waitFor();

            String result = builder.toString();
            ObjectMapper objectMapper = new ObjectMapper();

            SnaptikJSON[] snaptikJSONS = objectMapper.readValue(result, SnaptikJSON[].class);
            List<SnaptikJSON> list = new ArrayList<>(Arrays.asList(snaptikJSONS));

            // Remove all google play links lol
            list.removeIf(snaptikJSON -> snaptikJSON.getUrl().contains("play.google.com"));

            return list;
        } catch (Exception ignored) {
        }
        return null;
    }

    @SneakyThrows
    public static boolean isMP4File(File file) throws IOException {
        Process process = Runtime.getRuntime().exec("file -b --mime-type " + file.getAbsolutePath());
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = reader.readLine();
        process.waitFor();
        return line.equals("video/mp4");
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
