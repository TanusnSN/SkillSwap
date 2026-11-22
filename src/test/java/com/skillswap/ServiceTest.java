package com.skillswap;

import com.skillswap.dao.BookingDao;
import com.skillswap.dao.Database;
import com.skillswap.dao.SessionDao;
import com.skillswap.dao.UserDao;
import com.skillswap.exception.InsufficientCreditsException;
import com.skillswap.exception.MatchNotFoundException;
import com.skillswap.model.Session;
import com.skillswap.model.User;
import com.skillswap.service.MatchingService;
import com.skillswap.service.SessionService;
import com.skillswap.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the services with a fresh temporary database for every test.
 * Seed data: user 1 = Yasha (5 credits), session 1 = Java 1-on-1 by user 2 (cost 2),
 * session 4 = French group workshop by user 5 (cost 1), session 5 = Guitar 1-on-1 (cost 2).
 */
class ServiceTest {

    private UserDao userDao;
    private SessionService sessionService;
    private MatchingService matchingService;
    private WalletService walletService;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        String file = tempDir.resolve("test.db").toString().replace('\\', '/');
        Database.setUrl("jdbc:sqlite:" + file);
        Database.init();

        userDao = new UserDao();
        SessionDao sessionDao = new SessionDao(userDao);
        BookingDao bookingDao = new BookingDao();
        sessionService = new SessionService(userDao, sessionDao, bookingDao);
        matchingService = new MatchingService(userDao);
        walletService = new WalletService(userDao, bookingDao);
    }

    @Test
    void bookingMovesCreditsFromStudentToTeacher() throws InsufficientCreditsException {
        int balance = sessionService.book(1, 1);

        assertEquals(3, balance);                           // 5 - 2
        assertEquals(3, userDao.findById(1).getBalance());
        assertEquals(7, userDao.findById(2).getBalance());  // teacher earned 2
    }

    @Test
    void bookingFailsWhenNotEnoughCredits() throws InsufficientCreditsException {
        sessionService.book(1, 1);   // 5 -> 3
        sessionService.book(1, 3);   // 3 -> 1
        assertThrows(InsufficientCreditsException.class, () -> sessionService.book(1, 5));
        assertEquals(1, userDao.findById(1).getBalance());   // unchanged after the failure
    }

    @Test
    void cannotBookYourOwnSession() {
        assertThrows(IllegalArgumentException.class, () -> sessionService.book(2, 1));
    }

    @Test
    void cannotBookTheSameSessionTwice() throws InsufficientCreditsException {
        sessionService.book(1, 4);
        assertThrows(IllegalArgumentException.class, () -> sessionService.book(1, 4));
    }

    @Test
    void oneOnOneSessionIsTakenAfterOneBooking() throws InsufficientCreditsException {
        sessionService.book(1, 1);
        assertThrows(IllegalArgumentException.class, () -> sessionService.book(3, 1));
    }

    @Test
    void groupWorkshopBecomesFullAtMaximum() throws InsufficientCreditsException {
        Session workshop = sessionService.createSession(
                2, "Test workshop", "Other", "demo", "GROUP_WORKSHOP", 60, 2);

        sessionService.book(1, workshop.getId());
        sessionService.book(3, workshop.getId());
        assertThrows(IllegalArgumentException.class, () -> sessionService.book(4, workshop.getId()));
    }

    @Test
    void sessionCostDependsOnType() {
        assertEquals(2, sessionService.previewCost("ONE_ON_ONE", 60));
        assertEquals(1, sessionService.previewCost("GROUP_WORKSHOP", 60));
    }

    @Test
    void createdSessionAppearsInList() {
        int before = sessionService.listSessions().size();
        Session created = sessionService.createSession(
                2, "Python Basics", "Programming", "intro", "ONE_ON_ONE", 60, 1);

        assertEquals(before + 1, sessionService.listSessions().size());
        assertEquals(2, created.calculateCost());
        assertEquals("Asha Nair", created.getTeacher().getName());
    }

    @Test
    void matchingFindsTeacherOrThrows() throws MatchNotFoundException {
        User teacher = matchingService.findTeacher(1, "Guitar");
        assertEquals("Sana Ali", teacher.getName());
        assertThrows(MatchNotFoundException.class, () -> matchingService.findTeacher(1, "Cooking"));
    }

    @Test
    void historyShowsLearnedAndTaught() throws InsufficientCreditsException {
        sessionService.book(1, 4);

        List<BookingDao.HistoryItem> student = walletService.getHistory(1);
        List<BookingDao.HistoryItem> teacher = walletService.getHistory(5);

        assertEquals(1, student.size());
        assertEquals("LEARNED", student.get(0).role());
        assertEquals("Spoken French", student.get(0).skill());
        assertEquals(1, teacher.size());
        assertEquals("TAUGHT", teacher.get(0).role());
    }
}