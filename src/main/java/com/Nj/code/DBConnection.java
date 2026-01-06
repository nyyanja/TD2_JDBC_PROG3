package com.Nj.code;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private final String JDBC_URL = "jdbc:postgresql://localhost:5432/mini_dish_db";
    private final String USERNAME = "mini_dish_manager";
    private final String PASSWORD = "mini123";

    public Connection getDBConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
    }

    public void close() {}
}
