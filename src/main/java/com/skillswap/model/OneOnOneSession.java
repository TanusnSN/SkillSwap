package com.skillswap.model;

/**
 * One teacher, one student.
 * Cost rule: 1 credit per 30 minutes (rounded up), so 60 min = 2 credits.
 */
public class OneOnOneSession extends Session {

    private static final int MINUTES_PER_CREDIT = 30;

    public OneOnOneSession(int id, Skill skill, User teacher, int durationMinutes) {
        super(id, skill, teacher, durationMinutes);
    }

    @Override
    public int calculateCost() {
        return (int) Math.ceil(getDurationMinutes() / (double) MINUTES_PER_CREDIT);
    }

    @Override
    public String getSessionType() { return "ONE_ON_ONE"; }
}