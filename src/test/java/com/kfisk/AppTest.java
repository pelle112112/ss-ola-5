package com.kfisk;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AppTest {

    private final String TESTDB_URL = "jdbc:sqlite:src/test/java/test.db";
    PersistenceManager pm = new PersistenceManager();

    // INTEGRATION *************************************************************************

    @Test
    @BeforeEach
    public void DBConnectionTest() {

        try (var c = DriverManager.getConnection(TESTDB_URL)) {
            pm.initDB(c);

        } catch (SQLException e) {
            fail("Failed to connect to db: " + e.getMessage());
        }
    }

    @Test
    public void createTaskTest() {

        String testName = "create_test";
        Task t = new Task(testName, false);

        try (var c = DriverManager.getConnection(TESTDB_URL)) {
            pm.createTask(t, c);

        } catch (SQLException e) {
            fail("Failed to connect to db: " + e.getMessage());
        }

        String checkSql = "SELECT title, isCompleted FROM tasks WHERE title = ?";

        try (var c = DriverManager.getConnection(TESTDB_URL)) {

            try (var pstmt = c.prepareStatement(checkSql)) {

                pstmt.setString(1, testName);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    Task queriedT = new Task(rs.getString("title"), rs.getBoolean("isCompleted"));
                    assertNotNull(queriedT);
                    assertEquals(t, queriedT);
                } else {
                    fail("No result found for: " + testName);
                }
            }

        } catch (SQLException e) {
            fail("Failed to connect to db: " + e.getMessage());
        }
    }

    @Test
    void updateTaskTest() {

        String testTitle = "UpdateTest";
        String insertSql = "INSERT INTO tasks VALUES(?, ?)";

        try (var c = DriverManager.getConnection(TESTDB_URL)) {
            try (var pstmt = c.prepareStatement(insertSql)) {

                pstmt.setString(1, testTitle);
                pstmt.setBoolean(2, false);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                fail("Failed to insert test-task: " + e.getMessage());
            }

            pm.setCompletion(true, testTitle, c);

            String querySql = "SELECT isCompleted FROM tasks WHERE title = ?";

            try (var pstmt = c.prepareStatement(querySql)) {

                pstmt.setString(1, testTitle);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    assertEquals(true, rs.getBoolean("isCompleted"));
                }
            } catch (SQLException e) {
                fail("Failed to insert test-scenario: " + e.getMessage());
            }

        } catch (SQLException e) {
            fail("Failed to get connection: " + e.getMessage());
        }

    }

    @Test
    void getAllTasksTest() {
        String[] testTitles = {"getalltest1", "getalltest2", "getalltest3"};

        String insertSql = "INSERT INTO tasks (title, isCompleted) VALUES (?, ?)";

        try (var c = DriverManager.getConnection(TESTDB_URL)) {
            for (int i = 0; i < testTitles.length; i++) {
                try (var pstmt = c.prepareStatement(insertSql)) {

                    pstmt.setString(1, testTitles[i]);
                    pstmt.setBoolean(2, false);
                    pstmt.executeUpdate();
                } catch (SQLException e) {
                    fail("Failed to insert test-task: " + e.getMessage());
                }
            }

            List<Task> res = pm.getAllTasks(c);
            assertEquals(testTitles.length, res.size());

        } catch (SQLException e) {
            fail("Failed to connect to db: " + e.getMessage());
        }
    }

    @Test
    void deleteTask() {
        String testTitle = "DeleteTest";
        String insertSql = "INSERT INTO tasks VALUES(?, ?)";

        try (var c = DriverManager.getConnection(TESTDB_URL)) {
            try (var pstmt = c.prepareStatement(insertSql)) {

                pstmt.setString(1, testTitle);
                pstmt.setBoolean(2, false);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                fail("Failed to insert test-task: " + e.getMessage());
            }

            pm.deleteTask(testTitle, c);

            String querySQL = "SELECT * FROM tasks WHERE title = ?";

            try (var pstmt = c.prepareStatement(querySQL)) {

                pstmt.setString(1, testTitle);

                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    fail("Deleted result was found!");
                }
                
            } catch (SQLException e) {
                fail("Failed to insert test-task: " + e.getMessage());
            }

        } catch (SQLException e) {
            fail("Failed to connect to db: " + e.getMessage());
        }
    }

    // UNIT **********************************************************************************
    // Lets get better coverage!

    @Test 
    void taskOtherObjectComparisonTest() {

        var task = new Task("test", false);
        var thing = new HashSet<Object>();

        assertFalse(task.equals(thing));
    }
    
    @Test 
    void taskSameObjectOtherBoolComparisonTest() {

        var task = new Task("test", false);
        var thing = new Task("test", true);

        assertFalse(task.equals(thing));
    }

    @Test 
    void taskSameObjectOtherTitleComparisonTest() {

        var task = new Task("test", false);
        var thing = new Task("diff", false);

        assertFalse(task.equals(thing));
    }

    @Test 
    void taskSameObjectComparisonTest() {

        var task = new Task("test", false);
        var thing = new Task("test", false);

        assertTrue(task.equals(thing));
    }

}
