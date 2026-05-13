package com.fintrack.service;

import com.fintrack.config.DatabaseConfig;
import com.fintrack.dao.BudgetDAO;
import com.fintrack.dao.TransactionDAO;
import com.fintrack.dao.impl.BudgetDAOImpl;
import com.fintrack.dao.impl.TransactionDAOImpl;
import com.fintrack.model.Budget;
import com.fintrack.util.ValidationUtil;

import java.util.List;

/**
 * Service handling budget management and threshold monitoring.
 */
public class BudgetService {

    private final BudgetDAO budgetDAO;
    private final TransactionDAO transactionDAO;

    public BudgetService() {
        this.budgetDAO = new BudgetDAOImpl();
        this.transactionDAO = new TransactionDAOImpl();
    }

    public BudgetService(BudgetDAO budgetDAO, TransactionDAO transactionDAO) {
        this.budgetDAO = budgetDAO;
        this.transactionDAO = transactionDAO;
    }

    public List<Budget> getBudgetsByUser(int userId) {
        return budgetDAO.findByUserId(userId);
    }

    public List<Budget> getActiveBudgets(int userId) {
        return budgetDAO.findActiveBudgets(userId);
    }

    public void createBudget(Budget budget) {
        ValidationUtil.requirePositive(budget.getAmount(), "Budget Amount");
        ValidationUtil.requireNotBlank(budget.getPeriod(), "Budget Period");
        ValidationUtil.requireNonNull(budget.getStartDate(), "Start Date");
        ValidationUtil.requireNonNull(budget.getEndDate(), "End Date");

        budgetDAO.insert(budget);
        DatabaseConfig.commit();
    }

    public void updateBudget(Budget budget) {
        ValidationUtil.requirePositive(budget.getAmount(), "Budget Amount");
        budgetDAO.update(budget);
        DatabaseConfig.commit();
    }

    public void deleteBudget(int budgetId) {
        budgetDAO.delete(budgetId);
        DatabaseConfig.commit();
    }

    /**
     * Returns the spent amount for a budget's category within its date range.
     */
    public double getSpentAmount(Budget budget) {
        return transactionDAO.sumByCategory(
                budget.getCategoryId(),
                budget.getStartDate(),
                budget.getEndDate()
        );
    }

    /**
     * Returns spending percentage (0.0 - 100.0+) for a budget.
     */
    public double getSpendingPercentage(Budget budget) {
        double spent = getSpentAmount(budget);
        return budget.getAmount() > 0 ? (spent / budget.getAmount()) * 100.0 : 0.0;
    }

    /**
     * Checks if a budget has been exceeded.
     */
    public boolean isBudgetExceeded(Budget budget) {
        return getSpentAmount(budget) > budget.getAmount();
    }
}
