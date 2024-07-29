package com.msch.msch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource("classpath:application-dev.properties")
public class MschApplication implements CommandLineRunner {

	private static final Logger logger = LoggerFactory.getLogger(MschApplication.class);

	@Value("${default.scheduler.interval}")
	private long defaultSchedulerInterval;

	private String batFilePath;
	@Autowired
	private ManualScheduler manualScheduler;
	private long intervalInMinutes;

	public static void main(String[] args) {
		SpringApplication.run(MschApplication.class, args);
	}

	@Override
	public void run(String... args) {
		if (args.length > 0) {
			try {
				if (args.length == 1) {
					batFilePath = args[0];
					intervalInMinutes = defaultSchedulerInterval;
				} else if (args.length == 2) {
					if (isInteger(args[0])) {
						intervalInMinutes = Long.parseLong(args[0]);
						if (args[1].endsWith(".bat")) {
							batFilePath = args[1];
						} else {
							logger.error("Invalid .bat file extension: {}", args[1]);
							return;
						}
					} else if (isInteger(args[1])) {
						intervalInMinutes = Long.parseLong(args[1]);
						if (args[0].endsWith(".bat")) {
							batFilePath = args[0];
						} else {
							logger.error("Invalid .bat file extension: {}", args[0]);
							return;
						}
					} else {
						logger.error("Invalid arguments. Expected .bat file path and interval in minutes.");
						logger.info("e.g. arg1 = C:\\Users\\xx\\xx.bat, arg2 = 5");
						logger.info("e.g. java -jar C:\\Users\\xx\\xx.jar " +
								"C:\\Users\\xx\\xx.bat 2");
						return;
					}
				} else {
					logger.error("Invalid number of arguments. Expected 1 or 2 arguments.");
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
