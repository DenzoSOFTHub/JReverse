package it.denzosoft.jreverse.core.model;

/**
 * Enumeration of different types of method calls in a call graph.
 */
public enum CallType {
    
    /**
     * Controller method handling HTTP requests.
     */
    CONTROLLER("Controller", "HTTP request handler", "#FF6B6B"),
    
    /**
     * Service layer business logic call.
     */
    SERVICE_CALL("Service", "Business logic service", "#4ECDC4"),
    
    /**
     * Repository or database access call.
     */
    DATABASE_ACCESS("Database", "Database query/operation", "#45B7D1"),
    
    /**
     * JPA Repository method call.
     */
    REPOSITORY_CALL("Repository", "JPA Repository operation", "#96CEB4"),
    
    /**
     * External HTTP service call (RestTemplate, WebClient, etc.).
     */
    EXTERNAL_HTTP_CALL("External HTTP", "External service call", "#FECA57"),
    
    /**
     * Message publishing (JMS, RabbitMQ, Kafka, etc.).
     */
    MESSAGE_PUBLISH("Message", "Message publishing", "#FF9FF3"),
    
    /**
     * Caching operation (Redis, EhCache, etc.).
     */
    CACHE_OPERATION("Cache", "Caching operation", "#54A0FF"),
    
    /**
     * Transactional method boundary.
     */
    TRANSACTIONAL_CALL("Transaction", "Transactional boundary", "#5F27CD"),
    
    /**
     * Configuration or setup related call.
     */
    CONFIGURATION("Config", "Configuration operation", "#GRAY"),
    
    /**
     * Security related call (authentication, authorization).
     */
    SECURITY_CALL("Security", "Security operation", "#FF6348"),
    
    /**
     * Validation or constraint checking.
     */
    VALIDATION("Validation", "Data validation", "#2ED573"),
    
    /**
     * Mapping or transformation operation.
     */
    MAPPING("Mapping", "Object mapping/transformation", "#FFA502"),
    
    /**
     * Generic business logic call.
     */
    BUSINESS_LOGIC("Business", "Business logic", "#3742FA"),
    
    /**
     * Utility or helper method call.
     */
    UTILITY("Utility", "Utility/helper method", "#A4B0BE"),
    
    /**
     * Framework or library call.
     */
    FRAMEWORK_CALL("Framework", "Framework/library call", "#CED6E0"),
    
    /**
     * Unknown or unclassified call type.
     */
    UNKNOWN("Unknown", "Unclassified call", "#DDD"),
    
    /**
     * REST endpoint controller method.
     */
    REST_ENDPOINT("REST Endpoint", "REST endpoint handler", "#FF6B6B"),
    
    /**
     * Database call operation.
     */
    DATABASE_CALL("Database", "Database access", "#45B7D1"),
    
    /**
     * Internal method call within the application.
     */
    INTERNAL_CALL("Internal", "Internal method call", "#A4B0BE"),
    
    /**
     * External library or service call.
     */
    EXTERNAL_CALL("External", "External library call", "#CED6E0");
    
    private final String displayName;
    private final String description;
    private final String color; // For visual representation
    
    CallType(String displayName, String description, String color) {
        this.displayName = displayName;
        this.description = description;
        this.color = color;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getColor() {
        return color;
    }
    
    /**
     * Checks if this call type represents a database operation.
     */
    public boolean isDatabaseOperation() {
        return this == DATABASE_ACCESS || this == REPOSITORY_CALL;
    }
    
    /**
     * Checks if this call type represents an external operation.
     */
    public boolean isExternalOperation() {
        return this == EXTERNAL_HTTP_CALL || this == MESSAGE_PUBLISH;
    }
    
    /**
     * Checks if this call type represents a business layer operation.
     */
    public boolean isBusinessLayer() {
        return this == SERVICE_CALL || this == BUSINESS_LOGIC;
    }
    
    /**
     * Checks if this call type represents a cross-cutting concern.
     */
    public boolean isCrossCuttingConcern() {
        return this == CACHE_OPERATION || this == TRANSACTIONAL_CALL || 
               this == SECURITY_CALL || this == VALIDATION;
    }
    
    /**
     * Gets the layer priority for architectural analysis (lower = higher in stack).
     */
    public int getLayerPriority() {
        switch (this) {
            case CONTROLLER: return 1;
            case SERVICE_CALL:
            case BUSINESS_LOGIC: return 2;
            case REPOSITORY_CALL:
            case DATABASE_ACCESS: return 3;
            case EXTERNAL_HTTP_CALL: return 4;
            case CACHE_OPERATION:
            case TRANSACTIONAL_CALL:
            case SECURITY_CALL:
            case VALIDATION: return 0; // Cross-cutting concerns
            default: return 5;
        }
    }
    
    /**
     * Determines if calling from this type to the target type violates layered architecture.
     */
    public boolean isLayerViolation(CallType targetType) {
        // Controllers should not directly call repositories
        if (this == CONTROLLER && targetType.isDatabaseOperation()) {
            return true;
        }
        
        // Services should not call controllers
        if (this.isBusinessLayer() && targetType == CONTROLLER) {
            return true;
        }
        
        // Generally, higher layers should not be called by lower layers
        int currentPriority = this.getLayerPriority();
        int targetPriority = targetType.getLayerPriority();
        
        // Skip cross-cutting concerns in this check
        if (currentPriority == 0 || targetPriority == 0) {
            return false;
        }
        
        return targetPriority < currentPriority;
    }
}