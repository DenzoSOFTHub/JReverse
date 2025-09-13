package it.denzosoft.jreverse.analyzer.async;

import it.denzosoft.jreverse.core.model.ClassInfo;
import it.denzosoft.jreverse.core.model.JarContent;

/**
 * Analyzer for async entrypoints in Spring applications.
 * Detects and analyzes async patterns including @Async, CompletableFuture, reactive streams, etc.
 */
public interface AsyncEntrypointAnalyzer {
    
    /**
     * Analyzes async entrypoints in the provided JAR.
     *
     * @param jarContent the JAR content containing classes to analyze
     * @return comprehensive async analysis results
     */
    AsyncAnalysisResult analyze(JarContent jarContent);
    
    /**
     * Analyzes async entrypoints in a specific class.
     *
     * @param classInfo the class to analyze for async entrypoints
     * @return async analysis result for the specific class
     */
    AsyncAnalysisResult analyzeClass(ClassInfo classInfo);
}