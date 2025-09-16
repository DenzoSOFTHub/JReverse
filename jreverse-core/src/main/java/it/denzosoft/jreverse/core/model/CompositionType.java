package it.denzosoft.jreverse.core.model;

/**
 * Types of composition relationships in UML.
 */
public enum CompositionType {
    
    /**
     * Strong composition - whole controls the lifetime of parts.
     */
    COMPOSITION("composition", "*--"),
    
    /**
     * Weak aggregation - parts can exist independently.
     */
    AGGREGATION("aggregation", "o--"),
    
    /**
     * Simple association - classes are related but independent.
     */
    ASSOCIATION("association", "-->");
    
    private final String name;
    private final String symbol;
    
    CompositionType(String name, String symbol) {
        this.name = name;
        this.symbol = symbol;
    }
    
    public String getName() {
        return name;
    }
    
    public String getSymbol() {
        return symbol;
    }
    
    @Override
    public String toString() {
        return name;
    }
}