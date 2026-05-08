package com.budgetapp.model;

/**
 * Enumeration that defines different expense categories.
 * Applies OCP: new categories can be added without modifying existing logic.
 */
public enum Category {
    FOOD("Food"),
    TRANSPORT("Transport"),
    SHOPPING("Shopping"),
    OTHER("Other");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
