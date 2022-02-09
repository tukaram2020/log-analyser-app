package com.assignment.loganalyser.app.validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.assignment.loganalyser.app.model.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * 
 * @author tukaram
 *
 */

@Component
public class LogAnalyserValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogAnalyserValidator.class);

    public void validateInput(Context context, String... args) {
        LOGGER.info("Validating the input...");

        validateInput(args);
        validateFilePath(context, args[0]);
    }

    private void validateFilePath(Context context, String logFilePath) {
        LOGGER.info("Log file specified for LogAnalyserService: {}", logFilePath);
        context.setLogFilePath(logFilePath);

        try {
            File file = new ClassPathResource("samples/" + logFilePath).getFile();
            if (!file.exists()) {
                file = new ClassPathResource(logFilePath).getFile();
                if (!file.exists()) {
                    file = new File(logFilePath);
                }
            }

            if (!file.exists())
                throw new FileNotFoundException("Unable to open the file " + logFilePath);
        } catch (IOException e) {
            LOGGER.error("!!! Unable to find the specified file '{}'", logFilePath);
        }
    }

    private void validateInput(String[] args) {
        LOGGER.debug("Validating the program arguments...");
        if (args.length < 1) {
            throw new IllegalArgumentException("Could not find file , Please specify the filepath to analyse.");
        }
    }
}
