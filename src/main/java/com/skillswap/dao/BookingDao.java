package com.skillswap.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Saves and reads bookings (who booked which session and what it cost).
 */
public class BookingDao {

    /** One line of a student's wallet history. role is LEARNED or TAUGHT. */
    public record HistoryItem(String role, String skill, String person, int cost, String date) {}

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

    /** How many students booked this session so far. */
    public int countForSession(int sessionId) {
        String sql = "SELECT COUNT(*) FROM bookings WHERE session_id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /** Did this student already book this session? */
    public boolean hasBooked(int studentId, int sessionId) {
        String sql = "SELECT COUNT(*) FROM bookings WHERE student_id = ? AND session_id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /** Sessions the user learned (paid for) and taught (earned from), newest first. */
    public List<HistoryItem> findHistory(int userId) {
        String sql =
            "SELECT b.id AS booking_id, b.cost, b.booked_at, k.name AS skill, 'LEARNED' AS role, t.name AS person "
          + "FROM bookings b JOIN sessions s ON b.session_id = s.id "
          + "JOIN skills k ON s.skill_id = k.id JOIN users t ON s.teacher_id = t.id "
          + "WHERE b.student_id = ? "
          + "UNION ALL "
          + "SELECT b.id, b.cost, b.booked_at, k.name, 'TAUGHT', u.name "
          + "FROM bookings b JOIN sessions s ON b.session_id = s.id "
          + "JOIN skills k ON s.skill_id = k.id JOIN users u ON b.student_id = u.id "
          + "WHERE s.teacher_id = ? "
          + "ORDER BY booked_at DESC, booking_id DESC";
        List<HistoryItem> items = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(new HistoryItem(rs.getString("role"), rs.getString("skill"),
                            rs.getString("person"), rs.getInt("cost"), rs.getString("booked_at")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return items;
    }
}