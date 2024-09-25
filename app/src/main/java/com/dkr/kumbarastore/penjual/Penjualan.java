package com.dkr.kumbarastore.penjual;

import com.dkr.kumbarastore.pembeli.Product;

import java.util.List;

public class Penjualan {
    private String orderNumber;
    private List<Product> products;
    private String kta;
    private String totalPrice;
    private String namaPembeli;
    private String nomorPembeli;
    private String alamatPembeli;
    private String waktu;
    private String deliveryTime;
    private String status;

    // Konstruktor dengan field tambahan
    public Penjualan(String orderNumber, List<Product> products, String kta, String totalPrice, String namaPembeli,
                     String nomorPembeli, String alamatPembeli, String waktu, String deliveryTime, String status) {
        this.orderNumber = orderNumber;
        this.products = products;
        this.kta = kta;
        this.totalPrice = totalPrice;
        this.namaPembeli = namaPembeli;
        this.nomorPembeli = nomorPembeli;
        this.alamatPembeli = alamatPembeli;
        this.waktu = waktu;
        this.deliveryTime = deliveryTime;
        this.status = status;
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

    public String getNamaPembeli() {
        return namaPembeli;
    }

    public void setNamaPembeli(String namaPembeli) {
        this.namaPembeli = namaPembeli;
    }

    public String getNomorPembeli() {
        return nomorPembeli;
    }

    public void setNomorPembeli(String nomorPembeli) {
        this.nomorPembeli = nomorPembeli;
    }

    public String getAlamatPembeli() {
        return alamatPembeli;
    }

    public void setAlamatPembeli(String alamatPembeli) {
        this.alamatPembeli = alamatPembeli;
    }

    public String getWaktu() {
        return waktu;
    }

    public void setWaktu(String waktu) {
        this.waktu = waktu;
    }

    public String getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(String deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
