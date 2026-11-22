package com.skillswap.dao;

import com.skillswap.model.GroupWorkshop;
import com.skillswap.model.OneOnOneSession;
import com.skillswap.model.Session;
import com.skillswap.model.Skill;
import com.skillswap.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads sessions from the database and builds the correct subclass
 * (OneOnOneSession or GroupWorkshop) for each row.
 */
public class SessionDao {

    private static final String BASE_SQL =
            "SELECT s.id, s.type, s.duration, s.max_participants, s.rating, s.teacher_id, "
          + "k.id AS skill_id, k.name, k.category, k.description "
          + "FROM sessions s JOIN skills k ON s.skill_id = k.id";

    private final UserDao userDao;

    public SessionDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public List<Session> findAll() {
        List<Session> sessions = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(BASE_SQL + " ORDER BY s.id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                sessions.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return sessions;
    }

    /** Returns the session, or null if the id does not exist. */
    public Session findById(int id) {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(BASE_SQL + " WHERE s.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Session mapRow(ResultSet rs) throws SQLException {
        Skill skill = new Skill(rs.getInt("skill_id"), rs.getString("name"),
                rs.getString("category"), rs.getString("description"));
        User teacher = userDao.findById(rs.getInt("teacher_id"));

        int id = rs.getInt("id");
        int duration = rs.getInt("duration");

        // The type column decides which subclass we create (polymorphism)
        Session session;
        if ("GROUP_WORKSHOP".equals(rs.getString("type"))) {
            session = new GroupWorkshop(id, skill, teacher, duration, rs.getInt("max_participants"));
        } else {
            session = new OneOnOneSession(id, skill, teacher, duration);
        }
        session.setRating(rs.getDouble("rating"));
        return session;
    }
}