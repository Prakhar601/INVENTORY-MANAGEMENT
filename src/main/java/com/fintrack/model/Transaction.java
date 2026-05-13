package com.fintrack.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Domain model representing a financial transaction.
 */
public class Transaction {

    private int id;
    private int userId;
    private int accountId;
    private int categoryId;
    private double amount;
    private String type;          // INCOME, EXPENSE, TRANSFER
    private String description;
    private LocalDate date;
    private LocalDateTime createdAt;

    public Transaction() {}

    public Transaction(int userId, int accountId, int categoryId, double amount,
                       String type, String description, LocalDate date) {
        this.userId = userId;
        this.accountId = accountId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.date = date;
    }

    // ── Getters & Setters ──────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // ── equals / hashCode / toString ───────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Transaction{id=" + id + ", type='" + type + "', amount=" + amount
                + ", date=" + date + "}";
    }
}
