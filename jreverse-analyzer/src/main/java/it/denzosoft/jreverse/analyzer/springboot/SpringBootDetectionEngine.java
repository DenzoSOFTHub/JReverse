package it.denzosoft.jreverse.analyzer.springboot;

import it.denzosoft.jreverse.core.model.JarContent;
import it.denzosoft.jreverse.core.model.springboot.SpringBootDetectionResult;

/**
 * Interface for Spring Boot detection engines.
 * Provides abstraction for different detection strategies and algorithms.
 */
public interface SpringBootDetectionEngine {
    
    /**
     * Analyzes the given JAR content to detect Spring Boot characteristics.
     * 
     * @param jarContent the JAR content to analyze
     * @return detailed detection result with confidence score and evidence
     * @throws SpringBootAnalysisException if analysis fails
     */
    SpringBootDetectionResult detect(JarContent jarContent);
    
    /**
     * Gets the name of this detection engine for logging and diagnostics.
     */
    String getEngineName();
    
    /**
     * Gets the version of this detection engine.
     */
    String getEngineVersion();
    
    /**
     * Checks if this engine supports the analysis of the given JAR content.
     * This can be used for engine selection based on JAR characteristics.
     * 
     * @param jarContent the JAR content to check
     * @return true if this engine can analyze the content
     */
    boolean supports(JarContent jarContent);
    
    /**
     * Gets performance statistics for this engine.
     * Useful for monitoring and optimization.
     */
    EnginePerformanceStats getPerformanceStats();
    
    /**
     * Performance statistics for a detection engine.
     */
    interface EnginePerformanceStats {
        long getTotalAnalyses();
        double getAverageAnalysisTimeMs();
        double getSuccessRate();
        long getCacheHitRate();
    }
}