package it.denzosoft.jreverse.app;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnJre;
import org.junit.jupiter.api.condition.JRE;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for JReverseApplication
 */
public class JReverseApplicationTest {
    
    @Test
    void shouldGetVersionFromPackage() throws Exception {
        Method getVersionMethod = JReverseApplication.class.getDeclaredMethod("getVersion");
        getVersionMethod.setAccessible(true);
        
        String version = (String) getVersionMethod.invoke(null);
        
        assertThat(version).isNotNull();
        // In test environment, it should return development version
        assertThat(version).isEqualTo("1.0.0-SNAPSHOT");
    }
    
    @Test
    void shouldValidateJavaVersionForJava8() throws Exception {
        Method validateMethod = JReverseApplication.class.getDeclaredMethod("isJavaVersionSupported", String.class);
        validateMethod.setAccessible(true);
        
        // Test Java 1.8 format
        boolean isSupported18 = (boolean) validateMethod.invoke(null, "1.8.0_291");
        assertThat(isSupported18).isTrue();
        
        // Test older version (should fail)
        boolean isSupported17 = (boolean) validateMethod.invoke(null, "1.7.0_80");
        assertThat(isSupported17).isFalse();
    }
    
    @Test
    void shouldValidateJavaVersionForJava11Plus() throws Exception {
        Method validateMethod = JReverseApplication.class.getDeclaredMethod("isJavaVersionSupported", String.class);
        validateMethod.setAccessible(true);
        
        // Test Java 11+ format
        boolean isSupported11 = (boolean) validateMethod.invoke(null, "11.0.12");
        assertThat(isSupported11).isTrue();
        
        boolean isSupported17 = (boolean) validateMethod.invoke(null, "17.0.2");
        assertThat(isSupported17).isTrue();
        
        boolean isSupported21 = (boolean) validateMethod.invoke(null, "21.0.1");
        assertThat(isSupported21).isTrue();
    }
    
    @Test
    void shouldHandleInvalidJavaVersion() throws Exception {
        Method validateMethod = JReverseApplication.class.getDeclaredMethod("isJavaVersionSupported", String.class);
        validateMethod.setAccessible(true);
        
        boolean isSupported = (boolean) validateMethod.invoke(null, "invalid-version");
        assertThat(isSupported).isFalse();
    }
    
    @Test
    void shouldParseDebugArgument() throws Exception {
        Method parseMethod = JReverseApplication.class.getDeclaredMethod("parseCommandLineArguments", String[].class);
        parseMethod.setAccessible(true);
        
        CommandLineOptions options = (CommandLineOptions) parseMethod.invoke(null, (Object) new String[]{"--debug"});
        
        assertThat(options.getLogLevel()).isEqualTo(it.denzosoft.jreverse.core.logging.LogLevel.DEBUG);
        assertThat(options.isFileLogging()).isFalse();
        assertThat(options.isGuiMode()).isTrue();
    }
    
    @Test
    void shouldParseTraceArgument() throws Exception {
        Method parseMethod = JReverseApplication.class.getDeclaredMethod("parseCommandLineArguments", String[].class);
        parseMethod.setAccessible(true);
        
        CommandLineOptions options = (CommandLineOptions) parseMethod.invoke(null, (Object) new String[]{"--trace"});
        
        assertThat(options.getLogLevel()).isEqualTo(it.denzosoft.jreverse.core.logging.LogLevel.TRACE);
    }
    
    @Test
    void shouldParseQuietArgument() throws Exception {
        Method parseMethod = JReverseApplication.class.getDeclaredMethod("parseCommandLineArguments", String[].class);
        parseMethod.setAccessible(true);
        
        CommandLineOptions options = (CommandLineOptions) parseMethod.invoke(null, (Object) new String[]{"--quiet"});
        
        assertThat(options.getLogLevel()).isEqualTo(it.denzosoft.jreverse.core.logging.LogLevel.WARN);
    }
    
    @Test
    void shouldParseLogFileArgument() throws Exception {
        Method parseMethod = JReverseApplication.class.getDeclaredMethod("parseCommandLineArguments", String[].class);
        parseMethod.setAccessible(true);
        
        CommandLineOptions options = (CommandLineOptions) parseMethod.invoke(null, (Object) new String[]{"--log-file"});
        
        assertThat(options.isFileLogging()).isTrue();
    }
    
    @Test
    void shouldParseCliArgument() throws Exception {
        Method parseMethod = JReverseApplication.class.getDeclaredMethod("parseCommandLineArguments", String[].class);
        parseMethod.setAccessible(true);
        
        CommandLineOptions options = (CommandLineOptions) parseMethod.invoke(null, (Object) new String[]{"--cli"});
        
        assertThat(options.isGuiMode()).isFalse();
    }
    
    @Test
    void shouldParseMultipleArguments() throws Exception {
        Method parseMethod = JReverseApplication.class.getDeclaredMethod("parseCommandLineArguments", String[].class);
        parseMethod.setAccessible(true);
        
        CommandLineOptions options = (CommandLineOptions) parseMethod.invoke(null, 
            (Object) new String[]{"--debug", "--log-file", "--cli"});
        
        assertThat(options.getLogLevel()).isEqualTo(it.denzosoft.jreverse.core.logging.LogLevel.DEBUG);
        assertThat(options.isFileLogging()).isTrue();
        assertThat(options.isGuiMode()).isFalse();
    }
    
    @Test
    void shouldPrintUsageMessage() throws Exception {
        Method printUsageMethod = JReverseApplication.class.getDeclaredMethod("printUsage");
        printUsageMethod.setAccessible(true);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        
        try {
            System.setOut(new PrintStream(outputStream));
            printUsageMethod.invoke(null);
            
            String usage = outputStream.toString();
            assertThat(usage).contains("JReverse - Java Reverse Engineering Tool");
            assertThat(usage).contains("Usage:");
            assertThat(usage).contains("--debug");
            assertThat(usage).contains("--help");
            assertThat(usage).contains("Examples:");
            
        } finally {
            System.setOut(originalOut);
        }
    }
    
    @Test
    void shouldHandleUnknownArguments() throws Exception {
        Method parseMethod = JReverseApplication.class.getDeclaredMethod("parseCommandLineArguments", String[].class);
        parseMethod.setAccessible(true);
        
        // Should not throw exception for unknown arguments
        CommandLineOptions options = (CommandLineOptions) parseMethod.invoke(null, 
            (Object) new String[]{"--unknown-option", "some-file.jar"});
        
        // Should still have default values
        assertThat(options.getLogLevel()).isEqualTo(it.denzosoft.jreverse.core.logging.LogLevel.INFO);
        assertThat(options.isFileLogging()).isFalse();
        assertThat(options.isGuiMode()).isTrue();
    }
}