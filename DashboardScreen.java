package com.budgetapp.view;

import com.budgetapp.controller.BudgetController;
import com.budgetapp.controller.DashboardController;
import com.budgetapp.controller.ExpenseController;
import com.budgetapp.controller.HistoryController;
import com.budgetapp.model.BudgetCycle;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.Map;

/**
 * Boundary class for US3 (Dynamic Daily Limit View) and US4 Bonus (Visual Spending Insights).
 * Implements Observer: receives threshold notifications from DashboardController.
 */
public class DashboardScreen {

    private final BudgetController budgetController;
    private final DashboardController dashboardController;
    private final ExpenseController expenseController;
    private final HistoryController historyController;
    private final Stage stage;

    private Label dailyLimitVal;
    private Label remainingVal;
    private Label totalBudgetVal;
    private Label daysLeftVal;
    private Label statusVal;
    private Label notifBar;
    private ProgressBar progressBar;
    private Label progressLabel;
    private PieChart pieChart;

    public DashboardScreen(BudgetController budgetController,
                           DashboardController dashboardController,
                           ExpenseController expenseController,
                           HistoryController historyController,
                           Stage stage) {
        this.budgetController    = budgetController;
        this.dashboardController = dashboardController;
        this.expenseController   = expenseController;
        this.historyController   = historyController;
        this.stage = stage;
        // Register Observer callback
        dashboardController.setNotificationListener(this::showNotification);
    }

