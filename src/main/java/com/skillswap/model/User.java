package com.skillswap.model;

import com.skillswap.exception.InsufficientCreditsException;
import com.skillswap.interfaces.Matchable;
import com.skillswap.interfaces.Transactable;

import java.util.ArrayList;
import java.util.List;

/**
 * A student. Implements two interfaces:
 *  - Matchable   -> can be matched with other students
 *  - Transactable -> has a credit wallet
 * Fields are private (encapsulation); we use getters/setters.
 */
public class User implements Matchable, Transactable {

    public static final int STARTING_CREDITS = 5;   // every new student gets 5 credits

    private int id;
    private String name;
    private String email;
    private int credits;
    private final List<String> skillsOffered = new ArrayList<>();
    private final List<String> skillsWanted = new ArrayList<>();

    /** New student: starts with the default credits. */
    public User(int id, String name, String email) {
        this.id = id;
        setName(name);
        this.email = email;
        this.credits = STARTING_CREDITS;
    }

    /** Existing student loaded from the database (credits already known). */
    public User(int id, String name, String email, int credits) {
        this(id, name, email);
        this.credits = credits;
    }

    // ----- getters and setters -----
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        this.name = name;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public void addSkillOffered(String skill) { skillsOffered.add(skill); }
    public void addSkillWanted(String skill) { skillsWanted.add(skill); }

    // ----- Matchable -----
    @Override
    public List<String> getSkillsOffered() { return skillsOffered; }

    @Override
    public List<String> getSkillsWanted() { return skillsWanted; }

    @Override
    public boolean isMatchWith(Matchable other) {
        for (String wanted : skillsWanted) {
            for (String offered : other.getSkillsOffered()) {
                if (wanted.equalsIgnoreCase(offered)) {
                    return true;
                }
            }
        }
        return false;
    }

    // ----- Transactable (wallet) -----
    @Override
    public int getBalance() { return credits; }

    @Override
    public void credit(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        credits += amount;
    }

    @Override
    public void debit(int amount) throws InsufficientCreditsException {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (amount > credits) {
            throw new InsufficientCreditsException(credits, amount);
        }
        credits -= amount;
    }
}