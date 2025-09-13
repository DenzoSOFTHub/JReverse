package it.denzosoft.jreverse.core.logging;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

/**
 * Central configuration for JReverse logging system using java.util.logging
 */
public class LoggingConfiguration {
    
    private static final Logger ROOT_LOGGER = Logger.getLogger("");
    private static final String LOG_FILE_NAME = "jreverse.log";
    private static final String LOG_DIR = "logs";
    private static volatile boolean isConfigured = false;
    
    /**
     * Configure logging with specified level and optional file logging
     * 
     * @param level Log level to set
     * @param enableFileLogging Whether to enable file logging
     */
    public static synchronized void configureLogging(LogLevel level, boolean enableFileLogging) {
        if (isConfigured) {
            return; // Prevent multiple configurations
        }
        
        // Clear existing handlers to avoid duplicates
        Handler[] existingHandlers = ROOT_LOGGER.getHandlers();
        for (Handler handler : existingHandlers) {
            ROOT_LOGGER.removeHandler(handler);
            handler.close();
        }
        
        // Set log level
        ROOT_LOGGER.setLevel(level.toJavaLevel());
        
        // Configure console handler
        configureConsoleHandler();
        
        // Configure file handler if requested
        if (enableFileLogging) {
            configureFileHandler();
        }
        
        // Prevent parent loggers from processing our messages
        ROOT_LOGGER.setUseParentHandlers(false);
        
        isConfigured = true;
        
        // Log configuration success
        Logger logger = Logger.getLogger(LoggingConfiguration.class.getName());
        logger.info("Logging configured - Level: " + level + ", File logging: " + enableFileLogging);
    }
    
    /**
     * Configure logging with default settings (INFO level, console only)
     */
    public static void configureLogging() {
        configureLogging(LogLevel.INFO, false);
    }
    
    /**
     * Reset logging configuration (for testing)
     */
    public static synchronized void resetConfiguration() {
        Handler[] handlers = ROOT_LOGGER.getHandlers();
        for (Handler handler : handlers) {
            ROOT_LOGGER.removeHandler(handler);
            handler.close();
        }
        isConfigured = false;
    }
    
    private static void configureConsoleHandler() {
        try {
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new CustomFormatter());
            consoleHandler.setLevel(ROOT_LOGGER.getLevel());
            ROOT_LOGGER.addHandler(consoleHandler);
        } catch (Exception e) {
            System.err.println("Failed to configure console logging: " + e.getMessage());
        }
    }
    
    private static void configureFileHandler() {
        try {
            // Create logs directory if it doesn't exist
            Path logDir = Paths.get(LOG_DIR);
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
            }
            
            // Create file handler with rotation (10MB max, 5 files)
            String logFilePath = logDir.resolve(LOG_FILE_NAME).toString();
            FileHandler fileHandler = new FileHandler(logFilePath, 10 * 1024 * 1024, 5, true);
            fileHandler.setFormatter(new CustomFormatter());
            fileHandler.setLevel(ROOT_LOGGER.getLevel());
            ROOT_LOGGER.addHandler(fileHandler);
            
        } catch (IOException e) {
            ROOT_LOGGER.severe("Cannot create file handler: " + e.getMessage());
        }
    }
    
    /**
     * Check if logging has been configured
     */
    public static boolean isConfigured() {
        return isConfigured;
    }
}