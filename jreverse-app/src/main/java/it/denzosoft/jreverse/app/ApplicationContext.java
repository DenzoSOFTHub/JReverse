package it.denzosoft.jreverse.app;

import it.denzosoft.jreverse.core.logging.JReverseLogger;
import it.denzosoft.jreverse.core.port.JarAnalyzerPort;
import it.denzosoft.jreverse.analyzer.impl.WorkingEnhancedAnalyzer;

/**
 * Simplified application context for CLI analysis.
 * Provides basic JAR analysis capabilities with optimizations.
 */
public class ApplicationContext {

    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(ApplicationContext.class);

    private final JarAnalyzerPort jarAnalyzer;

    public ApplicationContext() {
        LOGGER.info("Initializing simplified application context");

        // Initialize enhanced analyzer with Phase 3 capabilities
        this.jarAnalyzer = new WorkingEnhancedAnalyzer();

        LOGGER.info("Application context initialized successfully");
    }

    public JarAnalyzerPort getJarAnalyzer() {
        return jarAnalyzer;
    }

    /**
     * Shutdown the application context and clean up resources
     */
    public void shutdown() {
        LOGGER.info("Shutting down application context");
        // No cleanup needed for simplified context
        LOGGER.info("Application context shutdown completed");
    }
}