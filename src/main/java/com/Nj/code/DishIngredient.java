package com.Nj.code;

public class DishIngredient {
    private Dish dish;
    private Ingredient ingredient;
    private Double quantity;
    private String unit;

    public DishIngredient(Dish dish, Ingredient ingredient, Double quantity, String unit) {
        this.dish = dish;
        this.ingredient = ingredient;
        this.quantity = quantity;
        this.unit = unit;
    }

    public Dish getDish() { return dish; }
    public Ingredient getIngredient() { return ingredient; }
    public Double getQuantity() { return quantity; }
    public String getUnit() { return unit; }
}
