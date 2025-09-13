package it.denzosoft.jreverse.core.port;

import it.denzosoft.jreverse.core.model.CallGraphAnalysisResult;
import it.denzosoft.jreverse.core.model.JarContent;

/**
 * Port interface for analyzing call graphs and dependency chains in Spring Boot applications.
 * Traces method calls from HTTP endpoints through service and data access layers.
 */
public interface CallGraphAnalyzer {
    
    /**
     * Analyzes call graphs for all HTTP endpoints in the JAR content.
     * Builds complete dependency chains from controllers to repositories/external services.
     * 
     * @param jarContent The JAR content to analyze
     * @return The analysis result containing call graphs, metrics, and issues
     */
    CallGraphAnalysisResult analyzeCallGraphs(JarContent jarContent);
    
    /**
     * Checks if this analyzer can analyze the provided JAR content.
     * 
     * @param jarContent The JAR content to check
     * @return true if this analyzer can analyze the content, false otherwise
     */
    boolean canAnalyze(JarContent jarContent);
}