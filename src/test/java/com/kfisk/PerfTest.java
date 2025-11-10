package com.kfisk;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.MountableFile;

public class PerfTest {

    @Test
    void runLoadTest() throws Exception {
        try (GenericContainer<?> api = new GenericContainer<>("taskmanageapi:test")
                .withEnv("APP_ENV", "test")
                .withCopyFileToContainer(
                        MountableFile.forHostPath("perf-tests/test.db"),
                        "/data/test.db")
                .withExposedPorts(7000)) {

            api.start();
            int mappedPort = api.getMappedPort(7000);

            File reportDir = new File("perf-tests/report");

            deleteDirectoryRecursively(reportDir);
            reportDir.mkdirs();

            File resultsFile = new File("perf-tests/results.jtl");

            if (resultsFile.exists() && !resultsFile.delete()) {
                throw new IOException("Failed to delete previous results file: " + resultsFile.getAbsolutePath());
            }

            String os = System.getProperty("os.name").toLowerCase();
            String userFlag = "";

            /* 
             * If tests are run on linux, we have to specify a user to run the test container.
             * Otherwise the container creates files as root, creating issues on host machine
            */
            if (os.contains("linux")) {
                String uid = new ProcessBuilder("id", "-u").start()
                        .inputReader().readLine();
                String gid = new ProcessBuilder("id", "-g").start()
                        .inputReader().readLine();
                userFlag = uid + ":" + gid;
            }

            List<String> commands = new ArrayList<>();
            commands.addAll(Arrays.asList("docker", "run", "--rm",
                    "-v", new File("perf-tests").getAbsolutePath() + ":/jmeter",
                    "-w", "/jmeter",
                    "justb4/jmeter",
                    "-n",
                    "-t", "test-plan.jmx",
                    "-JAPI_HOST=host.docker.internal",
                    "-JAPI_PORT=" + mappedPort,
                    "-l", "results.jtl",
                    "-e", "-o", "report"));

            if (!userFlag.isEmpty()) {
                commands.add("-u");
                commands.add(userFlag);
            }

            ProcessBuilder pb = new ProcessBuilder(commands);
            pb.inheritIO();
            Process proc = pb.start();
            int exit = proc.waitFor();
            if (exit != 0) {
                throw new RuntimeException("JMeter test failed. Exit Status: " + exit);
            }
        }
    }

    private void deleteDirectoryRecursively(File dir) throws IOException {
        if (dir.exists()) {
            File[] contents = dir.listFiles();
            if (contents != null) {
                for (File f : contents) {
                    if (f.isDirectory()) {
                        deleteDirectoryRecursively(f);
                    } else {
                        if (!f.delete()) {
                            throw new IOException("Failed to delete file: " + f.getAbsolutePath());
                        }
                    }
                }
            }
            if (!dir.delete()) {
                throw new IOException("Failed to delete directory: " + dir.getAbsolutePath());
            }
        }
    }
}
