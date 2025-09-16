package it.denzosoft.jreverse.core.model;

import java.util.Objects;

/**
 * Represents a relationship between two classes.
 * Placeholder for existing test compatibility.
 */
public final class ClassRelationship {
    
    public enum RelationshipType {
        INHERITANCE,
        IMPLEMENTATION,
        COMPOSITION,
        AGGREGATION,
        ASSOCIATION,
        DEPENDENCY,
        USES,
        INNER_CLASS
    }
    
    public enum RelationshipStrength {
        STRONG,
        MEDIUM,
        WEAK
    }
    
    private final String sourceClass;
    private final String targetClass;
    private final RelationshipType relationshipType;
    private final String description;
    private final RelationshipStrength strength;
    
    public ClassRelationship(String sourceClass, String targetClass, RelationshipType relationshipType, String description) {
        this(sourceClass, targetClass, relationshipType, description, determineStrength(relationshipType));
    }
    
    public ClassRelationship(String sourceClass, String targetClass, RelationshipType relationshipType, String description, RelationshipStrength strength) {
        this.sourceClass = Objects.requireNonNull(sourceClass, "Source class cannot be null");
        this.targetClass = Objects.requireNonNull(targetClass, "Target class cannot be null");
        this.relationshipType = Objects.requireNonNull(relationshipType, "Relationship type cannot be null");
        this.description = description;
        this.strength = strength != null ? strength : determineStrength(relationshipType);
    }
    
    private static RelationshipStrength determineStrength(RelationshipType type) {
        switch (type) {
            case INHERITANCE:
            case COMPOSITION:
                return RelationshipStrength.STRONG;
            case IMPLEMENTATION:
            case AGGREGATION:
                return RelationshipStrength.MEDIUM;
            case ASSOCIATION:
            case DEPENDENCY:
            case USES:
            default:
                return RelationshipStrength.WEAK;
        }
    }
    
    public String getSourceClass() {
        return sourceClass;
    }
    
    public String getTargetClass() {
        return targetClass;
    }
    
    public RelationshipType getRelationshipType() {
        return relationshipType;
    }
    
    public RelationshipType getType() {
        return relationshipType;
    }
    
    public RelationshipStrength getStrength() {
        return strength;
    }
    
    public boolean isInheritance() {
        return relationshipType == RelationshipType.INHERITANCE;
    }
    
    public boolean isComposition() {
        return relationshipType == RelationshipType.COMPOSITION;
    }
    
    public boolean isAggregation() {
        return relationshipType == RelationshipType.AGGREGATION;
    }
    
    public boolean isStrongRelationship() {
        return strength == RelationshipStrength.STRONG;
    }
    
    public String getDescription() {
        return description;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ClassRelationship that = (ClassRelationship) obj;
        return Objects.equals(sourceClass, that.sourceClass) &&
               Objects.equals(targetClass, that.targetClass) &&
               relationshipType == that.relationshipType;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(sourceClass, targetClass, relationshipType);
    }
    
    // Factory methods for convenience
    public static ClassRelationship inheritance(String sourceClass, String targetClass) {
        return new ClassRelationship(sourceClass, targetClass, RelationshipType.INHERITANCE, 
                                    sourceClass + " extends " + targetClass);
    }
    
    public static ClassRelationship implementation(String sourceClass, String targetClass) {
        return new ClassRelationship(sourceClass, targetClass, RelationshipType.IMPLEMENTATION, 
                                    sourceClass + " implements " + targetClass);
    }
    
    public static ClassRelationship composition(String sourceClass, String targetClass) {
        return new ClassRelationship(sourceClass, targetClass, RelationshipType.COMPOSITION, 
                                    sourceClass + " composes " + targetClass);
    }
    
    public static ClassRelationship aggregation(String sourceClass, String targetClass) {
        return new ClassRelationship(sourceClass, targetClass, RelationshipType.AGGREGATION, 
                                    sourceClass + " aggregates " + targetClass);
    }
    
    public static ClassRelationship association(String sourceClass, String targetClass) {
        return new ClassRelationship(sourceClass, targetClass, RelationshipType.ASSOCIATION, 
                                    sourceClass + " associates with " + targetClass);
    }
    
    public static ClassRelationship dependency(String sourceClass, String targetClass) {
        return new ClassRelationship(sourceClass, targetClass, RelationshipType.DEPENDENCY, 
                                    sourceClass + " depends on " + targetClass);
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String sourceClass;
        private String targetClass;
        private RelationshipType type;
        private RelationshipStrength strength;
        private String description;
        
        public Builder sourceClass(String sourceClass) {
            this.sourceClass = sourceClass;
            return this;
        }
        
        public Builder targetClass(String targetClass) {
            this.targetClass = targetClass;
            return this;
        }
        
        public Builder type(RelationshipType type) {
            this.type = type;
            return this;
        }
        
        public Builder strength(RelationshipStrength strength) {
            this.strength = strength;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public ClassRelationship build() {
            return new ClassRelationship(sourceClass, targetClass, type, description, strength);
        }
    }
    
    @Override
    public String toString() {
        return sourceClass + " " + relationshipType + " " + targetClass;
    }
}