package com.Nj;

import com.Nj.code.*;

import java.time.Instant;
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
        dataRetriever.findIngredient(2, 2)
                .forEach(i -> System.out.println(i.getName()));

        System.out.println("\nTEST 7.d");
        System.out.println(
                "Liste vide = " + dataRetriever.findIngredient(3, 5).isEmpty()
        );

        System.out.println("\nTEST 7.e");
        dataRetriever.findDishByIngredientName("eur")
                .forEach(d -> System.out.println(d.getName()));

        System.out.println("\nTEST 7.f");
        dataRetriever.findIngredientsByCriteria(
                null, Category.VEGETABLE, null, 1, 10
        ).forEach(i -> System.out.println(i.getName()));

        System.out.println("\nTEST 7.i – création ingrédients (avec stock)");
        Ingredient fromage = new Ingredient(
                0, "Fromage", 1200.0, Category.DAIRY, 20.0, "KG"
        );
        Ingredient oignon = new Ingredient(
                0, "Oignon", 500.0, Category.VEGETABLE, 30.0, "KG"
        );
        dataRetriever.createIngredients(List.of(fromage, oignon));
        System.out.println("Ingrédients créés");

        System.out.println("\nTEST 7.k – création plat (consomme stock)");
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
        System.out.println("Plat créé");

        System.out.println("\nTEST 4.a – coût du plat");
        Dish dishCost = dataRetriever.findDishById(soup.getId());
        System.out.println("Coût = " + dishCost.getDishCost());

        System.out.println("\nTEST 4.b – marge brute");
        System.out.println("Prix = " + dishCost.getPrice());
        System.out.println("Marge brute = " + dishCost.getGrossMargin());

        Instant t = Instant.parse("2024-01-06T12:00:00Z");
        double fromageStock = fromage.getStockValueAt(t);
        double oignonStock = oignon.getStockValueAt(t);

        System.out.println("\nStock au " + t + " :");
        System.out.println("Fromage = " + fromageStock + " " + fromage.getStockUnit());
        System.out.println("Oignon = " + oignonStock + " " + oignon.getStockUnit());
    }

}
