package it.denzosoft.jreverse.core.model;

/**
 * Enumeration representing different types of Java classes.
 */
public enum ClassType {
    CLASS("class"),
    PUBLIC_CLASS("public class"),
    ABSTRACT_CLASS("abstract class"),
    FINAL_CLASS("final class"),
    INTERFACE("interface"),
    PUBLIC_INTERFACE("public interface"),
    ENUM("enum"),
    PUBLIC_ENUM("public enum"),
    ANNOTATION("annotation"),
    PUBLIC_ANNOTATION("public annotation");
    
    private final String displayName;
    
    ClassType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean isPublic() {
        return this == PUBLIC_CLASS || 
               this == PUBLIC_INTERFACE ||
               this == PUBLIC_ENUM ||
               this == PUBLIC_ANNOTATION;
    }
    
    public boolean isInterface() {
        return this == INTERFACE || this == PUBLIC_INTERFACE;
    }
    
    public boolean isEnum() {
        return this == ENUM || this == PUBLIC_ENUM;
    }
    
    public boolean isAnnotation() {
        return this == ANNOTATION || this == PUBLIC_ANNOTATION;
    }
    
    public boolean isClass() {
        return this == CLASS || this == PUBLIC_CLASS || 
               this == ABSTRACT_CLASS || this == FINAL_CLASS;
    }
}