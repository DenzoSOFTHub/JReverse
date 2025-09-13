package it.denzosoft.jreverse.core.port;

import it.denzosoft.jreverse.core.model.JarContent;
import it.denzosoft.jreverse.core.model.ServiceLayerAnalysisResult;

/**
 * Port interface for analyzing Spring Boot service layer components.
 * Focuses specifically on @Service annotations and service layer patterns.
 */
public interface ServiceLayerAnalyzer {
    
    /**
     * Analyzes service layer components in the provided JAR content.
     * Extracts information about @Service beans, their dependencies,
     * business logic patterns, and service layer architecture.
     * 
     * @param jarContent The JAR content to analyze
     * @return The analysis result containing service layer information
     */
    ServiceLayerAnalysisResult analyzeServiceLayer(JarContent jarContent);
    
    /**
     * Checks if this analyzer can analyze the provided JAR content.
     * 
     * @param jarContent The JAR content to check
     * @return true if this analyzer can analyze the content, false otherwise
     */
    boolean canAnalyze(JarContent jarContent);
}