package it.denzosoft.jreverse.core.port;

import it.denzosoft.jreverse.core.model.JarContent;
import it.denzosoft.jreverse.core.model.WebMvcAnalysisResult;

/**
 * Analyzer for Spring MVC mapping configurations and request handling patterns.
 * Identifies detailed mapping attributes, conditions, and request/response patterns.
 */
public interface WebMvcAnalyzer {
    
    /**
     * Analyzes JAR content to identify Spring MVC mapping configurations.
     *
     * @param jarContent the JAR content to analyze
     * @return analysis result with detailed MVC mapping information
     */
    WebMvcAnalysisResult analyzeWebMvcMappings(JarContent jarContent);
    
    /**
     * Checks if this analyzer can analyze the given JAR content.
     *
     * @param jarContent the JAR content to check
     * @return true if this analyzer can process the content, false otherwise
     */
    boolean canAnalyze(JarContent jarContent);
}