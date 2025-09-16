package it.denzosoft.jreverse.analyzer.circulardependency;

import it.denzosoft.jreverse.core.model.CircularDependency;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Metrics and statistics for Spring circular dependency analysis,
 * providing quantitative insights into dependency patterns and issues.
 *
 * @author JReverse Development Team
 * @since 1.1.0 (Phase 3 - T3.3.1)
 */
public class SpringCircularDependencyMetrics {

    // Basic metrics
    private int totalCircularDependencies;
    private int totalComponents;
    private int affectedComponents;
    private double circularDependencyRatio;
    private double averageCycleLength;
    private double complexityScore;

    // Distribution metrics
    private Map<CircularDependency.CircularDependencySeverity, Long> severityDistribution = new HashMap<>();
    private Map<SpringCircularDependencyType, Long> typeDistribution = new HashMap<>();
    private Map<SpringCircularDependencyRisk, Long> riskDistribution = new HashMap<>();

    // Resolution metrics
    private int resolvableWithLazy;
    private int requiresArchitecturalChanges;
    private int alreadyResolved;

    // Component type metrics
    private Map<SpringComponentType, Integer> componentTypeCount = new HashMap<>();
    private Map<SpringComponentType, Integer> affectedComponentsByType = new HashMap<>();

    // Performance metrics
    private long analysisTimeMs;
    private int maxCycleLength;
    private int minCycleLength;

