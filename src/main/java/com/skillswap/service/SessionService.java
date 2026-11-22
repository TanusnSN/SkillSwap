package com.skillswap.service;

import com.skillswap.dao.BookingDao;
import com.skillswap.dao.SessionDao;
import com.skillswap.dao.UserDao;
import com.skillswap.exception.InsufficientCreditsException;
import com.skillswap.model.GroupWorkshop;
import com.skillswap.model.OneOnOneSession;
import com.skillswap.model.Session;
import com.skillswap.model.Skill;
import com.skillswap.model.User;

import java.util.List;

/**
 * Session logic: list, create (teach) and book sessions.
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

    /** Creates the right subclass for the chosen type. */
    private Session buildSession(int id, Skill skill, User teacher, String type,
                                 int duration, int maxParticipants) {
        if ("GROUP_WORKSHOP".equals(type)) {
            return new GroupWorkshop(id, skill, teacher, duration, maxParticipants);
        }
        if ("ONE_ON_ONE".equals(type)) {
            return new OneOnOneSession(id, skill, teacher, duration);
        }
        throw new IllegalArgumentException("Unknown session type: " + type);
    }

    /** How many credits would a session of this type and length cost? */
    public int previewCost(String type, int duration) {
        return buildSession(0, null, null, type, duration, 2).calculateCost();
    }

    /** Free places left: a 1-on-1 has 1 place, a group workshop has its maximum. */
    public int spotsLeft(Session session) {
        int capacity = (session instanceof GroupWorkshop g) ? g.getMaxParticipants() : 1;
        return capacity - bookingDao.countForSession(session.getId());
    }

    public boolean hasBooked(int studentId, int sessionId) {
        return bookingDao.hasBooked(studentId, sessionId);
    }

    /** A student hosts a new session. */
    public Session createSession(int teacherId, String name, String category, String description,
                                 String type, int duration, int maxParticipants) {
        User teacher = userDao.findById(teacherId);
        if (teacher == null) {
            throw new IllegalArgumentException("User not found: " + teacherId);
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Skill name is required");
        }
        if (duration < 15 || duration > 180) {
            throw new IllegalArgumentException("Duration must be between 15 and 180 minutes");
        }
        if ("GROUP_WORKSHOP".equals(type) && (maxParticipants < 2 || maxParticipants > 50)) {
            throw new IllegalArgumentException("A group workshop needs 2 to 50 students");
        }

        String safeCategory = (category == null || category.isBlank()) ? "Other" : category.trim();
        String safeDescription = (description == null) ? "" : description.trim();
        Skill skill = new Skill(0, name.trim(), safeCategory, safeDescription);

        Session session = buildSession(0, skill, teacher, type, duration, maxParticipants);
        int newId = sessionDao.insert(session);
        return sessionDao.findById(newId);
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
        if (bookingDao.hasBooked(studentId, sessionId)) {
            throw new IllegalArgumentException("You already booked this session");
        }
        if (spotsLeft(session) <= 0) {
            throw new IllegalArgumentException(session instanceof GroupWorkshop
                    ? "This workshop is full" : "This session is already taken");
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