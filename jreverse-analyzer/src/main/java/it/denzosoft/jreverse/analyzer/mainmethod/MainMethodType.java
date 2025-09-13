package it.denzosoft.jreverse.analyzer.mainmethod;

/**
 * Types of main methods that can be discovered during analysis.
 */
public enum MainMethodType {
    /**
     * A Spring Boot main method that calls SpringApplication.run().
     */
    SPRING_BOOT,
    
    /**
     * A regular Java main method that doesn't use Spring Boot.
     */
    REGULAR,
    
    /**
     * No main method was found in the JAR.
     */
    NONE,
    
    /**
     * Analysis failed due to an error.
     */
    ERROR
}