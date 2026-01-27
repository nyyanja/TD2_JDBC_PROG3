package com.Nj.code;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {

    private final DBConnection db;

    public DataRetriever(DBConnection db) {
        this.db = db;
    }

    // =========================
    // FIND DISH BY ID
    // =========================
    public Dish findDishById(Integer id) {
        try (Connection conn = db.getDBConnection()) {

            PreparedStatement psDish = conn.prepareStatement(
                    "SELECT id, name, dish_type, price FROM dish WHERE id = ?"
            );
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

            PreparedStatement psIng = conn.prepareStatement("""
                SELECT i.id, i.name, i.price, i.category, i.stock_quantity, i.stock_unit,
                       di.quantity, di.unit
                FROM dish_ingredient di
                JOIN ingredient i ON i.id = di.ingredient_id
                WHERE di.dish_id = ?
            """);
            psIng.setInt(1, id);

            ResultSet rs = psIng.executeQuery();
            List<DishIngredient> ingredients = new ArrayList<>();

            while (rs.next()) {
                Ingredient ing = new Ingredient(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        Category.valueOf(rs.getString("category")),
                        rs.getDouble("stock_quantity"),
                        rs.getString("stock_unit")
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
        List<Ingredient> list = new ArrayList<>();
        int offset = (page - 1) * size;

        try (Connection conn = db.getDBConnection()) {

            PreparedStatement ps = conn.prepareStatement("""
                SELECT id, name, price, category, stock_quantity, stock_unit
                FROM ingredient
                LIMIT ? OFFSET ?
            """);
            ps.setInt(1, size);
            ps.setInt(2, offset);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Ingredient(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        Category.valueOf(rs.getString("category")),
                        rs.getDouble("stock_quantity"),
                        rs.getString("stock_unit")
                ));
            }
            return list;

        } catch (SQLException e) {
            throw new RuntimeException("Erreur findIngredient : " + e.getMessage());
        }
    }

    // =========================
    // CREATE INGREDIENTS
    // =========================
    public List<Ingredient> createIngredients(List<Ingredient> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) return List.of();

        try (Connection conn = db.getDBConnection()) {
            conn.setAutoCommit(false);

            for (Ingredient i : ingredients) {
                PreparedStatement check = conn.prepareStatement(
                        "SELECT id FROM ingredient WHERE name = ? AND category = ?::ingredient_category_enum"
                );
                check.setString(1, i.getName());
                check.setString(2, i.getCategory().name());
                ResultSet rs = check.executeQuery();

                if (rs.next()) {
                    i.setId(rs.getInt("id"));
                    continue;
                }

                PreparedStatement ps = conn.prepareStatement("""
                INSERT INTO ingredient
                (name, price, category, stock_quantity, stock_unit)
                VALUES (?, ?, ?::ingredient_category_enum, ?, ?)
                RETURNING id
            """);
                ps.setString(1, i.getName());
                ps.setDouble(2, i.getPrice());
                ps.setString(3, i.getCategory().name());
                ps.setDouble(4, i.getStockQuantity());
                ps.setString(5, i.getStockUnit());
                ResultSet rsInsert = ps.executeQuery();
                if (rsInsert.next()) {
                    i.setId(rsInsert.getInt(1));
                }
            }

            conn.commit();
            return ingredients;

        } catch (SQLException e) {
            throw new RuntimeException("Erreur createIngredients : " + e.getMessage());
        }
    }


    // =========================
    // SAVE DISH + STOCK UPDATE
    // =========================
    public Dish saveDish(Dish dish) {
        try (Connection conn = db.getDBConnection()) {
            conn.setAutoCommit(false);

            if (dish.getId() == null || dish.getId() == 0) {
                PreparedStatement ps = conn.prepareStatement("""
                    INSERT INTO dish(name, dish_type, price)
                    VALUES (?, ?::dish_type_enum, ?)
                    RETURNING id
                """);
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

            PreparedStatement insert = conn.prepareStatement("""
                INSERT INTO dish_ingredient
                (dish_id, ingredient_id, quantity, unit)
                VALUES (?, ?, ?, ?)
            """);

            PreparedStatement updateStock = conn.prepareStatement("""
                UPDATE ingredient
                SET stock_quantity = ?
                WHERE id = ?
            """);

            for (DishIngredient di : dish.getIngredients()) {
                Ingredient ing = di.getIngredient();
                if (ing.getStockQuantity() < di.getQuantity()) {
                    throw new RuntimeException("Stock insuffisant pour " + ing.getName());
                }
                ing.consumeStock(di.getQuantity());

                updateStock.setDouble(1, ing.getStockQuantity());
                updateStock.setInt(2, ing.getId());
                updateStock.executeUpdate();

                insert.setInt(1, dish.getId());
                insert.setInt(2, ing.getId());
                insert.setDouble(3, di.getQuantity());
                insert.setString(4, di.getUnit());
                insert.executeUpdate();
            }

            conn.commit();
            return dish;

        } catch (Exception e) {
            throw new RuntimeException("Erreur saveDish : " + e.getMessage());
        }
    }

    // =========================
    // FIND DISH BY INGREDIENT NAME
    // =========================
    public List<Dish> findDishByIngredientName(String ingredientName) {
        List<Dish> dishes = new ArrayList<>();

        try (Connection conn = db.getDBConnection()) {

            PreparedStatement ps = conn.prepareStatement("""
                SELECT DISTINCT d.id, d.name, d.dish_type, d.price
                FROM dish d
                JOIN dish_ingredient di ON d.id = di.dish_id
                JOIN ingredient i ON i.id = di.ingredient_id
                WHERE i.name ILIKE ?
            """);
            ps.setString(1, "%" + ingredientName + "%");

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Dish d = new Dish();
                d.setId(rs.getInt("id"));
                d.setName(rs.getString("name"));
                d.setDishType(DishType.valueOf(rs.getString("dish_type")));
                d.setPrice(rs.getDouble("price"));
                dishes.add(d);
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
        List<Ingredient> list = new ArrayList<>();
        int offset = (page - 1) * size;

        try (Connection conn = db.getDBConnection()) {

            StringBuilder sql = new StringBuilder("""
                SELECT DISTINCT i.id, i.name, i.price, i.category,
                                i.stock_quantity, i.stock_unit
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
            int i = 1;

            if (ingredientName != null) ps.setString(i++, "%" + ingredientName + "%");
            if (category != null) ps.setString(i++, category.name());
            if (dishName != null) ps.setString(i++, "%" + dishName + "%");

            ps.setInt(i++, size);
            ps.setInt(i, offset);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Ingredient(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        Category.valueOf(rs.getString("category")),
                        rs.getDouble("stock_quantity"),
                        rs.getString("stock_unit")
                ));
            }
            return list;

        } catch (SQLException e) {
            throw new RuntimeException("Erreur findIngredientsByCriteria : " + e.getMessage());
        }
    }

    // =========================
    // SAVE INGREDIENT (TD4) + STOCK MOVEMENTS
    // =========================
    public Ingredient saveIngredient(Ingredient toSave) {
        try (Connection conn = db.getDBConnection()) {
            conn.setAutoCommit(false);

            if (toSave.getId() == null || toSave.getId() == 0) {
                PreparedStatement ps = conn.prepareStatement("""
                    INSERT INTO ingredient(name, price, category, stock_quantity, stock_unit)
                    VALUES (?, ?, ?::ingredient_category_enum, ?, ?)
                    RETURNING id
                """);
                ps.setString(1, toSave.getName());
                ps.setDouble(2, toSave.getPrice());
                ps.setString(3, toSave.getCategory().name());
                ps.setDouble(4, toSave.getStockQuantity());
                ps.setString(5, toSave.getStockUnit());
                ResultSet rs = ps.executeQuery();
                rs.next();
                toSave.setId(rs.getInt(1));
            }

            if (toSave.getStockMovementList() != null) {
                PreparedStatement psMov = conn.prepareStatement("""
                    INSERT INTO stock_movement(ingredient_id, quantity, unit, movement_type, movement_date)
                    VALUES (?, ?, ?, ?::movement_type_enum, ?)
                    ON CONFLICT (id) DO NOTHING
                """);

                for (StockMovement m : toSave.getStockMovementList()) {
                    if (m.getId() == null) {
                        psMov.setInt(1, toSave.getId());
                        psMov.setDouble(2, m.getQuantity());
                        psMov.setString(3, m.getUnit());
                        psMov.setString(4, m.getMovementType().name());
                        psMov.setTimestamp(5, Timestamp.from(m.getMovementDate()));
                        psMov.executeUpdate();
                    }
                }
            }

            conn.commit();
            return toSave;

        } catch (SQLException e) {
            throw new RuntimeException("Erreur saveIngredient : " + e.getMessage());
        }
    }
    // =========================
    // SAVE ORDER
    // =========================
    public Order saveOrder(Order orderToSave) {
        try (Connection conn = db.getDBConnection()) {
            conn.setAutoCommit(false);

            PreparedStatement refPs = conn.prepareStatement(
                    "SELECT COUNT(*) FROM \"order\""
            );
            ResultSet refRs = refPs.executeQuery();
            refRs.next();
            int next = refRs.getInt(1) + 1;
            String reference = String.format("ORD%05d", next);

            PreparedStatement psOrder = conn.prepareStatement("""
            INSERT INTO "order"(reference, creation_datetime)
            VALUES (?, ?)
            RETURNING id
        """);
            psOrder.setString(1, reference);
            psOrder.setTimestamp(2, Timestamp.from(Instant.now()));
            ResultSet rs = psOrder.executeQuery();
            rs.next();

            orderToSave.setId(rs.getInt(1));
            orderToSave.setReference(reference);

            for (DishOrder dOrder : orderToSave.getDishOrders()) {
                Dish dish = findDishById(dOrder.getDish().getId());

                for (DishIngredient di : dish.getIngredients()) {
                    Ingredient ing = di.getIngredient();
                    double needed = di.getQuantity() * dOrder.getQuantity();

                    if (ing.getStockQuantity() < needed) {
                        throw new RuntimeException("Stock insuffisant pour " + ing.getName());
                    }

                    ing.consumeStock(needed);

                    PreparedStatement upd = conn.prepareStatement("""
                    UPDATE ingredient SET stock_quantity = ?
                    WHERE id = ?
                """);
                    upd.setDouble(1, ing.getStockQuantity());
                    upd.setInt(2, ing.getId());
                    upd.executeUpdate();
                }

                PreparedStatement psDO = conn.prepareStatement("""
                INSERT INTO dish_order(order_id, dish_id, quantity)
                VALUES (?, ?, ?)
            """);
                psDO.setInt(1, orderToSave.getId());
                psDO.setInt(2, dish.getId());
                psDO.setInt(3, dOrder.getQuantity());
                psDO.executeUpdate();
            }

            conn.commit();
            return orderToSave;

        } catch (Exception e) {
            throw new RuntimeException("Erreur saveOrder : " + e.getMessage());
        }
    }

    // =========================
    // FIND ORDER BY REFERENCE
    // =========================
    public Order findOrderByReference(String reference) {
        try (Connection conn = db.getDBConnection()) {

            PreparedStatement ps = conn.prepareStatement("""
            SELECT id, reference, creation_datetime
            FROM "order"
            WHERE reference = ?
        """);
            ps.setString(1, reference);

            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                throw new RuntimeException("Commande introuvable");
            }

            Order order = new Order();
            order.setId(rs.getInt("id"));
            order.setReference(rs.getString("reference"));
            order.setCreationDatetime(rs.getTimestamp("creation_datetime").toInstant());

            PreparedStatement psDO = conn.prepareStatement("""
            SELECT dish_id, quantity
            FROM dish_order
            WHERE order_id = ?
        """);
            psDO.setInt(1, order.getId());

            ResultSet rsDO = psDO.executeQuery();
            List<DishOrder> list = new ArrayList<>();

            while (rsDO.next()) {
                Dish d = findDishById(rsDO.getInt("dish_id"));
                list.add(new DishOrder(null, d, rsDO.getInt("quantity")));
            }
            order.setDishOrders(list);
            return order;

        } catch (SQLException e) {
            throw new RuntimeException("Erreur findOrderByReference : " + e.getMessage());
        }
    }
}
