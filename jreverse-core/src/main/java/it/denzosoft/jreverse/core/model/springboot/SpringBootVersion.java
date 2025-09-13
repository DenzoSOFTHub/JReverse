package it.denzosoft.jreverse.core.model.springboot;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a Spring Boot version with parsing and comparison capabilities.
 * Supports semantic versioning (MAJOR.MINOR.PATCH) with optional qualifiers.
 */
public final class SpringBootVersion implements Comparable<SpringBootVersion> {
    
    private static final Pattern VERSION_PATTERN = Pattern.compile(
        "^(\\d+)\\.(\\d+)(?:\\.(\\d+))?(?:[-.]([A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)*))?$"
    );
    
    private final int major;
    private final int minor;
    private final int patch;
    private final String qualifier;
    private final String originalVersionString;
    
    private SpringBootVersion(int major, int minor, int patch, String qualifier, String originalVersionString) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.qualifier = qualifier;
        this.originalVersionString = originalVersionString;
    }
    
    public int getMajor() {
        return major;
    }
    
    public int getMinor() {
        return minor;
    }
    
    public int getPatch() {
        return patch;
    }
    
    public String getQualifier() {
        return qualifier;
    }
    
    public String getVersionString() {
        return originalVersionString;
    }
    
    /**
     * Gets the base version string without qualifier (e.g., "2.7.0" from "2.7.0-SNAPSHOT").
     */
    public String getBaseVersion() {
        return String.format("%d.%d.%d", major, minor, patch);
    }
    
    /**
     * Checks if this version is a release version (no qualifier or RELEASE qualifier).
     */
    public boolean isRelease() {
        return qualifier == null || "RELEASE".equalsIgnoreCase(qualifier);
    }
    
    /**
     * Checks if this version is a snapshot version.
     */
    public boolean isSnapshot() {
        return qualifier != null && qualifier.toUpperCase().contains("SNAPSHOT");
    }
    
    /**
     * Checks if this version is a milestone or release candidate.
     */
    public boolean isPreRelease() {
        if (qualifier == null) return false;
        String upperQualifier = qualifier.toUpperCase();
        return upperQualifier.contains("M") || upperQualifier.contains("RC") || 
               upperQualifier.contains("BETA") || upperQualifier.contains("ALPHA");
    }
    
    /**
     * Gets the Spring Boot generation (1.x, 2.x, 3.x, etc.).
     */
    public int getGeneration() {
        return major;
    }
    
    /**
     * Checks if this version is compatible with the given version.
     * Compatible means same major version and this version is >= given version.
     */
    public boolean isCompatibleWith(SpringBootVersion other) {
        if (other == null) return false;
        return this.major == other.major && this.compareTo(other) >= 0;
    }
    
    /**
     * Checks if this version supports a specific feature based on version ranges.
     */
    public boolean supportsFeature(SpringBootFeature feature) {
        switch (feature) {
            case ACTUATOR:
                return major >= 1;
            case WEBFLUX:
                return major >= 2;
            case NATIVE_IMAGE:
                return major >= 2 && (minor >= 7 || major > 2);
            case JAVA_17:
                return major >= 2 && minor >= 5;
            case JAVA_21:
                return major >= 3;
            case MICROMETER:
                return major >= 2;
            case CONFIGURATION_PROPERTIES_SCAN:
                return major >= 2 && minor >= 2;
            case GRADLE_KOTLIN_DSL:
                return major >= 2 && minor >= 1;
            default:
                return false;
        }
    }
    
    @Override
    public int compareTo(SpringBootVersion other) {
        if (other == null) return 1;
        
        // Compare major version
        int majorComparison = Integer.compare(this.major, other.major);
        if (majorComparison != 0) return majorComparison;
        
        // Compare minor version
        int minorComparison = Integer.compare(this.minor, other.minor);
        if (minorComparison != 0) return minorComparison;
        
        // Compare patch version
        int patchComparison = Integer.compare(this.patch, other.patch);
        if (patchComparison != 0) return patchComparison;
        
        // Compare qualifiers
        return compareQualifiers(this.qualifier, other.qualifier);
    }
    
    private int compareQualifiers(String q1, String q2) {
        // Null qualifier (release) is highest
        if (q1 == null && q2 == null) return 0;
        if (q1 == null) return 1;
        if (q2 == null) return -1;
        
        // RELEASE qualifier is treated as null
        if ("RELEASE".equalsIgnoreCase(q1)) q1 = null;
        if ("RELEASE".equalsIgnoreCase(q2)) q2 = null;
        
        if (q1 == null && q2 == null) return 0;
        if (q1 == null) return 1;
        if (q2 == null) return -1;
        
        // Compare qualifiers lexicographically
        return q1.compareToIgnoreCase(q2);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SpringBootVersion that = (SpringBootVersion) obj;
        return major == that.major &&
               minor == that.minor &&
               patch == that.patch &&
               Objects.equals(qualifier, that.qualifier);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(major, minor, patch, qualifier);
    }
    
    @Override
    public String toString() {
        return originalVersionString;
    }
    
    /**
     * Parses a version string into a SpringBootVersion object.
     * 
     * @param versionString the version string to parse
     * @return parsed SpringBootVersion or null if invalid
     */
    public static SpringBootVersion parse(String versionString) {
        if (versionString == null || versionString.trim().isEmpty()) {
            return null;
        }
        
        String trimmed = versionString.trim();
        Matcher matcher = VERSION_PATTERN.matcher(trimmed);
        
        if (!matcher.matches()) {
            return null;
        }
        
        try {
            int major = Integer.parseInt(matcher.group(1));
            int minor = Integer.parseInt(matcher.group(2));
            int patch = matcher.group(3) != null ? Integer.parseInt(matcher.group(3)) : 0;
            String qualifier = matcher.group(4);
            
            return new SpringBootVersion(major, minor, patch, qualifier, trimmed);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Creates a SpringBootVersion from individual components.
     */
    public static SpringBootVersion of(int major, int minor, int patch) {
        return of(major, minor, patch, null);
    }
    
    /**
     * Creates a SpringBootVersion from individual components with qualifier.
     */
    public static SpringBootVersion of(int major, int minor, int patch, String qualifier) {
        StringBuilder versionString = new StringBuilder();
        versionString.append(major).append('.').append(minor).append('.').append(patch);
        
        if (qualifier != null && !qualifier.trim().isEmpty()) {
            versionString.append('-').append(qualifier.trim());
        }
        
        return new SpringBootVersion(major, minor, patch, qualifier, versionString.toString());
    }
    
    // Common Spring Boot versions for testing and compatibility checks
    public static final SpringBootVersion SPRING_BOOT_1_5 = of(1, 5, 0);
    public static final SpringBootVersion SPRING_BOOT_2_0 = of(2, 0, 0);
    public static final SpringBootVersion SPRING_BOOT_2_7 = of(2, 7, 0);
    public static final SpringBootVersion SPRING_BOOT_3_0 = of(3, 0, 0);
    public static final SpringBootVersion SPRING_BOOT_3_1 = of(3, 1, 0);
    
    /**
     * Spring Boot features that are version-dependent.
     */
    public enum SpringBootFeature {
        ACTUATOR,
        WEBFLUX,
        NATIVE_IMAGE,
        JAVA_17,
        JAVA_21,
        MICROMETER,
        CONFIGURATION_PROPERTIES_SCAN,
        GRADLE_KOTLIN_DSL
    }
}