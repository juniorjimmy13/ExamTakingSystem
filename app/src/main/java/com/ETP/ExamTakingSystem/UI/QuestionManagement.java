/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ETP.ExamTakingSystem.UI;

/**
 *
 * @author OMEN 16
 */
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.*;

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

        saveBtn.setOnAction(e -> {
            saveQuestion(examTitle, questionField.getText(), options, correctAnswer.getValue());
        });

        VBox layout = new VBox(10);
        layout.getChildren().addAll(questionField, options[0], options[1], options[2], options[3], correctAnswer, saveBtn);

        Scene scene = new Scene(layout, 400, 300);
        window.setScene(scene);
        window.show();
    }

    private static void saveQuestion(String examTitle, String question, TextField[] options, String correct) {
        // Save question logic
    }
}
