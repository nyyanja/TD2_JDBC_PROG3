package com.Nj.code;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {
    private final DBConnection db;

    public DataRetriever() {
        this.db = new DBConnection();
    }

    public Dish findDishById(Integer id) {
        Dish dish = null;
        try (var connection = db.getDBConnection();
             var ps = connection.prepareStatement(
                     "SELECT d.id AS dish_id, d.name AS dish_name, d.type AS dish_type, " +
                             "i.id AS ingredient_id, i.name AS ingredient_name " +
                             "FROM dishes d " +
                             "LEFT JOIN dish_ingredients di ON d.id = di.dish_id " +
                             "LEFT JOIN ingredients i ON di.ingredient_id = i.id " +
                             "WHERE d.id = ?")) {

            ps.setInt(1, id);

            try (var rs = ps.executeQuery()) {
                List<Ingredient> ingredients = new ArrayList<>();

                while (rs.next()) {
                    if (dish == null) {
                        dish = new Dish();
                        dish.setId(rs.getInt("dish_id"));
                        dish.setName(rs.getString("dish_name"));
                        dish.setDishTypeEnum(DishType.valueOf(rs.getString("dish_type")));
                    }

                    Integer ingredientId = rs.getObject("ingredient_id", Integer.class);
                    if (ingredientId != null) {
                        Ingredient ingredient = new Ingredient();
                        ingredient.setId(ingredientId);
                        ingredient.setName(rs.getString("ingredient_name"));
                        ingredients.add(ingredient);
                    }
                }

                if (dish != null) {
                    dish.setIngredients(ingredients);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération du plat", e);
        }

        return dish;
    }


}
