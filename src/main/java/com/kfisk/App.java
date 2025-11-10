package com.kfisk;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;

import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import io.javalin.Javalin;
import io.prometheus.client.exporter.HTTPServer;

public class App {

    public static void main(String[] args) throws Exception {

        //String env = System.getenv().getOrDefault("APP_ENV", "prod");
        String dbUrl = "jdbc:sqlite:/data/prod.db";

        Connection conn = DriverManager.getConnection(dbUrl);
        var controller = new RouteController(conn);

        StatisticsHandler statisticsHandler = new StatisticsHandler();
        QueuedThreadPool queuedThreadPool = new QueuedThreadPool(200, 8, 60_000);

        var app = Javalin.create(config -> {
            config.jetty.threadPool = queuedThreadPool;
            config.jetty.modifyServer(server -> {
                server.setHandler(statisticsHandler);
            });
        })
                .get("/api/getAllTasks", controller::getAllTasks)
                .get("/api/metrics", controller::metrics)
                .post("/api/createTask", controller::createTask)
                .put("/api/setTaskComplete", controller::setTaskComplete)
                .delete("/api/deleteTask", controller::deleteTask)
                .start(7000);
                initializePrometheus(statisticsHandler, queuedThreadPool);
    }

    private static void initializePrometheus(StatisticsHandler statisticsHandler, QueuedThreadPool queuedThreadPool) throws IOException {
        StatisticsHandlerCollector.initialize(statisticsHandler);
        QueuedThreadPoolCollector.initialize(queuedThreadPool);
        HTTPServer prometheusServer = new HTTPServer(7080);
        System.out.println("Prometheus server started at 7080");
    }

}
