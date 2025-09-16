package it.denzosoft.jreverse.core.model;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Semantic version implementation following semver.org specification.
 * Supports version parsing and comparison for library version analysis.
 *
 * @author JReverse Development Team
 * @since 1.1.0 (Phase 3)
 */
public class SemanticVersion implements Comparable<SemanticVersion> {

    private static final Pattern SEMANTIC_VERSION_PATTERN = Pattern.compile(
        "^(\\d+)\\.(\\d+)\\.(\\d+)(?:[-.]([a-zA-Z0-9.-]+))?$"
    );

    private final int major;
    private final int minor;
    private final int patch;
    private final String qualifier;
    private final boolean valid;

    public SemanticVersion(int major, int minor, int patch) {
        this(major, minor, patch, null);
    }

    public SemanticVersion(int major, int minor, int patch, String qualifier) {
        this.major = Math.max(0, major);
        this.minor = Math.max(0, minor);
        this.patch = Math.max(0, patch);
        this.qualifier = qualifier;
        this.valid = true;
    }

    private SemanticVersion() {
        this.major = 0;
        this.minor = 0;
        this.patch = 0;
        this.qualifier = null;
        this.valid = false;
    }

    public static SemanticVersion parse(String version) {
        if (version == null || version.trim().isEmpty()) {
            return unknown();
        }

        String normalized = normalizeVersion(version.trim());
        Matcher matcher = SEMANTIC_VERSION_PATTERN.matcher(normalized);

        if (matcher.matches()) {
            try {
                int major = Integer.parseInt(matcher.group(1));
                int minor = Integer.parseInt(matcher.group(2));
                int patch = Integer.parseInt(matcher.group(3));
                String qualifier = matcher.group(4);

                return new SemanticVersion(major, minor, patch, qualifier);
            } catch (NumberFormatException e) {
                return unknown();
            }
        }

        // Try to extract at least major.minor from non-standard formats
        return parseNonStandard(normalized);
    }

    private static String normalizeVersion(String version) {
        // Handle common version patterns
        version = version.replaceAll("^v", ""); // Remove leading 'v'
        version = version.replaceAll("\\.RELEASE$", ""); // Remove .RELEASE suffix
        version = version.replaceAll("\\.Final$", ""); // Remove .Final suffix
        version = version.replaceAll("_", "."); // Replace underscores with dots

        return version;
    }

    private static SemanticVersion parseNonStandard(String version) {
        // Try to extract major.minor.patch from various formats
        Pattern relaxedPattern = Pattern.compile("(\\d+)(?:\\.(\\d+))?(?:\\.(\\d+))?");
        Matcher matcher = relaxedPattern.matcher(version);

        if (matcher.find()) {
            try {
                int major = Integer.parseInt(matcher.group(1));
                int minor = matcher.group(2) != null ? Integer.parseInt(matcher.group(2)) : 0;
                int patch = matcher.group(3) != null ? Integer.parseInt(matcher.group(3)) : 0;

                // Extract remaining part as qualifier
                String remainder = version.substring(matcher.end());
                String qualifier = remainder.isEmpty() ? null : remainder.replaceAll("^[-.]", "");

                return new SemanticVersion(major, minor, patch, qualifier);
            } catch (NumberFormatException e) {
                return unknown();
            }
        }

        return unknown();
    }

    public static SemanticVersion unknown() {
        return new SemanticVersion();
    }

    // Getters
    public int getMajor() { return major; }
    public int getMinor() { return minor; }
    public int getPatch() { return patch; }
    public String getQualifier() { return qualifier; }
    public boolean isValid() { return valid; }

    // Utility methods
    public boolean hasQualifier() {
        return qualifier != null && !qualifier.trim().isEmpty();
    }

    public boolean isPreRelease() {
        if (!hasQualifier()) return false;
        String lowerQualifier = qualifier.toLowerCase();
        return lowerQualifier.contains("alpha") ||
               lowerQualifier.contains("beta") ||
               lowerQualifier.contains("rc") ||
               lowerQualifier.contains("milestone") ||
               lowerQualifier.contains("snapshot");
    }

    public boolean isSnapshot() {
        return hasQualifier() && qualifier.toUpperCase().contains("SNAPSHOT");
    }

    public boolean isStable() {
        return valid && !isPreRelease() && !isSnapshot();
    }

    /**
     * Checks if this version is compatible with another version (same major version).
     */
    public boolean isCompatibleWith(SemanticVersion other) {
        if (!this.valid || !other.valid) return false;
        return this.major == other.major;
    }

    /**
     * Checks if this version has breaking changes compared to another version.
     */
    public boolean hasBreakingChangesFrom(SemanticVersion other) {
        if (!this.valid || !other.valid) return false;
        return this.major > other.major;
    }

    @Override
    public int compareTo(SemanticVersion other) {
        if (!this.valid && !other.valid) return 0;
        if (!this.valid) return -1;
        if (!other.valid) return 1;

        // Compare major, minor, patch
        int result = Integer.compare(this.major, other.major);
        if (result != 0) return result;

        result = Integer.compare(this.minor, other.minor);
        if (result != 0) return result;

        result = Integer.compare(this.patch, other.patch);
        if (result != 0) return result;

        // Handle qualifiers
        if (this.qualifier == null && other.qualifier == null) return 0;
        if (this.qualifier == null) return 1; // Release version is greater than pre-release
        if (other.qualifier == null) return -1;

        // Compare qualifiers lexicographically
        return this.qualifier.compareTo(other.qualifier);
    }

    @Override
    public String toString() {
        if (!valid) return "invalid";

        StringBuilder sb = new StringBuilder();
        sb.append(major).append(".").append(minor).append(".").append(patch);

        if (hasQualifier()) {
            sb.append("-").append(qualifier);
        }

        return sb.toString();
    }

    /**
     * Returns a short string representation (major.minor).
     */
    public String toShortString() {
        if (!valid) return "invalid";
        return major + "." + minor;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SemanticVersion that = (SemanticVersion) obj;
        return major == that.major &&
               minor == that.minor &&
               patch == that.patch &&
               valid == that.valid &&
               Objects.equals(qualifier, that.qualifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor, patch, qualifier, valid);
    }
}