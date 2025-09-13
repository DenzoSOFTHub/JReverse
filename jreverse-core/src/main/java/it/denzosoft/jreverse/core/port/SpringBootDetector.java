package it.denzosoft.jreverse.core.port;

import it.denzosoft.jreverse.core.model.JarContent;
import it.denzosoft.jreverse.core.model.springboot.SpringBootDetectionResult;

/**
 * Analyzer for detecting Spring Boot applications in JAR files.
 * Analyzes JAR structure, annotations, dependencies, and main class patterns
 * to determine if the application is built with Spring Boot framework.
 */
public interface SpringBootDetector {
    
    /**
     * Analyzes JAR content to detect Spring Boot patterns and features.
     *
     * @param jarContent the JAR content to analyze
     * @return comprehensive Spring Boot detection results with confidence scores
     */
    SpringBootDetectionResult detectSpringBoot(JarContent jarContent);
}