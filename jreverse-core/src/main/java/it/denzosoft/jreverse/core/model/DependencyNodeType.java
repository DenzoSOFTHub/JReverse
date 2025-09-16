package it.denzosoft.jreverse.core.model;

/**
 * Enumeration of dependency node types in the dependency graph.
 * 
 * @author JReverse Development Team
 * @since 1.1.0 (Phase 3)
 */
public enum DependencyNodeType {
    
    /**
     * Represents a Java package node.
     * Identifier format: "com.example.package"
     */
    PACKAGE("Package"),
    
    /**
     * Represents a Java class or interface node.
     * Identifier format: "com.example.Class"
     */
    CLASS("Class"),
    
    /**
     * Represents a method node within a class.
     * Identifier format: "com.example.Class.method(String,int)"
     */
    METHOD("Method"),
    
    /**
     * Represents a field node within a class.
     * Identifier format: "com.example.Class.fieldName"
     */
    FIELD("Field"),
    
    /**
     * Represents an external library or JAR dependency.
     * Identifier format: "external:library-name:version"
     */
    EXTERNAL_LIBRARY("External Library"),
    
    /**
     * Represents a Spring Boot module or component.
     * Identifier format: "spring:component:name"
     */
    SPRING_COMPONENT("Spring Component");
    
    private final String displayName;
    
    DependencyNodeType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Checks if this node type represents a code element (vs external dependency).
     * 
     * @return true for PACKAGE, CLASS, METHOD, FIELD, SPRING_COMPONENT
     */
    public boolean isCodeElement() {
        return this != EXTERNAL_LIBRARY;
    }
    
    /**
     * Checks if this node type can have child nodes.
     * 
     * @return true for PACKAGE, CLASS
     */
    public boolean canHaveChildren() {
        return this == PACKAGE || this == CLASS;
    }
    
    /**
     * Gets the parent node type for hierarchical relationships.
     * 
     * @return parent node type, or null if no parent
     */
    public DependencyNodeType getParentType() {
        switch (this) {
            case CLASS:
            case SPRING_COMPONENT:
                return PACKAGE;
            case METHOD:
            case FIELD:
                return CLASS;
            default:
                return null;
        }
    }
}