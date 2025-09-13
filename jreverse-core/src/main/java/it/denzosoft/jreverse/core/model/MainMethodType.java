package it.denzosoft.jreverse.core.model;

/**
 * Enumeration of main method types found in JAR analysis.
 */
public enum MainMethodType {
    
    SPRING_BOOT("Spring Boot application main method with SpringApplication.run()"),
    REGULAR("Regular Java application main method"),
    NONE("No main method found");
    
    private final String description;
    
    MainMethodType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isSpringBoot() {
        return this == SPRING_BOOT;
    }
    
    public boolean isRegular() {
        return this == REGULAR;
    }
    
    public boolean hasMainMethod() {
        return this != NONE;
    }
}