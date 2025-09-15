package it.denzosoft.jreverse.core.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Comprehensive result model for architectural violation detection.
 * Aggregates violations from multiple architectural analysis dimensions
 * into a unified assessment of architectural quality.
 *
 * @author JReverse Development Team
 * @since 1.1.0 (Phase 3)
 */
public class ArchitecturalViolationResult {

    private final List<LayerViolation> layerViolations;
    private final List<ArchitecturalAntiPattern> antiPatterns;
    private final List<NamingViolation> namingViolations;
    private final List<OrganizationIssue> organizationIssues;
    private final ArchitecturalQualityAssessment qualityAssessment;
    private final ArchitecturalViolationMetrics metrics;
    private final long analysisTimeMs;
    private final String jarFileName;

    private ArchitecturalViolationResult(Builder builder) {
        this.layerViolations = Collections.unmodifiableList(builder.layerViolations);
        this.antiPatterns = Collections.unmodifiableList(builder.antiPatterns);
        this.namingViolations = Collections.unmodifiableList(builder.namingViolations);
        this.organizationIssues = Collections.unmodifiableList(builder.organizationIssues);
        this.qualityAssessment = Objects.requireNonNull(builder.qualityAssessment);
        this.metrics = Objects.requireNonNull(builder.metrics);
        this.analysisTimeMs = builder.analysisTimeMs;
        this.jarFileName = builder.jarFileName != null ? builder.jarFileName : "";
    }

    public static Builder builder() {
        return new Builder();
    }

    // Core Data Accessors
    public List<LayerViolation> getLayerViolations() {
        return layerViolations;
    }

    public List<ArchitecturalAntiPattern> getAntiPatterns() {
        return antiPatterns;
    }

    public List<NamingViolation> getNamingViolations() {
        return namingViolations;
    }

    public List<OrganizationIssue> getOrganizationIssues() {
        return organizationIssues;
    }

    public ArchitecturalQualityAssessment getQualityAssessment() {
        return qualityAssessment;
    }

    public ArchitecturalViolationMetrics getMetrics() {
        return metrics;
    }

    public long getAnalysisTimeMs() {
        return analysisTimeMs;
    }

    public String getJarFileName() {
        return jarFileName;
    }

    // Violation Statistics
    public int getTotalViolationsCount() {
        return layerViolations.size() + antiPatterns.size() +
               namingViolations.size() + organizationIssues.size();
    }

    public boolean hasViolations() {
        return getTotalViolationsCount() > 0;
    }

    public boolean hasCriticalViolations() {
        return hasCriticalLayerViolations() || hasCriticalAntiPatterns() ||
               hasCriticalNamingViolations() || hasCriticalOrganizationIssues();
    }

    // Layer Violations Analysis
    public boolean hasLayerViolations() {
        return !layerViolations.isEmpty();
    }

    public boolean hasCriticalLayerViolations() {
        return layerViolations.stream()
            .anyMatch(violation -> violation.getSeverity() == LayerViolation.Severity.CRITICAL);
    }

    public long getCriticalLayerViolationsCount() {
        return layerViolations.stream()
            .filter(violation -> violation.getSeverity() == LayerViolation.Severity.CRITICAL)
            .count();
    }

    // Anti-Pattern Analysis
    public boolean hasAntiPatterns() {
        return !antiPatterns.isEmpty();
    }

    public boolean hasCriticalAntiPatterns() {
        return antiPatterns.stream()
            .anyMatch(pattern -> pattern.getSeverity() == ViolationSeverity.CRITICAL);
    }

    public long getCriticalAntiPatternsCount() {
        return antiPatterns.stream()
            .filter(pattern -> pattern.getSeverity() == ViolationSeverity.CRITICAL)
            .count();
    }

    // Naming Convention Violations Analysis
    public boolean hasNamingViolations() {
        return !namingViolations.isEmpty();
    }

    public boolean hasCriticalNamingViolations() {
        return namingViolations.stream()
            .anyMatch(violation -> violation.getSeverity() == ViolationSeverity.CRITICAL);
    }

    public long getCriticalNamingViolationsCount() {
        return namingViolations.stream()
            .filter(violation -> violation.getSeverity() == ViolationSeverity.CRITICAL)
            .count();
    }

    // Organization Issues Analysis
    public boolean hasOrganizationIssues() {
        return !organizationIssues.isEmpty();
    }

    public boolean hasCriticalOrganizationIssues() {
        return organizationIssues.stream()
            .anyMatch(issue -> issue.getSeverity() == ViolationSeverity.CRITICAL);
    }

