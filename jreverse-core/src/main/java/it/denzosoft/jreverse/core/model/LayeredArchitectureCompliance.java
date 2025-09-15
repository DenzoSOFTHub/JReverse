package it.denzosoft.jreverse.core.model;

import java.util.*;

/**
 * Represents the compliance assessment of a layered architecture.
 * This is an immutable value object following Clean Architecture principles.
 */
public final class LayeredArchitectureCompliance {
    
    private final ComplianceLevel overallCompliance;
    private final double complianceScore; // 0.0 - 1.0
    private final Map<LayerType, ComplianceLevel> layerCompliance;
    private final Set<LayerViolation> violations;
    private final Map<String, String> complianceDetails;
    private final Set<String> compliantAreas;
    private final Set<String> nonCompliantAreas;
    private final List<String> complianceIssues;
    private final List<String> strengths;
    private final Map<LayerViolation.Severity, Integer> violationSummary;
    
    private LayeredArchitectureCompliance(Builder builder) {
        this.overallCompliance = Objects.requireNonNull(builder.overallCompliance, "overallCompliance cannot be null");
        this.complianceScore = Math.min(1.0, Math.max(0.0, builder.complianceScore));
        this.layerCompliance = Collections.unmodifiableMap(new HashMap<>(builder.layerCompliance));
        this.violations = Collections.unmodifiableSet(new HashSet<>(builder.violations));
        this.complianceDetails = Collections.unmodifiableMap(new HashMap<>(builder.complianceDetails));
        this.compliantAreas = Collections.unmodifiableSet(new HashSet<>(builder.compliantAreas));
        this.nonCompliantAreas = Collections.unmodifiableSet(new HashSet<>(builder.nonCompliantAreas));
        this.complianceIssues = Collections.unmodifiableList(new ArrayList<>(builder.complianceIssues));
        this.strengths = Collections.unmodifiableList(new ArrayList<>(builder.strengths));
        this.violationSummary = Collections.unmodifiableMap(new HashMap<>(builder.violationSummary));
    }
    
    public ComplianceLevel getOverallCompliance() {
        return overallCompliance;
    }
    
    public double getComplianceScore() {
        return complianceScore;
    }
    
    public Map<LayerType, ComplianceLevel> getLayerCompliance() {
        return layerCompliance;
    }
    
    public Set<LayerViolation> getViolations() {
        return violations;
    }
    
    public Map<String, String> getComplianceDetails() {
        return complianceDetails;
    }
    
    public Set<String> getCompliantAreas() {
        return compliantAreas;
    }
    
    public Set<String> getNonCompliantAreas() {
        return nonCompliantAreas;
    }
    
    public List<String> getComplianceIssues() {
        return complianceIssues;
    }
    
    public List<String> getStrengths() {
        return strengths;
    }
    
    public Map<LayerViolation.Severity, Integer> getViolationSummary() {
        return violationSummary;
    }
    
    public ComplianceLevel getLayerCompliance(LayerType layerType) {
        return layerCompliance.getOrDefault(layerType, ComplianceLevel.UNKNOWN);
    }
    
    public boolean isLayerCompliant(LayerType layerType) {
        ComplianceLevel level = getLayerCompliance(layerType);
        return level == ComplianceLevel.EXCELLENT || level == ComplianceLevel.GOOD;
    }
    
    public boolean isOverallCompliant() {
        return overallCompliance == ComplianceLevel.EXCELLENT || overallCompliance == ComplianceLevel.GOOD;
    }
    
    public int getTotalViolations() {
        return violations.size();
    }
    
    public int getViolationCount(LayerViolation.Severity severity) {
        return violationSummary.getOrDefault(severity, 0);
    }
    
    public int getCriticalViolations() {
        return getViolationCount(LayerViolation.Severity.CRITICAL);
    }
    
    public int getHighSeverityViolations() {
        return getViolationCount(LayerViolation.Severity.HIGH);
    }
    
    public int getMediumSeverityViolations() {
        return getViolationCount(LayerViolation.Severity.MEDIUM);
    }
    
    public int getLowSeverityViolations() {
        return getViolationCount(LayerViolation.Severity.LOW);
    }
    
    public Set<LayerViolation> getViolationsBySeverity(LayerViolation.Severity severity) {
        return violations.stream()
                .filter(v -> v.getSeverity() == severity)
                .collect(HashSet::new, HashSet::add, HashSet::addAll);
    }
    
    public Set<LayerViolation> getViolationsByType(LayerViolation.Type type) {
        return violations.stream()
                .filter(v -> v.getViolationType() == type)
                .collect(HashSet::new, HashSet::add, HashSet::addAll);
    }
    
