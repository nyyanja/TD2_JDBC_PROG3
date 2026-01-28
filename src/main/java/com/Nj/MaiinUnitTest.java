package com.Nj;

import com.Nj.code.UnitConverter;

public class MaiinUnitTest {

    public static void main(String[] args) {
        // Ingrédients et stocks initiaux
        Object[][] ingredients = {
                {"Tomate",   4.0, "PCS"},
                {"Laitue",   5.0, "PCS"},
                {"Chocolat", 3.0, "L"},
                {"Poulet",   10.0, "PCS"},
                {"Beurre",   2.5, "L"}
        };

        for (Object[] ing : ingredients) {
            String name = (String) ing[0];
            double qty = (double) ing[1];
            String unit = (String) ing[2];

            System.out.println("=== " + name + " ===");
            System.out.println("Stock initial: " + qty + " " + unit);

            try {
                double kg = UnitConverter.toKg(name, qty, unit);
                System.out.println("Converti en KG: " + kg);
            } catch (RuntimeException e) {
                System.out.println("Erreur conversion: " + e.getMessage());
            }

            System.out.println();
        }
    }
}
