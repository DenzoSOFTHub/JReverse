package it.denzosoft.jreverse.analyzer.beancreation;

/**
 * Enumeration of different dependency injection mechanisms used by Spring.
 */
public enum DependencyInjectionType {
    
    /**
     * Constructor-based dependency injection.
     */
    CONSTRUCTOR("Constructor injection"),
    
    /**
     * Field-based dependency injection using @Autowired on fields.
     */
    FIELD("Field injection"),
    
    /**
     * Setter-based dependency injection using @Autowired on setter methods.
     */
    SETTER("Setter injection"),
    
    /**
     * Method-based dependency injection using @Autowired on other methods.
     */
    METHOD("Method injection"),
    
    /**
     * Bean method parameter injection (for @Bean methods).
     */
    PARAMETER("Parameter injection"),
    
    /**
     * Lookup method injection using @Lookup annotation.
     */
    LOOKUP("Lookup injection"),
    
    /**
     * Resource injection using @Resource annotation.
     */
    RESOURCE("Resource injection"),
    
    /**
     * Inject annotation-based injection (JSR-330).
     */
    INJECT("@Inject injection");
    
    private final String description;
    
    DependencyInjectionType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Checks if this injection type is constructor-based.
     */
    public boolean isConstructorBased() {
        return this == CONSTRUCTOR;
    }
    
    /**
     * Checks if this injection type is field-based.
     */
    public boolean isFieldBased() {
        return this == FIELD;
    }
    
    /**
     * Checks if this injection type is method-based.
     */
    public boolean isMethodBased() {
        return this == SETTER || this == METHOD || this == LOOKUP;
    }
    
    /**
     * Checks if this injection type is recommended by Spring best practices.
     * Constructor injection is preferred over field and setter injection.
     */
    public boolean isRecommended() {
        return this == CONSTRUCTOR;
    }
}