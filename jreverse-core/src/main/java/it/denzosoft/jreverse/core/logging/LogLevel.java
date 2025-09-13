package it.denzosoft.jreverse.core.logging;

import java.util.logging.Level;

/**
 * Enumeration for logging levels with mapping to java.util.logging levels
 */
public enum LogLevel {
    TRACE(Level.FINEST),
    DEBUG(Level.FINE), 
    INFO(Level.INFO),
    WARN(Level.WARNING),
    ERROR(Level.SEVERE);
    
    private final Level javaLevel;
    
    LogLevel(Level javaLevel) {
        this.javaLevel = javaLevel;
    }
    
    public Level toJavaLevel() {
        return javaLevel;
    }
    
    /**
     * Convert from java.util.logging.Level to LogLevel
     */
    public static LogLevel fromJavaLevel(Level javaLevel) {
        for (LogLevel logLevel : values()) {
            if (logLevel.javaLevel.equals(javaLevel)) {
                return logLevel;
            }
        }
        return INFO; // Default fallback
    }
}