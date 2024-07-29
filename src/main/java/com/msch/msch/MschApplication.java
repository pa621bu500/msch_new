package com.msch.msch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.ScheduledFuture;

@SpringBootApplication
public class MschApplication  implements CommandLineRunner {

	private static final Logger logger = LoggerFactory.getLogger(ManualScheduler.class);
	@Value("${default.scheduler.interval}")
	private long defaultSchedulerInterval;
	@Value("${bat.file.path}")
	private String batFilePath;
	@Autowired
	private ManualScheduler manualScheduler;
	private long intervalInMinutes;

	public static void main(String[] args) {
		SpringApplication.run(MschApplication.class, args);
	}

	//takes in the interval in minutes from command line argument
	@Override
	public void run(String... args) throws Exception {
		if (args.length > 0) {
			try {
				intervalInMinutes = Long.parseLong(args[0]);
				logger.info("Scheduler interval set to {} minutes.", intervalInMinutes);
			} catch (NumberFormatException e) {
				logger.error("Invalid interval value provided. Using default interval of "+ defaultSchedulerInterval +" minutes.");
				intervalInMinutes = defaultSchedulerInterval;
			}
		} else {
			intervalInMinutes = defaultSchedulerInterval;
			logger.info("Scheduler interval set to {} minutes.", intervalInMinutes);
		}
		manualScheduler.startScheduler(intervalInMinutes,batFilePath);
	}

}
