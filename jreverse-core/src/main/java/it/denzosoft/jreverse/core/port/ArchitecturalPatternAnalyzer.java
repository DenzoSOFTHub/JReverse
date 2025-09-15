package it.denzosoft.jreverse.core.port;

import it.denzosoft.jreverse.core.model.ArchitecturalPatternResult;
import it.denzosoft.jreverse.core.model.JarContent;

/**
 * Port for analyzing architectural patterns in Java applications.
 * Identifies common design patterns, architectural patterns, and anti-patterns.
 */
public interface ArchitecturalPatternAnalyzer {
    
    /**
     * Analyzes architectural patterns in the given JAR content.
     * 
     * @param jarContent the JAR content to analyze
     * @return result containing detected patterns, anti-patterns, and architectural metrics
     */
    ArchitecturalPatternResult analyzePatterns(JarContent jarContent);
    
    /**
     * Checks if this analyzer can analyze the given JAR content.
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