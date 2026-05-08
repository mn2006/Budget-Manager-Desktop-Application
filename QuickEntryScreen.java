package com.budgetapp.view;

import com.budgetapp.controller.BudgetController;
import com.budgetapp.controller.DashboardController;
import com.budgetapp.controller.ExpenseController;
import com.budgetapp.controller.HistoryController;
import com.budgetapp.exceptions.BudgetExceededException;
import com.budgetapp.exceptions.InvalidAmountException;
import com.budgetapp.model.Category;
import com.budgetapp.model.Expense;
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

/**
 * Boundary class for US2: Rapid Expense Logging.
 * Provides amount and category fields, forwards to ExpenseController.
 */
public class QuickEntryScreen {

    private final ExpenseController expenseController;
    private final BudgetController budgetController;
    private final DashboardController dashboardController;
    private final HistoryController historyController;
    private final Stage stage;

    private TextField amountField;
    private ComboBox<Category> categoryCombo;
    private Label statusLabel;

    public QuickEntryScreen(ExpenseController expenseController,
                            BudgetController budgetController,
                            DashboardController dashboardController,
                            HistoryController historyController,
                            Stage stage) {
        this.expenseController   = expenseController;
        this.budgetController    = budgetController;
        this.dashboardController = dashboardController;
        this.historyController   = historyController;
        this.stage = stage;
    }

    /**
     * Builds and returns the root UI node.
     * @return Parent root node
     */
    public Parent getRoot() {
        VBox outer = new VBox();
        outer.setStyle("-fx-background-color: #F4F6FB;");

        // Header
        HBox header = buildHeader();

        // Card
        VBox card = new VBox(14);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPadding(new Insets(30));
        card.setMaxWidth(400);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 12, 0, 0, 4);");

        Text title = new Text("Add New Expense");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        title.setFill(Color.web("#1E3A5F"));

        Separator sep = new Separator();

        // Amount
        Label amtLbl = fieldLabel("Amount (EGP)");
        amountField = new TextField();
        amountField.setPromptText("e.g.  150");
        styleField(amountField);

        // Category
        Label catLbl = fieldLabel("Category");
        categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll(Category.values());
        categoryCombo.setValue(Category.FOOD);
        categoryCombo.setMaxWidth(Double.MAX_VALUE);
        categoryCombo.setStyle("-fx-font-size: 13px;");

        // Status
        statusLabel = new Label("");
        statusLabel.setWrapText(true);
        statusLabel.setFont(Font.font("Arial", 13));

        // Buttons
        Button saveBtn = primaryButton("Save Expense");
        saveBtn.setOnAction(e -> handleSave());

        Button backBtn = secondaryButton("Back to Dashboard");
        backBtn.setOnAction(e -> goToDashboard());

        card.getChildren().addAll(title, sep, amtLbl, amountField,
                catLbl, categoryCombo, statusLabel, saveBtn, backBtn);

        VBox center = new VBox(card);
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(40));
        VBox.setVgrow(center, Priority.ALWAYS);

        outer.getChildren().addAll(header, center);
        return outer;
    }

    /** Handles saveExpense() — matches US2 sequence diagram. */
    private void handleSave() {
        statusLabel.setTextFill(Color.web("#DC2626"));
        statusLabel.setText("");
        String txt = amountField.getText().trim();
        if (txt.isEmpty()) { statusLabel.setText("Please enter an amount."); return; }

        try {
            double amount = Double.parseDouble(txt);
            Category cat = categoryCombo.getValue();
            // addExpense(amount, cat) → ExpenseController
            Expense exp = expenseController.addExpense(amount, cat);
            statusLabel.setTextFill(Color.web("#16A34A"));
            statusLabel.setText("Saved: " + String.format("%.2f EGP — %s", exp.getAmount(), exp.getCategory()));
            amountField.clear();
        } catch (NumberFormatException ex) {
            statusLabel.setText("Invalid amount — enter a number.");
        } catch (InvalidAmountException ex) {
            statusLabel.setText(ex.getMessage());
        } catch (BudgetExceededException ex) {
            statusLabel.setTextFill(Color.web("#D97706"));
            statusLabel.setText("Warning: " + ex.getMessage());
        } catch (IllegalStateException ex) {
            statusLabel.setText(ex.getMessage());
        }
    }

    private void goToDashboard() {
        DashboardScreen ds = new DashboardScreen(budgetController, dashboardController,
                expenseController, historyController, stage);
        stage.setScene(new Scene(ds.getRoot(), 900, 650));
    }

    private HBox buildHeader() {
        HBox h = new HBox();
        h.setPadding(new Insets(14, 28, 14, 28));
        h.setAlignment(Pos.CENTER_LEFT);
        h.setSpacing(16);
        h.setStyle("-fx-background-color: #1E3A5F;");
        Button back = new Button("< Dashboard");
        back.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-text-fill: white; " +
                "-fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 12px;");
        back.setOnAction(e -> goToDashboard());
        Label logo = new Label("Budget Manager");
        logo.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        logo.setTextFill(Color.WHITE);
        h.getChildren().addAll(back, logo);
        return h;
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

    private Button primaryButton(String text) {
        Button b = new Button(text);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        b.setStyle("-fx-background-color: #1E3A5F; -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 10 0; -fx-cursor: hand;");
        return b;
    }

    private Button secondaryButton(String text) {
        Button b = new Button(text);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setFont(Font.font("Arial", 13));
        b.setStyle("-fx-background-color: #F3F4F6; -fx-text-fill: #374151; " +
                "-fx-background-radius: 8; -fx-padding: 9 0; -fx-cursor: hand; " +
                "-fx-border-color: #D1D5DB; -fx-border-radius: 8;");
        return b;
    }
}
