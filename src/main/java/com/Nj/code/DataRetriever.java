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
        Dish dish;
        List<Ingredient> ingredients = new ArrayList<>();

        String dishSql = "SELECT id, name, dish_type FROM dish WHERE id = ?";
        String ingredientSql = "SELECT * FROM ingredient WHERE id_dish = ?";

        try {
            conn = db.getDBConnection();
            PreparedStatement psDish = conn.prepareStatement(dishSql);
            psDish.setInt(1, id);
            ResultSet rsDish = psDish.executeQuery();

            if (!rsDish.next()) throw new RuntimeException("Plat non trouvé pour id = " + id);

            dish = new Dish(
                    rsDish.getInt("id"),
                    rsDish.getString("name"),
                    DishType.valueOf(rsDish.getString("dish_type")),
                    ingredients
            );

            PreparedStatement psIng = conn.prepareStatement(ingredientSql);
            psIng.setInt(1, id);
            ResultSet rsIng = psIng.executeQuery();

            while (rsIng.next()) {
                ingredients.add(new Ingredient(
                        rsIng.getInt("id"),
                        rsIng.getString("name"),
                        rsIng.getDouble("price"),
                        Category.valueOf(rsIng.getString("category")),
                        dish
                ));
            }

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

        String sql = "SELECT * FROM ingredient LIMIT ? OFFSET ?";

        try {
            conn = db.getDBConnection();
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
        Connection conn = null;
        String deleteSql = "DELETE FROM ingredient WHERE name = ?";
        String insertSql = "INSERT INTO ingredient(name, price, category, id_dish) VALUES (?, ?, ?::ingredient_category_enum, ?)";

        try {
            conn = db.getDBConnection();
            conn.setAutoCommit(false);

            for (Ingredient ing : newIngredients) {
                try (PreparedStatement psDelete = conn.prepareStatement(deleteSql)) {
                    psDelete.setString(1, ing.getName());
                    psDelete.executeUpdate();
                }

                try (PreparedStatement psInsert = conn.prepareStatement(insertSql)) {
                    psInsert.setString(1, ing.getName());
                    psInsert.setDouble(2, ing.getPrice());
                    psInsert.setString(3, ing.getCategoryEnum().name());

                    if (ing.getDish() != null) psInsert.setInt(4, ing.getDish().getId());
                    else psInsert.setNull(4, Types.INTEGER);

                    psInsert.executeUpdate();
                }
            }

            conn.commit();
            return newIngredients;

        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ignored) {}
            throw new RuntimeException("Erreur createIngredients: " + e.getMessage());
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException ignored) {}
        }
    }

    public Dish saveDish(Dish dishToSave) {
        Connection conn = null;
        String insertSql = "INSERT INTO dish(name, dish_type) VALUES (?, ?::dish_type_enum) RETURNING id";
        String updateSql = "UPDATE dish SET name = ?, dish_type = ?::dish_type_enum WHERE id = ?";
        String clearIngredientSql = "UPDATE ingredient SET id_dish = NULL WHERE id_dish = ?";
        String attachIngredientSql = "UPDATE ingredient SET id_dish = ? WHERE name = ?";

        try {
            conn = db.getDBConnection();
            conn.setAutoCommit(false);

            if (dishToSave.getId() == 0) {
                PreparedStatement ps = conn.prepareStatement(insertSql);
                ps.setString(1, dishToSave.getName());
                ps.setString(2, dishToSave.getDishTypeEnum().name());

                ResultSet rs = ps.executeQuery();
                if (rs.next()) dishToSave.setId(rs.getInt(1));

            } else {
                PreparedStatement ps = conn.prepareStatement(updateSql);
                ps.setString(1, dishToSave.getName());
                ps.setString(2, dishToSave.getDishTypeEnum().name());
                ps.setInt(3, dishToSave.getId());
                ps.executeUpdate();

                PreparedStatement psClear = conn.prepareStatement(clearIngredientSql);
                psClear.setInt(1, dishToSave.getId());
                psClear.executeUpdate();
            }

            for (Ingredient ing : dishToSave.getIngredients()) {
                PreparedStatement psAttach = conn.prepareStatement(attachIngredientSql);
                psAttach.setInt(1, dishToSave.getId());
                psAttach.setString(2, ing.getName());
                psAttach.executeUpdate();
            }

            conn.commit();
            return dishToSave;

        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ignored) {}
            throw new RuntimeException("Erreur saveDish: " + e.getMessage());
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException ignored) {}
        }
    }

    public List<Dish> findDishByIngredientName(String ingredientName) {
        Connection conn = null;
        List<Dish> dishes = new ArrayList<>();
        String sql = "SELECT DISTINCT d.id, d.name, d.dish_type FROM dish d JOIN ingredient i ON d.id = i.id_dish WHERE i.name ILIKE ?";

        try {
            conn = db.getDBConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + ingredientName + "%");

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                dishes.add(new Dish(
                        rs.getInt("id"),
                        rs.getString("name"),
                        DishType.valueOf(rs.getString("dish_type")),
                        new ArrayList<>()
                ));
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

        StringBuilder sql = new StringBuilder("SELECT i.* FROM ingredient i LEFT JOIN dish d ON i.id_dish = d.id WHERE 1=1 ");
        if (ingredientName != null) sql.append("AND i.name ILIKE ? ");
        if (category != null) sql.append("AND i.category = ?::ingredient_category_enum ");
        if (dishName != null) sql.append("AND d.name ILIKE ? ");
        sql.append("LIMIT ? OFFSET ?");

        try {
            conn = db.getDBConnection();
            PreparedStatement ps = conn.prepareStatement(sql.toString());
            int index = 1;

            if (ingredientName != null) ps.setString(index++, "%" + ingredientName + "%");
            if (category != null) ps.setString(index++, category.name());
            if (dishName != null) ps.setString(index++, "%" + dishName + "%");

            ps.setInt(index++, size);
            ps.setInt(index++, offset);

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
