package it.denzosoft.jreverse.analyzer.circulardependency;

/**
 * Enumeration of risk levels for Spring circular dependencies,
 * representing the potential impact on application behavior.
 *
 * @author JReverse Development Team
 * @since 1.1.0 (Phase 3 - T3.3.1)
 */
public enum SpringCircularDependencyRisk {

    /**
     * Application will fail to start due to circular dependency.
     * This occurs with constructor-only circular dependencies without @Lazy.
     */
    APPLICATION_STARTUP_FAILURE("Application Startup Failure",
        "The Spring application will fail to start due to unresolvable circular dependencies",
        "BeanCurrentlyInCreationException will be thrown during application context initialization",
        10),

    /**
     * Bean creation will fail at runtime when the circular dependency is triggered.
     */
    BEAN_CREATION_EXCEPTION("Bean Creation Exception",
        "Runtime exception when trying to create beans with circular dependencies",
        "Specific bean creation may fail with BeanCreationException when dependency cycle is triggered",
        8),

    /**
     * Architecture becomes overly complex and difficult to understand.
     */
    ARCHITECTURE_COMPLEXITY("Architecture Complexity",
        "Circular dependencies increase architectural complexity and reduce code maintainability",
        "Development and maintenance become more difficult due to tightly coupled components",
        6),

    /**
     * Code becomes harder to maintain and test due to circular dependencies.
     */
    MAINTENANCE_DIFFICULTY("Maintenance Difficulty",
        "Circular dependencies make code harder to maintain, test, and reason about",
        "Unit testing becomes more complex due to dependency cycles, refactoring is more risky",
        4),

    /**
     * Performance impact due to proxy creation and lazy loading overhead.
     */
    PERFORMANCE_IMPACT("Performance Impact",
        "Circular dependencies resolved with @Lazy may impact performance",
        "Proxy creation overhead and delayed initialization may affect application performance",
        3),

    /**
     * Low risk - circular dependency is properly managed with @Lazy or design patterns.
     */
    MANAGED_DEPENDENCY("Managed Dependency",
        "Circular dependency is properly managed and poses minimal risk",
        "Dependencies are resolved using @Lazy or proper architectural patterns",
        1);

    private final String displayName;
    private final String description;
    private final String technicalDetails;
    private final int severityScore; // 1-10 scale

    SpringCircularDependencyRisk(String displayName, String description, String technicalDetails, int severityScore) {
        this.displayName = displayName;
        this.description = description;
        this.technicalDetails = technicalDetails;
        this.severityScore = severityScore;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getTechnicalDetails() {
        return technicalDetails;
    }

    public int getSeverityScore() {
        return severityScore;
    }

    /**
     * Checks if this risk level prevents application startup.
     */
    public boolean preventsStartup() {
        return this == APPLICATION_STARTUP_FAILURE;
    }

    /**
     * Checks if this risk level causes runtime exceptions.
     */
    public boolean causesRuntimeExceptions() {
        return this == APPLICATION_STARTUP_FAILURE || this == BEAN_CREATION_EXCEPTION;
    }

    /**
     * Checks if this risk level requires immediate attention.
     */
    public boolean requiresImmediateAttention() {
        return severityScore >= 8;
    }

    /**
     * Checks if this risk level can be tolerated temporarily.
     */
    public boolean canBeToleratedTemporarily() {
        return severityScore <= 4;
    }

    /**
     * Gets the recommended action for this risk level.
     */
    public String getRecommendedAction() {
        switch (this) {
            case APPLICATION_STARTUP_FAILURE:
                return "CRITICAL: Add @Lazy annotation immediately or refactor architecture";
            case BEAN_CREATION_EXCEPTION:
                return "HIGH PRIORITY: Resolve circular dependency to prevent runtime failures";
            case ARCHITECTURE_COMPLEXITY:
                return "MEDIUM PRIORITY: Plan architectural refactoring to reduce coupling";
            case MAINTENANCE_DIFFICULTY:
                return "LOW PRIORITY: Consider refactoring during next major update";
            case PERFORMANCE_IMPACT:
                return "MONITOR: Evaluate performance impact and optimize if necessary";
            case MANAGED_DEPENDENCY:
                return "ACCEPTABLE: Monitor for architectural changes that might affect resolution";
            default:
                return "REVIEW: Analyze specific circumstances and determine appropriate action";
        }
    }

    /**
     * Gets the urgency level for addressing this risk.
     */
    public String getUrgencyLevel() {
        if (severityScore >= 8) {
            return "CRITICAL";
        } else if (severityScore >= 6) {
            return "HIGH";
        } else if (severityScore >= 4) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }

    /**
     * Gets potential consequences if this risk is not addressed.
     */
    public String getPotentialConsequences() {
        switch (this) {
            case APPLICATION_STARTUP_FAILURE:
                return "Application will not start, production deployment will fail";
            case BEAN_CREATION_EXCEPTION:
                return "Runtime failures, potentially in production, affecting specific features";
            case ARCHITECTURE_COMPLEXITY:
                return "Increased development time, higher defect rate, difficult code evolution";
            case MAINTENANCE_DIFFICULTY:
                return "Slower development velocity, higher risk of introducing bugs during changes";
            case PERFORMANCE_IMPACT:
                return "Degraded application performance, especially during bean initialization";
            case MANAGED_DEPENDENCY:
                return "Minimal consequences if current management approach is maintained";
            default:
                return "Various negative impacts depending on specific implementation";
        }
    }

    /**
     * Gets the color code for UI representation.
     */
    public String getColorCode() {
        switch (this) {
            case APPLICATION_STARTUP_FAILURE:
            case BEAN_CREATION_EXCEPTION:
                return "#FF0000"; // Red
            case ARCHITECTURE_COMPLEXITY:
                return "#FF8800"; // Orange
            case MAINTENANCE_DIFFICULTY:
                return "#FFAA00"; // Yellow-Orange
            case PERFORMANCE_IMPACT:
                return "#FFDD00"; // Yellow
            case MANAGED_DEPENDENCY:
                return "#00AA00"; // Green
            default:
                return "#888888"; // Gray
        }
    }

    @Override
    public String toString() {
        return String.format("%s (Severity: %d/10)", displayName, severityScore);
    }
}