package com.skillswap.model;

/**
 * A skill that can be taught, for example "Java OOP Basics".
 */
public class Skill {

    private int id;
    private String name;
    private String category;
    private String description;

    public Skill(int id, String name, String category, String description) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.description = description;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }

    public void setName(String name) { this.name = name; }
    public void setCategory(String category) { this.category = category; }
    public void setDescription(String description) { this.description = description; }
}