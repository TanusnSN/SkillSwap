package com.skillswap.api;

import com.skillswap.dao.SkillDao;
import com.skillswap.dao.SwapRequestDao;
import com.skillswap.dao.UserDao;
import com.skillswap.model.Skill;
import com.skillswap.model.SwapRequest;
import com.skillswap.model.User;
import io.javalin.config.JavalinConfig;
import java.util.Map;

public class Routes {
    public static void register(JavalinConfig config) {
        UserDao userDao = new UserDao();
        SkillDao skillDao = new SkillDao();
        SwapRequestDao swapDao = new SwapRequestDao();

        // Users
        config.routes.post("/api/users/register", ctx -> {
            User user = ctx.bodyAsClass(User.class);
            ctx.status(201).json(userDao.createUser(user));
        });

        config.routes.post("/api/users/login", ctx -> {
            User creds = ctx.bodyAsClass(User.class);
            User found = userDao.getUserByEmailAndPassword(creds.email, creds.password);
            if (found != null) ctx.json(found);
            else ctx.status(401).json(Map.of("error", "Invalid credentials"));
        });

        config.routes.get("/api/users", ctx -> ctx.json(userDao.getAllUsers()));

        // Skills
        config.routes.get("/api/skills", ctx -> ctx.json(skillDao.getAllSkills()));

        config.routes.post("/api/skills", ctx -> {
            Skill skill = ctx.bodyAsClass(Skill.class);
            ctx.status(201).json(skillDao.addSkill(skill));
        });

        // Swap Requests
        config.routes.post("/api/swaps", ctx -> {
            SwapRequest req = ctx.bodyAsClass(SwapRequest.class);
            ctx.status(201).json(swapDao.createRequest(req));
        });

        config.routes.get("/api/swaps/user/{userId}", ctx -> {
            int userId = Integer.parseInt(ctx.pathParam("userId"));
            ctx.json(swapDao.getRequestsByUserId(userId));
        });

        config.routes.put("/api/swaps/{id}/status", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            String status = ctx.bodyAsClass(SwapRequest.class).status;
            boolean updated = swapDao.updateStatus(id, status);
            if (updated) ctx.json(Map.of("message", "Status updated", "status", status));
            else ctx.status(404).json(Map.of("error", "Swap request not found"));
        });
    }
}