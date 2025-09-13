package it.denzosoft.jreverse.analyzer.beancreation;

import it.denzosoft.jreverse.core.model.JarContent;

/**
 * Analyzer for detecting and analyzing Spring Bean creation patterns including
 * @Bean, @Component, @Service, @Repository, @Controller, @RestController, and @Configuration annotations.
 * 
 * This analyzer identifies all Spring beans in the application, analyzes their creation patterns,
 * dependencies, and provides insights for multiple report types including:
 * - Bean dependency graphs (Architecture category)
 * - Component lifecycle analysis (Entrypoint category)
 * - Service layer analysis (Architecture category)
 * - Configuration analysis (Quality category)
 */
public interface BeanCreationAnalyzer {
    
    /**
     * Analyzes the JAR content to find and analyze Spring bean creation patterns.
     * This includes detecting all Spring stereotypes, @Bean methods, configuration classes,
     * and analyzing their dependencies and relationships.
     *
     * @param jarContent the JAR content to analyze
     * @return the analysis result containing comprehensive information about beans and their creation patterns
     */
    BeanCreationResult analyzeBeanCreation(JarContent jarContent);
    
    /**
     * Checks if this analyzer can analyze the given JAR content.
     * The analyzer can process content that contains Java classes and has potential Spring components.
     *
     * @param jarContent the JAR content to check
     * @return true if the analyzer can process this content
     */
    default boolean canAnalyze(JarContent jarContent) {
        return jarContent != null && jarContent.getClassCount() > 0;
    }
}