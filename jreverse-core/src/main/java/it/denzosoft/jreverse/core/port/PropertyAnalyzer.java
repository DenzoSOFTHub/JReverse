package it.denzosoft.jreverse.core.port;

import it.denzosoft.jreverse.core.model.PropertyAnalysisResult;
import it.denzosoft.jreverse.core.model.JarContent;

/**
 * Port interface for analyzing Spring Boot property configurations and usage.
 * Analyzes @Value annotations, @ConfigurationProperties, and property source configurations.
 */
public interface PropertyAnalyzer {
    
    /**
     * Analyzes property usage patterns in the provided JAR content.
     * 
     * @param jarContent The JAR content to analyze
     * @return The analysis result containing property configuration information
     */
    PropertyAnalysisResult analyzeProperties(JarContent jarContent);
    
    /**
     * Checks if this analyzer can analyze the provided JAR content.
     * 
     * @param jarContent The JAR content to check
     * @return true if this analyzer can analyze the content, false otherwise
     */
    boolean canAnalyze(JarContent jarContent);
}