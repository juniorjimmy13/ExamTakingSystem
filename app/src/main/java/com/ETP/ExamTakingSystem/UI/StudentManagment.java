/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ETP.ExamTakingSystem.UI;

/**
 *
 * @author OMEN 16
 */
import com.ETP.ExamTakingSystem.DatabaseManager;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.*;

public class StudentManagment {
    public static void showWindow() {
        Stage window = new Stage();
        window.setTitle("Manage Students");

        ListView<String> studentList = new ListView<>();
        Button addStudentBtn = new Button("Add Student");
        Button deleteStudentBtn = new Button("Delete Selected");

        loadStudents(studentList);

        addStudentBtn.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Add Student");
            dialog.setHeaderText("Enter Student Username:");
            dialog.setContentText("Username:");

            dialog.showAndWait().ifPresent(username -> {
                if (!username.isEmpty()) {
                    addStudent(username);
                    loadStudents(studentList);
                }
            });
        });

        deleteStudentBtn.setOnAction(e -> {
            String selected = studentList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                deleteStudent(selected);
                loadStudents(studentList);
            }
        });

        VBox layout = new VBox(10);
        layout.getChildren().addAll(studentList, addStudentBtn, deleteStudentBtn);

        Scene scene = new Scene(layout, 300, 400);
        window.setScene(scene);
        window.show();
    }

    private static void loadStudents(ListView<String> studentList) {
        studentList.getItems().clear();
        try (Connection conn = DatabaseManager.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT username FROM users WHERE role='student'")) {
            while (rs.next()) {
                studentList.getItems().add(rs.getString("username"));
            }
        } catch (SQLException e) {
            System.out.println("Error loading students: " + e.getMessage());
        }
    }

    private static void addStudent(String username) {
        String sql = "INSERT INTO users (username, password, role) VALUES (?, 'password123', 'student')";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error adding student: " + e.getMessage());
        }
    }

    private static void deleteStudent(String username) {
        String sql = "DELETE FROM users WHERE username = ?";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error deleting student: " + e.getMessage());
        }
    }
}

