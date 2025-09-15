package it.denzosoft.jreverse.core.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Result of layered architecture analysis containing layer classification, violations, and compliance metrics.
 */
public class LayeredArchitectureResult {
    
    private final Map<LayerType, Set<ClassInfo>> layerClassification;
    private final List<LayerDependency> layerDependencies;
    private final List<LayerViolation> violations;
    private final LayeredArchitectureMetrics metrics;
    private final LayeredArchitectureCompliance compliance;
    private final List<LayerRecommendation> recommendations;
    
    private LayeredArchitectureResult(Builder builder) {
        this.layerClassification = Map.copyOf(builder.layerClassification);
        this.layerDependencies = List.copyOf(builder.layerDependencies);
        this.violations = List.copyOf(builder.violations);
        this.metrics = builder.metrics;
        this.compliance = builder.compliance;
        this.recommendations = List.copyOf(builder.recommendations);
    }
    
    public Map<LayerType, Set<ClassInfo>> getLayerClassification() {
        return layerClassification;
    }
    
    public List<LayerDependency> getLayerDependencies() {
        return layerDependencies;
    }
    
    public List<LayerViolation> getViolations() {
        return violations;
    }
    
    public LayeredArchitectureMetrics getMetrics() {
        return metrics;
    }
    
    public LayeredArchitectureCompliance getCompliance() {
        return compliance;
    }
    
    public List<LayerRecommendation> getRecommendations() {
        return recommendations;
    }
    
    /**
     * Gets classes in a specific layer.
     */
    public Set<ClassInfo> getClassesInLayer(LayerType layerType) {
        return layerClassification.getOrDefault(layerType, Collections.emptySet());
    }
    
    /**
     * Gets violations by type.
     */
    public List<LayerViolation> getViolationsByType(LayerViolation.Type type) {
        return violations.stream()
            .filter(violation -> violation.getViolationType() == type)
            .toList();
    }
    
    /**
     * Gets violations by severity.
     */
    public List<LayerViolation> getViolationsBySeverity(LayerViolation.Severity severity) {
        return violations.stream()
            .filter(violation -> violation.getSeverity() == severity)
            .toList();
    }
    
    /**
     * Checks if any critical violations were found.
     */
    public boolean hasCriticalViolations() {
        return violations.stream()
            .anyMatch(violation -> violation.getSeverity() == LayerViolation.Severity.CRITICAL);
    }
    
    /**
     * Gets the total number of classes analyzed.
     */
    public int getTotalClassesAnalyzed() {
        return layerClassification.values().stream()
            .mapToInt(Set::size)
            .sum();
    }
    
    /**
     * Gets the layer with most classes.
     */
    public LayerType getLargestLayer() {
        return layerClassification.entrySet().stream()
            .max(Map.Entry.<LayerType, Set<ClassInfo>>comparingByValue((s1, s2) -> Integer.compare(s1.size(), s2.size())))
            .map(Map.Entry::getKey)
            .orElse(null);
    }
    
    /**
     * Checks if the architecture follows layered principles.
     */
    public boolean isLayeredArchitectureCompliant() {
        return compliance.getComplianceScore() >= 0.7; // 70% threshold for compliance
    }
    
    /**
     * Gets dependencies from a specific layer.
     */
    public List<LayerDependency> getDependenciesFromLayer(LayerType fromLayer) {
        return layerDependencies.stream()
            .filter(dependency -> dependency.getSourceLayer() == fromLayer)
            .toList();
    }
    
    /**
     * Gets dependencies to a specific layer.
     */
    public List<LayerDependency> getDependenciesToLayer(LayerType toLayer) {
        return layerDependencies.stream()
            .filter(dependency -> dependency.getTargetLayer() == toLayer)
            .toList();
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private Map<LayerType, Set<ClassInfo>> layerClassification = Collections.emptyMap();
        private List<LayerDependency> layerDependencies = Collections.emptyList();
        private List<LayerViolation> violations = Collections.emptyList();
        private LayeredArchitectureMetrics metrics;
        private LayeredArchitectureCompliance compliance;
        private List<LayerRecommendation> recommendations = Collections.emptyList();
        
        public Builder layerClassification(Map<LayerType, Set<ClassInfo>> layerClassification) {
            this.layerClassification = layerClassification != null ? layerClassification : Collections.emptyMap();
            return this;
        }
        
        public Builder layerDependencies(List<LayerDependency> layerDependencies) {
            this.layerDependencies = layerDependencies != null ? layerDependencies : Collections.emptyList();
            return this;
        }
        
        public Builder violations(List<LayerViolation> violations) {
            this.violations = violations != null ? violations : Collections.emptyList();
            return this;
        }
        
        public Builder metrics(LayeredArchitectureMetrics metrics) {
            this.metrics = metrics;
            return this;
        }
        
        public Builder compliance(LayeredArchitectureCompliance compliance) {
            this.compliance = compliance;
            return this;
        }
        
        public Builder recommendations(List<LayerRecommendation> recommendations) {
            this.recommendations = recommendations != null ? recommendations : Collections.emptyList();
            return this;
        }
        
        public LayeredArchitectureResult build() {
            if (metrics == null) {
                throw new IllegalStateException("Metrics is required");
            }
            if (compliance == null) {
                throw new IllegalStateException("Compliance is required");
            }
            return new LayeredArchitectureResult(this);
        }
    }
    
    @Override
    public String toString() {
        return String.format("LayeredArchitectureResult{layers=%d, violations=%d, compliance=%.1f%%}",
            layerClassification.size(), violations.size(), 
            compliance != null ? compliance.getComplianceScore() * 100.0 : 0.0);
    }
}