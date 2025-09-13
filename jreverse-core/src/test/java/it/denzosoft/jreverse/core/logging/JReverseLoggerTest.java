package it.denzosoft.jreverse.core.logging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogRecord;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for JReverseLogger
 */
public class JReverseLoggerTest {
    
    private JReverseLogger jReverseLogger;
    private TestLogHandler testHandler;
    private Logger underlyingLogger;
    
    @BeforeEach
    void setUp() {
        jReverseLogger = JReverseLogger.getLogger(JReverseLoggerTest.class);
        underlyingLogger = jReverseLogger.getUnderlyingLogger();
        
        testHandler = new TestLogHandler();
        testHandler.setLevel(Level.ALL);
        underlyingLogger.addHandler(testHandler);
        underlyingLogger.setLevel(Level.ALL);
        underlyingLogger.setUseParentHandlers(false);
    }
    
    @AfterEach
    void tearDown() {
        underlyingLogger.removeHandler(testHandler);
        testHandler.close();
    }
    
    @Test
    void shouldCreateLoggerForClass() {
        JReverseLogger logger = JReverseLogger.getLogger(String.class);
        
        assertThat(logger).isNotNull();
        assertThat(logger.getUnderlyingLogger().getName()).isEqualTo(String.class.getName());
    }
    
    @Test
    void shouldCreateLoggerForName() {
        String loggerName = "test.logger";
        JReverseLogger logger = JReverseLogger.getLogger(loggerName);
        
        assertThat(logger).isNotNull();
        assertThat(logger.getUnderlyingLogger().getName()).isEqualTo(loggerName);
    }
    
    @Test
    void shouldLogInfoMessage() {
        jReverseLogger.info("Test info message");
        
        List<LogRecord> records = testHandler.getRecords();
        assertThat(records).hasSize(1);
        assertThat(records.get(0).getLevel()).isEqualTo(Level.INFO);
        assertThat(records.get(0).getMessage()).isEqualTo("Test info message");
    }
    
    @Test
    void shouldLogInfoMessageWithParameters() {
        jReverseLogger.info("Test message with param: %s", "value");
        
        List<LogRecord> records = testHandler.getRecords();
        assertThat(records).hasSize(1);
        assertThat(records.get(0).getMessage()).isEqualTo("Test message with param: value");
    }
    
    @Test
    void shouldLogDebugMessage() {
        jReverseLogger.debug("Debug message");
        
        List<LogRecord> records = testHandler.getRecords();
        assertThat(records).hasSize(1);
        assertThat(records.get(0).getLevel()).isEqualTo(Level.FINE);
        assertThat(records.get(0).getMessage()).isEqualTo("Debug message");
    }
    
    @Test
    void shouldLogTraceMessage() {
        jReverseLogger.trace("Trace message");
        
        List<LogRecord> records = testHandler.getRecords();
        assertThat(records).hasSize(1);
        assertThat(records.get(0).getLevel()).isEqualTo(Level.FINEST);
        assertThat(records.get(0).getMessage()).isEqualTo("Trace message");
    }
    
    @Test
    void shouldLogWarningMessage() {
        jReverseLogger.warn("Warning message");
        
        List<LogRecord> records = testHandler.getRecords();
        assertThat(records).hasSize(1);
        assertThat(records.get(0).getLevel()).isEqualTo(Level.WARNING);
        assertThat(records.get(0).getMessage()).isEqualTo("Warning message");
    }
    
    @Test
    void shouldLogErrorMessage() {
        jReverseLogger.error("Error message");
        
        List<LogRecord> records = testHandler.getRecords();
        assertThat(records).hasSize(1);
        assertThat(records.get(0).getLevel()).isEqualTo(Level.SEVERE);
        assertThat(records.get(0).getMessage()).isEqualTo("Error message");
    }
    
    @Test
    void shouldLogErrorMessageWithException() {
        Exception exception = new RuntimeException("Test exception");
        jReverseLogger.error("Error occurred", exception);
        
        List<LogRecord> records = testHandler.getRecords();
        assertThat(records).hasSize(1);
        LogRecord record = records.get(0);
        assertThat(record.getLevel()).isEqualTo(Level.SEVERE);
        assertThat(record.getMessage()).isEqualTo("Error occurred");
        assertThat(record.getThrown()).isEqualTo(exception);
    }
    
    @Test
    void shouldLogErrorMessageWithParametersAndException() {
        Exception exception = new RuntimeException("Test exception");
        jReverseLogger.error("Error in operation: %s", exception, "testOp");
        
        List<LogRecord> records = testHandler.getRecords();
        assertThat(records).hasSize(1);
        LogRecord record = records.get(0);
        assertThat(record.getLevel()).isEqualTo(Level.SEVERE);
        assertThat(record.getMessage()).isEqualTo("Error in operation: testOp");
        assertThat(record.getThrown()).isEqualTo(exception);
    }
    
    @Test
    void shouldHandleFormattingErrors() {
        // Test with wrong number of parameters
        jReverseLogger.info("Message with %s and %s", "only_one_param");
        
        List<LogRecord> records = testHandler.getRecords();
        assertThat(records).hasSize(1);
        // Should contain original message and format error indication
        assertThat(records.get(0).getMessage()).contains("Message with %s and %s");
        assertThat(records.get(0).getMessage()).contains("Format error:");
    }
    
    @Test
    void shouldCheckDebugEnabled() {
        underlyingLogger.setLevel(Level.FINE);
        assertThat(jReverseLogger.isDebugEnabled()).isTrue();
        
        underlyingLogger.setLevel(Level.INFO);
        assertThat(jReverseLogger.isDebugEnabled()).isFalse();
    }
    
    @Test
    void shouldCheckTraceEnabled() {
        underlyingLogger.setLevel(Level.FINEST);
        assertThat(jReverseLogger.isTraceEnabled()).isTrue();
        
        underlyingLogger.setLevel(Level.FINE);
        assertThat(jReverseLogger.isTraceEnabled()).isFalse();
    }
    
    @Test
    void shouldStartAndEndOperation() {
        try (JReverseLogger.OperationTimer timer = jReverseLogger.startOperation("TestOperation")) {
            // Simulate some work
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        List<LogRecord> records = testHandler.getRecords();
        assertThat(records).hasSize(2);
        
        // First record: start operation
        assertThat(records.get(0).getMessage()).contains("Starting operation: TestOperation");
        
        // Second record: end operation
        assertThat(records.get(1).getMessage()).contains("Completed operation: TestOperation in");
        assertThat(records.get(1).getMessage()).contains("ms");
    }
    
    @Test
    void shouldMeasureOperationTime() throws InterruptedException {
        JReverseLogger.OperationTimer timer = jReverseLogger.startOperation("TimedOperation");
        
        Thread.sleep(50); // Sleep for 50ms
        
        long elapsed = timer.getElapsedTime();
        timer.close();
        
        assertThat(elapsed).isGreaterThanOrEqualTo(40); // Allow some variance
    }
    
    @Test
    void shouldSkipLoggingWhenLevelTooLow() {
        underlyingLogger.setLevel(Level.WARNING);
        
        jReverseLogger.info("This should not be logged");
        jReverseLogger.debug("This should not be logged");
        
        List<LogRecord> records = testHandler.getRecords();
        assertThat(records).isEmpty();
    }
    
    @Test
    void shouldLogWhenLevelAppropriate() {
        underlyingLogger.setLevel(Level.WARNING);
        
        jReverseLogger.warn("This should be logged");
        jReverseLogger.error("This should be logged");
        
        List<LogRecord> records = testHandler.getRecords();
        assertThat(records).hasSize(2);
    }
}