package com.skillswap;

import com.skillswap.exception.InsufficientCreditsException;
import com.skillswap.model.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModelTest {

    private final Skill skill = new Skill(1, "Java", "Programming", "OOP basics");
    private final User teacher = new User(1, "Asha", "asha@campus.edu");

    @Test
    void costDiffersBySessionType() {
        Session oneOnOne = new OneOnOneSession(1, skill, teacher, 60);
        Session group = new GroupWorkshop(2, skill, teacher, 60, 10);

        assertEquals(2, oneOnOne.calculateCost());   // 1-on-1 costs more
        assertEquals(1, group.calculateCost());      // group costs less
    }

    @Test
    void debitReducesBalance() throws InsufficientCreditsException {
        User student = new User(2, "Ravi", "ravi@campus.edu");
        student.debit(2);
        assertEquals(3, student.getBalance());
    }

    @Test
    void debitTooMuchThrowsException() {
        User student = new User(2, "Ravi", "ravi@campus.edu");
        assertThrows(InsufficientCreditsException.class, () -> student.debit(10));
    }

    @Test
    void matchingWorksBothWays() {
        User learner = new User(3, "Meera", "meera@campus.edu");
        learner.addSkillWanted("Java");
        teacher.addSkillOffered("java");

        assertTrue(learner.isMatchWith(teacher));
    }
}