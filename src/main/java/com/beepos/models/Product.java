package com.beepos.models;

public class Product {
    private int prodId;
    private String name;
    private int quantity;
    private double price;
    private String category;

    public Product(int prodId, String name, int quantity, double price, String category) {
        this.prodId = prodId;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.category = category;
    }

    // Getters and setters
    public int getProdId() { return prodId; }
    public void setProdId(int prodId) { this.prodId = prodId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
} 