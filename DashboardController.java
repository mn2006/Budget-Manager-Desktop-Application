package com.budgetapp.controller;

import com.budgetapp.data.Persistence;
import com.budgetapp.data.PersistenceFactory;
import com.budgetapp.model.BudgetCycle;
import com.budgetapp.model.CalculationEngine;
import com.budgetapp.model.Expense;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Controller responsible for calculating daily limit, generating charts,
 * and checking budget thresholds.
 * Applies SRP: only handles dashboard-related logic.
 * Applies Observer Pattern: notifies the DashboardScreen when thresholds are breached.
 */
public class DashboardController {

    private final Persistence db;
    private final BudgetController budgetController;
    private final CalculationEngine calcEngine;

    // Observer callback - notifies DashboardScreen (Observer Pattern)
    private Consumer<String> notificationListener;

    public DashboardController(BudgetController budgetController) {
        this.db = PersistenceFactory.getDB();
        this.budgetController = budgetController;
        this.calcEngine = new CalculationEngine();
    }

    /**
     * Registers a listener to receive notifications (Observer Pattern).
     * @param listener callback that receives a notification message
     */
    public void setNotificationListener(Consumer<String> listener) {
        this.notificationListener = listener;
    }

    /**
     * Calculates the safe daily spending limit (US3: Dynamic Daily Limit View).
     * @return daily limit in EGP, or 0 if no active cycle
     */
    public double calculateDailyLimit() {
        BudgetCycle cycle = budgetController.getActiveCycle();
        if (cycle == null) return 0.0;
        budgetController.refreshCycle();
        cycle = budgetController.getActiveCycle();
        return calcEngine.computeDailyLimit(cycle);
    }

    /**
     * Generates spending breakdown by category (US4 Bonus: Visual Spending Insights).
     * @return map of category name → total amount spent
     */
    public Map<String, Double> generateChart() {
        List<Expense> expenses = db.loadExpenses();
        Map<String, Double> chartData = new HashMap<>();
        for (Expense e : expenses) {
            String cat = e.getCategory().getDisplayName();
            chartData.merge(cat, e.getAmount(), Double::sum);
        }
        return chartData;
    }

    /**
     * Checks budget threshold and triggers notifications (US5: Budget Threshold Notification).
     * Notifies if budget is exceeded or >= 80% spent.
     */
    public void checkThreshold() {
        BudgetCycle cycle = budgetController.getActiveCycle();
        if (cycle == null || notificationListener == null) return;

        double total = cycle.getTotalBudget();
        double remaining = cycle.getRemainingBalance();
        double spent = total - remaining;

        if (remaining < 0) {
            notificationListener.accept("⚠️ Budget Exceeded! You overspent by " +
                    String.format("%.2f", Math.abs(remaining)) + " EGP.");
        } else if (spent / total >= 0.80) {
            notificationListener.accept("⚠️ Warning: You have spent 80% of your budget!");
        }
    }

    /**
     * Returns all expenses from DB.
     * @return list of all expenses
     */
    public List<Expense> loadExpenses() {
        return db.loadExpenses();
    }

    /**
     * Returns total spent amount.
     * @return total spent
     */
    public double getTotalSpent() {
        List<Expense> expenses = db.loadExpenses();
        return calcEngine.computeTotals(expenses);
    }

    /**
     * Returns the active BudgetCycle.
     * @return active cycle or null
     */
    public BudgetCycle getActiveCycle() {
        budgetController.refreshCycle();
        return budgetController.getActiveCycle();
    }
}
