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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads and saves sessions. Builds the correct subclass
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

    /** Saves a new session (and its skill). Returns the new session id. */
    public int insert(Session session) {
        int maxParticipants = (session instanceof GroupWorkshop g) ? g.getMaxParticipants() : 1;

        try (Connection c = Database.getConnection()) {
            int skillId;
            String skillSql = "INSERT INTO skills (name, category, description) VALUES (?, ?, ?)";
            try (PreparedStatement ps = c.prepareStatement(skillSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, session.getSkill().getName());
                ps.setString(2, session.getSkill().getCategory());
                ps.setString(3, session.getSkill().getDescription());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    skillId = keys.getInt(1);
                }
            }

            String sessionSql = "INSERT INTO sessions "
                    + "(skill_id, teacher_id, type, duration, max_participants, rating) "
                    + "VALUES (?, ?, ?, ?, ?, 0)";
            try (PreparedStatement ps = c.prepareStatement(sessionSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, skillId);
                ps.setInt(2, session.getTeacher().getId());
                ps.setString(3, session.getSessionType());
                ps.setInt(4, session.getDurationMinutes());
                ps.setInt(5, maxParticipants);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    return keys.getInt(1);
                }
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