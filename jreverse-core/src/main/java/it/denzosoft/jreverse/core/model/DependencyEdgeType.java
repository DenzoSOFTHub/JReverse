package it.denzosoft.jreverse.core.model;

/**
 * Enumeration of dependency edge types representing different kinds of relationships
 * between nodes in the dependency graph.
 * 
 * @author JReverse Development Team
 * @since 1.1.0 (Phase 3)
 */
public enum DependencyEdgeType {
    
    /**
     * Represents an inheritance relationship (extends/implements).
     * Source inherits from/implements target.
     * High coupling strength.
     */
    INHERITANCE("Inherits", 10),
    
    /**
     * Represents a composition relationship (strong "has-a").
     * Source contains target as a component.
     * High coupling strength.
     */
    COMPOSITION("Composes", 8),
    
    /**
     * Represents an aggregation relationship (weak "has-a").
     * Source aggregates target.
     * Medium coupling strength.
     */
    AGGREGATION("Aggregates", 6),
    
    /**
     * Represents a usage relationship (method calls, field access).
     * Source uses target functionality.
     * Medium coupling strength.
     */
    USES("Uses", 4),
    
    /**
     * Represents an import dependency.
     * Source imports target package/class.
     * Low coupling strength.
     */
    IMPORTS("Imports", 2),
    
    /**
     * Represents interface implementation.
     * Source implements target interface.
     * High coupling strength.
     */
    IMPLEMENTS("Implements", 9),
    
    /**
     * Represents method invocation dependency.
     * Source method calls target method.
     * Medium coupling strength.
     */
    INVOKES("Invokes", 5),
    
    /**
     * Represents field access dependency.
     * Source accesses target field.
     * Medium coupling strength.
     */
    ACCESSES("Accesses", 3),
    
    /**
     * Represents annotation usage.
     * Source is annotated with target annotation.
     * Low coupling strength.
     */
    ANNOTATED_WITH("AnnotatedWith", 1),
    
    /**
     * Represents Spring dependency injection.
     * Source is injected with target bean.
     * Medium coupling strength.
     */
    INJECTED_WITH("InjectedWith", 7);
    
    private final String description;
    private final int defaultWeight;
    
    DependencyEdgeType(String description, int defaultWeight) {
        this.description = description;
        this.defaultWeight = defaultWeight;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getDefaultWeight() {
        return defaultWeight;
    }
    
    /**
     * Checks if this edge type represents a strong coupling.
     * 
     * @return true for INHERITANCE, COMPOSITION, IMPLEMENTS, INJECTED_WITH
     */
    public boolean isStrongCoupling() {
        return defaultWeight >= 7;
    }
    
    /**
     * Checks if this edge type represents a structural relationship.
     * 
     * @return true for INHERITANCE, COMPOSITION, AGGREGATION, IMPLEMENTS
     */
    public boolean isStructural() {
        return this == INHERITANCE || 
               this == COMPOSITION || 
               this == AGGREGATION || 
               this == IMPLEMENTS;
    }
    
    /**
     * Checks if this edge type represents a behavioral relationship.
     * 
     * @return true for USES, INVOKES, ACCESSES
     */
    public boolean isBehavioral() {
        return this == USES || 
               this == INVOKES || 
               this == ACCESSES;
    }
    
    /**
     * Gets the inverse edge type if applicable.
     * 
     * @return inverse edge type, or null if no inverse exists
     */
    public DependencyEdgeType getInverse() {
        // Most relationships are directional and don't have clear inverses
        // Could be extended in future if needed
        return null;
    }
}