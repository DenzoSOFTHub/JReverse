package it.denzosoft.jreverse.core.model;

/**
 * Information about Spring Boot @Value annotation property usage.
 */
public class PropertyUsageInfo {
    
    public enum InjectionTarget {
        FIELD,
        CONSTRUCTOR_PARAMETER,
        METHOD_PARAMETER,
        SETTER_METHOD
    }
    
    private final String propertyKey;
    private final String defaultValue;
    private final String targetClass;
    private final String targetMember;
    private final InjectionTarget injectionTarget;
    private final String targetType;
    private final boolean hasSpELExpression;
    private final String fullExpression;
    
    private PropertyUsageInfo(String propertyKey,
                             String defaultValue,
                             String targetClass,
                             String targetMember,
                             InjectionTarget injectionTarget,
                             String targetType,
                             boolean hasSpELExpression,
                             String fullExpression) {
        this.propertyKey = propertyKey;
        this.defaultValue = defaultValue;
        this.targetClass = targetClass;
        this.targetMember = targetMember;
        this.injectionTarget = injectionTarget;
        this.targetType = targetType;
        this.hasSpELExpression = hasSpELExpression;
        this.fullExpression = fullExpression;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public String getPropertyKey() {
        return propertyKey;
    }
    
    public String getDefaultValue() {
        return defaultValue;
    }
    
    public String getTargetClass() {
        return targetClass;
    }
    
    public String getTargetMember() {
        return targetMember;
    }
    
    public InjectionTarget getInjectionTarget() {
        return injectionTarget;
    }
    
    public String getTargetType() {
        return targetType;
    }
    
    public boolean hasSpELExpression() {
        return hasSpELExpression;
    }
    
    public String getFullExpression() {
        return fullExpression;
    }
    
    public boolean hasDefaultValue() {
        return defaultValue != null && !defaultValue.isEmpty();
    }
    
    @Override
    public String toString() {
        return "PropertyUsageInfo{" +
                "propertyKey='" + propertyKey + '\'' +
                ", targetClass='" + targetClass + '\'' +
                ", targetMember='" + targetMember + '\'' +
                ", injectionTarget=" + injectionTarget +
                ", hasDefaultValue=" + hasDefaultValue() +
                ", hasSpELExpression=" + hasSpELExpression +
                '}';
    }
    
    public static class Builder {
        private String propertyKey;
        private String defaultValue;
        private String targetClass;
        private String targetMember;
        private InjectionTarget injectionTarget = InjectionTarget.FIELD;
        private String targetType;
        private boolean hasSpELExpression = false;
        private String fullExpression;
        
        public Builder propertyKey(String propertyKey) {
            this.propertyKey = propertyKey;
            return this;
        }
        
        public Builder defaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }
        
        public Builder targetClass(String targetClass) {
            this.targetClass = targetClass;
            return this;
        }
        
        public Builder targetMember(String targetMember) {
            this.targetMember = targetMember;
            return this;
        }
        
        public Builder injectionTarget(InjectionTarget injectionTarget) {
            this.injectionTarget = injectionTarget != null ? injectionTarget : InjectionTarget.FIELD;
            return this;
        }
        
        public Builder targetType(String targetType) {
            this.targetType = targetType;
            return this;
        }
        
        public Builder hasSpELExpression(boolean hasSpELExpression) {
            this.hasSpELExpression = hasSpELExpression;
            return this;
        }
        
        public Builder fullExpression(String fullExpression) {
            this.fullExpression = fullExpression;
            return this;
        }
        
        public PropertyUsageInfo build() {
            return new PropertyUsageInfo(propertyKey, defaultValue, targetClass, 
                                       targetMember, injectionTarget, targetType, 
                                       hasSpELExpression, fullExpression);
        }
    }
}