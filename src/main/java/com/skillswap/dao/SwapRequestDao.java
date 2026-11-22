package com.skillswap.dao;

import com.skillswap.db.Database;
import com.skillswap.model.SwapRequest;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SwapRequestDao {
    public SwapRequest createRequest(SwapRequest req) throws SQLException {
        String sql = "INSERT INTO swap_requests (sender_id, receiver_id, skill_id, status) VALUES (?, ?, ?, 'PENDING')";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, req.senderId);
            stmt.setInt(2, req.receiverId);
            stmt.setInt(3, req.skillId);
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) req.id = keys.getInt(1);
            }
            req.status = "PENDING";
            return req;
        }
    }

    public List<SwapRequest> getRequestsByUserId(int userId) throws SQLException {
        List<SwapRequest> list = new ArrayList<>();
        String sql = "SELECT * FROM swap_requests WHERE sender_id = ? OR receiver_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new SwapRequest(
                        rs.getInt("id"),
                        rs.getInt("sender_id"),
                        rs.getInt("receiver_id"),
                        rs.getInt("skill_id"),
                        rs.getString("status"),
                        rs.getString("created_at")
                    ));
                }
            }
        }
        return list;
    }

    public boolean updateStatus(int requestId, String status) throws SQLException {
        String sql = "UPDATE swap_requests SET status = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, requestId);
            return stmt.executeUpdate() > 0;
        }
    }
}