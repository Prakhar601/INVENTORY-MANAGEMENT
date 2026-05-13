package com.fintrack.dao.impl;

import com.fintrack.config.DatabaseConfig;
import com.fintrack.dao.CategoryDAO;
import com.fintrack.exception.DatabaseException;
import com.fintrack.model.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of {@link CategoryDAO}.
 */
public class CategoryDAOImpl implements CategoryDAO {

    @Override
    public Category findById(int id) throws DatabaseException {
        String sql = "SELECT * FROM categories WHERE id = ?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapRow(rs) : null;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find category.", e);
        }
    }

    @Override
    public List<Category> findAll() throws DatabaseException {
        String sql = "SELECT * FROM categories ORDER BY name";
        List<Category> list = new ArrayList<>();
        try (Statement stmt = DatabaseConfig.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve categories.", e);
        }
        return list;
    }

    @Override
    public boolean insert(Category category) throws DatabaseException {
        String sql = "INSERT INTO categories (user_id, name, type, icon) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, category.getUserId());
            ps.setString(2, category.getName());
            ps.setString(3, category.getType());
            ps.setString(4, category.getIcon());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) category.setId(keys.getInt(1));
                }
            }
            return rows > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to insert category.", e);
        }
    }

    @Override
    public boolean update(Category category) throws DatabaseException {
        String sql = "UPDATE categories SET name = ?, type = ?, icon = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, category.getName());
            ps.setString(2, category.getType());
            ps.setString(3, category.getIcon());
            ps.setInt(4, category.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update category.", e);
        }
    }

    @Override
    public boolean delete(int id) throws DatabaseException {
        String sql = "DELETE FROM categories WHERE id = ?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete category.", e);
        }
    }

    @Override
    public List<Category> findByUserId(int userId) throws DatabaseException {
        String sql = "SELECT * FROM categories WHERE user_id = ? ORDER BY name";
        List<Category> list = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find categories by user.", e);
        }
        return list;
    }

    @Override
    public List<Category> findByUserIdAndType(int userId, String type) throws DatabaseException {
        String sql = "SELECT * FROM categories WHERE user_id = ? AND type = ? ORDER BY name";
        List<Category> list = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, type);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find categories by type.", e);
        }
        return list;
    }

    private Category mapRow(ResultSet rs) throws SQLException {
        Category c = new Category();
        c.setId(rs.getInt("id"));
        c.setUserId(rs.getInt("user_id"));
        c.setName(rs.getString("name"));
        c.setType(rs.getString("type"));
        c.setIcon(rs.getString("icon"));
        return c;
    }
}
