package com.Nj.code;

import java.util.ArrayList;
import java.util.List;

public class Ingredient {

    private Integer id;
    private String name;
    private Double price;
    private Category category;
    private Double stockQuantity;
    private String stockUnit;
    private List<StockMovement> stockMovementList;

    public Ingredient(Integer id, String name, Double price, Category category) {
        this(id, name, price, category, 0.0, "KG");
    }

    public Ingredient(Integer id, String name, Double price, Category category, Double stockQuantity, String stockUnit) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.stockQuantity = stockQuantity;
        this.stockUnit = stockUnit;
        this.stockMovementList = new ArrayList<>();
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public Double getPrice() { return price; }
    public Category getCategory() { return category; }

    public Double getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Double stockQuantity) { this.stockQuantity = stockQuantity; }

    public String getStockUnit() { return stockUnit; }
    public void setStockUnit(String stockUnit) { this.stockUnit = stockUnit; }

    public List<StockMovement> getStockMovementList() { return stockMovementList; }
    public void setStockMovementList(List<StockMovement> stockMovementList) { this.stockMovementList = stockMovementList; }

    public void consumeStock(double quantity) {
        if (quantity > stockQuantity) {
            throw new RuntimeException("Stock insuffisant pour " + name);
        }
        this.stockQuantity -= quantity;
    }
}
