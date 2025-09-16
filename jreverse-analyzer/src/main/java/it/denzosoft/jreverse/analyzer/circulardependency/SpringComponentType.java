package it.denzosoft.jreverse.analyzer.circulardependency;

/**
 * Enumeration of Spring component types based on stereotype annotations.
 *
 * @author JReverse Development Team
 * @since 1.1.0 (Phase 3 - T3.3.1)
 */
public enum SpringComponentType {

    /**
     * Generic Spring component (@Component).
     */
    COMPONENT("Component", "Generic Spring-managed component"),

    /**
     * Service layer component (@Service).
     */
    SERVICE("Service", "Business logic service component"),

    /**
     * Data access component (@Repository).
     */
    REPOSITORY("Repository", "Data access layer component"),

    /**
     * Presentation layer component (@Controller).
     */
    CONTROLLER("Controller", "Web MVC controller component"),

    /**
     * REST API component (@RestController).
     */
    REST_CONTROLLER("RestController", "RESTful web service controller"),

    /**
     * Configuration class (@Configuration).
     */
    CONFIGURATION("Configuration", "Spring configuration class");

    private final String displayName;
    private final String description;

    SpringComponentType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Gets the priority for @Lazy target selection.
     * Higher values indicate better candidates for @Lazy annotation.
     */
    public int getLazyTargetPriority() {
        switch (this) {
            case SERVICE:
                return 10; // Services are often good candidates for lazy initialization
            case REPOSITORY:
                return 8;  // Repositories can be lazily initialized
            case COMPONENT:
                return 6;  // Generic components
            case CONFIGURATION:
                return 4;  // Configuration classes should generally not be lazy
            case CONTROLLER:
            case REST_CONTROLLER:
                return 2;  // Controllers should be eagerly initialized for web requests
            default:
                return 5;
        }
    }

    /**
     * Checks if this component type typically participates in circular dependencies.
     */
    public boolean isProneToCircularDependencies() {
        return this == SERVICE || this == REPOSITORY || this == COMPONENT;
    }

    /**
     * Checks if this component type should be analyzed for circular dependencies.
     */
    public boolean shouldAnalyzeForCircularDependencies() {
        // All Spring components should be analyzed
        return true;
    }

    /**
     * Gets the Spring annotation class name for this component type.
     */
    public String getAnnotationClassName() {
        switch (this) {
            case COMPONENT:
                return "org.springframework.stereotype.Component";
            case SERVICE:
                return "org.springframework.stereotype.Service";
            case REPOSITORY:
                return "org.springframework.stereotype.Repository";
            case CONTROLLER:
                return "org.springframework.stereotype.Controller";
            case REST_CONTROLLER:
                return "org.springframework.web.bind.annotation.RestController";
            case CONFIGURATION:
                return "org.springframework.context.annotation.Configuration";
            default:
                return "org.springframework.stereotype.Component";
        }
    }

    /**
     * Checks if this component type is a web component.
     */
    public boolean isWebComponent() {
        return this == CONTROLLER || this == REST_CONTROLLER;
    }

    /**
     * Checks if this component type is a business logic component.
     */
    public boolean isBusinessComponent() {
        return this == SERVICE;
    }

    /**
     * Checks if this component type is a data access component.
     */
    public boolean isDataAccessComponent() {
        return this == REPOSITORY;
    }

    /**
     * Gets a detailed description including typical usage patterns.
     */
    public String getDetailedDescription() {
        switch (this) {
            case COMPONENT:
                return "Generic Spring-managed component. Used for classes that don't fit other stereotypes.";
            case SERVICE:
                return "Business logic service component. Contains business rules and orchestrates operations.";
            case REPOSITORY:
                return "Data access layer component. Encapsulates data access logic and provides a more object-oriented view of the persistence layer.";
            case CONTROLLER:
                return "Web MVC controller component. Handles HTTP requests and returns responses in traditional Spring MVC applications.";
            case REST_CONTROLLER:
                return "RESTful web service controller. Combines @Controller and @ResponseBody for REST API endpoints.";
            case CONFIGURATION:
                return "Spring configuration class. Contains @Bean method definitions and configuration settings.";
            default:
                return description;
        }
    }

    @Override
    public String toString() {
        return displayName;
    }
}