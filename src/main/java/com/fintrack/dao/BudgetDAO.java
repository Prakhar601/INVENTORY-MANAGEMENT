package com.fintrack.dao;

import com.fintrack.exception.DatabaseException;
import com.fintrack.model.Budget;
import java.util.List;

/**
 * Data access interface for {@link Budget} entities.
 */
public interface BudgetDAO extends BaseDAO<Budget> {

    List<Budget> findByUserId(int userId) throws DatabaseException;

    Budget findByUserIdAndCategoryId(int userId, int categoryId) throws DatabaseException;

    List<Budget> findActiveBudgets(int userId) throws DatabaseException;
}
