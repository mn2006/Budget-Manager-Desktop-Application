package com.budgetapp.controller;

import com.budgetapp.data.Persistence;
import com.budgetapp.data.PersistenceFactory;
import com.budgetapp.exceptions.NotFoundException;
import com.budgetapp.model.Expense;

import java.util.List;

/**
 * Controller responsible for retrieving all stored transactions from the database.
 * Applies SRP: only handles history/retrieval logic.
 */
public class HistoryController {

    private final Persistence db;
    private final ExpenseController expenseController;

    public HistoryController(ExpenseController expenseController) {
        this.db = PersistenceFactory.getDB();
        this.expenseController = expenseController;
    }

    /**
     * Retrieves all stored expense transactions (US4: View Transaction History).
     * @return list of all expenses
     */
    public List<Expense> getAllTransactions() {
        return db.loadExpenses();
    }

    /**
     * Deletes a transaction by ID (US4: Delete Transaction).
     * @param id the expense ID to delete
     * @throws NotFoundException if transaction not found
     */
    public void deleteTransaction(int id) throws NotFoundException {
        expenseController.deleteExpense(id);
    }
}
