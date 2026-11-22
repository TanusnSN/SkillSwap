package com.skillswap.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Saves bookings (who booked which session and what it cost).
 */
public class BookingDao {

    public void insert(int sessionId, int studentId, int cost) {
        String sql = "INSERT INTO bookings (session_id, student_id, cost) VALUES (?, ?, ?)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            ps.setInt(2, studentId);
            ps.setInt(3, cost);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}