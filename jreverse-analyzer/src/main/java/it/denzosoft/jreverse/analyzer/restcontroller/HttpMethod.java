package it.denzosoft.jreverse.analyzer.restcontroller;

import java.util.Arrays;
import java.util.Optional;

/**
 * Enumeration of HTTP methods supported by Spring MVC request mappings.
 * Provides mapping between Spring annotation names and standard HTTP methods.
 */
public enum HttpMethod {
    
    GET("GET", "GetMapping"),
    POST("POST", "PostMapping"),
    PUT("PUT", "PutMapping"),
    DELETE("DELETE", "DeleteMapping"),
    PATCH("PATCH", "PatchMapping"),
    HEAD("HEAD"),
    OPTIONS("OPTIONS"),
    TRACE("TRACE");
    
    private final String method;
    private final String springAnnotation;
    
    HttpMethod(String method) {
        this.method = method;
        this.springAnnotation = null;
    }
    
    HttpMethod(String method, String springAnnotation) {
        this.method = method;
        this.springAnnotation = springAnnotation;
    }
    
    /**
     * Gets the HTTP method name.
     * @return the standard HTTP method name
     */
    public String getMethod() {
        return method;
    }
    
    /**
     * Gets the corresponding Spring annotation simple name.
     * @return the Spring annotation name, or null if no dedicated annotation exists
     */
    public String getSpringAnnotation() {
        return springAnnotation;
    }
    
    /**
     * Checks if this HTTP method has a dedicated Spring annotation.
     * @return true if a dedicated Spring annotation exists (e.g., @GetMapping)
     */
    public boolean hasSpringAnnotation() {
        return springAnnotation != null;
    }
    
    /**
     * Finds an HTTP method by its standard method name.
     * @param method the HTTP method name
     * @return the corresponding HttpMethod enum, or empty if not found
     */
    public static Optional<HttpMethod> fromMethod(String method) {
        if (method == null || method.trim().isEmpty()) {
            return Optional.empty();
        }
        
        String normalizedMethod = method.trim().toUpperCase();
        return Arrays.stream(values())
                .filter(httpMethod -> httpMethod.method.equals(normalizedMethod))
                .findFirst();
    }
    
    /**
     * Finds an HTTP method by its Spring annotation name.
     * @param annotation the Spring annotation name (simple or full qualified)
     * @return the corresponding HttpMethod enum, or empty if not found
     */
    public static Optional<HttpMethod> fromSpringAnnotation(String annotation) {
        if (annotation == null || annotation.trim().isEmpty()) {
            return Optional.empty();
        }
        
        // Extract simple name if fully qualified
        String simpleName = annotation;
        int lastDotIndex = annotation.lastIndexOf('.');
        if (lastDotIndex >= 0 && lastDotIndex < annotation.length() - 1) {
            simpleName = annotation.substring(lastDotIndex + 1);
        }
        
        final String finalSimpleName = simpleName;
        return Arrays.stream(values())
                .filter(httpMethod -> httpMethod.springAnnotation != null && 
                                    httpMethod.springAnnotation.equals(finalSimpleName))
                .findFirst();
    }
    
    /**
     * Checks if the given annotation name corresponds to a Spring HTTP method annotation.
     * @param annotation the annotation name to check
     * @return true if this is a recognized Spring HTTP method annotation
     */
    public static boolean isSpringHttpAnnotation(String annotation) {
        return fromSpringAnnotation(annotation).isPresent();
    }
    
    /**
     * Checks if the given annotation name is the @RequestMapping annotation.
     * @param annotation the annotation name to check
     * @return true if this is @RequestMapping
     */
    public static boolean isRequestMapping(String annotation) {
        if (annotation == null) {
            return false;
        }
        
        String simpleName = annotation;
        int lastDotIndex = annotation.lastIndexOf('.');
        if (lastDotIndex >= 0 && lastDotIndex < annotation.length() - 1) {
            simpleName = annotation.substring(lastDotIndex + 1);
        }
        
        return "RequestMapping".equals(simpleName);
    }
    
    @Override
    public String toString() {
        return method;
    }
}