package com.aesthetica.dto;

public class GalleryProductDTO {
    private int productId;
    private String title;
    private String description;
    private double price;
    private String image;

    public GalleryProductDTO(int productId, String title, String description, double price, String image) {
        this.productId = productId;
        this.title = title;
        this.description = description;
        this.price = price;
        this.image = image;
    }

    public int getProductId() { return productId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public String getImage() { return image; }
}
