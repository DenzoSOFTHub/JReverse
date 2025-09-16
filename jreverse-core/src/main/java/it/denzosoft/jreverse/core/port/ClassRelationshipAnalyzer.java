package it.denzosoft.jreverse.core.port;

import it.denzosoft.jreverse.core.model.JarContent;
import it.denzosoft.jreverse.core.model.ClassRelationshipResult;

/**
 * Port interface for analyzing relationships between classes including
 * inheritance, composition, aggregation, and associations.
 * 
 * This analyzer is fundamental for understanding the design and structure
 * of the application, identifying design patterns, and generating UML diagrams.
 * 
 * @author JReverse Development Team
 * @since 1.1.0 (Phase 3)
 */
public interface ClassRelationshipAnalyzer {
    
    /**
     * Analyzes all class relationships in the given JAR content.
     * 
     * The analysis includes:
     * - Inheritance hierarchies (extends relationships)
     * - Interface implementations
     * - Composition relationships (strong "has-a")
     * - Aggregation relationships (weak "has-a")
     * - Association relationships (uses)
     * - Dependency relationships
     * - Inner class relationships
     * 
     * @param jarContent the JAR content to analyze
     * @return comprehensive analysis of class relationships
     * @throws IllegalArgumentException if jarContent is null
     */
    ClassRelationshipResult analyzeRelationships(JarContent jarContent);
    
    /**
     * Checks if this analyzer can analyze the given JAR content.
     * 
     * @param jarContent the JAR content to check
     * @return true if analysis is supported, false otherwise
     */
    boolean canAnalyze(JarContent jarContent);
    
    /**
     * Gets the types of relationships this analyzer can detect.
     * 
     * @return array of supported relationship types
     */
    RelationshipType[] getSupportedRelationshipTypes();
    
    /**
     * Enumeration of class relationship types.
     */
    enum RelationshipType {
        /**
         * Inheritance relationship (class extends another class).
         */
        INHERITANCE,
        
        /**
         * Interface implementation relationship.
         */
        IMPLEMENTATION,
        
        /**
         * Composition relationship (strong ownership, lifecycle dependency).
         */
        COMPOSITION,
        
        /**
         * Aggregation relationship (weak ownership, no lifecycle dependency).
         */
        AGGREGATION,
        
        /**
         * Association relationship (uses relationship).
         */
        ASSOCIATION,
        
        /**
         * Dependency relationship (depends on but doesn't own).
         */
        DEPENDENCY,
        
        /**
         * Inner class relationship (nested/inner classes).
         */
        INNER_CLASS,
        
        /**
         * Generalization relationship (abstraction).
         */
        GENERALIZATION,
        
        /**
         * Realization relationship (interface realization).
         */
        REALIZATION
    }
}