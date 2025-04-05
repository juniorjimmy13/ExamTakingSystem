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
import javafx.scene.layout.HBox;

public class QuestionManagement {
    public static void showWindow(String examTitle) {
        Stage window = new Stage();
        window.setTitle("Add Question to " + examTitle);

        TextField questionField = new TextField();
        questionField.setPromptText("Enter question text");

        TextField[] options = new TextField[4];
        for (int i = 0; i < 4; i++) {
            options[i] = new TextField();
            options[i].setPromptText("Option " + (char) ('A' + i));
        }

        ComboBox<String> correctAnswer = new ComboBox<>();
        correctAnswer.getItems().addAll("A", "B", "C", "D");

        Button saveBtn = new Button("Save Question");
        Button viewQuestionsBtn = new Button("View Questions for This Exam");
        viewQuestionsBtn.setOnAction(e -> showQuestionList(examTitle));
        saveBtn.setOnAction(e -> {
            saveQuestion(examTitle, questionField.getText(), options, correctAnswer.getValue());
        });

        VBox layout = new VBox(10);
        
        layout.getChildren().addAll(questionField, options[0], options[1], options[2], options[3], correctAnswer, saveBtn, viewQuestionsBtn);

        
        
        Scene scene = new Scene(layout, 400, 300);
        window.setScene(scene);
        window.show();
    }

    private static void saveQuestion(String examTitle, String question, TextField[] options, String correct) {
    if (question.isEmpty() || correct == null || options[0].getText().isEmpty() || options[1].getText().isEmpty() ||
        options[2].getText().isEmpty() || options[3].getText().isEmpty()) {
        Alert alert = new Alert(Alert.AlertType.ERROR, "Please fill in all fields and select the correct answer.");
        alert.show();
        return;
    }

    try (Connection conn = DatabaseManager.connect()) {
        // Step 1: Get exam_id
        int examId = -1;
        String getExamIdSQL = "SELECT id FROM exams WHERE title = ?";
        try (PreparedStatement stmt = conn.prepareStatement(getExamIdSQL)) {
            stmt.setString(1, examTitle);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                examId = rs.getInt("id");
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Exam not found.");
                alert.show();
                return;
            }
        }

        // Step 2: Insert question
        String insertSQL = "INSERT INTO questions (exam_id, question_text, option_a, option_b, option_c, option_d, correct_option) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setInt(1, examId);
            pstmt.setString(2, question);
            pstmt.setString(3, options[0].getText());
            pstmt.setString(4, options[1].getText());
            pstmt.setString(5, options[2].getText());
            pstmt.setString(6, options[3].getText());
            pstmt.setString(7, correct);
            pstmt.executeUpdate();
        }

        Alert success = new Alert(Alert.AlertType.INFORMATION, "Question added successfully.");
        success.show();

    } catch (SQLException e) {
        e.printStackTrace();
        Alert error = new Alert(Alert.AlertType.ERROR, "Error saving question: " + e.getMessage());
        error.show();
    }
    }
    private static void showQuestionList(String examTitle) {
    Stage window = new Stage();
    window.setTitle("Questions for " + examTitle);

    VBox layout = new VBox(10);
    layout.getChildren().add(new Label("Questions:"));

    try (Connection conn = DatabaseManager.connect()) {
        // Get exam_id first
        int examId = -1;
        String getExamIdSQL = "SELECT id FROM exams WHERE title = ?";
        try (PreparedStatement stmt = conn.prepareStatement(getExamIdSQL)) {
            stmt.setString(1, examTitle);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                examId = rs.getInt("id");
            } else {
                new Alert(Alert.AlertType.ERROR, "Exam not found.").show();
                return;
            }
        }

        // Get questions
        String getQuestionsSQL = "SELECT * FROM questions WHERE exam_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(getQuestionsSQL)) {
            pstmt.setInt(1, examId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int qid = rs.getInt("id");
                String text = rs.getString("question_text");

                HBox questionBox = new HBox(10);
                Label qLabel = new Label(text);
                Button editBtn = new Button("Edit");
                Button deleteBtn = new Button("Delete");

                editBtn.setOnAction(e -> openEditWindow(qid, examTitle));
                deleteBtn.setOnAction(e -> {
                    deleteQuestion(qid);
                    window.close();
                    showQuestionList(examTitle); // Refresh list
                });

                questionBox.getChildren().addAll(qLabel, editBtn, deleteBtn);
                layout.getChildren().add(questionBox);
            }
        }

    } catch (SQLException e) {
        e.printStackTrace();
        new Alert(Alert.AlertType.ERROR, "Error loading questions: " + e.getMessage()).show();
    }

    Scene scene = new Scene(new ScrollPane(layout), 500, 400);
    window.setScene(scene);
    window.show();
}
private static void openEditWindow(int questionId, String examTitle) {
    Stage editWindow = new Stage();
    editWindow.setTitle("Edit Question");

    TextField questionField = new TextField();
    TextField[] options = new TextField[4];
    for (int i = 0; i < 4; i++) {
        options[i] = new TextField();
    }
    ComboBox<String> correctBox = new ComboBox<>();
    correctBox.getItems().addAll("A", "B", "C", "D");

    // Load question data
    try (Connection conn = DatabaseManager.connect()) {
        String sql = "SELECT * FROM questions WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, questionId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                questionField.setText(rs.getString("question_text"));
                options[0].setText(rs.getString("option_a"));
                options[1].setText(rs.getString("option_b"));
                options[2].setText(rs.getString("option_c"));
                options[3].setText(rs.getString("option_d"));
                correctBox.setValue(rs.getString("correct_option"));
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    Button saveBtn = new Button("Save Changes");
    saveBtn.setOnAction(e -> {
        try (Connection conn = DatabaseManager.connect()) {
            String updateSQL = "UPDATE questions SET question_text=?, option_a=?, option_b=?, option_c=?, option_d=?, correct_option=? WHERE id=?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {
                pstmt.setString(1, questionField.getText());
                pstmt.setString(2, options[0].getText());
                pstmt.setString(3, options[1].getText());
                pstmt.setString(4, options[2].getText());
                pstmt.setString(5, options[3].getText());
                pstmt.setString(6, correctBox.getValue());
                pstmt.setInt(7, questionId);
                pstmt.executeUpdate();
            }

            new Alert(Alert.AlertType.INFORMATION, "Question updated successfully.").show();
            editWindow.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    });

    VBox layout = new VBox(10, questionField, options[0], options[1], options[2], options[3], correctBox, saveBtn);
    Scene scene = new Scene(layout, 400, 300);
    editWindow.setScene(scene);
    editWindow.show();
}
private static void deleteQuestion(int questionId) {
    try (Connection conn = DatabaseManager.connect()) {
        String sql = "DELETE FROM questions WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, questionId);
            stmt.executeUpdate();
        }
    } catch (SQLException e) {
        e.printStackTrace();
        new Alert(Alert.AlertType.ERROR, "Error deleting question: " + e.getMessage()).show();
    }
}


}