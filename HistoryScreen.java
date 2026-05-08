package com.budgetapp.view;

import com.budgetapp.controller.BudgetController;
import com.budgetapp.controller.DashboardController;
import com.budgetapp.controller.ExpenseController;
import com.budgetapp.controller.HistoryController;
import com.budgetapp.exceptions.InvalidAmountException;
import com.budgetapp.exceptions.NotFoundException;
import com.budgetapp.model.Category;
import com.budgetapp.model.Expense;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.List;

/**
 * Boundary class for US4 (View/Delete Transactions) and US6 (Update Expense).
 */
public class HistoryScreen {

    private final HistoryController historyController;
    private final ExpenseController expenseController;
    private final BudgetController budgetController;
    private final DashboardController dashboardController;
    private final Stage stage;

    private TableView<Expense> table;
    private Label statusLabel;
    private Label totalLabel;

    public HistoryScreen(HistoryController historyController,
                         ExpenseController expenseController,
                         BudgetController budgetController,
                         DashboardController dashboardController,
                         Stage stage) {
        this.historyController   = historyController;
        this.expenseController   = expenseController;
        this.budgetController    = budgetController;
        this.dashboardController = dashboardController;
        this.stage = stage;
    }

    /**
     * Builds and returns the root UI node.
     * @return Parent root node
     */
    public Parent getRoot() {
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #F4F6FB;");

        // Nav
        HBox nav = new HBox();
        nav.setPadding(new Insets(14, 24, 14, 24));
        nav.setAlignment(Pos.CENTER_LEFT);
        nav.setSpacing(16);
        nav.setStyle("-fx-background-color: #1E3A5F;");
        Button backBtn = new Button("< Dashboard");
        backBtn.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-text-fill: white; " +
                "-fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 12px; -fx-padding: 6 12;");
        backBtn.setOnAction(e -> goToDashboard());
        Label logo = new Label("Transaction History");
        logo.setFont(Font.font("Arial", FontWeight.BOLD, 17));
        logo.setTextFill(Color.WHITE);
        nav.getChildren().addAll(backBtn, logo);

        // Body
        VBox body = new VBox(14);
        body.setPadding(new Insets(20));
        VBox.setVgrow(body, Priority.ALWAYS);

        // Status + total row
        HBox infoRow = new HBox(16);
        infoRow.setAlignment(Pos.CENTER_LEFT);
        statusLabel = new Label("");
        statusLabel.setFont(Font.font("Arial", 13));
        statusLabel.setWrapText(true);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        totalLabel = new Label("");
        totalLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        totalLabel.setTextFill(Color.web("#1E3A5F"));
        infoRow.getChildren().addAll(statusLabel, spacer, totalLabel);

        // Table
        table = new TableView<>();
        table.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Expense, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getId()).asObject());
        idCol.setMaxWidth(55);
        idCol.setMinWidth(45);

        TableColumn<Expense, Double> amtCol = new TableColumn<>("Amount (EGP)");
        amtCol.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getAmount()).asObject());
        amtCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : String.format("%.2f", v));
            }
        });

        TableColumn<Expense, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getCategory().getDisplayName()));

        TableColumn<Expense, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getDate().toString()));

        TableColumn<Expense, Void> actCol = new TableColumn<>("Actions");
        actCol.setMinWidth(160);
        actCol.setCellFactory(col -> new TableCell<>() {
            final Button editBtn   = new Button("Edit");
            final Button deleteBtn = new Button("Delete");
            final HBox box = new HBox(8, editBtn, deleteBtn);
            {
                editBtn.setStyle("-fx-background-color: #EFF6FF; -fx-text-fill: #1D4ED8; " +
                        "-fx-background-radius: 5; -fx-cursor: hand; -fx-font-size: 11px; -fx-padding: 4 10;");
                deleteBtn.setStyle("-fx-background-color: #FEF2F2; -fx-text-fill: #DC2626; " +
                        "-fx-background-radius: 5; -fx-cursor: hand; -fx-font-size: 11px; -fx-padding: 4 10;");
                editBtn.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
                box.setAlignment(Pos.CENTER_LEFT);
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : box);
            }
        });

        table.getColumns().addAll(idCol, amtCol, catCol, dateCol, actCol);
        body.getChildren().addAll(infoRow, table);
        root.getChildren().addAll(nav, body);

        loadData();
        return root;
    }

    /**
     * Loads all transactions — matches US4 sequence: getAllTransactions() → loadExpenses().
     */
    private void loadData() {
        List<Expense> list = historyController.getAllTransactions();
        table.getItems().setAll(list);
        double total = list.stream().mapToDouble(Expense::getAmount).sum();
        totalLabel.setText(String.format("Total Spent: %.2f EGP", total));
    }

    /**
     * Handles delete — matches US4 sequence: deleteTransaction(id) → SQLiteDB.deleteExpense().
     */
    private void handleDelete(Expense e) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete this expense of " + String.format("%.2f EGP", e.getAmount()) + "?",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Confirm Deletion");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    historyController.deleteTransaction(e.getId());
                    setStatus("Expense deleted.", true);
                    loadData();
                } catch (NotFoundException ex) {
                    setStatus(ex.getMessage(), false);
                }
            }
        });
    }

    /**
     * Handles edit — matches US6 sequence: updateExpense() → ExpenseController.
     */
    private void handleEdit(Expense expense) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Expense #" + expense.getId());
        dialog.setHeaderText("Update the expense details");

        TextField amtField = new TextField(String.valueOf(expense.getAmount()));
        amtField.setStyle("-fx-font-size: 13px;");
        ComboBox<Category> catBox = new ComboBox<>();
        catBox.getItems().addAll(Category.values());
        catBox.setValue(expense.getCategory());
        catBox.setStyle("-fx-font-size: 13px;");

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10); grid.setPadding(new Insets(14));
        grid.add(new Label("Amount (EGP):"), 0, 0);
        grid.add(amtField, 1, 0);
        grid.add(new Label("Category:"), 0, 1);
        grid.add(catBox, 1, 1);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    double newAmt = Double.parseDouble(amtField.getText().trim());
                    expense.setAmount(newAmt);
                    expense.setCategory(catBox.getValue());
                    expenseController.updateExpense(expense);
                    setStatus("Expense updated.", true);
                    loadData();
                } catch (NumberFormatException ex) {
                    setStatus("Invalid amount.", false);
                } catch (InvalidAmountException | NotFoundException ex) {
                    setStatus(ex.getMessage(), false);
                }
            }
        });
    }

    private void setStatus(String msg, boolean ok) {
        statusLabel.setText(msg);
        statusLabel.setTextFill(ok ? Color.web("#16A34A") : Color.web("#DC2626"));
    }

    private void goToDashboard() {
        DashboardScreen ds = new DashboardScreen(budgetController, dashboardController,
                expenseController, historyController, stage);
        stage.setScene(new Scene(ds.getRoot(), 900, 650));
    }
}
