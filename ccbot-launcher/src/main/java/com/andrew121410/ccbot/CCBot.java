package com.andrew121410.ccbot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CCBot extends CCBotCore {

    public CCBot(String[] args) {
        super("/config/");
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
}
