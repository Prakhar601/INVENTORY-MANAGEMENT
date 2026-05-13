package com.fintrack.service;

import com.fintrack.config.DatabaseConfig;
import com.fintrack.dao.TransactionDAO;
import com.fintrack.dao.impl.TransactionDAOImpl;
import com.fintrack.model.Transaction;
import com.fintrack.util.ValidationUtil;

import java.time.LocalDate;
import java.util.List;

/**
 * Service handling transaction operations with business rule enforcement.
 */
public class TransactionService {

    private final TransactionDAO transactionDAO;

    public TransactionService() {
        this.transactionDAO = new TransactionDAOImpl();
    }

    public TransactionService(TransactionDAO transactionDAO) {
        this.transactionDAO = transactionDAO;
    }

    public List<Transaction> getTransactionsByUser(int userId) {
        return transactionDAO.findByUserId(userId);
    }

    public List<Transaction> getTransactionsByAccount(int accountId) {
        return transactionDAO.findByAccountId(accountId);
    }

    public List<Transaction> getTransactionsByDateRange(int userId, LocalDate from, LocalDate to) {
        return transactionDAO.findByDateRange(userId, from, to);
    }

    public void addTransaction(Transaction txn) {
        ValidationUtil.requireNonNull(txn, "Transaction");
        ValidationUtil.requirePositive(txn.getAmount(), "Amount");
        ValidationUtil.requireNotBlank(txn.getType(), "Transaction Type");
        ValidationUtil.requireNonNull(txn.getDate(), "Date");

        transactionDAO.insert(txn);
        DatabaseConfig.commit();
    }

    public void updateTransaction(Transaction txn) {
        ValidationUtil.requireNonNull(txn, "Transaction");
        ValidationUtil.requirePositive(txn.getAmount(), "Amount");

        transactionDAO.update(txn);
        DatabaseConfig.commit();
    }

    public void deleteTransaction(int transactionId) {
        transactionDAO.delete(transactionId);
        DatabaseConfig.commit();
    }

    public double getTotalIncome(int userId, LocalDate from, LocalDate to) {
        return transactionDAO.sumByType(userId, "INCOME", from, to);
    }

    public double getTotalExpense(int userId, LocalDate from, LocalDate to) {
        return transactionDAO.sumByType(userId, "EXPENSE", from, to);
    }
}
