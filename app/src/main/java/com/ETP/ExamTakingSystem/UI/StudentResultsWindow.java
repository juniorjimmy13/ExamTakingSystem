/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ETP.ExamTakingSystem.UI;

import com.ETP.ExamTakingSystem.DatabaseManager;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;

public class StudentResultsWindow {
    public static void showWindow(String studentUsername) {
        Stage window = new Stage();
        window.setTitle("Exam Results - " + studentUsername);

        ListView<String> resultsList = new ListView<>();
        loadStudentResults(studentUsername, resultsList);
        
        resultsList.setOnMouseClicked(event -> {
    if (event.getClickCount() == 2) { // Detect double-click
        String selectedExam = resultsList.getSelectionModel().getSelectedItem();
        if (selectedExam != null) {
            String examTitle = selectedExam.split(":")[0].trim(); // Extract exam title
            window.close();
            ExamReviewWindow.showWindow(studentUsername, examTitle);
        }
    }
    
});

        Button backButton = new Button("Back");
        backButton.setOnAction(e->{
            window.close();
            StudentDashboard.showWindow(studentUsername);
        });
        VBox layout = new VBox(10);
        layout.getChildren().addAll(new Label("Your Exam Results:"), resultsList,backButton);
        window.setScene(new Scene(layout, 400, 300));
        window.show();
    }

    private static void loadStudentResults(String studentUsername, ListView<String> listView) {
        listView.getItems().clear();
        String sql = "SELECT e.title, sr.score FROM student_results sr " +
                     "JOIN exams e ON sr.exam_id = e.id " +
                     "JOIN users u ON sr.student_id = u.id " +
                     "WHERE u.username = ?";

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentUsername);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String examTitle = rs.getString("title");
                int score = rs.getInt("score");
                listView.getItems().add(examTitle + ": " + score + "%");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

