package com.andrew121410.ccbot.downloader;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public interface IDownloader {

    CompletableFuture<File> download(String url, TextChannel textChannel);
}
