package com.fintrack.dao.impl;

import com.fintrack.config.DatabaseConfig;
import com.fintrack.dao.UserDAO;
import com.fintrack.exception.DatabaseException;
import com.fintrack.model.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of {@link UserDAO} with runtime execution logging.
 */
public class UserDAOImpl implements UserDAO {

    @Override
    public User findById(int id) throws DatabaseException {
        // ... 
        return null;
    }

    @Override
    public List<User> findAll() throws DatabaseException {
        return new ArrayList<>();
    }

    @Override
    public boolean insert(User user) throws DatabaseException {
        String sql = "INSERT INTO users (username, email, password_hash) VALUES (?, ?, ?)";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) user.setId(keys.getInt(1));
                }
            }
            return rows > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to insert user.", e);
        }
    }

    @Override
    public boolean update(User user) throws DatabaseException {
        return false;
    }

    @Override
    public boolean delete(int id) throws DatabaseException {
        return false;
    }

    // ── UserDAO Specific ───────────────────────────────────────────────

    @Override
    public User findByUsername(String username) throws DatabaseException {
        System.out.println("[DEBUG-DAO] UserDAOImpl.findByUsername() requested: " + username);
        String sql = "SELECT * FROM users WHERE username = ?";
        
        try {
            Connection conn = DatabaseConfig.getConnection();
            if (conn == null || conn.isClosed()) {
                System.err.println("[FATAL-DAO] Database connection is null or closed!");
                throw new DatabaseException("Connection is broken.");
            }
            
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                System.out.println("[DEBUG-DAO] Executing SQL: " + sql);
                ResultSet rs = ps.executeQuery();
                
                if (rs.next()) {
                    System.out.println("[DEBUG-DAO] Record found! Calling mapRow...");
                    User u = mapRow(rs);
                    System.out.println("[DEBUG-DAO] mapRow succeeded.");
                    return u;
                } else {
                    System.out.println("[DEBUG-DAO] No record found for: " + username);
                    return null;
                }
            }
        } catch (SQLException e) {
            System.err.println("[FATAL-DAO] SQLite Exception thrown!");
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace(System.err);
            throw new DatabaseException("Failed to find user by username.", e);
        }
    }

    @Override
    public User findByEmail(String email) throws DatabaseException {
        return null;
    }

    @Override
    public boolean existsByUsername(String username) throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to check username existence.", e);
        }
    }

    @Override
    public boolean existsByEmail(String email) throws DatabaseException {
        return false;
    }

    // ── Row Mapper ─────────────────────────────────────────────────────

    private User mapRow(ResultSet rs) throws SQLException {
        try {
            User user = new User();
            user.setId(rs.getInt("id"));
            user.setUsername(rs.getString("username"));
            user.setEmail(rs.getString("email"));
            user.setPasswordHash(rs.getString("password_hash"));
            
            // Note: We use try-catch here because if the schema mismatch happened here, it throws SQLException
            String createdAt = rs.getString("created_at");
            if (createdAt != null) {
                user.setCreatedAt(LocalDateTime.parse(createdAt.replace(" ", "T")));
            }
            return user;
        } catch (SQLException e) {
            System.err.println("[FATAL-DAO] mapRow crashed! Likely a missing column in SQLite.");
            e.printStackTrace(System.err);
            throw e;
        }
    }
}
