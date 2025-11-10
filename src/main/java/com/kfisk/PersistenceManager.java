/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kfisk;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author kotteletfisk
 */
public class PersistenceManager {

    public void initDB(Connection c) throws SQLException {

        String dropSql = "DROP TABLE IF EXISTS tasks;";
        String createSql = "CREATE TABLE tasks ("
                + "	title TEXT PRIMARY KEY,"
                + "	isCompleted INTEGER"
                + ");";
        try (var stmt = c.createStatement()) {
            stmt.execute(dropSql);
            stmt.execute(createSql);
        }
    }

    public void createTask(Task t, Connection c) throws SQLException {

        String sql = "INSERT INTO tasks VALUES(?, ?)";

        try (var pstmt = c.prepareStatement(sql)) {

            pstmt.setString(1, t.title);
            pstmt.setBoolean(2, t.isCompleted);
            pstmt.executeUpdate();
        }
    }

    public void setCompletion(boolean isCompleted, String title, Connection c) throws SQLException {

        String sql = "UPDATE tasks SET isCompleted = ? WHERE title = ?";

        try (var pstmt = c.prepareStatement(sql)) {

            pstmt.setBoolean(1, isCompleted);
            pstmt.setString(2, title);
            pstmt.executeUpdate();
        }
    }

    // output might be null!
    public List<Task> getAllTasks(Connection c) throws SQLException {
        
        String sql = "SELECT title, isCompleted FROM tasks";
        List<Task> output = new ArrayList<>();

        try (var stmt = c.createStatement()) {

            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                String title = rs.getString("title");
                boolean isCompleted = rs.getBoolean("isCompleted");

                output.add(new Task(title, isCompleted));
            }
        }
        return output;
    }

    public void deleteTask(String title, Connection c) throws SQLException {
        
        String sql = "DELETE FROM tasks WHERE title = ?";

        try (var pstmt = c.prepareStatement(sql)) {

            pstmt.setString(1, title);
            pstmt.executeUpdate();
        }
    }
}
