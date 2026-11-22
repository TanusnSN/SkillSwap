package com.skillswap.exception;

/**
 * Thrown when no student can be matched for a requested skill.
 */
public class MatchNotFoundException extends Exception {

    public MatchNotFoundException(String skillName) {
        super("No match found for skill: " + skillName);
    }
}