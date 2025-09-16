package it.denzosoft.jreverse.core.model;

import java.util.*;

/**
 * Represents a detected anti-pattern in the analyzed codebase.
 * This is an immutable value object following Clean Architecture principles.
 */
public final class DetectedAntiPattern {
    
    private final AntiPatternType antiPatternType;
    private final String name;
    private final String description;
    private final ViolationSeverity severity;
    private final Set<String> affectedClasses;
    private final Set<String> affectedPackages;
    private final String location;
    private final String recommendation;
    private final double impactScore; // 0.0 - 1.0
    private final Map<String, String> evidence;
    private final Set<String> violationDetails;
    
    private DetectedAntiPattern(Builder builder) {
        this.antiPatternType = Objects.requireNonNull(builder.antiPatternType, "antiPatternType cannot be null");
        this.name = requireNonEmpty(builder.name, "name");
        this.description = builder.description;
        this.severity = Objects.requireNonNull(builder.severity, "severity cannot be null");
        this.affectedClasses = Collections.unmodifiableSet(new HashSet<>(builder.affectedClasses));
        this.affectedPackages = Collections.unmodifiableSet(new HashSet<>(builder.affectedPackages));
        this.location = builder.location;
        this.recommendation = builder.recommendation;
        this.impactScore = Math.min(1.0, Math.max(0.0, builder.impactScore));
        this.evidence = Collections.unmodifiableMap(new HashMap<>(builder.evidence));
        this.violationDetails = Collections.unmodifiableSet(new HashSet<>(builder.violationDetails));
    }
    
    public AntiPatternType getAntiPatternType() {
        return antiPatternType;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public ViolationSeverity getSeverity() {
        return severity;
    }
    
    public Set<String> getAffectedClasses() {
        return affectedClasses;
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
    
    public Map<String, String> getEvidence() {
        return evidence;
    }
    
    public Set<String> getViolationDetails() {
        return violationDetails;
    }
    
    public boolean hasLocation() {
        return location != null && !location.isEmpty();
    }
    
    public boolean hasRecommendation() {
        return recommendation != null && !recommendation.isEmpty();
    }
    
    public boolean isHighSeverity() {
        return severity == ViolationSeverity.HIGH;
    }
    
    public boolean isMediumSeverity() {
        return severity == ViolationSeverity.MEDIUM;
    }
    
    public boolean isLowSeverity() {
        return severity == ViolationSeverity.LOW;
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
    
    public int getTotalAffectedElements() {
        return affectedClasses.size() + affectedPackages.size();
    }
    
    public boolean hasEvidence(String evidenceKey) {
        return evidence.containsKey(evidenceKey);
    }
    
    public String getEvidenceValue(String evidenceKey) {
        return evidence.get(evidenceKey);
    }
    
    public boolean hasViolationDetail(String detail) {
        return violationDetails.contains(detail);
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
        DetectedAntiPattern that = (DetectedAntiPattern) obj;
        return Double.compare(that.impactScore, impactScore) == 0 &&
               antiPatternType == that.antiPatternType &&
               Objects.equals(name, that.name) &&
               Objects.equals(description, that.description) &&
               severity == that.severity &&
               Objects.equals(affectedClasses, that.affectedClasses) &&
               Objects.equals(affectedPackages, that.affectedPackages) &&
               Objects.equals(location, that.location) &&
               Objects.equals(recommendation, that.recommendation) &&
               Objects.equals(evidence, that.evidence) &&
               Objects.equals(violationDetails, that.violationDetails);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(antiPatternType, name, description, severity, affectedClasses,
                affectedPackages, location, recommendation, impactScore, evidence, violationDetails);
    }
    
    @Override
    public String toString() {
        return "DetectedAntiPattern{" +
                "type=" + antiPatternType +
                ", name='" + name + '\'' +
                ", severity=" + severity +
                ", impact=" + String.format("%.2f", impactScore) +
                ", affectedClasses=" + affectedClasses.size() +
                ", affectedPackages=" + affectedPackages.size() +
                '}';
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private AntiPatternType antiPatternType;
        private String name;
        private String description;
        private ViolationSeverity severity = ViolationSeverity.MEDIUM;
        private Set<String> affectedClasses = new HashSet<>();
        private Set<String> affectedPackages = new HashSet<>();
        private String location;
        private String recommendation;
        private double impactScore = 0.5;
        private Map<String, String> evidence = new HashMap<>();
        private Set<String> violationDetails = new HashSet<>();
        
        public Builder antiPatternType(AntiPatternType antiPatternType) {
            this.antiPatternType = antiPatternType;
            if (antiPatternType != null) {
                this.name = antiPatternType.getDisplayName();
                this.description = antiPatternType.getDescription();
            }
            return this;
        }
        
        public Builder name(String name) {
            this.name = name;
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
        
        public Builder addAffectedClass(String className) {
            if (className != null && !className.trim().isEmpty()) {
                affectedClasses.add(className.trim());
            }
            return this;
        }
        
        public Builder affectedClasses(Set<String> classes) {
            this.affectedClasses = new HashSet<>(classes != null ? classes : Collections.emptySet());
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
        
        public Builder addEvidence(String key, String value) {
            if (key != null && value != null) {
                evidence.put(key, value);
            }
            return this;
        }
        
        public Builder evidence(Map<String, String> evidence) {
            this.evidence = new HashMap<>(evidence != null ? evidence : Collections.emptyMap());
            return this;
        }
        
        public Builder addViolationDetail(String detail) {
            if (detail != null && !detail.trim().isEmpty()) {
                violationDetails.add(detail.trim());
            }
            return this;
        }
        
        public Builder violationDetails(Set<String> details) {
            this.violationDetails = new HashSet<>(details != null ? details : Collections.emptySet());
            return this;
        }
        
        public DetectedAntiPattern build() {
            // Set default name if not provided
            if (name == null && antiPatternType != null) {
                name = antiPatternType.getDisplayName();
            }
            return new DetectedAntiPattern(this);
        }
    }
}