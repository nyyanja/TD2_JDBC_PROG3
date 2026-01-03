package com.Nj.code;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private final String JDBC_URL = System.getenv("JDDBC_URL");
    private final String USERNAME =System.getenv("USERNAME");
    private  final String PASSWORD =System.getenv("PASSWORD");

    public  Connection getDBConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
    }
}