package it.denzosoft.jreverse.core.model;

import java.util.*;

/**
 * Represents an architectural anti-pattern detected in the package structure.
 */
public final class ArchitecturalAntiPattern {
    
    private final AntiPatternType patternType;
    private final String description;
    private final ViolationSeverity severity;
    private final Set<String> affectedPackages;
    private final String location;
    private final String recommendation;
    private final double impactScore; // 0.0 - 1.0
    
    private ArchitecturalAntiPattern(Builder builder) {
        this.patternType = Objects.requireNonNull(builder.patternType, "patternType cannot be null");
        this.description = requireNonEmpty(builder.description, "description");
        this.severity = Objects.requireNonNull(builder.severity, "severity cannot be null");
        this.affectedPackages = Collections.unmodifiableSet(new HashSet<>(builder.affectedPackages));
        this.location = builder.location;
        this.recommendation = builder.recommendation;
        this.impactScore = Math.min(1.0, Math.max(0.0, builder.impactScore));
    }
    
    public AntiPatternType getPatternType() {
        return patternType;
    }
    
    public String getDescription() {
        return description;
    }
    
    public ViolationSeverity getSeverity() {
        return severity;
    }
    
    public Set<String> getAffectedPackages() {
        return affectedPackages;
    }
    
    public String getLocation() {
        return location;
    }
    
    public String getRecommendation() {
        return recommendation;
    }
    
    public double getImpactScore() {
        return impactScore;
    }
    
    public boolean hasLocation() {
        return location != null && !location.isEmpty();
    }
    
    public boolean hasRecommendation() {
        return recommendation != null && !recommendation.isEmpty();
    }
    
    public int getAffectedPackageCount() {
        return affectedPackages.size();
    }
    
    public boolean isHighImpact() {
        return impactScore >= 0.7;
    }
    
    public boolean isMediumImpact() {
        return impactScore >= 0.4 && impactScore < 0.7;
    }
    
    public boolean isLowImpact() {
        return impactScore < 0.4;
    }
    
    private String requireNonEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }
        return value.trim();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ArchitecturalAntiPattern that = (ArchitecturalAntiPattern) obj;
        return Double.compare(that.impactScore, impactScore) == 0 &&
               patternType == that.patternType &&
               Objects.equals(description, that.description) &&
               severity == that.severity &&
               Objects.equals(affectedPackages, that.affectedPackages) &&
               Objects.equals(location, that.location) &&
               Objects.equals(recommendation, that.recommendation);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(patternType, description, severity, affectedPackages, 
                          location, recommendation, impactScore);
    }
    
    @Override
    public String toString() {
        return "ArchitecturalAntiPattern{" +
                "type=" + patternType +
                ", severity=" + severity +
                ", affectedPackages=" + affectedPackages.size() +
                ", impactScore=" + String.format("%.2f", impactScore) +
                '}';
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private AntiPatternType patternType;
        private String description;
        private ViolationSeverity severity = ViolationSeverity.MEDIUM;
        private Set<String> affectedPackages = new HashSet<>();
        private String location;
        private String recommendation;
        private double impactScore = 0.5;
        
        public Builder patternType(AntiPatternType patternType) {
            this.patternType = patternType;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder severity(ViolationSeverity severity) {
            this.severity = severity;
            return this;
        }
        
        public Builder addAffectedPackage(String packageName) {
            if (packageName != null && !packageName.trim().isEmpty()) {
                affectedPackages.add(packageName.trim());
            }
            return this;
        }
        
        public Builder affectedPackages(Set<String> packages) {
            this.affectedPackages = new HashSet<>(packages != null ? packages : Collections.emptySet());
            return this;
        }
        
        public Builder location(String location) {
            this.location = location;
            return this;
        }
        
        public Builder recommendation(String recommendation) {
            this.recommendation = recommendation;
            return this;
        }
        
        public Builder impactScore(double impactScore) {
            this.impactScore = impactScore;
            return this;
        }
        
        public ArchitecturalAntiPattern build() {
            return new ArchitecturalAntiPattern(this);
        }
    }
}