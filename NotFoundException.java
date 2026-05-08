package com.budgetapp.exceptions;

/**
 * Exception thrown when a requested entity (e.g., expense) is not found.
 */
public class NotFoundException extends Exception {
    public NotFoundException(String message) {
        super(message);
    }
}
