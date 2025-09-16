package it.denzosoft.jreverse.analyzer.circulardependency;

/**
 * Enumeration of Spring circular dependency types based on injection patterns.
 *
 * @author JReverse Development Team
 * @since 1.1.0 (Phase 3 - T3.3.1)
 */
public enum SpringCircularDependencyType {

    /**
     * Circular dependency involving only constructor injection.
     * This is the most problematic type as Spring cannot resolve it without @Lazy.
     */
    CONSTRUCTOR_ONLY("Constructor Only",
        "Circular dependency using only constructor injection",
        "Spring cannot resolve constructor-only circular dependencies without @Lazy annotation"),

    /**
     * Circular dependency involving only field injection.
     * Spring can resolve this automatically but it's not recommended practice.
     */
    FIELD_ONLY("Field Only",
        "Circular dependency using only field injection (@Autowired on fields)",
        "Spring resolves this by creating bean instances first, then injecting fields"),

    /**
     * Circular dependency involving only method injection (setter/other methods).
     * Spring can resolve this similar to field injection.
     */
    METHOD_ONLY("Method Only",
        "Circular dependency using only method injection (@Autowired on methods)",
        "Spring resolves this by creating bean instances first, then calling injection methods"),

    /**
     * Circular dependency involving mixed injection types.
     * Resolution depends on the specific combination and may require @Lazy.
     */
    MIXED("Mixed Injection",
        "Circular dependency using multiple injection types",
        "Resolution complexity depends on the specific combination of injection types");

    private final String displayName;
    private final String description;
    private final String resolutionNote;

    SpringCircularDependencyType(String displayName, String description, String resolutionNote) {
        this.displayName = displayName;
        this.description = description;
        this.resolutionNote = resolutionNote;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getResolutionNote() {
        return resolutionNote;
    }

    /**
     * Checks if this type requires @Lazy annotation for resolution.
     */
    public boolean requiresLazyAnnotation() {
        return this == CONSTRUCTOR_ONLY;
    }

    /**
     * Checks if Spring can automatically resolve this type without @Lazy.
     */
    public boolean canBeAutoResolved() {
        return this == FIELD_ONLY || this == METHOD_ONLY;
    }

    /**
     * Gets the severity level associated with this dependency type.
     */
    public String getSeverityLevel() {
        switch (this) {
            case CONSTRUCTOR_ONLY:
                return "HIGH - Requires @Lazy annotation";
            case FIELD_ONLY:
                return "MEDIUM - Can be auto-resolved but not recommended";
            case METHOD_ONLY:
                return "MEDIUM - Can be auto-resolved";
            case MIXED:
                return "VARIABLE - Depends on injection combination";
            default:
                return "UNKNOWN";
        }
    }

    /**
     * Gets recommended resolution approaches for this type.
     */
    public String getRecommendedResolution() {
        switch (this) {
            case CONSTRUCTOR_ONLY:
                return "Add @Lazy annotation to one of the constructor parameters in the cycle";
            case FIELD_ONLY:
                return "Convert to constructor injection with @Lazy, or use setter injection";
            case METHOD_ONLY:
                return "Consider converting to constructor injection with @Lazy for better practices";
            case MIXED:
                return "Standardize on constructor injection with @Lazy where needed";
            default:
                return "Analyze specific injection pattern and apply appropriate strategy";
        }
    }

    /**
     * Checks if this type indicates a design issue that should be addressed.
     */
    public boolean indicatesDesignIssue() {
        // All circular dependencies indicate some level of design issue
        return true;
    }

    /**
     * Gets the Spring documentation reference for this pattern.
     */
    public String getSpringDocumentationNote() {
        switch (this) {
            case CONSTRUCTOR_ONLY:
                return "See Spring Framework Reference: 'Circular dependencies with constructor injection'";
            case FIELD_ONLY:
                return "See Spring Framework Reference: 'Field injection and circular dependencies'";
            case METHOD_ONLY:
                return "See Spring Framework Reference: 'Setter injection and circular dependencies'";
            case MIXED:
                return "See Spring Framework Reference: 'Dependency injection best practices'";
            default:
                return "See Spring Framework Reference: 'Dependency injection'";
        }
    }

    /**
     * Gets the priority for resolution (lower numbers = higher priority).
     */
    public int getResolutionPriority() {
        switch (this) {
            case CONSTRUCTOR_ONLY:
                return 1; // Highest priority - must be resolved
            case MIXED:
                return 2; // Second priority - complex to understand
            case FIELD_ONLY:
                return 3; // Third priority - bad practice but works
            case METHOD_ONLY:
                return 4; // Lowest priority - generally works fine
            default:
                return 5;
        }
    }

    @Override
    public String toString() {
        return displayName;
    }
}