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
    private static final String URL = "jdbc:sqlite:exam_system.db";

    // Establish a connection to the SQLite database
    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL);
            System.out.println("Connected to database.");
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
        }
        return conn;
    }

    // Initialize database tables
    public static void initializeDatabase() {
        String sql = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                password TEXT NOT NULL,
                role TEXT CHECK(role IN ('student', 'teacher')) NOT NULL
            );

            CREATE TABLE IF NOT EXISTS exams (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                duration INTEGER NOT NULL,
                teacher_id INTEGER,
                FOREIGN KEY (teacher_id) REFERENCES users(id)
            );

            CREATE TABLE IF NOT EXISTS questions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                exam_id INTEGER,
                question_text TEXT NOT NULL,
                option_a TEXT NOT NULL,
                option_b TEXT NOT NULL,
                option_c TEXT NOT NULL,
                option_d TEXT NOT NULL,
                correct_option TEXT CHECK(correct_option IN ('A', 'B', 'C', 'D')) NOT NULL,
                FOREIGN KEY (exam_id) REFERENCES exams(id)
            );
            
            CREATE TABLE IF NOT EXISTS student_exams (
                student_id INTEGER,
                exam_id INTEGER,
                PRIMARY KEY (student_id, exam_id),
                FOREIGN KEY (student_id) REFERENCES users(id),
                FOREIGN KEY (exam_id) REFERENCES exams(id)
            );

            CREATE TABLE IF NOT EXISTS results (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                student_id INTEGER,
                exam_id INTEGER,
                score INTEGER,
                FOREIGN KEY (student_id) REFERENCES users(id),
                FOREIGN KEY (exam_id) REFERENCES exams(id)
            );
        """;

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("Database initialized successfully.");
        } catch (SQLException e) {
            System.out.println("Error initializing database: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        initializeDatabase();
    }
}

