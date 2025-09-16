package it.denzosoft.jreverse.analyzer.circulardependency;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Result of Spring IoC circular dependency analysis, containing detected
 * circular dependencies, metrics, and analysis metadata.
 *
 * @author JReverse Development Team
 * @since 1.1.0 (Phase 3 - T3.3.1)
 */
public class SpringCircularDependencyResult {

    private final List<SpringCircularDependency> circularDependencies;
    private final SpringCircularDependencyMetrics metrics;
    private final int totalComponents;
    private final int analyzedComponents;
    private final long analysisTimeMs;
    private final String error; // null if no error occurred

    // Analysis metadata
    private final long analysisTimestamp = System.currentTimeMillis();
    private final String analyzerVersion = "1.1.0";

    private SpringCircularDependencyResult(Builder builder) {
        this.circularDependencies = builder.circularDependencies != null ?
            new ArrayList<>(builder.circularDependencies) : new ArrayList<>();
        this.metrics = builder.metrics != null ? builder.metrics : SpringCircularDependencyMetrics.empty();
        this.totalComponents = builder.totalComponents;
        this.analyzedComponents = builder.analyzedComponents;
        this.analysisTimeMs = builder.analysisTimeMs;
        this.error = builder.error;
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public List<SpringCircularDependency> getCircularDependencies() {
        return new ArrayList<>(circularDependencies);
    }

    public SpringCircularDependencyMetrics getMetrics() {
        return metrics;
    }

    public int getTotalComponents() {
        return totalComponents;
    }

    public int getAnalyzedComponents() {
        return analyzedComponents;
    }

    public long getAnalysisTimeMs() {
        return analysisTimeMs;
    }

    public String getError() {
        return error;
    }

    public long getAnalysisTimestamp() {
        return analysisTimestamp;
    }

    public String getAnalyzerVersion() {
        return analyzerVersion;
    }

    // Utility methods

    /**
     * Checks if the analysis completed successfully.
     */
    public boolean isSuccessful() {
        return error == null;
    }

    /**
     * Checks if any circular dependencies were detected.
     */
    public boolean hasCircularDependencies() {
        return !circularDependencies.isEmpty();
    }

    /**
     * Gets the number of circular dependencies detected.
     */
    public int getCircularDependencyCount() {
        return circularDependencies.size();
    }

    /**
     * Gets circular dependencies filtered by severity.
     */
    public List<SpringCircularDependency> getCircularDependenciesBySeverity(
            it.denzosoft.jreverse.core.model.CircularDependency.CircularDependencySeverity severity) {
        return circularDependencies.stream()
            .filter(cd -> cd.getSeverity() == severity)
            .collect(Collectors.toList());
    }

    /**
     * Gets circular dependencies filtered by type.
     */
    public List<SpringCircularDependency> getCircularDependenciesByType(SpringCircularDependencyType type) {
        return circularDependencies.stream()
            .filter(cd -> cd.getType() == type)
            .collect(Collectors.toList());
    }

    /**
     * Gets circular dependencies that can be resolved with @Lazy.
     */
    public List<SpringCircularDependency> getLazyResolvableCircularDependencies() {
        return circularDependencies.stream()
            .filter(cd -> !cd.isHasLazyResolution() &&
                         (cd.getType() == SpringCircularDependencyType.CONSTRUCTOR_ONLY ||
                          cd.getType() == SpringCircularDependencyType.MIXED))
            .collect(Collectors.toList());
    }

    /**
     * Gets circular dependencies that are already resolved.
     */
    public List<SpringCircularDependency> getAlreadyResolvedCircularDependencies() {
        return circularDependencies.stream()
            .filter(SpringCircularDependency::isHasLazyResolution)
            .collect(Collectors.toList());
    }

    /**
     * Gets circular dependencies that require architectural changes.
     */
    public List<SpringCircularDependency> getArchitecturalRefactoringRequired() {
        return circularDependencies.stream()
            .filter(cd -> cd.isComplexCycle() ||
                         cd.getSeverity() == it.denzosoft.jreverse.core.model.CircularDependency.CircularDependencySeverity.CRITICAL)
            .collect(Collectors.toList());
    }

    /**
     * Gets the most critical circular dependency.
     */
    public SpringCircularDependency getMostCriticalCircularDependency() {
        return circularDependencies.stream()
            .min((cd1, cd2) -> {
                // Compare by severity first, then by cycle length
                int severityComparison = cd2.getSeverity().compareTo(cd1.getSeverity());
                if (severityComparison != 0) {
                    return severityComparison;
                }
                return Integer.compare(cd2.getCycleLength(), cd1.getCycleLength());
            })
            .orElse(null);
    }

    /**
     * Gets a list of all classes involved in circular dependencies.
     */
    public List<String> getAllInvolvedClasses() {
        return circularDependencies.stream()
            .flatMap(cd -> cd.getInvolvedClasses().stream())
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }

    /**
     * Checks if a specific class is involved in any circular dependency.
     */
    public boolean isClassInvolvedInCircularDependency(String className) {
        return circularDependencies.stream()
            .anyMatch(cd -> cd.involvesClass(className));
    }

    /**
     * Gets circular dependencies involving a specific class.
     */
    public List<SpringCircularDependency> getCircularDependenciesInvolvingClass(String className) {
        return circularDependencies.stream()
            .filter(cd -> cd.involvesClass(className))
            .collect(Collectors.toList());
    }

    /**
     * Gets the analysis coverage percentage.
     */
    public double getAnalysisCoveragePercentage() {
        return totalComponents > 0 ? (analyzedComponents * 100.0) / totalComponents : 0.0;
    }

    /**
     * Gets a summary of the analysis results.
     */
    public String getSummary() {
        if (!isSuccessful()) {
            return String.format("Analysis failed: %s", error);
        }

        StringBuilder summary = new StringBuilder();
        summary.append(String.format("Spring Circular Dependency Analysis Results\n"));
        summary.append(String.format("==========================================\n"));
        summary.append(String.format("Components analyzed: %d/%d (%.1f%%)\n",
            analyzedComponents, totalComponents, getAnalysisCoveragePercentage()));
        summary.append(String.format("Analysis time: %d ms\n", analysisTimeMs));
        summary.append(String.format("Circular dependencies found: %d\n", getCircularDependencyCount()));

        if (hasCircularDependencies()) {
            summary.append(String.format("\nSeverity breakdown:\n"));
            summary.append(String.format("- Critical: %d\n", metrics.getCriticalCircularDependencies()));
            summary.append(String.format("- High: %d\n", metrics.getHighSeverityCircularDependencies()));
            summary.append(String.format("- Medium: %d\n", metrics.getMediumSeverityCircularDependencies()));
            summary.append(String.format("- Low: %d\n", metrics.getLowSeverityCircularDependencies()));

            summary.append(String.format("\nResolution summary:\n"));
            summary.append(String.format("- Resolvable with @Lazy: %d\n", metrics.getResolvableWithLazy()));
            summary.append(String.format("- Already resolved: %d\n", metrics.getAlreadyResolved()));
            summary.append(String.format("- Require architectural changes: %d\n", metrics.getRequiresArchitecturalChanges()));

            summary.append(String.format("\nHealth score: %d/100 (%s risk)\n",
                metrics.getHealthScore(), metrics.getOverallRiskLevel()));
        } else {
            summary.append("No circular dependencies detected.\n");
        }

        return summary.toString();
    }

    /**
     * Gets detailed information about all circular dependencies.
     */
    public String getDetailedReport() {
        StringBuilder report = new StringBuilder();
        report.append(getSummary()).append("\n");

        if (hasCircularDependencies()) {
            report.append("Detailed Circular Dependency Analysis:\n");
            report.append("=====================================\n\n");

            for (int i = 0; i < circularDependencies.size(); i++) {
                SpringCircularDependency cd = circularDependencies.get(i);
                report.append(String.format("%d. %s\n", i + 1, cd.getSummary()));
                report.append(String.format("   Path: %s\n", cd.getCycleDescription()));
                report.append(String.format("   Severity: %s\n", cd.getSeverity()));
                report.append(String.format("   Type: %s\n", cd.getType()));
                report.append(String.format("   Risk: %s\n", cd.getRisk()));

                SpringResolutionStrategy primaryStrategy = cd.getPrimaryResolutionStrategy();
                if (primaryStrategy != null) {
                    report.append(String.format("   Recommended solution: %s (%s complexity)\n",
                        primaryStrategy.getDescription(), primaryStrategy.getComplexity()));
                }

                report.append("\n");
            }
        }

        return report.toString();
    }

    @Override
    public String toString() {
        return String.format("SpringCircularDependencyResult{dependencies=%d, components=%d/%d, time=%dms, success=%s}",
            getCircularDependencyCount(), analyzedComponents, totalComponents, analysisTimeMs, isSuccessful());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SpringCircularDependencyResult that = (SpringCircularDependencyResult) obj;
        return Objects.equals(circularDependencies, that.circularDependencies) &&
               Objects.equals(metrics, that.metrics);
    }

    @Override
    public int hashCode() {
        return Objects.hash(circularDependencies, metrics);
    }

    // Builder class
    public static class Builder {
        private List<SpringCircularDependency> circularDependencies;
        private SpringCircularDependencyMetrics metrics;
        private int totalComponents;
        private int analyzedComponents;
        private long analysisTimeMs;
        private String error;

        private Builder() {}

        public Builder circularDependencies(List<SpringCircularDependency> circularDependencies) {
            this.circularDependencies = circularDependencies;
            return this;
        }

        public Builder metrics(SpringCircularDependencyMetrics metrics) {
            this.metrics = metrics;
            return this;
        }

        public Builder totalComponents(int totalComponents) {
            this.totalComponents = totalComponents;
            return this;
        }

        public Builder analyzedComponents(int analyzedComponents) {
            this.analyzedComponents = analyzedComponents;
            return this;
        }

        public Builder analysisTimeMs(long analysisTimeMs) {
            this.analysisTimeMs = analysisTimeMs;
            return this;
        }

        public Builder error(String error) {
            this.error = error;
            return this;
        }

        public SpringCircularDependencyResult build() {
            return new SpringCircularDependencyResult(this);
        }
    }
}