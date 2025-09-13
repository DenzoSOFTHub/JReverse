package it.denzosoft.jreverse.analyzer.springboot.indicators;

import it.denzosoft.jreverse.analyzer.springboot.SpringBootAnalysisException;
import it.denzosoft.jreverse.analyzer.springboot.SpringBootIndicator;
import it.denzosoft.jreverse.core.logging.JReverseLogger;
import it.denzosoft.jreverse.core.model.AnnotationInfo;
import it.denzosoft.jreverse.core.model.ClassInfo;
import it.denzosoft.jreverse.core.model.JarContent;
import it.denzosoft.jreverse.core.model.springboot.IndicatorResult;
import it.denzosoft.jreverse.core.model.springboot.SpringBootIndicatorType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Indicator that analyzes Spring Boot specific annotations.
 * This is typically the most reliable indicator for Spring Boot detection.
 */
public class SpringBootAnnotationIndicator implements SpringBootIndicator {
    
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(SpringBootAnnotationIndicator.class);
    
    // Spring Boot specific annotations with their confidence weights
    private static final Map<String, Double> SPRING_BOOT_ANNOTATIONS = Map.of(
        "org.springframework.boot.autoconfigure.SpringBootApplication", 0.95,
        "org.springframework.boot.autoconfigure.EnableAutoConfiguration", 0.85,
        "org.springframework.boot.context.properties.EnableConfigurationProperties", 0.7,
        "org.springframework.boot.context.properties.ConfigurationProperties", 0.6,
        "org.springframework.boot.web.servlet.ServletComponentScan", 0.75,
        "org.springframework.boot.autoconfigure.condition.ConditionalOnProperty", 0.5,
        "org.springframework.boot.autoconfigure.condition.ConditionalOnClass", 0.5,
        "org.springframework.boot.test.context.SpringBootTest", 0.8
    );
    
    // Supporting Spring annotations that indicate Spring Boot context
    private static final Map<String, Double> SUPPORTING_ANNOTATIONS = Map.of(
        "org.springframework.context.annotation.ComponentScan", 0.3,
        "org.springframework.context.annotation.Configuration", 0.25,
        "org.springframework.stereotype.Component", 0.1,
        "org.springframework.stereotype.Service", 0.1,
        "org.springframework.stereotype.Repository", 0.1,
        "org.springframework.stereotype.Controller", 0.15,
        "org.springframework.web.bind.annotation.RestController", 0.2
    );
    
    @Override
    public IndicatorResult analyze(JarContent jarContent) {
        LOGGER.debug("Starting Spring Boot annotation analysis for JAR: %s", 
                    jarContent.getLocation().getFileName());
        
        long startTime = System.currentTimeMillis();
        
        try {
            AnnotationAnalysisResult analysisResult = analyzeAnnotations(jarContent);
            
            double confidence = calculateConfidence(analysisResult);
            Map<String, Object> evidence = buildEvidence(analysisResult);
            
            long analysisTime = System.currentTimeMillis() - startTime;
            
            LOGGER.debug("Spring Boot annotation analysis completed. Confidence: %.2f, Time: %dms", 
                        confidence, analysisTime);
            
            return IndicatorResult.builder()
                .confidence(confidence)
                .status(confidence > 0.0 ? IndicatorResult.AnalysisStatus.SUCCESS : IndicatorResult.AnalysisStatus.NOT_FOUND)
                .evidence(evidence)
                .analysisTimeMs(analysisTime)
                .build();
                
        } catch (Exception e) {
            long analysisTime = System.currentTimeMillis() - startTime;
            LOGGER.error("Spring Boot annotation analysis failed", e);
            
            return IndicatorResult.builder()
                .confidence(0.0)
                .status(IndicatorResult.AnalysisStatus.ERROR)
                .errorMessage(e.getMessage())
                .analysisTimeMs(analysisTime)
                .build();
        }
    }
    
    @Override
    public SpringBootIndicatorType getType() {
        return SpringBootIndicatorType.ANNOTATION;
    }
    
    @Override
    public boolean canAnalyze(JarContent jarContent) {
        return jarContent != null && jarContent.getClassCount() > 0;
    }
    
    private AnnotationAnalysisResult analyzeAnnotations(JarContent jarContent) {
        AnnotationAnalysisResult.Builder resultBuilder = AnnotationAnalysisResult.builder();
        
        for (ClassInfo classInfo : jarContent.getClasses()) {
            try {
                analyzeClassAnnotations(classInfo, resultBuilder);
            } catch (Exception e) {
                LOGGER.warn("Failed to analyze annotations for class %s: %s", 
                           classInfo.getFullyQualifiedName(), e.getMessage());
                // Continue with other classes
            }
        }
        
        return resultBuilder.build();
    }
    
