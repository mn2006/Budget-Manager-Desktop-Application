package com.budgetapp.data;

import com.budgetapp.model.BudgetCycle;
import com.budgetapp.model.Expense;

import java.util.List;

/**
 * Interface defining generic data persistence operations.
 * Applies DIP: high-level controllers depend on this abstraction, not on SQLiteDB.
 * Applies OCP: new persistence types (e.g., CloudDB) can be added without changing controllers.
 */
public interface Persistence {

    /**
     * Saves an expense to the data store.
     * @param expense the expense to save
     */
    void saveExpense(Expense expense);

    /**
     * Loads all expenses from the data store.
     * @return list of all expenses
     */
    List<Expense> loadExpenses();

    /**
     * Deletes an expense by its ID.
     * @param id the expense ID
     */
    void deleteExpense(int id);

    /**
     * Saves a budget cycle to the data store.
     * @param cycle the cycle to save
     */
    void saveCycle(BudgetCycle cycle);

    /**
     * Loads the current active budget cycle.
     * @return the active BudgetCycle, or null if none exists
     */
    BudgetCycle loadCycle();

    /**
     * Updates an existing expense record.
     * @param expense the expense with updated values
     */
    void updateExpense(Expense expense);

    /**
     * Updates the remaining balance of the active cycle.
     * @param remaining the new remaining balance
     */
    void updateCycleBalance(double remaining);
}
