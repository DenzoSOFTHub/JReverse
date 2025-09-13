package it.denzosoft.jreverse.analyzer.springboot;

import it.denzosoft.jreverse.core.logging.JReverseLogger;
import it.denzosoft.jreverse.core.model.JarContent;
import it.denzosoft.jreverse.core.model.springboot.SpringBootDetectionResult;
import it.denzosoft.jreverse.core.port.SpringBootDetector;

/**
 * Javassist-based implementation of SpringBootDetector.
 * Bridges the SpringBootDetector interface to the JavassistSpringBootDetectionEngine.
 */
public class JavassistSpringBootDetector implements SpringBootDetector {
    
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(JavassistSpringBootDetector.class);
    
    private final SpringBootDetectionEngine detectionEngine;
    
    public JavassistSpringBootDetector() {
        this.detectionEngine = new JavassistSpringBootDetectionEngine();
        LOGGER.info("Initialized JavassistSpringBootDetector with engine: %s v%s", 
                   detectionEngine.getEngineName(), detectionEngine.getEngineVersion());
    }
    
    /**
     * For testing purposes - allows injection of custom detection engine.
     */
    public JavassistSpringBootDetector(SpringBootDetectionEngine detectionEngine) {
        this.detectionEngine = detectionEngine;
        LOGGER.info("Initialized JavassistSpringBootDetector with custom engine: %s v%s", 
                   detectionEngine.getEngineName(), detectionEngine.getEngineVersion());
    }
    
    @Override
    public SpringBootDetectionResult detectSpringBoot(JarContent jarContent) {
        if (jarContent == null) {
            LOGGER.warn("Cannot analyze null JAR content");
            return SpringBootDetectionResult.noEvidence();
        }
        
        LOGGER.info("Starting Spring Boot detection for JAR: %s", 
                   jarContent.getLocation() != null ? jarContent.getLocation().getFileName() : "unknown");
        
        try {
            if (!detectionEngine.supports(jarContent)) {
                LOGGER.warn("Detection engine does not support this JAR content");
                return SpringBootDetectionResult.noEvidence();
            }
            
            SpringBootDetectionResult result = detectionEngine.detect(jarContent);
            
            LOGGER.info("Spring Boot detection completed for %s: isSpringBoot=%s, confidence=%.2f", 
                       jarContent.getLocation() != null ? jarContent.getLocation().getFileName() : "unknown",
                       result.isSpringBootApplication(), 
                       result.getOverallConfidence());
            
            return result;
            
        } catch (Exception e) {
            LOGGER.error("Spring Boot detection failed", e);
            return SpringBootDetectionResult.noEvidence();
        }
    }
    
    /**
     * Gets the underlying detection engine for advanced operations.
     * 
     * @return the detection engine instance
     */
    public SpringBootDetectionEngine getDetectionEngine() {
        return detectionEngine;
    }
    
    /**
     * Gets performance statistics from the underlying engine.
     * 
     * @return performance statistics
     */
    public SpringBootDetectionEngine.EnginePerformanceStats getPerformanceStats() {
        return detectionEngine.getPerformanceStats();
    }
    
    /**
     * Checks if the detector can analyze the given JAR content.
     * 
     * @param jarContent the JAR content to check
     * @return true if the JAR can be analyzed
     */
    public boolean canAnalyze(JarContent jarContent) {
        return jarContent != null && detectionEngine.supports(jarContent);
    }
}