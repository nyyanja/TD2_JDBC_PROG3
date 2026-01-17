package com.Nj.code;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {

    private final DBConnection db;

    public DataRetriever(DBConnection db) {
        this.db = db;
    }

    public Dish findDishById(Integer id) {
        Connection conn = null;
        List<Ingredient> ingredients = new ArrayList<>();

        String dishSql = """
                SELECT id, name, dish_type, price
                FROM dish
                WHERE id = ?
                """;

        String ingredientSql = """
                SELECT id, name, price, category
                FROM ingredient
                WHERE id_dish = ?
                """;

        try {
            conn = db.getDBConnection();

            PreparedStatement psDish = conn.prepareStatement(dishSql);
            psDish.setInt(1, id);
            ResultSet rsDish = psDish.executeQuery();

            if (!rsDish.next()) {
                throw new RuntimeException("Plat non trouvé : " + id);
            }

            Dish dish = new Dish(
                    rsDish.getInt("id"),
                    rsDish.getString("name"),
                    DishType.valueOf(rsDish.getString("dish_type")),
                    ingredients
            );

            Object price = rsDish.getObject("price");
            dish.setPrice(price == null ? null : rsDish.getDouble("price"));

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
            throw new RuntimeException("Erreur findDishById", e);
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException ignored) {}
        }
    }

    public List<Ingredient> findIngredient(int page, int size) {
        Connection conn = null;
        List<Ingredient> ingredients = new ArrayList<>();
        int offset = (page - 1) * size;

        String sql = """
                SELECT id, name, price, category
                FROM ingredient
                LIMIT ? OFFSET ?
                """;

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
            throw new RuntimeException("Erreur findIngredient", e);
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException ignored) {}
        }
    }

    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {
        Connection conn = null;

        String deleteSql = "DELETE FROM ingredient WHERE name = ?";
        String insertSql = """
                INSERT INTO ingredient(name, price, category, id_dish)
                VALUES (?, ?, ?::ingredient_category_enum, ?)
                """;

        try {
            conn = db.getDBConnection();
            conn.setAutoCommit(false);

            for (Ingredient ing : newIngredients) {

                PreparedStatement psDelete = conn.prepareStatement(deleteSql);
                psDelete.setString(1, ing.getName());
                psDelete.executeUpdate();

                PreparedStatement psInsert = conn.prepareStatement(insertSql);
                psInsert.setString(1, ing.getName());
                psInsert.setDouble(2, ing.getPrice());
                psInsert.setString(3, ing.getCategoryEnum().name());

                if (ing.getDish() != null) {
                    psInsert.setInt(4, ing.getDish().getId());
                } else {
                    psInsert.setNull(4, Types.INTEGER);
                }

                psInsert.executeUpdate();
            }

            conn.commit();
            return newIngredients;

        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ignored) {}
            throw new RuntimeException("Erreur createIngredients", e);
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException ignored) {}
        }
    }

    public Dish saveDish(Dish dish) {
        Connection conn = null;

        String insertSql = """
                INSERT INTO dish(name, dish_type, price)
                VALUES (?, ?::dish_type_enum, ?)
                RETURNING id
                """;

        String updateSql = """
                UPDATE dish
                SET name = ?, dish_type = ?::dish_type_enum, price = ?
                WHERE id = ?
                """;

        String detachSql = "UPDATE ingredient SET id_dish = NULL WHERE id_dish = ?";
        String attachSql = "UPDATE ingredient SET id_dish = ? WHERE id = ?";

        try {
            conn = db.getDBConnection();
            conn.setAutoCommit(false);

            if (dish.getId() == 0) {
                PreparedStatement ps = conn.prepareStatement(insertSql);
                ps.setString(1, dish.getName());
                ps.setString(2, dish.getDishTypeEnum().name());

                if (dish.getPrice() != null) ps.setDouble(3, dish.getPrice());
                else ps.setNull(3, Types.DOUBLE);

                ResultSet rs = ps.executeQuery();
                rs.next();
                dish.setId(rs.getInt(1));

            } else {
                PreparedStatement ps = conn.prepareStatement(updateSql);
                ps.setString(1, dish.getName());
                ps.setString(2, dish.getDishTypeEnum().name());

                if (dish.getPrice() != null) ps.setDouble(3, dish.getPrice());
                else ps.setNull(3, Types.DOUBLE);

                ps.setInt(4, dish.getId());
                ps.executeUpdate();

                PreparedStatement detach = conn.prepareStatement(detachSql);
                detach.setInt(1, dish.getId());
                detach.executeUpdate();
            }

            for (Ingredient ing : dish.getIngredients()) {
                PreparedStatement psAttach = conn.prepareStatement(attachSql);
                psAttach.setInt(1, dish.getId());
                psAttach.setInt(2, ing.getId());
                psAttach.executeUpdate();
            }

            conn.commit();
            return dish;

        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ignored) {}
            throw new RuntimeException("Erreur saveDish", e);
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException ignored) {}
        }
    }

    public List<Dish> findDishByIngredientName(String ingredientName) {
        Connection conn = null;
        List<Dish> dishes = new ArrayList<>();

        String sql = """
                SELECT DISTINCT d.id, d.name, d.dish_type, d.price
                FROM dish d
                JOIN ingredient i ON d.id = i.id_dish
                WHERE i.name ILIKE ?
                """;

        try {
            conn = db.getDBConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + ingredientName + "%");

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Dish dish = new Dish(
                        rs.getInt("id"),
                        rs.getString("name"),
                        DishType.valueOf(rs.getString("dish_type")),
                        new ArrayList<>()
                );
                Object price = rs.getObject("price");
                dish.setPrice(price == null ? null : rs.getDouble("price"));
                dishes.add(dish);
            }

            return dishes;

        } catch (SQLException e) {
            throw new RuntimeException("Erreur findDishByIngredientName", e);
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException ignored) {}
        }
    }

    public List<Ingredient> findIngredientsByCriteria(
            String ingredientName,
            Category category,
            String dishName,
            int page,
            int size
    ) {
        Connection conn = null;
        List<Ingredient> ingredients = new ArrayList<>();
        int offset = (page - 1) * size;

        StringBuilder sql = new StringBuilder("""
                SELECT i.id, i.name, i.price, i.category
                FROM ingredient i
                LEFT JOIN dish d ON i.id_dish = d.id
                WHERE 1=1
                """);

        if (ingredientName != null) sql.append(" AND i.name ILIKE ? ");
        if (category != null) sql.append(" AND i.category = ?::ingredient_category_enum ");
        if (dishName != null) sql.append(" AND d.name ILIKE ? ");
        sql.append(" LIMIT ? OFFSET ? ");

        try {
            conn = db.getDBConnection();
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
            throw new RuntimeException("Erreur findIngredientsByCriteria", e);
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException ignored) {}
        }
    }
}
