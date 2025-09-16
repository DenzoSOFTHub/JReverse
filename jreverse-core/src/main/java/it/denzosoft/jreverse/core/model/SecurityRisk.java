package it.denzosoft.jreverse.core.model;

/**
 * Enumeration of security risk levels for external libraries.
 * Used to classify libraries based on known vulnerabilities and security assessment.
 *
 * @author JReverse Development Team
 * @since 1.1.0 (Phase 3)
 */
public enum SecurityRisk {

    /**
     * Critical security risk - immediate attention required.
     * Known high-severity vulnerabilities or security issues.
     */
    CRITICAL("Critical", 100, "Immediate attention required"),

    /**
     * High security risk - should be addressed promptly.
     * Known medium-severity vulnerabilities or outdated with security implications.
     */
    HIGH("High", 75, "Should be addressed promptly"),

    /**
     * Medium security risk - should be monitored.
     * Minor vulnerabilities or potential security concerns.
     */
    MEDIUM("Medium", 50, "Should be monitored"),

    /**
     * Low security risk - minimal concerns.
     * No known vulnerabilities, up-to-date library.
     */
    LOW("Low", 25, "Minimal security concerns"),

    /**
     * Unknown security risk - assessment unavailable.
     * Unable to determine security status.
     */
    UNKNOWN("Unknown", 0, "Security assessment unavailable");

    private final String displayName;
    private final int score;
    private final String description;

    SecurityRisk(String displayName, int score, String description) {
        this.displayName = displayName;
        this.score = score;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getScore() {
        return score;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Checks if this risk level requires immediate attention.
     */
    public boolean requiresImmediateAction() {
        return this == CRITICAL || this == HIGH;
    }

    /**
     * Checks if this risk level should be monitored.
     */
    public boolean shouldBeMonitored() {
        return this != LOW && this != UNKNOWN;
    }

    /**
     * Gets the CSS class name for UI styling.
     */
    public String getCssClass() {
        switch (this) {
            case CRITICAL: return "risk-critical";
            case HIGH: return "risk-high";
            case MEDIUM: return "risk-medium";
            case LOW: return "risk-low";
            default: return "risk-unknown";
        }
    }

    /**
     * Determines the security risk based on vulnerability count and severity.
     */
    public static SecurityRisk fromVulnerabilityCount(int vulnerabilityCount, boolean hasCriticalVulns) {
        if (hasCriticalVulns) {
            return CRITICAL;
        }

        if (vulnerabilityCount >= 5) {
            return HIGH;
        } else if (vulnerabilityCount >= 2) {
            return MEDIUM;
        } else if (vulnerabilityCount == 1) {
            return LOW;
        } else {
            return LOW;
        }
    }

    /**
     * Combines multiple risk levels to determine overall risk.
     */
    public static SecurityRisk combineRisks(SecurityRisk... risks) {
        SecurityRisk highest = LOW;

        for (SecurityRisk risk : risks) {
            if (risk.score > highest.score) {
                highest = risk;
            }
        }

        return highest;
    }
}