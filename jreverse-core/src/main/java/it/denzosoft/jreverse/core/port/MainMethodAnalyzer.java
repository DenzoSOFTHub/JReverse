package it.denzosoft.jreverse.core.port;

import it.denzosoft.jreverse.core.model.JarContent;
import it.denzosoft.jreverse.core.model.MainMethodAnalysisResult;

/**
 * Analyzer for detecting and analyzing main methods in JAR files.
 * Identifies Spring Boot main methods with SpringApplication.run() calls
 * and regular Java main methods.
 */
public interface MainMethodAnalyzer {
    
    /**
     * Analyzes JAR content to find and classify main methods.
     *
     * @param jarContent the JAR content to analyze
     * @return analysis result with main method information and Spring Boot detection
     */
    MainMethodAnalysisResult analyzeMainMethod(JarContent jarContent);
    
    /**
     * Checks if this analyzer can analyze the given JAR content.
     *
     * @param jarContent the JAR content to check
     * @return true if this analyzer can process the content, false otherwise
     */
    boolean canAnalyze(JarContent jarContent);
}