    public Set<LayerViolation> getViolationsForLayer(LayerType layerType) {
        return violations.stream()
                .filter(v -> layerType.equals(v.getSourceLayer()) || layerType.equals(v.getTargetLayer()))
                .collect(HashSet::new, HashSet::add, HashSet::addAll);
    }
    
    public boolean hasDetailFor(String key) {
        return complianceDetails.containsKey(key);
    }
    
    public String getDetail(String key) {
        return complianceDetails.get(key);
    }
    
    public boolean isCompliantArea(String area) {
        return compliantAreas.contains(area);
    }
    
    public boolean isNonCompliantArea(String area) {
        return nonCompliantAreas.contains(area);
    }
    
    public int getCompliantAreaCount() {
        return compliantAreas.size();
    }
    
    public int getNonCompliantAreaCount() {
        return nonCompliantAreas.size();
    }
    
    public int getIssueCount() {
        return complianceIssues.size();
    }
    
    public int getStrengthCount() {
        return strengths.size();
    }
    
    public double getCompliancePercentage() {
        return complianceScore * 100.0;
    }
    
    public boolean hasCriticalIssues() {
        return getCriticalViolations() > 0;
    }
    
    public boolean hasSignificantIssues() {
        return getCriticalViolations() > 0 || getHighSeverityViolations() > 0;
    }
    
    public String getComplianceSummary() {
        return String.format("Overall: %s (%.1f%%), Violations: %d, Critical: %d",
                overallCompliance.getDisplayName(),
                getCompliancePercentage(),
                getTotalViolations(),
                getCriticalViolations());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        LayeredArchitectureCompliance that = (LayeredArchitectureCompliance) obj;
        return Double.compare(that.complianceScore, complianceScore) == 0 &&
               overallCompliance == that.overallCompliance &&
               Objects.equals(layerCompliance, that.layerCompliance) &&
               Objects.equals(violations, that.violations) &&
               Objects.equals(complianceDetails, that.complianceDetails) &&
               Objects.equals(compliantAreas, that.compliantAreas) &&
               Objects.equals(nonCompliantAreas, that.nonCompliantAreas) &&
               Objects.equals(complianceIssues, that.complianceIssues) &&
               Objects.equals(strengths, that.strengths) &&
               Objects.equals(violationSummary, that.violationSummary);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(overallCompliance, complianceScore, layerCompliance, violations,
                complianceDetails, compliantAreas, nonCompliantAreas, complianceIssues,
                strengths, violationSummary);
    }
    
