package it.denzosoft.jreverse.core.model;

import java.util.Objects;
import java.util.Set;
import java.util.Collections;

/**
 * Represents a node in the dependency graph.
 * Can represent packages, classes, or methods depending on the analysis level.
 * 
 * @author JReverse Development Team
 * @since 1.1.0 (Phase 3)
 */
public class DependencyNode {
    
    private final String identifier;
    private final DependencyNodeType type;
    private final String displayName;
    private final String fullyQualifiedName;
    private final Set<String> metadata;
    private final int inDegree;
    private final int outDegree;
    
    // Private constructor for builder
    private DependencyNode(Builder builder) {
        this.identifier = Objects.requireNonNull(builder.identifier, "Node identifier cannot be null");
        this.type = Objects.requireNonNull(builder.type, "Node type cannot be null");
        this.displayName = builder.displayName != null ? builder.displayName : builder.identifier;
        this.fullyQualifiedName = builder.fullyQualifiedName != null ? 
            builder.fullyQualifiedName : builder.identifier;
        this.metadata = builder.metadata != null ? Set.copyOf(builder.metadata) : Collections.emptySet();
        this.inDegree = Math.max(0, builder.inDegree);
        this.outDegree = Math.max(0, builder.outDegree);
    }
    
    // Getters
    public String getIdentifier() { return identifier; }
    public DependencyNodeType getType() { return type; }
    public String getDisplayName() { return displayName; }
    public String getFullyQualifiedName() { return fullyQualifiedName; }
    public Set<String> getMetadata() { return metadata; }
    public int getInDegree() { return inDegree; }
    public int getOutDegree() { return outDegree; }

    // Compatibility method for existing code
    public String getName() { return displayName; }
    
    // Utility methods
    public int getTotalDegree() {
        return inDegree + outDegree;
    }
    
    public boolean isHighCoupling() {
        return getTotalDegree() > 10; // Configurable threshold
    }
    
    public boolean hasMetadata(String key) {
        return metadata.contains(key);
    }
    
    // Factory methods
    public static Builder builder() {
        return new Builder();
    }
    
    public static DependencyNode packageNode(String packageName) {
        return builder()
            .identifier(packageName)
            .type(DependencyNodeType.PACKAGE)
            .displayName(getSimplePackageName(packageName))
            .fullyQualifiedName(packageName)
            .build();
    }
    
    public static DependencyNode classNode(String className) {
        return builder()
            .identifier(className)
            .type(DependencyNodeType.CLASS)
            .displayName(getSimpleClassName(className))
            .fullyQualifiedName(className)
            .build();
    }
    
    public static DependencyNode methodNode(String methodSignature) {
        return builder()
            .identifier(methodSignature)
            .type(DependencyNodeType.METHOD)
            .displayName(getSimpleMethodName(methodSignature))
            .fullyQualifiedName(methodSignature)
            .build();
    }
    
    // Helper methods for display names
    private static String getSimplePackageName(String packageName) {
        int lastDot = packageName.lastIndexOf('.');
        return lastDot >= 0 ? packageName.substring(lastDot + 1) : packageName;
    }
    
    private static String getSimpleClassName(String className) {
        int lastDot = className.lastIndexOf('.');
        return lastDot >= 0 ? className.substring(lastDot + 1) : className;
    }
    
    private static String getSimpleMethodName(String methodSignature) {
        // Extract method name from signature like "com.example.Class.method(String,int)"
        int lastDot = methodSignature.lastIndexOf('.');
        int openParen = methodSignature.indexOf('(');
        if (lastDot >= 0 && openParen > lastDot) {
            return methodSignature.substring(lastDot + 1, openParen);
        }
        return methodSignature;
    }
    
    // equals and hashCode based on identifier and type
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DependencyNode that = (DependencyNode) obj;
        return Objects.equals(identifier, that.identifier) &&
               type == that.type;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(identifier, type);
    }
    
    @Override
    public String toString() {
        return String.format("DependencyNode{id='%s', type=%s, degree=%d}", 
            identifier, type, getTotalDegree());
    }
    
    // Builder class
    public static class Builder {
        private String identifier;
        private DependencyNodeType type;
        private String displayName;
        private String fullyQualifiedName;
        private Set<String> metadata;
        private int inDegree;
        private int outDegree;
        
        private Builder() {}
        
        public Builder identifier(String identifier) {
            this.identifier = identifier;
            return this;
        }
        
        public Builder type(DependencyNodeType type) {
            this.type = type;
            return this;
        }
        
        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }
        
        public Builder fullyQualifiedName(String fullyQualifiedName) {
            this.fullyQualifiedName = fullyQualifiedName;
            return this;
        }
        
        public Builder metadata(Set<String> metadata) {
            this.metadata = metadata;
            return this;
        }
        
        public Builder inDegree(int inDegree) {
            this.inDegree = inDegree;
            return this;
        }
        
        public Builder outDegree(int outDegree) {
            this.outDegree = outDegree;
            return this;
        }
        
        public DependencyNode build() {
            return new DependencyNode(this);
        }
    }
}