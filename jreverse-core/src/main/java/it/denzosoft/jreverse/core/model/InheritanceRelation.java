package it.denzosoft.jreverse.core.model;

import java.util.Objects;

/**
 * Represents a single inheritance relationship between two classes.
 */
public final class InheritanceRelation {
    
    private final String parent;
    private final String child;
    private final InheritanceType type;
    
    public InheritanceRelation(String parent, String child, InheritanceType type) {
        this.parent = Objects.requireNonNull(parent, "Parent class cannot be null");
        this.child = Objects.requireNonNull(child, "Child class cannot be null");
        this.type = Objects.requireNonNull(type, "Inheritance type cannot be null");
    }
    
    public String getParent() {
        return parent;
    }
    
    public String getChild() {
        return child;
    }
    
    public InheritanceType getType() {
        return type;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        InheritanceRelation that = (InheritanceRelation) obj;
        return Objects.equals(parent, that.parent) &&
               Objects.equals(child, that.child) &&
               type == that.type;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(parent, child, type);
    }
    
    @Override
    public String toString() {
        return child + " " + type.getSymbol() + " " + parent;
    }
}