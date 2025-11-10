package com.kfisk;

import io.javalin.Javalin;
import java.sql.Connection;
import java.sql.DriverManager;

public class App {
    public static void main(String[] args) throws Exception{

        String env = System.getenv().getOrDefault("APP_ENV", "prod");
        String dbUrl;

        if ("prod".equals(env)) {
            dbUrl = "jdbc:sqlite:data/prod.db";
        } else {
            dbUrl = "jdbc:sqlite:/data/test.db";
        }

        Connection conn = DriverManager.getConnection(dbUrl);

        var controller = new RouteController(conn);

        var app = Javalin.create()
                .get("/api/getAllTasks", controller::getAllTasks)
                .post("/api/createTask", controller::createTask)
                .put("/api/setTaskComplete", controller::setTaskComplete)
                .delete("/api/deleteTask", controller::deleteTask)
                .start(7000);
    }

}
