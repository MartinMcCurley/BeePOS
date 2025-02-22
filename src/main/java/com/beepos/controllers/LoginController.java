package com.beepos.controllers;

import com.beepos.utils.DatabaseUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {
    @FXML
    private ComboBox<String> roleComboBox;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private Button clearButton;

    @FXML
    public void initialize() {
        roleComboBox.getItems().addAll("Seller", "Manager");
        roleComboBox.setValue("Seller");

        loginButton.setOnAction(e -> handleLogin());
        clearButton.setOnAction(e -> handleClear());
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please enter both username and password.");
            return;
        }

        if (DatabaseUtil.validateUser(username, password)) {
            String userRole = DatabaseUtil.getUserRole(username);
            if (userRole != null && userRole.equals(roleComboBox.getValue())) {
                try {
                    String fxmlFile = userRole.equals("Manager") ? "/fxml/manager.fxml" : "/fxml/billing.fxml";
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
                    Parent root = loader.load();
                    
                    if (userRole.equals("Manager")) {
                        ManagerController controller = loader.getController();
                        controller.setUsername(username);
                    } else {
                        BillingController controller = loader.getController();
                        controller.setUsername(username);
                    }
                    
                    Stage stage = (Stage) loginButton.getScene().getWindow();
                    Scene scene = new Scene(root);
                    stage.setScene(scene);
                    stage.setTitle("BeePOS - " + userRole + " Panel");
                    stage.show();
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert("Error", "Error loading application.");
                }
            } else {
                showAlert("Error", "Selected role does not match your account.");
            }
        } else {
            showAlert("Error", "Invalid username or password.");
        }
    }

    private void handleClear() {
        usernameField.clear();
        passwordField.clear();
        roleComboBox.setValue("Seller");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 