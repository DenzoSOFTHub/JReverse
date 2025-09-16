package it.denzosoft.jreverse.core.model;

import java.util.Objects;

/**
 * Represents a composition or aggregation relationship between classes.
 */
public final class CompositionRelationship {
    
    private final String source;
    private final String target;
    private final CompositionType compositionType;
    private final String multiplicity;
    
    public CompositionRelationship(String source, String target, CompositionType compositionType, String multiplicity) {
        this.source = Objects.requireNonNull(source, "Source class cannot be null");
        this.target = Objects.requireNonNull(target, "Target class cannot be null");
        this.compositionType = Objects.requireNonNull(compositionType, "Composition type cannot be null");
        this.multiplicity = multiplicity != null ? multiplicity : "1";
    }
    
    public String getSource() {
        return source;
    }
    
    public String getTarget() {
        return target;
    }
    
    public CompositionType getCompositionType() {
        return compositionType;
    }
    
    public String getMultiplicity() {
        return multiplicity;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CompositionRelationship that = (CompositionRelationship) obj;
        return Objects.equals(source, that.source) &&
               Objects.equals(target, that.target) &&
               compositionType == that.compositionType;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(source, target, compositionType);
    }
    
    @Override
    public String toString() {
        return source + " " + compositionType.getSymbol() + " \"" + multiplicity + "\" " + target;
    }
}