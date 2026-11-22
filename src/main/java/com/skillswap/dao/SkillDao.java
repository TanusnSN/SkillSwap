package com.skillswap.dao;

import com.skillswap.db.Database;
import com.skillswap.model.Skill;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SkillDao {
    public Skill addSkill(Skill skill) throws SQLException {
        String sql = "INSERT INTO skills (user_id, title, category, description, type) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, skill.userId);
            stmt.setString(2, skill.title);
            stmt.setString(3, skill.category);
            stmt.setString(4, skill.description);
            stmt.setString(5, skill.type);
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) skill.id = keys.getInt(1);
            }
            return skill;
        }
    }

    public List<Skill> getAllSkills() throws SQLException {
        List<Skill> skills = new ArrayList<>();
        String sql = "SELECT * FROM skills";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                skills.add(new Skill(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getString("title"),
                    rs.getString("category"),
                    rs.getString("description"),
                    rs.getString("type")
                ));
            }
        }
        return skills;
    }
}