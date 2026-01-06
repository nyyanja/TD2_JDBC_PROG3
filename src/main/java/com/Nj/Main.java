package com.Nj;

import com.Nj.code.*;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        DBConnection dbConnection = new DBConnection();
        DataRetriever dataRetriever = new DataRetriever(dbConnection);

        try {

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
            System.out.println("Liste vide");

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

            System.out.println("\nTEST 7.i");
            List<Ingredient> newIngredients = List.of(
                    new Ingredient(0, "Fromage", 1200.0, Category.DAIRY, null),
                    new Ingredient(0, "Oignon", 500.0, Category.VEGETABLE, null)
            );
            dataRetriever.createIngredients(newIngredients);
            System.out.println("Ingrédients créés avec succès");

            System.out.println("\nTEST 7.j");
            List<Ingredient> wrongIngredients = List.of(
                    new Ingredient(0, "Carotte", 2000.0, Category.VEGETABLE, null),
                    new Ingredient(0, "Laitue", 2000.0, Category.VEGETABLE, null)
            );
            try {
                dataRetriever.createIngredients(wrongIngredients);
                System.out.println("ERREUR : atomicité non respectée");
            } catch (RuntimeException e) {
                System.out.println("Atomicité respectée (rollback effectué)");
            }

            System.out.println("\nTEST 7.k");
            Dish soup = new Dish(
                    0,
                    "Soupe de légumes",
                    DishType.START,
                    List.of(
                            new Ingredient(0, "Oignon", 100.0, Category.VEGETABLE, null)
                    )
            );
            soup.setPrice(2500.0);
            dataRetriever.saveDish(soup);
            System.out.println("Plat créé avec succès");

            System.out.println("\nTEST 7.l");
            Dish updateAdd = new Dish(
                    1,
                    "Salade fraîche",
                    DishType.START,
                    List.of(
                            new Ingredient(0, "Oignon", 100.0, Category.VEGETABLE, null),
                            new Ingredient(0, "Fromage", 1200.0, Category.DAIRY, null)
                    )
            );
            updateAdd.setPrice(4000.0);
            dataRetriever.saveDish(updateAdd);
            System.out.println("Plat mis à jour (ajout ingrédients)");

            System.out.println("\nTEST 7.m");
            Dish updateRemove = new Dish(
                    1,
                    "Salade de fromage",
                    DishType.START,
                    List.of(
                            new Ingredient(0, "Fromage", 1200.0, Category.DAIRY, null)
                    )
            );
            updateRemove.setPrice(3500.0);
            dataRetriever.saveDish(updateRemove);
            System.out.println("Plat mis à jour (suppression ingrédients)");

            /*   TESTS DU POINT 4    */

            System.out.println("\nTEST 4.a – coût du plat");
            Dish dishCost = dataRetriever.findDishById(1);
            System.out.println("Coût = " + dishCost.getDishCost());

            System.out.println("\nTEST 4.b – marge brute OK");
            System.out.println("Prix = " + dishCost.getPrice());
            System.out.println("Marge brute = " + dishCost.getGrossMargin());

            System.out.println("\nTEST 4.c – marge brute sans prix");
            Dish noPriceDish = new Dish(
                    0,
                    "Plat sans prix",
                    DishType.MAIN,
                    List.of(
                            new Ingredient(0, "Oignon", 200.0, Category.VEGETABLE, null)
                    )
            );

            try {
                System.out.println(noPriceDish.getGrossMargin());
                System.out.println("ERREUR : exception attendue");
            } catch (RuntimeException e) {
                System.out.println("Exception levée correctement (prix manquant)");
            }

        } finally {
        }
    }
}
