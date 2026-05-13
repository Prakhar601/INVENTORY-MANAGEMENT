package com.fintrack.config;

import com.fintrack.exception.DatabaseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.stream.Collectors;

/**
 * Robust Database configuration and connection management.
 */
public final class DatabaseConfig {

    private static final String DB_URL = "jdbc:sqlite:fintrack.db";
    private static final String SCHEMA_RESOURCE = "/schema.sql";

    private static Connection connection;

    private DatabaseConfig() {}

    public static synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                System.out.println("[DEBUG-DB] Opening new SQLite connection to " + DB_URL);
                connection = DriverManager.getConnection(DB_URL);
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("PRAGMA foreign_keys = ON");
                    stmt.execute("PRAGMA journal_mode = WAL");
                }
                connection.setAutoCommit(false); // Manual transaction control
                System.out.println("[DEBUG-DB] Connection opened successfully. AutoCommit=false, WAL=on.");
            }
        } catch (SQLException e) {
            System.err.println("[FATAL-DB] Failed to acquire connection!");
            e.printStackTrace(System.err);
            throw new DatabaseException("Failed to establish database connection.", e);
        }
        return connection;
    }

    public static void initialize() {
        System.out.println("[DEBUG-DB] DatabaseConfig.initialize() called.");
        Connection conn = getConnection();

        try (InputStream is = DatabaseConfig.class.getResourceAsStream(SCHEMA_RESOURCE)) {
            if (is == null) {
                System.err.println("[FATAL-DB] schema.sql NOT FOUND at " + SCHEMA_RESOURCE);
                throw new DatabaseException("Schema file not found: " + SCHEMA_RESOURCE);
            }

            String schema;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                schema = reader.lines().collect(Collectors.joining("\n"));
            }

            System.out.println("[DEBUG-DB] schema.sql loaded. Executing statements...");
            for (String statement : schema.split(";")) {
                String trimmed = statement.trim();
                if (!trimmed.isEmpty()) {
                    try (Statement stmt = conn.createStatement()) {
                        stmt.execute(trimmed);
                    } catch (SQLException ex) {
                        System.err.println("[WARN-DB] Statement execution failed: " + trimmed);
                        System.err.println("          Reason: " + ex.getMessage());
                        // We do NOT throw here to allow IF NOT EXISTS statements to fail gracefully if needed
                    }
                }
            }

            if (!hasColumn(conn, "transactions", "user_id")) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER TABLE transactions ADD COLUMN user_id INTEGER NOT NULL DEFAULT 0");
                    System.out.println("[DEBUG-DB] Migrated transactions table to add missing user_id column.");
                } catch (SQLException ex) {
                    System.err.println("[WARN-DB] Failed to migrate transactions table for user_id: " + ex.getMessage());
                }
            }

            conn.commit();
            System.out.println("[DEBUG-DB] Database schema initialization fully committed.");

        } catch (IOException e) {
            System.err.println("[FATAL-DB] IO Error reading schema.sql");
            throw new DatabaseException("Failed to read schema file.", e);
        } catch (SQLException e) {
            rollbackQuietly(conn);
            System.err.println("[FATAL-DB] SQL Error during schema commit");
            throw new DatabaseException("Failed to commit schema.", e);
        }
    }

    public static void commit() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.commit();
            }
        } catch (SQLException e) {
            System.err.println("[FATAL-DB] Commit failed!");
            throw new DatabaseException("Failed to commit transaction.", e);
        }
    }

    public static void rollback() {
        rollbackQuietly(connection);
    }

    public static void shutdown() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DEBUG-DB] Connection closed gracefully.");
            }
        } catch (SQLException e) {
            System.err.println("[WARN-DB] Error closing database connection.");
            e.printStackTrace(System.err);
        }
    }

    private static void rollbackQuietly(Connection conn) {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.rollback();
                System.out.println("[DEBUG-DB] Transaction rolled back safely.");
            }
        } catch (SQLException e) {
            System.err.println("[WARN-DB] Failed to rollback quietly.");
        }
    }

    private static boolean hasColumn(Connection conn, String tableName, String columnName) throws SQLException {
        String sql = "PRAGMA table_info(" + tableName + ")";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                if (columnName.equalsIgnoreCase(rs.getString("name"))) {
                    return true;
                }
            }
        }
        return false;
    }
}
