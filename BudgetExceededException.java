package com.budgetapp.exceptions;

/**
 * Exception thrown when a user's expense exceeds the defined budget limit.
 */
public class BudgetExceededException extends Exception {
    public BudgetExceededException(String message) {
        super(message);
    }
}
