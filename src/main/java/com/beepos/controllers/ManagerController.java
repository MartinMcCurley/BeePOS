package com.beepos.controllers;

import com.beepos.models.Product;
import com.beepos.utils.DatabaseUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.sql.*;
import java.util.Optional;

public class ManagerController {
    @FXML private TextField productNameField;
    @FXML private TextField quantityField;
    @FXML private TextField priceField;
    @FXML private ComboBox<String> categoryComboBox;
    
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, Integer> idColumn;
    @FXML private TableColumn<Product, String> nameColumn;
    @FXML private TableColumn<Product, Integer> quantityColumn;
    @FXML private TableColumn<Product, Double> priceColumn;
    @FXML private TableColumn<Product, String> categoryColumn;
    
    @FXML private Label totalProductsLabel;
    @FXML private Label totalSalesLabel;
    @FXML private Label lowStockLabel;
    
    @FXML private Button addProductButton;
    @FXML private Button updateProductButton;
    @FXML private Button deleteProductButton;
    @FXML private Button clearFormButton;
    @FXML private Button viewDetailedStatsButton;
    @FXML private Button logoutButton;
    
    private String username;
    private ObservableList<Product> products = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        setupButtons();
        setupCategoryComboBox();
        loadProducts();
        updateStatistics();
        
        // Setup table selection listener
        productTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                productNameField.setText(newSelection.getName());
                quantityField.setText(String.valueOf(newSelection.getQuantity()));
                priceField.setText(String.format("%.2f", newSelection.getPrice()));
                categoryComboBox.setValue(newSelection.getCategory());
            }
        });
    }

    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("prodId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        productTable.setItems(products);
    }

    private void setupButtons() {
        addProductButton.setOnAction(e -> handleAddProduct());
        updateProductButton.setOnAction(e -> handleUpdateProduct());
        deleteProductButton.setOnAction(e -> handleDeleteProduct());
        clearFormButton.setOnAction(e -> clearForm());
        viewDetailedStatsButton.setOnAction(e -> showDetailedStats());
        logoutButton.setOnAction(e -> handleLogout());
    }

    private void setupCategoryComboBox() {
        categoryComboBox.getItems().addAll("Drinks", "Food", "Alcohol");
        categoryComboBox.setValue("Drinks");
    }

    private void loadProducts() {
        products.clear();
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM products ORDER BY prod_id")) {
            
            while (rs.next()) {
                products.add(new Product(
                    rs.getInt("prod_id"),
                    rs.getString("name"),
                    rs.getInt("quantity"),
                    rs.getDouble("price"),
                    rs.getString("category")
                ));
            }
        } catch (SQLException e) {
            showError("Error loading products", e.getMessage());
        }
    }

    private void handleAddProduct() {
        if (!validateInput()) return;
        
        String sql = "INSERT INTO products (name, quantity, price, category) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, productNameField.getText());
            pstmt.setInt(2, Integer.parseInt(quantityField.getText()));
            pstmt.setDouble(3, Double.parseDouble(priceField.getText()));
            pstmt.setString(4, categoryComboBox.getValue());
            
            pstmt.executeUpdate();
            clearForm();
            loadProducts();
            updateStatistics();
            showInfo("Success", "Product added successfully");
        } catch (SQLException e) {
            showError("Error adding product", e.getMessage());
        }
    }

    private void handleUpdateProduct() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Error", "Please select a product to update");
            return;
        }
        
        if (!validateInput()) return;
        
        String sql = "UPDATE products SET name=?, quantity=?, price=?, category=? WHERE prod_id=?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, productNameField.getText());
            pstmt.setInt(2, Integer.parseInt(quantityField.getText()));
            pstmt.setDouble(3, Double.parseDouble(priceField.getText()));
            pstmt.setString(4, categoryComboBox.getValue());
            pstmt.setInt(5, selected.getProdId());
            
            pstmt.executeUpdate();
            clearForm();
            loadProducts();
            updateStatistics();
            showInfo("Success", "Product updated successfully");
        } catch (SQLException e) {
            showError("Error updating product", e.getMessage());
        }
    }

    private void handleDeleteProduct() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Error", "Please select a product to delete");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete " + selected.getName() + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String sql = "DELETE FROM products WHERE prod_id = ?";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setInt(1, selected.getProdId());
                pstmt.executeUpdate();
                
                clearForm();
                loadProducts();
                updateStatistics();
                showInfo("Success", "Product deleted successfully");
            } catch (SQLException e) {
                showError("Error deleting product", e.getMessage());
            }
        }
    }

    private void updateStatistics() {
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Total products
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM products");
            if (rs.next()) {
                totalProductsLabel.setText(String.valueOf(rs.getInt(1)));
            }
            
            // Total sales
            rs = stmt.executeQuery("SELECT SUM(total_price) FROM sales");
            if (rs.next()) {
                totalSalesLabel.setText(String.format("£%.2f", rs.getDouble(1)));
            }
            
            // Low stock items (less than 10)
            rs = stmt.executeQuery("SELECT COUNT(*) FROM products WHERE quantity < 10");
            if (rs.next()) {
                lowStockLabel.setText(String.valueOf(rs.getInt(1)));
            }
        } catch (SQLException e) {
            showError("Error updating statistics", e.getMessage());
        }
    }

    private void showDetailedStats() {
        // TODO: Implement detailed statistics view
        showInfo("Coming Soon", "Detailed statistics view is under development");
    }

    private void handleLogout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            Scene scene = new Scene(root);
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("BeePOS - Login");
            stage.show();
        } catch (Exception e) {
            showError("Error", "Could not load login screen");
        }
    }

    private boolean validateInput() {
        if (productNameField.getText().isEmpty()) {
            showError("Validation Error", "Product name is required");
            return false;
        }
        
        try {
            int quantity = Integer.parseInt(quantityField.getText());
            if (quantity < 0) {
                showError("Validation Error", "Quantity must be positive");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Validation Error", "Invalid quantity");
            return false;
        }
        
        try {
            double price = Double.parseDouble(priceField.getText());
            if (price < 0) {
                showError("Validation Error", "Price must be positive");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Validation Error", "Invalid price");
            return false;
        }
        
        return true;
    }

    private void clearForm() {
        productNameField.clear();
        quantityField.clear();
        priceField.clear();
        categoryComboBox.setValue("Drinks");
        productTable.getSelectionModel().clearSelection();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void setUsername(String username) {
        this.username = username;
    }
} 