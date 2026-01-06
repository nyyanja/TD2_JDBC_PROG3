package com.Nj.code;

import java.util.Objects;

public class Ingredient {
    private int id;
    private String name;
    private Double price;
    private Category CategoryEnum;
    private Dish dish;

    public Ingredient(int id, String name, Double price, Category category, Dish dish) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.CategoryEnum = category;
        this.dish = dish;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Category getCategoryEnum() { return CategoryEnum; }
    public void setCategoryEnum(Category categoryEnum) { CategoryEnum = categoryEnum; }

    public Dish getDish() { return dish; }
    public void setDish(Dish dish) { this.dish = dish; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ingredient)) return false;
        Ingredient that = (Ingredient) o;
        return id == that.id &&
                Objects.equals(price, that.price) &&
                Objects.equals(name, that.name) &&
                CategoryEnum == that.CategoryEnum &&
                Objects.equals(dish, that.dish);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, price, CategoryEnum, dish);
    }

    @Override
    public String toString() {
        return "Ingredient{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", CategoryEnum=" + CategoryEnum +
                ", dishName=" + getDishName() +
                '}';
    }

    String getDishName() {
        return dish != null ? dish.getName() : "No Dish";
    }
}
