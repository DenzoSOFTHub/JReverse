package it.denzosoft.jreverse.core.model;

/**
 * Security risk level enumeration for comprehensive security analysis.
 *
 * @author JReverse Development Team
 * @since 1.1.0 (Phase 3)
 */
public enum SecurityRiskLevel {
    LOW("Low", 1, "Minimal security risks identified"),
    MEDIUM("Medium", 2, "Moderate security risks requiring attention"),
    HIGH("High", 3, "Significant security risks requiring immediate action"),
    CRITICAL("Critical", 4, "Critical security vulnerabilities requiring urgent remediation");

    private final String displayName;
    private final int severity;
    private final String description;

    SecurityRiskLevel(String displayName, int severity, String description) {
        this.displayName = displayName;
        this.severity = severity;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getSeverity() {
        return severity;
    }

    public String getDescription() {
        return description;
    }

    public static SecurityRiskLevel fromScore(double riskScore) {
        if (riskScore >= 80) return CRITICAL;
        if (riskScore >= 60) return HIGH;
        if (riskScore >= 40) return MEDIUM;
        return LOW;
    }
}