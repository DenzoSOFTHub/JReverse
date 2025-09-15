package it.denzosoft.jreverse.core.port;

import it.denzosoft.jreverse.core.model.LayeredArchitectureResult;
import it.denzosoft.jreverse.core.model.JarContent;

/**
 * Port for analyzing layered architecture compliance in Java applications.
 * Validates layer separation, dependency direction, and architectural principles.
 */
public interface LayeredArchitectureAnalyzer {
    
    /**
     * Analyzes layered architecture compliance in the given JAR content.
     * 
     * @param jarContent the JAR content to analyze
     * @return result containing layer analysis, violations, and architectural quality metrics
     */
    LayeredArchitectureResult analyzeLayeredArchitecture(JarContent jarContent);
    
    /**
     * Checks if this analyzer can analyze the given JAR content for layered architecture.
     * 
     * @param jarContent the JAR content to check
     * @return true if this analyzer can analyze the content, false otherwise
     */
    boolean canAnalyze(JarContent jarContent);
    
    /**
     * Performs lifecycle management for the analyzer.
     */
    void shutdown();
}