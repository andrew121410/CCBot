package com.andrew121410.ccbot.downloader;

import com.andrew121410.ccbot.CCBotCore;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Downloads videos using yt-dlp (supports TikTok, Instagram Reels, and many other platforms).
 * Requires yt-dlp and ffmpeg to be installed on the host system.
 * <p>
 * Install yt-dlp: pip install yt-dlp (or download from https://github.com/yt-dlp/yt-dlp/releases)
 * Install ffmpeg: https://ffmpeg.org/download.html
 */
public class YtDlpDownloader implements IDownloader {

    private final CCBotCore ccBotCore;
    private final File downloadFolder;

    public YtDlpDownloader(CCBotCore ccBotCore, File videosFolder) {
        this.ccBotCore = ccBotCore;

        this.downloadFolder = new File(videosFolder, "yt-dlp");
        if (!downloadFolder.exists()) {
            downloadFolder.mkdir();
        }
    }

    @Override
    public CompletableFuture<File> download(String url, TextChannel textChannel) {
        return CompletableFuture.supplyAsync(() -> {
            long time = System.currentTimeMillis();
            String outputFileName = time + ".mp4";
            File outputFile = new File(downloadFolder, outputFileName);

            try {
                ProcessBuilder processBuilder = new ProcessBuilder(
                        "yt-dlp",
                        "--no-playlist",
                        "--merge-output-format", "mp4",
                        "--max-filesize", "100M",
                        "-o", outputFile.getAbsolutePath(),
                        url
                );
                processBuilder.directory(downloadFolder);
                processBuilder.redirectErrorStream(true);

                Process process = processBuilder.start();

                // Read output for debugging
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder output = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }

                boolean finished = process.waitFor(120, TimeUnit.SECONDS);
                if (!finished) {
                    process.destroyForcibly();
                    System.out.println("yt-dlp timed out for URL: " + url);
                    textChannel.sendMessage("*Download timed out.*").queue(message -> message.delete().queueAfter(10, TimeUnit.SECONDS));
                    return null;
                }

                int exitCode = process.exitValue();
                if (exitCode != 0) {
                    System.out.println("yt-dlp failed with exit code " + exitCode + " for URL: " + url);
                    System.out.println("Output: " + output);
                    return null;
                }

                if (!outputFile.exists() || outputFile.length() == 0) {
                    System.out.println("yt-dlp produced no output file for URL: " + url);
                    if (outputFile.exists()) outputFile.delete();
                    return null;
                }

                return outputFile;
            } catch (Exception e) {
                e.printStackTrace();
                textChannel.sendMessage("*Failed to download the video.*").queue(message -> message.delete().queueAfter(10, TimeUnit.SECONDS));
                if (outputFile.exists()) outputFile.delete();
                return null;
            }
        }, VideoDownloader.VIDEO_DOWNLOADER_EXECUTOR_SERVICE);
    }

    public CompletableFuture<String> update() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ProcessBuilder processBuilder = new ProcessBuilder(
                        "pip", "install", "-U", "yt-dlp[default,curl-cffi]"
                );
                processBuilder.directory(ccBotCore.getWorkingDirectory());
                processBuilder.redirectErrorStream(true);

                Process process = processBuilder.start();

                StringBuilder output = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }

                boolean finished = process.waitFor(120, TimeUnit.SECONDS);
                if (!finished) {
                    process.destroyForcibly();
                    return "yt-dlp update timed out.";
                }

                int exitCode = process.exitValue();
                if (exitCode != 0) {
                    return "yt-dlp update failed (exit code " + exitCode + "):\n" + output;
                }

                return "yt-dlp updated successfully:\n" + output;
            } catch (Exception e) {
                e.printStackTrace();
                return "yt-dlp update failed: " + e.getMessage();
            }
        }, VideoDownloader.VIDEO_DOWNLOADER_EXECUTOR_SERVICE);
    }
}

