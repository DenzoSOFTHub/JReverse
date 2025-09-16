package it.denzosoft.jreverse.core.model;

import java.util.Objects;

/**
 * Metrics for a specific package including complexity, coupling and cohesion measurements.
 */
public final class PackageMetrics {
    
    private final int classCount;
    private final int interfaceCount;
    private final int abstractClassCount;
    private final int publicClassCount;
    private final int totalLinesOfCode;
    private final double complexityScore;
    private final double cohesionScore;
    private final int afferentCoupling; // Ca - packages that depend on this package
    private final int efferentCoupling; // Ce - packages this package depends on
    private final double instability; // I = Ce / (Ca + Ce)
    private final double abstractness; // A = abstract classes / total classes
    private final double distanceFromMainSequence; // D = |A + I - 1|
    private final int cyclomaticComplexity;
    
    private PackageMetrics(Builder builder) {
        this.classCount = Math.max(0, builder.classCount);
        this.interfaceCount = Math.max(0, builder.interfaceCount);
        this.abstractClassCount = Math.max(0, builder.abstractClassCount);
        this.publicClassCount = Math.max(0, builder.publicClassCount);
        this.totalLinesOfCode = Math.max(0, builder.totalLinesOfCode);
        this.complexityScore = Math.max(0.0, builder.complexityScore);
        this.cohesionScore = Math.min(1.0, Math.max(0.0, builder.cohesionScore));
        this.afferentCoupling = Math.max(0, builder.afferentCoupling);
        this.efferentCoupling = Math.max(0, builder.efferentCoupling);
        this.instability = calculateInstability(afferentCoupling, efferentCoupling);
        this.abstractness = calculateAbstractness(abstractClassCount, interfaceCount, classCount);
        this.distanceFromMainSequence = calculateDistanceFromMainSequence(abstractness, instability);
        this.cyclomaticComplexity = Math.max(0, builder.cyclomaticComplexity);
    }
    
    public int getClassCount() {
        return classCount;
    }
    
    public int getInterfaceCount() {
        return interfaceCount;
    }
    
    public int getAbstractClassCount() {
        return abstractClassCount;
    }
    
    public int getPublicClassCount() {
        return publicClassCount;
    }
    
    public int getTotalLinesOfCode() {
        return totalLinesOfCode;
    }
    
    public double getComplexityScore() {
        return complexityScore;
    }
    
    public double getCohesionScore() {
        return cohesionScore;
    }
    
    public int getAfferentCoupling() {
        return afferentCoupling;
    }
    
    public int getEfferentCoupling() {
        return efferentCoupling;
    }
    
    public double getInstability() {
        return instability;
    }
    
    public double getAbstractness() {
        return abstractness;
    }
    
    public double getDistanceFromMainSequence() {
        return distanceFromMainSequence;
    }
    
    public int getCyclomaticComplexity() {
        return cyclomaticComplexity;
    }
    
    public int getTotalTypes() {
        return classCount + interfaceCount;
    }
    
    public double getAverageComplexityPerClass() {
        return classCount > 0 ? complexityScore / classCount : 0.0;
    }
    
    public double getAverageLinesPerClass() {
        return classCount > 0 ? (double) totalLinesOfCode / classCount : 0.0;
    }
    
    public boolean isHighlyCoupled() {
        return instability > 0.7; // High instability indicates high coupling
    }
    
    public boolean isLowCohesion() {
        return cohesionScore < 0.3;
    }
    
    public boolean isInPainZone() {
        return distanceFromMainSequence > 0.5;
    }
    
    public PackageQuality getQualityRating() {
        if (isInPainZone()) {
            return PackageQuality.POOR;
        } else if (isHighlyCoupled() || isLowCohesion()) {
            return PackageQuality.FAIR;
        } else if (cohesionScore > 0.7 && distanceFromMainSequence < 0.2) {
            return PackageQuality.EXCELLENT;
        } else {
            return PackageQuality.GOOD;
        }
    }
    
    private static double calculateInstability(int afferent, int efferent) {
        int total = afferent + efferent;
        return total == 0 ? 0.0 : (double) efferent / total;
    }
    
    private static double calculateAbstractness(int abstractClasses, int interfaces, int totalClasses) {
        if (totalClasses == 0) return 0.0;
        return (double) (abstractClasses + interfaces) / totalClasses;
    }
    
    private static double calculateDistanceFromMainSequence(double abstractness, double instability) {
        return Math.abs(abstractness + instability - 1.0);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PackageMetrics that = (PackageMetrics) obj;
        return classCount == that.classCount &&
               interfaceCount == that.interfaceCount &&
               abstractClassCount == that.abstractClassCount &&
               publicClassCount == that.publicClassCount &&
               totalLinesOfCode == that.totalLinesOfCode &&
               Double.compare(that.complexityScore, complexityScore) == 0 &&
               Double.compare(that.cohesionScore, cohesionScore) == 0 &&
               afferentCoupling == that.afferentCoupling &&
               efferentCoupling == that.efferentCoupling &&
               cyclomaticComplexity == that.cyclomaticComplexity;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(classCount, interfaceCount, abstractClassCount, publicClassCount,
                          totalLinesOfCode, complexityScore, cohesionScore, afferentCoupling,
                          efferentCoupling, cyclomaticComplexity);
    }
    
    @Override
    public String toString() {
        return "PackageMetrics{" +
                "classCount=" + classCount +
                ", instability=" + String.format("%.2f", instability) +
                ", abstractness=" + String.format("%.2f", abstractness) +
                ", cohesionScore=" + String.format("%.2f", cohesionScore) +
                ", quality=" + getQualityRating() +
                '}';
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public enum PackageQuality {
        EXCELLENT, GOOD, FAIR, POOR
    }
    
    public static class Builder {
        private int classCount = 0;
        private int interfaceCount = 0;
        private int abstractClassCount = 0;
        private int publicClassCount = 0;
        private int totalLinesOfCode = 0;
        private double complexityScore = 0.0;
        private double cohesionScore = 0.0;
        private int afferentCoupling = 0;
        private int efferentCoupling = 0;
        private int cyclomaticComplexity = 0;
        
        public Builder classCount(int classCount) {
            this.classCount = classCount;
            return this;
        }
        
        public Builder interfaceCount(int interfaceCount) {
            this.interfaceCount = interfaceCount;
            return this;
        }
        
        public Builder abstractClassCount(int abstractClassCount) {
            this.abstractClassCount = abstractClassCount;
            return this;
        }
        
        public Builder publicClassCount(int publicClassCount) {
            this.publicClassCount = publicClassCount;
            return this;
        }
        
        public Builder totalLinesOfCode(int totalLinesOfCode) {
            this.totalLinesOfCode = totalLinesOfCode;
            return this;
        }
        
        public Builder complexityScore(double complexityScore) {
            this.complexityScore = complexityScore;
            return this;
        }
        
        public Builder cohesionScore(double cohesionScore) {
            this.cohesionScore = cohesionScore;
            return this;
        }
        
        public Builder afferentCoupling(int afferentCoupling) {
            this.afferentCoupling = afferentCoupling;
            return this;
        }
        
        public Builder efferentCoupling(int efferentCoupling) {
            this.efferentCoupling = efferentCoupling;
            return this;
        }
        
        public Builder cyclomaticComplexity(int cyclomaticComplexity) {
            this.cyclomaticComplexity = cyclomaticComplexity;
            return this;
        }
        
        public PackageMetrics build() {
            return new PackageMetrics(this);
        }
    }
}