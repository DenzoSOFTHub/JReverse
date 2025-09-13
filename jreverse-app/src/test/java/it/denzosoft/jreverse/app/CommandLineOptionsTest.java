package it.denzosoft.jreverse.app;

import it.denzosoft.jreverse.core.logging.LogLevel;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for CommandLineOptions
 */
public class CommandLineOptionsTest {
    
    @Test
    void shouldCreateWithDefaultValues() {
        CommandLineOptions options = CommandLineOptions.builder().build();
        
        assertThat(options.getLogLevel()).isEqualTo(LogLevel.INFO);
        assertThat(options.isFileLogging()).isFalse();
        assertThat(options.isGuiMode()).isTrue();
    }
    
    @Test
    void shouldCreateWithCustomLogLevel() {
        CommandLineOptions options = CommandLineOptions.builder()
            .logLevel(LogLevel.DEBUG)
            .build();
        
        assertThat(options.getLogLevel()).isEqualTo(LogLevel.DEBUG);
        assertThat(options.isFileLogging()).isFalse();
        assertThat(options.isGuiMode()).isTrue();
    }
    
    @Test
    void shouldCreateWithFileLogging() {
        CommandLineOptions options = CommandLineOptions.builder()
            .fileLogging(true)
            .build();
        
        assertThat(options.getLogLevel()).isEqualTo(LogLevel.INFO);
        assertThat(options.isFileLogging()).isTrue();
        assertThat(options.isGuiMode()).isTrue();
    }
    
    @Test
    void shouldCreateWithCliMode() {
        CommandLineOptions options = CommandLineOptions.builder()
            .guiMode(false)
            .build();
        
        assertThat(options.getLogLevel()).isEqualTo(LogLevel.INFO);
        assertThat(options.isFileLogging()).isFalse();
        assertThat(options.isGuiMode()).isFalse();
    }
    
    @Test
    void shouldCreateWithAllOptions() {
        CommandLineOptions options = CommandLineOptions.builder()
            .logLevel(LogLevel.TRACE)
            .fileLogging(true)
            .guiMode(false)
            .build();
        
        assertThat(options.getLogLevel()).isEqualTo(LogLevel.TRACE);
        assertThat(options.isFileLogging()).isTrue();
        assertThat(options.isGuiMode()).isFalse();
    }
    
    @Test
    void shouldSupportMethodChaining() {
        CommandLineOptions options = CommandLineOptions.builder()
            .logLevel(LogLevel.WARN)
            .fileLogging(true)
            .guiMode(false)
            .build();
        
        assertThat(options.getLogLevel()).isEqualTo(LogLevel.WARN);
        assertThat(options.isFileLogging()).isTrue();
        assertThat(options.isGuiMode()).isFalse();
    }
    
    @Test
    void shouldHandleNullLogLevel() {
        CommandLineOptions options = CommandLineOptions.builder()
            .logLevel(null)
            .build();
        
        // Should default to INFO when null
        assertThat(options.getLogLevel()).isEqualTo(LogLevel.INFO);
    }
    
    @Test
    void shouldHaveProperToString() {
        CommandLineOptions options = CommandLineOptions.builder()
            .logLevel(LogLevel.DEBUG)
            .fileLogging(true)
            .guiMode(false)
            .build();
        
        String toString = options.toString();
        
        assertThat(toString).contains("CommandLineOptions{");
        assertThat(toString).contains("logLevel=DEBUG");
        assertThat(toString).contains("fileLogging=true");
        assertThat(toString).contains("guiMode=false");
    }
    
    @Test
    void shouldBeImmutable() {
        CommandLineOptions.Builder builder = CommandLineOptions.builder()
            .logLevel(LogLevel.DEBUG)
            .fileLogging(true);
        
        CommandLineOptions options1 = builder.build();
        
        // Modify builder after first build
        CommandLineOptions options2 = builder
            .logLevel(LogLevel.ERROR)
            .fileLogging(false)
            .build();
        
        // First instance should remain unchanged
        assertThat(options1.getLogLevel()).isEqualTo(LogLevel.DEBUG);
        assertThat(options1.isFileLogging()).isTrue();
        
        // Second instance should have new values
        assertThat(options2.getLogLevel()).isEqualTo(LogLevel.ERROR);
        assertThat(options2.isFileLogging()).isFalse();
    }
}