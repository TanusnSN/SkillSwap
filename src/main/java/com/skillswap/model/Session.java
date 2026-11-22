package com.skillswap.model;

/**
 * ABSTRACT class: a teaching session.
 * We can never create a plain Session. We create OneOnOneSession or GroupWorkshop.
 * Each subclass calculates its own cost (POLYMORPHISM).
 */
public abstract class Session {

    private int id;
    private Skill skill;
    private User teacher;
    private int durationMinutes;
    private double rating;          // average rating shown on the website

    protected Session(int id, Skill skill, User teacher, int durationMinutes) {
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("Duration must be positive");
        }
        this.id = id;
        this.skill = skill;
        this.teacher = teacher;
        this.durationMinutes = durationMinutes;
    }

    /** Credits a student pays to join. Every subclass has its own rule. */
    public abstract int calculateCost();

    /** Text label such as "ONE_ON_ONE". The frontend uses this. */
    public abstract String getSessionType();

    public int getId() { return id; }
    public Skill getSkill() { return skill; }
    public User getTeacher() { return teacher; }
    public int getDurationMinutes() { return durationMinutes; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
}