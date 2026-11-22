package com.skillswap.exception;

/**
 * Thrown when a student tries to spend more credits than they have.
 * It extends Exception (a "checked" exception), so Java forces us to handle it.
 */
public class InsufficientCreditsException extends Exception {

    private final int available;  // credits the student has
    private final int required;   // credits the action needs

    public InsufficientCreditsException(int available, int required) {
        super("Not enough credits. Needed " + required + " but you have " + available + ".");
        this.available = available;
        this.required = required;
    }

    public int getAvailable() { return available; }
    public int getRequired() { return required; }
}