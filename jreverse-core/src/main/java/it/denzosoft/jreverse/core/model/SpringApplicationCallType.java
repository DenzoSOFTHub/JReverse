package it.denzosoft.jreverse.core.model;

/**
 * Types of SpringApplication.run() call patterns.
 */
public enum SpringApplicationCallType {
    
    STANDARD("SpringApplication.run(Class, args)"),
    BUILDER("SpringApplicationBuilder pattern"),
    MULTIPLE_SOURCES("SpringApplication.run(Class[], args)"),
    CUSTOM("Custom SpringApplication configuration"),
    UNKNOWN("Unknown or complex call pattern");
    
    private final String description;
    
    SpringApplicationCallType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isStandard() {
        return this == STANDARD;
    }
    
    public boolean isCustom() {
        return this == CUSTOM || this == BUILDER;
    }
}