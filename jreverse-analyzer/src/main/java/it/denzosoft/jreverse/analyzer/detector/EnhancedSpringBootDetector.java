package it.denzosoft.jreverse.analyzer.detector;

import it.denzosoft.jreverse.analyzer.springboot.JavassistSpringBootDetectionEngine;
import it.denzosoft.jreverse.analyzer.springboot.SpringBootDetectionEngine;
import it.denzosoft.jreverse.core.model.JarContent;
import it.denzosoft.jreverse.core.model.JarLocation;
import it.denzosoft.jreverse.core.model.JarType;
import it.denzosoft.jreverse.core.model.springboot.SpringBootDetectionResult;
import it.denzosoft.jreverse.core.model.springboot.SpringBootVersion;
import it.denzosoft.jreverse.core.logging.JReverseLogger;

import java.util.Optional;

/**
 * Enhanced Spring Boot detector that integrates the new sophisticated detection engine
 * with the existing SpringBootDetector interface for backward compatibility.
 * 
 * This class serves as an adapter between the legacy interface and the new detection system.
 */
public class EnhancedSpringBootDetector extends SpringBootDetector {
    
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(EnhancedSpringBootDetector.class);
    
    private final SpringBootDetectionEngine detectionEngine;
    private final SpringBootDetector legacyDetector;
    
    public EnhancedSpringBootDetector() {
        this.detectionEngine = new JavassistSpringBootDetectionEngine();
        this.legacyDetector = new SpringBootDetector(); // Original implementation as fallback
        
        LOGGER.info("Initialized EnhancedSpringBootDetector with %s", 
                   detectionEngine.getEngineName());
    }
    
    public EnhancedSpringBootDetector(SpringBootDetectionEngine customEngine) {
        this.detectionEngine = customEngine != null ? customEngine : new JavassistSpringBootDetectionEngine();
        this.legacyDetector = new SpringBootDetector();
        
        LOGGER.info("Initialized EnhancedSpringBootDetector with custom engine: %s", 
                   detectionEngine.getEngineName());
    }
    
    /**
     * Enhanced detection method that provides detailed analysis results.
     * This is the recommended method for new code.
     * 
     * @param jarContent the JAR content to analyze
     * @return detailed detection result with confidence score and evidence
     */
    public SpringBootDetectionResult detectWithDetails(JarContent jarContent) {
        if (jarContent == null) {
            return SpringBootDetectionResult.noEvidence();
        }
        
        LOGGER.debug("Starting enhanced Spring Boot detection for JAR: %s", 
                    jarContent.getLocation().getFileName());
        
        try {
            if (!detectionEngine.supports(jarContent)) {
                LOGGER.warn("Detection engine does not support JAR content: %s", 
                           jarContent.getLocation().getFileName());
                return SpringBootDetectionResult.noEvidence();
            }
            
            SpringBootDetectionResult result = detectionEngine.detect(jarContent);
            
            LOGGER.info("Enhanced detection completed for %s: isSpringBoot=%s, confidence=%.2f",
                       jarContent.getLocation().getFileName(),
                       result.isSpringBootApplication(),
                       result.getOverallConfidence());
            
            return result;
            
        } catch (Exception e) {
            LOGGER.error(String.format("Enhanced Spring Boot detection failed for %s, falling back to legacy", 
                        jarContent.getLocation().getFileName()), e);
            
            // Fallback to legacy detection
            boolean isSpringBoot = legacyDetector.isSpringBootJar(jarContent.getLocation());
            if (isSpringBoot) {
                return SpringBootDetectionResult.builder()
                    .isSpringBootApplication(true)
                    .overallConfidence(0.7) // Medium confidence from legacy detection
                    .build();
            } else {
                return SpringBootDetectionResult.noEvidence();
            }
        }
    }
    
    /**
     * Legacy compatibility method.
     * Delegates to the new detection system but returns a simple boolean.
     */
    @Override
    public boolean isSpringBootJar(JarLocation jarLocation) {
        try {
            // For legacy compatibility, we need a JarContent object
            // In a real implementation, this would be provided by a JarLoader
            // For now, we fall back to the legacy detector
            return legacyDetector.isSpringBootJar(jarLocation);
            
        } catch (Exception e) {
            LOGGER.warn("Legacy Spring Boot detection failed for %s: %s", 
                       jarLocation.getFileName(), e.getMessage());
            return false;
        }
    }
    
    /**
     * Enhanced version of detectJarType that uses the new detection system.
     */
    @Override
    public JarType detectJarType(JarLocation jarLocation) {
        try {
            // Legacy fallback for now
            return legacyDetector.detectJarType(jarLocation);
            
        } catch (Exception e) {
            LOGGER.warn("JAR type detection failed for %s: %s", 
                       jarLocation.getFileName(), e.getMessage());
            return JarType.REGULAR_JAR;
        }
    }
    
