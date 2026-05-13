package com.fintrack.dao;

import com.fintrack.exception.DatabaseException;
import com.fintrack.model.User;

/**
 * Data access interface for {@link User} entities.
 */
public interface UserDAO extends BaseDAO<User> {

    /**
     * Finds a user by their username.
     *
     * @param username the username to search for
     * @return the user, or {@code null} if not found
     * @throws DatabaseException if a database error occurs
     */
    User findByUsername(String username) throws DatabaseException;

    /**
     * Finds a user by their email address.
     *
     * @param email the email to search for
     * @return the user, or {@code null} if not found
     * @throws DatabaseException if a database error occurs
     */
    User findByEmail(String email) throws DatabaseException;

    /**
     * Checks whether a username already exists.
     *
     * @param username the username to check
     * @return {@code true} if the username is taken
     * @throws DatabaseException if a database error occurs
     */
    boolean existsByUsername(String username) throws DatabaseException;

    /**
     * Checks whether an email already exists.
     *
     * @param email the email to check
     * @return {@code true} if the email is taken
     * @throws DatabaseException if a database error occurs
     */
    boolean existsByEmail(String email) throws DatabaseException;
}
