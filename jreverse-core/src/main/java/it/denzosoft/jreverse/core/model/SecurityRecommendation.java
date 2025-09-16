package it.denzosoft.jreverse.core.model;

/**
 * Security recommendation model.
 */
public class SecurityRecommendation {
    public enum Priority { LOW, MEDIUM, HIGH, CRITICAL }
    public enum ImplementationComplexity { LOW, MEDIUM, HIGH }

    private final String title;
    private final String description;
    private final Priority priority;
    private final ImplementationComplexity implementationComplexity;

    public SecurityRecommendation(String title, String description,
                                 Priority priority, ImplementationComplexity implementationComplexity) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.implementationComplexity = implementationComplexity;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Priority getPriority() { return priority; }
    public ImplementationComplexity getImplementationComplexity() { return implementationComplexity; }
}