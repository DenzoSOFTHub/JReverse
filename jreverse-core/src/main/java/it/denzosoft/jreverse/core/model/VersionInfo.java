package it.denzosoft.jreverse.core.model;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Comprehensive version information for external libraries including
 * semantic version parsing and comparison capabilities.
 *
 * @author JReverse Development Team
 * @since 1.1.0 (Phase 3)
 */
public class VersionInfo {

    private final String rawVersion;
    private final SemanticVersion semanticVersion;
    private final boolean isSnapshot;
    private final boolean isPreRelease;
    private final boolean isOutdated;
    private final String latestVersion;
    private final VersionSource source;

    private VersionInfo(Builder builder) {
        this.rawVersion = Objects.requireNonNull(builder.rawVersion, "rawVersion cannot be null");
        this.semanticVersion = builder.semanticVersion;
        this.isSnapshot = builder.isSnapshot;
        this.isPreRelease = builder.isPreRelease;
        this.isOutdated = builder.isOutdated;
        this.latestVersion = builder.latestVersion;
        this.source = builder.source;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static VersionInfo parse(String version) {
        return builder()
            .rawVersion(version)
            .semanticVersion(SemanticVersion.parse(version))
            .isSnapshot(version.toUpperCase().contains("SNAPSHOT"))
            .isPreRelease(isPreReleaseVersion(version))
            .source(VersionSource.MANIFEST)
            .build();
    }

    public static VersionInfo unknown() {
        return builder()
            .rawVersion("unknown")
            .semanticVersion(SemanticVersion.unknown())
            .source(VersionSource.UNKNOWN)
            .build();
    }

    // Getters
    public String getRawVersion() { return rawVersion; }
    public String getVersion() { return rawVersion; } // Compatibility alias
    public SemanticVersion getSemanticVersion() { return semanticVersion; }
    public boolean isSnapshot() { return isSnapshot; }
    public boolean isPreRelease() { return isPreRelease; }
    public boolean isOutdated() { return isOutdated; }
    public String getLatestVersion() { return latestVersion; }
    public VersionSource getSource() { return source; }

    // Utility methods
    public boolean isValid() {
        return !"unknown".equals(rawVersion) && semanticVersion != null;
    }

    public boolean isStable() {
        return !isSnapshot && !isPreRelease && isValid();
    }

    public int compareVersionTo(VersionInfo other) {
        if (this.semanticVersion == null || other.semanticVersion == null) {
            return this.rawVersion.compareTo(other.rawVersion);
        }
        return this.semanticVersion.compareTo(other.semanticVersion);
    }

    public boolean isNewerThan(VersionInfo other) {
        return compareVersionTo(other) > 0;
    }

    public boolean isOlderThan(VersionInfo other) {
        return compareVersionTo(other) < 0;
    }

    private static boolean isPreReleaseVersion(String version) {
        String lowerVersion = version.toLowerCase();
        return lowerVersion.contains("alpha") ||
               lowerVersion.contains("beta") ||
               lowerVersion.contains("rc") ||
               lowerVersion.contains("milestone") ||
               lowerVersion.contains("m1") ||
               lowerVersion.contains("m2") ||
               lowerVersion.contains("preview");
    }

    @Override
    public String toString() {
        return rawVersion;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        VersionInfo that = (VersionInfo) obj;
        return Objects.equals(rawVersion, that.rawVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rawVersion);
    }

    public static class Builder {
        private String rawVersion;
        private SemanticVersion semanticVersion;
        private boolean isSnapshot = false;
        private boolean isPreRelease = false;
        private boolean isOutdated = false;
        private String latestVersion;
        private VersionSource source = VersionSource.UNKNOWN;

        public Builder rawVersion(String rawVersion) {
            this.rawVersion = rawVersion;
            return this;
        }

        public Builder semanticVersion(SemanticVersion semanticVersion) {
            this.semanticVersion = semanticVersion;
            return this;
        }

        public Builder isSnapshot(boolean isSnapshot) {
            this.isSnapshot = isSnapshot;
            return this;
        }

        public Builder isPreRelease(boolean isPreRelease) {
            this.isPreRelease = isPreRelease;
            return this;
        }

        public Builder isOutdated(boolean isOutdated) {
            this.isOutdated = isOutdated;
            return this;
        }

        public Builder latestVersion(String latestVersion) {
            this.latestVersion = latestVersion;
            return this;
        }

        public Builder source(VersionSource source) {
            this.source = source != null ? source : VersionSource.UNKNOWN;
            return this;
        }

        public VersionInfo build() {
            return new VersionInfo(this);
        }
    }

    /**
     * Enumeration of version information sources.
     */
    public enum VersionSource {
        MANIFEST("JAR Manifest"),
        CLASS_NAME("Class Name Pattern"),
        CONSTANT_FIELD("Version Constant"),
        PACKAGE_NAME("Package Name"),
        INFERRED("Heuristic Detection"),
        UNKNOWN("Unknown Source");

        private final String description;

        VersionSource(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}