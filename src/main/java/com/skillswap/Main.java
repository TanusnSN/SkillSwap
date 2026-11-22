package com.skillswap;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

import java.util.Map;

/**
 * Entry point of SkillSwap.
 * Starts the web server and also serves the frontend/ folder,
 * so we open http://localhost:7070 to see the website.
 */
public class Main {

    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> {
            // Serve index.html, css/ and js/ from the frontend folder
            config.staticFiles.add("frontend", Location.EXTERNAL);
        });

        // Simple test endpoint to check the server is alive
        app.get("/api/health", ctx -> ctx.json(Map.of("status", "ok")));

        app.start(7070);
    }
}