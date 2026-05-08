package com.budgetapp.view;

import com.budgetapp.controller.BudgetController;
import com.budgetapp.controller.DashboardController;
import com.budgetapp.controller.ExpenseController;
import com.budgetapp.controller.HistoryController;
import com.budgetapp.exceptions.InvalidAmountException;
import com.budgetapp.model.BudgetCycle;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.time.LocalDate;

/**
 * Boundary class for US1: Set Initial Budget Cycle.
 * Collects allowance amount and start/end dates from the user.
 */
public class SetupScreen {

    private final BudgetController budgetController;
    private final DashboardController dashboardController;
    private final ExpenseController expenseController;
    private final HistoryController historyController;
    private final Stage stage;

    private TextField amountField;
    private DatePicker startPicker;
    private DatePicker endPicker;
    private Label statusLabel;

    public SetupScreen(BudgetController budgetController,
                       DashboardController dashboardController,
                       ExpenseController expenseController,
                       HistoryController historyController,
                       Stage stage) {
        this.budgetController    = budgetController;
        this.dashboardController = dashboardController;
        this.expenseController   = expenseController;
        this.historyController   = historyController;
        this.stage = stage;
    }

    /**
     * Builds and returns the root UI node.
     * @return Parent root node
     */
    public Parent getRoot() {
        // Outer background
        VBox outer = new VBox();
        outer.setAlignment(Pos.CENTER);
        outer.setSpacing(0);
        outer.setStyle("-fx-background-color: #F4F6FB;");

        // Header bar
        HBox header = new HBox();
        header.setPadding(new Insets(16, 28, 16, 28));
        header.setStyle("-fx-background-color: #1E3A5F;");
        Label logo = new Label("Budget Manager");
        logo.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        logo.setTextFill(Color.WHITE);
        header.getChildren().add(logo);

        // Card
        VBox card = new VBox(14);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPadding(new Insets(32));
        card.setMaxWidth(420);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 12, 0, 0, 4);");

        Text title = new Text("Set Up Your Budget Cycle");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        title.setFill(Color.web("#1E3A5F"));

        Text sub = new Text("Enter your monthly allowance and choose the cycle period.");
        sub.setFont(Font.font("Arial", 13));
        sub.setFill(Color.web("#6B7280"));
        sub.setWrappingWidth(360);

        Separator sep = new Separator();

        // Amount
        Label amtLbl = fieldLabel("Monthly Allowance (EGP)");
        amountField = new TextField();
        amountField.setPromptText("e.g.  5000");
        styleField(amountField);

        // Dates
        Label startLbl = fieldLabel("Start Date");
        startPicker = new DatePicker(LocalDate.now());
        startPicker.setMaxWidth(Double.MAX_VALUE);

        Label endLbl = fieldLabel("End Date");
        endPicker = new DatePicker(LocalDate.now().plusMonths(1).minusDays(1));
        endPicker.setMaxWidth(Double.MAX_VALUE);

        // Status
        statusLabel = new Label("");
        statusLabel.setWrapText(true);
        statusLabel.setFont(Font.font("Arial", 13));
        statusLabel.setTextFill(Color.web("#DC2626"));

        // Button
        Button btn = new Button("Start Budget Cycle");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        btn.setStyle("-fx-background-color: #1E3A5F; -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 11 0; -fx-cursor: hand;");
        btn.setOnAction(e -> handleStart());

        card.getChildren().addAll(title, sub, sep, amtLbl, amountField,
                startLbl, startPicker, endLbl, endPicker, statusLabel, btn);

        VBox center = new VBox(card);
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(40));
        VBox.setVgrow(center, Priority.ALWAYS);

        outer.getChildren().addAll(header, center);
        return outer;
    }

    /** Handles startCycle() — matches US1 sequence diagram. */
    private void handleStart() {
        statusLabel.setText("");
        String txt = amountField.getText().trim();
        LocalDate start = startPicker.getValue();
        LocalDate end   = endPicker.getValue();

        if (txt.isEmpty()) { statusLabel.setText("Please enter an amount."); return; }
        if (start == null || end == null) { statusLabel.setText("Please select both dates."); return; }

        try {
            double amount = Double.parseDouble(txt);
            BudgetCycle cycle = budgetController.createCycle(amount, start, end);
            goToDashboard();
        } catch (NumberFormatException ex) {
            statusLabel.setText("Invalid amount — please enter a number.");
        } catch (InvalidAmountException ex) {
            statusLabel.setText(ex.getMessage());
        }
    }

    private void goToDashboard() {
        DashboardScreen ds = new DashboardScreen(budgetController, dashboardController,
                expenseController, historyController, stage);
        stage.setScene(new Scene(ds.getRoot(), 900, 650));
        stage.setMinWidth(800);
    }

    private Label fieldLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        l.setTextFill(Color.web("#374151"));
        return l;
    }

    private void styleField(TextField f) {
        f.setStyle("-fx-background-color: #F9FAFB; -fx-border-color: #D1D5DB; " +
                "-fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 8; -fx-font-size: 13px;");
    }
}
