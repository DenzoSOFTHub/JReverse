package it.denzosoft.jreverse.analyzer.external;

import java.util.Set;

/**
 * Information about how an external library is used in the application.
 */
public class LibraryUsageInfo {

    private final String libraryName;
    private final int usageCount;
    private final double usageIntensity;
    private final Set<String> mainUsagePatterns;

    private LibraryUsageInfo(String libraryName, int usageCount, double usageIntensity, Set<String> mainUsagePatterns) {
        this.libraryName = libraryName;
        this.usageCount = usageCount;
        this.usageIntensity = usageIntensity;
        this.mainUsagePatterns = mainUsagePatterns != null ? Set.copyOf(mainUsagePatterns) : Set.of();
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public String getLibraryName() { return libraryName; }
    public int getUsageCount() { return usageCount; }
    public double getUsageIntensity() { return usageIntensity; }
    public Set<String> getMainUsagePatterns() { return mainUsagePatterns; }

    public static class Builder {
        private String libraryName;
        private int usageCount = 0;
        private double usageIntensity = 0.0;
        private Set<String> mainUsagePatterns = Set.of();

        public Builder libraryName(String libraryName) {
            this.libraryName = libraryName;
            return this;
        }

        public Builder usageCount(int usageCount) {
            this.usageCount = usageCount;
            return this;
        }

        public Builder usageIntensity(double usageIntensity) {
            this.usageIntensity = usageIntensity;
            return this;
        }

        public Builder mainUsagePatterns(Set<String> mainUsagePatterns) {
            this.mainUsagePatterns = mainUsagePatterns;
            return this;
        }

        public LibraryUsageInfo build() {
            return new LibraryUsageInfo(libraryName, usageCount, usageIntensity, mainUsagePatterns);
        }
    }
}