/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ETP.ExamTakingSystem;

/**
 *
 * @author OMEN 16
 */
import java.sql.*;

public class DatabaseManager {
    private static final String URL = "jdbc:mysql://localhost:3306/exam_system1"; // For XAMPP
    private static final String DB_URL = "jdbc:sqlite:exam_system.db"; // Local
    private static final String USER = "root";
    private static final String PASSWORD = ""; // Default XAMPP password is empty

    public static Connection connect() {
        try {
            // Load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establish connection for Xamp
            return DriverManager.getConnection(URL,USER,PASSWORD);
            
            //return DriverManager.getConnection(DB_URL) // for local
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return null;
        }
    }


    // Initialize database tables
    public static void initializeDatabase() {
        
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            //stmt.executeUpdate(sql);
            System.out.println("Database initialized successfully.");
        } catch (SQLException e) {
            System.out.println("Error initializing database: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        initializeDatabase();
    }
}

