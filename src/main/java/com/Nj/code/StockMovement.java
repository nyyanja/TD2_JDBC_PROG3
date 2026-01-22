package com.Nj.code;

import java.time.Instant;

public class StockMovement {

    private Integer id;
    private Ingredient ingredient;
    private Double quantity;
    private String unit;
    private StockMovementType movementType;
    private Instant movementDate;

    public StockMovement() {}

    public StockMovement(
            Integer id,
            Ingredient ingredient,
            Double quantity,
            String unit,
            StockMovementType movementType,
            Instant movementDate
    ) {
        this.id = id;
        this.ingredient = ingredient;
        this.quantity = quantity;
        this.unit = unit;
        this.movementType = movementType;
        this.movementDate = movementDate;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Ingredient getIngredient() { return ingredient; }
    public void setIngredient(Ingredient ingredient) { this.ingredient = ingredient; }

    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public StockMovementType getMovementType() { return movementType; }
    public void setMovementType(StockMovementType movementType) {
        this.movementType = movementType;
    }

    public Instant getMovementDate() { return movementDate; }
    public void setMovementDate(Instant movementDate) {
        this.movementDate = movementDate;
    }
}
