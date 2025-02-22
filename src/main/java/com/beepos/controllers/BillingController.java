package com.beepos.controllers;

import com.beepos.models.Product;
import com.beepos.utils.DatabaseUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import javafx.beans.property.SimpleDoubleProperty;
import java.sql.*;
import java.util.Optional;

public class BillingController {
    @FXML private TextField billIdField;
    @FXML private TextField nameField;
    @FXML private TextField quantityField;
    
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, Integer> prodIdColumn;
    @FXML private TableColumn<Product, String> productNameColumn;
    @FXML private TableColumn<Product, Integer> stockColumn;
    @FXML private TableColumn<Product, Double> priceColumn;
    @FXML private TableColumn<Product, String> categoryColumn;
    
    @FXML private ComboBox<String> filterComboBox;
    @FXML private Button filterButton;
    @FXML private Button showAllButton;
    @FXML private Button addToBillButton;
    @FXML private Button clearButton;
    
    @FXML private TableView<Product> billTable;
    @FXML private TableColumn<Product, Integer> billProdIdColumn;
    @FXML private TableColumn<Product, String> billProductNameColumn;
    @FXML private TableColumn<Product, Integer> billQuantityColumn;
    @FXML private TableColumn<Product, Double> billPriceColumn;
    @FXML private TableColumn<Product, Double> billTotalColumn;
    
    @FXML private Label totalLabel;
    @FXML private Button clearDbButton;
    @FXML private Button checkoutButton;
    @FXML private Button statsButton;
    @FXML private Button logoutButton;
    
    private String username;
    private ObservableList<Product> products = FXCollections.observableArrayList();
    private ObservableList<Product> currentBill = FXCollections.observableArrayList();
    private double total = 0.0;

