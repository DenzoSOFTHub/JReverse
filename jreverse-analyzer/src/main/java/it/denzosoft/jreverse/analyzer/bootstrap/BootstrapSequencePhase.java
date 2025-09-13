package it.denzosoft.jreverse.analyzer.bootstrap;

/**
 * Enumeration of Spring Boot bootstrap sequence phases.
 * Represents the different stages of the Spring Boot application startup process.
 */
public enum BootstrapSequencePhase {
    
    /**
     * Main method execution phase - Initial entry point
     */
    MAIN_METHOD_EXECUTION(1, "Main Method Execution", "Application entry point execution"),
    
    /**
     * SpringApplication.run() invocation phase
     */
    SPRING_APPLICATION_RUN(2, "SpringApplication.run()", "SpringApplication.run() method invocation"),
    
    /**
     * Application context creation and preparation phase
     */
    CONTEXT_PREPARATION(3, "Context Preparation", "Application context creation and preparation"),
    
    /**
     * Component scanning phase - Discovery of Spring components
     */
    COMPONENT_SCANNING(4, "Component Scanning", "Scanning for @Component, @Service, @Repository, @Controller annotations"),
    
    /**
     * Auto-configuration activation phase
     */
    AUTO_CONFIGURATION(5, "Auto-Configuration", "Spring Boot auto-configuration classes activation"),
    
    /**
     * Bean creation and dependency injection phase
     */
    BEAN_CREATION(6, "Bean Creation", "Spring bean instantiation and dependency injection"),
    
    /**
     * Post-processing and initialization phase
     */
    POST_PROCESSING(7, "Post-Processing", "Bean post-processing and initialization callbacks"),
    
    /**
     * Application ready event phase - Final startup completion
     */
    APPLICATION_READY(8, "Application Ready", "Application ready event and final initialization");
    
    private final int order;
    private final String displayName;
    private final String description;
    
    BootstrapSequencePhase(int order, String displayName, String description) {
        this.order = order;
        this.displayName = displayName;
        this.description = description;
    }
    
    public int getOrder() {
        return order;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Returns the previous phase in the sequence.
     * Returns null if this is the first phase.
     */
    public BootstrapSequencePhase getPreviousPhase() {
        if (order == 1) {
            return null;
        }
        for (BootstrapSequencePhase phase : values()) {
            if (phase.order == this.order - 1) {
                return phase;
            }
        }
        return null;
    }
    
    /**
     * Returns the next phase in the sequence.
     * Returns null if this is the last phase.
     */
    public BootstrapSequencePhase getNextPhase() {
        for (BootstrapSequencePhase phase : values()) {
            if (phase.order == this.order + 1) {
                return phase;
            }
        }
        return null;
    }
    
    /**
     * Checks if this phase comes before the specified phase.
     */
    public boolean comesBefore(BootstrapSequencePhase other) {
        return this.order < other.order;
    }
    
    /**
     * Checks if this phase comes after the specified phase.
     */
    public boolean comesAfter(BootstrapSequencePhase other) {
        return this.order > other.order;
    }
}