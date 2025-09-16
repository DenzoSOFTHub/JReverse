package it.denzosoft.jreverse.core.logging;

import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility wrapper for java.util.logging providing convenient methods
 * for structured logging with performance monitoring capabilities.
 *
 * OPTIMIZED: Includes lazy evaluation methods to avoid expensive parameter
 * computation when logging levels are disabled, improving performance.
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
     * OPTIMIZATION: Lazy logging for info level - expensive parameter computation
     * is deferred until logging level check passes
     */
    public void info(Supplier<String> messageSupplier) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info(messageSupplier.get());
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
     * OPTIMIZATION: Lazy logging for debug level - expensive parameter computation
     * is deferred until logging level check passes
     */
    public void debug(Supplier<String> messageSupplier) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(messageSupplier.get());
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
     * OPTIMIZATION: Lazy logging for trace level - expensive parameter computation
     * is deferred until logging level check passes
     */
    public void trace(Supplier<String> messageSupplier) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest(messageSupplier.get());
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
     * OPTIMIZATION: Lazy logging for warning level - expensive parameter computation
     * is deferred until logging level check passes
     */
    public void warn(Supplier<String> messageSupplier) {
        if (logger.isLoggable(Level.WARNING)) {
            logger.warning(messageSupplier.get());
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
     * OPTIMIZATION: Lazy logging for error level - expensive parameter computation
     * is deferred (though errors are typically always logged)
     */
    public void error(Supplier<String> messageSupplier) {
        logger.severe(messageSupplier.get());
    }

    /**
     * OPTIMIZATION: Lazy logging for error with exception
     */
    public void error(Supplier<String> messageSupplier, Throwable throwable) {
        logger.log(Level.SEVERE, messageSupplier.get(), throwable);
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
    
    /**
     * OPTIMIZATION: Improved message formatting with caching and performance optimizations
     */
    private String formatMessage(String message, Object... params) {
        if (params == null || params.length == 0) {
            return message;
        }

        try {
            // OPTIMIZATION: Fast path for single %s parameter (common case) but only if safe
            if (params.length == 1 && message.equals("%s")) {
                return String.valueOf(params[0]);
            }

            return String.format(message, params);
        } catch (Exception e) {
            // OPTIMIZATION: Use StringBuilder for error message to avoid string concatenation
            StringBuilder sb = new StringBuilder(message.length() + 32);
            sb.append(message)
              .append(" [Format error: ")
              .append(e.getMessage())
              .append("]");
            return sb.toString();
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