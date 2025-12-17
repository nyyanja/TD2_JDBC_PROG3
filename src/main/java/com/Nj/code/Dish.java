package com.Nj.code;

import java.util.List;
import java.util.Objects;

public class Dish {
    private int id;
    private String name;
    private  DishType DishTypeEnum;
    private List<Ingredient> ingredients;

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
        return DishTypeEnum;
    }

    public void setDishTypeEnum(DishType dishTypeEnum) {
        DishTypeEnum = dishTypeEnum;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dish dish = (Dish) o;
        return id == dish.id && Objects.equals(name, dish.name) && DishTypeEnum == dish.DishTypeEnum && Objects.equals(ingredients, dish.ingredients);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, DishTypeEnum, ingredients);
    }

    @Override
    public String toString() {
        return "Dish{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", DishTypeEnum=" + DishTypeEnum +
                ", ingredients=" + ingredients +
                '}';
    }

    Double getDishPrice() {
        return ingredients == null ? 0.0 :
        ingredients.stream().mapToDouble(Ingredient::getPrice).sum();
    }
}
