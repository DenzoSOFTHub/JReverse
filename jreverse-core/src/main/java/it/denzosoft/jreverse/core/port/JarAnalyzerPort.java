package it.denzosoft.jreverse.core.port;

import it.denzosoft.jreverse.core.model.JarContent;
import it.denzosoft.jreverse.core.model.JarLocation;
import it.denzosoft.jreverse.core.exception.JarAnalysisException;

/**
 * Output port for JAR analysis functionality.
 * Follows Clean Architecture principles - defines contract for external adapters.
 */
public interface JarAnalyzerPort {
    
    /**
     * Analyzes a JAR file and extracts its content and metadata.
     * 
     * @param location the location of the JAR file to analyze
     * @return analyzed JAR content with all extracted metadata
     * @throws JarAnalysisException if the JAR cannot be analyzed
     */
    JarContent analyzeJar(JarLocation location) throws JarAnalysisException;
    
    /**
     * Checks if the analyzer supports the given JAR file.
     * 
     * @param location the JAR file location to check
     * @return true if this analyzer can handle the JAR file
     */
    boolean supportsJar(JarLocation location);
    
    /**
     * Gets the name of this analyzer implementation.
     * 
     * @return analyzer name for identification purposes
     */
    String getAnalyzerName();
}