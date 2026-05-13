package com.fintrack.dao;

import com.fintrack.exception.DatabaseException;
import java.util.List;

/**
 * Generic Data Access Object interface defining standard CRUD operations.
 * <p>
 * All entity-specific DAO interfaces extend this to inherit basic
 * operations, then add domain-specific query methods.
 * </p>
 *
 * @param <T> the entity type this DAO manages
 */
public interface BaseDAO<T> {

    /**
     * Finds an entity by its primary key.
     *
     * @param id the primary key
     * @return the entity, or {@code null} if not found
     * @throws DatabaseException if a database error occurs
     */
    T findById(int id) throws DatabaseException;

    /**
     * Retrieves all entities of this type.
     *
     * @return list of all entities (may be empty, never null)
     * @throws DatabaseException if a database error occurs
     */
    List<T> findAll() throws DatabaseException;

    /**
     * Inserts a new entity into the database.
     *
     * @param entity the entity to insert
     * @return {@code true} if the insertion was successful
     * @throws DatabaseException if a database error occurs
     */
    boolean insert(T entity) throws DatabaseException;

    /**
     * Updates an existing entity in the database.
     *
     * @param entity the entity with updated fields
     * @return {@code true} if the update affected at least one row
     * @throws DatabaseException if a database error occurs
     */
    boolean update(T entity) throws DatabaseException;

    /**
     * Deletes an entity by its primary key.
     *
     * @param id the primary key of the entity to delete
     * @return {@code true} if the deletion affected at least one row
     * @throws DatabaseException if a database error occurs
     */
    boolean delete(int id) throws DatabaseException;
}
