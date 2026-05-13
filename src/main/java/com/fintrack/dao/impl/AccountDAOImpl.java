package com.fintrack.dao.impl;

import com.fintrack.config.DatabaseConfig;
import com.fintrack.dao.AccountDAO;
import com.fintrack.exception.DatabaseException;
import com.fintrack.model.Account;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of {@link AccountDAO}.
 */
public class AccountDAOImpl implements AccountDAO {

    @Override
    public Account findById(int id) throws DatabaseException {
        String sql = "SELECT * FROM accounts WHERE id = ?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapRow(rs) : null;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find account by id.", e);
        }
    }

    @Override
    public List<Account> findAll() throws DatabaseException {
        String sql = "SELECT * FROM accounts ORDER BY name";
        List<Account> accounts = new ArrayList<>();
        try (Statement stmt = DatabaseConfig.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                accounts.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve all accounts.", e);
        }
        return accounts;
    }

    @Override
    public boolean insert(Account account) throws DatabaseException {
        String sql = "INSERT INTO accounts (user_id, name, type, balance) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, account.getUserId());
            ps.setString(2, account.getName());
            ps.setString(3, account.getType());
            ps.setDouble(4, account.getBalance());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) account.setId(keys.getInt(1));
                }
            }
            return rows > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to insert account.", e);
        }
    }

    @Override
    public boolean update(Account account) throws DatabaseException {
        String sql = "UPDATE accounts SET name = ?, type = ?, balance = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, account.getName());
            ps.setString(2, account.getType());
            ps.setDouble(3, account.getBalance());
            ps.setInt(4, account.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update account.", e);
        }
    }

    @Override
    public boolean delete(int id) throws DatabaseException {
        String sql = "DELETE FROM accounts WHERE id = ?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete account.", e);
        }
    }

    @Override
    public List<Account> findByUserId(int userId) throws DatabaseException {
        String sql = "SELECT * FROM accounts WHERE user_id = ? ORDER BY name";
        List<Account> accounts = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                accounts.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find accounts by user.", e);
        }
        return accounts;
    }

    @Override
    public boolean updateBalance(int accountId, double newBalance) throws DatabaseException {
        String sql = "UPDATE accounts SET balance = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setDouble(1, newBalance);
            ps.setInt(2, accountId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update account balance.", e);
        }
    }

    private Account mapRow(ResultSet rs) throws SQLException {
        Account a = new Account();
        a.setId(rs.getInt("id"));
        a.setUserId(rs.getInt("user_id"));
        a.setName(rs.getString("name"));
        a.setType(rs.getString("type"));
        a.setBalance(rs.getDouble("balance"));
        String createdAt = rs.getString("created_at");
        if (createdAt != null) {
            a.setCreatedAt(LocalDateTime.parse(createdAt.replace(" ", "T")));
        }
        return a;
    }
}
