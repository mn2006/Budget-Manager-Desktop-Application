package com.budgetapp.exceptions;

/**
 * Exception thrown when a user enters an invalid amount (negative or non-numeric).
 */
public class InvalidAmountException extends Exception {
    public InvalidAmountException(String message) {
        super(message);
    }
}
