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
        
        Button assignExamBtn = new Button("Assign Exam to Student");
       

        assignExamBtn.setOnAction(e -> {
        String selectedStudent = studentList.getSelectionModel().getSelectedItem();
            if (selectedStudent != null) {
                   assignExamToStudent(selectedStudent);
            }    
            else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a student first.");
                alert.show();
                }
            });


        VBox layout = new VBox(10);
        layout.getChildren().addAll(studentList, addStudentBtn, deleteStudentBtn);
        layout.getChildren().add(assignExamBtn);

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
    private static void assignExamToStudent(String username) {
    try (Connection conn = DatabaseManager.connect()) {
        // Get all exams
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT id, title FROM exams");

        ChoiceDialog<String> dialog = new ChoiceDialog<>();
        while (rs.next()) {
            dialog.getItems().add(rs.getInt("id") + " - " + rs.getString("title"));
        }
        dialog.setTitle("Assign Exam");
        dialog.setHeaderText("Select an exam for: " + username);

        dialog.showAndWait().ifPresent(selected -> {
            int examId = Integer.parseInt(selected.split(" - ")[0]);
            try {
                // Get student ID
                PreparedStatement getIdStmt = conn.prepareStatement("SELECT id FROM users WHERE username = ?");
                getIdStmt.setString(1, username);
                ResultSet idRs = getIdStmt.executeQuery();
                if (idRs.next()) {
                    int studentId = idRs.getInt("id");

                    // Link student to exam
                    PreparedStatement insertStmt = conn.prepareStatement("INSERT OR IGNORE INTO student_exams (student_id, exam_id) VALUES (?, ?)");
                    insertStmt.setInt(1, studentId);
                    insertStmt.setInt(2, examId);
                    insertStmt.executeUpdate();
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Exam assigned successfully.");
                    alert.show();
                }
            } catch (SQLException ex) {
                System.out.println("Error assigning exam: " + ex.getMessage());
            }
        });

    } catch (SQLException e) {
        System.out.println("Error loading exams: " + e.getMessage());
    }
}

}

