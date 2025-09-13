package it.denzosoft.jreverse.analyzer.security;

/**
 * Enumeration of security entrypoint types for Spring applications.
 * Categorizes different security annotation patterns and their usage.
 */
public enum SecurityEntrypointType {
    
    PRE_AUTHORIZE("@PreAuthorize", "Pre-authorization with SpEL expressions", true),
    POST_AUTHORIZE("@PostAuthorize", "Post-authorization with result-based checks", true),
    SECURED("@Secured", "Role-based security with simple role strings", false),
    ROLES_ALLOWED("@RolesAllowed", "JSR-250 role-based authorization", false),
    DENY_ALL("@DenyAll", "Explicitly denies access to all users", false),
    PERMIT_ALL("@PermitAll", "Explicitly permits access to all users", false),
    RUN_AS("@RunAs", "Run with specific role context", false),
    
    // Method-level security
    METHOD_SECURITY("Method Security", "Generic method-level security", false),
    
    // Class-level security
    CLASS_SECURITY("Class Security", "Class-level security applied to all methods", false),
    
    // Custom security
    CUSTOM_SECURITY("Custom Security", "Custom security annotations", false);
    
    private final String annotationName;
    private final String description;
    private final boolean supportsSpEL;
    
    SecurityEntrypointType(String annotationName, String description, boolean supportsSpEL) {
        this.annotationName = annotationName;
        this.description = description;
        this.supportsSpEL = supportsSpEL;
    }
    
    public String getAnnotationName() {
        return annotationName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean supportsSpEL() {
        return supportsSpEL;
    }
    
    /**
     * Determines if this security type is a high-risk annotation.
     */
    public boolean isHighRisk() {
        return this == PRE_AUTHORIZE || this == POST_AUTHORIZE;
    }
    
    /**
     * Determines if this security type allows access.
     */
    public boolean isPermissive() {
        return this == PERMIT_ALL;
    }
    
    /**
     * Determines if this security type denies access.
     */
    public boolean isRestrictive() {
        return this == DENY_ALL;
    }
    
    /**
     * Determines if this is a JSR-250 standard annotation.
     */
    public boolean isJSR250() {
        return this == ROLES_ALLOWED || this == DENY_ALL || this == PERMIT_ALL || this == RUN_AS;
    }
    
    /**
     * Determines if this is a Spring Security annotation.
     */
    public boolean isSpringSecurityAnnotation() {
        return this == PRE_AUTHORIZE || this == POST_AUTHORIZE || this == SECURED;
    }
}