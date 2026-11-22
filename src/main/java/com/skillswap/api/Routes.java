package com.skillswap.api;

import com.skillswap.exception.InsufficientCreditsException;
import com.skillswap.exception.MatchNotFoundException;
import com.skillswap.model.Session;
import com.skillswap.model.User;
import com.skillswap.service.MatchingService;
import com.skillswap.service.SessionService;
import com.skillswap.service.WalletService;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * All REST URLs of the app. The frontend calls these.
 * The JSON field names match what frontend/js/app.js expects.
 */
public class Routes {

    /** JSON body sent by the frontend when booking. skillId is the session id. */
    public record BookingRequest(int userId, int skillId) {}

    private final WalletService walletService;
    private final SessionService sessionService;
    private final MatchingService matchingService;

    public Routes(WalletService walletService, SessionService sessionService,
                  MatchingService matchingService) {
        this.walletService = walletService;
        this.sessionService = sessionService;
        this.matchingService = matchingService;
    }

    public void register(Javalin app) {
        app.get("/api/skills", this::listSkills);
        app.get("/api/wallet/{userId}", this::getWallet);
        app.post("/api/sessions", this::bookSession);
        app.get("/api/match", this::findMatch);

        // Our custom exceptions become clean JSON errors
        app.exception(InsufficientCreditsException.class,
                (e, ctx) -> ctx.status(400).json(Map.of("error", e.getMessage())));
        app.exception(MatchNotFoundException.class,
                (e, ctx) -> ctx.status(404).json(Map.of("error", e.getMessage())));
        app.exception(IllegalArgumentException.class,
                (e, ctx) -> ctx.status(400).json(Map.of("error", e.getMessage())));
    }

    // GET /api/skills
    private void listSkills(Context ctx) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Session s : sessionService.listSessions()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", s.getId());
            item.put("name", s.getSkill().getName());
            item.put("category", s.getSkill().getCategory());
            item.put("teacher", s.getTeacher().getName());
            item.put("rating", s.getRating());
            item.put("sessionType", s.getSessionType());
            item.put("creditCost", s.calculateCost());
            item.put("duration", s.getDurationMinutes());
            item.put("description", s.getSkill().getDescription());
            result.add(item);
        }
        ctx.json(result);
    }

    // GET /api/wallet/{userId}
    private void getWallet(Context ctx) {
        int userId = Integer.parseInt(ctx.pathParam("userId"));
        User user = walletService.getUser(userId);
        ctx.json(Map.of("userId", user.getId(), "name", user.getName(),
                "balance", user.getBalance()));
    }

    // POST /api/sessions   body: {"userId":1,"skillId":4}
    private void bookSession(Context ctx) throws InsufficientCreditsException {
        BookingRequest request = ctx.bodyAsClass(BookingRequest.class);
        int balance = sessionService.book(request.userId(), request.skillId());
        ctx.json(Map.of("balance", balance));
    }

    // GET /api/match?userId=1&skill=Guitar
    private void findMatch(Context ctx) throws MatchNotFoundException {
        String skill = ctx.queryParam("skill");
        if (skill == null || skill.isBlank()) {
            throw new IllegalArgumentException("skill is required");
        }
        int userId = Integer.parseInt(String.valueOf(ctx.queryParam("userId")));
        User teacher = matchingService.findTeacher(userId, skill);
        ctx.json(Map.of("teacherId", teacher.getId(), "teacher", teacher.getName(),
                "skill", skill));
    }
}