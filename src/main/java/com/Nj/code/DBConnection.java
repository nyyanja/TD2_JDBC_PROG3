package com.Nj.code;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private final String URL = "jdbc:postgresql://localhost:5432/mini_dish_db";
    private final String USER = "mini_dish_manager";
    private  final String PASSWORD = "123456";

    public Connection getDBConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
