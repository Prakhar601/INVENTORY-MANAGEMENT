package com.fintrack.dao.impl;

import com.fintrack.config.DatabaseConfig;
import com.fintrack.dao.BudgetDAO;
import com.fintrack.exception.DatabaseException;
import com.fintrack.model.Budget;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of {@link BudgetDAO}.
 */
public class BudgetDAOImpl implements BudgetDAO {

    @Override
    public Budget findById(int id) throws DatabaseException {
        String sql = "SELECT * FROM budgets WHERE id = ?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapRow(rs) : null;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find budget.", e);
        }
    }

    @Override
    public List<Budget> findAll() throws DatabaseException {
        String sql = "SELECT * FROM budgets";
        List<Budget> list = new ArrayList<>();
        try (Statement stmt = DatabaseConfig.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve budgets.", e);
        }
        return list;
    }

    @Override
    public boolean insert(Budget budget) throws DatabaseException {
        String sql = "INSERT INTO budgets (user_id, category_id, amount, period, start_date, end_date) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, budget.getUserId());
            ps.setInt(2, budget.getCategoryId());
            ps.setDouble(3, budget.getAmount());
            ps.setString(4, budget.getPeriod());
            ps.setString(5, budget.getStartDate().toString());
            ps.setString(6, budget.getEndDate().toString());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) budget.setId(keys.getInt(1));
                }
            }
            return rows > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to insert budget.", e);
        }
    }

    @Override
    public boolean update(Budget budget) throws DatabaseException {
        String sql = "UPDATE budgets SET category_id = ?, amount = ?, period = ?, "
                   + "start_date = ?, end_date = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, budget.getCategoryId());
            ps.setDouble(2, budget.getAmount());
            ps.setString(3, budget.getPeriod());
            ps.setString(4, budget.getStartDate().toString());
            ps.setString(5, budget.getEndDate().toString());
            ps.setInt(6, budget.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update budget.", e);
        }
    }

    @Override
    public boolean delete(int id) throws DatabaseException {
        String sql = "DELETE FROM budgets WHERE id = ?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete budget.", e);
        }
    }

    @Override
    public List<Budget> findByUserId(int userId) throws DatabaseException {
        String sql = "SELECT * FROM budgets WHERE user_id = ?";
        List<Budget> list = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find budgets by user.", e);
        }
        return list;
    }

    @Override
    public Budget findByUserIdAndCategoryId(int userId, int categoryId) throws DatabaseException {
        String sql = "SELECT * FROM budgets WHERE user_id = ? AND category_id = ?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, categoryId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapRow(rs) : null;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find budget by user and category.", e);
        }
    }

    @Override
    public List<Budget> findActiveBudgets(int userId) throws DatabaseException {
        String today = LocalDate.now().toString();
        String sql = "SELECT * FROM budgets WHERE user_id = ? AND start_date <= ? AND end_date >= ?";
        List<Budget> list = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, today);
            ps.setString(3, today);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find active budgets.", e);
        }
        return list;
    }

    private Budget mapRow(ResultSet rs) throws SQLException {
        Budget b = new Budget();
        b.setId(rs.getInt("id"));
        b.setUserId(rs.getInt("user_id"));
        b.setCategoryId(rs.getInt("category_id"));
        b.setAmount(rs.getDouble("amount"));
        b.setPeriod(rs.getString("period"));
        String start = rs.getString("start_date");
        if (start != null) b.setStartDate(LocalDate.parse(start));
        String end = rs.getString("end_date");
        if (end != null) b.setEndDate(LocalDate.parse(end));
        return b;
    }
}
