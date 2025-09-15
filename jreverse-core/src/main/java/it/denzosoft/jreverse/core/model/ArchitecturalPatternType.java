package it.denzosoft.jreverse.core.model;

/**
 * Types of architectural patterns that can be detected in package structures.
 */
public enum ArchitecturalPatternType {
    
    LAYERED("Layered Architecture", "Traditional N-tier layered architecture"),
    HEXAGONAL("Hexagonal Architecture", "Ports and adapters architecture"),
    MODULAR("Modular Architecture", "Module-based organization"),
    DOMAIN_DRIVEN("Domain-Driven Design", "Domain-centric package organization"),
    MICROSERVICE("Microservice Pattern", "Microservice-oriented structure"),
    MVC("Model-View-Controller", "MVC architectural pattern"),
    MVP("Model-View-Presenter", "MVP architectural pattern"),
    CLEAN_ARCHITECTURE("Clean Architecture", "Clean architecture principles"),
    ONION("Onion Architecture", "Onion-style layering"),
    PLUGIN("Plugin Architecture", "Plugin-based architecture"),
    SERVICE_ORIENTED("Service-Oriented", "Service-oriented architecture"),
    EVENT_DRIVEN("Event-Driven", "Event-driven architecture"),
    UNKNOWN("Unknown", "Pattern type could not be determined");
    
    private final String displayName;
    private final String description;
    
    ArchitecturalPatternType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
}