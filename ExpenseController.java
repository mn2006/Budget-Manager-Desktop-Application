package com.budgetapp.controller;

import com.budgetapp.data.Persistence;
import com.budgetapp.data.PersistenceFactory;
import com.budgetapp.exceptions.BudgetExceededException;
import com.budgetapp.exceptions.InvalidAmountException;
import com.budgetapp.exceptions.NotFoundException;
import com.budgetapp.model.BudgetCycle;
import com.budgetapp.model.CalculationEngine;
import com.budgetapp.model.Category;
import com.budgetapp.model.Expense;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller class responsible for handling expense operations (add, update, delete).
 * Applies SRP: only handles expense-related logic.
 * Applies DIP: depends on Persistence interface, NOT on SQLiteDB directly.
 * Observer-like: notifies DashboardController after changes.
 */
public class ExpenseController {

    private final Persistence db;
    private final BudgetController budgetController;
    private final CalculationEngine calcEngine;

    /** Observer reference to dashboard (Observer Pattern) */
    private DashboardController dashboardController;

    public ExpenseController(BudgetController budgetController) {
        this.db = PersistenceFactory.getDB();
        this.budgetController = budgetController;
        this.calcEngine = new CalculationEngine();
    }

    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    /**
     * Adds a new expense (US2: Rapid Expense Logging).
     * @param amount   expense amount
     * @param category expense category
     * @return the created Expense
     * @throws InvalidAmountException  if amount is invalid
     * @throws BudgetExceededException if expense exceeds remaining budget
     */
    public Expense addExpense(double amount, Category category)
            throws InvalidAmountException, BudgetExceededException {

        if (amount <= 0) {
            throw new InvalidAmountException("Amount must be greater than zero.");
        }

        BudgetCycle cycle = budgetController.getActiveCycle();
        if (cycle == null) {
            throw new IllegalStateException("No active budget cycle. Please set up a budget first.");
        }

        Expense expense = new Expense(amount, category, LocalDate.now());
        db.saveExpense(expense);

        // Refresh cycle and recompute totals
        budgetController.refreshCycle();
        cycle = budgetController.getActiveCycle();
        double total = calcEngine.computeTotals(cycle.getExpenses());

        // Update remaining balance via Persistence interface (no casting needed)
        double remaining = cycle.getTotalBudget() - total;
        db.updateCycleBalance(remaining);
        cycle.setRemainingBalance(remaining);
        cycle.updateState();

        // Notify dashboard (Observer Pattern)
        if (dashboardController != null) dashboardController.checkThreshold();

        if (remaining < 0) {
            throw new BudgetExceededException("Budget exceeded! Overspent by " +
                    String.format("%.2f", Math.abs(remaining)) + " EGP.");
        }

        return expense;
    }

    /**
     * Updates an existing expense (US6: Update Existing Expense).
     * @param expense the updated expense object
     * @throws InvalidAmountException if new amount is invalid
     * @throws NotFoundException      if expense ID not found
     */
    public void updateExpense(Expense expense) throws InvalidAmountException, NotFoundException {
        if (expense.getAmount() <= 0) {
            throw new InvalidAmountException("Amount must be greater than zero.");
        }
        List<Expense> all = db.loadExpenses();
        boolean found = all.stream().anyMatch(e -> e.getId() == expense.getId());
        if (!found) {
            throw new NotFoundException("Expense with ID " + expense.getId() + " not found.");
        }

        db.updateExpense(expense);
        budgetController.refreshCycle();

        BudgetCycle cycle = budgetController.getActiveCycle();
        double total = calcEngine.computeTotals(cycle.getExpenses());
        double remaining = cycle.getTotalBudget() - total;
        db.updateCycleBalance(remaining);
        cycle.setRemainingBalance(remaining);
        cycle.updateState();

        if (dashboardController != null) dashboardController.checkThreshold();
    }

    /**
     * Deletes an expense by ID (US4: Delete Transaction).
     * @param id the expense ID to delete
     * @throws NotFoundException if expense not found
     */
    public void deleteExpense(int id) throws NotFoundException {
        List<Expense> all = db.loadExpenses();
        boolean found = all.stream().anyMatch(e -> e.getId() == id);
        if (!found) {
            throw new NotFoundException("Expense with ID " + id + " not found.");
        }
        db.deleteExpense(id);
        budgetController.refreshCycle();

        BudgetCycle cycle = budgetController.getActiveCycle();
        if (cycle != null) {
            double total = calcEngine.computeTotals(cycle.getExpenses());
            double remaining = cycle.getTotalBudget() - total;
            db.updateCycleBalance(remaining);
            cycle.setRemainingBalance(remaining);
            cycle.updateState();
        }
        if (dashboardController != null) dashboardController.checkThreshold();
    }
}
