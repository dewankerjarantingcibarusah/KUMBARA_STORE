package com.dkr.kumbarastore.pembeli;

public class Kategori {
    private String nama;
    private String imageUrl;

    public Kategori() {
        // Diperlukan konstruktor kosong untuk Firestore
    }

    public Kategori(String nama, String imageUrl) {
        this.nama = nama;
        this.imageUrl = imageUrl;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
