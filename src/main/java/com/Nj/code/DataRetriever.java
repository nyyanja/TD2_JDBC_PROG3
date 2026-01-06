package com.Nj.code;

import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {
    private DBConnection db ;
    public   DataRetriever(DBConnection  db){
        this.db = db;
    }

    public Dish findDishById  (Integer id){
        Connection conn = null;
        Dish dish = null;
        List<Ingredient> ingredients = new ArrayList<>();

        String dishSql = "SELECT * FROM Dish WHERE id = ?";
        String ingredientSql = "SELECT * FROM Ingredient WHERE id = ?";

        try{
            conn = db.getDBConnection();

            //dish
            PreparedStatement psDish = conn.prepareStatement(dishSql);
            psDish.setInt(1,id);
            ResultSet rsDish = psDish.executeQuery();

            if(rsDish.next()){
                dish = new Dish(
                        rsDish.getInt("id"),
                        rsDish.getString("name"),
                        DishType.valueOf(rsDish.getString("dish_type")),
                        ingredients
                );
            }
            //ingredients
            PreparedStatement psIng = conn.prepareStatement(ingredientSql);
            psIng.setInt(1,id);
            ResultSet rsIng = psIng.executeQuery();

            while (rsIng.next()){
                Ingredient  ingredient = new Ingredient(
                        rsIng.getInt("id"),
                        rsIng.getString("name"),
                        rsIng.getDouble("price"),
                        Category.valueOf(rsIng.getString("category")),
                        dish
                );
                ingredients.add(ingredient);
            }
        }catch (SQLException e){
            throw  new RuntimeException("Erreur lors de la recuperation du plat");
        }finally {
            try{
                if (conn != null) conn.close();
            }catch (SQLException e){
                throw new RuntimeException("Erreur de la fermeture de la connexion");
            }
        }
        return dish;
    }

    public  List<Ingredient> findIngredient (int  page,int size){
        Connection conn =  null;
        List<Ingredient> ingredients = new ArrayList<>();
        int offset = (page -1) * size;

        String sql = "SELECT * FROM Ingredient LIMIT ? OFFSET ?";

        try{
            conn = db.getDBConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1,size);
            ps.setInt(2,offset);

            ResultSet rs = ps.executeQuery();

            while (rs.next()){
                Ingredient ingredient = new Ingredient(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        Category.valueOf(rs.getString("category")),
                        null
                );
                ingredients.add(ingredient);
            }
        }catch (SQLException e){
            throw new RuntimeException("Erreur lors de la pagination des ingredients");
        }finally {
            try {
                if (conn != null) conn.close();
            }catch (SQLException e){
                throw new RuntimeException("Erreur de la fermeture connexion");
            }
        }
        return ingredients;
    }

    public List<Ingredient> createIngredients(List<Ingredient> newIngredient){
        Connection conn =  null;

        String checkSql = "SELECT COUNT(*) FROM Ingredient WHERE name = ?";
        String insertsql = "INSERT INTO Ingredient (name,prive, category, id_dish) VALUES (?,?,?,?)";

        try {
            //connexion et debut transaction
            conn = db.getDBConnection();
            conn.setAutoCommit(false);

            // boucle pour verifier et inserer
            for (Ingredient ing : newIngredient) {
                try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                    psCheck.setString(1, ing.getName());
                    ResultSet rs = psCheck.executeQuery();
                    if (rs.next() && rs.getInt(1) > 0) {
                        throw new RuntimeException("Ingredient déjà existant : " + ing.getName());
                    }
                }


                //insertion
                try(PreparedStatement psInsert = conn.prepareStatement(insertsql)){
                    psInsert.setString(1,ing.getName());
                    psInsert.setDouble(2,ing.getPrice());
                    psInsert.setString(3,ing.getCategoryEnum().name());

                    if (ing.getDish() != null){
                        psInsert.setInt(4,ing.getDish().getId());
                    }else {
                        psInsert.setNull(4, Types.INTEGER);
                    }
                    psInsert.executeUpdate();
                }
            }
            //commit si tout est ok
            conn.commit();
        }catch (Exception e){
            //rollback si erreur
            try{
                if (conn!= null) conn.rollback();
            }catch (SQLException ex){
                throw new RuntimeException("Erreur rollback");
            }
            //propagation de l'erreur
            throw  new RuntimeException( "Erreur lors de la creation des ingretiens : " + e.getMessage());
        }finally {
            //fermeture de la connection
            try {
                if (conn != null) conn.close();
            }catch (SQLException e){
                throw new RuntimeException("Erreur de la fermeture connexion");
            }
        }
        return newIngredient;
    }

    public Dish saveDish( Dish dishToSave){
        Connection conn = null;

        String insertDishSql = "INSERT INTO Dish(name,dish_type) VALUES (?,?) RETURNING id";
        String updateDishSql = "UPDATE Dish SET name = ? , dish_type = ? WHERE id = ? ";
        String clearIngredientSql = "UPDATE Ingredient SET id_dish = NULL WHERE id_dish = ? ";
        String attachIngredientSql = "UPDATE Ingredient SET id_dish =? WHERE name = ? ";

        try {
                conn = db.getDBConnection();
                conn.setAutoCommit(false);

                //INSERT
                if (dishToSave.getId() == 0){
                    PreparedStatement ps = conn.prepareStatement(insertDishSql);
                    ps.setString( 1, dishToSave.getName());
                    ps.setString(2,dishToSave.getDishTypeEnum().name());

                    ResultSet rs = ps.executeQuery();
                    if (rs.next()){
                        dishToSave.setId(rs.getInt(1));
                    }
                }
                //UPDATE
                else {
                    PreparedStatement ps = conn.prepareStatement(updateDishSql);
                    ps.setString(1,dishToSave.getName());
                    ps.setString(2,dishToSave.getDishTypeEnum().name());
                    ps.setInt(3,dishToSave.getId());

                    //suprimer anciennes associations
                    PreparedStatement psClear = conn.prepareStatement(clearIngredientSql);
                    psClear.setInt(1,dishToSave.getId());
                    psClear.executeUpdate();
                }
                // associer les nouveaux ingredients
                for (Ingredient ing : dishToSave.getIngredients()){
                    PreparedStatement psAttach = conn.prepareStatement(attachIngredientSql);
                    psAttach.setInt(1,dishToSave.getId());
                    psAttach.setString(2,ing.getName());
                    psAttach.executeUpdate();
                }

                conn.commit();
                return dishToSave;
        }catch (Exception e){
                    try {
                        if (conn!= null) conn.rollback();
                    }catch (SQLException ex){
                        throw new RuntimeException("Erreur rollback");
                    }
                throw  new RuntimeException("Erreur daveDish");
            }finally {
                try {
                    if (conn!= null) conn.close();
                }catch (SQLException e){
                    throw new RuntimeException("Erreur lors de la fermeture connexion");
                }
        }
    }
}