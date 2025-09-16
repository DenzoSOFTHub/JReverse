package it.denzosoft.jreverse.core.model;

import java.util.List;
import java.util.Set;
import java.util.Collections;

/**
 * Result of dependency graph analysis containing the complete graph structure,
 * metrics and detected issues like circular dependencies.
 * 
 * @author JReverse Development Team
 * @since 1.1.0 (Phase 3)
 */
public class DependencyGraphResult {
    
    private final Set<DependencyNode> nodes;
    private final Set<DependencyEdge> edges;
    private final DependencyMetrics metrics;
    private final List<CircularDependency> circularDependencies;
    private final AnalysisMetadata metadata;
    private final boolean successful;
    private final String errorMessage;
    
    // Private constructor for builder
    private DependencyGraphResult(Builder builder) {
        this.nodes = builder.nodes != null ? Set.copyOf(builder.nodes) : Collections.emptySet();
        this.edges = builder.edges != null ? Set.copyOf(builder.edges) : Collections.emptySet();
        this.metrics = builder.metrics;
        this.circularDependencies = builder.circularDependencies != null ? 
            List.copyOf(builder.circularDependencies) : Collections.emptyList();
        this.metadata = builder.metadata;
        this.successful = builder.successful;
        this.errorMessage = builder.errorMessage;
    }
    
    // Getters
    public Set<DependencyNode> getNodes() { return nodes; }
    public Set<DependencyEdge> getEdges() { return edges; }
    public DependencyMetrics getMetrics() { return metrics; }
    public List<CircularDependency> getCircularDependencies() { return circularDependencies; }
    public AnalysisMetadata getMetadata() { return metadata; }
    public boolean isSuccessful() { return successful; }
    public String getErrorMessage() { return errorMessage; }
    
    // Utility methods
    public boolean hasCircularDependencies() {
        return !circularDependencies.isEmpty();
    }
    
    public int getTotalNodes() {
        return nodes.size();
    }
    
    public int getTotalEdges() {
        return edges.size();
    }
    
    // Factory methods
    public static Builder builder() {
        return new Builder();
    }
    
    public static DependencyGraphResult successful(Set<DependencyNode> nodes, 
                                                  Set<DependencyEdge> edges,
                                                  DependencyMetrics metrics,
                                                  List<CircularDependency> circularDeps) {
        return builder()
            .nodes(nodes)
            .edges(edges)
            .metrics(metrics)
            .circularDependencies(circularDeps)
            .successful(true)
            .metadata(AnalysisMetadata.successful())
            .build();
    }
    
    public static DependencyGraphResult failed(String errorMessage) {
        return builder()
            .successful(false)
            .errorMessage(errorMessage)
            .metadata(AnalysisMetadata.error(errorMessage))
            .build();
    }
    
    // Builder class
    public static class Builder {
        private Set<DependencyNode> nodes;
        private Set<DependencyEdge> edges;
        private DependencyMetrics metrics;
        private List<CircularDependency> circularDependencies;
        private AnalysisMetadata metadata;
        private boolean successful;
        private String errorMessage;
        
        private Builder() {}
        
        public Builder nodes(Set<DependencyNode> nodes) {
            this.nodes = nodes;
            return this;
        }
        
        public Builder edges(Set<DependencyEdge> edges) {
            this.edges = edges;
            return this;
        }
        
        public Builder metrics(DependencyMetrics metrics) {
            this.metrics = metrics;
            return this;
        }
        
        public Builder circularDependencies(List<CircularDependency> circularDependencies) {
            this.circularDependencies = circularDependencies;
            return this;
        }
        
        public Builder metadata(AnalysisMetadata metadata) {
            this.metadata = metadata;
            return this;
        }
        
        public Builder successful(boolean successful) {
            this.successful = successful;
            return this;
        }
        
        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }
        
        public DependencyGraphResult build() {
            return new DependencyGraphResult(this);
        }
    }
}