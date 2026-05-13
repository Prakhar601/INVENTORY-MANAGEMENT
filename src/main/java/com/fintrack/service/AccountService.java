package com.fintrack.service;

import com.fintrack.config.DatabaseConfig;
import com.fintrack.dao.AccountDAO;
import com.fintrack.dao.impl.AccountDAOImpl;
import com.fintrack.exception.ValidationException;
import com.fintrack.model.Account;
import com.fintrack.util.ValidationUtil;

import java.util.List;

/**
 * Service handling account operations — create, update, delete, balance management.
 */
public class AccountService {

    private final AccountDAO accountDAO;

    public AccountService() {
        this.accountDAO = new AccountDAOImpl();
    }

    public AccountService(AccountDAO accountDAO) {
        this.accountDAO = accountDAO;
    }

    public List<Account> getAccountsByUser(int userId) {
        return accountDAO.findByUserId(userId);
    }

    public Account getAccountById(int id) {
        return accountDAO.findById(id);
    }

    public void createAccount(Account account) {
        ValidationUtil.requireNotBlank(account.getName(), "Account Name");
        ValidationUtil.requireNotBlank(account.getType(), "Account Type");

        accountDAO.insert(account);
        DatabaseConfig.commit();
    }

    public void updateAccount(Account account) {
        ValidationUtil.requireNotBlank(account.getName(), "Account Name");
        accountDAO.update(account);
        DatabaseConfig.commit();
    }

    public void deleteAccount(int accountId) {
        accountDAO.delete(accountId);
        DatabaseConfig.commit();
    }

    public void recalculateBalance(int accountId) {
        // This will be enhanced to compute balance from transactions
        // For now, the balance is stored directly on the account
    }

    public double getTotalBalance(int userId) {
        return getAccountsByUser(userId).stream()
                .mapToDouble(Account::getBalance)
                .sum();
    }
}
