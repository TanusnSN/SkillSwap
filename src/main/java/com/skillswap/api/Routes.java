package com.skillswap.api;

import com.skillswap.dao.UserDao;
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
 */
public class Routes {

    /** Body sent when booking. skillId is the session id. */
    public record BookingRequest(int userId, int skillId) {}

    /** Body sent when a student hosts a new session. */
    public record TeachRequest(int userId, String name, String category, String description,
                               String sessionType, int duration, int maxParticipants) {}

    private final WalletService walletService;
    private final SessionService sessionService;
    private final MatchingService matchingService;
    private final UserDao userDao;

    public Routes(WalletService walletService, SessionService sessionService,
                  MatchingService matchingService, UserDao userDao) {
        this.walletService = walletService;
        this.sessionService = sessionService;
        this.matchingService = matchingService;
        this.userDao = userDao;
    }

    public void register(Javalin app) {
        app.get("/api/skills", this::listSkills);
        app.get("/api/users", this::listUsers);
        app.get("/api/wallet/{userId}", this::getWallet);
        app.get("/api/history/{userId}", this::getHistory);
        app.get("/api/cost", this::previewCost);
        app.get("/api/match", this::findMatch);
        app.post("/api/sessions", this::bookSession);
        app.post("/api/teach", this::teach);

        // Our custom exceptions become clean JSON errors
        app.exception(InsufficientCreditsException.class,
                (e, ctx) -> ctx.status(400).json(Map.of("error", e.getMessage())));
        app.exception(MatchNotFoundException.class,
                (e, ctx) -> ctx.status(404).json(Map.of("error", e.getMessage())));
        app.exception(IllegalArgumentException.class,
                (e, ctx) -> ctx.status(400).json(Map.of("error", e.getMessage())));
    }

    /** One session as JSON, with the field names the frontend expects. */
    private Map<String, Object> toJson(Session s) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", s.getId());
        item.put("name", s.getSkill().getName());
        item.put("category", s.getSkill().getCategory());
        item.put("teacher", s.getTeacher().getName());
        item.put("teacherId", s.getTeacher().getId());
        item.put("rating", s.getRating());
        item.put("sessionType", s.getSessionType());
        item.put("creditCost", s.calculateCost());
        item.put("duration", s.getDurationMinutes());
        item.put("description", s.getSkill().getDescription());
        return item;
    }

    // GET /api/skills?userId=1   (userId is optional: it adds "bookedByMe")
    private void listSkills(Context ctx) {
        String userParam = ctx.queryParam("userId");
        Integer userId = (userParam == null) ? null : Integer.valueOf(userParam);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Session s : sessionService.listSessions()) {
            Map<String, Object> item = toJson(s);
            item.put("spotsLeft", sessionService.spotsLeft(s));
            item.put("bookedByMe", userId != null && sessionService.hasBooked(userId, s.getId()));
            result.add(item);
        }
        ctx.json(result);
    }

    // GET /api/users
    private void listUsers(Context ctx) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (User u : userDao.findAll()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", u.getId());
            item.put("name", u.getName());
            item.put("balance", u.getBalance());
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

    // GET /api/history/{userId}
    private void getHistory(Context ctx) {
        int userId = Integer.parseInt(ctx.pathParam("userId"));
        ctx.json(walletService.getHistory(userId));
    }

    // GET /api/cost?type=ONE_ON_ONE&duration=60
    private void previewCost(Context ctx) {
        String type = ctx.queryParam("type");
        int duration = Integer.parseInt(String.valueOf(ctx.queryParam("duration")));
        ctx.json(Map.of("creditCost", sessionService.previewCost(type, duration)));
    }

    // POST /api/sessions   body: {"userId":1,"skillId":4}
    private void bookSession(Context ctx) throws InsufficientCreditsException {
        BookingRequest request = ctx.bodyAsClass(BookingRequest.class);
        int balance = sessionService.book(request.userId(), request.skillId());
        ctx.json(Map.of("balance", balance));
    }

    // POST /api/teach
    private void teach(Context ctx) {
        TeachRequest r = ctx.bodyAsClass(TeachRequest.class);
        Session created = sessionService.createSession(r.userId(), r.name(), r.category(),
                r.description(), r.sessionType(), r.duration(), r.maxParticipants());
        ctx.status(201).json(toJson(created));
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