package it.denzosoft.jreverse.core.model;

import java.util.List;
import java.util.Objects;
import java.util.Collections;

/**
 * Represents a circular dependency detected in the dependency graph.
 * Contains the cycle path and severity information.
 * 
 * @author JReverse Development Team
 * @since 1.1.0 (Phase 3)
 */
public class CircularDependency {
    
    private final List<DependencyNode> cyclePath;
    private final CircularDependencySeverity severity;
    private final String description;
    private final List<String> suggestions;
    
    // Private constructor for builder
    private CircularDependency(Builder builder) {
        this.cyclePath = builder.cyclePath != null ? 
            List.copyOf(builder.cyclePath) : Collections.emptyList();
        this.severity = Objects.requireNonNull(builder.severity, "Severity cannot be null");
        this.description = builder.description != null ? builder.description : 
            generateDefaultDescription(this.cyclePath);
        this.suggestions = builder.suggestions != null ? 
            List.copyOf(builder.suggestions) : Collections.emptyList();
    }
    
    // Getters
    public List<DependencyNode> getCyclePath() { return cyclePath; }
    public CircularDependencySeverity getSeverity() { return severity; }
    public String getDescription() { return description; }
    public List<String> getSuggestions() { return suggestions; }

    // Compatibility method for existing code
    public List<String> getNodes() {
        return cyclePath.stream()
                .map(DependencyNode::getDisplayName)
                .collect(java.util.stream.Collectors.toList());
    }
    
    // Utility methods
    public int getCycleLength() {
        return cyclePath.size();
    }
    
    public boolean isEmpty() {
        return cyclePath.isEmpty();
    }
    
    public boolean involvesPackages() {
        return cyclePath.stream()
            .anyMatch(node -> node.getType() == DependencyNodeType.PACKAGE);
    }
    
    public boolean involvesClasses() {
        return cyclePath.stream()
            .anyMatch(node -> node.getType() == DependencyNodeType.CLASS);
    }
    
    public boolean isSpringRelated() {
        return cyclePath.stream()
            .anyMatch(node -> node.getType() == DependencyNodeType.SPRING_COMPONENT ||
                             node.hasMetadata("spring"));
    }
    
    // Factory methods
    public static Builder builder() {
        return new Builder();
    }
    
    public static CircularDependency packageCycle(List<DependencyNode> packagePath) {
        return builder()
            .cyclePath(packagePath)
            .severity(CircularDependencySeverity.HIGH)
            .description("Package-level circular dependency detected")
            .suggestions(List.of(
                "Restructure packages to eliminate circular references",
                "Extract common functionality to a shared package",
                "Use dependency inversion principle"
            ))
            .build();
    }
    
    public static CircularDependency classCycle(List<DependencyNode> classPath) {
        CircularDependencySeverity severity = classPath.size() > 5 ? 
            CircularDependencySeverity.HIGH : CircularDependencySeverity.MEDIUM;
            
        return builder()
            .cyclePath(classPath)
            .severity(severity)
            .description("Class-level circular dependency detected")
            .suggestions(List.of(
                "Extract common interface or abstract class",
                "Use dependency injection to break the cycle",
                "Refactor to eliminate bidirectional references"
            ))
            .build();
    }
    
    public static CircularDependency springBeanCycle(List<DependencyNode> beanPath) {
        return builder()
            .cyclePath(beanPath)
            .severity(CircularDependencySeverity.MEDIUM)
            .description("Spring bean circular dependency detected")
            .suggestions(List.of(
                "Use @Lazy annotation to break the cycle",
                "Refactor beans to eliminate circular references",
                "Use setter injection instead of constructor injection"
            ))
            .build();
    }
    
    // Helper method
    private static String generateDefaultDescription(List<DependencyNode> cyclePath) {
        if (cyclePath.isEmpty()) {
            return "Empty circular dependency";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("Circular dependency: ");
        
        for (int i = 0; i < cyclePath.size(); i++) {
            sb.append(cyclePath.get(i).getDisplayName());
            if (i < cyclePath.size() - 1) {
                sb.append(" -> ");
            }
        }
        
        // Close the cycle
        if (!cyclePath.isEmpty()) {
            sb.append(" -> ").append(cyclePath.get(0).getDisplayName());
        }
        
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CircularDependency that = (CircularDependency) obj;
        return Objects.equals(cyclePath, that.cyclePath) &&
               severity == that.severity;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(cyclePath, severity);
    }
    
    @Override
    public String toString() {
        return String.format("CircularDependency{length=%d, severity=%s}", 
            getCycleLength(), severity);
    }
    
    // Severity enumeration
    public enum CircularDependencySeverity {
        LOW("Low impact - can be resolved with minor refactoring"),
        MEDIUM("Medium impact - requires careful refactoring"),
        HIGH("High impact - major architectural issue"),
        CRITICAL("Critical - blocks build or causes runtime failures");
        
        private final String description;
        
        CircularDependencySeverity(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }

        public double getScore() {
            switch (this) {
                case LOW: return 0.25;
                case MEDIUM: return 0.5;
                case HIGH: return 0.75;
                case CRITICAL: return 1.0;
                default: return 0.5;
            }
        }
    }
    
    // Builder class
    public static class Builder {
        private List<DependencyNode> cyclePath;
        private CircularDependencySeverity severity;
        private String description;
        private List<String> suggestions;
        
        private Builder() {}
        
        public Builder cyclePath(List<DependencyNode> cyclePath) {
            this.cyclePath = cyclePath;
            return this;
        }
        
        public Builder severity(CircularDependencySeverity severity) {
            this.severity = severity;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder suggestions(List<String> suggestions) {
            this.suggestions = suggestions;
            return this;
        }
        
        public CircularDependency build() {
            return new CircularDependency(this);
        }
    }
}