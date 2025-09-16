package it.denzosoft.jreverse.core.model;

/**
 * Metrics and statistics for external library analysis.
 *
 * @author JReverse Development Team
 * @since 1.1.0 (Phase 3)
 */
public class LibraryAnalysisMetrics {

    private final int totalLibraries;
    private final int totalClasses;
    private final int frameworkCount;
    private final int utilityCount;
    private final int testLibraryCount;
    private final long totalLibrarySize;
    private final double averageLibrarySize;
    private final int dependencyDepth;

    public LibraryAnalysisMetrics(int totalLibraries, int totalClasses, int frameworkCount,
                                 int utilityCount, int testLibraryCount, long totalLibrarySize,
                                 int dependencyDepth) {
        this.totalLibraries = totalLibraries;
        this.totalClasses = totalClasses;
        this.frameworkCount = frameworkCount;
        this.utilityCount = utilityCount;
        this.testLibraryCount = testLibraryCount;
        this.totalLibrarySize = totalLibrarySize;
        this.averageLibrarySize = totalLibraries > 0 ? (double) totalLibrarySize / totalLibraries : 0.0;
        this.dependencyDepth = dependencyDepth;
    }

    public static LibraryAnalysisMetrics empty() {
        return new LibraryAnalysisMetrics(0, 0, 0, 0, 0, 0L, 0);
    }

    // Getters
    public int getTotalLibraries() { return totalLibraries; }
    public int getTotalClasses() { return totalClasses; }
    public int getFrameworkCount() { return frameworkCount; }
    public int getUtilityCount() { return utilityCount; }
    public int getTestLibraryCount() { return testLibraryCount; }
    public long getTotalLibrarySize() { return totalLibrarySize; }
    public double getAverageLibrarySize() { return averageLibrarySize; }
    public int getDependencyDepth() { return dependencyDepth; }

    public double getClassesPerLibrary() {
        return totalLibraries > 0 ? (double) totalClasses / totalLibraries : 0.0;
    }
}