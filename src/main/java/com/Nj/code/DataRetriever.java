package com.Nj.code;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
}