package it.denzosoft.jreverse.analyzer.restcontroller;

/**
 * Enumeration of Spring MVC parameter binding types.
 * Categorizes different ways method parameters can be bound in REST endpoints.
 */
public enum ParameterType {
    
    /**
     * Parameter bound from query string using @RequestParam annotation.
     */
    REQUEST_PARAM("org.springframework.web.bind.annotation.RequestParam", "Query Parameter"),
    
    /**
     * Parameter bound from URL path variables using @PathVariable annotation.
     */
    PATH_VARIABLE("org.springframework.web.bind.annotation.PathVariable", "Path Variable"),
    
    /**
     * Parameter bound from request body using @RequestBody annotation.
     */
    REQUEST_BODY("org.springframework.web.bind.annotation.RequestBody", "Request Body"),
    
    /**
     * Parameter bound from HTTP headers using @RequestHeader annotation.
     */
    REQUEST_HEADER("org.springframework.web.bind.annotation.RequestHeader", "Request Header"),
    
    /**
     * Parameter bound from cookies using @CookieValue annotation.
     */
    COOKIE_VALUE("org.springframework.web.bind.annotation.CookieValue", "Cookie Value"),
    
    /**
     * Parameter bound from request attributes using @RequestAttribute annotation.
     */
    REQUEST_ATTRIBUTE("org.springframework.web.bind.annotation.RequestAttribute", "Request Attribute"),
    
    /**
     * Parameter bound from session attributes using @SessionAttribute annotation.
     */
    SESSION_ATTRIBUTE("org.springframework.web.bind.annotation.SessionAttribute", "Session Attribute"),
    
    /**
     * Parameter bound from model using @ModelAttribute annotation.
     */
    MODEL_ATTRIBUTE("org.springframework.web.bind.annotation.ModelAttribute", "Model Attribute"),
    
    /**
     * Parameter bound from matrix variables using @MatrixVariable annotation.
     */
    MATRIX_VARIABLE("org.springframework.web.bind.annotation.MatrixVariable", "Matrix Variable"),
    
    /**
     * Parameter bound from request parts (multipart) using @RequestPart annotation.
     */
    REQUEST_PART("org.springframework.web.bind.annotation.RequestPart", "Request Part"),
    
    /**
     * Validation marker for parameters annotated with @Valid.
     */
    VALIDATION("javax.validation.Valid", "Validation"),
    
    /**
     * Parameter without explicit binding annotation (usually model attributes).
     */
    IMPLICIT("", "Implicit Binding"),
    
    /**
     * Standard servlet API parameters (HttpServletRequest, HttpServletResponse, etc.).
     */
    SERVLET_API("", "Servlet API"),
    
    /**
     * Spring security principal parameter.
     */
    PRINCIPAL("", "Security Principal"),
    
    /**
     * Unknown or unsupported parameter binding type.
     */
    UNKNOWN("", "Unknown");
    
    private final String annotationClass;
    private final String displayName;
    
    ParameterType(String annotationClass, String displayName) {
        this.annotationClass = annotationClass;
        this.displayName = displayName;
    }
    
    /**
     * Gets the fully qualified class name of the binding annotation.
     * 
     * @return the annotation class name, empty for special types
     */
    public String getAnnotationClass() {
        return annotationClass;
    }
    
    /**
     * Gets the human-readable display name for this parameter type.
     * 
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Checks if this parameter type requires an explicit annotation.
     * 
     * @return true if annotation is required
     */
    public boolean requiresAnnotation() {
        return !annotationClass.isEmpty() && this != IMPLICIT && this != SERVLET_API && this != PRINCIPAL;
    }
    
    /**
     * Checks if this parameter type represents a validation annotation.
     * 
     * @return true if this is a validation parameter type
     */
    public boolean isValidation() {
        return this == VALIDATION;
    }
    
    /**
     * Checks if this parameter type represents request content (body or part).
     * 
     * @return true if parameter contains request content
     */
    public boolean isRequestContent() {
        return this == REQUEST_BODY || this == REQUEST_PART;
    }
    
