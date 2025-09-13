package it.denzosoft.jreverse.analyzer.mainmethod;

import it.denzosoft.jreverse.core.model.JarContent;
import it.denzosoft.jreverse.core.model.MainMethodAnalysisResult;

/**
 * Analyzer for identifying and analyzing main methods in JAR files,
 * with special focus on Spring Boot applications.
 */
public interface MainMethodAnalyzer {
    
    /**
     * Analyzes the JAR content to find and analyze main methods.
     *
     * @param jarContent the JAR content to analyze
     * @return the analysis result containing information about found main methods
     */
    MainMethodAnalysisResult analyzeMainMethod(JarContent jarContent);
    
    /**
     * Checks if this analyzer can analyze the given JAR content.
     *
     * @param jarContent the JAR content to check
     * @return true if the analyzer can process this content
     */
    default boolean canAnalyze(JarContent jarContent) {
        return jarContent != null && jarContent.getClassCount() > 0;
    }
}