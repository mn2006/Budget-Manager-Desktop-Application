package com.budgetapp.model;

import java.util.List;

/**
 * Implementation class that performs all financial calculations.
 * Applies SRP: only responsible for calculations.
 * Applies OCP: implements CalculationService; new engines can be added freely.
 */
public class CalculationEngine implements CalculationService {

    /**
     * Computes the safe daily spending limit based on remaining balance and days.
     * @param cycle the active BudgetCycle
     * @return daily limit (0 if no days remaining)
     */
    @Override
    public double computeDailyLimit(BudgetCycle cycle) {
        int remainingDays = cycle.getRemainingDays();
        if (remainingDays <= 0) return 0.0;
        return cycle.getRemainingBalance() / remainingDays;
    }

    /**
     * Computes total expenses from a list.
     * @param expenses list of Expense objects
     * @return total amount spent
     */
    @Override
    public double computeTotals(List<Expense> expenses) {
        return expenses.stream().mapToDouble(Expense::getAmount).sum();
    }

    /**
     * Applies rollover logic: carries remaining balance to next cycle.
     * @param cycle the expired cycle
     * @return the leftover balance to carry forward
     */
    public double applyRollover(BudgetCycle cycle) {
        double leftover = Math.max(0, cycle.getRemainingBalance());
        cycle.applyRollover();
        return leftover;
    }
}
