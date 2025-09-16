package it.denzosoft.jreverse.analyzer.circulardependency;

/**
 * Enumeration representing the complexity levels for implementing
 * Spring circular dependency resolution strategies.
 *
 * @author JReverse Development Team
 * @since 1.1.0 (Phase 3 - T3.3.1)
 */
public enum SpringResolutionComplexity {

    /**
     * Low complexity - can be implemented quickly with minimal changes.
     */
    LOW("Low",
        "Quick fix requiring minimal code changes",
        "1-2 hours",
        "Add annotations, change injection methods"),

    /**
     * Medium complexity - requires some planning and moderate changes.
     */
    MEDIUM("Medium",
        "Moderate refactoring requiring careful planning",
        "1-3 days",
        "Interface extraction, event-driven patterns, factory implementations"),

    /**
     * High complexity - requires significant architectural changes.
     */
    HIGH("High",
        "Major refactoring requiring architectural changes",
        "1-2 weeks",
        "Complete module restructuring, significant design changes");

    private final String displayName;
    private final String description;
    private final String timeEstimate;
    private final String typicalChanges;

    SpringResolutionComplexity(String displayName, String description, String timeEstimate, String typicalChanges) {
        this.displayName = displayName;
        this.description = description;
        this.timeEstimate = timeEstimate;
        this.typicalChanges = typicalChanges;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getTimeEstimate() {
        return timeEstimate;
    }

    public String getTypicalChanges() {
        return typicalChanges;
    }

    /**
     * Gets the effort level as a numerical score (1-10).
     */
    public int getEffortScore() {
        switch (this) {
            case LOW:
                return 2;
            case MEDIUM:
                return 5;
            case HIGH:
                return 9;
            default:
                return 5;
        }
    }

    /**
     * Gets the risk level associated with this complexity.
     */
    public String getRiskLevel() {
        switch (this) {
            case LOW:
                return "Minimal risk - localized changes";
            case MEDIUM:
                return "Moderate risk - requires testing";
            case HIGH:
                return "High risk - potential for regression";
            default:
                return "Unknown risk level";
        }
    }

    /**
     * Gets the recommended team size for implementation.
     */
    public String getRecommendedTeamSize() {
        switch (this) {
            case LOW:
                return "1 developer";
            case MEDIUM:
                return "1-2 developers";
            case HIGH:
                return "2-3 developers + architect";
            default:
                return "Variable team size";
        }
    }

    /**
     * Gets the testing requirements for this complexity level.
     */
    public String getTestingRequirements() {
        switch (this) {
            case LOW:
                return "Unit tests for modified classes";
            case MEDIUM:
                return "Unit and integration tests, code review";
            case HIGH:
                return "Comprehensive testing, multiple code reviews, QA validation";
            default:
                return "Standard testing practices";
        }
    }

    /**
     * Checks if this complexity level requires architectural review.
     */
    public boolean requiresArchitecturalReview() {
        return this == HIGH;
    }

    /**
     * Checks if this complexity level can be implemented immediately.
     */
    public boolean canBeImplementedImmediately() {
        return this == LOW;
    }

    /**
     * Gets the approval level required for implementation.
     */
    public String getRequiredApprovalLevel() {
        switch (this) {
            case LOW:
                return "Developer self-approval";
            case MEDIUM:
                return "Team lead approval";
            case HIGH:
                return "Architecture committee approval";
            default:
                return "Standard approval process";
        }
    }

    /**
     * Gets the documentation requirements.
     */
    public String getDocumentationRequirements() {
        switch (this) {
            case LOW:
                return "Code comments and commit messages";
            case MEDIUM:
                return "Implementation notes and decision rationale";
            case HIGH:
                return "Comprehensive architecture documentation and migration guide";
            default:
                return "Standard documentation";
        }
    }

    /**
     * Gets the rollback strategy for this complexity level.
     */
    public String getRollbackStrategy() {
        switch (this) {
            case LOW:
                return "Simple code revert";
            case MEDIUM:
                return "Coordinated rollback with database changes";
            case HIGH:
                return "Phased rollback plan with backward compatibility";
            default:
                return "Standard rollback procedures";
        }
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", displayName, timeEstimate);
    }
}