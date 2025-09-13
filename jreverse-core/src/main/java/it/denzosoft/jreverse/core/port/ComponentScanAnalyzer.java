package it.denzosoft.jreverse.core.port;

import it.denzosoft.jreverse.core.model.ComponentScanAnalysisResult;
import it.denzosoft.jreverse.core.model.JarContent;

/**
 * Analyzer for @ComponentScan configurations in Spring applications.
 * Identifies component scanning strategies, base packages, and filters.
 */
public interface ComponentScanAnalyzer {
    
    /**
     * Analyzes JAR content to identify component scan configurations.
     *
     * @param jarContent the JAR content to analyze
     * @return analysis result with component scan details
     */
    ComponentScanAnalysisResult analyzeComponentScan(JarContent jarContent);
    
    /**
     * Checks if this analyzer can analyze the given JAR content.
     *
     * @param jarContent the JAR content to check
     * @return true if this analyzer can process the content, false otherwise
     */
    boolean canAnalyze(JarContent jarContent);
}