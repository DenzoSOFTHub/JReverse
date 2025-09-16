package it.denzosoft.jreverse.analyzer.circulardependency;

/**
 * Enumeration of Spring-specific resolution strategy types for circular dependencies.
 *
 * @author JReverse Development Team
 * @since 1.1.0 (Phase 3 - T3.3.1)
 */
public enum SpringResolutionStrategyType {

    /**
     * Use @Lazy annotation to delay bean initialization.
     */
    LAZY_INITIALIZATION("@Lazy Initialization",
        "Add @Lazy annotation to break circular dependency by delaying bean initialization",
        "Quick and effective solution for most circular dependencies"),

    /**
     * Convert constructor injection to setter injection.
     */
    SETTER_INJECTION("Setter Injection",
        "Convert constructor injection to setter injection to allow Spring to resolve the cycle",
        "Allows Spring to create bean instances first, then inject dependencies"),

    /**
     * Introduce interfaces to eliminate direct class dependencies.
     */
    INTERFACE_SEGREGATION("Interface Segregation",
        "Extract interfaces to reduce direct coupling between classes",
        "Improves testability and reduces coupling, follows SOLID principles"),

    /**
     * Use ApplicationEventPublisher for loose coupling.
     */
    EVENT_DRIVEN("Event-Driven Communication",
        "Replace direct method calls with Spring application events",
        "Completely decouples components using publish-subscribe pattern"),

    /**
     * Extract common functionality to eliminate circular dependencies.
     */
    ARCHITECTURAL_REFACTORING("Architectural Refactoring",
        "Refactor architecture to eliminate the need for circular dependencies",
        "Long-term solution that improves overall architecture"),

    /**
     * Use factory pattern to control bean creation.
     */
    FACTORY_PATTERN("Factory Pattern",
        "Use factory beans or factory methods to control dependency creation",
        "Provides fine-grained control over bean creation and initialization"),

    /**
     * Use Spring's lookup method injection.
     */
    LOOKUP_METHOD("Lookup Method Injection",
        "Use @Lookup annotation for method injection to resolve circular dependencies",
        "Spring creates a subclass and overrides the lookup method at runtime"),

    /**
     * Configure beans in different profiles to avoid circular dependencies.
     */
    PROFILE_SEPARATION("Profile Separation",
        "Separate beans into different Spring profiles to avoid circular dependencies",
        "Useful when different configurations require different dependency patterns");

    private final String displayName;
    private final String description;
    private final String benefits;

