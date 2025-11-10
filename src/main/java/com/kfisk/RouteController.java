package com.kfisk;
import java.sql.Connection;
import java.util.List;

import io.javalin.http.Context;

public class RouteController {


        private final Connection conn;
        private final PersistenceManager dao = new PersistenceManager();

        public RouteController(Connection conn) {
            this.conn = conn;
        }

        public void getAllTasks(Context ctx) {
            try {
                List<Task> tasks = dao.getAllTasks(conn);
                ctx.json(tasks);
            } catch (Exception e) {
                ctx.status(500).result("Error fetching tasks");
            }
        }

        public void createTask(Context ctx) {
            try {
                Task task = ctx.bodyAsClass(Task.class);
                dao.createTask(task, conn);
                ctx.status(201).json(task);
            } catch (Exception e) {
                ctx.status(500).result("Error creating task");
            }
        }

        public void setTaskComplete(Context ctx) {
            try {
                Task input = ctx.bodyAsClass(Task.class);
                dao.setCompletion(input.isCompleted,  input.title, conn);
                ctx.status(200).result("Task updated");
            } catch (Exception e) {
                ctx.status(500).result("Error updating task");
            }
        }

        public void deleteTask(Context ctx) {
            try {
                String title = ctx.bodyAsClass(String.class);
                dao.deleteTask(title, conn);
                ctx.status(200).result("Task deleted");
            } catch (Exception e) {
                ctx.status(500).result("Error deleting task");
            }
        }

}
