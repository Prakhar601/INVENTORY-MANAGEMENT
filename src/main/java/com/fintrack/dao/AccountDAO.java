package com.fintrack.dao;

import com.fintrack.exception.DatabaseException;
import com.fintrack.model.Account;
import java.util.List;

/**
 * Data access interface for {@link Account} entities.
 */
public interface AccountDAO extends BaseDAO<Account> {

    /**
     * Finds all accounts belonging to a specific user.
     */
    List<Account> findByUserId(int userId) throws DatabaseException;

    /**
     * Updates the balance of a specific account.
     */
    boolean updateBalance(int accountId, double newBalance) throws DatabaseException;
}
