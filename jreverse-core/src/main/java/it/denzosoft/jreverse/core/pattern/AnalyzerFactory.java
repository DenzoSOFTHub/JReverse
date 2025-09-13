package it.denzosoft.jreverse.core.pattern;

import it.denzosoft.jreverse.core.model.JarLocation;
import it.denzosoft.jreverse.core.port.JarAnalyzerPort;

/**
 * Abstract Factory for creating JAR analyzers.
 * Follows Factory Pattern for creation of analyzer instances.
 */
public interface AnalyzerFactory {
    
    /**
     * Creates an analyzer appropriate for the given JAR location.
     * 
     * @param jarLocation the JAR file location
     * @return appropriate analyzer for the JAR type
     */
    JarAnalyzerPort createAnalyzer(JarLocation jarLocation);
    
    /**
     * Creates a default analyzer that works with most JAR files.
     * 
     * @return default JAR analyzer
     */
    JarAnalyzerPort createDefaultAnalyzer();
    
    /**
     * Creates an analyzer specifically optimized for Spring Boot JARs.
     * 
     * @return Spring Boot analyzer
     */
    JarAnalyzerPort createSpringBootAnalyzer();
    
    /**
     * Creates an analyzer for regular (non-Spring Boot) JARs.
     * 
     * @return regular JAR analyzer
     */
    JarAnalyzerPort createRegularJarAnalyzer();
    
    /**
     * Gets the name of this factory implementation.
     * 
     * @return factory name for identification
     */
    String getFactoryName();
    
    /**
     * Gets the supported analyzer types by this factory.
     * 
     * @return array of supported analyzer types
     */
    AnalyzerType[] getSupportedTypes();
}