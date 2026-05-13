package com.fintrack.service;

import com.fintrack.dao.TransactionDAO;
import com.fintrack.dao.impl.TransactionDAOImpl;

import java.time.LocalDate;

/**
 * Service handling report generation and data aggregation.
 */
public class ReportService {

    private final TransactionDAO transactionDAO;

    public ReportService() {
        this.transactionDAO = new TransactionDAOImpl();
    }

    public ReportService(TransactionDAO transactionDAO) {
        this.transactionDAO = transactionDAO;
    }

    public double calculateNetWorth(int userId) {
        // Simple net worth calculation based on income and expenses for all time
        // Note: A more accurate calculation would sum account balances, 
        // but this serves as a basic report metric based on transactions.
        LocalDate from = LocalDate.of(1900, 1, 1); // Way in the past
        LocalDate to = LocalDate.now().plusDays(1); // Tomorrow

        double totalIncome = transactionDAO.sumByType(userId, "INCOME", from, to);
        double totalExpense = transactionDAO.sumByType(userId, "EXPENSE", from, to);

        return totalIncome - totalExpense;
    }
    
    public double getIncomeForMonth(int userId, int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        return transactionDAO.sumByType(userId, "INCOME", start, end);
    }
    
    public double getExpenseForMonth(int userId, int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        return transactionDAO.sumByType(userId, "EXPENSE", start, end);
    }
}
