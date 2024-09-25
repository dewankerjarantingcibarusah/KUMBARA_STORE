package com.dkr.kumbarastore.pembeli;

public class CategoryItem {
    private int imageResource;
    private String title;
    private double price;

    public CategoryItem(int imageResource, String title, double price) {
        this.imageResource = imageResource;
        this.title = title;
        this.price = price;
    }

    public int getImageResource() {
        return imageResource;
    }

    public String getTitle() {
        return title;
    }

    public double getPrice() {
        return price;
    }
}
