package com.skillswap.dao;

import com.skillswap.db.Database;
import com.skillswap.model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDao {
    public User createUser(User user) throws SQLException {
        String sql = "INSERT INTO users (name, email, password, bio) VALUES (?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.name);
            stmt.setString(2, user.email);
            stmt.setString(3, user.password);
            stmt.setString(4, user.bio);
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) user.id = keys.getInt(1);
            }
            return user;
        }
    }

    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, name, email, bio FROM users";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(new User(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    null,
                    rs.getString("bio")
                ));
            }
        }
        return users;
    }

    public User getUserByEmailAndPassword(String email, String password) throws SQLException {
        String sql = "SELECT id, name, email, bio FROM users WHERE email = ? AND password = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, password);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        null,
                        rs.getString("bio")
                    );
                }
            }
        }
        return null;
    }
}