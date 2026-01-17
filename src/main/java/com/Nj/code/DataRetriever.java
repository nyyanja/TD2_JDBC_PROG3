package com.Nj.code;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {

    private DBConnection db;

    public DataRetriever(DBConnection db) {
        this.db = db;
    }

    public Dish findDishById(Integer id) {
        Connection conn = null;
        try {
            conn = db.getDBConnection();
            String dishSql = "SELECT id, name, dish_type, price FROM dish WHERE id = ?";
            PreparedStatement psDish = conn.prepareStatement(dishSql);
            psDish.setInt(1, id);
            ResultSet rsDish = psDish.executeQuery();

            if (!rsDish.next()) throw new RuntimeException("Plat non trouvé pour id = " + id);

            Dish dish = new Dish();
            dish.setId(rsDish.getInt("id"));
            dish.setName(rsDish.getString("name"));
            dish.setDishType(DishType.valueOf(rsDish.getString("dish_type")));
            dish.setPrice(rsDish.getDouble("price"));

            String ingredientSql = "SELECT id, name, price, category FROM ingredient WHERE id_dish = ?";
            PreparedStatement psIng = conn.prepareStatement(ingredientSql);
            psIng.setInt(1, id);
            ResultSet rsIng = psIng.executeQuery();

            List<Ingredient> ingredients = new ArrayList<>();
            while (rsIng.next()) {
                ingredients.add(new Ingredient(
                        rsIng.getInt("id"),
                        rsIng.getString("name"),
                        rsIng.getDouble("price"),
                        Category.valueOf(rsIng.getString("category")),
                        dish
                ));
            }
            dish.setIngredients(ingredients);

            return dish;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findDishById: " + e.getMessage());
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException ignored) {}
        }
    }

    public List<Ingredient> findIngredient(int page, int size) {
        Connection conn = null;
        List<Ingredient> ingredients = new ArrayList<>();
        int offset = (page - 1) * size;

        try {
            conn = db.getDBConnection();
            String sql = "SELECT id, name, price, category FROM ingredient LIMIT ? OFFSET ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, size);
            ps.setInt(2, offset);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ingredients.add(new Ingredient(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        Category.valueOf(rs.getString("category")),
                        null
                ));
            }
            return ingredients;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findIngredient: " + e.getMessage());
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException ignored) {}
        }
    }

    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {
        if (newIngredients == null || newIngredients.isEmpty()) return List.of();
        Connection conn = null;

        try {
            conn = db.getDBConnection();
            conn.setAutoCommit(false);

            String insertSql = "INSERT INTO ingredient(name, price, category, id_dish) VALUES (?, ?, ?::ingredient_category_enum, ?)";

            for (Ingredient ing : newIngredients) {
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setString(1, ing.getName());
                    ps.setDouble(2, ing.getPrice());
                    ps.setString(3, ing.getCategory().name());
                    if (ing.getDish() != null) ps.setInt(4, ing.getDish().getId());
                    else ps.setNull(4, Types.INTEGER);
                    ps.executeUpdate();
                }
            }
            conn.commit();
            return newIngredients;
        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ignored) {}
            throw new RuntimeException("Erreur createIngredients: " + e.getMessage());
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException ignored) {}
        }
    }

    public Dish saveDish(Dish dishToSave) {
        Connection conn = null;
        try {
            conn = db.getDBConnection();
            conn.setAutoCommit(false);

            // Si plat déjà existant, update
            if (dishToSave.getId() != null && dishToSave.getId() != 0) {
                String updateSql = "UPDATE dish SET name = ?, dish_type = ?::dish_type_enum, price = ? WHERE id = ?";
                PreparedStatement ps = conn.prepareStatement(updateSql);
                ps.setString(1, dishToSave.getName());
                ps.setString(2, dishToSave.getDishType().name());
                ps.setDouble(3, dishToSave.getPrice());
                ps.setInt(4, dishToSave.getId());
                ps.executeUpdate();
            } else {
                String insertSql = "INSERT INTO dish(name, dish_type, price) VALUES (?, ?::dish_type_enum, ?) RETURNING id";
                PreparedStatement ps = conn.prepareStatement(insertSql);
                ps.setString(1, dishToSave.getName());
                ps.setString(2, dishToSave.getDishType().name());
                ps.setDouble(3, dishToSave.getPrice());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) dishToSave.setId(rs.getInt(1));
            }

            // Détache les ingrédients existants
            String clearSql = "UPDATE ingredient SET id_dish = NULL WHERE id_dish = ?";
            PreparedStatement psClear = conn.prepareStatement(clearSql);
            psClear.setInt(1, dishToSave.getId());
            psClear.executeUpdate();

            // Attache les nouveaux ingrédients
            String attachSql = "UPDATE ingredient SET id_dish = ? WHERE name = ?";
            for (Ingredient ing : dishToSave.getIngredients()) {
                PreparedStatement psAttach = conn.prepareStatement(attachSql);
                psAttach.setInt(1, dishToSave.getId());
                psAttach.setString(2, ing.getName());
                psAttach.executeUpdate();
            }

            conn.commit();
            return dishToSave;

        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ignored) {}
            throw new RuntimeException("Erreur saveDish: " + e.getMessage());
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException ignored) {}
        }
    }

    // Recherche de plats contenant un ingrédient
    public List<Dish> findDishByIngredientName(String ingredientName) {
        Connection conn = null;
        List<Dish> dishes = new ArrayList<>();

        try {
            conn = db.getDBConnection();
            String sql = "SELECT DISTINCT d.id, d.name, d.dish_type, d.price FROM dish d " +
                    "JOIN ingredient i ON d.id = i.id_dish WHERE i.name ILIKE ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + ingredientName + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Dish dish = new Dish();
                dish.setId(rs.getInt("id"));
                dish.setName(rs.getString("name"));
                dish.setDishType(DishType.valueOf(rs.getString("dish_type")));
                dish.setPrice(rs.getDouble("price"));
                dishes.add(dish);
            }

            return dishes;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findDishByIngredientName: " + e.getMessage());
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException ignored) {}
        }
    }

    public List<Ingredient> findIngredientsByCriteria(String ingredientName, Category category, String dishName, int page, int size) {
        Connection conn = null;
        List<Ingredient> ingredients = new ArrayList<>();
        int offset = (page - 1) * size;

        try {
            conn = db.getDBConnection();
            StringBuilder sql = new StringBuilder(
                    "SELECT i.id, i.name, i.price, i.category FROM ingredient i LEFT JOIN dish d ON i.id_dish = d.id WHERE 1=1 "
            );
            if (ingredientName != null) sql.append("AND i.name ILIKE ? ");
            if (category != null) sql.append("AND i.category = ?::ingredient_category_enum ");
            if (dishName != null) sql.append("AND d.name ILIKE ? ");
            sql.append("LIMIT ? OFFSET ?");

            PreparedStatement ps = conn.prepareStatement(sql.toString());
            int index = 1;
            if (ingredientName != null) ps.setString(index++, "%" + ingredientName + "%");
            if (category != null) ps.setString(index++, category.name());
            if (dishName != null) ps.setString(index++, "%" + dishName + "%");

            ps.setInt(index++, size);
            ps.setInt(index, offset);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ingredients.add(new Ingredient(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        Category.valueOf(rs.getString("category")),
                        null
                ));
            }

            return ingredients;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findIngredientsByCriteria: " + e.getMessage());
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException ignored) {}
        }
    }
}
