module com.budgetapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.xerial.sqlitejdbc;

    opens com.budgetapp to javafx.fxml;
    opens com.budgetapp.view to javafx.fxml;
    opens com.budgetapp.controller to javafx.fxml;
    opens com.budgetapp.model to javafx.fxml;
    opens com.budgetapp.data to javafx.fxml;

    exports com.budgetapp;
    exports com.budgetapp.view;
    exports com.budgetapp.controller;
    exports com.budgetapp.model;
    exports com.budgetapp.data;
    exports com.budgetapp.exceptions;
}
