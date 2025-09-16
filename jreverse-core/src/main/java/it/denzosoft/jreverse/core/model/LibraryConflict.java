package it.denzosoft.jreverse.core.model;

import java.util.List;
import java.util.Objects;

/**
 * Represents a version conflict between external libraries.
 *
 * @author JReverse Development Team
 * @since 1.1.0 (Phase 3)
 */
public class LibraryConflict {

    private final String libraryName;
    private final List<String> conflictingVersions;
    private final ConflictType type;
    private final String description;
    private final ConflictSeverity severity;

    public LibraryConflict(String libraryName, List<String> conflictingVersions,
                          ConflictType type, String description, ConflictSeverity severity) {
        this.libraryName = Objects.requireNonNull(libraryName);
        this.conflictingVersions = List.copyOf(conflictingVersions);
        this.type = Objects.requireNonNull(type);
        this.description = description;
        this.severity = Objects.requireNonNull(severity);
    }

    // Getters
    public String getLibraryName() { return libraryName; }
    public List<String> getConflictingVersions() { return conflictingVersions; }
    public ConflictType getType() { return type; }
    public String getDescription() { return description; }
    public ConflictSeverity getSeverity() { return severity; }

    public enum ConflictType {
        VERSION_MISMATCH,
        DUPLICATE_LIBRARY,
        INCOMPATIBLE_VERSIONS
    }

    public enum ConflictSeverity {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}