    private void analyzeClassAnnotations(ClassInfo classInfo, AnnotationAnalysisResult.Builder resultBuilder) {
        for (AnnotationInfo annotation : classInfo.getAnnotations()) {
            String annotationType = annotation.getType();
            
            // Check for Spring Boot specific annotations
            Double springBootWeight = SPRING_BOOT_ANNOTATIONS.get(annotationType);
            if (springBootWeight != null) {
                resultBuilder.addSpringBootAnnotation(
                    classInfo.getFullyQualifiedName(),
                    annotationType,
                    springBootWeight
                );
                
                // Extract additional information from key annotations
                if ("org.springframework.boot.autoconfigure.SpringBootApplication".equals(annotationType)) {
                    analyzeSpringBootApplicationAnnotation(classInfo, annotation, resultBuilder);
                }
            }
            
            // Check for supporting Spring annotations
            Double supportingWeight = SUPPORTING_ANNOTATIONS.get(annotationType);
            if (supportingWeight != null) {
                resultBuilder.addSupportingAnnotation(
                    classInfo.getFullyQualifiedName(),
                    annotationType,
                    supportingWeight
                );
            }
        }
    }
    
    private void analyzeSpringBootApplicationAnnotation(ClassInfo classInfo, AnnotationInfo annotation, 
                                                       AnnotationAnalysisResult.Builder resultBuilder) {
        // This is likely the main application class
        resultBuilder.mainApplicationClass(classInfo.getFullyQualifiedName());
        
        // Check for configuration attributes
        Map<String, Object> attributes = annotation.getAttributes();
        if (attributes != null && !attributes.isEmpty()) {
            resultBuilder.configurationAttributes(attributes);
        }
    }
    
    private double calculateConfidence(AnnotationAnalysisResult result) {
        double confidence = 0.0;
        
        // Primary confidence from Spring Boot specific annotations
        double springBootConfidence = result.getSpringBootAnnotations().values().stream()
            .mapToDouble(Double::doubleValue)
            .max()
            .orElse(0.0);
        
        // Boost from supporting annotations (but cap the boost)
        double supportingBoost = Math.min(0.2, result.getSupportingAnnotations().values().stream()
            .mapToDouble(Double::doubleValue)
            .sum() * 0.5); // Reduced weight for supporting annotations
        
        confidence = springBootConfidence + supportingBoost;
        
        // Additional boost if we found a main application class
        if (result.getMainApplicationClass() != null) {
            confidence = Math.min(1.0, confidence * 1.1);
        }
        
        // Additional boost for configuration diversity
        if (result.getSpringBootAnnotations().size() > 2) {
            confidence = Math.min(1.0, confidence * 1.05);
        }
        
        return Math.max(0.0, Math.min(1.0, confidence));
    }
    
    private Map<String, Object> buildEvidence(AnnotationAnalysisResult result) {
        Map<String, Object> evidence = new HashMap<>();
        
        evidence.put("springBootAnnotations", result.getSpringBootAnnotations());
        evidence.put("supportingAnnotations", result.getSupportingAnnotations());
        evidence.put("annotationCount", result.getSpringBootAnnotations().size());
        evidence.put("supportingAnnotationCount", result.getSupportingAnnotations().size());
        
        if (result.getMainApplicationClass() != null) {
            evidence.put("mainApplicationClass", result.getMainApplicationClass());
        }
        
        if (result.getConfigurationAttributes() != null && !result.getConfigurationAttributes().isEmpty()) {
            evidence.put("configurationAttributes", result.getConfigurationAttributes());
        }
        
        // Create a summary of found annotation types
        Set<String> annotationTypes = new HashSet<>(result.getSpringBootAnnotations().keySet());
        annotationTypes.addAll(result.getSupportingAnnotations().keySet());
        evidence.put("foundAnnotationTypes", annotationTypes);
        
        return evidence;
    }
    
    /**
     * Internal class to hold annotation analysis results.
     */
    private static class AnnotationAnalysisResult {
        private final Map<String, Double> springBootAnnotations;
        private final Map<String, Double> supportingAnnotations;
        private final String mainApplicationClass;
        private final Map<String, Object> configurationAttributes;
        
        private AnnotationAnalysisResult(Builder builder) {
            this.springBootAnnotations = Map.copyOf(builder.springBootAnnotations);
            this.supportingAnnotations = Map.copyOf(builder.supportingAnnotations);
            this.mainApplicationClass = builder.mainApplicationClass;
            this.configurationAttributes = builder.configurationAttributes != null ? 
                Map.copyOf(builder.configurationAttributes) : null;
        }
        
        public Map<String, Double> getSpringBootAnnotations() {
            return springBootAnnotations;
        }
        
        public Map<String, Double> getSupportingAnnotations() {
            return supportingAnnotations;
        }
        
        public String getMainApplicationClass() {
            return mainApplicationClass;
        }
        
        public Map<String, Object> getConfigurationAttributes() {
            return configurationAttributes;
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private final Map<String, Double> springBootAnnotations = new HashMap<>();
            private final Map<String, Double> supportingAnnotations = new HashMap<>();
            private String mainApplicationClass;
            private Map<String, Object> configurationAttributes;
            
            public Builder addSpringBootAnnotation(String className, String annotationType, double weight) {
                springBootAnnotations.put(annotationType, weight);
                return this;
            }
            
            public Builder addSupportingAnnotation(String className, String annotationType, double weight) {
                supportingAnnotations.put(annotationType, weight);
                return this;
            }
            
            public Builder mainApplicationClass(String className) {
                this.mainApplicationClass = className;
                return this;
            }
            
            public Builder configurationAttributes(Map<String, Object> attributes) {
                this.configurationAttributes = attributes;
                return this;
            }
            
            public AnnotationAnalysisResult build() {
                return new AnnotationAnalysisResult(this);
            }
        }
    }
}