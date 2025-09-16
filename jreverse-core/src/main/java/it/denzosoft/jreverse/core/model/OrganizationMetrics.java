package it.denzosoft.jreverse.core.model;

import java.util.Objects;

/**
 * Metrics for package organization analysis.
 */
public final class OrganizationMetrics {
    
    private final int totalPackages;
    private final int leafPackages;
    private final int rootPackages;
    private final int maxDepth;
    private final double averageDepth;
    private final double organizationScore;
    private final double modularityScore;
    private final double separationScore;
    
    private OrganizationMetrics(Builder builder) {
        this.totalPackages = Math.max(0, builder.totalPackages);
        this.leafPackages = Math.max(0, builder.leafPackages);
        this.rootPackages = Math.max(0, builder.rootPackages);
        this.maxDepth = Math.max(0, builder.maxDepth);
        this.averageDepth = Math.max(0.0, builder.averageDepth);
        this.organizationScore = Math.min(1.0, Math.max(0.0, builder.organizationScore));
        this.modularityScore = Math.min(1.0, Math.max(0.0, builder.modularityScore));
        this.separationScore = Math.min(1.0, Math.max(0.0, builder.separationScore));
    }
    
    public int getTotalPackages() {
        return totalPackages;
    }
    
    public int getLeafPackages() {
        return leafPackages;
    }
    
    public int getRootPackages() {
        return rootPackages;
    }
    
    public int getMaxDepth() {
        return maxDepth;
    }
    
    public double getAverageDepth() {
        return averageDepth;
    }
    
    public double getOrganizationScore() {
        return organizationScore;
    }
    
    public double getModularityScore() {
        return modularityScore;
    }
    
    public double getSeparationScore() {
        return separationScore;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        OrganizationMetrics that = (OrganizationMetrics) obj;
        return totalPackages == that.totalPackages &&
               leafPackages == that.leafPackages &&
               rootPackages == that.rootPackages &&
               maxDepth == that.maxDepth &&
               Double.compare(that.averageDepth, averageDepth) == 0 &&
               Double.compare(that.organizationScore, organizationScore) == 0 &&
               Double.compare(that.modularityScore, modularityScore) == 0 &&
               Double.compare(that.separationScore, separationScore) == 0;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(totalPackages, leafPackages, rootPackages, maxDepth,
                          averageDepth, organizationScore, modularityScore, separationScore);
    }
    
    @Override
    public String toString() {
        return "OrganizationMetrics{" +
                "totalPackages=" + totalPackages +
                ", maxDepth=" + maxDepth +
                ", organizationScore=" + String.format("%.2f", organizationScore) +
                '}';
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private int totalPackages = 0;
        private int leafPackages = 0;
        private int rootPackages = 0;
        private int maxDepth = 0;
        private double averageDepth = 0.0;
        private double organizationScore = 0.0;
        private double modularityScore = 0.0;
        private double separationScore = 0.0;
        
        public Builder totalPackages(int totalPackages) {
            this.totalPackages = totalPackages;
            return this;
        }
        
        public Builder leafPackages(int leafPackages) {
            this.leafPackages = leafPackages;
            return this;
        }
        
        public Builder rootPackages(int rootPackages) {
            this.rootPackages = rootPackages;
            return this;
        }
        
        public Builder maxDepth(int maxDepth) {
            this.maxDepth = maxDepth;
            return this;
        }
        
        public Builder averageDepth(double averageDepth) {
            this.averageDepth = averageDepth;
            return this;
        }
        
        public Builder organizationScore(double organizationScore) {
            this.organizationScore = organizationScore;
            return this;
        }
        
        public Builder modularityScore(double modularityScore) {
            this.modularityScore = modularityScore;
            return this;
        }
        
        public Builder separationScore(double separationScore) {
            this.separationScore = separationScore;
            return this;
        }
        
        public OrganizationMetrics build() {
            return new OrganizationMetrics(this);
        }
    }
}