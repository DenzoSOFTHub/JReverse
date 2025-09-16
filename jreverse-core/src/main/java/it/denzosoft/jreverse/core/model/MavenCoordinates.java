package it.denzosoft.jreverse.core.model;

import java.util.Objects;

/**
 * Represents Maven coordinates (groupId, artifactId, version) for external libraries.
 * Immutable value object for library identification.
 *
 * @author JReverse Development Team
 * @since 1.1.0 (Phase 3)
 */
public class MavenCoordinates {

    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String classifier;
    private final String packaging;

    public MavenCoordinates(String groupId, String artifactId, String version) {
        this(groupId, artifactId, version, null, "jar");
    }

    public MavenCoordinates(String groupId, String artifactId, String version,
                           String classifier, String packaging) {
        this.groupId = Objects.requireNonNull(groupId, "groupId cannot be null");
        this.artifactId = Objects.requireNonNull(artifactId, "artifactId cannot be null");
        this.version = Objects.requireNonNull(version, "version cannot be null");
        this.classifier = classifier;
        this.packaging = packaging != null ? packaging : "jar";
    }

    public static MavenCoordinates parse(String coordinateString) {
        if (coordinateString == null || coordinateString.trim().isEmpty()) {
            throw new IllegalArgumentException("Coordinate string cannot be null or empty");
        }

        String[] parts = coordinateString.split(":");
        if (parts.length < 3) {
            throw new IllegalArgumentException("Invalid coordinate format. Expected: groupId:artifactId:version[:classifier[:packaging]]");
        }

        String groupId = parts[0];
        String artifactId = parts[1];
        String version = parts[2];
        String classifier = parts.length > 3 ? parts[3] : null;
        String packaging = parts.length > 4 ? parts[4] : "jar";

        return new MavenCoordinates(groupId, artifactId, version, classifier, packaging);
    }

    public static MavenCoordinates unknown(String libraryName) {
        return new MavenCoordinates("unknown", libraryName, "unknown");
    }

    // Getters
    public String getGroupId() { return groupId; }
    public String getArtifactId() { return artifactId; }
    public String getVersion() { return version; }
    public String getClassifier() { return classifier; }
    public String getPackaging() { return packaging; }

    // Utility methods
    public boolean isUnknown() {
        return "unknown".equals(groupId) || "unknown".equals(version);
    }

    public boolean hasClassifier() {
        return classifier != null && !classifier.trim().isEmpty();
    }

    public boolean isSnapshot() {
        return version.toUpperCase().contains("SNAPSHOT");
    }

    public String getBaseArtifactId() {
        // Remove common suffixes like -spring-boot-starter
        String base = artifactId;
        if (base.endsWith("-spring-boot-starter")) {
            base = base.substring(0, base.length() - "-spring-boot-starter".length());
        } else if (base.endsWith("-starter")) {
            base = base.substring(0, base.length() - "-starter".length());
        }
        return base;
    }

    /**
     * Returns the coordinate string in standard Maven format.
     * Format: groupId:artifactId:version[:classifier[:packaging]]
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(groupId).append(":").append(artifactId).append(":").append(version);

        if (hasClassifier()) {
            sb.append(":").append(classifier);
            if (!"jar".equals(packaging)) {
                sb.append(":").append(packaging);
            }
        } else if (!"jar".equals(packaging)) {
            sb.append("::").append(packaging);
        }

        return sb.toString();
    }

    /**
     * Returns a short display format for UI purposes.
     * Format: artifactId:version
     */
    public String toShortString() {
        return artifactId + ":" + version;
    }

    /**
     * Returns a filename-safe string representation.
     * Format: groupId-artifactId-version[-classifier].packaging
     */
    public String toFileName() {
        StringBuilder sb = new StringBuilder();
        sb.append(groupId.replace(".", "-"))
          .append("-").append(artifactId)
          .append("-").append(version);

        if (hasClassifier()) {
            sb.append("-").append(classifier);
        }

        sb.append(".").append(packaging);
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MavenCoordinates that = (MavenCoordinates) obj;
        return Objects.equals(groupId, that.groupId) &&
               Objects.equals(artifactId, that.artifactId) &&
               Objects.equals(version, that.version) &&
               Objects.equals(classifier, that.classifier) &&
               Objects.equals(packaging, that.packaging);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, artifactId, version, classifier, packaging);
    }
}