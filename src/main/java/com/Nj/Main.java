package com.Nj;

import com.Nj.code.*;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        DBConnection dbConnection = new DBConnection();
        DataRetriever dataRetriever = new DataRetriever(dbConnection);

        System.out.println("TEST 7.a");
        Dish dish1 = dataRetriever.findDishById(1);
        System.out.println(dish1.getName() + " | ingrédients = " + dish1.getIngredients().size());

        System.out.println("\nTEST 7.b");
        try {
            dataRetriever.findDishById(999);
            System.out.println("ERREUR : exception attendue");
        } catch (RuntimeException e) {
            System.out.println("Exception levée correctement");
        }

        System.out.println("\nTEST 7.c");
        List<Ingredient> page2 = dataRetriever.findIngredient(2, 2);
        page2.forEach(i -> System.out.println(i.getName()));

        System.out.println("\nTEST 7.d");
        List<Ingredient> emptyPage = dataRetriever.findIngredient(3, 5);
        System.out.println("Liste vide = " + emptyPage.isEmpty());

        System.out.println("\nTEST 7.e");
        List<Dish> dishesByIngredient = dataRetriever.findDishByIngredientName("eur");
        dishesByIngredient.forEach(d -> System.out.println(d.getName()));

        System.out.println("\nTEST 7.f");
        List<Ingredient> vegs = dataRetriever.findIngredientsByCriteria(
                null,
                Category.VEGETABLE,
                null,
                1,
                10
        );
        vegs.forEach(i -> System.out.println(i.getName()));

        System.out.println("\nTEST 7.g");
        List<Ingredient> emptyCriteria = dataRetriever.findIngredientsByCriteria(
                "cho",
                null,
                "Sal",
                1,
                10
        );
        System.out.println("Liste vide ? " + emptyCriteria.isEmpty());

        System.out.println("\nTEST 7.h");
        List<Ingredient> chocolate = dataRetriever.findIngredientsByCriteria(
                "cho",
                null,
                "gâteau",
                1,
                10
        );
        chocolate.forEach(i -> System.out.println(i.getName()));

        System.out.println("\nTEST 7.i – création ingrédients");
        Ingredient fromage = new Ingredient(0, "Fromage", 1200.0, Category.DAIRY);
        Ingredient oignon = new Ingredient(0, "Oignon", 500.0, Category.VEGETABLE);
        dataRetriever.createIngredients(List.of(fromage, oignon));
        System.out.println("Ingrédients créés avec succès");

        System.out.println("\nTEST 7.k – création plat");
        Ingredient ing1 = dataRetriever.findIngredient(1, 1).get(0);

        Dish soup = new Dish(
                0,
                "Soupe de légumes",
                DishType.START,
                List.of(
                        new DishIngredient(null, ing1, 2.0, "KG")
                )
        );
        soup.setPrice(2500.0);
        dataRetriever.saveDish(soup);
        System.out.println("Plat créé avec succès");

        System.out.println("\nTEST 7.l – mise à jour plat");
        Ingredient ing2 = dataRetriever.findIngredient(2, 1).get(0);

        Dish updateDish = new Dish(
                1,
                "Salade fraîche",
                DishType.START,
                List.of(
                        new DishIngredient(null, ing1, 1.0, "KG"),
                        new DishIngredient(null, ing2, 1.0, "KG")
                )
        );
        updateDish.setPrice(4000.0);
        dataRetriever.saveDish(updateDish);
        System.out.println("Plat mis à jour");

        System.out.println("\nTEST 4.a – coût du plat");
        Dish dishCost = dataRetriever.findDishById(1);
        System.out.println("Coût = " + dishCost.getDishCost());

        System.out.println("\nTEST 4.b – marge brute");
        System.out.println("Prix = " + dishCost.getPrice());
        System.out.println("Marge brute = " + dishCost.getGrossMargin());
    }
}
