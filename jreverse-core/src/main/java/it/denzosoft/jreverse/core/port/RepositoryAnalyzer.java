package it.denzosoft.jreverse.core.port;

import it.denzosoft.jreverse.core.model.JarContent;
import it.denzosoft.jreverse.core.model.RepositoryAnalysisResult;

/**
 * Port interface for analyzing Spring Boot repository layer components.
 * Focuses specifically on @Repository annotations, JpaRepository interfaces,
 * and data access layer patterns.
 */
public interface RepositoryAnalyzer {
    
    /**
     * Analyzes repository layer components in the provided JAR content.
     * Extracts information about @Repository beans, JPA repositories,
     * custom query methods, and data access patterns.
     * 
     * @param jarContent The JAR content to analyze
     * @return The analysis result containing repository layer information
     */
    RepositoryAnalysisResult analyzeRepositories(JarContent jarContent);
    
    /**
     * Checks if this analyzer can analyze the provided JAR content.
     * 
     * @param jarContent The JAR content to check
     * @return true if this analyzer can analyze the content, false otherwise
     */
    boolean canAnalyze(JarContent jarContent);
}