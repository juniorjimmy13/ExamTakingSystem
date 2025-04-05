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
        Button viewResultsBtn = new Button("View Results");

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


        viewResultsBtn.setOnAction(e -> {
            String selectedStudent = studentList.getSelectionModel().getSelectedItem();
            if (selectedStudent != null) {
                showStudentResults(selectedStudent);
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a student first.");
                alert.show();
            }
        });

        VBox layout = new VBox(10);
        layout.getChildren().addAll(studentList, addStudentBtn, deleteStudentBtn, viewResultsBtn);
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
                    PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO student_exams (student_id, exam_id) VALUES (?, ?)");
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
    private static void showStudentResults(String studentUsername) {
        Stage resultWindow = new Stage();
        resultWindow.setTitle("Results for: " + studentUsername);

        ListView<String> resultList = new ListView<>();
        loadStudentResults(studentUsername, resultList);

        resultList.setOnMouseClicked(e -> {
            String selectedResult = resultList.getSelectionModel().getSelectedItem();
            if (selectedResult != null) {
                int examId = Integer.parseInt(selectedResult.split(" - ")[0]);
                showDetailedResults(studentUsername, examId);
            }
        });

        VBox layout = new VBox(10, resultList);
        Scene scene = new Scene(layout, 300, 400);
        resultWindow.setScene(scene);
        resultWindow.show();
    }

    private static void loadStudentResults(String studentUsername, ListView<String> resultList) {
        resultList.getItems().clear();
        try (Connection conn = DatabaseManager.connect()) {
            int studentId = getId(conn, "users", "username", studentUsername);
            String sql = "SELECT e.id, e.title, sr.score FROM student_results sr " +
                         "JOIN exams e ON sr.exam_id = e.id WHERE sr.student_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, studentId);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    int examId = rs.getInt("id");
                    String examTitle = rs.getString("title");
                    int score = rs.getInt("score");
                    resultList.getItems().add(examId + " - " + examTitle + " | Score: " + score + "%");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void showDetailedResults(String studentUsername, int examId) {
        Stage detailsWindow = new Stage();
        detailsWindow.setTitle("Exam Review");

        ListView<String> answersList = new ListView<>();
        loadExamAnswers(studentUsername, examId, answersList);

        VBox layout = new VBox(10, answersList);
        Scene scene = new Scene(layout, 400, 500);
        detailsWindow.setScene(scene);
        detailsWindow.show();
    }

    private static void loadExamAnswers(String studentUsername, int examId, ListView<String> answersList) {
        answersList.getItems().clear();
        try (Connection conn = DatabaseManager.connect()) {
            int studentId = getId(conn, "users", "username", studentUsername);
            String sql = "SELECT q.question_text, q.correct_option, sa.selected_option FROM student_answers sa " +
                         "JOIN questions q ON sa.question_id = q.id WHERE sa.student_id = ? AND sa.exam_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, studentId);
                pstmt.setInt(2, examId);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    String question = rs.getString("question_text");
                    String correct = rs.getString("correct_option");
                    String chosen = rs.getString("selected_option");

                    String status = chosen.equals(correct) ? "✔ Correct" : "✘ Incorrect";
                    answersList.getItems().add(question + "\nYour Answer: " + chosen + " | Correct: " + correct + " " + status);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static int getId(Connection conn, String table, String field, String value) throws SQLException {
        String sql = "SELECT id FROM " + table + " WHERE " + field + "=?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, value);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("id");
        }
        return -1;
    }
}


