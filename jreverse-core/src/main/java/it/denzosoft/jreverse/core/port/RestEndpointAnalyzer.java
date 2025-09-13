package it.denzosoft.jreverse.core.port;

import it.denzosoft.jreverse.core.model.JarContent;
import it.denzosoft.jreverse.core.model.RestEndpointAnalysisResult;

/**
 * Analyzer for REST endpoints in Spring applications.
 * Identifies controllers, request mappings, and HTTP method configurations.
 */
public interface RestEndpointAnalyzer {
    
    /**
     * Analyzes JAR content to identify REST endpoints from Spring controllers.
     *
     * @param jarContent the JAR content to analyze
     * @return analysis result with discovered REST endpoints
     */
    RestEndpointAnalysisResult analyzeRestEndpoints(JarContent jarContent);
    
    /**
     * Checks if this analyzer can analyze the given JAR content.
     *
     * @param jarContent the JAR content to check
     * @return true if this analyzer can process the content, false otherwise
     */
    boolean canAnalyze(JarContent jarContent);
}