    /**
     * Checks if this parameter type represents URL-based binding.
     * 
     * @return true if parameter is bound from URL
     */
    public boolean isUrlBased() {
        return this == PATH_VARIABLE || this == MATRIX_VARIABLE || this == REQUEST_PARAM;
    }
    
    /**
     * Checks if this parameter type represents HTTP header/cookie binding.
     * 
     * @return true if parameter is bound from headers or cookies
     */
    public boolean isHeaderBased() {
        return this == REQUEST_HEADER || this == COOKIE_VALUE;
    }
    
    /**
     * Checks if this parameter type represents server-side binding.
     * 
     * @return true if parameter is bound from server-side sources
     */
    public boolean isServerSideBased() {
        return this == REQUEST_ATTRIBUTE || this == SESSION_ATTRIBUTE || 
               this == SERVLET_API || this == PRINCIPAL;
    }
    
    /**
     * Determines the parameter type from an annotation class name.
     * 
     * @param annotationClass the fully qualified annotation class name
     * @return the corresponding parameter type, UNKNOWN if not recognized
     */
    public static ParameterType fromAnnotation(String annotationClass) {
        if (annotationClass == null || annotationClass.trim().isEmpty()) {
            return UNKNOWN;
        }
        
        for (ParameterType type : values()) {
            if (type.annotationClass.equals(annotationClass)) {
                return type;
            }
        }
        
        return UNKNOWN;
    }
    
    /**
     * Determines the parameter type from parameter type class for implicit binding.
     * 
     * @param parameterTypeClass the parameter class name
     * @return the corresponding parameter type for special classes
     */
    public static ParameterType fromParameterClass(String parameterTypeClass) {
        if (parameterTypeClass == null) {
            return UNKNOWN;
        }
        
        // Servlet API types
        if (parameterTypeClass.equals("javax.servlet.http.HttpServletRequest") ||
            parameterTypeClass.equals("javax.servlet.http.HttpServletResponse") ||
            parameterTypeClass.equals("javax.servlet.ServletRequest") ||
            parameterTypeClass.equals("javax.servlet.ServletResponse") ||
            parameterTypeClass.equals("javax.servlet.http.HttpSession") ||
            parameterTypeClass.equals("javax.servlet.ServletContext")) {
            return SERVLET_API;
        }
        
        // Security principal types
        if (parameterTypeClass.equals("java.security.Principal") ||
            parameterTypeClass.startsWith("org.springframework.security.core") ||
            parameterTypeClass.equals("org.springframework.security.core.Authentication")) {
            return PRINCIPAL;
        }
        
        // Other framework types that are implicitly bound
        if (parameterTypeClass.equals("org.springframework.ui.Model") ||
            parameterTypeClass.equals("org.springframework.ui.ModelMap") ||
            parameterTypeClass.equals("org.springframework.validation.BindingResult") ||
            parameterTypeClass.equals("org.springframework.web.servlet.mvc.support.RedirectAttributes")) {
            return IMPLICIT;
        }
        
        return IMPLICIT; // Default for custom objects without explicit annotations
    }
    
    /**
     * Gets all parameter types that represent explicit Spring binding annotations.
     * 
     * @return array of explicit binding parameter types
     */
    public static ParameterType[] getExplicitBindingTypes() {
        return new ParameterType[] {
            REQUEST_PARAM, PATH_VARIABLE, REQUEST_BODY, REQUEST_HEADER,
            COOKIE_VALUE, REQUEST_ATTRIBUTE, SESSION_ATTRIBUTE, 
            MODEL_ATTRIBUTE, MATRIX_VARIABLE, REQUEST_PART
        };
    }
    
    /**
     * Gets all parameter types that represent validation annotations.
     * 
     * @return array of validation parameter types
     */
    public static ParameterType[] getValidationTypes() {
        return new ParameterType[] { VALIDATION };
    }
}