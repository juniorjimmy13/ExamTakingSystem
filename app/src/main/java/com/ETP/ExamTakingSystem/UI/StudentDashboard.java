package com.ETP.ExamTakingSystem.UI;

import com.ETP.ExamTakingSystem.DatabaseManager;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.*;

public class StudentDashboard {
    public static void showWindow(String studentUsername) {
        Stage window = new Stage();
        window.setTitle("Welcome, " + studentUsername);

        ListView<String> assignedExamsList = new ListView<>();
        Button takeExamBtn = new Button("Take Selected Exam");
        
       Button viewResultsBtn = new Button("View Results");

        viewResultsBtn.setOnAction(e -> {
            window.close();
            StudentResultsWindow.showWindow(studentUsername);
            });
        
        loadAssignedExams(studentUsername, assignedExamsList);

        takeExamBtn.setOnAction(e -> {
            String selectedExam = assignedExamsList.getSelectionModel().getSelectedItem();
            if (selectedExam != null) {
                window.close();
                ExamTakingWindow.showWindow(studentUsername, selectedExam);
            }
        });

        VBox layout = new VBox(10);
        layout.getChildren().addAll(new Label("Assigned Exams:"), assignedExamsList, takeExamBtn,viewResultsBtn);
        window.setScene(new Scene(layout, 400, 300));
        window.show();
    }

    private static void loadAssignedExams(String studentUsername, ListView<String> listView) {
        listView.getItems().clear();
        String sql = "SELECT e.title FROM exams e " +
                     "JOIN student_exams se ON e.id = se.exam_id " +
                     "JOIN users u ON se.student_id = u.id " +
                     "WHERE u.username = ?";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentUsername);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                listView.getItems().add(rs.getString("title"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
