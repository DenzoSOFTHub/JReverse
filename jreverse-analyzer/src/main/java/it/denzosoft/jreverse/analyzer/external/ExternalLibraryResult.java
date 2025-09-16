package it.denzosoft.jreverse.analyzer.external;

import java.util.*;

/**
 * Result of external library analysis containing information about external dependencies,
 * usage patterns, and version conflicts.
 */
public class ExternalLibraryResult {

    private final Set<ExternalLibraryInfo> externalLibraries;
    private final Map<String, LibraryUsageInfo> usagePatterns;
    private final List<VersionConflict> versionConflicts;
    private final Map<String, Set<String>> dependencyGraph;
    private final ExternalLibraryMetrics metrics;
    private final int totalLibraries;
    private final boolean successful;
    private final String errorMessage;

    private ExternalLibraryResult(Set<ExternalLibraryInfo> externalLibraries,
                                 Map<String, LibraryUsageInfo> usagePatterns,
                                 List<VersionConflict> versionConflicts,
                                 Map<String, Set<String>> dependencyGraph,
                                 ExternalLibraryMetrics metrics,
                                 int totalLibraries,
                                 boolean successful,
                                 String errorMessage) {
        this.externalLibraries = externalLibraries != null ? Set.copyOf(externalLibraries) : Set.of();
        this.usagePatterns = usagePatterns != null ? Map.copyOf(usagePatterns) : Map.of();
        this.versionConflicts = versionConflicts != null ? List.copyOf(versionConflicts) : List.of();
        this.dependencyGraph = dependencyGraph != null ? Map.copyOf(dependencyGraph) : Map.of();
        this.metrics = metrics;
        this.totalLibraries = totalLibraries;
        this.successful = successful;
        this.errorMessage = errorMessage;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static ExternalLibraryResult error(String errorMessage) {
        return new ExternalLibraryResult(null, null, null, null, null, 0, false, errorMessage);
    }

    // Getters
    public Set<ExternalLibraryInfo> getExternalLibraries() { return externalLibraries; }
    public Map<String, LibraryUsageInfo> getUsagePatterns() { return usagePatterns; }
    public List<VersionConflict> getVersionConflicts() { return versionConflicts; }
    public Map<String, Set<String>> getDependencyGraph() { return dependencyGraph; }
    public ExternalLibraryMetrics getMetrics() { return metrics; }
    public int getTotalLibraries() { return totalLibraries; }
    public boolean isSuccessful() { return successful; }
    public String getErrorMessage() { return errorMessage; }

    // Legacy compatibility methods for tests
    public List<ExternalLibrary> getLibraries() {
        return externalLibraries.stream()
            .map(ExternalLibrary::fromExternalLibraryInfo)
            .collect(java.util.stream.Collectors.toList());
    }

    public boolean hasLibraries() {
        return !externalLibraries.isEmpty();
    }

    public static class Builder {
        private Set<ExternalLibraryInfo> externalLibraries = new HashSet<>();
        private Map<String, LibraryUsageInfo> usagePatterns = new HashMap<>();
        private List<VersionConflict> versionConflicts = new ArrayList<>();
        private Map<String, Set<String>> dependencyGraph = new HashMap<>();
        private ExternalLibraryMetrics metrics;
        private int totalLibraries = 0;

        public Builder externalLibraries(Set<ExternalLibraryInfo> externalLibraries) {
            this.externalLibraries = externalLibraries;
            return this;
        }

        public Builder usagePatterns(Map<String, LibraryUsageInfo> usagePatterns) {
            this.usagePatterns = usagePatterns;
            return this;
        }

        public Builder versionConflicts(List<VersionConflict> versionConflicts) {
            this.versionConflicts = versionConflicts;
            return this;
        }

        public Builder dependencyGraph(Map<String, Set<String>> dependencyGraph) {
            this.dependencyGraph = dependencyGraph;
            return this;
        }

        public Builder metrics(ExternalLibraryMetrics metrics) {
            this.metrics = metrics;
            return this;
        }

        public Builder totalLibraries(int totalLibraries) {
            this.totalLibraries = totalLibraries;
            return this;
        }

        public ExternalLibraryResult build() {
            return new ExternalLibraryResult(
                externalLibraries,
                usagePatterns,
                versionConflicts,
                dependencyGraph,
                metrics,
                totalLibraries,
                true,
                null
            );
        }
    }
}