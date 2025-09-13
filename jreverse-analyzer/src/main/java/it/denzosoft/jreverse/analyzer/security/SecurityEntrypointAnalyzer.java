package it.denzosoft.jreverse.analyzer.security;

import it.denzosoft.jreverse.core.model.ClassInfo;
import it.denzosoft.jreverse.core.model.JarContent;

/**
 * Analyzer for security entrypoints in Spring applications.
 * Detects and analyzes security annotations such as @PreAuthorize, @PostAuthorize,
 * @Secured, @RolesAllowed, and related JSR-250 security annotations.
 */
public interface SecurityEntrypointAnalyzer {
    
    /**
     * Analyzes security entrypoints in the provided JAR.
     *
     * @param jarContent the JAR content containing classes to analyze
     * @return comprehensive security analysis results
     */
    SecurityAnalysisResult analyze(JarContent jarContent);
    
    /**
     * Analyzes security entrypoints in a specific class.
     *
     * @param classInfo the class to analyze for security entrypoints
     * @return security analysis result for the specific class
     */
    SecurityAnalysisResult analyzeClass(ClassInfo classInfo);
}