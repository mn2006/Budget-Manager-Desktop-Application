package com.budgetapp.data;

import com.budgetapp.model.BudgetCycle;
import com.budgetapp.model.Category;
import com.budgetapp.model.Expense;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data layer class responsible for all SQLite database operations.
 * Applies Singleton Pattern: only one instance manages all DB connections.
 * Implements Persistence interface (DIP).
 */
public class SQLiteDB implements Persistence {

    private static SQLiteDB instance;
    private Connection connection;
    private static final String DB_URL = "jdbc:sqlite:budget_app.db";

    /** Private constructor - Singleton Pattern */
    private SQLiteDB() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            createTables();
        } catch (SQLException e) {
            System.err.println("DB Connection error: " + e.getMessage());
        }
    }

    /**
     * Returns the single instance of SQLiteDB (Singleton Pattern).
     * @return the SQLiteDB instance
     */
    public static SQLiteDB getInstance() {
        if (instance == null) {
            instance = new SQLiteDB();
        }
        return instance;
    }

    /** Creates tables if they don't exist. */
    private void createTables() {
        String createCycleTable = """
            CREATE TABLE IF NOT EXISTS budget_cycle (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                total_budget REAL NOT NULL,
                start_date TEXT NOT NULL,
                end_date TEXT NOT NULL,
                remaining_balance REAL NOT NULL
            );
        """;
        String createExpenseTable = """
            CREATE TABLE IF NOT EXISTS expenses (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                amount REAL NOT NULL,
                category TEXT NOT NULL,
                date TEXT NOT NULL
            );
        """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createCycleTable);
            stmt.execute(createExpenseTable);
        } catch (SQLException e) {
            System.err.println("Create table error: " + e.getMessage());
        }
    }

    /**
     * Saves an expense to the database.
     * @param expense the expense to save
     */
    @Override
    public void saveExpense(Expense expense) {
        String sql = "INSERT INTO expenses (amount, category, date) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setDouble(1, expense.getAmount());
            pstmt.setString(2, expense.getCategory().name());
            pstmt.setString(3, expense.getDate().toString());
            pstmt.executeUpdate();
            ResultSet keys = pstmt.getGeneratedKeys();
            if (keys.next()) expense.setId(keys.getInt(1));
        } catch (SQLException e) {
            System.err.println("Save expense error: " + e.getMessage());
        }
    }

    /**
     * Loads all expenses from the database.
     * @return list of all expenses
     */
    @Override
    public List<Expense> loadExpenses() {
        List<Expense> expenses = new ArrayList<>();
        String sql = "SELECT * FROM expenses";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                double amount = rs.getDouble("amount");
                Category category = Category.valueOf(rs.getString("category"));
                LocalDate date = LocalDate.parse(rs.getString("date"));
                expenses.add(new Expense(id, amount, category, date));
            }
        } catch (SQLException e) {
            System.err.println("Load expenses error: " + e.getMessage());
        }
        return expenses;
    }

    /**
     * Deletes an expense by ID.
     * @param id the expense ID to delete
     */
    @Override
    public void deleteExpense(int id) {
        String sql = "DELETE FROM expenses WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Delete expense error: " + e.getMessage());
        }
    }

    /**
     * Saves or updates the budget cycle (only one active cycle at a time).
     * @param cycle the BudgetCycle to save
     */
    @Override
    public void saveCycle(BudgetCycle cycle) {
        try {
            // Clear old cycle first
            connection.createStatement().execute("DELETE FROM budget_cycle");
            String sql = "INSERT INTO budget_cycle (total_budget, start_date, end_date, remaining_balance) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setDouble(1, cycle.getTotalBudget());
                pstmt.setString(2, cycle.getStartDate().toString());
                pstmt.setString(3, cycle.getEndDate().toString());
                pstmt.setDouble(4, cycle.getRemainingBalance());
                pstmt.executeUpdate();
                ResultSet keys = pstmt.getGeneratedKeys();
                if (keys.next()) cycle.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            System.err.println("Save cycle error: " + e.getMessage());
        }
    }

    /**
     * Loads the active budget cycle from the database.
     * @return BudgetCycle or null if none found
     */
    @Override
    public BudgetCycle loadCycle() {
        String sql = "SELECT * FROM budget_cycle LIMIT 1";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                int id = rs.getInt("id");
                double totalBudget = rs.getDouble("total_budget");
                LocalDate startDate = LocalDate.parse(rs.getString("start_date"));
                LocalDate endDate = LocalDate.parse(rs.getString("end_date"));
                double remaining = rs.getDouble("remaining_balance");
                return new BudgetCycle(id, totalBudget, startDate, endDate, remaining);
            }
        } catch (SQLException e) {
            System.err.println("Load cycle error: " + e.getMessage());
        }
        return null;
    }

    /** Updates only the remaining balance of the cycle in DB. */
    public void updateCycleBalance(double remaining) {
        String sql = "UPDATE budget_cycle SET remaining_balance = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, remaining);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Update balance error: " + e.getMessage());
        }
    }

    /** Updates an existing expense record. */
    public void updateExpense(Expense expense) {
        String sql = "UPDATE expenses SET amount = ?, category = ?, date = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, expense.getAmount());
            pstmt.setString(2, expense.getCategory().name());
            pstmt.setString(3, expense.getDate().toString());
            pstmt.setInt(4, expense.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Update expense error: " + e.getMessage());
        }
    }
}
