package it.denzosoft.jreverse.core.model;

/**
 * Enumeration of architectural layer types in a layered architecture.
 */
public enum LayerType {
    
    /**
     * Presentation layer - handles user interface and user interaction.
     */
    PRESENTATION("Presentation", "Handles user interface and user interaction", 1),
    
    /**
     * Controller layer - manages HTTP requests and responses.
     */
    CONTROLLER("Controller", "Manages HTTP requests and responses", 2),
    
    /**
     * Service layer - contains business logic and application services.
     */
    SERVICE("Service", "Contains business logic and application services", 3),
    
    /**
     * Business layer - core business logic and domain rules.
     */
    BUSINESS("Business", "Core business logic and domain rules", 4),
    
    /**
     * Domain layer - domain entities and domain services.
     */
    DOMAIN("Domain", "Domain entities and domain services", 5),
    
    /**
     * Repository layer - data access abstraction.
     */
    REPOSITORY("Repository", "Data access abstraction", 6),
    
    /**
     * Data Access layer - direct database and external service access.
     */
    DATA_ACCESS("Data Access", "Direct database and external service access", 7),
    
    /**
     * Persistence layer - data persistence mechanisms.
     */
    PERSISTENCE("Persistence", "Data persistence mechanisms", 8),
    
    /**
     * Infrastructure layer - technical concerns and external dependencies.
     */
    INFRASTRUCTURE("Infrastructure", "Technical concerns and external dependencies", 9),
    
    /**
     * Utility layer - helper classes and common utilities.
     */
    UTILITY("Utility", "Helper classes and common utilities", 10),
    
    /**
     * Configuration layer - application and framework configuration.
     */
    CONFIGURATION("Configuration", "Application and framework configuration", 11),
    
    /**
     * Security layer - authentication and authorization.
     */
    SECURITY("Security", "Authentication and authorization", 12),
    
    /**
     * Integration layer - external system integration.
     */
    INTEGRATION("Integration", "External system integration", 13),
    
    /**
     * Common layer - shared components across layers.
     */
    COMMON("Common", "Shared components across layers", 14),
    
    /**
     * Model layer - data transfer objects and models.
     */
    MODEL("Model", "Data transfer objects and models", 15),
    
    /**
     * Entity layer - JPA entities and data models.
     */
    ENTITY("Entity", "JPA entities and data models", 16),
    
    /**
     * Unknown or unclassified layer.
     */
    UNKNOWN("Unknown", "Unclassified layer type", 999);
    
    private final String displayName;
    private final String description;
    private final int hierarchyLevel;
    
    LayerType(String displayName, String description, int hierarchyLevel) {
        this.displayName = displayName;
        this.description = description;
        this.hierarchyLevel = hierarchyLevel;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getHierarchyLevel() {
        return hierarchyLevel;
    }
    
    /**
     * Determines if this layer should be above the other layer in the hierarchy.
     * Lower hierarchy levels are "above" higher levels.
     * 
     * @param other the other layer to compare with
     * @return true if this layer should be above the other layer
     */
    public boolean isAbove(LayerType other) {
        return this.hierarchyLevel < other.hierarchyLevel;
    }
    
    /**
     * Determines if this layer should be below the other layer in the hierarchy.
     * Higher hierarchy levels are "below" lower levels.
     * 
     * @param other the other layer to compare with
     * @return true if this layer should be below the other layer
     */
    public boolean isBelow(LayerType other) {
        return this.hierarchyLevel > other.hierarchyLevel;
    }
    
    /**
     * Determines if this layer is at the same level as the other layer.
     * 
     * @param other the other layer to compare with
     * @return true if both layers are at the same hierarchy level
     */
    public boolean isSameLevel(LayerType other) {
        return this.hierarchyLevel == other.hierarchyLevel;
    }
    
    /**
     * Determines if this layer is a presentation-related layer.
     * 
     * @return true if this is a presentation-related layer
     */
    public boolean isPresentationLayer() {
        return this == PRESENTATION || this == CONTROLLER;
    }
    
    /**
     * Determines if this layer is a business-related layer.
     * 
     * @return true if this is a business-related layer
     */
    public boolean isBusinessLayer() {
        return this == SERVICE || this == BUSINESS || this == DOMAIN;
    }
    
    /**
     * Determines if this layer is a data-related layer.
     * 
     * @return true if this is a data-related layer
     */
    public boolean isDataLayer() {
        return this == REPOSITORY || this == DATA_ACCESS || this == PERSISTENCE || this == ENTITY;
    }
    
    /**
     * Determines if this layer is an infrastructure-related layer.
     * 
     * @return true if this is an infrastructure-related layer
     */
    public boolean isInfrastructureLayer() {
        return this == INFRASTRUCTURE || this == CONFIGURATION || this == SECURITY || this == INTEGRATION;
    }
    
    /**
     * Determines if this layer is a utility or common layer.
     * 
     * @return true if this is a utility or common layer
     */
    public boolean isUtilityLayer() {
        return this == UTILITY || this == COMMON || this == MODEL;
    }
    
    /**
     * Determines if a dependency from this layer to the target layer would be allowed
     * in a proper layered architecture.
     * 
     * @param target the target layer of the dependency
     * @return true if the dependency is architecturally valid
     */
    public boolean canDependOn(LayerType target) {
        // A layer can depend on layers below it (higher hierarchy levels)
        // or layers at the same level (peer dependencies)
        // but should not depend on layers above it (lower hierarchy levels)
        
        if (this == UNKNOWN || target == UNKNOWN) {
            return true; // Allow unknown dependencies for now
        }
        
        // Utility and common layers can be depended upon by any layer
        if (target.isUtilityLayer()) {
            return true;
        }
        
        // Infrastructure layers can generally be depended upon by business layers
        if (target.isInfrastructureLayer() && this.isBusinessLayer()) {
            return true;
        }
        
        // Standard layered architecture rule: can depend on same level or lower levels
        return this.hierarchyLevel <= target.hierarchyLevel;
    }
    
    /**
     * Gets the expected package naming patterns for this layer type.
     * 
     * @return array of common package name patterns for this layer
     */
    public String[] getPackagePatterns() {
        switch (this) {
            case PRESENTATION: return new String[]{"presentation", "ui", "view", "web"};
            case CONTROLLER: return new String[]{"controller", "rest", "api", "endpoint"};
            case SERVICE: return new String[]{"service", "application"};
            case BUSINESS: return new String[]{"business", "logic", "core"};
            case DOMAIN: return new String[]{"domain", "model", "entity"};
            case REPOSITORY: return new String[]{"repository", "repo", "dao"};
            case DATA_ACCESS: return new String[]{"data", "access", "persistence"};
            case PERSISTENCE: return new String[]{"persistence", "jpa", "hibernate"};
            case INFRASTRUCTURE: return new String[]{"infrastructure", "infra", "technical"};
            case UTILITY: return new String[]{"util", "utils", "utility", "helper", "common"};
            case CONFIGURATION: return new String[]{"config", "configuration", "settings"};
            case SECURITY: return new String[]{"security", "auth", "authentication", "authorization"};
            case INTEGRATION: return new String[]{"integration", "external", "client"};
            case COMMON: return new String[]{"common", "shared", "base"};
            case MODEL: return new String[]{"model", "dto", "vo", "data"};
            case ENTITY: return new String[]{"entity", "domain", "model"};
            default: return new String[]{};
        }
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}