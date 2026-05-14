package com.fintrack.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Structured logging utility that centralizes application logging to the console
 * and a persistent rolling log file (fintrack.log).
 */
public class LoggerUtil {

    private static final Logger rootLogger = Logger.getLogger("com.fintrack");
    private static boolean isInitialized = false;

    public static void initialize() {
        if (isInitialized) return;

        try {
            // Ensure logs directory exists
            Files.createDirectories(Paths.get("logs"));

            // Remove default console handlers if needed, or keep them and just add FileHandler
            // We'll add a FileHandler that rolls over at 5MB, keeping 3 old logs
            FileHandler fileHandler = new FileHandler("logs/fintrack.log", 5 * 1024 * 1024, 3, true);
            fileHandler.setFormatter(new Formatter() {
                private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

                @Override
                public String format(LogRecord record) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("[").append(dtf.format(LocalDateTime.now())).append("] ");
                    sb.append(record.getLevel().getLocalizedName()).append(" ");
                    sb.append("[").append(record.getSourceClassName()).append(".").append(record.getSourceMethodName()).append("] ");
                    sb.append("- ").append(formatMessage(record)).append(System.lineSeparator());

                    if (record.getThrown() != null) {
                        try {
                            StringWriter sw = new StringWriter();
                            PrintWriter pw = new PrintWriter(sw);
                            record.getThrown().printStackTrace(pw);
                            pw.close();
                            sb.append(sw.toString());
                        } catch (Exception ex) {
                            // Ignored
                        }
                    }
                    return sb.toString();
                }
            });

            rootLogger.addHandler(fileHandler);
            rootLogger.setLevel(Level.ALL);
            isInitialized = true;

            rootLogger.info("Logger initialization complete. System starting up.");

        } catch (IOException e) {
            System.err.println("Failed to initialize LoggerUtil: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Logger getLogger(Class<?> clazz) {
        return Logger.getLogger(clazz.getName());
    }
}
