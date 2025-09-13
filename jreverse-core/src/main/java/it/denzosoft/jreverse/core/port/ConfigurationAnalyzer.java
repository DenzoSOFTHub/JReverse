package it.denzosoft.jreverse.core.port;

import it.denzosoft.jreverse.core.model.ConfigurationAnalysisResult;
import it.denzosoft.jreverse.core.model.JarContent;

/**
 * Analyzer for Spring configuration classes and bean definitions.
 * Identifies @Configuration classes, @Bean methods, and bean metadata.
 */
public interface ConfigurationAnalyzer {
    
    /**
     * Analyzes JAR content to identify Spring configuration classes and bean definitions.
     *
     * @param jarContent the JAR content to analyze
     * @return analysis result with configuration and bean information
     */
    ConfigurationAnalysisResult analyzeConfigurations(JarContent jarContent);
    
    /**
     * Checks if this analyzer can analyze the given JAR content.
     *
     * @param jarContent the JAR content to check
     * @return true if this analyzer can process the content, false otherwise
     */
    boolean canAnalyze(JarContent jarContent);
}