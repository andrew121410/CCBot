package com.andrew121410.ccbot;

import lombok.SneakyThrows;

import java.io.*;

public class CCBot extends CCBotCore {

    public static void main(String[] args) {
        new CCBot(args);
    }

    public CCBot(String[] args) {
        super(getTheFolderTheJarIsIn());
        setupScanner();
    }

    private void setupScanner() {
        try (InputStream in = System.in) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                switch (line.toLowerCase()) {
                    case "stop", "end", "exit" -> exit();
                    default -> System.out.println("Sorry don't understand: " + line);
                }
            }
        } catch (IOException x) {
            x.printStackTrace();
        }
    }

    @SneakyThrows
    private static File getTheFolderTheJarIsIn() {
        return new File(CCBotCore.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();
    }
}
