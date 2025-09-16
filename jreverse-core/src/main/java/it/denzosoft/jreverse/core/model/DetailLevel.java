package it.denzosoft.jreverse.core.model;

/**
 * Enumeration representing different levels of detail for UML diagram generation.
 */
public enum DetailLevel {
    
    /**
     * Minimal detail - only class names and basic structure.
     */
    MINIMAL("minimal", "Shows only class names"),
    
    /**
     * Summary detail - class names, key methods and fields.
     */
    SUMMARY("summary", "Shows class names with key methods and fields"),
    
    /**
     * Detailed view - all methods, fields, modifiers, and annotations.
     */
    DETAILED("detailed", "Shows all class details including private members"),
    
    /**
     * Full detail - everything including parameter names and method bodies (if available).
     */
    FULL("full", "Shows complete class information");
    
    private final String code;
    private final String description;
    
    DetailLevel(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Determines if this detail level should include private members.
     * 
     * @return true if private members should be included
     */
    public boolean includePrivateMembers() {
        return this == DETAILED || this == FULL;
    }
    
    /**
     * Determines if this detail level should include method parameters.
     * 
     * @return true if method parameters should be included
     */
    public boolean includeMethodParameters() {
        return this == DETAILED || this == FULL;
    }
    
    /**
     * Determines if this detail level should include field types.
     * 
     * @return true if field types should be included
     */
    public boolean includeFieldTypes() {
        return this != MINIMAL;
    }
    
    /**
     * Determines if this detail level should include annotations.
     * 
     * @return true if annotations should be included
     */
    public boolean includeAnnotations() {
        return this == DETAILED || this == FULL;
    }
    
    @Override
    public String toString() {
        return code;
    }
}