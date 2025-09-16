package it.denzosoft.jreverse.core.model;

import java.util.Objects;
import java.util.Set;
import java.util.Collections;

/**
 * Represents an edge (relationship) in the dependency graph between two nodes.
 * 
 * @author JReverse Development Team
 * @since 1.1.0 (Phase 3)
 */
public class DependencyEdge {
    
    private final DependencyNode source;
    private final DependencyNode target;
    private final DependencyEdgeType type;
    private final String description;
    private final int weight;
    private final Set<String> metadata;
    
    // Private constructor for builder
    private DependencyEdge(Builder builder) {
        this.source = Objects.requireNonNull(builder.source, "Source node cannot be null");
        this.target = Objects.requireNonNull(builder.target, "Target node cannot be null");
        this.type = Objects.requireNonNull(builder.type, "Edge type cannot be null");
        this.description = builder.description != null ? builder.description : 
            generateDefaultDescription(builder.source, builder.target, builder.type);
        this.weight = Math.max(1, builder.weight);
        this.metadata = builder.metadata != null ? Set.copyOf(builder.metadata) : Collections.emptySet();
    }
    
    // Getters
    public DependencyNode getSource() { return source; }
    public DependencyNode getTarget() { return target; }
    public DependencyEdgeType getType() { return type; }
    public String getDescription() { return description; }
    public int getWeight() { return weight; }
    public Set<String> getMetadata() { return metadata; }
    
    // Utility methods
    public boolean isStrongDependency() {
        return type == DependencyEdgeType.INHERITANCE || 
               type == DependencyEdgeType.COMPOSITION ||
               weight > 5;
    }
    
    public boolean isWeakDependency() {
        return type == DependencyEdgeType.USES || 
               type == DependencyEdgeType.IMPORTS ||
               weight == 1;
    }
    
    public boolean connectsSamePackage() {
        if (source.getType() == DependencyNodeType.CLASS && 
            target.getType() == DependencyNodeType.CLASS) {
            String sourcePackage = extractPackageName(source.getFullyQualifiedName());
            String targetPackage = extractPackageName(target.getFullyQualifiedName());
            return Objects.equals(sourcePackage, targetPackage);
        }
        return false;
    }
    
    public boolean hasMetadata(String key) {
        return metadata.contains(key);
    }
    
    // Factory methods
    public static Builder builder() {
        return new Builder();
    }
    
    public static DependencyEdge inheritance(DependencyNode child, DependencyNode parent) {
        return builder()
            .source(child)
            .target(parent)
            .type(DependencyEdgeType.INHERITANCE)
            .weight(10) // High weight for inheritance
            .build();
    }
    
    public static DependencyEdge composition(DependencyNode owner, DependencyNode component) {
        return builder()
            .source(owner)
            .target(component)
            .type(DependencyEdgeType.COMPOSITION)
            .weight(8) // High weight for composition
            .build();
    }
    
    public static DependencyEdge uses(DependencyNode user, DependencyNode used) {
        return builder()
            .source(user)
            .target(used)
            .type(DependencyEdgeType.USES)
            .weight(3) // Medium weight for usage
            .build();
    }
    
    public static DependencyEdge imports(DependencyNode importer, DependencyNode imported) {
        return builder()
            .source(importer)
            .target(imported)
            .type(DependencyEdgeType.IMPORTS)
            .weight(1) // Low weight for imports
            .build();
    }
    
    // Helper methods
    private static String generateDefaultDescription(DependencyNode source, 
                                                   DependencyNode target, 
                                                   DependencyEdgeType type) {
        return String.format("%s %s %s", 
            source.getDisplayName(), 
            type.getDescription().toLowerCase(), 
            target.getDisplayName());
    }
    
    private String extractPackageName(String fullyQualifiedName) {
        int lastDot = fullyQualifiedName.lastIndexOf('.');
        return lastDot >= 0 ? fullyQualifiedName.substring(0, lastDot) : "";
    }
    
    // equals and hashCode based on source, target, and type
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DependencyEdge that = (DependencyEdge) obj;
        return Objects.equals(source, that.source) &&
               Objects.equals(target, that.target) &&
               type == that.type;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(source, target, type);
    }
    
    @Override
    public String toString() {
        return String.format("DependencyEdge{%s -[%s]-> %s, weight=%d}", 
            source.getIdentifier(), type, target.getIdentifier(), weight);
    }
    
    // Builder class
    public static class Builder {
        private DependencyNode source;
        private DependencyNode target;
        private DependencyEdgeType type;
        private String description;
        private int weight = 1;
        private Set<String> metadata;
        
        private Builder() {}
        
        public Builder source(DependencyNode source) {
            this.source = source;
            return this;
        }
        
        public Builder target(DependencyNode target) {
            this.target = target;
            return this;
        }
        
        public Builder type(DependencyEdgeType type) {
            this.type = type;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder weight(int weight) {
            this.weight = weight;
            return this;
        }
        
        public Builder metadata(Set<String> metadata) {
            this.metadata = metadata;
            return this;
        }
        
        public DependencyEdge build() {
            return new DependencyEdge(this);
        }
    }
}