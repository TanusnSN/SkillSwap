package com.skillswap.model;

import java.util.ArrayList;
import java.util.List;

/**
 * One teacher, many students. Cheaper per student because the cost is shared.
 * Cost rule: 1 credit per 60 minutes (rounded up), so 60 min = 1 credit.
 */
public class GroupWorkshop extends Session {

    private static final int MINUTES_PER_CREDIT = 60;

    private int maxParticipants;
    private final List<User> participants = new ArrayList<>();

    public GroupWorkshop(int id, Skill skill, User teacher, int durationMinutes, int maxParticipants) {
        super(id, skill, teacher, durationMinutes);
        this.maxParticipants = maxParticipants;
    }

    @Override
    public int calculateCost() {
        return (int) Math.ceil(getDurationMinutes() / (double) MINUTES_PER_CREDIT);
    }

    @Override
    public String getSessionType() { return "GROUP_WORKSHOP"; }

    public int getMaxParticipants() { return maxParticipants; }
    public List<User> getParticipants() { return participants; }

    /** Returns false when the workshop is already full. */
    public boolean addParticipant(User user) {
        if (participants.size() >= maxParticipants) {
            return false;
        }
        participants.add(user);
        return true;
    }
}