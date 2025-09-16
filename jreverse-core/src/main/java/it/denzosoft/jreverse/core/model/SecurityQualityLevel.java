package it.denzosoft.jreverse.core.model;

/**
 * Security quality level enumeration for security configuration assessment.
 *
 * @author JReverse Development Team
 * @since 1.1.0 (Phase 3)
 */
public enum SecurityQualityLevel {
    EXCELLENT("Excellent", "A"),
    GOOD("Good", "B"),
    FAIR("Fair", "C"),
    POOR("Poor", "D"),
    CRITICAL("Critical", "F");

    private final String displayName;
    private final String grade;

    SecurityQualityLevel(String displayName, String grade) {
        this.displayName = displayName;
        this.grade = grade;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getGrade() {
        return grade;
    }
}