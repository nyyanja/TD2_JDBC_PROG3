package com.Nj.code;

public class Ingredient {

    private Integer id;
    private String name;
    private Double price;
    private Category category;

    private Double stockQuantity;
    private String stockUnit;

    public Ingredient(
            Integer id,
            String name,
            Double price,
            Category category,
            Double stockQuantity,
            String stockUnit
    ) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.stockQuantity = stockQuantity;
        this.stockUnit = stockUnit;
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public Double getPrice() { return price; }
    public Category getCategory() { return category; }

    public Double getStockQuantity() { return stockQuantity; }
    public String getStockUnit() { return stockUnit; }

    public void consumeStock(Double quantity) {
        if (stockQuantity < quantity) {
            throw new RuntimeException(
                    "Stock insuffisant pour " + name
            );
        }
        stockQuantity -= quantity;
    }
}
