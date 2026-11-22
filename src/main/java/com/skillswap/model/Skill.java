package com.skillswap.model;

public class Skill {
    public int id;
    public int userId;
    public String title;
    public String category;
    public String description;
    public String type; // "OFFER" or "WANT"

    public Skill() {}

    public Skill(int id, int userId, String title, String category, String description, String type) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.category = category;
        this.description = description;
        this.type = type;
    }
}