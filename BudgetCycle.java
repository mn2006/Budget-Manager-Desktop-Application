package com.budgetapp.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity class representing a budget cycle.
 * Stores allowance, start/end dates, remaining balance, and contained expenses.
 * Applies SRP: only responsible for budget cycle data and basic calculations.
 *
 * State transitions: CREATED → ACTIVE → BUDGET_EXCEEDED → EXPIRED → CLOSED
 */
public class BudgetCycle {

    /** Possible states of a BudgetCycle (State Diagram) */
    public enum State {
        CREATED, ACTIVE, BUDGET_EXCEEDED, EXPIRED, CLOSED
    }

    private int id;
    private double totalBudget;
    private LocalDate startDate;
    private LocalDate endDate;
    private double remainingBalance;
    private State state;
    private List<Expense> expenses;

    /**
     * Constructor for a new BudgetCycle.
     * @param totalBudget total allowance amount
     * @param startDate   cycle start date
     * @param endDate     cycle end date
     */
    public BudgetCycle(double totalBudget, LocalDate startDate, LocalDate endDate) {
        this.totalBudget = totalBudget;
        this.startDate = startDate;
        this.endDate = endDate;
        this.remainingBalance = totalBudget;
        this.expenses = new ArrayList<>();
        this.state = State.CREATED;
        updateState();
    }

    /**
     * Full constructor used when loading from DB.
     */
    public BudgetCycle(int id, double totalBudget, LocalDate startDate, LocalDate endDate, double remainingBalance) {
        this.id = id;
        this.totalBudget = totalBudget;
        this.startDate = startDate;
        this.endDate = endDate;
        this.remainingBalance = remainingBalance;
        this.expenses = new ArrayList<>();
        this.state = State.CREATED;
        updateState();
    }

    /** Updates the state based on current date and balance. */
    public void updateState() {
        LocalDate today = LocalDate.now();
        if (today.isAfter(endDate)) {
            state = State.EXPIRED;
        } else if (today.isBefore(startDate)) {
            state = State.CREATED;
        } else if (remainingBalance < 0) {
            state = State.BUDGET_EXCEEDED;
        } else {
            state = State.ACTIVE;
        }
    }

    /**
     * Calculates the remaining balance after deducting all expenses.
     * @return remaining balance
     */
    public double calculateRemaining() {
        double spent = expenses.stream().mapToDouble(Expense::getAmount).sum();
        remainingBalance = totalBudget - spent;
        return remainingBalance;
    }

    /**
     * Returns number of remaining days in the cycle.
     * @return remaining days (0 if expired)
     */
    public int getRemainingDays() {
        LocalDate today = LocalDate.now();
        if (today.isAfter(endDate)) return 0;
        return (int) ChronoUnit.DAYS.between(today, endDate) + 1;
    }

    /**
     * Applies rollover: closes the cycle.
     */
    public void applyRollover() {
        this.state = State.CLOSED;
    }

    public double getRemainingBalance() { return remainingBalance; }
    public void setRemainingBalance(double remainingBalance) { this.remainingBalance = remainingBalance; }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public double getTotalBudget() { return totalBudget; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public State getState() { return state; }
    public List<Expense> getExpenses() { return expenses; }
    public void setExpenses(List<Expense> expenses) {
        this.expenses = expenses;
        calculateRemaining();
        updateState();
    }
}
