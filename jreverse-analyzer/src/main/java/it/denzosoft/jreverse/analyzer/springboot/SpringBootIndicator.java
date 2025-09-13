package it.denzosoft.jreverse.analyzer.springboot;

import it.denzosoft.jreverse.core.model.JarContent;
import it.denzosoft.jreverse.core.model.springboot.IndicatorResult;
import it.denzosoft.jreverse.core.model.springboot.SpringBootIndicatorType;

/**
 * Interface for Spring Boot detection indicators.
 * Each indicator analyzes a specific aspect of the JAR to determine Spring Boot likelihood.
 */
public interface SpringBootIndicator {
    
    /**
     * Analyzes the JAR content for Spring Boot indicators.
     * 
     * @param jarContent the JAR content to analyze
     * @return result containing confidence score and evidence
     */
    IndicatorResult analyze(JarContent jarContent);
    
    /**
     * Gets the type of this indicator.
     */
    SpringBootIndicatorType getType();
    
    /**
     * Gets the weight of this indicator for confidence calculations.
     * Higher weights indicate more reliable indicators.
     */
    default double getWeight() {
        return getType().getWeight();
    }
    
    /**
     * Gets the analysis priority for performance optimization.
     * Lower numbers indicate higher priority (should be analyzed first).
     */
    default int getAnalysisPriority() {
        return getType().getAnalysisPriority();
    }
    
    /**
     * Gets a human-readable description of this indicator.
     */
    default String getDescription() {
        return getType().getDescription();
    }
    
    /**
     * Checks if this indicator can analyze the given JAR content.
     * Can be used for conditional analysis based on JAR characteristics.
     * 
     * @param jarContent the JAR content to check
     * @return true if this indicator can analyze the content
     */
    default boolean canAnalyze(JarContent jarContent) {
        return jarContent != null && !jarContent.isEmpty();
    }
    
    /**
     * Gets the expected analysis performance category for this indicator.
     */
    default SpringBootIndicatorType.PerformanceCategory getPerformanceCategory() {
        return getType().getPerformanceCategory();
    }
}