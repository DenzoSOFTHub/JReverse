package it.denzosoft.jreverse.core.model;

/**
 * Types of architectural anti-patterns that can be detected in package structures.
 */
public enum AntiPatternType {
    
    /**
     * God Package - package contains too many classes or responsibilities.
     */
    GOD_PACKAGE("God Package", "Package contains too many classes or responsibilities"),
    
    /**
     * Cyclic Dependencies - circular dependencies between packages.
     */
    CYCLIC_DEPENDENCIES("Cyclic Dependencies", "Circular dependencies detected between packages"),
    
    /**
     * Feature Envy - classes in one package heavily use classes from another package.
     */
    FEATURE_ENVY("Feature Envy", "Classes heavily depend on external packages"),
    
    /**
     * Inappropriate Intimacy - packages are too tightly coupled.
     */
    INAPPROPRIATE_INTIMACY("Inappropriate Intimacy", "Packages are too tightly coupled"),
    
    /**
     * Lazy Package - package has very few classes or minimal functionality.
     */
    LAZY_PACKAGE("Lazy Package", "Package has minimal functionality or very few classes"),
    
    /**
     * Dead Package - package contains classes that are never used.
     */
    DEAD_PACKAGE("Dead Package", "Package contains unused classes"),
    
    /**
     * Shotgun Surgery - changes require modifications across multiple packages.
     */
    SHOTGUN_SURGERY("Shotgun Surgery", "Changes require modifications across multiple packages"),
    
    /**
     * Hub Package - package is used by too many other packages.
     */
    HUB_PACKAGE("Hub Package", "Package is used by too many other packages"),
    
    /**
     * Unstable Dependencies - package depends on less stable packages.
     */
    UNSTABLE_DEPENDENCIES("Unstable Dependencies", "Package depends on less stable packages"),
    
    /**
     * Violation of Acyclic Dependencies Principle.
     */
    ADP_VIOLATION("ADP Violation", "Violation of Acyclic Dependencies Principle"),
    
    /**
     * Violation of Stable Dependencies Principle.
     */
    SDP_VIOLATION("SDP Violation", "Violation of Stable Dependencies Principle"),
    
    /**
     * Violation of Stable Abstractions Principle.
     */
    SAP_VIOLATION("SAP Violation", "Violation of Stable Abstractions Principle"),

    /**
     * High Coupling - package has too many outgoing dependencies.
     */
    HIGH_COUPLING("High Coupling", "Package has excessive efferent coupling"),

    /**
     * Cyclic Dependency - circular dependency detected in package structure.
     */
    CYCLIC_DEPENDENCY("Cyclic Dependency", "Circular dependency detected in package structure"),
    
    /**
     * Package organization doesn't follow domain-driven design principles.
     */
    POOR_DOMAIN_SEPARATION("Poor Domain Separation", "Package structure doesn't reflect domain boundaries"),
    
    /**
     * Technical packages mixed with business logic packages.
     */
    MIXED_CONCERNS("Mixed Concerns", "Technical and business concerns are mixed in packages"),
    
    /**
     * Package structure doesn't follow layered architecture principles.
     */
    LAYERING_VIOLATION("Layering Violation", "Package structure violates layered architecture");
    
    private final String displayName;
    private final String description;
    
    AntiPatternType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isStructuralAntiPattern() {
        return this == GOD_PACKAGE || 
               this == LAZY_PACKAGE || 
               this == DEAD_PACKAGE ||
               this == HUB_PACKAGE;
    }
    
    public boolean isDependencyAntiPattern() {
        return this == CYCLIC_DEPENDENCIES ||
               this == CYCLIC_DEPENDENCY ||
               this == HIGH_COUPLING ||
               this == FEATURE_ENVY ||
               this == INAPPROPRIATE_INTIMACY ||
               this == UNSTABLE_DEPENDENCIES ||
               this == ADP_VIOLATION ||
               this == SDP_VIOLATION ||
               this == SAP_VIOLATION;
    }
    
    public boolean isDesignAntiPattern() {
        return this == SHOTGUN_SURGERY ||
               this == POOR_DOMAIN_SEPARATION ||
               this == MIXED_CONCERNS ||
               this == LAYERING_VIOLATION;
    }
}