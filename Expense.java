package com.budgetapp.model;

import java.time.LocalDate;

/**
 * Entity class representing a single expense transaction.
 * Applies SRP: only responsible for expense data.
 */
public class Expense {
    private int id;
    private double amount;
    private Category category;
    private LocalDate date;

    /**
     * Constructor for creating a new Expense.
     * @param amount  the expense amount
     * @param category the expense category
     * @param date    the date of the expense
     */
    public Expense(double amount, Category category, LocalDate date) {
        this.amount = amount;
        this.category = category;
        this.date = date;
    }

    /**
     * Constructor with ID (used when loading from DB).
     */
    public Expense(int id, double amount, Category category, LocalDate date) {
        this.id = id;
        this.amount = amount;
        this.category = category;
        this.date = date;
    }

    /**
     * Returns a formatted string with expense details.
     * @return expense details as String
     */
    public String getDetails() {
        return String.format("ID: %d | Amount: %.2f EGP | Category: %s | Date: %s",
                id, amount, category.getDisplayName(), date.toString());
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
}
