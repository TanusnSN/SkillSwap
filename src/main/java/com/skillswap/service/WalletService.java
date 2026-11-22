package com.skillswap.service;

import com.skillswap.dao.UserDao;
import com.skillswap.model.User;

/**
 * Wallet logic: look up a student and their credit balance.
 */
public class WalletService {

    private final UserDao userDao;

    public WalletService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User getUser(int userId) {
        User user = userDao.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + userId);
        }
        return user;
    }
}