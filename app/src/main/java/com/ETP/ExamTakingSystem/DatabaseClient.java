/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ETP.ExamTakingSystem;

import java.io.*;
import java.net.*;
//import java.sql.ResultSet;
//import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class DatabaseClient{
    private static final String SERVER_IP = "192.168.181.114"; // Change to your server's IP
    private static final int SERVER_PORT = 5000;

    /**
     * Executes a SQL query (SELECT) and returns the result as a list of string arrays.
     */
    public static List<String[]> executeQuery(String sql) {
        List<String[]> results = new ArrayList<>();
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("QUERY:" + sql); // Indicate it's a query
            String response;
            
            while ((response = in.readLine()) != null) {
                results.add(response.split(",")); // Split CSV response into an array
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return results;
    }

    /**
     * Executes a SQL update (INSERT, UPDATE, DELETE) and returns the number of affected rows.
     */
    public static int executeUpdate(String sql, Object... params) {
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("UPDATE:" + sql); // Indicate it's an update
            String response = in.readLine();
            return Integer.parseInt(response.trim()); // Expecting row count from the server
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
            return -1; // Indicate failure
        }
    }
}
