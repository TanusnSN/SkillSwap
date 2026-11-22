package com.skillswap.service;

import com.skillswap.dao.BookingDao;
import com.skillswap.dao.UserDao;
import com.skillswap.model.User;

import java.util.List;

/**
 * Wallet logic: look up a student, their balance and their history.
 */
public class WalletService {

    private final UserDao userDao;
    private final BookingDao bookingDao;

    public WalletService(UserDao userDao, BookingDao bookingDao) {
        this.userDao = userDao;
        this.bookingDao = bookingDao;
    }

    public User getUser(int userId) {
        User user = userDao.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + userId);
        }
        return user;
    }

    public List<BookingDao.HistoryItem> getHistory(int userId) {
        getUser(userId);   // throws if the user does not exist
        return bookingDao.findHistory(userId);
    }
}