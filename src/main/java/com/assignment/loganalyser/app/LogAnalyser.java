package com.assignment.loganalyser.app;

import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.assignment.loganalyser.app.service.LogAnalyserServiceImpl;

@SpringBootApplication
public class LogAnalyser implements CommandLineRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogAnalyser.class);

    @Autowired
    private LogAnalyserServiceImpl logAnalyserService;

    public static void main(String... args) {
        SpringApplication app = new SpringApplication(LogAnalyser.class);
        app.run(args);
    }

    @Override
    public void run(String... args) {
        Instant start = Instant.now();
        logAnalyserService.execute(args);
        Instant end = Instant.now();
        LOGGER.info("Total execution time: {}ms", Duration.between(start, end).toMillis());
    }
}
