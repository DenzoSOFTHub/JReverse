package it.denzosoft.jreverse.core.model;

/**
 * Enumeration representing different types of JAR files.
 */
public enum JarType {
    REGULAR_JAR("Regular JAR"),
    SPRING_BOOT_JAR("Spring Boot JAR"),
    WAR_ARCHIVE("Web Archive (WAR)"),
    EXECUTABLE_JAR("Executable JAR"),
    LIBRARY_JAR("Library JAR"),
    MAVEN_PLUGIN("Maven Plugin"),
    UNKNOWN("Unknown Type");

    // Legacy enum names for backward compatibility
    public static final JarType REGULAR = REGULAR_JAR;
    public static final JarType SPRING_BOOT = SPRING_BOOT_JAR;
    public static final JarType WAR = WAR_ARCHIVE;
    public static final JarType EXECUTABLE = EXECUTABLE_JAR;
    public static final JarType LIBRARY = LIBRARY_JAR;
    
    private final String displayName;
    
    JarType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean isSpringBoot() {
        return this == SPRING_BOOT_JAR;
    }
    
    public boolean isWebArchive() {
        return this == WAR_ARCHIVE;
    }
    
    public boolean isExecutable() {
        return this == EXECUTABLE_JAR || this == SPRING_BOOT_JAR;
    }
    
    public boolean isLibrary() {
        return this == LIBRARY_JAR;
    }
}