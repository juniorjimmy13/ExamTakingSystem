package com.ETP.ExamTakingSystem.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author OMEN 16
 */
public class sqLiteServer {
    private static final int PORT = 5000;
    private static final String DB_URL = "jdbc:sqlite:exam_system.db"; // Path to your SQLite database

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("SQLite Server running on port " + PORT + "...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                String query = in.readLine(); // Read SQL query from client
                System.out.println("Executing query: " + query);

                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(query)) {

                    StringBuilder result = new StringBuilder();
                    int columns = rs.getMetaData().getColumnCount();

                    while (rs.next()) {
                        for (int i = 1; i <= columns; i++) {
                            result.append(rs.getString(i)).append("\t");
                        }
                        result.append("\n");
                    }

                    out.println(result.toString()); // Send result back to client
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
