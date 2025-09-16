package it.denzosoft.jreverse.analyzer.external;

/**
 * Metrics about external library usage in the application.
 */
public class ExternalLibraryMetrics {

    private final int totalLibraries;
    private final int totalExternalClasses;
    private final double averageUsageIntensity;
    private final String mostUsedLibrary;

    private ExternalLibraryMetrics(int totalLibraries, int totalExternalClasses,
                                  double averageUsageIntensity, String mostUsedLibrary) {
        this.totalLibraries = totalLibraries;
        this.totalExternalClasses = totalExternalClasses;
        this.averageUsageIntensity = averageUsageIntensity;
        this.mostUsedLibrary = mostUsedLibrary;
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public int getTotalLibraries() { return totalLibraries; }
    public int getTotalExternalClasses() { return totalExternalClasses; }
    public double getAverageUsageIntensity() { return averageUsageIntensity; }
    public String getMostUsedLibrary() { return mostUsedLibrary; }

    public static class Builder {
        private int totalLibraries = 0;
        private int totalExternalClasses = 0;
        private double averageUsageIntensity = 0.0;
        private String mostUsedLibrary = "None";

        public Builder totalLibraries(int totalLibraries) {
            this.totalLibraries = totalLibraries;
            return this;
        }

        public Builder totalExternalClasses(int totalExternalClasses) {
            this.totalExternalClasses = totalExternalClasses;
            return this;
        }

        public Builder averageUsageIntensity(double averageUsageIntensity) {
            this.averageUsageIntensity = averageUsageIntensity;
            return this;
        }

        public Builder mostUsedLibrary(String mostUsedLibrary) {
            this.mostUsedLibrary = mostUsedLibrary;
            return this;
        }

        public ExternalLibraryMetrics build() {
            return new ExternalLibraryMetrics(totalLibraries, totalExternalClasses,
                                            averageUsageIntensity, mostUsedLibrary);
        }
    }
}