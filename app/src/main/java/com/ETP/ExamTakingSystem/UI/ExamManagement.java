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

public class ExamManagement {
    public static void showWindow() {
        Stage window = new Stage();
        window.setTitle("Manage Exams");

        ListView<String> examList = new ListView<>();
        Button addExamBtn = new Button("Create Exam");
        Button addQuestionBtn = new Button("Add Question to Exam");

        loadExams(examList);

        addExamBtn.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Create Exam");
            dialog.setHeaderText("Enter Exam Title:");
            dialog.setContentText("Title:");

            dialog.showAndWait().ifPresent(title -> {
                if (!title.isEmpty()) {
                    createExam(title);
                    loadExams(examList);
                }
            });
        });

        addQuestionBtn.setOnAction(e -> {
            String selectedExam = examList.getSelectionModel().getSelectedItem();
            if (selectedExam != null) {
                QuestionManagement.showWindow(selectedExam);
            }
        });

        VBox layout = new VBox(10);
        layout.getChildren().addAll(examList, addExamBtn, addQuestionBtn);

        Scene scene = new Scene(layout, 300, 400);
        window.setScene(scene);
        window.show();
    }

    private static void loadExams(ListView<String> examList) {
        examList.getItems().clear();
        try (Connection conn = DatabaseManager.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT title FROM exams")) {
            while (rs.next()) {
                examList.getItems().add(rs.getString("title"));
            }
        } catch (SQLException e) {
            System.out.println("Error loading exams: " + e.getMessage());
        }
    }

    private static void createExam(String title) {
        String sql = "INSERT INTO exams (title, duration, teacher_id) VALUES (?, 60, 1)";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error creating exam: " + e.getMessage());
        }
    }
}
