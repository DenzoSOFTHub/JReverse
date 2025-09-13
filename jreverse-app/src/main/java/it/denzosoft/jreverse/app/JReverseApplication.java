package it.denzosoft.jreverse.app;

import it.denzosoft.jreverse.core.logging.JReverseLogger;
import it.denzosoft.jreverse.core.logging.LogLevel;
import it.denzosoft.jreverse.core.logging.LoggingConfiguration;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.util.Arrays;

/**
 * Main application class for JReverse Java Reverse Engineering Tool.
 * Provides entry point and initialization of the application.
 */
public class JReverseApplication {
    
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(JReverseApplication.class);
    private static final String REQUIRED_JAVA_VERSION = "1.8";
    
    public static void main(String[] args) {
        try {
            // Parse command line arguments
            CommandLineOptions options = parseCommandLineArguments(args);
            
            // Initialize logging
            LoggingConfiguration.configureLogging(options.getLogLevel(), options.isFileLogging());
            
            LOGGER.info("Starting JReverse Application v%s", getVersion());
            LOGGER.info("Arguments: %s", Arrays.toString(args));
            
            // Check Java version compatibility
            validateJavaVersion();
            
            // Initialize application context
            ApplicationContext context = new ApplicationContext();
            
            if (options.isGuiMode()) {
                // Launch Swing UI
                launchSwingUI(context);
            } else {
                // Run in CLI mode (future implementation)
                LOGGER.info("CLI mode not yet implemented - launching GUI");
                launchSwingUI(context);
            }
            
        } catch (Exception e) {
            System.err.println("Failed to start JReverse Application: " + e.getMessage());
            LOGGER.error("Application startup failed", e);
            System.exit(1);
        }
    }
    
    private static void launchSwingUI(ApplicationContext context) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Set system look and feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                
                LOGGER.info("Launching Swing UI");
                
                // TODO: Create and show main window when UI module is implemented
                // For now, just show a placeholder message
                showPlaceholderUI();
                
            } catch (Exception e) {
                LOGGER.error("Failed to start UI", e);
                showErrorDialog("Failed to start application UI: " + e.getMessage());
                System.exit(1);
            }
        });
    }
    
    private static void showPlaceholderUI() {
        javax.swing.JOptionPane.showMessageDialog(
            null,
            "JReverse Application Started Successfully!\n\n" +
            "Version: " + getVersion() + "\n" +
            "Java Version: " + System.getProperty("java.version") + "\n\n" +
            "UI Module will be implemented in future phases.",
            "JReverse - Application Started",
            javax.swing.JOptionPane.INFORMATION_MESSAGE
        );
        
        LOGGER.info("Placeholder UI shown - application will exit");
        System.exit(0);
    }
    
    private static void showErrorDialog(String message) {
        javax.swing.JOptionPane.showMessageDialog(
            null,
            message,
            "JReverse - Error",
            javax.swing.JOptionPane.ERROR_MESSAGE
        );
    }
    
    private static CommandLineOptions parseCommandLineArguments(String[] args) {
        CommandLineOptions.Builder builder = CommandLineOptions.builder();
        
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            
            switch (arg) {
                case "--debug":
                    builder.logLevel(LogLevel.DEBUG);
                    break;
                case "--trace":
                    builder.logLevel(LogLevel.TRACE);
                    break;
                case "--quiet":
                    builder.logLevel(LogLevel.WARN);
                    break;
                case "--log-file":
                    builder.fileLogging(true);
                    break;
                case "--cli":
                    builder.guiMode(false);
                    break;
                case "--help":
                case "-h":
                    printUsage();
                    System.exit(0);
                    break;
                case "--version":
                case "-v":
                    System.out.println("JReverse v" + getVersion());
                    System.exit(0);
                    break;
                default:
                    if (arg.startsWith("--")) {
                        LOGGER.warn("Unknown option: %s", arg);
                    }
                    // Ignore unknown options for now
                    break;
            }
        }
        
        return builder.build();
    }
    
    private static void printUsage() {
        String usage = "JReverse - Java Reverse Engineering Tool v" + getVersion() + "\n\n" +
                      "Usage: java -jar jreverse.jar [OPTIONS]\n\n" +
                      "Options:\n" +
                      "  --debug        Enable debug logging\n" +
                      "  --trace        Enable trace logging (very verbose)\n" +
                      "  --quiet        Only show warnings and errors\n" +
                      "  --log-file     Enable logging to file\n" +
                      "  --cli          Run in command-line mode (not yet implemented)\n" +
                      "  --help, -h     Show this help message\n" +
                      "  --version, -v  Show version information\n\n" +
                      "Examples:\n" +
                      "  java -jar jreverse.jar\n" +
                      "  java -jar jreverse.jar --debug --log-file\n" +
                      "  java -jar jreverse.jar --quiet";
        
        System.out.println(usage);
    }
    
    private static void validateJavaVersion() {
        String version = System.getProperty("java.version");
        LOGGER.debug("Detected Java version: %s", version);
        
        if (!isJavaVersionSupported(version)) {
            String message = String.format("Java %s or higher is required. Current version: %s", 
                                          REQUIRED_JAVA_VERSION, version);
            throw new RuntimeException(message);
        }
        
        LOGGER.info("Java version validation passed: %s", version);
    }
    
    private static boolean isJavaVersionSupported(String version) {
        try {
            // Handle different Java version formats
            String majorVersion;
            if (version.startsWith("1.")) {
                // Java 1.8 format: "1.8.0_xxx"
                majorVersion = version.substring(0, 3);
                return majorVersion.compareTo(REQUIRED_JAVA_VERSION) >= 0;
            } else {
                // Java 9+ format: "11.0.1", "17.0.2", etc.
                int dotIndex = version.indexOf('.');
                majorVersion = dotIndex > 0 ? version.substring(0, dotIndex) : version;
                int versionNumber = Integer.parseInt(majorVersion);
                return versionNumber >= 8; // Java 8 = version 8
            }
        } catch (Exception e) {
            LOGGER.warn("Could not parse Java version: %s", version);
            return false;
        }
    }
    
    private static String getVersion() {
        Package pkg = JReverseApplication.class.getPackage();
        String version = pkg.getImplementationVersion();
        return version != null ? version : "1.0.0-SNAPSHOT";
    }
}