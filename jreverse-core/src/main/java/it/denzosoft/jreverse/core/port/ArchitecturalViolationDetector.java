package it.denzosoft.jreverse.core.port;

import it.denzosoft.jreverse.core.model.ArchitecturalViolationResult;
import it.denzosoft.jreverse.core.model.JarContent;

/**
 * Port interface for comprehensive architectural violation detection.
 * This detector aggregates analysis from multiple architectural analyzers to provide
 * a unified view of architectural quality issues and violations.
 *
 * @author JReverse Development Team
 * @since 1.1.0 (Phase 3)
 */
public interface ArchitecturalViolationDetector {

    /**
     * Performs comprehensive architectural violation detection on the provided JAR content.
     * This includes layer violations, anti-patterns, naming convention issues, and quality assessment.
     *
     * @param jarContent the JAR content to analyze for architectural violations
     * @return comprehensive architectural violation analysis result
     * @throws IllegalArgumentException if jarContent is null
     */
    ArchitecturalViolationResult detectViolations(JarContent jarContent);

    /**
     * Checks if this detector can analyze the given JAR content.
     * Returns true if the JAR contains analyzable architectural patterns.
     *
     * @param jarContent the JAR content to check
     * @return true if this detector can analyze the content, false otherwise
     */
    default boolean canAnalyze(JarContent jarContent) {
        return jarContent != null && !jarContent.getClasses().isEmpty();
    }

    /**
     * Returns the violation detection capabilities supported by this detector.
     *
     * @return array of supported violation detection capabilities
     */
    default ViolationDetectionCapability[] getSupportedCapabilities() {
        return new ViolationDetectionCapability[] {
            ViolationDetectionCapability.LAYER_VIOLATIONS,
            ViolationDetectionCapability.ANTI_PATTERN_DETECTION,
            ViolationDetectionCapability.NAMING_CONVENTION_VIOLATIONS,
            ViolationDetectionCapability.DEPENDENCY_VIOLATIONS,
            ViolationDetectionCapability.PACKAGE_ORGANIZATION_ISSUES,
            ViolationDetectionCapability.ARCHITECTURAL_QUALITY_ASSESSMENT
        };
    }

    /**
     * Enumeration of architectural violation detection capabilities.
     */
    enum ViolationDetectionCapability {
        LAYER_VIOLATIONS("Layer Violations"),
        ANTI_PATTERN_DETECTION("Anti-Pattern Detection"),
        NAMING_CONVENTION_VIOLATIONS("Naming Convention Violations"),
        DEPENDENCY_VIOLATIONS("Dependency Violations"),
        PACKAGE_ORGANIZATION_ISSUES("Package Organization Issues"),
        ARCHITECTURAL_QUALITY_ASSESSMENT("Architectural Quality Assessment");

        private final String displayName;

        ViolationDetectionCapability(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}