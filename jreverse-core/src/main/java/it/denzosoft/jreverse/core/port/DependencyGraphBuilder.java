package it.denzosoft.jreverse.core.port;

import it.denzosoft.jreverse.core.model.JarContent;
import it.denzosoft.jreverse.core.model.DependencyGraphResult;

/**
 * Port interface for building dependency graphs between packages, classes and modules.
 * This is the core component for Phase 3 architectural analysis.
 * 
 * The dependency graph captures various types of relationships:
 * - Import dependencies between packages
 * - Inheritance relationships between classes  
 * - Composition and aggregation relationships
 * - Interface implementations
 * - Method invocations and field accesses
 * 
 * @author JReverse Development Team
 * @since 1.1.0 (Phase 3)
 */
public interface DependencyGraphBuilder {
    
    /**
     * Builds a comprehensive dependency graph for the given JAR content.
     *
     * The graph includes:
     * - Package-level dependencies (import relationships)
     * - Class-level dependencies (inheritance, composition, aggregation)
     * - Method-level dependencies (invocations, field accesses)
     * - External library dependencies
     *
     * @param jarContent the JAR content to analyze
     * @return complete dependency graph with nodes, edges and metrics
     * @throws IllegalArgumentException if jarContent is null
     */
    DependencyGraphResult buildDependencyGraph(JarContent jarContent);

    /**
     * Builds a dependency graph with specific analysis types.
     *
     * @param jarContent the JAR content to analyze
     * @param analysisTypes specific analysis types to include
     * @return dependency graph with requested analysis types
     * @throws IllegalArgumentException if jarContent is null
     */
    default DependencyGraphResult buildDependencyGraph(JarContent jarContent, java.util.EnumSet<DependencyAnalysisType> analysisTypes) {
        // Default implementation falls back to full analysis
        return buildDependencyGraph(jarContent);
    }
    
    /**
     * Checks if this builder can analyze the given JAR content.
     * 
     * @param jarContent the JAR content to check
     * @return true if analysis is supported, false otherwise
     */
    boolean canAnalyze(JarContent jarContent);
    
    /**
     * Gets the supported analysis types for this builder.
     * 
     * @return array of supported dependency analysis types
     */
    DependencyAnalysisType[] getSupportedAnalysisTypes();
    
    /**
     * Enumeration of dependency analysis types supported by the builder.
     */
    enum DependencyAnalysisType {
        PACKAGE_DEPENDENCIES,
        PACKAGE_LEVEL,  // Alias for package analysis
        CLASS_INHERITANCE,
        CLASS_COMPOSITION,
        METHOD_INVOCATIONS,
        FIELD_ACCESS,
        INTERFACE_IMPLEMENTATIONS,
        EXTERNAL_LIBRARIES,
        CIRCULAR_DEPENDENCIES
    }
}