package com.budgetapp;

import com.budgetapp.controller.BudgetController;
import com.budgetapp.controller.DashboardController;
import com.budgetapp.controller.ExpenseController;
import com.budgetapp.controller.HistoryController;
import com.budgetapp.view.DashboardScreen;
import com.budgetapp.view.SetupScreen;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main entry point for the Budget Management Desktop Application.
 * Wires together all controllers and launches the appropriate screen.
 */
public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Wire controllers (DIP: via PersistenceFactory)
        BudgetController budgetController   = new BudgetController();
        ExpenseController expenseController = new ExpenseController(budgetController);
        DashboardController dashboardController = new DashboardController(budgetController);
        HistoryController historyController = new HistoryController(expenseController);

        // Wire Observer: expense changes notify dashboard
        expenseController.setDashboardController(dashboardController);

        primaryStage.setTitle("Budget Manager - CS251");
        primaryStage.setMinWidth(750);
        primaryStage.setMinHeight(580);
        primaryStage.setResizable(true);

        if (budgetController.getActiveCycle() == null) {
            SetupScreen setup = new SetupScreen(budgetController, dashboardController,
                    expenseController, historyController, primaryStage);
            primaryStage.setScene(new Scene(setup.getRoot(), 750, 550));
        } else {
            DashboardScreen dash = new DashboardScreen(budgetController, dashboardController,
                    expenseController, historyController, primaryStage);
            primaryStage.setScene(new Scene(dash.getRoot(), 900, 650));
        }

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
