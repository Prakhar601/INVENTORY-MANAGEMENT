package com.fintrack.model;

import java.util.Objects;

/**
 * Domain model representing a transaction category (income or expense).
 */
public class Category {

    private int id;
    private int userId;
    private String name;
    private String type;    // INCOME, EXPENSE
    private String icon;

    public Category() {}

    public Category(int userId, String name, String type) {
        this.userId = userId;
        this.name = name;
        this.type = type;
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

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    // ── equals / hashCode / toString ───────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return id == category.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Category{id=" + id + ", name='" + name + "', type='" + type + "'}";
    }
}
