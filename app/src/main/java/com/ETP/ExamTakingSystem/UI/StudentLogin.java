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

public class StudentLogin {
    public static void showWindow() {
        Stage window = new Stage();
        window.setTitle("Student Login");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter username");

        Button loginBtn = new Button("Login");

        loginBtn.setOnAction(e -> {
            String username = usernameField.getText();
            if (authenticate(username)) {
                window.close();
                
                // we pass through the student username so that we can reference it for the rest of the student session
                StudentDashboard.showWindow(username);
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid Student Username");
                alert.show();
            }
        });

        VBox layout = new VBox(10);
        layout.getChildren().addAll(usernameField, loginBtn);
        window.setScene(new Scene(layout, 300, 150));
        window.show();
    }

    private static boolean authenticate(String username) {
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM users WHERE username=? AND role='student'")) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}