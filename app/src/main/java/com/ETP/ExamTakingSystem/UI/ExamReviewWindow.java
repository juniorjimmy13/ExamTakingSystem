package com.ETP.ExamTakingSystem.UI;

import com.ETP.ExamTakingSystem.DatabaseManager;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;

public class ExamReviewWindow {
    public static void showWindow(String studentUsername, String examTitle) {
        Stage window = new Stage();
        window.setTitle("Exam Review - " + examTitle);

        VBox layout = new VBox(10);
        layout.getChildren().add(new Label("Review of: " + examTitle));

        loadExamReview(studentUsername, examTitle, layout);

        ScrollPane scrollPane = new ScrollPane(layout);
        scrollPane.setFitToWidth(true);
        
        window.setOnCloseRequest(event -> {
    event.consume();// Prevents the window from closing immediately
    window.close();
    StudentResultsWindow.showWindow(studentUsername);
});


        window.setScene(new Scene(scrollPane, 500, 500));
        window.show();
    }

    private static void loadExamReview(String studentUsername, String examTitle, VBox layout) {
        String sql = "SELECT q.question_text, q.correct_option, sa.selected_option " +
                     "FROM student_answers sa " +
                     "JOIN questions q ON sa.question_id = q.id " +
                     "JOIN users u ON sa.student_id = u.id " +
                     "JOIN exams e ON sa.exam_id = e.id " +
                     "WHERE u.username = ? AND e.title = ?";

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentUsername);
            pstmt.setString(2, examTitle);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String question = rs.getString("question_text");
                String correctAnswer = rs.getString("correct_option");
                String studentAnswer = rs.getString("selected_option");

                Label questionLabel = new Label("Q: " + question);
                Label studentAnswerLabel = new Label("Your Answer: " + studentAnswer);
                Label correctAnswerLabel = new Label("Correct Answer: " + correctAnswer);

                if (studentAnswer.equals(correctAnswer)) {
                    studentAnswerLabel.setStyle("-fx-text-fill: green;"); // Correct answer in green
                } else {
                    studentAnswerLabel.setStyle("-fx-text-fill: red;"); // Incorrect answer in red
                }

                layout.getChildren().addAll(questionLabel, studentAnswerLabel, correctAnswerLabel, new Label(" "));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
