package com.skillswap.interfaces;

import com.skillswap.exception.InsufficientCreditsException;

/**
 * Anything that has a credit wallet: it can receive and spend credits.
 */
public interface Transactable {

    int getBalance();

    /** Add credits (for example after teaching a session). */
    void credit(int amount);

    /** Remove credits (for example when booking a session). */
    void debit(int amount) throws InsufficientCreditsException;
}