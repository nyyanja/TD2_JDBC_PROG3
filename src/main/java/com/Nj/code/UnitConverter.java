package com.Nj.code;

import java.util.HashMap;
import java.util.Map;

public class UnitConverter {

    private static final Map<String, ConversionRule> RULES = new HashMap<>();

    static {
        RULES.put("Tomate",   new ConversionRule(10.0, null));
        RULES.put("Laitue",   new ConversionRule(2.0,  null));
        RULES.put("Chocolat", new ConversionRule(10.0, 2.5));
        RULES.put("Poulet",   new ConversionRule(8.0,  null));
        RULES.put("Beurre",   new ConversionRule(4.0,  5.0));
    }

    public static double toKg(String ingredientName, double quantity, String unit) {

        if (unit.equalsIgnoreCase("KG")) {
            return quantity;
        }

        ConversionRule rule = RULES.get(ingredientName);
        if (rule == null) {
            throw new RuntimeException("Aucune règle de conversion pour " + ingredientName);
        }

        switch (unit.toUpperCase()) {
            case "PCS":
                if (rule.pcsPerKg == null) {
                    throw new RuntimeException("Conversion PCS → KG impossible pour " + ingredientName);
                }
                return quantity / rule.pcsPerKg;

            case "L":
                if (rule.lPerKg == null) {
                    throw new RuntimeException("Conversion L → KG impossible pour " + ingredientName);
                }
                return quantity / rule.lPerKg;

            default:
                throw new RuntimeException("Unité inconnue : " + unit);
        }
    }

    private static class ConversionRule {
        Double pcsPerKg;
        Double lPerKg;

        ConversionRule(Double pcsPerKg, Double lPerKg) {
            this.pcsPerKg = pcsPerKg;
            this.lPerKg = lPerKg;
        }
    }
}
