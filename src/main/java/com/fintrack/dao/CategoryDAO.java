package com.fintrack.dao;

import com.fintrack.exception.DatabaseException;
import com.fintrack.model.Category;
import java.util.List;

/**
 * Data access interface for {@link Category} entities.
 */
public interface CategoryDAO extends BaseDAO<Category> {

    List<Category> findByUserId(int userId) throws DatabaseException;

    List<Category> findByUserIdAndType(int userId, String type) throws DatabaseException;
}
