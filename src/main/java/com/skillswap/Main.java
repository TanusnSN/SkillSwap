package com.skillswap;

import com.skillswap.api.Routes;
import com.skillswap.dao.BookingDao;
import com.skillswap.dao.Database;
import com.skillswap.dao.SessionDao;
import com.skillswap.dao.UserDao;
import com.skillswap.service.MatchingService;
import com.skillswap.service.SessionService;
import com.skillswap.service.WalletService;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

import java.util.Map;

/**
 * Entry point of SkillSwap.
 * Sets up the database, creates the objects, starts the web server
 * and also serves the frontend/ folder at http://localhost:7070
 */
public class Main {

    public static void main(String[] args) {
        Database.init();

        // Create the layers: dao -> service -> api
        UserDao userDao = new UserDao();
        SessionDao sessionDao = new SessionDao(userDao);
        BookingDao bookingDao = new BookingDao();

        WalletService walletService = new WalletService(userDao, bookingDao);
        SessionService sessionService = new SessionService(userDao, sessionDao, bookingDao);
        MatchingService matchingService = new MatchingService(userDao);

        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("frontend", Location.EXTERNAL);
        });

        app.get("/api/health", ctx -> ctx.json(Map.of("status", "ok")));
        new Routes(walletService, sessionService, matchingService, userDao).register(app);

        app.start(7070);
    }
}