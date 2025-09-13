package it.denzosoft.jreverse.analyzer.mainmethod;

/**
 * Types of SpringApplication.run() calls that can be detected.
 */
public enum SpringApplicationCallType {
    /**
     * Standard SpringApplication.run(Class, args) call.
     * Example: SpringApplication.run(Application.class, args)
     */
    STANDARD,
    
    /**
     * SpringApplication.run() with multiple source classes.
     * Example: SpringApplication.run(new Class[]{App.class, Config.class}, args)
     */
    MULTIPLE_SOURCES,
    
    /**
     * SpringApplication instance-based call.
     * Example: new SpringApplication(App.class).run(args)
     */
    INSTANCE_BASED,
    
    /**
     * SpringApplication with builder pattern.
     * Example: new SpringApplicationBuilder(App.class).run(args)
     */
    BUILDER_BASED,
    
    /**
     * Custom or unknown SpringApplication call pattern.
     */
    CUSTOM,
    
    /**
     * Could not determine the call type.
     */
    UNKNOWN
}