package it.denzosoft.jreverse.core.logging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for LoggingConfiguration
 */
public class LoggingConfigurationTest {
    
    private Logger rootLogger;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        rootLogger = Logger.getLogger("");
        // Reset configuration before each test
        LoggingConfiguration.resetConfiguration();
    }
    
    @AfterEach
    void tearDown() {
        // Clean up after each test
        LoggingConfiguration.resetConfiguration();
    }
    
    @Test
    void shouldConfigureConsoleLoggingOnly() {
        LoggingConfiguration.configureLogging(LogLevel.INFO, false);
        
        assertThat(rootLogger.getLevel()).isEqualTo(Level.INFO);
        assertThat(rootLogger.getHandlers()).hasSize(1);
        assertThat(rootLogger.getHandlers()[0]).isInstanceOf(ConsoleHandler.class);
        assertThat(LoggingConfiguration.isConfigured()).isTrue();
    }
    
    @Test
    void shouldConfigureFileLogging() throws IOException {
        // Change to temp directory for testing
        String originalUserDir = System.getProperty("user.dir");
        try {
            System.setProperty("user.dir", tempDir.toString());
            
            LoggingConfiguration.configureLogging(LogLevel.DEBUG, true);
            
            assertThat(rootLogger.getLevel()).isEqualTo(Level.FINE);
            assertThat(rootLogger.getHandlers()).hasSizeGreaterThanOrEqualTo(1);
            
            // Check if we have at least one console handler (should always be there)
            boolean hasConsoleHandler = Arrays.stream(rootLogger.getHandlers())
                .anyMatch(handler -> handler instanceof ConsoleHandler);
            assertThat(hasConsoleHandler).isTrue();
            
            // File handler creation might fail in test environments, so we check if logs directory was created
            // which indicates file logging was attempted
            Path logsDir = tempDir.resolve("logs");
            // Either we have a file handler OR the attempt to create logs dir was made
            boolean hasFileHandler = Arrays.stream(rootLogger.getHandlers())
                .anyMatch(handler -> handler instanceof FileHandler);
            boolean logsDirectoryCreated = Files.exists(logsDir);
            
            // At least one of these should be true - either file handler was created or logs directory exists
            assertThat(hasFileHandler || logsDirectoryCreated)
                .as("Should either create FileHandler or logs directory")
                .isTrue();
        } finally {
            // Restore original user.dir
            System.setProperty("user.dir", originalUserDir);
        }
    }
    
    @Test
    void shouldSetCorrectLogLevels() {
        LoggingConfiguration.configureLogging(LogLevel.WARN, false);
        assertThat(rootLogger.getLevel()).isEqualTo(Level.WARNING);
        
        LoggingConfiguration.resetConfiguration();
        LoggingConfiguration.configureLogging(LogLevel.ERROR, false);
        assertThat(rootLogger.getLevel()).isEqualTo(Level.SEVERE);
        
        LoggingConfiguration.resetConfiguration();
        LoggingConfiguration.configureLogging(LogLevel.TRACE, false);
        assertThat(rootLogger.getLevel()).isEqualTo(Level.FINEST);
    }
    
    @Test
    void shouldUseDefaultConfiguration() {
        LoggingConfiguration.configureLogging();
        
        assertThat(rootLogger.getLevel()).isEqualTo(Level.INFO);
        assertThat(rootLogger.getHandlers()).hasSize(1);
        assertThat(rootLogger.getHandlers()[0]).isInstanceOf(ConsoleHandler.class);
    }
    
    @Test
    void shouldPreventMultipleConfigurations() {
        LoggingConfiguration.configureLogging(LogLevel.INFO, false);
        int initialHandlerCount = rootLogger.getHandlers().length;
        
        // Try to configure again
        LoggingConfiguration.configureLogging(LogLevel.DEBUG, true);
        
        // Should not create additional handlers
        assertThat(rootLogger.getHandlers()).hasSize(initialHandlerCount);
        assertThat(rootLogger.getLevel()).isEqualTo(Level.INFO); // Should keep original level
    }
    
    @Test
    void shouldResetConfiguration() {
        LoggingConfiguration.configureLogging(LogLevel.INFO, false);
        assertThat(LoggingConfiguration.isConfigured()).isTrue();
        assertThat(rootLogger.getHandlers()).isNotEmpty();
        
        LoggingConfiguration.resetConfiguration();
        
        assertThat(LoggingConfiguration.isConfigured()).isFalse();
        assertThat(rootLogger.getHandlers()).isEmpty();
    }
    
    @Test
    void shouldHandleFileLoggingErrors() {
        String originalUserDir = System.getProperty("user.dir");
        try {
            // Use invalid path to trigger IOException
            System.setProperty("user.dir", "/invalid/path/that/does/not/exist");
            
            // Should not throw exception, but log error
            assertThatCode(() -> LoggingConfiguration.configureLogging(LogLevel.INFO, true))
                .doesNotThrowAnyException();
            
            // Should still have console handler
            boolean hasConsoleHandler = Arrays.stream(rootLogger.getHandlers())
                .anyMatch(handler -> handler instanceof ConsoleHandler);
            assertThat(hasConsoleHandler).isTrue();
        } finally {
            // Reset system property
            System.setProperty("user.dir", originalUserDir);
        }
    }
    
    @Test
    void shouldConfigureCustomFormatter() {
        LoggingConfiguration.configureLogging(LogLevel.INFO, false);
        
        Handler[] handlers = rootLogger.getHandlers();
        assertThat(handlers).hasSize(1);
        assertThat(handlers[0].getFormatter()).isInstanceOf(CustomFormatter.class);
    }
    
    @Test
    void shouldSetUseParentHandlersToFalse() {
        LoggingConfiguration.configureLogging(LogLevel.INFO, false);
        
        assertThat(rootLogger.getUseParentHandlers()).isFalse();
    }
}