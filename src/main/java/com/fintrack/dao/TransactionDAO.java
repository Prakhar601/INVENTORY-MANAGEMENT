package com.fintrack.dao;

import com.fintrack.exception.DatabaseException;
import com.fintrack.model.Transaction;

import java.time.LocalDate;
import java.util.List;

/**
 * Data access interface for {@link Transaction} entities.
 */
public interface TransactionDAO extends BaseDAO<Transaction> {

    List<Transaction> findByAccountId(int accountId) throws DatabaseException;

    List<Transaction> findByUserId(int userId) throws DatabaseException;

    List<Transaction> findByDateRange(int userId, LocalDate from, LocalDate to) throws DatabaseException;

    List<Transaction> findByCategory(int userId, int categoryId) throws DatabaseException;

    double sumByType(int userId, String type, LocalDate from, LocalDate to) throws DatabaseException;

    double sumByCategory(int categoryId, LocalDate from, LocalDate to) throws DatabaseException;

    /**
     * Advanced search and filter with pagination, amount ranges, and dynamic sorting support.
     */
    List<Transaction> filterTransactions(int userId, String type, Integer categoryId, LocalDate from, LocalDate to, String keyword, Double minAmount, Double maxAmount, String sortBy, String sortOrder, int offset, int limit) throws DatabaseException;
}
