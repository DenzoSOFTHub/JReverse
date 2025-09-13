package it.denzosoft.jreverse.core.pattern;

/**
 * Enumeration of analyzer types supported by the factory.
 */
public enum AnalyzerType {
    DEFAULT("Default Analyzer", "General-purpose analyzer for all JAR types"),
    SPRING_BOOT("Spring Boot Analyzer", "Specialized analyzer for Spring Boot applications"),
    REGULAR_JAR("Regular JAR Analyzer", "Analyzer for standard Java JAR files"),
    LIBRARY_JAR("Library JAR Analyzer", "Analyzer for Java library JAR files"),
    WAR_ANALYZER("WAR Analyzer", "Analyzer for Web Application Archives"),
    MAVEN_PLUGIN("Maven Plugin Analyzer", "Analyzer for Maven plugin JARs");
    
    private final String displayName;
    private final String description;
    
    AnalyzerType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isSpecialized() {
        return this != DEFAULT;
    }
    
    public boolean isSpringBoot() {
        return this == SPRING_BOOT;
    }
    
    public boolean isWebArchive() {
        return this == WAR_ANALYZER;
    }
}