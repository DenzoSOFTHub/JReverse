package it.denzosoft.jreverse.core.logging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for CustomFormatter
 */
public class CustomFormatterTest {
    
    private CustomFormatter formatter;
    private LogRecord logRecord;
    
    @BeforeEach
    void setUp() {
        formatter = new CustomFormatter();
        logRecord = new LogRecord(Level.INFO, "Test message");
        logRecord.setLoggerName("it.denzosoft.jreverse.core.TestClass");
        logRecord.setMillis(System.currentTimeMillis());
    }
    
    @Test
    void shouldFormatBasicLogRecord() {
        String formatted = formatter.format(logRecord);
        
        assertThat(formatted).contains("INFO");
        assertThat(formatted).contains("TestClass");
        assertThat(formatted).contains("Test message");
        assertThat(formatted).containsPattern("\\[\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\]");
    }
    
    @Test
    void shouldExtractSimpleClassName() {
        logRecord.setLoggerName("it.denzosoft.jreverse.core.analyzer.JarAnalyzer");
        
        String formatted = formatter.format(logRecord);
        
        assertThat(formatted).contains("JarAnalyzer:");
        assertThat(formatted).doesNotContain("it.denzosoft.jreverse.core.analyzer.JarAnalyzer:");
    }
    
    @Test
    void shouldHandleRootLogger() {
        logRecord.setLoggerName("");
        
        String formatted = formatter.format(logRecord);
        
        assertThat(formatted).contains("ROOT:");
    }
    
    @Test
    void shouldHandleNullLoggerName() {
        logRecord.setLoggerName(null);
        
        String formatted = formatter.format(logRecord);
        
        assertThat(formatted).contains("ROOT:");
    }
    
    @Test
    void shouldHandleSimpleLoggerName() {
        logRecord.setLoggerName("TestClass");
        
        String formatted = formatter.format(logRecord);
        
        assertThat(formatted).contains("TestClass:");
    }
    
    @Test
    void shouldIncludeExceptionStackTrace() {
        Exception exception = new RuntimeException("Test exception");
        logRecord.setThrown(exception);
        
        String formatted = formatter.format(logRecord);
        
        assertThat(formatted).contains("Test message");
        assertThat(formatted).contains("java.lang.RuntimeException: Test exception");
        assertThat(formatted).contains("at ");
    }
    
    @Test
    void shouldFormatDifferentLogLevels() {
        // Test SEVERE
        logRecord.setLevel(Level.SEVERE);
        String formatted = formatter.format(logRecord);
        assertThat(formatted).contains("SEVERE");
        
        // Test WARNING
        logRecord.setLevel(Level.WARNING);
        formatted = formatter.format(logRecord);
        assertThat(formatted).contains("WARNING");
        
        // Test FINE
        logRecord.setLevel(Level.FINE);
        formatted = formatter.format(logRecord);
        assertThat(formatted).contains("FINE");
        
        // Test FINEST
        logRecord.setLevel(Level.FINEST);
        formatted = formatter.format(logRecord);
        assertThat(formatted).contains("FINEST");
    }
    
    @Test
    void shouldEndWithNewline() {
        String formatted = formatter.format(logRecord);
        
        assertThat(formatted).endsWith(System.lineSeparator());
    }
    
    @Test
    void shouldFormatTimestampCorrectly() {
        long timestamp = 1640995200000L; // 2022-01-01 00:00:00 UTC
        logRecord.setMillis(timestamp);
        
        String formatted = formatter.format(logRecord);
        
        // Should contain date in format YYYY-MM-DD
        assertThat(formatted).containsPattern("\\[\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\]");
    }
}