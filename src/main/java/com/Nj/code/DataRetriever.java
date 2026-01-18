package com.Nj.code;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {

    private DBConnection db;

    public DataRetriever(DBConnection db) {
        this.db = db;
    }

    // =========================
    // FIND DISH BY ID (TD3)
    // =========================
    public Dish findDishById(Integer id) {
        try (Connection conn = db.getDBConnection()) {

            String dishSql = "SELECT id, name, dish_type, price FROM dish WHERE id = ?";
            PreparedStatement psDish = conn.prepareStatement(dishSql);
            psDish.setInt(1, id);
            ResultSet rsDish = psDish.executeQuery();

            if (!rsDish.next()) {
                throw new RuntimeException("Plat non trouvé pour id = " + id);
            }

            Dish dish = new Dish();
            dish.setId(rsDish.getInt("id"));
            dish.setName(rsDish.getString("name"));
            dish.setDishType(DishType.valueOf(rsDish.getString("dish_type")));
            dish.setPrice(rsDish.getDouble("price"));

            String sql = """
                SELECT i.id, i.name, i.price, i.category,
                       di.quantity, di.unit
                FROM dish_ingredient di
                JOIN ingredient i ON i.id = di.ingredient_id
                WHERE di.dish_id = ?
            """;

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            List<DishIngredient> ingredients = new ArrayList<>();
            while (rs.next()) {
                Ingredient ing = new Ingredient(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        Category.valueOf(rs.getString("category"))
                );

                ingredients.add(new DishIngredient(
                        dish,
                        ing,
                        rs.getDouble("quantity"),
                        rs.getString("unit")
                ));
            }

            dish.setIngredients(ingredients);
            return dish;

        } catch (SQLException e) {
            throw new RuntimeException("Erreur findDishById : " + e.getMessage());
        }
    }

    // =========================
    // FIND INGREDIENT (PAGINATION)
    // =========================
    public List<Ingredient> findIngredient(int page, int size) {
        List<Ingredient> ingredients = new ArrayList<>();
        int offset = (page - 1) * size;

        try (Connection conn = db.getDBConnection()) {

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
                        Category.valueOf(rs.getString("category"))
                ));
            }

            return ingredients;

        } catch (SQLException e) {
            throw new RuntimeException("Erreur findIngredient : " + e.getMessage());
        }
    }

    // =========================
    // CREATE INGREDIENTS (TD3)
    // =========================
    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {
        if (newIngredients == null || newIngredients.isEmpty()) {
            return List.of();
        }

        try (Connection conn = db.getDBConnection()) {
            conn.setAutoCommit(false);

            String sql = "INSERT INTO ingredient(name, price, category) VALUES (?, ?, ?::ingredient_category_enum)";

            for (Ingredient ing : newIngredients) {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, ing.getName());
                    ps.setDouble(2, ing.getPrice());
                    ps.setString(3, ing.getCategory().name());
                    ps.executeUpdate();
                }
            }

            conn.commit();
            return newIngredients;

        } catch (SQLException e) {
            throw new RuntimeException("Erreur createIngredients : " + e.getMessage());
        }
    }

    // =========================
    // SAVE DISH (TD3)
    // =========================
    public Dish saveDish(Dish dish) {
        try (Connection conn = db.getDBConnection()) {
            conn.setAutoCommit(false);

            if (dish.getId() == null || dish.getId() == 0) {
                String insertDish =
                        "INSERT INTO dish(name, dish_type, price) VALUES (?, ?::dish_type_enum, ?) RETURNING id";
                PreparedStatement ps = conn.prepareStatement(insertDish);
                ps.setString(1, dish.getName());
                ps.setString(2, dish.getDishType().name());
                ps.setDouble(3, dish.getPrice());
                ResultSet rs = ps.executeQuery();
                rs.next();
                dish.setId(rs.getInt(1));
            }

            PreparedStatement clear = conn.prepareStatement(
                    "DELETE FROM dish_ingredient WHERE dish_id = ?"
            );
            clear.setInt(1, dish.getId());
            clear.executeUpdate();

            PreparedStatement insert = conn.prepareStatement(
                    "INSERT INTO dish_ingredient(dish_id, ingredient_id, quantity, unit) VALUES (?, ?, ?, ?)"
            );

            for (DishIngredient di : dish.getIngredients()) {
                insert.setInt(1, dish.getId());
                insert.setInt(2, di.getIngredient().getId());
                insert.setDouble(3, di.getQuantity());
                insert.setString(4, di.getUnit());
                insert.executeUpdate();
            }

            conn.commit();
            return dish;

        } catch (SQLException e) {
            throw new RuntimeException("Erreur saveDish : " + e.getMessage());
        }
    }

    // =========================
    // FIND DISH BY INGREDIENT NAME
    // =========================
    public List<Dish> findDishByIngredientName(String ingredientName) {
        List<Dish> dishes = new ArrayList<>();

        try (Connection conn = db.getDBConnection()) {

            String sql = """
                SELECT DISTINCT d.id, d.name, d.dish_type, d.price
                FROM dish d
                JOIN dish_ingredient di ON d.id = di.dish_id
                JOIN ingredient i ON i.id = di.ingredient_id
                WHERE i.name ILIKE ?
            """;

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
            throw new RuntimeException("Erreur findDishByIngredientName : " + e.getMessage());
        }
    }

    // =========================
    // FIND INGREDIENTS BY CRITERIA
    // =========================
    public List<Ingredient> findIngredientsByCriteria(
            String ingredientName,
            Category category,
            String dishName,
            int page,
            int size
    ) {
        List<Ingredient> ingredients = new ArrayList<>();
        int offset = (page - 1) * size;

        try (Connection conn = db.getDBConnection()) {

            StringBuilder sql = new StringBuilder("""
            SELECT DISTINCT i.id, i.name, i.price, i.category
            FROM ingredient i
            LEFT JOIN dish_ingredient di ON i.id = di.ingredient_id
            LEFT JOIN dish d ON d.id = di.dish_id
            WHERE 1=1
        """);

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
                        Category.valueOf(rs.getString("category"))
                ));
            }

            return ingredients;

        } catch (SQLException e) {
            throw new RuntimeException("Erreur findIngredientsByCriteria : " + e.getMessage());
        }
    }
}
