    package com.kfisk;
    import java.lang.management.ManagementFactory;
    import java.sql.Connection;
    import java.sql.SQLException;
    import java.util.HashMap;
    import java.util.List;
    import java.util.concurrent.atomic.AtomicLong;

    import com.sun.management.OperatingSystemMXBean;
    import io.javalin.http.Context;

    public class RouteController {

            private final Connection conn;
            private final PersistenceManager dao;

        private static final AtomicLong requestAmount = new AtomicLong(0);
        private static final AtomicLong requestUnderAcceptableLatency = new AtomicLong(0);
        private static final AtomicLong failedRequests = new AtomicLong(0);

        private static final OperatingSystemMXBean osBean =
                (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        public RouteController(Connection conn) throws SQLException {
            this.conn = conn;
            this.dao = new PersistenceManager(conn);
        }

            public void getAllTasks(Context ctx) {
                long start = System.currentTimeMillis();
                requestAmount.incrementAndGet();
                try {
                    List<Task> tasks = dao.getAllTasks(conn);
                    long end = System.currentTimeMillis();
                    if(end - start < 200){
                        requestUnderAcceptableLatency.incrementAndGet();
                    }
                    ctx.json(tasks);
                } catch (Exception e) {
                    failedRequests.incrementAndGet();
                    ctx.status(500).result("Error fetching tasks" +  e.getMessage());
                }
            }

            public void createTask(Context ctx) {
                long start = System.currentTimeMillis();
                requestAmount.incrementAndGet();
                try {
                    Thread.sleep(500);
                    Task task = ctx.bodyAsClass(Task.class);
                    dao.createTask(task, conn);
                    long end = System.currentTimeMillis();
                    if(end - start < 200){
                        requestUnderAcceptableLatency.incrementAndGet();
                    }
                    ctx.status(201).json(task);
                } catch (Exception e) {
                    failedRequests.incrementAndGet();
                    ctx.status(500).result("Error creating task");
                }
            }

            public void setTaskComplete(Context ctx) {
                long start = System.currentTimeMillis();
                requestAmount.incrementAndGet();
                try {
                    Task input = ctx.bodyAsClass(Task.class);
                    dao.setCompletion(input.isCompleted,  input.title, conn);
                    long end = System.currentTimeMillis();
                    if(end - start < 200){
                        requestUnderAcceptableLatency.incrementAndGet();
                    }
                    ctx.status(200).result("Task updated");
                } catch (Exception e) {
                    failedRequests.incrementAndGet();
                    ctx.status(500).result("Error updating task");
                }
            }

            public void deleteTask(Context ctx) {
                long start = System.currentTimeMillis();
                requestAmount.incrementAndGet();
                try {
                    String title = ctx.bodyAsClass(String.class);
                    dao.deleteTask(title, conn);
                    long end = System.currentTimeMillis();
                    if(end - start < 200){
                        requestUnderAcceptableLatency.incrementAndGet();
                    }
                    ctx.status(200).result("Task deleted");
                } catch (Exception e) {
                    failedRequests.incrementAndGet();
                    ctx.status(500).result("Error deleting task");
                }
            }
        public void metrics(Context ctx) {
            try {
                System.out.println("Metrics endpoint hit");
                StringBuilder sb = new StringBuilder();
                sb.append("# HELP test_metric A simple test metric\n");
                sb.append("# TYPE test_metric gauge\n");
                sb.append("test_metric{label1=\"Hej\",label2=\"test\"} 1\n");

                double request_time_rate;
                long total = requestAmount.get();
                long ok = requestUnderAcceptableLatency.get();

                if (total == 0) {
                    request_time_rate = 0.0;
                } else {
                    request_time_rate = ((double) ok / total) * 100;
                }

                sb.append("# HELP response_time_metric Measures percentage of acceptable response times\n");
                sb.append("# TYPE response_time_metric gauge\n");
                sb.append("response_time_metric ").append(request_time_rate).append("\n");

                double requestSuccessRate;

                if(total == 0){
                    requestSuccessRate = 0.0;
                } else {
                    requestSuccessRate = ((double) (total - failedRequests.get()) / total) * 100;
                }

                sb.append("# HELP success_rate_metric Measures percentage of acceptable response times\n");
                sb.append("# TYPE success_rate_metric gauge\n");
                sb.append("success_rate_metric ").append(requestSuccessRate).append("\n");

                double load = osBean.getSystemCpuLoad();
                if (load < 0) {
                    load = 0;
                } else {
                    load = load * 100;
                }

                sb.append("# HELP cpu_usage_metric Measures percentage of acceptable response times\n");
                sb.append("# TYPE cpu_usage_metric gauge\n");
                sb.append("cpu_usage_metric ").append(load).append("\n");

                HashMap<String, Integer> taskMap = dao.getMetrics(conn);

                sb.append("# HELP total_tasks_metric Measures percentage of acceptable response times\n");
                sb.append("# TYPE total_tasks_metric gauge\n");
                sb.append("total_tasks_metric ").append(taskMap.get("total")).append("\n");

                sb.append("# HELP tasks_completed_metric Measures percentage of acceptable response times\n");
                sb.append("# TYPE tasks_completed_metric gauge\n");
                sb.append("tasks_completed_metric ").append(taskMap.get("completed")).append("\n");

                ctx.contentType("text/plain; version=0.0.4").result(sb.toString());
                ctx.result(sb.toString());
            } catch (Exception e) {
                System.out.println(e.getMessage());
                ctx.status(500).result("Error metrics");
            }
        }

    }
