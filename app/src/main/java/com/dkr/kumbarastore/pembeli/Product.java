package com.dkr.kumbarastore.pembeli;

import java.util.HashMap;
import java.util.Map;

public class Product {
    private String imageUrl;
    private String name;
    private String price;
    private int stock;
    private int terjual;
    private String id;
    private int quantity;
    private boolean isSelected;
    private String kta; // KTA (atau ID lainnya) ditambahkan di sini

    public Product(String imageUrl, String name, String price, int quantity) {
        this.imageUrl = imageUrl;
        this.name = name;
        this.price = price;
        this.stock = 0; // Default stock value
        this.terjual = 0; // Default stock value
        this.id = ""; // Default ID value
        this.quantity = quantity;
        this.isSelected = false; // Default selected status
    }
    // Existing constructors
    public Product(String imageUrl, String name, String price, int stock, int terjual,String id) {
        this.imageUrl = imageUrl;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.terjual = terjual;
        this.id = id;
        this.quantity = 1; // Default quantity
        this.isSelected = false; // Default selected status
    }

    public Product(Product product) {
        this.imageUrl = product.imageUrl;
        this.name = product.name;
        this.price = product.price;
        this.stock = product.stock;
        this.terjual = product.terjual;
        this.id = product.id;
        this.quantity = product.quantity;
        this.isSelected = product.isSelected;
    }
    // New constructor for four-string arguments
    public Product(String imageUrl, String name, String price, String id) {
        this.imageUrl = imageUrl;
        this.name = name;
        this.price = price;
        this.id = id;
        this.stock = 0; // Default stock value
        this.quantity = 1; // Default quantity
        this.isSelected = false; // Default selected status
    }

    public Product copy() {
        Product newProduct = new Product(this.imageUrl, this.name, this.price, this.stock,this.terjual, this.id);
        newProduct.setQuantity(this.quantity); // Copy quantity
        newProduct.setSelected(this.isSelected); // Copy isSelected
        return newProduct;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("imageUrl", imageUrl);
        map.put("name", name);
        map.put("price", price);
        map.put("stock", stock);
        map.put("id", id);
        map.put("quantity", quantity);
        map.put("isSelected", isSelected); // Add isSelected property if needed
        return map;
    }
    public void incrementQuantity() {
        this.quantity++;
    }

    public void decrementQuantity() {
        if (this.quantity > 0) {
            this.quantity--;
        }
    }

    public String getKta() {
        return kta;
    }
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getTerjual() {
        return terjual;
    }

    public void setTerjual(int terjual) {
        this.terjual = terjual;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
