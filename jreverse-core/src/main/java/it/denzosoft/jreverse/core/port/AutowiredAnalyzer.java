package it.denzosoft.jreverse.core.port;

import it.denzosoft.jreverse.core.model.AutowiredAnalysisResult;
import it.denzosoft.jreverse.core.model.JarContent;

/**
 * Port interface for analyzing Spring Boot autowiring patterns and dependency injection.
 * Focuses specifically on @Autowired and @Inject annotations and their usage patterns.
 */
public interface AutowiredAnalyzer {
    
    /**
     * Analyzes autowiring patterns in the provided JAR content.
     * Extracts information about @Autowired and @Inject usage, injection types,
     * and potential autowiring issues.
     * 
     * @param jarContent The JAR content to analyze
     * @return The analysis result containing autowiring information
     */
    AutowiredAnalysisResult analyzeAutowiring(JarContent jarContent);
    
    /**
     * Checks if this analyzer can analyze the provided JAR content.
     * 
     * @param jarContent The JAR content to check
     * @return true if this analyzer can analyze the content, false otherwise
     */
    boolean canAnalyze(JarContent jarContent);
}