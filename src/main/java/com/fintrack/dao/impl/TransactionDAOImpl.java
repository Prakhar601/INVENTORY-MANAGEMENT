package com.fintrack.dao.impl;

import com.fintrack.config.DatabaseConfig;
import com.fintrack.dao.TransactionDAO;
import com.fintrack.exception.DatabaseException;
import com.fintrack.model.Transaction;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of {@link TransactionDAO}.
 */
public class TransactionDAOImpl implements TransactionDAO {

    @Override
    public Transaction findById(int id) throws DatabaseException {
        String sql = "SELECT * FROM transactions WHERE id = ? AND is_deleted = 0";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapRow(rs) : null;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find transaction.", e);
        }
    }

    @Override
    public List<Transaction> findAll() throws DatabaseException {
        String sql = "SELECT * FROM transactions WHERE is_deleted = 0 ORDER BY transaction_date DESC";
        List<Transaction> list = new ArrayList<>();
        try (Statement stmt = DatabaseConfig.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve all transactions.", e);
        }
        return list;
    }

    @Override
    public boolean insert(Transaction txn) throws DatabaseException {
        String sql = "INSERT INTO transactions (user_id, account_id, category_id, amount, type, description, transaction_date) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, txn.getUserId());
            ps.setInt(2, txn.getAccountId());
            if (txn.getCategoryId() > 0) ps.setInt(3, txn.getCategoryId()); else ps.setNull(3, Types.INTEGER);
            ps.setDouble(4, txn.getAmount());
            ps.setString(5, txn.getType());
            ps.setString(6, txn.getDescription());
            ps.setString(7, txn.getDate().toString());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) txn.setId(keys.getInt(1));
                }
            }
            return rows > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to insert transaction.", e);
        }
    }

    @Override
    public boolean update(Transaction txn) throws DatabaseException {
        String sql = "UPDATE transactions SET user_id = ?, account_id = ?, category_id = ?, amount = ?, "
                   + "type = ?, description = ?, transaction_date = ?, updated_at = datetime('now') WHERE id = ?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, txn.getUserId());
            ps.setInt(2, txn.getAccountId());
            if (txn.getCategoryId() > 0) ps.setInt(3, txn.getCategoryId()); else ps.setNull(3, Types.INTEGER);
            ps.setDouble(4, txn.getAmount());
            ps.setString(5, txn.getType());
            ps.setString(6, txn.getDescription());
            ps.setString(7, txn.getDate().toString());
            ps.setInt(8, txn.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update transaction.", e);
        }
    }

    @Override
    public boolean delete(int id) throws DatabaseException {
        // Soft delete based on the new schema design
        String sql = "UPDATE transactions SET is_deleted = 1, updated_at = datetime('now') WHERE id = ?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete transaction.", e);
        }
    }

    @Override
    public List<Transaction> findByAccountId(int accountId) throws DatabaseException {
        String sql = "SELECT * FROM transactions WHERE account_id = ? AND is_deleted = 0 ORDER BY transaction_date DESC";
        return queryList(sql, ps -> ps.setInt(1, accountId));
    }

    @Override
    public List<Transaction> findByUserId(int userId) throws DatabaseException {
        // We can now query directly on user_id thanks to the schema update
        String sql = "SELECT * FROM transactions WHERE user_id = ? AND is_deleted = 0 ORDER BY transaction_date DESC";
        return queryList(sql, ps -> ps.setInt(1, userId));
    }

    @Override
    public List<Transaction> findByDateRange(int userId, LocalDate from, LocalDate to)
            throws DatabaseException {
        String sql = "SELECT * FROM transactions "
                   + "WHERE user_id = ? AND transaction_date BETWEEN ? AND ? AND is_deleted = 0 ORDER BY transaction_date DESC";
        return queryList(sql, ps -> {
            ps.setInt(1, userId);
            ps.setString(2, from.toString());
            ps.setString(3, to.toString());
        });
    }

    @Override
    public List<Transaction> findByCategory(int userId, int categoryId) throws DatabaseException {
        String sql = "SELECT * FROM transactions "
                   + "WHERE user_id = ? AND category_id = ? AND is_deleted = 0 ORDER BY transaction_date DESC";
        return queryList(sql, ps -> {
            ps.setInt(1, userId);
            ps.setInt(2, categoryId);
        });
    }

    @Override
    public double sumByType(int userId, String type, LocalDate from, LocalDate to)
            throws DatabaseException {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM transactions "
                   + "WHERE user_id = ? AND type = ? AND transaction_date BETWEEN ? AND ? AND is_deleted = 0";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, type);
            ps.setString(3, from.toString());
            ps.setString(4, to.toString());
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getDouble(1) : 0.0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to sum transactions by type.", e);
        }
    }

    @Override
    public double sumByCategory(int categoryId, LocalDate from, LocalDate to)
            throws DatabaseException {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM transactions "
                   + "WHERE category_id = ? AND transaction_date BETWEEN ? AND ? AND is_deleted = 0";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            ps.setString(2, from.toString());
            ps.setString(3, to.toString());
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getDouble(1) : 0.0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to sum by category.", e);
        }
    }

    @Override
    public List<Transaction> filterTransactions(int userId, String type, Integer categoryId, LocalDate from, LocalDate to, String keyword, Double minAmount, Double maxAmount, String sortBy, String sortOrder, int offset, int limit) throws DatabaseException {
        StringBuilder sql = new StringBuilder("SELECT * FROM transactions WHERE user_id = ? AND is_deleted = 0");
        List<Object> params = new ArrayList<>();
        params.add(userId);

        if (type != null && !type.equals("All")) {
            sql.append(" AND type = ?");
            params.add(type);
        }
        if (categoryId != null && categoryId > 0) {
            sql.append(" AND category_id = ?");
            params.add(categoryId);
        }
        if (from != null) {
            sql.append(" AND transaction_date >= ?");
            params.add(from.toString());
        }
        if (to != null) {
            sql.append(" AND transaction_date <= ?");
            params.add(to.toString());
        }
        if (minAmount != null) {
            sql.append(" AND amount >= ?");
            params.add(minAmount);
        }
        if (maxAmount != null) {
            sql.append(" AND amount <= ?");
            params.add(maxAmount);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND LOWER(description) LIKE ?");
            params.add("%" + keyword.trim().toLowerCase() + "%");
        }

        // Validate sort parameters to prevent SQL injection
        String validSortBy = "transaction_date";
        if ("amount".equalsIgnoreCase(sortBy)) {
            validSortBy = "amount";
        } else if ("description".equalsIgnoreCase(sortBy)) {
            validSortBy = "description";
        }
        
        String validSortOrder = "DESC".equalsIgnoreCase(sortOrder) ? "DESC" : "ASC";

        sql.append(" ORDER BY ").append(validSortBy).append(" ").append(validSortOrder).append(", id DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        return queryList(sql.toString(), ps -> {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
        });
    }

    // ── Helpers ────────────────────────────────────────────────────────

    @FunctionalInterface
    private interface ParameterSetter {
        void set(PreparedStatement ps) throws SQLException;
    }

    private List<Transaction> queryList(String sql, ParameterSetter setter) throws DatabaseException {
        List<Transaction> list = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            setter.set(ps);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DatabaseException("Transaction query failed.", e);
        }
        return list;
    }

    private Transaction mapRow(ResultSet rs) throws SQLException {
        Transaction t = new Transaction();
        t.setId(rs.getInt("id"));
        t.setUserId(rs.getInt("user_id"));
        t.setAccountId(rs.getInt("account_id"));
        t.setCategoryId(rs.getInt("category_id"));
        t.setAmount(rs.getDouble("amount"));
        t.setType(rs.getString("type"));
        t.setDescription(rs.getString("description"));
        String date = rs.getString("transaction_date");
        if (date != null) t.setDate(LocalDate.parse(date));
        String createdAt = rs.getString("created_at");
        if (createdAt != null) t.setCreatedAt(LocalDateTime.parse(createdAt.replace(" ", "T")));
        return t;
    }
}
