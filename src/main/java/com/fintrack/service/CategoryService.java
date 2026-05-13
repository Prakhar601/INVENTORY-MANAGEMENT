package com.fintrack.service;

import com.fintrack.config.DatabaseConfig;
import com.fintrack.dao.CategoryDAO;
import com.fintrack.dao.impl.CategoryDAOImpl;
import com.fintrack.model.Category;
import com.fintrack.util.ValidationUtil;

import java.util.List;

/**
 * Service handling category CRUD operations.
 */
public class CategoryService {

    private final CategoryDAO categoryDAO;

    public CategoryService() {
        this.categoryDAO = new CategoryDAOImpl();
    }

    public CategoryService(CategoryDAO categoryDAO) {
        this.categoryDAO = categoryDAO;
    }

    public List<Category> getCategoriesByUser(int userId) {
        return categoryDAO.findByUserId(userId);
    }

    public List<Category> getIncomeCategories(int userId) {
        return categoryDAO.findByUserIdAndType(userId, "INCOME");
    }

    public List<Category> getExpenseCategories(int userId) {
        return categoryDAO.findByUserIdAndType(userId, "EXPENSE");
    }

    public Category getCategoryById(int id) {
        return categoryDAO.findById(id);
    }

    public void createCategory(Category category) {
        ValidationUtil.requireNotBlank(category.getName(), "Category Name");
        ValidationUtil.requireNotBlank(category.getType(), "Category Type");

        categoryDAO.insert(category);
        DatabaseConfig.commit();
    }

    public void updateCategory(Category category) {
        ValidationUtil.requireNotBlank(category.getName(), "Category Name");
        categoryDAO.update(category);
        DatabaseConfig.commit();
    }

    public void deleteCategory(int categoryId) {
        categoryDAO.delete(categoryId);
        DatabaseConfig.commit();
    }
}
