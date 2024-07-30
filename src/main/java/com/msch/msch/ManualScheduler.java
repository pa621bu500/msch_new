package com.msch.msch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ScheduledFuture;

@Component
@EnableScheduling
public class ManualScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ManualScheduler.class);

    @Autowired
    private ThreadPoolTaskScheduler scheduler;
    private ScheduledFuture<?> scheduledFuture;

    public ManualScheduler(ThreadPoolTaskScheduler scheduler) {
        this.scheduler = scheduler;
    }

    public void startScheduler(long intervalInMinutes, String batFilePath) {
        // scheduledFuture != null = there is some scheduled task
        //!scheduledFuture.isCancelled() = the scheduled task is not cancelled
        //if there is some scheduled task and it is not cancelled, cancel it, to prevent multiple tasks running at the same time
        if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
            scheduledFuture.cancel(false); //cancels the scheduled task, only when it is not running
        }

        String cronExpression = "0 0/" + intervalInMinutes + " * * * *";
        scheduledFuture = scheduler.schedule(() -> executeBatFile(batFilePath), new CronTrigger(cronExpression));
    }

    public void executeBatFile(String batFilePath) {
        try {
            // Step 1: Create a ProcessBuilder to start the batch file
            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", batFilePath);

            // Step 2: Start the process and get the Process object
            Process process = processBuilder.start();

            // Step 3: Read the output of the process
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    logger.info(line);
                }
            }

            // Step 4: Wait for the process to complete
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            logger.error("Failed to execute .bat file", e);
        }
    }
}
