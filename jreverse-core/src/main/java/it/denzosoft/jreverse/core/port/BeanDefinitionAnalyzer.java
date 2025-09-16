package it.denzosoft.jreverse.core.port;

import it.denzosoft.jreverse.core.model.BeanDefinitionAnalysisResult;
import it.denzosoft.jreverse.core.model.JarContent;

/**
 * Port interface for granular analysis of Spring Bean definitions, particularly @Bean factory methods.
 * This analyzer provides detailed analysis of bean lifecycle, scoping, dependencies, and creation patterns,
 * extending beyond basic bean discovery to provide architectural insights.
 *
 * Key capabilities:
 * - Granular @Bean factory method analysis
 * - Lifecycle hook detection (@PostConstruct, @PreDestroy, @Bean attributes)
 * - Advanced bean scoping analysis including custom scopes
 * - Bean relationship and dependency analysis
 * - Conditional bean creation analysis
 */
public interface BeanDefinitionAnalyzer {

    /**
     * Performs detailed analysis of bean definitions in the provided JAR content.
     * This includes deep analysis of @Bean methods, their parameters, return types,
     * lifecycle hooks, scoping, and relationships.
     *
     * @param jarContent The JAR content to analyze
     * @return The analysis result containing detailed bean definition information
     */
    BeanDefinitionAnalysisResult analyzeBeanDefinitions(JarContent jarContent);

    /**
     * Checks if this analyzer can analyze the provided JAR content.
     *
     * @param jarContent The JAR content to check
     * @return true if this analyzer can analyze the content, false otherwise
     */
    boolean canAnalyze(JarContent jarContent);
}