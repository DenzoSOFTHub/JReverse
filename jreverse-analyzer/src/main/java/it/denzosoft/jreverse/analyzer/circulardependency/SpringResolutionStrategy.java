package it.denzosoft.jreverse.analyzer.circulardependency;

import java.util.Objects;

/**
 * Represents a Spring-specific resolution strategy for circular dependencies,
 * including implementation details and complexity assessment.
 *
 * @author JReverse Development Team
 * @since 1.1.0 (Phase 3 - T3.3.1)
 */
public class SpringResolutionStrategy {

    private SpringResolutionStrategyType type;
    private String description;
    private SpringResolutionComplexity complexity;
    private String targetClass; // Specific class to apply the strategy to
    private String implementation; // Detailed implementation instructions
    private String impact; // Expected impact of applying this strategy

    // Additional guidance
    private String codeExample;
    private String alternativeApproach;
    private String notes;

    private SpringResolutionStrategy(Builder builder) {
        this.type = builder.type;
        this.description = builder.description;
        this.complexity = builder.complexity;
        this.targetClass = builder.targetClass;
        this.implementation = builder.implementation;
        this.impact = builder.impact;
        this.codeExample = builder.codeExample;
        this.alternativeApproach = builder.alternativeApproach;
        this.notes = builder.notes;
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public SpringResolutionStrategyType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public SpringResolutionComplexity getComplexity() {
        return complexity;
    }

    public String getTargetClass() {
        return targetClass;
    }

    public String getImplementation() {
        return implementation;
    }

    public String getImpact() {
        return impact;
    }

    public String getCodeExample() {
        return codeExample;
    }

    public String getAlternativeApproach() {
        return alternativeApproach;
    }

    public String getNotes() {
        return notes;
    }

    // Utility methods

    /**
     * Gets simple class name for target class.
     */
    public String getTargetSimpleName() {
        if (targetClass == null) return null;
        int lastDot = targetClass.lastIndexOf('.');
        return lastDot != -1 ? targetClass.substring(lastDot + 1) : targetClass;
    }

    /**
     * Checks if this strategy targets a specific class.
     */
    public boolean hasSpecificTarget() {
        return targetClass != null && !targetClass.trim().isEmpty();
    }

    /**
     * Gets the effort level required to implement this strategy.
     */
    public String getEffortLevel() {
        if (complexity == null) return "Unknown";
        switch (complexity) {
            case LOW:
                return "Minimal effort - Quick fix";
            case MEDIUM:
                return "Moderate effort - Requires planning";
            case HIGH:
                return "Significant effort - Major refactoring";
            default:
                return "Unknown effort level";
        }
    }

    /**
     * Gets the time estimate for implementing this strategy.
     */
    public String getTimeEstimate() {
        if (complexity == null) return "Unknown";
        switch (complexity) {
            case LOW:
                return "1-2 hours";
            case MEDIUM:
                return "1-3 days";
            case HIGH:
                return "1-2 weeks";
            default:
                return "Unknown timeframe";
        }
    }

    /**
     * Checks if this strategy is recommended for immediate implementation.
     */
    public boolean isRecommendedForImmediateImplementation() {
        return complexity == SpringResolutionComplexity.LOW ||
               type == SpringResolutionStrategyType.LAZY_INITIALIZATION;
    }

    /**
     * Gets the priority score for this strategy (higher = more urgent).
     */
    public int getPriorityScore() {
        int score = 0;

        // Priority based on strategy type
        if (type != null) {
            score += type.getPriorityScore();
        }

        // Bonus for low complexity (easier to implement)
        if (complexity == SpringResolutionComplexity.LOW) {
            score += 10;
        } else if (complexity == SpringResolutionComplexity.MEDIUM) {
            score += 5;
        }

        return score;
    }

    /**
     * Gets a complete implementation guide.
     */
    public String getCompleteImplementationGuide() {
        StringBuilder guide = new StringBuilder();

        guide.append("Strategy: ").append(description).append("\n\n");

        if (implementation != null) {
            guide.append("Implementation:\n").append(implementation).append("\n\n");
        }

        if (codeExample != null) {
            guide.append("Code Example:\n").append(codeExample).append("\n\n");
        }

        if (impact != null) {
            guide.append("Expected Impact:\n").append(impact).append("\n\n");
        }

        guide.append("Complexity: ").append(complexity != null ? complexity.getDescription() : "Unknown").append("\n");
        guide.append("Time Estimate: ").append(getTimeEstimate()).append("\n");

        if (alternativeApproach != null) {
            guide.append("\nAlternative Approach:\n").append(alternativeApproach).append("\n");
        }

        if (notes != null) {
            guide.append("\nNotes:\n").append(notes).append("\n");
        }

        return guide.toString();
    }

    @Override
    public String toString() {
        return String.format("SpringResolutionStrategy{type=%s, complexity=%s, target=%s}",
            type, complexity, getTargetSimpleName());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SpringResolutionStrategy that = (SpringResolutionStrategy) obj;
        return Objects.equals(type, that.type) &&
               Objects.equals(targetClass, that.targetClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, targetClass);
    }

    // Builder class
    public static class Builder {
        private SpringResolutionStrategyType type;
        private String description;
        private SpringResolutionComplexity complexity;
        private String targetClass;
        private String implementation;
        private String impact;
        private String codeExample;
        private String alternativeApproach;
        private String notes;

        private Builder() {}

        public Builder type(SpringResolutionStrategyType type) {
            this.type = type;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder complexity(SpringResolutionComplexity complexity) {
            this.complexity = complexity;
            return this;
        }

        public Builder targetClass(String targetClass) {
            this.targetClass = targetClass;
            return this;
        }

        public Builder implementation(String implementation) {
            this.implementation = implementation;
            return this;
        }

        public Builder impact(String impact) {
            this.impact = impact;
            return this;
        }

        public Builder codeExample(String codeExample) {
            this.codeExample = codeExample;
            return this;
        }

        public Builder alternativeApproach(String alternativeApproach) {
            this.alternativeApproach = alternativeApproach;
            return this;
        }

        public Builder notes(String notes) {
            this.notes = notes;
            return this;
        }

        public SpringResolutionStrategy build() {
            Objects.requireNonNull(type, "Strategy type is required");
            Objects.requireNonNull(description, "Description is required");
            Objects.requireNonNull(complexity, "Complexity is required");

            return new SpringResolutionStrategy(this);
        }
    }
}