package com.skillswap.interfaces;

import java.util.List;

/**
 * Anything that can be matched with another profile
 * (a student who offers skills and wants to learn skills).
 */
public interface Matchable {

    List<String> getSkillsOffered();   // skills this profile can teach

    List<String> getSkillsWanted();    // skills this profile wants to learn

    /** True if the other profile offers something this profile wants. */
    boolean isMatchWith(Matchable other);
}