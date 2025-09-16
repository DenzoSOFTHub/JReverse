package it.denzosoft.jreverse.core.model;

/**
 * Severity levels for violations and issues.
 */
public enum ViolationSeverity {
    
    /**
     * Low severity - suggestions for improvement, best practices.
     */
    LOW("Low", 1),
    
    /**
     * Medium severity - issues that should be addressed, potential problems.
     */
    MEDIUM("Medium", 2),
    
    /**
     * High severity - critical issues that must be fixed, potential errors.
     */
    HIGH("High", 3),

    /**
     * Critical severity - system breaking issues, immediate attention required.
     */
    CRITICAL("Critical", 4);
    
    private final String displayName;
    private final int priority;
    
    ViolationSeverity(String displayName, int priority) {
        this.displayName = displayName;
        this.priority = priority;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public int getPriority() {
        return priority;
    }
    
    public boolean isHigherThan(ViolationSeverity other) {
        return this.priority > other.priority;
    }
    
    public boolean isLowerThan(ViolationSeverity other) {
        return this.priority < other.priority;
    }
    
    public static ViolationSeverity fromPriority(int priority) {
        for (ViolationSeverity severity : values()) {
            if (severity.priority == priority) {
                return severity;
            }
        }
        throw new IllegalArgumentException("Invalid priority: " + priority);
    }
}