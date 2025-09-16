package it.denzosoft.jreverse.core.model;

import java.util.*;

/**
 * Result of organizational analysis for package structure.
 */
public final class OrganizationAnalysisResult {
    
    private final List<ArchitecturalPattern> detectedPatterns;
    private final OrganizationMetrics metrics;
    private final List<OrganizationIssue> issues;
    private final PackageOrganizationType organizationType;
    private final double organizationScore; // 0.0 - 1.0
    
    private OrganizationAnalysisResult(Builder builder) {
        this.detectedPatterns = Collections.unmodifiableList(new ArrayList<>(builder.detectedPatterns));
        this.metrics = builder.metrics;
        this.issues = Collections.unmodifiableList(new ArrayList<>(builder.issues));
        this.organizationType = builder.organizationType;
        this.organizationScore = Math.min(1.0, Math.max(0.0, builder.organizationScore));
    }
    
    public List<ArchitecturalPattern> getDetectedPatterns() {
        return detectedPatterns;
    }
    
    public OrganizationMetrics getMetrics() {
        return metrics;
    }
    
    public List<OrganizationIssue> getIssues() {
        return issues;
    }
    
    public PackageOrganizationType getOrganizationType() {
        return organizationType;
    }
    
    public double getOrganizationScore() {
        return organizationScore;
    }
    
    public boolean hasIssues() {
        return !issues.isEmpty();
    }
    
    public int getIssueCount() {
        return issues.size();
    }
    
    public List<OrganizationIssue> getIssuesBySeverity(ViolationSeverity severity) {
        return issues.stream()
                .filter(issue -> severity.equals(issue.getSeverity()))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    public List<OrganizationIssue> getHighSeverityIssues() {
        return getIssuesBySeverity(ViolationSeverity.HIGH);
    }
    
    public List<OrganizationIssue> getMediumSeverityIssues() {
        return getIssuesBySeverity(ViolationSeverity.MEDIUM);
    }
    
    public List<OrganizationIssue> getLowSeverityIssues() {
        return getIssuesBySeverity(ViolationSeverity.LOW);
    }
    
    public boolean hasPatternDetected(ArchitecturalPatternType patternType) {
        return detectedPatterns.stream()
                .anyMatch(pattern -> pattern.getPatternType() == patternType);
    }
    
    public List<ArchitecturalPattern> getPatternsByType(ArchitecturalPatternType patternType) {
        return detectedPatterns.stream()
                .filter(pattern -> pattern.getPatternType() == patternType)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    public boolean isWellOrganized() {
        return organizationScore >= 0.7;
    }
    
    public boolean isFairlyOrganized() {
        return organizationScore >= 0.5 && organizationScore < 0.7;
    }
    
    public boolean isPoorlyOrganized() {
        return organizationScore < 0.5;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        OrganizationAnalysisResult that = (OrganizationAnalysisResult) obj;
        return Double.compare(that.organizationScore, organizationScore) == 0 &&
               Objects.equals(detectedPatterns, that.detectedPatterns) &&
               Objects.equals(metrics, that.metrics) &&
               Objects.equals(issues, that.issues) &&
               organizationType == that.organizationType;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(detectedPatterns, metrics, issues, organizationType, organizationScore);
    }
    
    @Override
    public String toString() {
        return "OrganizationAnalysisResult{" +
                "organizationType=" + organizationType +
                ", patternsDetected=" + detectedPatterns.size() +
                ", issuesFound=" + issues.size() +
                ", organizationScore=" + String.format("%.2f", organizationScore) +
                '}';
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static OrganizationAnalysisResult clean(PackageOrganizationType organizationType) {
        return builder()
                .organizationType(organizationType)
                .organizationScore(1.0)
                .build();
    }
    
    public static class Builder {
        private List<ArchitecturalPattern> detectedPatterns = new ArrayList<>();
        private OrganizationMetrics metrics;
        private List<OrganizationIssue> issues = new ArrayList<>();
        private PackageOrganizationType organizationType = PackageOrganizationType.UNKNOWN;
        private double organizationScore = 0.0;
        
        public Builder addPattern(ArchitecturalPattern pattern) {
            if (pattern != null) {
                detectedPatterns.add(pattern);
            }
            return this;
        }
        
        public Builder detectedPatterns(List<ArchitecturalPattern> patterns) {
            this.detectedPatterns = new ArrayList<>(patterns != null ? patterns : Collections.emptyList());
            return this;
        }
        
        public Builder metrics(OrganizationMetrics metrics) {
            this.metrics = metrics;
            return this;
        }
        
        public Builder addIssue(OrganizationIssue issue) {
            if (issue != null) {
                issues.add(issue);
            }
            return this;
        }
        
        public Builder issues(List<OrganizationIssue> issues) {
            this.issues = new ArrayList<>(issues != null ? issues : Collections.emptyList());
            return this;
        }
        
        public Builder organizationType(PackageOrganizationType organizationType) {
            this.organizationType = organizationType != null ? organizationType : PackageOrganizationType.UNKNOWN;
            return this;
        }
        
        public Builder organizationScore(double organizationScore) {
            this.organizationScore = organizationScore;
            return this;
        }
        
        public OrganizationAnalysisResult build() {
            return new OrganizationAnalysisResult(this);
        }
    }
}