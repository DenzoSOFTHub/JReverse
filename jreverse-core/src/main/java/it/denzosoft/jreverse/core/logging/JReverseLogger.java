package it.denzosoft.jreverse.core.logging;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility wrapper for java.util.logging providing convenient methods
 * for structured logging with performance monitoring capabilities
 */
public class JReverseLogger {
    
    private final Logger logger;
    
    private JReverseLogger(String name) {
        this.logger = Logger.getLogger(name);
    }
    
    /**
     * Create logger for the specified class
     */
    public static JReverseLogger getLogger(Class<?> clazz) {
        return new JReverseLogger(clazz.getName());
    }
    
    /**
     * Create logger with specified name
     */
    public static JReverseLogger getLogger(String name) {
        return new JReverseLogger(name);
    }
    
    /**
     * Log info message with optional parameters
     */
    public void info(String message, Object... params) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info(formatMessage(message, params));
        }
    }
    
    /**
     * Log debug message with optional parameters
     */
    public void debug(String message, Object... params) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(formatMessage(message, params));
        }
    }
    
    /**
     * Log trace message with optional parameters
     */
    public void trace(String message, Object... params) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest(formatMessage(message, params));
        }
    }
    
    /**
     * Log warning message with optional parameters
     */
    public void warn(String message, Object... params) {
        if (logger.isLoggable(Level.WARNING)) {
            logger.warning(formatMessage(message, params));
        }
    }
    
    /**
     * Log error message
     */
    public void error(String message) {
        logger.severe(message);
    }
    
    /**
     * Log error message with exception
     */
    public void error(String message, Throwable throwable) {
        logger.log(Level.SEVERE, message, throwable);
    }
    
    /**
     * Log error message with parameters and exception
     */
    public void error(String message, Throwable throwable, Object... params) {
        logger.log(Level.SEVERE, formatMessage(message, params), throwable);
    }
    
    /**
     * Start operation logging with timestamp
     */
    public OperationTimer startOperation(String operation) {
        info("Starting operation: %s", operation);
        return new OperationTimer(this, operation);
    }
    
    /**
     * Log operation completion with duration
     */
    public void endOperation(String operation, long durationMs) {
        info("Completed operation: %s in %d ms", operation, durationMs);
    }
    
    /**
     * Check if debug logging is enabled
     */
    public boolean isDebugEnabled() {
        return logger.isLoggable(Level.FINE);
    }
    
    /**
     * Check if trace logging is enabled
     */
    public boolean isTraceEnabled() {
        return logger.isLoggable(Level.FINEST);
    }
    
    /**
     * Get underlying Java logger (for advanced use cases)
     */
    public Logger getUnderlyingLogger() {
        return logger;
    }
    
    private String formatMessage(String message, Object... params) {
        if (params == null || params.length == 0) {
            return message;
        }
        try {
            return String.format(message, params);
        } catch (Exception e) {
            // Fallback if formatting fails
            return message + " [Format error: " + e.getMessage() + "]";
        }
    }
    
    /**
     * Timer class for measuring operation durations
     */
    public static class OperationTimer implements AutoCloseable {
        private final JReverseLogger logger;
        private final String operation;
        private final long startTime;
        
        private OperationTimer(JReverseLogger logger, String operation) {
            this.logger = logger;
            this.operation = operation;
            this.startTime = System.currentTimeMillis();
        }
        
        @Override
        public void close() {
            long duration = System.currentTimeMillis() - startTime;
            logger.endOperation(operation, duration);
        }
        
        /**
         * Get elapsed time in milliseconds
         */
        public long getElapsedTime() {
            return System.currentTimeMillis() - startTime;
        }
    }
}