    @FXML
    public void initialize() {
        setupTables();
        setupFilters();
        setupButtons();
        loadProducts();
        
        // Setup product table selection listener
        productTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                nameField.setText(newSelection.getName());
            }
        });
    }

    private void setupTables() {
        // Setup product table columns
        prodIdColumn.setCellValueFactory(new PropertyValueFactory<>("prodId"));
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        
        // Setup bill table columns
        billProdIdColumn.setCellValueFactory(new PropertyValueFactory<>("prodId"));
        billProductNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        billQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        billPriceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        billTotalColumn.setCellValueFactory(cellData -> {
            Product product = cellData.getValue();
            double total = product.getQuantity() * product.getPrice();
            return new SimpleDoubleProperty(total).asObject();
        });
        
        productTable.setItems(products);
        billTable.setItems(currentBill);
    }

    private void setupFilters() {
        filterComboBox.getItems().addAll("Drinks", "Food", "Alcohol");
        filterComboBox.setValue("Drinks");
        
        filterButton.setOnAction(e -> filterProducts());
        showAllButton.setOnAction(e -> showAllProducts());
    }

    private void setupButtons() {
        addToBillButton.setOnAction(e -> addToBill());
        clearButton.setOnAction(e -> clearBillForm());
        clearDbButton.setOnAction(e -> clearDatabase());
        checkoutButton.setOnAction(e -> handleCheckout());
        statsButton.setOnAction(e -> showStats());
        logoutButton.setOnAction(e -> handleLogout());
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

    private void addToBill() {
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            showError("Error", "Please select a product");
            return;
        }

        if (quantityField.getText().isEmpty()) {
            showError("Error", "Please enter quantity");
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityField.getText());
            if (quantity <= 0) {
                showError("Error", "Quantity must be positive");
                return;
            }

            if (quantity > selectedProduct.getQuantity()) {
                showError("Error", "Not enough stock available");
                return;
            }

            // Check if product already in bill
            Optional<Product> existingProduct = currentBill.stream()
                .filter(p -> p.getProdId() == selectedProduct.getProdId())
                .findFirst();

            if (existingProduct.isPresent()) {
                Product product = existingProduct.get();
                product.setQuantity(product.getQuantity() + quantity);
            } else {
                Product billProduct = new Product(
                    selectedProduct.getProdId(),
                    selectedProduct.getName(),
                    quantity,
                    selectedProduct.getPrice(),
                    selectedProduct.getCategory()
                );
                currentBill.add(billProduct);
            }

            updateTotal();
            clearBillForm();
        } catch (NumberFormatException e) {
            showError("Error", "Invalid quantity");
        }
    }

    private void handleCheckout() {
        if (currentBill.isEmpty()) {
            showError("Error", "No items in bill");
            return;
        }

        // Create payment dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Checkout");
        dialog.setHeaderText("Total Amount: £" + String.format("%.2f", total));

        // Set the button types
        ButtonType payButton = new ButtonType("Pay", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(payButton, ButtonType.CANCEL);

        // Create the payment grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField cashField = new TextField();
        cashField.setPromptText("Enter cash amount");
        grid.add(new Label("Cash Amount:"), 0, 0);
        grid.add(cashField, 1, 0);
        grid.add(new Label("£"), 0, 1);
        Label changeLabel = new Label("0.00");
        grid.add(changeLabel, 1, 1);

        // Calculate change on cash amount entry
        cashField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                double cash = Double.parseDouble(newValue);
                double change = cash - total;
                changeLabel.setText(String.format("%.2f", Math.max(0, change)));
            } catch (NumberFormatException e) {
                changeLabel.setText("Invalid amount");
            }
        });

        dialog.getDialogPane().setContent(grid);

        // Enable/Disable pay button depending on the cash amount
        Node payButtonNode = dialog.getDialogPane().lookupButton(payButton);
        payButtonNode.setDisable(true);

        cashField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                double cash = Double.parseDouble(newValue);
                payButtonNode.setDisable(cash < total);
            } catch (NumberFormatException e) {
                payButtonNode.setDisable(true);
            }
        });

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == payButton) {
            try {
                double cash = Double.parseDouble(cashField.getText());
                double change = cash - total;
                
                // Process the sale
                processSale();
                
                // Show change dialog
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Change");
                alert.setHeaderText(null);
                alert.setContentText(String.format("Change due: £%.2f", change));
                alert.showAndWait();
                
                // Clear the bill
                currentBill.clear();
                total = 0.0;
                totalLabel.setText("£0.00");
                loadProducts(); // Refresh product list
            } catch (NumberFormatException e) {
                showError("Error", "Invalid cash amount");
            }
        }
    }

    private void processSale() {
        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Update product quantities
                String updateProductSql = "UPDATE products SET quantity = quantity - ? WHERE prod_id = ?";
                // Insert sales records
                String insertSaleSql = "INSERT INTO sales (product_id, quantity, total_price) VALUES (?, ?, ?)";
                
                PreparedStatement updateProduct = conn.prepareStatement(updateProductSql);
                PreparedStatement insertSale = conn.prepareStatement(insertSaleSql);
                
                for (Product product : currentBill) {
                    // Update product quantity
                    updateProduct.setInt(1, product.getQuantity());
                    updateProduct.setInt(2, product.getProdId());
                    updateProduct.executeUpdate();
                    
                    // Insert sale record
                    insertSale.setInt(1, product.getProdId());
                    insertSale.setInt(2, product.getQuantity());
                    insertSale.setDouble(3, product.getQuantity() * product.getPrice());
                    insertSale.executeUpdate();
                }
                
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            showError("Error processing sale", e.getMessage());
        }
    }

    private void showStats() {
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            
            StringBuilder stats = new StringBuilder();
            
            // Total sales
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count, SUM(total_price) as total FROM sales");
            if (rs.next()) {
                stats.append(String.format("Total Sales: %d\n", rs.getInt("count")));
                stats.append(String.format("Total Revenue: £%.2f\n\n", rs.getDouble("total")));
            }
            
            // Top selling products
            rs = stmt.executeQuery("""
                SELECT p.name, SUM(s.quantity) as total_qty, SUM(s.total_price) as total_revenue
                FROM sales s
                JOIN products p ON s.product_id = p.prod_id
                GROUP BY p.prod_id
                ORDER BY total_qty DESC
                LIMIT 5
            """);
            
            stats.append("Top 5 Selling Products:\n");
            while (rs.next()) {
                stats.append(String.format("%s - Qty: %d, Revenue: £%.2f\n",
                    rs.getString("name"),
                    rs.getInt("total_qty"),
                    rs.getDouble("total_revenue")
                ));
            }
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Sales Statistics");
            alert.setHeaderText(null);
            alert.setContentText(stats.toString());
            alert.showAndWait();
            
        } catch (SQLException e) {
            showError("Error loading statistics", e.getMessage());
        }
    }

    private void filterProducts() {
        String category = filterComboBox.getValue();
        ObservableList<Product> filteredProducts = products.filtered(
            product -> product.getCategory().equals(category)
        );
        productTable.setItems(filteredProducts);
    }

    private void showAllProducts() {
        productTable.setItems(products);
    }

    private void clearDatabase() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Clear Database");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to clear all products?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection conn = DatabaseUtil.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM products");
                products.clear();
                currentBill.clear();
                total = 0.0;
                totalLabel.setText("£0.00");
            } catch (SQLException e) {
                showError("Error clearing database", e.getMessage());
            }
        }
    }

    private void clearBillForm() {
        nameField.clear();
        quantityField.clear();
        productTable.getSelectionModel().clearSelection();
    }

    private void updateTotal() {
        total = currentBill.stream()
            .mapToDouble(p -> p.getQuantity() * p.getPrice())
            .sum();
        totalLabel.setText(String.format("£%.2f", total));
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

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void setUsername(String username) {
        this.username = username;
    }
} 