package com.ETP.ExamTakingSystem.UI;

import com.ETP.ExamTakingSystem.DatabaseManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.*;
import java.util.*;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class ExamTakingWindow {
    private static Map<Integer, ToggleGroup> questionAnswers = new HashMap<>();
    private static Timeline timer;
    private static boolean submitted = false;

    public static void showWindow(String studentUsername, String examTitle) {
        Stage window = new Stage();
        window.setTitle("Exam: " + examTitle);

        VBox layout = new VBox(10);
        Label timerLabel = new Label("Time Left: ");

        // Load questions
        List<Question> questions = loadQuestions(examTitle, layout);

        // Timer setup (e.g., 10 minutes = 600 seconds)
        IntegerProperty timeLeft = new SimpleIntegerProperty(600); // 10 minutes

        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeLeft.set(timeLeft.get() - 1);
             timerLabel.setText("Time Left: " + timeLeft.get() + "s");
             if (timeLeft.get() <= 0) {
                timer.stop();
                 submitAnswers(studentUsername, examTitle);
            window.close();
            }
            }));
            timer.setCycleCount(600);
            timer.play();
       

        Button submitBtn = new Button("Submit Answers");
        submitBtn.setOnAction(e -> {
            timer.stop();
            submitAnswers(studentUsername, examTitle);
            submitted = true;
            window.close();
        });

        layout.getChildren().add(0, timerLabel);
        layout.getChildren().add(submitBtn);

        window.setScene(new Scene(new ScrollPane(layout), 1920, 1080));
        window.show();
        
        window.setOnCloseRequest(e -> {
            
    e.consume(); // Prevent closing
    Alert alert = new Alert(Alert.AlertType.WARNING, "Closing the exam will submit it automatically!");
    alert.showAndWait();
    submitAnswers(studentUsername, examTitle);
    StudentDashboard.showWindow(studentUsername);
    window.close();
            
});

IntegerProperty warnings = new SimpleIntegerProperty(0); // Track cheating attempts


// Detect Alt+Tab or Loss of Focus
window.focusedProperty().addListener((obs, oldValue, newValue) -> {
    if (!newValue) { // Window lost focus
        warnings.set(warnings.get() + 1);
        checkWarnings(window, studentUsername, examTitle, warnings);
    }
});

    }

    private static List<Question> loadQuestions(String examTitle, VBox layout) {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT q.id, q.question_text, q.option_a, q.option_b, q.option_c, q.option_d FROM questions q " +
                     "JOIN exams e ON q.exam_id = e.id WHERE e.title = ?";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, examTitle);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int qid = rs.getInt("id");
                String qtext = rs.getString("question_text");
                String a = rs.getString("option_a");
                String b = rs.getString("option_b");
                String c = rs.getString("option_c");
                String d = rs.getString("option_d");

                Question q = new Question(qid, qtext, a, b, c, d);
                questions.add(q);

                Label questionLabel = new Label(qtext);
                ToggleGroup tg = new ToggleGroup();

                RadioButton ra = new RadioButton("A: " + a);
                RadioButton rb = new RadioButton("B: " + b);
                RadioButton rc = new RadioButton("C: " + c);
                RadioButton rd = new RadioButton("D: " + d);

                ra.setToggleGroup(tg);
                rb.setToggleGroup(tg);
                rc.setToggleGroup(tg);
                rd.setToggleGroup(tg);

                layout.getChildren().addAll(questionLabel, ra, rb, rc, rd);
                questionAnswers.put(qid, tg);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return questions;
    }

    private static void submitAnswers(String studentUsername, String examTitle) {
    try (Connection conn = DatabaseManager.connect()) {
        int studentId = getId(conn, "users", "username", studentUsername);
        int examId = getId(conn, "exams", "title", examTitle);

        int correctCount = 0;
        int totalQuestions = questionAnswers.size();

        for (Map.Entry<Integer, ToggleGroup> entry : questionAnswers.entrySet()) {
            int questionId = entry.getKey();
            ToggleGroup group = entry.getValue();
            RadioButton selected = (RadioButton) group.getSelectedToggle();
            
            if (selected != null) {
                String selectedAnswer = selected.getText().substring(0, 1); // Extract "A", "B", etc.

                // Get correct answer from database
                String correctAnswer = getCorrectAnswer(conn, questionId);

                // Check if the answer is correct
                if (selectedAnswer.equals(correctAnswer)) {
                    correctCount++;
                }

                // Store student answer
                String sql = "INSERT INTO student_answers (student_id, exam_id, question_id, selected_option) VALUES (?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, studentId);
                    pstmt.setInt(2, examId);
                    pstmt.setInt(3, questionId);
                    pstmt.setString(4, selectedAnswer);
                    pstmt.executeUpdate();
                }
            }
        }

        // Calculate percentage score
        int score = (int) (((double) correctCount / totalQuestions) * 100);

        // Store the score in student_results
        String insertResultSql = "INSERT INTO student_results (student_id, exam_id, score) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertResultSql)) {
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, examId);
            pstmt.setInt(3, score);
            pstmt.executeUpdate();
        }

        // Show confirmation
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Exam submitted! Your score: " + score + "%");
        alert.showAndWait();
        StudentDashboard.showWindow(studentUsername);
        
    } catch (SQLException e) {
        e.printStackTrace();
    }
}
    // Helper Method for anti cheating features
    private static void checkWarnings(Stage window, String studentUsername, String examTitle, IntegerProperty warnings) {
    if (warnings.get() == 1 ) {
        Alert warning = new Alert(Alert.AlertType.WARNING, "Warning: Do not switch windows! Next time, the exam will be auto-submitted.");
        warning.showAndWait();
    } else if (warnings.get() >= 2) {
        Alert alert = new Alert(Alert.AlertType.ERROR, "You switched windows again. Your exam has been auto-submitted.");
        alert.showAndWait();
        submitAnswers(studentUsername, examTitle);
        //window.close();
    }
}


// Helper method to get the correct answer for a question
private static String getCorrectAnswer(Connection conn, int questionId) throws SQLException {
    String sql = "SELECT correct_option FROM questions WHERE id = ?";
    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setInt(1, questionId);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            return rs.getString("correct_option");
        }
    }
    return "";
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

    private static class Question {
        int id;
        String text, a, b, c, d;

        Question(int id, String text, String a, String b, String c, String d) {
            this.id = id;
            this.text = text;
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
        }
    }
}
