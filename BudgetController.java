package com.budgetapp.controller;

import com.budgetapp.data.Persistence;
import com.budgetapp.data.PersistenceFactory;
import com.budgetapp.exceptions.InvalidAmountException;
import com.budgetapp.model.BudgetCycle;
import com.budgetapp.model.Expense;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller class responsible for creating and managing the budget cycle.
 * Applies SRP: only handles budget cycle operations.
 * Applies DIP: depends on Persistence interface, not SQLiteDB directly.
 */
public class BudgetController {

    private final Persistence db;
    private BudgetCycle activeCycle;

    public BudgetController() {
        this.db = PersistenceFactory.getDB();
        // Load existing cycle on startup
        this.activeCycle = db.loadCycle();
        if (activeCycle != null) {
            List<Expense> expenses = db.loadExpenses();
            activeCycle.setExpenses(expenses);
        }
    }

    /**
     * Creates a new budget cycle (US1: Set Initial Budget Cycle).
     * @param amount total allowance
     * @param start  start date
     * @param end    end date
     * @return the created BudgetCycle
     * @throws InvalidAmountException if amount is invalid
     */
    public BudgetCycle createCycle(double amount, LocalDate start, LocalDate end)
            throws InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException("Amount must be greater than zero.");
        }
        if (end.isBefore(start)) {
            throw new InvalidAmountException("End date must be after start date.");
        }
        activeCycle = new BudgetCycle(amount, start, end);
        db.saveCycle(activeCycle);
        return activeCycle;
    }

    /**
     * Returns the currently active budget cycle.
     * @return active BudgetCycle or null
     */
    public BudgetCycle getActiveCycle() {
        return activeCycle;
    }

    /**
     * Refreshes cycle expenses from DB and recalculates balance.
     */
    public void refreshCycle() {
        if (activeCycle != null) {
            List<Expense> expenses = db.loadExpenses();
            activeCycle.setExpenses(expenses);
        }
    }
}
