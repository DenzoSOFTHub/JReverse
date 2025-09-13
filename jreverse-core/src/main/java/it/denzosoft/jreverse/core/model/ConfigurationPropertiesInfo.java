package it.denzosoft.jreverse.core.model;

import java.util.Collections;
import java.util.List;

/**
 * Information about Spring Boot @ConfigurationProperties usage.
 */
public class ConfigurationPropertiesInfo {
    
    public enum BindingType {
        CLASS_LEVEL,
        METHOD_LEVEL,
        CONSTRUCTOR_BINDING
    }
    
    private final String prefix;
    private final String targetClass;
    private final BindingType bindingType;
    private final List<PropertyFieldInfo> properties;
    private final boolean ignoreInvalidFields;
    private final boolean ignoreUnknownFields;
    private final boolean isValidated;
    private final List<String> validationGroups;
    
    private ConfigurationPropertiesInfo(String prefix,
                                       String targetClass,
                                       BindingType bindingType,
                                       List<PropertyFieldInfo> properties,
                                       boolean ignoreInvalidFields,
                                       boolean ignoreUnknownFields,
                                       boolean isValidated,
                                       List<String> validationGroups) {
        this.prefix = prefix;
        this.targetClass = targetClass;
        this.bindingType = bindingType;
        this.properties = Collections.unmodifiableList(properties);
        this.ignoreInvalidFields = ignoreInvalidFields;
        this.ignoreUnknownFields = ignoreUnknownFields;
        this.isValidated = isValidated;
        this.validationGroups = Collections.unmodifiableList(validationGroups);
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public String getPrefix() {
        return prefix;
    }
    
    public String getTargetClass() {
        return targetClass;
    }
    
    public BindingType getBindingType() {
        return bindingType;
    }
    
    public List<PropertyFieldInfo> getProperties() {
        return properties;
    }
    
    public boolean isIgnoreInvalidFields() {
        return ignoreInvalidFields;
    }
    
    public boolean isIgnoreUnknownFields() {
        return ignoreUnknownFields;
    }
    
    public boolean isValidated() {
        return isValidated;
    }
    
    public List<String> getValidationGroups() {
        return validationGroups;
    }
    
    public int getPropertyCount() {
        return properties.size();
    }
    
    @Override
    public String toString() {
        return "ConfigurationPropertiesInfo{" +
                "prefix='" + prefix + '\'' +
                ", targetClass='" + targetClass + '\'' +
                ", bindingType=" + bindingType +
                ", properties=" + properties.size() +
                ", isValidated=" + isValidated +
                '}';
    }
    
    public static class Builder {
        private String prefix;
        private String targetClass;
        private BindingType bindingType = BindingType.CLASS_LEVEL;
        private List<PropertyFieldInfo> properties = Collections.emptyList();
        private boolean ignoreInvalidFields = false;
        private boolean ignoreUnknownFields = true;
        private boolean isValidated = false;
        private List<String> validationGroups = Collections.emptyList();
        
        public Builder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }
        
        public Builder targetClass(String targetClass) {
            this.targetClass = targetClass;
            return this;
        }
        
        public Builder bindingType(BindingType bindingType) {
            this.bindingType = bindingType != null ? bindingType : BindingType.CLASS_LEVEL;
            return this;
        }
        
        public Builder properties(List<PropertyFieldInfo> properties) {
            this.properties = properties != null ? properties : Collections.emptyList();
            return this;
        }
        
        public Builder ignoreInvalidFields(boolean ignoreInvalidFields) {
            this.ignoreInvalidFields = ignoreInvalidFields;
            return this;
        }
        
        public Builder ignoreUnknownFields(boolean ignoreUnknownFields) {
            this.ignoreUnknownFields = ignoreUnknownFields;
            return this;
        }
        
        public Builder isValidated(boolean isValidated) {
            this.isValidated = isValidated;
            return this;
        }
        
        public Builder validationGroups(List<String> validationGroups) {
            this.validationGroups = validationGroups != null ? validationGroups : Collections.emptyList();
            return this;
        }
        
        public ConfigurationPropertiesInfo build() {
            return new ConfigurationPropertiesInfo(prefix, targetClass, bindingType, 
                                                  properties, ignoreInvalidFields, ignoreUnknownFields,
                                                  isValidated, validationGroups);
        }
    }
}