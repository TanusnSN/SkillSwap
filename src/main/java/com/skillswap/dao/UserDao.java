package com.skillswap.dao;

import com.skillswap.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO = Data Access Object. The only class that runs SQL on the users table.
 */
public class UserDao {

    /** Returns the user, or null if the id does not exist. */
    public User findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM users");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return users;
    }

    public void updateCredits(int id, int credits) {
        String sql = "UPDATE users SET credits = ? WHERE id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, credits);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /** Turns one database row into a User object. */
    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User(rs.getInt("id"), rs.getString("name"),
                rs.getString("email"), rs.getInt("credits"));
        for (String skill : split(rs.getString("skills_offered"))) {
            user.addSkillOffered(skill);
        }
        for (String skill : split(rs.getString("skills_wanted"))) {
            user.addSkillWanted(skill);
        }
        return user;
    }

    /** "Java,SQL" -> ["Java", "SQL"] (empty text -> empty list) */
    private List<String> split(String text) {
        List<String> result = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return result;
        }
        for (String part : text.split(",")) {
            result.add(part.trim());
        }
        return result;
    }
}