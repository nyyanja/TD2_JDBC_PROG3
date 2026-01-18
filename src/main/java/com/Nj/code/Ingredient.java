package com.Nj.code;

public class Ingredient {

    private Integer id;
    private String name;
    private Double price;
    private Category category;

    public Ingredient(Integer id, String name, Double price, Category category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public Double getPrice() { return price; }
    public Category getCategory() { return category; }
}