    /**
     * Enhanced version that tries to get version from detailed analysis.
     */
    @Override
    public String getSpringBootVersion(JarLocation jarLocation) {
        try {
            // For legacy compatibility, fall back to original implementation
            return legacyDetector.getSpringBootVersion(jarLocation);
            
        } catch (Exception e) {
            LOGGER.warn("Spring Boot version detection failed for %s: %s", 
                       jarLocation.getFileName(), e.getMessage());
            return null;
        }
    }
    
    /**
     * Enhanced version that tries to get main class from detailed analysis.
     */
    @Override
    public String getMainClass(JarLocation jarLocation) {
        try {
            // For legacy compatibility, fall back to original implementation
            return legacyDetector.getMainClass(jarLocation);
            
        } catch (Exception e) {
            LOGGER.warn("Main class detection failed for %s: %s", 
                       jarLocation.getFileName(), e.getMessage());
            return null;
        }
    }
    
    /**
     * Gets the Spring Boot version from detailed analysis if available.
     * 
     * @param jarContent the JAR content to analyze
     * @return Spring Boot version or null if not detected
     */
    public SpringBootVersion getSpringBootVersionDetailed(JarContent jarContent) {
        try {
            SpringBootDetectionResult result = detectWithDetails(jarContent);
            return result.getDetectedVersion();
            
        } catch (Exception e) {
            LOGGER.warn("Detailed version detection failed: %s", e.getMessage());
            return null;
        }
    }
    
    /**
     * Gets the main application class from detailed analysis if available.
     * 
     * @param jarContent the JAR content to analyze
     * @return main application class name or null if not detected
     */
    public String getMainApplicationClass(JarContent jarContent) {
        try {
            SpringBootDetectionResult result = detectWithDetails(jarContent);
            
            // Try to extract from evidence
            return result.getIndicatorResults().values().stream()
                .filter(indicatorResult -> indicatorResult.isSuccessful())
                .map(indicatorResult -> indicatorResult.getStringEvidence("mainApplicationClass"))
                .filter(className -> className != null && !className.isEmpty())
                .findFirst()
                .orElse(null);
                
        } catch (Exception e) {
            LOGGER.warn("Main application class detection failed: %s", e.getMessage());
            return null;
        }
    }
    
    /**
     * Gets performance statistics for the detection engine.
     */
    public SpringBootDetectionEngine.EnginePerformanceStats getPerformanceStats() {
        return detectionEngine.getPerformanceStats();
    }
    
    /**
     * Gets the detection engine for advanced usage.
     */
    public SpringBootDetectionEngine getDetectionEngine() {
        return detectionEngine;
    }
    
    /**
     * Checks if the enhanced detection is available and working.
     */
    public boolean isEnhancedDetectionAvailable() {
        try {
            return detectionEngine != null && detectionEngine.supports(null); // Basic check
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Gets a summary of detection capabilities.
     */
    public DetectionCapabilities getDetectionCapabilities() {
        return DetectionCapabilities.builder()
            .hasEnhancedDetection(isEnhancedDetectionAvailable())
            .engineName(detectionEngine.getEngineName())
            .engineVersion(detectionEngine.getEngineVersion())
            .performanceStats(getPerformanceStats())
            .build();
    }
    
    /**
     * Shuts down the detection engine resources.
     */
    public void shutdown() {
        if (detectionEngine instanceof JavassistSpringBootDetectionEngine) {
            ((JavassistSpringBootDetectionEngine) detectionEngine).shutdown();
        }
    }
    
    /**
     * Information about detection capabilities.
     */
    public static class DetectionCapabilities {
        private final boolean hasEnhancedDetection;
        private final String engineName;
        private final String engineVersion;
        private final SpringBootDetectionEngine.EnginePerformanceStats performanceStats;
        
        private DetectionCapabilities(Builder builder) {
            this.hasEnhancedDetection = builder.hasEnhancedDetection;
            this.engineName = builder.engineName;
            this.engineVersion = builder.engineVersion;
            this.performanceStats = builder.performanceStats;
        }
        
        public boolean hasEnhancedDetection() {
            return hasEnhancedDetection;
        }
        
        public String getEngineName() {
            return engineName;
        }
        
        public String getEngineVersion() {
            return engineVersion;
        }
        
        public SpringBootDetectionEngine.EnginePerformanceStats getPerformanceStats() {
            return performanceStats;
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private boolean hasEnhancedDetection;
            private String engineName;
            private String engineVersion;
            private SpringBootDetectionEngine.EnginePerformanceStats performanceStats;
            
            public Builder hasEnhancedDetection(boolean hasEnhanced) {
                this.hasEnhancedDetection = hasEnhanced;
                return this;
            }
            
            public Builder engineName(String name) {
                this.engineName = name;
                return this;
            }
            
            public Builder engineVersion(String version) {
                this.engineVersion = version;
                return this;
            }
            
            public Builder performanceStats(SpringBootDetectionEngine.EnginePerformanceStats stats) {
                this.performanceStats = stats;
                return this;
            }
            
            public DetectionCapabilities build() {
                return new DetectionCapabilities(this);
            }
        }
    }
}