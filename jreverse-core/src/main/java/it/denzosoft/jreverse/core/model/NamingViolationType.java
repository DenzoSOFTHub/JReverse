package it.denzosoft.jreverse.core.model;

/**
 * Types of naming convention violations that can be detected in package names.
 */
public enum NamingViolationType {
    
    /**
     * Package name contains uppercase letters (should be lowercase).
     */
    UPPERCASE_LETTERS("Package names should be lowercase"),
    
    /**
     * Package name contains invalid characters (should only contain letters, numbers, dots).
     */
    INVALID_CHARACTERS("Package names should only contain lowercase letters, numbers and dots"),
    
    /**
     * Package name starts with a number.
     */
    STARTS_WITH_NUMBER("Package names should not start with a number"),
    
    /**
     * Package name uses reserved keywords.
     */
    RESERVED_KEYWORD("Package names should not use Java reserved keywords"),
    
    /**
     * Package name is too short (less than 3 characters).
     */
    TOO_SHORT("Package names should be at least 3 characters long"),
    
    /**
     * Package name is excessively long.
     */
    TOO_LONG("Package names should not exceed reasonable length"),
    
    /**
     * Package name uses non-descriptive naming (e.g., "util", "misc", "other").
     */
    NON_DESCRIPTIVE("Package names should be descriptive and meaningful"),
    
    /**
     * Package name doesn't follow domain-driven design conventions.
     */
    POOR_DOMAIN_NAMING("Package names should reflect business domain concepts"),
    
    /**
     * Package name uses abbreviations that are not widely understood.
     */
    UNCLEAR_ABBREVIATIONS("Package names should avoid unclear abbreviations"),
    
    /**
     * Package contains multiple dots in sequence (e.g., "com..example").
     */
    MULTIPLE_DOTS("Package names should not contain consecutive dots"),
    
    /**
     * Package name ends with a dot.
     */
    ENDS_WITH_DOT("Package names should not end with a dot"),
    
    /**
     * Package name doesn't match organizational standards.
     */
    ORGANIZATIONAL_STANDARD("Package name doesn't follow organizational naming standards");
    
    private final String description;
    
    NamingViolationType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isStructuralViolation() {
        return this == UPPERCASE_LETTERS || 
               this == INVALID_CHARACTERS || 
               this == STARTS_WITH_NUMBER ||
               this == MULTIPLE_DOTS ||
               this == ENDS_WITH_DOT;
    }
    
    public boolean isSemanticViolation() {
        return this == RESERVED_KEYWORD ||
               this == NON_DESCRIPTIVE ||
               this == POOR_DOMAIN_NAMING ||
               this == UNCLEAR_ABBREVIATIONS;
    }
    
    public boolean isLengthViolation() {
        return this == TOO_SHORT || this == TOO_LONG;
    }
    
    public boolean isPolicyViolation() {
        return this == ORGANIZATIONAL_STANDARD;
    }
}