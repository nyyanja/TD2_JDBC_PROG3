package com.Nj.code;

import java.util.List;
import java.util.Objects;

public class Dish {

    private int id;
    private String name;
    private DishType dishTypeEnum;
    private List<Ingredient> ingredients;
    private Double price;

    public Dish(int id, String name, DishType dishType, List<Ingredient> ingredients) {
        this.id = id;
        this.name = name;
        this.dishTypeEnum = dishType;
        this.ingredients = ingredients;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DishType getDishTypeEnum() {
        return dishTypeEnum;
    }

    public void setDishTypeEnum(DishType dishTypeEnum) {
        this.dishTypeEnum = dishTypeEnum;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getDishCost() {
        if (ingredients == null || ingredients.isEmpty()) {
            return 0.0;
        }
        return ingredients.stream()
                .mapToDouble(Ingredient::getPrice)
                .sum();
    }

    public Double getGrossMargin() {
        if (price == null) {
            throw new RuntimeException("Le prix de vente n'est pas défini");
        }
        return price - getDishCost();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Dish)) return false;
        Dish dish = (Dish) o;
        return id == dish.id &&
                Objects.equals(name, dish.name) &&
                dishTypeEnum == dish.dishTypeEnum;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, dishTypeEnum);
    }

    @Override
    public String toString() {
        return "Dish{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", dishTypeEnum=" + dishTypeEnum +
                ", price=" + price +
                '}';
    }
}