    @Override
    public String toString() {
        return "LayeredArchitectureCompliance{" +
                "overall=" + overallCompliance +
                ", score=" + String.format("%.2f", complianceScore) +
                ", violations=" + getTotalViolations() +
                ", critical=" + getCriticalViolations() +
                '}';
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public enum ComplianceLevel {
        EXCELLENT("Excellent", 4, "Exceptional adherence to layered architecture principles"),
        GOOD("Good", 3, "Good adherence with minor issues"),
        FAIR("Fair", 2, "Adequate adherence with some violations"),
        POOR("Poor", 1, "Poor adherence with significant violations"),
        UNKNOWN("Unknown", 0, "Compliance level cannot be determined");
        
        private final String displayName;
        private final int priority;
        private final String description;
        
        ComplianceLevel(String displayName, int priority, String description) {
            this.displayName = displayName;
            this.priority = priority;
            this.description = description;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public int getPriority() {
            return priority;
        }
        
        public String getDescription() {
            return description;
        }
        
        public boolean isBetterThan(ComplianceLevel other) {
            return this.priority > other.priority;
        }
        
        public boolean isWorseThan(ComplianceLevel other) {
            return this.priority < other.priority;
        }
        
        public static ComplianceLevel fromScore(double score) {
            if (score >= 0.9) return EXCELLENT;
            if (score >= 0.7) return GOOD;
            if (score >= 0.5) return FAIR;
            if (score >= 0.0) return POOR;
            return UNKNOWN;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
    
    public static class Builder {
        private ComplianceLevel overallCompliance = ComplianceLevel.UNKNOWN;
        private double complianceScore = 0.5;
        private Map<LayerType, ComplianceLevel> layerCompliance = new HashMap<>();
        private Set<LayerViolation> violations = new HashSet<>();
        private Map<String, String> complianceDetails = new HashMap<>();
        private Set<String> compliantAreas = new HashSet<>();
        private Set<String> nonCompliantAreas = new HashSet<>();
        private List<String> complianceIssues = new ArrayList<>();
        private List<String> strengths = new ArrayList<>();
        private Map<LayerViolation.Severity, Integer> violationSummary = new HashMap<>();
        
        public Builder overallCompliance(ComplianceLevel overallCompliance) {
            this.overallCompliance = overallCompliance;
            return this;
        }
        
        public Builder complianceScore(double complianceScore) {
            this.complianceScore = complianceScore;
            // Auto-set overall compliance if not already set
            if (overallCompliance == ComplianceLevel.UNKNOWN) {
                overallCompliance = ComplianceLevel.fromScore(complianceScore);
            }
            return this;
        }
        
        public Builder addLayerCompliance(LayerType layerType, ComplianceLevel compliance) {
            layerCompliance.put(layerType, compliance);
            return this;
        }
        
        public Builder layerCompliance(Map<LayerType, ComplianceLevel> layerCompliance) {
            this.layerCompliance = new HashMap<>(layerCompliance != null ? layerCompliance : Collections.emptyMap());
            return this;
        }
        
        public Builder addViolation(LayerViolation violation) {
            if (violation != null) {
                violations.add(violation);
                // Update violation summary
                violationSummary.merge(violation.getSeverity(), 1, Integer::sum);
            }
            return this;
        }
        
        public Builder violations(Set<LayerViolation> violations) {
            this.violations = new HashSet<>(violations != null ? violations : Collections.emptySet());
            // Recalculate violation summary
            violationSummary.clear();
            this.violations.forEach(v -> violationSummary.merge(v.getSeverity(), 1, Integer::sum));
            return this;
        }
        
        public Builder addDetail(String key, String value) {
            if (key != null && value != null) {
                complianceDetails.put(key, value);
            }
            return this;
        }
        
        public Builder complianceDetails(Map<String, String> details) {
            this.complianceDetails = new HashMap<>(details != null ? details : Collections.emptyMap());
            return this;
        }
        
        public Builder addCompliantArea(String area) {
            if (area != null && !area.trim().isEmpty()) {
                compliantAreas.add(area.trim());
            }
            return this;
        }
        
        public Builder compliantAreas(Set<String> areas) {
            this.compliantAreas = new HashSet<>(areas != null ? areas : Collections.emptySet());
            return this;
        }
        
        public Builder addNonCompliantArea(String area) {
            if (area != null && !area.trim().isEmpty()) {
                nonCompliantAreas.add(area.trim());
            }
            return this;
        }
        
        public Builder nonCompliantAreas(Set<String> areas) {
            this.nonCompliantAreas = new HashSet<>(areas != null ? areas : Collections.emptySet());
            return this;
        }
        
        public Builder addIssue(String issue) {
            if (issue != null && !issue.trim().isEmpty()) {
                complianceIssues.add(issue.trim());
            }
            return this;
        }
        
        public Builder complianceIssues(List<String> issues) {
            this.complianceIssues = new ArrayList<>(issues != null ? issues : Collections.emptyList());
            return this;
        }
        
        public Builder addStrength(String strength) {
            if (strength != null && !strength.trim().isEmpty()) {
                strengths.add(strength.trim());
            }
            return this;
        }
        
        public Builder strengths(List<String> strengths) {
            this.strengths = new ArrayList<>(strengths != null ? strengths : Collections.emptyList());
            return this;
        }
        
        public Builder violationSummary(Map<LayerViolation.Severity, Integer> summary) {
            this.violationSummary = new HashMap<>(summary != null ? summary : Collections.emptyMap());
            return this;
        }
        
        public LayeredArchitectureCompliance build() {
            // Auto-calculate compliance score if violations are provided but score is default
            if (complianceScore == 0.5 && !violations.isEmpty()) {
                int totalViolations = violations.size();
                int criticalViolations = (int) violations.stream().filter(v -> v.isCriticalSeverity()).count();
                int highViolations = (int) violations.stream().filter(v -> v.isHighSeverity()).count();
                
                // Simple scoring: start at 1.0 and deduct for violations
                double score = 1.0;
                score -= criticalViolations * 0.2; // Critical violations have high impact
                score -= highViolations * 0.1; // High violations have medium impact
                score -= (totalViolations - criticalViolations - highViolations) * 0.05; // Other violations have low impact
                
                complianceScore = Math.max(0.0, score);
                
                if (overallCompliance == ComplianceLevel.UNKNOWN) {
                    overallCompliance = ComplianceLevel.fromScore(complianceScore);
                }
            }
            
            return new LayeredArchitectureCompliance(this);
        }
    }
}