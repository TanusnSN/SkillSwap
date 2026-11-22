package com.skillswap.model;

public class User {
    public int id;
    public String name;
    public String email;
    public String password;
    public String bio;

    public User() {}

    public User(int id, String name, String email, String password, String bio) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.bio = bio;
    }
}