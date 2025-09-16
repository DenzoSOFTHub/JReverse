package it.denzosoft.jreverse.core.model;

import java.util.*;

/**
 * Summary of naming convention analysis results.
 */
public final class NamingConventionSummary {
    
    private final int totalPackages;
    private final int packagesWithViolations;
    private final int totalViolations;
    private final int highSeverityViolations;
    private final int mediumSeverityViolations;
    private final int lowSeverityViolations;
    private final double conformanceScore;
    private final Map<NamingViolationType, Integer> violationCounts;
    
    private NamingConventionSummary(Builder builder) {
        this.totalPackages = Math.max(0, builder.totalPackages);
        this.packagesWithViolations = Math.max(0, builder.packagesWithViolations);
        this.totalViolations = Math.max(0, builder.totalViolations);
        this.highSeverityViolations = Math.max(0, builder.highSeverityViolations);
        this.mediumSeverityViolations = Math.max(0, builder.mediumSeverityViolations);
        this.lowSeverityViolations = Math.max(0, builder.lowSeverityViolations);
        this.conformanceScore = Math.min(1.0, Math.max(0.0, builder.conformanceScore));
        this.violationCounts = Collections.unmodifiableMap(new HashMap<>(builder.violationCounts));
    }
    
    public int getTotalPackages() {
        return totalPackages;
    }
    
    public int getPackagesWithViolations() {
        return packagesWithViolations;
    }
    
    public int getTotalViolations() {
        return totalViolations;
    }
    
    public int getHighSeverityViolations() {
        return highSeverityViolations;
    }
    
    public int getMediumSeverityViolations() {
        return mediumSeverityViolations;
    }
    
    public int getLowSeverityViolations() {
        return lowSeverityViolations;
    }
    
    public double getConformanceScore() {
        return conformanceScore;
    }
    
    public Map<NamingViolationType, Integer> getViolationCounts() {
        return violationCounts;
    }
    
    public int getCleanPackages() {
        return totalPackages - packagesWithViolations;
    }
    
    public double getViolationRate() {
        return totalPackages > 0 ? (double) packagesWithViolations / totalPackages : 0.0;
    }
    
    public double getAverageViolationsPerPackage() {
        return totalPackages > 0 ? (double) totalViolations / totalPackages : 0.0;
    }
    
    public boolean isExcellentConformance() {
        return conformanceScore >= 0.9;
    }
    
    public boolean isGoodConformance() {
        return conformanceScore >= 0.7 && conformanceScore < 0.9;
    }
    
    public boolean isFairConformance() {
        return conformanceScore >= 0.5 && conformanceScore < 0.7;
    }
    
    public boolean isPoorConformance() {
        return conformanceScore < 0.5;
    }
    
    public NamingViolationType getMostCommonViolationType() {
        return violationCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        NamingConventionSummary that = (NamingConventionSummary) obj;
        return totalPackages == that.totalPackages &&
               packagesWithViolations == that.packagesWithViolations &&
               totalViolations == that.totalViolations &&
               highSeverityViolations == that.highSeverityViolations &&
               mediumSeverityViolations == that.mediumSeverityViolations &&
               lowSeverityViolations == that.lowSeverityViolations &&
               Double.compare(that.conformanceScore, conformanceScore) == 0 &&
               Objects.equals(violationCounts, that.violationCounts);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(totalPackages, packagesWithViolations, totalViolations,
                          highSeverityViolations, mediumSeverityViolations, lowSeverityViolations,
                          conformanceScore, violationCounts);
    }
    
    @Override
    public String toString() {
        return "NamingConventionSummary{" +
                "totalPackages=" + totalPackages +
                ", violations=" + totalViolations +
                ", conformanceScore=" + String.format("%.2f", conformanceScore) +
                ", cleanPackages=" + getCleanPackages() +
                '}';
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static NamingConventionSummary clean() {
        return builder()
                .conformanceScore(1.0)
                .build();
    }
    
    public static NamingConventionSummary fromViolations(List<NamingViolation> violations) {
        Builder builder = builder();
        
        Set<String> packagesWithViolations = new HashSet<>();
        Map<NamingViolationType, Integer> violationCounts = new HashMap<>();
        int high = 0, medium = 0, low = 0;
        
        for (NamingViolation violation : violations) {
            packagesWithViolations.add(violation.getPackageName());
            violationCounts.merge(violation.getViolationType(), 1, Integer::sum);
            
            switch (violation.getSeverity()) {
                case HIGH: high++; break;
                case MEDIUM: medium++; break;
                case LOW: low++; break;
            }
        }
        
        return builder
                .totalViolations(violations.size())
                .packagesWithViolations(packagesWithViolations.size())
                .highSeverityViolations(high)
                .mediumSeverityViolations(medium)
                .lowSeverityViolations(low)
                .violationCounts(violationCounts)
                .build();
    }
    
    public static class Builder {
        private int totalPackages = 0;
        private int packagesWithViolations = 0;
        private int totalViolations = 0;
        private int highSeverityViolations = 0;
        private int mediumSeverityViolations = 0;
        private int lowSeverityViolations = 0;
        private double conformanceScore = 1.0;
        private Map<NamingViolationType, Integer> violationCounts = new HashMap<>();
        
        public Builder totalPackages(int totalPackages) {
            this.totalPackages = totalPackages;
            updateConformanceScore();
            return this;
        }
        
        public Builder packagesWithViolations(int packagesWithViolations) {
            this.packagesWithViolations = packagesWithViolations;
            updateConformanceScore();
            return this;
        }
        
        public Builder totalViolations(int totalViolations) {
            this.totalViolations = totalViolations;
            return this;
        }
        
        public Builder highSeverityViolations(int highSeverityViolations) {
            this.highSeverityViolations = highSeverityViolations;
            return this;
        }
        
        public Builder mediumSeverityViolations(int mediumSeverityViolations) {
            this.mediumSeverityViolations = mediumSeverityViolations;
            return this;
        }
        
        public Builder lowSeverityViolations(int lowSeverityViolations) {
            this.lowSeverityViolations = lowSeverityViolations;
            return this;
        }
        
        public Builder conformanceScore(double conformanceScore) {
            this.conformanceScore = conformanceScore;
            return this;
        }
        
        public Builder violationCounts(Map<NamingViolationType, Integer> violationCounts) {
            this.violationCounts = new HashMap<>(violationCounts != null ? violationCounts : Collections.emptyMap());
            return this;
        }
        
        private void updateConformanceScore() {
            if (totalPackages > 0) {
                double cleanPackageRatio = (double) (totalPackages - packagesWithViolations) / totalPackages;
                this.conformanceScore = cleanPackageRatio;
            }
        }
        
        public NamingConventionSummary build() {
            return new NamingConventionSummary(this);
        }
    }
}