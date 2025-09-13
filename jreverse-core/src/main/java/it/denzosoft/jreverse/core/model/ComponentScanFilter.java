package it.denzosoft.jreverse.core.model;

/**
 * Represents a component scan filter configuration.
 */
public class ComponentScanFilter {
    
    private final ComponentScanFilterType type;
    private final String pattern;
    private final String value;
    
    public ComponentScanFilter(ComponentScanFilterType type, String pattern, String value) {
        this.type = type;
        this.pattern = pattern;
        this.value = value;
    }
    
    public static ComponentScanFilter annotation(String annotationType) {
        return new ComponentScanFilter(ComponentScanFilterType.ANNOTATION, annotationType, annotationType);
    }
    
    public static ComponentScanFilter assignableType(String className) {
        return new ComponentScanFilter(ComponentScanFilterType.ASSIGNABLE_TYPE, className, className);
    }
    
    public static ComponentScanFilter regex(String regexPattern) {
        return new ComponentScanFilter(ComponentScanFilterType.REGEX, regexPattern, regexPattern);
    }
    
    public static ComponentScanFilter aspectj(String aspectjPattern) {
        return new ComponentScanFilter(ComponentScanFilterType.ASPECTJ, aspectjPattern, aspectjPattern);
    }
    
    public static ComponentScanFilter custom(String filterClass) {
        return new ComponentScanFilter(ComponentScanFilterType.CUSTOM, filterClass, filterClass);
    }
    
    public ComponentScanFilterType getType() {
        return type;
    }
    
    public String getPattern() {
        return pattern;
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public String toString() {
        return "ComponentScanFilter{" +
                "type=" + type +
                ", pattern='" + pattern + '\'' +
                '}';
    }
    
    public enum ComponentScanFilterType {
        ANNOTATION("Annotation-based filter"),
        ASSIGNABLE_TYPE("Assignable type filter"),
        REGEX("Regular expression filter"),
        ASPECTJ("AspectJ pattern filter"),
        CUSTOM("Custom filter implementation");
        
        private final String description;
        
        ComponentScanFilterType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}