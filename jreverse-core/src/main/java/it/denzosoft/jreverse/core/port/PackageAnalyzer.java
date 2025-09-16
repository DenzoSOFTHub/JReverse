package it.denzosoft.jreverse.core.port;

import it.denzosoft.jreverse.core.model.JarContent;
import it.denzosoft.jreverse.core.model.PackageAnalysisResult;

/**
 * Port for package structure analysis.
 * Analyzes package hierarchy, naming conventions, organization patterns and architectural compliance.
 */
public interface PackageAnalyzer {
    
    /**
     * Analyzes the package structure of the JAR content.
     * 
     * @param jarContent the JAR content to analyze
     * @return the package analysis result
     */
    PackageAnalysisResult analyzePackageStructure(JarContent jarContent);
    
    /**
     * Checks if this analyzer can analyze the given JAR content.
     * 
     * @param jarContent the JAR content to check
     * @return true if the analyzer can process this JAR content
     */
    boolean canAnalyze(JarContent jarContent);
    
    /**
     * Gets the analyzer name for identification purposes.
     * 
     * @return the analyzer name
     */
    String getAnalyzerName();
}