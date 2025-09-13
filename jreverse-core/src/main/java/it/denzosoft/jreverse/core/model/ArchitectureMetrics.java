package it.denzosoft.jreverse.core.model;

/**
 * Architecture quality metrics for call graph analysis.
 */
public class ArchitectureMetrics {
    
    private final int layerViolations;
    private final int circularDependencies;
    private final double couplingScore;
    private final double cohesionScore;
    private final int architectureQualityScore;
    
    private ArchitectureMetrics(Builder builder) {
        this.layerViolations = builder.layerViolations;
        this.circularDependencies = builder.circularDependencies;
        this.couplingScore = builder.couplingScore;
        this.cohesionScore = builder.cohesionScore;
        this.architectureQualityScore = builder.architectureQualityScore;
    }
    
    // Constructor diretto per compatibilità
    public ArchitectureMetrics(int coupling, int cohesion, int layerViolations, int qualityScore) {
        this.couplingScore = coupling;
        this.cohesionScore = cohesion;
        this.layerViolations = layerViolations;
        this.architectureQualityScore = qualityScore;
        this.circularDependencies = 0;
    }
    
    public int getLayerViolations() { return layerViolations; }
    public int getCircularDependencies() { return circularDependencies; }
    public double getCouplingScore() { return couplingScore; }
    public double getCohesionScore() { return cohesionScore; }
    public int getArchitectureQualityScore() { return architectureQualityScore; }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private int layerViolations;
        private int circularDependencies;
        private double couplingScore;
        private double cohesionScore;
        private int architectureQualityScore;
        
        public Builder layerViolations(int layerViolations) {
            this.layerViolations = layerViolations;
            return this;
        }
        
        public Builder circularDependencies(int circularDependencies) {
            this.circularDependencies = circularDependencies;
            return this;
        }
        
        public Builder couplingScore(double couplingScore) {
            this.couplingScore = couplingScore;
            return this;
        }
        
        public Builder cohesionScore(double cohesionScore) {
            this.cohesionScore = cohesionScore;
            return this;
        }
        
        public Builder architectureQualityScore(int architectureQualityScore) {
            this.architectureQualityScore = architectureQualityScore;
            return this;
        }
        
        public ArchitectureMetrics build() {
            return new ArchitectureMetrics(this);
        }
    }
}