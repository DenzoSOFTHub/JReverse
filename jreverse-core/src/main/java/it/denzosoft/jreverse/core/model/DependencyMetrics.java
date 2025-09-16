package it.denzosoft.jreverse.core.model;

import java.util.Map;
import java.util.Collections;

/**
 * Metrics calculated from dependency graph analysis.
 * Provides insights into architectural quality and coupling/cohesion.
 * 
 * @author JReverse Development Team
 * @since 1.1.0 (Phase 3)
 */
public class DependencyMetrics {
    
    private final int totalNodes;
    private final int totalEdges;
    private final int packageNodes;
    private final int classNodes;
    private final int methodNodes;
    private final int circularDependencies;
    private final double averageDegree;
    private final double density;
    private final int maxDepth;
    private final Map<String, Double> packageCohesion;
    private final Map<String, Double> packageCoupling;
    private final double instability;
    private final double abstractness;
    private final double distance;
    
    // Private constructor for builder
    private DependencyMetrics(Builder builder) {
        this.totalNodes = Math.max(0, builder.totalNodes);
        this.totalEdges = Math.max(0, builder.totalEdges);
        this.packageNodes = Math.max(0, builder.packageNodes);
        this.classNodes = Math.max(0, builder.classNodes);
        this.methodNodes = Math.max(0, builder.methodNodes);
        this.circularDependencies = Math.max(0, builder.circularDependencies);
        this.averageDegree = Math.max(0.0, builder.averageDegree);
        this.density = Math.max(0.0, Math.min(1.0, builder.density));
        this.maxDepth = Math.max(0, builder.maxDepth);
        this.packageCohesion = builder.packageCohesion != null ? 
            Map.copyOf(builder.packageCohesion) : Collections.emptyMap();
        this.packageCoupling = builder.packageCoupling != null ? 
            Map.copyOf(builder.packageCoupling) : Collections.emptyMap();
        this.instability = Math.max(0.0, Math.min(1.0, builder.instability));
        this.abstractness = Math.max(0.0, Math.min(1.0, builder.abstractness));
        this.distance = Math.max(0.0, builder.distance);
    }
    
    // Getters
    public int getTotalNodes() { return totalNodes; }
    public int getTotalEdges() { return totalEdges; }
    public int getPackageNodes() { return packageNodes; }
    public int getClassNodes() { return classNodes; }
    public int getMethodNodes() { return methodNodes; }
    public int getCircularDependencies() { return circularDependencies; }
    public double getAverageDegree() { return averageDegree; }
    public double getDensity() { return density; }
    public int getMaxDepth() { return maxDepth; }
    public Map<String, Double> getPackageCohesion() { return packageCohesion; }
    public Map<String, Double> getPackageCoupling() { return packageCoupling; }
    public double getInstability() { return instability; }
    public double getAbstractness() { return abstractness; }
    public double getDistance() { return distance; }
    
    // Derived metrics
    public double getConnectivity() {
        return totalNodes > 0 ? (double) totalEdges / totalNodes : 0.0;
    }
    
    public boolean hasHighCoupling() {
        return averageDegree > 10.0 || density > 0.3;
    }
    
    public boolean hasCircularDependencies() {
        return circularDependencies > 0;
    }
    
    public ArchitecturalHealth getArchitecturalHealth() {
        if (hasCircularDependencies()) {
            return ArchitecturalHealth.POOR;
        } else if (hasHighCoupling()) {
            return ArchitecturalHealth.FAIR;
        } else if (distance < 0.1) {
            return ArchitecturalHealth.EXCELLENT;
        } else {
            return ArchitecturalHealth.GOOD;
        }
    }
    
    public double getPackageCohesion(String packageName) {
        return packageCohesion.getOrDefault(packageName, 0.0);
    }
    
    public double getPackageCoupling(String packageName) {
        return packageCoupling.getOrDefault(packageName, 0.0);
    }
    
    // Factory method
    public static Builder builder() {
        return new Builder();
    }
    
    // Architectural health enumeration
    public enum ArchitecturalHealth {
        EXCELLENT("Excellent architectural quality"),
        GOOD("Good architectural quality"), 
        FAIR("Fair architectural quality with some issues"),
        POOR("Poor architectural quality requiring refactoring");
        
        private final String description;
        
        ArchitecturalHealth(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    @Override
    public String toString() {
        return String.format(
            "DependencyMetrics{nodes=%d, edges=%d, avgDegree=%.2f, density=%.2f, health=%s}", 
            totalNodes, totalEdges, averageDegree, density, getArchitecturalHealth());
    }
    
    // Builder class
    public static class Builder {
        private int totalNodes;
        private int totalEdges;
        private int packageNodes;
        private int classNodes;
        private int methodNodes;
        private int circularDependencies;
        private double averageDegree;
        private double density;
        private int maxDepth;
        private Map<String, Double> packageCohesion;
        private Map<String, Double> packageCoupling;
        private double instability;
        private double abstractness;
        private double distance;
        
        private Builder() {}
        
        public Builder totalNodes(int totalNodes) {
            this.totalNodes = totalNodes;
            return this;
        }
        
        public Builder totalEdges(int totalEdges) {
            this.totalEdges = totalEdges;
            return this;
        }
        
        public Builder packageNodes(int packageNodes) {
            this.packageNodes = packageNodes;
            return this;
        }
        
        public Builder classNodes(int classNodes) {
            this.classNodes = classNodes;
            return this;
        }
        
        public Builder methodNodes(int methodNodes) {
            this.methodNodes = methodNodes;
            return this;
        }
        
        public Builder circularDependencies(int circularDependencies) {
            this.circularDependencies = circularDependencies;
            return this;
        }
        
        public Builder averageDegree(double averageDegree) {
            this.averageDegree = averageDegree;
            return this;
        }
        
        public Builder density(double density) {
            this.density = density;
            return this;
        }
        
        public Builder maxDepth(int maxDepth) {
            this.maxDepth = maxDepth;
            return this;
        }
        
        public Builder packageCohesion(Map<String, Double> packageCohesion) {
            this.packageCohesion = packageCohesion;
            return this;
        }
        
        public Builder packageCoupling(Map<String, Double> packageCoupling) {
            this.packageCoupling = packageCoupling;
            return this;
        }
        
        public Builder instability(double instability) {
            this.instability = instability;
            return this;
        }
        
        public Builder abstractness(double abstractness) {
            this.abstractness = abstractness;
            return this;
        }
        
        public Builder distance(double distance) {
            this.distance = distance;
            return this;
        }
        
        public DependencyMetrics build() {
            return new DependencyMetrics(this);
        }
    }
}