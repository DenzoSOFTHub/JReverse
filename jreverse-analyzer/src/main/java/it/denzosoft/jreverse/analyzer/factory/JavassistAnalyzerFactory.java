package it.denzosoft.jreverse.analyzer.factory;

import it.denzosoft.jreverse.core.model.JarLocation;
import it.denzosoft.jreverse.core.pattern.AnalyzerFactory;
import it.denzosoft.jreverse.core.pattern.AnalyzerType;
import it.denzosoft.jreverse.core.port.JarAnalyzerPort;
import it.denzosoft.jreverse.analyzer.impl.DefaultJarAnalyzer;
import it.denzosoft.jreverse.analyzer.impl.SpringBootJarAnalyzer;
import it.denzosoft.jreverse.analyzer.impl.RegularJarAnalyzer;
import it.denzosoft.jreverse.analyzer.detector.SpringBootDetector;
import javassist.ClassPool;

import java.util.Objects;
import java.util.logging.Logger;

/**
 * Concrete Factory implementation using Javassist for creating JAR analyzers.
 * Follows Factory Pattern with automatic analyzer selection based on JAR type detection.
 */
public class JavassistAnalyzerFactory implements AnalyzerFactory {
    
    private static final Logger LOGGER = Logger.getLogger(JavassistAnalyzerFactory.class.getName());
    
    private final ClassPool classPool;
    private final SpringBootDetector springBootDetector;
    
    public JavassistAnalyzerFactory() {
        this.classPool = createConfiguredClassPool();
        this.springBootDetector = new SpringBootDetector();
        LOGGER.info("JavassistAnalyzerFactory initialized with ClassPool");
    }
    
    // Package-visible constructor for testing
    JavassistAnalyzerFactory(SpringBootDetector springBootDetector) {
        this.classPool = createConfiguredClassPool();
        this.springBootDetector = springBootDetector;
        LOGGER.info("JavassistAnalyzerFactory initialized with ClassPool and custom SpringBootDetector");
    }
    
    @Override
    public JarAnalyzerPort createAnalyzer(JarLocation jarLocation) {
        Objects.requireNonNull(jarLocation, "jarLocation cannot be null");
        
        LOGGER.info("Creating analyzer for JAR: " + jarLocation.getPath());
        
        try {
            if (springBootDetector.isSpringBootJar(jarLocation)) {
                LOGGER.info("Detected Spring Boot JAR, using specialized analyzer");
                return createSpringBootAnalyzer();
            } else {
                LOGGER.info("Detected regular JAR, using standard analyzer");
                return createRegularJarAnalyzer();
            }
        } catch (Exception e) {
            LOGGER.warning("Failed to detect JAR type for " + jarLocation.getPath() + 
                         ", falling back to default analyzer: " + e.getMessage());
            return createDefaultAnalyzer();
        }
    }
    
    @Override
    public JarAnalyzerPort createDefaultAnalyzer() {
        LOGGER.fine("Creating default analyzer");
        return new DefaultJarAnalyzer(classPool);
    }
    
    @Override
    public JarAnalyzerPort createSpringBootAnalyzer() {
        LOGGER.fine("Creating Spring Boot analyzer");
        return new SpringBootJarAnalyzer(classPool, springBootDetector);
    }
    
    @Override
    public JarAnalyzerPort createRegularJarAnalyzer() {
        LOGGER.fine("Creating regular JAR analyzer");
        return new RegularJarAnalyzer(classPool);
    }
    
    @Override
    public String getFactoryName() {
        return "Javassist Analyzer Factory";
    }
    
    @Override
    public AnalyzerType[] getSupportedTypes() {
        return new AnalyzerType[] {
            AnalyzerType.DEFAULT,
            AnalyzerType.SPRING_BOOT,
            AnalyzerType.REGULAR_JAR,
            AnalyzerType.LIBRARY_JAR
        };
    }
    
    /**
     * Creates and configures a ClassPool with appropriate settings for JAR analysis.
     * 
     * @return configured ClassPool instance
     */
    private ClassPool createConfiguredClassPool() {
        ClassPool pool = ClassPool.getDefault();
        
        // Optimize for JAR analysis
        pool.childFirstLookup = true;
        
        // Add system classpath for better class resolution
        try {
            pool.appendSystemPath();
        } catch (Exception e) {
            LOGGER.warning("Could not append system path to ClassPool: " + e.getMessage());
        }
        
        LOGGER.fine("ClassPool configured for JAR analysis");
        return pool;
    }
    
    /**
     * Validates that the factory is properly initialized.
     * 
     * @return true if factory is ready for use
     */
    public boolean isInitialized() {
        return classPool != null && springBootDetector != null;
    }
    
    /**
     * Gets the underlying ClassPool for advanced usage.
     * 
     * @return the configured ClassPool
     */
    public ClassPool getClassPool() {
        return classPool;
    }
    
    /**
     * Gets the Spring Boot detector for testing purposes.
     * 
     * @return the Spring Boot detector
     */
    public SpringBootDetector getSpringBootDetector() {
        return springBootDetector;
    }
}