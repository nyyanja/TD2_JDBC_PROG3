package com.Nj.code;

import java.util.List;

public class Dish {

    private Integer id;
    private String name;
    private DishType dishType;
    private Double price;
    private List<DishIngredient> ingredients;


    public Dish(Integer id, String name, DishType dishType, List<DishIngredient> ingredients) {
        this.id = id;
        this.name = name;
        this.dishType = dishType;
        this.ingredients = ingredients;
    }

    public Dish() {

    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public DishType getDishType() { return dishType; }
    public void setDishType(DishType dishType) { this.dishType = dishType; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public List<DishIngredient> getIngredients() { return ingredients; }
    public void setIngredients(List<DishIngredient> ingredients) { this.ingredients = ingredients; }

    // ===== TD3 =====
    public Double getDishCost() {
        double total = 0.0;
        if (ingredients != null) {
            for (DishIngredient di : ingredients) {
                total += di.getIngredient().getPrice() * di.getQuantity();
            }
        }
        return total;
    }

    public Double getGrossMargin() {
        if (price == null) {
            throw new RuntimeException("Prix de vente non défini");
        }
        return price - getDishCost();
    }
}
