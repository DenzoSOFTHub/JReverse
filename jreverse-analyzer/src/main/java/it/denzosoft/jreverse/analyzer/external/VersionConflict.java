package it.denzosoft.jreverse.analyzer.external;

import java.util.Set;

/**
 * Information about version conflicts between external libraries.
 */
public class VersionConflict {

    private final String libraryName;
    private final Set<String> conflictingVersions;
    private final String severity;

    private VersionConflict(String libraryName, Set<String> conflictingVersions, String severity) {
        this.libraryName = libraryName;
        this.conflictingVersions = conflictingVersions != null ? Set.copyOf(conflictingVersions) : Set.of();
        this.severity = severity;
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public String getLibraryName() { return libraryName; }
    public Set<String> getConflictingVersions() { return conflictingVersions; }
    public String getSeverity() { return severity; }

    public static class Builder {
        private String libraryName;
        private Set<String> conflictingVersions = Set.of();
        private String severity = "LOW";

        public Builder libraryName(String libraryName) {
            this.libraryName = libraryName;
            return this;
        }

        public Builder conflictingVersions(Set<String> conflictingVersions) {
            this.conflictingVersions = conflictingVersions;
            return this;
        }

        public Builder severity(String severity) {
            this.severity = severity;
            return this;
        }

        public VersionConflict build() {
            return new VersionConflict(libraryName, conflictingVersions, severity);
        }
    }
}