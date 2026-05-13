package com.fintrack.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Domain model representing a financial account (savings, checking, credit, cash).
 */
public class Account {

    private int id;
    private int userId;
    private String name;
    private String type;        // SAVINGS, CHECKING, CREDIT, CASH
    private double balance;
    private LocalDateTime createdAt;

    public Account() {}

    public Account(int userId, String name, String type) {
        this.userId = userId;
        this.name = name;
        this.type = type;
        this.balance = 0.0;
    }

    // ── Getters & Setters ──────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // ── equals / hashCode / toString ───────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return id == account.id && userId == account.userId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId);
    }

    @Override
    public String toString() {
        return "Account{id=" + id + ", name='" + name + "', type='" + type
                + "', balance=" + balance + "}";
    }
}
