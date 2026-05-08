package com.budgetapp.data;

/**
 * Factory class responsible for creating Persistence instances.
 * Applies Factory Pattern: decouples object creation from usage.
 * Applies DIP: callers get a Persistence reference, not a concrete SQLiteDB.
 */
public class PersistenceFactory {

    /**
     * Creates and returns a Persistence implementation.
     * Currently returns SQLiteDB singleton; can be extended for other DB types.
     * @return a Persistence instance
     */
    public static Persistence getDB() {
        return SQLiteDB.getInstance();
    }
}
