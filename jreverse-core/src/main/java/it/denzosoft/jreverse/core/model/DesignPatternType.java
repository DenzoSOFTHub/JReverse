package it.denzosoft.jreverse.core.model;

/**
 * Enumeration of supported design patterns for detection and analysis.
 */
public enum DesignPatternType {
    
    // Creational Patterns
    SINGLETON("Singleton", "Ensures a class has only one instance"),
    FACTORY_METHOD("Factory Method", "Creates objects without specifying their concrete classes"),
    ABSTRACT_FACTORY("Abstract Factory", "Creates families of related objects"),
    BUILDER("Builder", "Constructs complex objects step by step"),
    PROTOTYPE("Prototype", "Creates objects by cloning existing instances"),
    
    // Structural Patterns
    ADAPTER("Adapter", "Allows incompatible interfaces to work together"),
    BRIDGE("Bridge", "Separates abstraction from implementation"),
    COMPOSITE("Composite", "Composes objects into tree structures"),
    DECORATOR("Decorator", "Adds behavior to objects dynamically"),
    FACADE("Facade", "Provides simplified interface to complex subsystem"),
    FLYWEIGHT("Flyweight", "Reduces memory usage by sharing common data"),
    PROXY("Proxy", "Provides placeholder or surrogate for another object"),
    
    // Behavioral Patterns
    CHAIN_OF_RESPONSIBILITY("Chain of Responsibility", "Passes requests along chain of handlers"),
    COMMAND("Command", "Encapsulates requests as objects"),
    INTERPRETER("Interpreter", "Defines grammar and interpreter for language"),
    ITERATOR("Iterator", "Provides sequential access to collection elements"),
    MEDIATOR("Mediator", "Defines how objects interact with each other"),
    MEMENTO("Memento", "Captures and restores object state"),
    OBSERVER("Observer", "Notifies multiple objects about state changes"),
    STATE("State", "Changes object behavior based on internal state"),
    STRATEGY("Strategy", "Defines family of algorithms and makes them interchangeable"),
    TEMPLATE_METHOD("Template Method", "Defines skeleton of algorithm in base class"),
    VISITOR("Visitor", "Separates algorithms from objects they operate on"),
    
    // Spring-specific Patterns
    DEPENDENCY_INJECTION("Dependency Injection", "Injects dependencies from external source"),
    MVC("Model-View-Controller", "Separates application into three components"),
    REPOSITORY("Repository", "Encapsulates data access logic"),
    SERVICE_LAYER("Service Layer", "Defines application's boundary and operations"),
    DATA_TRANSFER_OBJECT("Data Transfer Object", "Transfers data between layers"),
    
    // Architectural Patterns
    LAYERED_ARCHITECTURE("Layered Architecture", "Organizes code into horizontal layers"),
    HEXAGONAL_ARCHITECTURE("Hexagonal Architecture", "Isolates core logic from external concerns"),
    MICROSERVICES("Microservices", "Decomposes application into small services"),
    
    // Unknown or unrecognized pattern
    UNKNOWN("Unknown", "Pattern not recognized or classified");
    
    private final String displayName;
    private final String description;
    
    DesignPatternType(String displayName, String description) {
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
     * Determines if this is a creational pattern.
     * 
     * @return true if this is a creational pattern
     */
    public boolean isCreational() {
        return this == SINGLETON || this == FACTORY_METHOD || this == ABSTRACT_FACTORY ||
               this == BUILDER || this == PROTOTYPE;
    }
    
    /**
     * Determines if this is a structural pattern.
     * 
     * @return true if this is a structural pattern
     */
    public boolean isStructural() {
        return this == ADAPTER || this == BRIDGE || this == COMPOSITE ||
               this == DECORATOR || this == FACADE || this == FLYWEIGHT || this == PROXY;
    }
    
    /**
     * Determines if this is a behavioral pattern.
     * 
     * @return true if this is a behavioral pattern
     */
    public boolean isBehavioral() {
        return this == CHAIN_OF_RESPONSIBILITY || this == COMMAND || this == INTERPRETER ||
               this == ITERATOR || this == MEDIATOR || this == MEMENTO || this == OBSERVER ||
               this == STATE || this == STRATEGY || this == TEMPLATE_METHOD || this == VISITOR;
    }
    
    /**
     * Determines if this is a Spring-specific pattern.
     * 
     * @return true if this is a Spring-specific pattern
     */
    public boolean isSpringPattern() {
        return this == DEPENDENCY_INJECTION || this == MVC || this == REPOSITORY ||
               this == SERVICE_LAYER || this == DATA_TRANSFER_OBJECT;
    }
    
    /**
     * Determines if this is an architectural pattern.
     * 
     * @return true if this is an architectural pattern
     */
    public boolean isArchitectural() {
        return this == LAYERED_ARCHITECTURE || this == HEXAGONAL_ARCHITECTURE ||
               this == MICROSERVICES;
    }
    
    /**
     * Gets the UML stereotype for this pattern type.
     * 
     * @return the stereotype string for PlantUML
     */
    public String getUMLStereotype() {
        switch (this) {
            case SINGLETON: return "<<Singleton>>";
            case FACTORY_METHOD: return "<<Factory>>";
            case ABSTRACT_FACTORY: return "<<AbstractFactory>>";
            case BUILDER: return "<<Builder>>";
            case ADAPTER: return "<<Adapter>>";
            case DECORATOR: return "<<Decorator>>";
            case FACADE: return "<<Facade>>";
            case PROXY: return "<<Proxy>>";
            case OBSERVER: return "<<Observer>>";
            case STRATEGY: return "<<Strategy>>";
            case COMMAND: return "<<Command>>";
            case REPOSITORY: return "<<Repository>>";
            case SERVICE_LAYER: return "<<Service>>";
            case DATA_TRANSFER_OBJECT: return "<<DTO>>";
            case MVC: return "<<Controller>>";
            default: return "<<" + displayName + ">>";
        }
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}