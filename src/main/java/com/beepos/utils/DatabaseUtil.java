package com.beepos.utils;

import java.sql.*;

public class DatabaseUtil {
    private static final String DB_URL = "jdbc:sqlite:beepos.db";
    
    static {
        try {
            createTables();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private static void createTables() throws SQLException {
        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                username TEXT PRIMARY KEY,
                password TEXT NOT NULL,
                role TEXT NOT NULL
            )
        """;
        
        String createProductsTable = """
            CREATE TABLE IF NOT EXISTS products (
                prod_id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                quantity INTEGER NOT NULL,
                price REAL NOT NULL,
                category TEXT NOT NULL
            )
        """;
        
        String createSalesTable = """
            CREATE TABLE IF NOT EXISTS sales (
                sale_id INTEGER PRIMARY KEY AUTOINCREMENT,
                product_id INTEGER,
                quantity INTEGER,
                total_price REAL,
                sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY(product_id) REFERENCES products(prod_id)
            )
        """;
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createProductsTable);
            stmt.execute(createSalesTable);
            
            // Add default users if they don't exist
            String checkUsers = "SELECT COUNT(*) FROM users";
            ResultSet rs = stmt.executeQuery(checkUsers);
            if (rs.getInt(1) == 0) {
                String insertUsers = """
                    INSERT INTO users (username, password, role) VALUES 
                    ('seller1', 'password123', 'Seller'),
                    ('manager1', 'admin123', 'Manager')
                """;
                stmt.execute(insertUsers);
            }
            
            // Add sample products if they don't exist
            String checkProducts = "SELECT COUNT(*) FROM products";
            rs = stmt.executeQuery(checkProducts);
            if (rs.getInt(1) == 0) {
                String insertProducts = """
                    INSERT INTO products (name, quantity, price, category) VALUES 
                    ('Pepsi', 45, 1.0, 'Drinks'),
                    ('Coke', 48, 1.0, 'Drinks'),
                    ('Burger', 20, 2.0, 'Food'),
                    ('Chicken Sandwich', 15, 3.0, 'Food'),
                    ('Vodka', 24, 15.0, 'Alcohol'),
                    ('Beer', 172, 7.0, 'Alcohol'),
                    ('Beef Steak', 51, 3.0, 'Food'),
                    ('Curry', 55, 3.0, 'Food'),
                    ('Water', 100, 0.5, 'Drinks'),
                    ('Pizza Slice', 30, 2.5, 'Food'),
                    ('Wine', 40, 12.0, 'Alcohol'),
                    ('Coffee', 80, 1.5, 'Drinks'),
                    ('Tea', 90, 1.0, 'Drinks'),
                    ('Salad', 25, 4.0, 'Food'),
                    ('Fish & Chips', 35, 5.0, 'Food')
                """;
                stmt.execute(insertProducts);
            }
        }
    }
    
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
    
    public static boolean validateUser(String username, String password) {
        String query = "SELECT role FROM users WHERE username = ? AND password = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static String getUserRole(String username) {
        String query = "SELECT role FROM users WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("role");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
} 