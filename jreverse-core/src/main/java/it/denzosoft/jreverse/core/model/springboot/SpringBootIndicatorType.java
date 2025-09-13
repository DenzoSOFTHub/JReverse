package it.denzosoft.jreverse.core.model.springboot;

/**
 * Types of Spring Boot detection indicators with their associated weights.
 * Weights are calibrated based on empirical analysis of real Spring Boot applications.
 */
public enum SpringBootIndicatorType {
    
    /**
     * Detects @SpringBootApplication and related annotations.
     * Highest confidence indicator.
     */
    ANNOTATION(0.9),
    
    /**
     * Analyzes main method for SpringApplication.run() calls.
     * Very high confidence indicator.
     */
    MAIN_CLASS(0.85),
    
    /**
     * Checks manifest attributes and Spring Boot specific headers.
     * High confidence indicator.
     */
    MANIFEST(0.8),
    
    /**
     * Analyzes JAR dependencies for Spring Boot libraries.
     * Good confidence indicator.
     */
    DEPENDENCY(0.7),
    
    /**
     * Examines JAR structure (BOOT-INF/, spring.factories, etc.).
     * Medium confidence indicator due to possible false positives.
     */
    JAR_STRUCTURE(0.6);
    
    private final double weight;
    
    SpringBootIndicatorType(double weight) {
        this.weight = weight;
    }
    
    /**
     * Gets the weight of this indicator for confidence calculation.
     * Higher weights indicate more reliable indicators.
     */
    public double getWeight() {
        return weight;
    }
    
    /**
     * Gets a human-readable description of this indicator.
     */
    public String getDescription() {
        switch (this) {
            case ANNOTATION:
                return "Spring Boot annotation analysis (@SpringBootApplication, @EnableAutoConfiguration)";
            case MAIN_CLASS:
                return "Main class analysis for SpringApplication.run() calls";
            case MANIFEST:
                return "JAR manifest Spring Boot attributes (Start-Class, Spring-Boot-Version)";
            case DEPENDENCY:
                return "Spring Boot dependency analysis (spring-boot-starter-*, spring-boot-autoconfigure)";
            case JAR_STRUCTURE:
                return "JAR structure analysis (BOOT-INF/, spring.factories, META-INF/spring.*)";
            default:
                return "Unknown indicator type";
        }
    }
    
    /**
     * Gets the analysis priority for performance optimization.
     * Lower numbers indicate higher priority (should be analyzed first).
     */
    public int getAnalysisPriority() {
        switch (this) {
            case MANIFEST:
                return 1;        // Fastest, check first
            case ANNOTATION:
                return 2;        // Fast and highly reliable
            case JAR_STRUCTURE:
                return 3;        // Fast but medium reliability
            case MAIN_CLASS:
                return 4;        // Slower but highly reliable
            case DEPENDENCY:
                return 5;        // Slowest, analyze last
            default:
                return 99;       // Unknown types have lowest priority
        }
    }
    
    /**
     * Gets the expected analysis performance category.
     */
    public PerformanceCategory getPerformanceCategory() {
        switch (this) {
            case MANIFEST:
            case JAR_STRUCTURE:
                return PerformanceCategory.FAST;
            case ANNOTATION:
                return PerformanceCategory.MEDIUM;
            case MAIN_CLASS:
            case DEPENDENCY:
                return PerformanceCategory.SLOW;
            default:
                return PerformanceCategory.SLOW;
        }
    }
    
    public enum PerformanceCategory {
        FAST,    // < 10ms typical
        MEDIUM,  // 10-50ms typical  
        SLOW     // 50-200ms typical
    }
}