    /**
     * Builds and returns the root UI node.
     * @return Parent root node
     */
    public Parent getRoot() {
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #F4F6FB;");

        // ── Nav bar ──────────────────────────────────────
        HBox nav = new HBox();
        nav.setPadding(new Insets(14, 24, 14, 24));
        nav.setAlignment(Pos.CENTER_LEFT);
        nav.setSpacing(24);
        nav.setStyle("-fx-background-color: #1E3A5F;");

        Label logo = new Label("Budget Manager");
        logo.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        logo.setTextFill(Color.WHITE);

        Region navSpacer = new Region();
        HBox.setHgrow(navSpacer, Priority.ALWAYS);

        Button addBtn  = navButton("+ Add Expense");
        Button histBtn = navButton("History");
        Button newBtn  = navButton("New Cycle");
        addBtn.setOnAction(e  -> goToEntry());
        histBtn.setOnAction(e -> goToHistory());
        newBtn.setOnAction(e  -> goToSetup());

        nav.getChildren().addAll(logo, navSpacer, addBtn, histBtn, newBtn);

        // ── Notification bar ─────────────────────────────
        notifBar = new Label("");
        notifBar.setMaxWidth(Double.MAX_VALUE);
        notifBar.setPadding(new Insets(9, 20, 9, 20));
        notifBar.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        notifBar.setVisible(false);
        notifBar.setManaged(false);

        // ── Stat cards row ───────────────────────────────
        HBox cards = new HBox(14);
        cards.setPadding(new Insets(20, 20, 0, 20));

        VBox c1 = statCard("Safe Daily Limit",  "#1E3A5F");
        VBox c2 = statCard("Remaining Balance", "#16A34A");
        VBox c3 = statCard("Total Budget",      "#D97706");
        VBox c4 = statCard("Days Remaining",    "#6366F1");
        VBox c5 = statCard("Cycle Status",      "#6B7280");

        dailyLimitVal  = (Label) c1.getChildren().get(1);
        remainingVal   = (Label) c2.getChildren().get(1);
        totalBudgetVal = (Label) c3.getChildren().get(1);
        daysLeftVal    = (Label) c4.getChildren().get(1);
        statusVal      = (Label) c5.getChildren().get(1);

        for (VBox c : new VBox[]{c1, c2, c3, c4, c5}) {
            HBox.setHgrow(c, Priority.ALWAYS);
        }
        cards.getChildren().addAll(c1, c2, c3, c4, c5);

        // ── Progress bar ──────────────────────────────────
        VBox progressBox = new VBox(5);
        progressBox.setPadding(new Insets(14, 20, 0, 20));
        progressLabel = new Label("Budget used: 0%");
        progressLabel.setFont(Font.font("Arial", 12));
        progressLabel.setTextFill(Color.web("#6B7280"));
        progressBar = new ProgressBar(0);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(10);
        progressBar.setStyle("-fx-accent: #1E3A5F;");
        progressBox.getChildren().addAll(progressLabel, progressBar);

        // ── Main content (chart + quick actions) ─────────
        HBox content = new HBox(14);
        content.setPadding(new Insets(16, 20, 20, 20));
        VBox.setVgrow(content, Priority.ALWAYS);

        // Left: pie chart
        VBox chartCard = new VBox(10);
        chartCard.setPadding(new Insets(18));
        chartCard.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.08),8,0,0,2);");
        HBox.setHgrow(chartCard, Priority.ALWAYS);

        Label chartTitle = new Label("Spending by Category");
        chartTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        chartTitle.setTextFill(Color.web("#1E3A5F"));

        pieChart = new PieChart();
        pieChart.setLegendVisible(true);
        pieChart.setLabelsVisible(true);
        pieChart.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(pieChart, Priority.ALWAYS);

        chartCard.getChildren().addAll(chartTitle, pieChart);

        // Right: quick panel
        VBox quickPanel = new VBox(12);
        quickPanel.setMinWidth(200);
        quickPanel.setMaxWidth(220);
        quickPanel.setPadding(new Insets(18));
        quickPanel.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.08),8,0,0,2);");

        Label quickTitle = new Label("Quick Actions");
        quickTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        quickTitle.setTextFill(Color.web("#1E3A5F"));

        Button qAdd     = actionButton("+ Add Expense",    "#1E3A5F", "white");
        Button qHistory = actionButton("View History",     "#F3F4F6", "#374151");
        Button qRefresh = actionButton("Refresh",          "#F3F4F6", "#374151");
        Button qNewCycle= actionButton("Start New Cycle",  "#FEF3C7", "#92400E");

        qAdd.setOnAction(e     -> goToEntry());
        qHistory.setOnAction(e -> goToHistory());
        qRefresh.setOnAction(e -> refresh());
        qNewCycle.setOnAction(e-> goToSetup());

        quickPanel.getChildren().addAll(quickTitle, new Separator(),
                qAdd, qHistory, qRefresh, qNewCycle);

        content.getChildren().addAll(chartCard, quickPanel);

        root.getChildren().addAll(nav, notifBar, cards, progressBox, content);

        // Load data
        refresh();
        return root;
    }

    /**
     * Refreshes all dashboard data.
     * Matches US3 sequence: open() → calculateDailyLimit() → display data.
     */
    public void refresh() {
        BudgetCycle cycle = dashboardController.getActiveCycle();
        if (cycle == null) return;

        // calculateDailyLimit()
        double daily    = dashboardController.calculateDailyLimit();
        double remaining = cycle.getRemainingBalance();
        double total    = cycle.getTotalBudget();
        double spent    = total - remaining;
        int    days     = cycle.getRemainingDays();

        displayDailyLimit(daily);
        remainingVal.setText(String.format("%.2f EGP", remaining));
        totalBudgetVal.setText(String.format("%.2f EGP", total));
        daysLeftVal.setText(days + " days");
        statusVal.setText(cycle.getState().toString());

        // Progress bar
        double pct = total > 0 ? Math.min(spent / total, 1.0) : 0;
        progressBar.setProgress(pct);
        progressLabel.setText(String.format("Budget used: %.1f%%  (%.2f / %.2f EGP)", pct * 100, spent, total));
        if (pct >= 0.8) {
            progressBar.setStyle("-fx-accent: #DC2626;");
        } else if (pct >= 0.6) {
            progressBar.setStyle("-fx-accent: #D97706;");
        } else {
            progressBar.setStyle("-fx-accent: #1E3A5F;");
        }

        // generateChart() — US4 Bonus
        Map<String, Double> chartData = dashboardController.generateChart();
        pieChart.getData().clear();
        if (chartData.isEmpty()) {
            pieChart.getData().add(new PieChart.Data("No expenses yet", 1));
        } else {
            chartData.forEach((cat, amt) ->
                    pieChart.getData().add(
                            new PieChart.Data(cat + "  " + String.format("%.0f EGP", amt), amt)));
        }

        // checkThreshold()
        dashboardController.checkThreshold();
    }

    /**
     * Displays the daily limit value on the card.
     * @param d daily limit in EGP
     */
    public void displayDailyLimit(double d) {
        dailyLimitVal.setText(String.format("%.2f EGP", d));
    }

    /**
     * Shows a notification bar — called by Observer callback from DashboardController.
     * @param msg notification message
     */
    public void showNotification(String msg) {
        notifBar.setText("  " + msg);
        notifBar.setVisible(true);
        notifBar.setManaged(true);
        if (msg.contains("Exceeded")) {
            notifBar.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #991B1B;");
        } else {
            notifBar.setStyle("-fx-background-color: #FEF3C7; -fx-text-fill: #92400E;");
        }
    }

    // ── Helpers ───────────────────────────────────────────

    private VBox statCard(String title, String valueColor) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(14));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.08),8,0,0,2);");
        Label t = new Label(title);
        t.setFont(Font.font("Arial", 11));
        t.setTextFill(Color.web("#6B7280"));
        Label v = new Label("—");
        v.setFont(Font.font("Arial", FontWeight.BOLD, 17));
        v.setTextFill(Color.web(valueColor));
        card.getChildren().addAll(t, v);
        return card;
    }

    private Button navButton(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-text-fill: white; " +
                "-fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 12px; -fx-padding: 6 12;");
        return b;
    }

    private Button actionButton(String text, String bg, String fg) {
        Button b = new Button(text);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        b.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg + "; " +
                "-fx-background-radius: 7; -fx-padding: 9 0; -fx-cursor: hand;");
        return b;
    }

    private void goToEntry() {
        QuickEntryScreen q = new QuickEntryScreen(expenseController, budgetController,
                dashboardController, historyController, stage);
        stage.setScene(new Scene(q.getRoot(), 750, 560));
    }

    private void goToHistory() {
        HistoryScreen h = new HistoryScreen(historyController, expenseController,
                budgetController, dashboardController, stage);
        stage.setScene(new Scene(h.getRoot(), 950, 650));
    }

    private void goToSetup() {
        SetupScreen s = new SetupScreen(budgetController, dashboardController,
                expenseController, historyController, stage);
        stage.setScene(new Scene(s.getRoot(), 750, 560));
    }
}
