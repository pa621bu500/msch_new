package com.msch.msch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
@PropertySource("classpath:application-dev.properties")
public class MschApplication implements CommandLineRunner {

	private static final Logger logger = LoggerFactory.getLogger(MschApplication.class);

	@Value("${filepath:null}")
	private String batFilePath;
	@Value("${interval:${default.scheduler.interval}}")
	private Integer intervalInMinutes;


	@Autowired
	private ManualScheduler manualScheduler;


	public static void main(String[] args) {
		SpringApplication.run(MschApplication.class, args);
	}

	@Override
	public void run(String... args) {
		if (args.length > 0) {
			try {
				// Step 1: Check if the .bat file exists
				Path path = Paths.get(batFilePath);

				if(batFilePath == null || batFilePath.isEmpty()) {
					logger.error("No arguments provided. Expected .bat file path and interval in minutes (optional).");
					return;
				}
				if(batFilePath.endsWith(".bat") == false) {
					logger.error("wrong extension, expected extension .bat");
					return;
				}
				if (!Files.exists(path)) {
					logger.error("unable to find batch file at " + batFilePath);
					return;
				}
				if(intervalInMinutes <= 0) {
					logger.error("intervalInMinutes must be greater than 0");
					return;
				}

				logger.info("Scheduler interval set to {} minutes.", intervalInMinutes);
				manualScheduler.startScheduler(intervalInMinutes, batFilePath);
			} catch (NumberFormatException e) {
				logger.error("Invalid number format for interval: {}", e.getMessage());
			}
		} else {
			logger.error("No arguments provided. Expected .bat file path and interval in minutes (optional).");
		}
	}

	private boolean isInteger(String str) {
		try {
			Long.parseLong(str); // Use Long instead of Integer to match the type of intervalInMinutes
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
}
