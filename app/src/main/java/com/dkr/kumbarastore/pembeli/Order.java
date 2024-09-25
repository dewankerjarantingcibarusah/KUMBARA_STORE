package com.dkr.kumbarastore.pembeli;

import java.util.List;

public class Order {
    private String orderNumber;
    private List<Product> products;
    private String kta;
    private String totalPrice; // Tambahkan field ini

    // Konstruktor
    public Order(String orderNumber, List<Product> products, String kta, String totalPrice) {
        this.orderNumber = orderNumber;
        this.products = products;
        this.kta = kta;
        this.totalPrice = totalPrice; // Inisialisasi field ini
    }

    // Getter dan Setter
    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public String getKta() {
        return kta;
    }

    public void setKta(String kta) {
        this.kta = kta;
    }

    public String getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(String totalPrice) {
        this.totalPrice = totalPrice;
    }
}
