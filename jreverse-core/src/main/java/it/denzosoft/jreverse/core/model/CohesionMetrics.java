package it.denzosoft.jreverse.core.model;

import java.util.Objects;

/**
 * Metrics measuring package cohesion and internal consistency.
 */
public final class CohesionMetrics {
    
    private final double cohesionScore; // 0.0 - 1.0
    private final int totalClasses;
    private final int cohesiveClasses;
    private final double averageMethodsPerClass;
    private final double averageFieldsPerClass;
    private final double functionalCohesion; // How well methods work together
    private final double dataCohesion; // How well data is grouped
    
    private CohesionMetrics(Builder builder) {
        this.cohesionScore = Math.min(1.0, Math.max(0.0, builder.cohesionScore));
        this.totalClasses = Math.max(0, builder.totalClasses);
        this.cohesiveClasses = Math.max(0, builder.cohesiveClasses);
        this.averageMethodsPerClass = Math.max(0.0, builder.averageMethodsPerClass);
        this.averageFieldsPerClass = Math.max(0.0, builder.averageFieldsPerClass);
        this.functionalCohesion = Math.min(1.0, Math.max(0.0, builder.functionalCohesion));
        this.dataCohesion = Math.min(1.0, Math.max(0.0, builder.dataCohesion));
    }
    
    public double getCohesionScore() {
        return cohesionScore;
    }
    
    public int getTotalClasses() {
        return totalClasses;
    }
    
    public int getCohesiveClasses() {
        return cohesiveClasses;
    }
    
    public double getAverageMethodsPerClass() {
        return averageMethodsPerClass;
    }
    
    public double getAverageFieldsPerClass() {
        return averageFieldsPerClass;
    }
    
    public double getFunctionalCohesion() {
        return functionalCohesion;
    }
    
    public double getDataCohesion() {
        return dataCohesion;
    }
    
    public double getCohesionRatio() {
        return totalClasses > 0 ? (double) cohesiveClasses / totalClasses : 0.0;
    }
    
    public boolean isHighCohesion() {
        return cohesionScore >= 0.7;
    }
    
    public boolean isMediumCohesion() {
        return cohesionScore >= 0.4 && cohesionScore < 0.7;
    }
    
    public boolean isLowCohesion() {
        return cohesionScore < 0.4;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CohesionMetrics that = (CohesionMetrics) obj;
        return Double.compare(that.cohesionScore, cohesionScore) == 0 &&
               totalClasses == that.totalClasses &&
               cohesiveClasses == that.cohesiveClasses &&
               Double.compare(that.averageMethodsPerClass, averageMethodsPerClass) == 0 &&
               Double.compare(that.averageFieldsPerClass, averageFieldsPerClass) == 0 &&
               Double.compare(that.functionalCohesion, functionalCohesion) == 0 &&
               Double.compare(that.dataCohesion, dataCohesion) == 0;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(cohesionScore, totalClasses, cohesiveClasses, 
                          averageMethodsPerClass, averageFieldsPerClass, 
                          functionalCohesion, dataCohesion);
    }
    
    @Override
    public String toString() {
        return "CohesionMetrics{" +
                "cohesionScore=" + String.format("%.2f", cohesionScore) +
                ", totalClasses=" + totalClasses +
                ", cohesionRatio=" + String.format("%.2f", getCohesionRatio()) +
                '}';
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private double cohesionScore = 0.0;
        private int totalClasses = 0;
        private int cohesiveClasses = 0;
        private double averageMethodsPerClass = 0.0;
        private double averageFieldsPerClass = 0.0;
        private double functionalCohesion = 0.0;
        private double dataCohesion = 0.0;
        
        public Builder cohesionScore(double cohesionScore) {
            this.cohesionScore = cohesionScore;
            return this;
        }
        
        public Builder totalClasses(int totalClasses) {
            this.totalClasses = totalClasses;
            return this;
        }
        
        public Builder cohesiveClasses(int cohesiveClasses) {
            this.cohesiveClasses = cohesiveClasses;
            return this;
        }
        
        public Builder averageMethodsPerClass(double averageMethodsPerClass) {
            this.averageMethodsPerClass = averageMethodsPerClass;
            return this;
        }
        
        public Builder averageFieldsPerClass(double averageFieldsPerClass) {
            this.averageFieldsPerClass = averageFieldsPerClass;
            return this;
        }
        
        public Builder functionalCohesion(double functionalCohesion) {
            this.functionalCohesion = functionalCohesion;
            return this;
        }
        
        public Builder dataCohesion(double dataCohesion) {
            this.dataCohesion = dataCohesion;
            return this;
        }
        
        public CohesionMetrics build() {
            return new CohesionMetrics(this);
        }
    }
}