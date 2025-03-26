/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ETP.ExamTakingSystem.Server;

/**
 *
 * @author OMEN 16
 */
import com.ETP.ExamTakingSystem.DatabaseManager;
import java.io.*;
import java.net.*;
import java.sql.*;

public class ClientHandler extends Thread {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println("Connected to Exam Server");

            while (true) {
                String request = in.readLine();
                if (request == null) break;

                System.out.println("Received: " + request);
                processRequest(request);
            }

        } catch (IOException e) {
            System.out.println("Client disconnected.");
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Error closing socket: " + e.getMessage());
            }
        }
    }

    private void processRequest(String request) {
        if (request.startsWith("LOGIN")) {
            String[] parts = request.split(" ");
            if (parts.length == 3) {
                String username = parts[1];
                String password = parts[2];
                boolean success = authenticateUser(username, password);
                out.println(success ? "LOGIN_SUCCESS" : "LOGIN_FAILED");
            } else {
                out.println("ERROR Invalid login format.");
            }
        } else {
            out.println("ERROR Unknown command.");
        }
    }

    private boolean authenticateUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.out.println("Authentication error: " + e.getMessage());
            return false;
        }
    }
}