package com.Nj.code;

import java.util.List;
import java.util.Objects;

public class Dish {

    private Integer id;
    private String name;
    private DishType dishType;
    private List<Ingredient> ingredients;
    private Double price;

    public Dish() {}

    public Dish(Integer id, String name, DishType dishType, List<Ingredient> ingredients) {
        this.id = id;
        this.name = name;
        this.dishType = dishType;
        this.setIngredients(ingredients);
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public DishType getDishType() { return dishType; }
    public void setDishType(DishType dishType) { this.dishType = dishType; }

    public List<Ingredient> getIngredients() { return ingredients; }
    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
        if (ingredients != null) {
            for (Ingredient ing : ingredients) {
                ing.setDish(this);
            }
        }
    }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Double getDishCost() {
        if (ingredients == null || ingredients.isEmpty()) return 0.0;
        double total = 0.0;
        for (Ingredient ing : ingredients) {
            if (ing.getQuantity() == null) {
                throw new RuntimeException("Quantité manquante pour l'ingrédient : " + ing.getName());
            }
            total += ing.getPrice() * ing.getQuantity();
        }
        return total;
    }

    public Double getGrossMargin() {
        if (price == null) {
            throw new RuntimeException("Prix de vente non défini");
        }
        return price - getDishCost();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Dish)) return false;
        Dish dish = (Dish) o;
        return Objects.equals(id, dish.id) &&
                Objects.equals(name, dish.name) &&
                dishType == dish.dishType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, dishType);
    }

    @Override
    public String toString() {
        return "Dish{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", dishType=" + dishType +
                ", price=" + price +
                ", ingredients=" + ingredients +
                '}';
    }
}
