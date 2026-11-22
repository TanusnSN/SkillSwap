package com.skillswap.service;

import com.skillswap.dao.UserDao;
import com.skillswap.exception.MatchNotFoundException;
import com.skillswap.model.User;

/**
 * Matching logic: find a teacher for a skill a student wants to learn.
 */
public class MatchingService {

    private final UserDao userDao;

    public MatchingService(UserDao userDao) {
        this.userDao = userDao;
    }

    /** Finds another student who offers the skill, or throws MatchNotFoundException. */
    public User findTeacher(int studentId, String skillName) throws MatchNotFoundException {
        // A temporary profile that only "wants" this skill, so we can use isMatchWith()
        User wanted = new User(studentId, "searcher", "none");
        wanted.addSkillWanted(skillName);

        for (User candidate : userDao.findAll()) {
            if (candidate.getId() != studentId && wanted.isMatchWith(candidate)) {
                return candidate;
            }
        }
        throw new MatchNotFoundException(skillName);
    }
}