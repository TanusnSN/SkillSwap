package com.skillswap.service;

import com.skillswap.dao.BookingDao;
import com.skillswap.dao.SessionDao;
import com.skillswap.dao.UserDao;
import com.skillswap.exception.InsufficientCreditsException;
import com.skillswap.model.Session;
import com.skillswap.model.User;

import java.util.List;

/**
 * Session logic: list sessions and book one using credits.
 */
public class SessionService {

    private final UserDao userDao;
    private final SessionDao sessionDao;
    private final BookingDao bookingDao;

    public SessionService(UserDao userDao, SessionDao sessionDao, BookingDao bookingDao) {
        this.userDao = userDao;
        this.sessionDao = sessionDao;
        this.bookingDao = bookingDao;
    }

    public List<Session> listSessions() {
        return sessionDao.findAll();
    }

    /**
     * Student pays credits, teacher earns the same credits.
     * Returns the student's new balance.
     */
    public int book(int studentId, int sessionId) throws InsufficientCreditsException {
        User student = userDao.findById(studentId);
        if (student == null) {
            throw new IllegalArgumentException("User not found: " + studentId);
        }
        Session session = sessionDao.findById(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Session not found: " + sessionId);
        }
        User teacher = session.getTeacher();
        if (teacher.getId() == studentId) {
            throw new IllegalArgumentException("You cannot book your own session");
        }

        int cost = session.calculateCost();   // polymorphism: each session type has its own rule

        student.debit(cost);                  // throws InsufficientCreditsException if too poor
        teacher.credit(cost);

        userDao.updateCredits(student.getId(), student.getBalance());
        userDao.updateCredits(teacher.getId(), teacher.getBalance());
        bookingDao.insert(sessionId, studentId, cost);

        return student.getBalance();
    }
}