    public long getCriticalOrganizationIssuesCount() {
        return organizationIssues.stream()
            .filter(issue -> issue.getSeverity() == ViolationSeverity.CRITICAL)
            .count();
    }

    // Quality Assessment
    public double getOverallQualityScore() {
        return qualityAssessment.getOverallScore();
    }

    public String getArchitecturalGrade() {
        return qualityAssessment.getGrade();
    }

    public boolean isArchitecturallySound() {
        return getOverallQualityScore() >= 80.0 && !hasCriticalViolations();
    }

    // Severity Distribution
    public long getCriticalViolationsCount() {
        return getCriticalLayerViolationsCount() + getCriticalAntiPatternsCount() +
               getCriticalNamingViolationsCount() + getCriticalOrganizationIssuesCount();
    }

    public long getHighViolationsCount() {
        return getViolationsBySeverity(ViolationSeverity.HIGH);
    }

    public long getMediumViolationsCount() {
        return getViolationsBySeverity(ViolationSeverity.MEDIUM);
    }

    public long getLowViolationsCount() {
        return getViolationsBySeverity(ViolationSeverity.LOW);
    }

    private long getViolationsBySeverity(ViolationSeverity severity) {
        long layerCount = layerViolations.stream()
            .filter(v -> mapLayerSeverity(v.getSeverity()) == severity).count();
        long antiPatternCount = antiPatterns.stream()
            .filter(p -> p.getSeverity() == severity).count();
        long namingCount = namingViolations.stream()
            .filter(v -> v.getSeverity() == severity).count();
        long organizationCount = organizationIssues.stream()
            .filter(i -> i.getSeverity() == severity).count();

        return layerCount + antiPatternCount + namingCount + organizationCount;
    }

    private ViolationSeverity mapLayerSeverity(LayerViolation.Severity layerSeverity) {
        switch (layerSeverity) {
            case CRITICAL: return ViolationSeverity.CRITICAL;
            case HIGH: return ViolationSeverity.HIGH;
            case MEDIUM: return ViolationSeverity.MEDIUM;
            case LOW: return ViolationSeverity.LOW;
            default: return ViolationSeverity.MEDIUM;
        }
    }

    @Override
    public String toString() {
        return "ArchitecturalViolationResult{" +
                "totalViolations=" + getTotalViolationsCount() +
                ", criticalViolations=" + getCriticalViolationsCount() +
                ", qualityScore=" + String.format("%.1f", getOverallQualityScore()) +
                ", grade='" + getArchitecturalGrade() + '\'' +
                ", layerViolations=" + layerViolations.size() +
                ", antiPatterns=" + antiPatterns.size() +
                ", namingViolations=" + namingViolations.size() +
                ", organizationIssues=" + organizationIssues.size() +
                ", analysisTime=" + analysisTimeMs + "ms" +
                '}';
    }

    public static class Builder {
        private List<LayerViolation> layerViolations = Collections.emptyList();
        private List<ArchitecturalAntiPattern> antiPatterns = Collections.emptyList();
        private List<NamingViolation> namingViolations = Collections.emptyList();
        private List<OrganizationIssue> organizationIssues = Collections.emptyList();
        private ArchitecturalQualityAssessment qualityAssessment;
        private ArchitecturalViolationMetrics metrics;
        private long analysisTimeMs;
        private String jarFileName;

        public Builder layerViolations(List<LayerViolation> layerViolations) {
            this.layerViolations = layerViolations != null ? layerViolations : Collections.emptyList();
            return this;
        }

        public Builder antiPatterns(List<ArchitecturalAntiPattern> antiPatterns) {
            this.antiPatterns = antiPatterns != null ? antiPatterns : Collections.emptyList();
            return this;
        }

        public Builder namingViolations(List<NamingViolation> namingViolations) {
            this.namingViolations = namingViolations != null ? namingViolations : Collections.emptyList();
            return this;
        }

        public Builder organizationIssues(List<OrganizationIssue> organizationIssues) {
            this.organizationIssues = organizationIssues != null ? organizationIssues : Collections.emptyList();
            return this;
        }

        public Builder qualityAssessment(ArchitecturalQualityAssessment qualityAssessment) {
            this.qualityAssessment = qualityAssessment;
            return this;
        }

        public Builder metrics(ArchitecturalViolationMetrics metrics) {
            this.metrics = metrics;
            return this;
        }

        public Builder analysisTimeMs(long analysisTimeMs) {
            this.analysisTimeMs = analysisTimeMs;
            return this;
        }

        public Builder jarFileName(String jarFileName) {
            this.jarFileName = jarFileName;
            return this;
        }

        public ArchitecturalViolationResult build() {
            return new ArchitecturalViolationResult(this);
        }
    }
}