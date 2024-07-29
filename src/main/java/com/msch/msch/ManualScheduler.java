package com.msch.msch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private ThreadPoolTaskScheduler scheduler;
    private ScheduledFuture<?> scheduledFuture;

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        if (scheduler == null) {
            scheduler = new ThreadPoolTaskScheduler();
            scheduler.setPoolSize(1);
            scheduler.initialize();
        }
        return scheduler;
    }

    public void startScheduler(long intervalInMinutes, String batFilePath) {
        if (scheduler != null) {
            if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
                scheduledFuture.cancel(false);
            }

            String cronExpression = "0 0/" + intervalInMinutes + " * * * *";
            scheduledFuture = scheduler.schedule(() -> executeBatFile(batFilePath), new CronTrigger(cronExpression));
        }
    }

    public void executeBatFile(String batFilePath) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", batFilePath);
            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    logger.info(line);
                }
            }
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            logger.error("Failed to execute .bat file", e);
        }
    }
}
