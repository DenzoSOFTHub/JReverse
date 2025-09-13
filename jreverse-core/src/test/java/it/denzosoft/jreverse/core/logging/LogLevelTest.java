package it.denzosoft.jreverse.core.logging;

import org.junit.jupiter.api.Test;

import java.util.logging.Level;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for LogLevel enum
 */
public class LogLevelTest {
    
    @Test
    void shouldMapToCorrectJavaLevels() {
        assertThat(LogLevel.TRACE.toJavaLevel()).isEqualTo(Level.FINEST);
        assertThat(LogLevel.DEBUG.toJavaLevel()).isEqualTo(Level.FINE);
        assertThat(LogLevel.INFO.toJavaLevel()).isEqualTo(Level.INFO);
        assertThat(LogLevel.WARN.toJavaLevel()).isEqualTo(Level.WARNING);
        assertThat(LogLevel.ERROR.toJavaLevel()).isEqualTo(Level.SEVERE);
    }
    
    @Test
    void shouldConvertFromJavaLevels() {
        assertThat(LogLevel.fromJavaLevel(Level.FINEST)).isEqualTo(LogLevel.TRACE);
        assertThat(LogLevel.fromJavaLevel(Level.FINE)).isEqualTo(LogLevel.DEBUG);
        assertThat(LogLevel.fromJavaLevel(Level.INFO)).isEqualTo(LogLevel.INFO);
        assertThat(LogLevel.fromJavaLevel(Level.WARNING)).isEqualTo(LogLevel.WARN);
        assertThat(LogLevel.fromJavaLevel(Level.SEVERE)).isEqualTo(LogLevel.ERROR);
    }
    
    @Test
    void shouldReturnInfoForUnknownJavaLevel() {
        LogLevel result = LogLevel.fromJavaLevel(Level.CONFIG);
        
        assertThat(result).isEqualTo(LogLevel.INFO);
    }
    
    @Test
    void shouldHaveAllExpectedValues() {
        LogLevel[] values = LogLevel.values();
        
        assertThat(values).hasSize(5);
        assertThat(values).containsExactly(
            LogLevel.TRACE, 
            LogLevel.DEBUG, 
            LogLevel.INFO, 
            LogLevel.WARN, 
            LogLevel.ERROR
        );
    }
    
    @Test
    void shouldBeOrderedByLogLevel() {
        // Verify ordering from least to most severe
        assertThat(LogLevel.TRACE.ordinal()).isLessThan(LogLevel.DEBUG.ordinal());
        assertThat(LogLevel.DEBUG.ordinal()).isLessThan(LogLevel.INFO.ordinal());
        assertThat(LogLevel.INFO.ordinal()).isLessThan(LogLevel.WARN.ordinal());
        assertThat(LogLevel.WARN.ordinal()).isLessThan(LogLevel.ERROR.ordinal());
    }
}