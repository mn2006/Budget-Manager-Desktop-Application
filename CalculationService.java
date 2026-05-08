package com.budgetapp.model;

import java.util.List;

/**
 * Interface defining calculation operations for the budget system.
 * Applies OCP: new calculation strategies can be added without modifying existing code.
 * Applies DIP: controllers depend on this abstraction, not on concrete implementations.
 */
public interface CalculationService {

    /**
     * Computes the safe daily spending limit.
     * @param cycle the active budget cycle
     * @return daily limit in EGP
     */
    double computeDailyLimit(BudgetCycle cycle);

    /**
     * Computes the total amount spent from a list of expenses.
     * @param expenses list of expenses
     * @return total spent
     */
    double computeTotals(List<Expense> expenses);
}
