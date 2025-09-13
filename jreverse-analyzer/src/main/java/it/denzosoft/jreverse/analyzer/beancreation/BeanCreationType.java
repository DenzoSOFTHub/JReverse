package it.denzosoft.jreverse.analyzer.beancreation;

/**
 * Enumeration of different ways Spring beans can be created.
 */
public enum BeanCreationType {
    
    /**
     * Bean created through @Bean method in a @Configuration class.
     */
    BEAN_METHOD("@Bean method"),
    
    /**
     * Bean created through @Component annotation.
     */
    COMPONENT("@Component"),
    
    /**
     * Bean created through @Service annotation.
     */
    SERVICE("@Service"),
    
    /**
     * Bean created through @Repository annotation.
     */
    REPOSITORY("@Repository"),
    
    /**
     * Bean created through @Controller annotation.
     */
    CONTROLLER("@Controller"),
    
    /**
     * Bean created through @RestController annotation.
     */
    REST_CONTROLLER("@RestController"),
    
    /**
     * Configuration class itself (annotated with @Configuration).
     */
    CONFIGURATION("@Configuration"),
    
    /**
     * Bean created through custom stereotype annotation.
     */
    CUSTOM_STEREOTYPE("Custom stereotype");
    
    private final String description;
    
    BeanCreationType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Determines the bean creation type from a Spring annotation.
     * 
     * @param annotationType the fully qualified annotation type name
     * @return the corresponding BeanCreationType, or null if not a known bean creation annotation
     */
    public static BeanCreationType fromAnnotation(String annotationType) {
        if (annotationType == null) {
            return null;
        }
        
        switch (annotationType) {
            case "org.springframework.context.annotation.Bean":
                return BEAN_METHOD;
            case "org.springframework.stereotype.Component":
                return COMPONENT;
            case "org.springframework.stereotype.Service":
                return SERVICE;
            case "org.springframework.stereotype.Repository":
                return REPOSITORY;
            case "org.springframework.stereotype.Controller":
                return CONTROLLER;
            case "org.springframework.web.bind.annotation.RestController":
                return REST_CONTROLLER;
            case "org.springframework.context.annotation.Configuration":
                return CONFIGURATION;
            default:
                // Check if it's a meta-annotated stereotype
                if (annotationType.contains("Component") || 
                    annotationType.contains("Service") || 
                    annotationType.contains("Repository") || 
                    annotationType.contains("Controller")) {
                    return CUSTOM_STEREOTYPE;
                }
                return null;
        }
    }
    
    /**
     * Checks if this creation type represents a stereotype annotation.
     */
    public boolean isStereotype() {
        return this != BEAN_METHOD;
    }
    
    /**
     * Checks if this creation type represents a web component.
     */
    public boolean isWebComponent() {
        return this == CONTROLLER || this == REST_CONTROLLER;
    }
    
    /**
     * Checks if this creation type represents a data access component.
     */
    public boolean isDataAccessComponent() {
        return this == REPOSITORY;
    }
    
    /**
     * Checks if this creation type represents a service layer component.
     */
    public boolean isServiceComponent() {
        return this == SERVICE;
    }
}