    private SpringCircularDependencyMetrics(Builder builder) {
        this.totalCircularDependencies = builder.totalCircularDependencies;
        this.totalComponents = builder.totalComponents;
        this.affectedComponents = builder.affectedComponents;
        this.circularDependencyRatio = builder.circularDependencyRatio;
        this.averageCycleLength = builder.averageCycleLength;
        this.complexityScore = builder.complexityScore;
        this.severityDistribution = new HashMap<>(builder.severityDistribution);
        this.typeDistribution = new HashMap<>(builder.typeDistribution);
        this.riskDistribution = new HashMap<>(builder.riskDistribution);
        this.resolvableWithLazy = builder.resolvableWithLazy;
        this.requiresArchitecturalChanges = builder.requiresArchitecturalChanges;
        this.alreadyResolved = builder.alreadyResolved;
        this.componentTypeCount = new HashMap<>(builder.componentTypeCount);
        this.affectedComponentsByType = new HashMap<>(builder.affectedComponentsByType);
        this.analysisTimeMs = builder.analysisTimeMs;
        this.maxCycleLength = builder.maxCycleLength;
        this.minCycleLength = builder.minCycleLength;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static SpringCircularDependencyMetrics empty() {
        return builder().build();
    }

    // Getters
    public int getTotalCircularDependencies() {
        return totalCircularDependencies;
    }

    public int getTotalComponents() {
        return totalComponents;
    }

    public int getAffectedComponents() {
        return affectedComponents;
    }

    public double getCircularDependencyRatio() {
        return circularDependencyRatio;
    }

    public double getAverageCycleLength() {
        return averageCycleLength;
    }

    public double getComplexityScore() {
        return complexityScore;
    }

    public Map<CircularDependency.CircularDependencySeverity, Long> getSeverityDistribution() {
        return new HashMap<>(severityDistribution);
    }

    public Map<SpringCircularDependencyType, Long> getTypeDistribution() {
        return new HashMap<>(typeDistribution);
    }

    public Map<SpringCircularDependencyRisk, Long> getRiskDistribution() {
        return new HashMap<>(riskDistribution);
    }

    public int getResolvableWithLazy() {
        return resolvableWithLazy;
    }

    public int getRequiresArchitecturalChanges() {
        return requiresArchitecturalChanges;
    }

    public int getAlreadyResolved() {
        return alreadyResolved;
    }

    public Map<SpringComponentType, Integer> getComponentTypeCount() {
        return new HashMap<>(componentTypeCount);
    }

    public Map<SpringComponentType, Integer> getAffectedComponentsByType() {
        return new HashMap<>(affectedComponentsByType);
    }

    public long getAnalysisTimeMs() {
        return analysisTimeMs;
    }

    public int getMaxCycleLength() {
        return maxCycleLength;
    }

    public int getMinCycleLength() {
        return minCycleLength;
    }

    // Calculated metrics

    /**
     * Gets the percentage of components affected by circular dependencies.
     */
    public double getAffectedComponentsPercentage() {
        return totalComponents > 0 ? (affectedComponents * 100.0) / totalComponents : 0.0;
    }

    /**
     * Gets the number of critical circular dependencies.
     */
    public long getCriticalCircularDependencies() {
        return severityDistribution.getOrDefault(CircularDependency.CircularDependencySeverity.CRITICAL, 0L);
    }

    /**
     * Gets the number of high severity circular dependencies.
     */
    public long getHighSeverityCircularDependencies() {
        return severityDistribution.getOrDefault(CircularDependency.CircularDependencySeverity.HIGH, 0L);
    }

    /**
     * Gets the number of medium severity circular dependencies.
     */
    public long getMediumSeverityCircularDependencies() {
        return severityDistribution.getOrDefault(CircularDependency.CircularDependencySeverity.MEDIUM, 0L);
    }

    /**
     * Gets the number of low severity circular dependencies.
     */
    public long getLowSeverityCircularDependencies() {
        return severityDistribution.getOrDefault(CircularDependency.CircularDependencySeverity.LOW, 0L);
    }

    /**
     * Gets the percentage of circular dependencies that can be resolved with @Lazy.
     */
    public double getLazyResolvablePercentage() {
        return totalCircularDependencies > 0 ? (resolvableWithLazy * 100.0) / totalCircularDependencies : 0.0;
    }

    /**
     * Gets the percentage of circular dependencies already resolved.
     */
    public double getAlreadyResolvedPercentage() {
        return totalCircularDependencies > 0 ? (alreadyResolved * 100.0) / totalCircularDependencies : 0.0;
    }

    /**
     * Gets the health score (0-100) based on circular dependency metrics.
     */
    public int getHealthScore() {
        if (totalComponents == 0) {
            return 100; // No components = perfect health
        }

        double score = 100.0;

        // Penalize based on affected component ratio
        score -= (circularDependencyRatio * 50);

        // Penalize based on severity distribution
        score -= (getCriticalCircularDependencies() * 15);
        score -= (getHighSeverityCircularDependencies() * 10);
        score -= (getMediumSeverityCircularDependencies() * 5);
        score -= (getLowSeverityCircularDependencies() * 2);

        // Penalize based on complexity
        score -= (complexityScore * 0.3);

        // Bonus for resolved dependencies
        score += (getAlreadyResolvedPercentage() * 0.2);

        return Math.max(0, Math.min(100, (int) score));
    }

    /**
     * Gets the risk level based on metrics.
     */
    public String getOverallRiskLevel() {
        long criticalCount = getCriticalCircularDependencies();
        long highCount = getHighSeverityCircularDependencies();

        if (criticalCount > 0) {
            return "CRITICAL";
        } else if (highCount > 2) {
            return "HIGH";
        } else if (getAffectedComponentsPercentage() > 20) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }

    /**
     * Gets the most problematic component type.
     */
    public SpringComponentType getMostProblematicComponentType() {
        return affectedComponentsByType.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }

    /**
     * Gets the most common circular dependency type.
     */
    public SpringCircularDependencyType getMostCommonDependencyType() {
        return typeDistribution.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }

    /**
     * Gets a summary of the metrics.
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(String.format("Circular Dependencies: %d/%d components (%.1f%%)\n",
            affectedComponents, totalComponents, getAffectedComponentsPercentage()));
        summary.append(String.format("Health Score: %d/100 (%s risk)\n",
            getHealthScore(), getOverallRiskLevel()));
        summary.append(String.format("Severity Distribution: %d critical, %d high, %d medium, %d low\n",
            getCriticalCircularDependencies(), getHighSeverityCircularDependencies(),
            getMediumSeverityCircularDependencies(), getLowSeverityCircularDependencies()));
        summary.append(String.format("Resolution: %d resolvable with @Lazy (%.1f%%), %d already resolved (%.1f%%)\n",
            resolvableWithLazy, getLazyResolvablePercentage(),
            alreadyResolved, getAlreadyResolvedPercentage()));
        return summary.toString();
    }

    @Override
    public String toString() {
        return String.format("SpringCircularDependencyMetrics{total=%d, affected=%d/%d, health=%d/100}",
            totalCircularDependencies, affectedComponents, totalComponents, getHealthScore());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SpringCircularDependencyMetrics that = (SpringCircularDependencyMetrics) obj;
        return totalCircularDependencies == that.totalCircularDependencies &&
               totalComponents == that.totalComponents &&
               affectedComponents == that.affectedComponents;
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalCircularDependencies, totalComponents, affectedComponents);
    }

    // Builder class
    public static class Builder {
        private int totalCircularDependencies;
        private int totalComponents;
        private int affectedComponents;
        private double circularDependencyRatio;
        private double averageCycleLength;
        private double complexityScore;
        private Map<CircularDependency.CircularDependencySeverity, Long> severityDistribution = new HashMap<>();
        private Map<SpringCircularDependencyType, Long> typeDistribution = new HashMap<>();
        private Map<SpringCircularDependencyRisk, Long> riskDistribution = new HashMap<>();
        private int resolvableWithLazy;
        private int requiresArchitecturalChanges;
        private int alreadyResolved;
        private Map<SpringComponentType, Integer> componentTypeCount = new HashMap<>();
        private Map<SpringComponentType, Integer> affectedComponentsByType = new HashMap<>();
        private long analysisTimeMs;
        private int maxCycleLength;
        private int minCycleLength;

        private Builder() {}

        public Builder totalCircularDependencies(int totalCircularDependencies) {
            this.totalCircularDependencies = totalCircularDependencies;
            return this;
        }

        public Builder totalComponents(int totalComponents) {
            this.totalComponents = totalComponents;
            return this;
        }

        public Builder affectedComponents(int affectedComponents) {
            this.affectedComponents = affectedComponents;
            return this;
        }

        public Builder circularDependencyRatio(double circularDependencyRatio) {
            this.circularDependencyRatio = circularDependencyRatio;
            return this;
        }

        public Builder averageCycleLength(double averageCycleLength) {
            this.averageCycleLength = averageCycleLength;
            return this;
        }

        public Builder complexityScore(double complexityScore) {
            this.complexityScore = complexityScore;
            return this;
        }

        public Builder severityDistribution(Map<CircularDependency.CircularDependencySeverity, Long> severityDistribution) {
            this.severityDistribution = severityDistribution != null ? new HashMap<>(severityDistribution) : new HashMap<>();
            return this;
        }

        public Builder typeDistribution(Map<SpringCircularDependencyType, Long> typeDistribution) {
            this.typeDistribution = typeDistribution != null ? new HashMap<>(typeDistribution) : new HashMap<>();
            return this;
        }

        public Builder riskDistribution(Map<SpringCircularDependencyRisk, Long> riskDistribution) {
            this.riskDistribution = riskDistribution != null ? new HashMap<>(riskDistribution) : new HashMap<>();
            return this;
        }

        public Builder resolvableWithLazy(int resolvableWithLazy) {
            this.resolvableWithLazy = resolvableWithLazy;
            return this;
        }

        public Builder requiresArchitecturalChanges(int requiresArchitecturalChanges) {
            this.requiresArchitecturalChanges = requiresArchitecturalChanges;
            return this;
        }

        public Builder alreadyResolved(int alreadyResolved) {
            this.alreadyResolved = alreadyResolved;
            return this;
        }

        public Builder componentTypeCount(Map<SpringComponentType, Integer> componentTypeCount) {
            this.componentTypeCount = componentTypeCount != null ? new HashMap<>(componentTypeCount) : new HashMap<>();
            return this;
        }

        public Builder affectedComponentsByType(Map<SpringComponentType, Integer> affectedComponentsByType) {
            this.affectedComponentsByType = affectedComponentsByType != null ? new HashMap<>(affectedComponentsByType) : new HashMap<>();
            return this;
        }

        public Builder analysisTimeMs(long analysisTimeMs) {
            this.analysisTimeMs = analysisTimeMs;
            return this;
        }

        public Builder maxCycleLength(int maxCycleLength) {
            this.maxCycleLength = maxCycleLength;
            return this;
        }

        public Builder minCycleLength(int minCycleLength) {
            this.minCycleLength = minCycleLength;
            return this;
        }

        public SpringCircularDependencyMetrics build() {
            return new SpringCircularDependencyMetrics(this);
        }
    }
}