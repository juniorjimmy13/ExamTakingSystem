/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ETP.ExamTakingSystem.UI;

import com.ETP.ExamTakingSystem.DatabaseManager;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ResultViewer {
    public static void showWindow(int studentId) {
        Stage window = new Stage();
        window.setTitle("Your Results");

        ListView<String> resultList = new ListView<>();

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT e.title, r.score FROM results r JOIN exams e ON r.exam_id = e.id WHERE r.student_id = ?"
             )) {
            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String examTitle = rs.getString("title");
                int score = rs.getInt("score");
                resultList.getItems().add("Exam: " + examTitle + " | Score: " + score + "%");
            }
        } catch (Exception e) {
            System.out.println("Error loading results: " + e.getMessage());
        }

        VBox layout = new VBox(10, resultList);
        Scene scene = new Scene(layout, 400, 300);
        window.setScene(scene);
        window.show();
    }
}

