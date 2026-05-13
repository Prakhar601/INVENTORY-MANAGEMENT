package com.fintrack.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Domain model representing a budget for a specific category and time period.
 */
public class Budget {

    private int id;
    private int userId;
    private int categoryId;
    private double amount;
    private String period;      // WEEKLY, MONTHLY, YEARLY
    private LocalDate startDate;
    private LocalDate endDate;

    public Budget() {}

    public Budget(int userId, int categoryId, double amount,
                  String period, LocalDate startDate, LocalDate endDate) {
        this.userId = userId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.period = period;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // ── Getters & Setters ──────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    // ── equals / hashCode / toString ───────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Budget budget = (Budget) o;
        return id == budget.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Budget{id=" + id + ", categoryId=" + categoryId
                + ", amount=" + amount + ", period='" + period + "'}";
    }
}
