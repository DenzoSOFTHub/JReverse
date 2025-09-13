package it.denzosoft.jreverse.core.port;

import it.denzosoft.jreverse.core.model.BeanCreationAnalysisResult;
import it.denzosoft.jreverse.core.model.JarContent;

/**
 * Port interface for analyzing Spring Boot bean creation and lifecycle patterns.
 * Analyzes bean instantiation methods, lifecycle callbacks, and creation dependencies.
 */
public interface BeanCreationAnalyzer {
    
    /**
     * Analyzes bean creation patterns in the provided JAR content.
     * 
     * @param jarContent The JAR content to analyze
     * @return The analysis result containing bean creation information
     */
    BeanCreationAnalysisResult analyzeBeanCreation(JarContent jarContent);
    
    /**
     * Checks if this analyzer can analyze the provided JAR content.
     * 
     * @param jarContent The JAR content to check
     * @return true if this analyzer can analyze the content, false otherwise
     */
    boolean canAnalyze(JarContent jarContent);
}