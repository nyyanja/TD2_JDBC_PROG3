package com.Nj.code;

import java.time.Instant;
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

    public Ingredient(Integer id, String name, Double price, Category category, Double stockQuantity, String stockUnit) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.stockQuantity = stockQuantity != null ? stockQuantity : 0.0;
        this.stockUnit = stockUnit != null ? stockUnit : "KG";
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

    public void consumeStock(Double quantity) {
        if (quantity > getStockValueAtNow()) {
            throw new RuntimeException("Stock insuffisant pour " + name);
        }
        this.stockQuantity -= quantity;
    }

    public double getStockValueAt(Instant t) {
        double stock = this.stockQuantity;
        for (StockMovement m : stockMovementList) {
            if (!m.getMovementDate().isAfter(t)) {
                if (m.getMovementType() == StockMovementType.IN) stock += m.getQuantity();
                else stock -= m.getQuantity();
            }
        }
        return stock;
    }

    public double getStockValueAtNow() {
        return getStockValueAt(Instant.now());
    }
}
