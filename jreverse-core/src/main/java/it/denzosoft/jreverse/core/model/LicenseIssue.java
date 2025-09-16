package it.denzosoft.jreverse.core.model;

import java.util.Objects;

/**
 * Represents a license compliance issue for an external library.
 *
 * @author JReverse Development Team
 * @since 1.1.0 (Phase 3)
 */
public class LicenseIssue {

    private final String libraryName;
    private final String license;
    private final IssueType type;
    private final String description;
    private final IssueSeverity severity;

    public LicenseIssue(String libraryName, String license, IssueType type,
                       String description, IssueSeverity severity) {
        this.libraryName = Objects.requireNonNull(libraryName);
        this.license = license;
        this.type = Objects.requireNonNull(type);
        this.description = description;
        this.severity = Objects.requireNonNull(severity);
    }

    // Getters
    public String getLibraryName() { return libraryName; }
    public String getLicense() { return license; }
    public IssueType getType() { return type; }
    public String getDescription() { return description; }
    public IssueSeverity getSeverity() { return severity; }

    public enum IssueType {
        UNKNOWN_LICENSE,
        RESTRICTIVE_LICENSE,
        INCOMPATIBLE_LICENSE,
        COMMERCIAL_LICENSE
    }

    public enum IssueSeverity {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}