    SpringResolutionStrategyType(String displayName, String description, String benefits) {
        this.displayName = displayName;
        this.description = description;
        this.benefits = benefits;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getBenefits() {
        return benefits;
    }

    /**
     * Gets the typical complexity for implementing this strategy.
     */
    public SpringResolutionComplexity getTypicalComplexity() {
        switch (this) {
            case LAZY_INITIALIZATION:
            case SETTER_INJECTION:
                return SpringResolutionComplexity.LOW;
            case INTERFACE_SEGREGATION:
            case EVENT_DRIVEN:
            case FACTORY_PATTERN:
            case LOOKUP_METHOD:
            case PROFILE_SEPARATION:
                return SpringResolutionComplexity.MEDIUM;
            case ARCHITECTURAL_REFACTORING:
                return SpringResolutionComplexity.HIGH;
            default:
                return SpringResolutionComplexity.MEDIUM;
        }
    }

    /**
     * Gets the priority score for this strategy (higher = more important/effective).
     */
    public int getPriorityScore() {
        switch (this) {
            case LAZY_INITIALIZATION:
                return 20; // Most common and effective
            case SETTER_INJECTION:
                return 15; // Quick fix but not always best practice
            case INTERFACE_SEGREGATION:
                return 18; // Good architectural practice
            case EVENT_DRIVEN:
                return 16; // Excellent for decoupling
            case ARCHITECTURAL_REFACTORING:
                return 12; // Best long-term but high effort
            case FACTORY_PATTERN:
                return 14; // Good for complex scenarios
            case LOOKUP_METHOD:
                return 10; // Specific use cases
            case PROFILE_SEPARATION:
                return 8;  // Limited applicability
            default:
                return 10;
        }
    }

    /**
     * Checks if this strategy is a quick fix.
     */
    public boolean isQuickFix() {
        return this == LAZY_INITIALIZATION || this == SETTER_INJECTION;
    }

    /**
     * Checks if this strategy improves architecture.
     */
    public boolean improvesArchitecture() {
        return this == INTERFACE_SEGREGATION ||
               this == EVENT_DRIVEN ||
               this == ARCHITECTURAL_REFACTORING;
    }

    /**
     * Checks if this strategy requires significant code changes.
     */
    public boolean requiresSignificantChanges() {
        return this == ARCHITECTURAL_REFACTORING ||
               this == INTERFACE_SEGREGATION ||
               this == EVENT_DRIVEN;
    }

    /**
     * Gets the Spring Framework feature this strategy relies on.
     */
    public String getSpringFeature() {
        switch (this) {
            case LAZY_INITIALIZATION:
                return "@Lazy annotation (Spring 3.0+)";
            case SETTER_INJECTION:
                return "Setter-based dependency injection";
            case INTERFACE_SEGREGATION:
                return "Interface-based dependency injection";
            case EVENT_DRIVEN:
                return "ApplicationEventPublisher (Spring 1.1+)";
            case ARCHITECTURAL_REFACTORING:
                return "General Spring IoC features";
            case FACTORY_PATTERN:
                return "FactoryBean interface or @Bean methods";
            case LOOKUP_METHOD:
                return "@Lookup annotation (Spring 4.1+)";
            case PROFILE_SEPARATION:
                return "@Profile annotation (Spring 3.1+)";
            default:
                return "Core Spring IoC";
        }
    }

    /**
     * Gets recommended use cases for this strategy.
     */
    public String getRecommendedUseCases() {
        switch (this) {
            case LAZY_INITIALIZATION:
                return "Constructor injection circular dependencies, non-critical beans";
            case SETTER_INJECTION:
                return "Simple circular dependencies, legacy code compatibility";
            case INTERFACE_SEGREGATION:
                return "Tightly coupled services, improving testability";
            case EVENT_DRIVEN:
                return "Cross-module communication, audit logging, notifications";
            case ARCHITECTURAL_REFACTORING:
                return "Complex circular dependencies, major version upgrades";
            case FACTORY_PATTERN:
                return "Complex object creation, conditional bean creation";
            case LOOKUP_METHOD:
                return "Singleton beans needing prototype dependencies";
            case PROFILE_SEPARATION:
                return "Environment-specific configurations, optional features";
            default:
                return "General circular dependency scenarios";
        }
    }

    /**
     * Gets potential drawbacks of this strategy.
     */
    public String getPotentialDrawbacks() {
        switch (this) {
            case LAZY_INITIALIZATION:
                return "Delayed initialization may hide startup issues, proxy overhead";
            case SETTER_INJECTION:
                return "Optional dependencies, harder to enforce required dependencies";
            case INTERFACE_SEGREGATION:
                return "Increased number of interfaces, potential over-abstraction";
            case EVENT_DRIVEN:
                return "Asynchronous behavior complexity, harder debugging";
            case ARCHITECTURAL_REFACTORING:
                return "High development cost, risk of introducing bugs";
            case FACTORY_PATTERN:
                return "Increased complexity, additional boilerplate code";
            case LOOKUP_METHOD:
                return "Runtime proxy creation, limited to specific scenarios";
            case PROFILE_SEPARATION:
                return "Configuration complexity, potential runtime surprises";
            default:
                return "Strategy-specific limitations";
        }
    }

    @Override
    public String toString() {
        return displayName;
    }
}