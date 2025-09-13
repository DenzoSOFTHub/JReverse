package it.denzosoft.jreverse.core.model;

import java.util.Collections;
import java.util.List;

/**
 * Information about a field in a @ConfigurationProperties class.
 */
public class PropertyFieldInfo {
    
    private final String fieldName;
    private final String fieldType;
    private final String propertyPath;
    private final boolean isRequired;
    private final String defaultValue;
    private final List<String> validationAnnotations;
    private final boolean isNested;
    private final boolean isCollection;
    
    private PropertyFieldInfo(String fieldName,
                             String fieldType,
                             String propertyPath,
                             boolean isRequired,
                             String defaultValue,
                             List<String> validationAnnotations,
                             boolean isNested,
                             boolean isCollection) {
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.propertyPath = propertyPath;
        this.isRequired = isRequired;
        this.defaultValue = defaultValue;
        this.validationAnnotations = Collections.unmodifiableList(validationAnnotations);
        this.isNested = isNested;
        this.isCollection = isCollection;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public String getFieldName() {
        return fieldName;
    }
    
    public String getFieldType() {
        return fieldType;
    }
    
    public String getPropertyPath() {
        return propertyPath;
    }
    
    public boolean isRequired() {
        return isRequired;
    }
    
    public String getDefaultValue() {
        return defaultValue;
    }
    
    public List<String> getValidationAnnotations() {
        return validationAnnotations;
    }
    
    public boolean isNested() {
        return isNested;
    }
    
    public boolean isCollection() {
        return isCollection;
    }
    
    public boolean hasDefaultValue() {
        return defaultValue != null && !defaultValue.isEmpty();
    }
    
    public boolean hasValidation() {
        return !validationAnnotations.isEmpty();
    }
    
    @Override
    public String toString() {
        return "PropertyFieldInfo{" +
                "fieldName='" + fieldName + '\'' +
                ", fieldType='" + fieldType + '\'' +
                ", propertyPath='" + propertyPath + '\'' +
                ", isRequired=" + isRequired +
                ", hasValidation=" + hasValidation() +
                ", isNested=" + isNested +
                '}';
    }
    
    public static class Builder {
        private String fieldName;
        private String fieldType;
        private String propertyPath;
        private boolean isRequired = false;
        private String defaultValue;
        private List<String> validationAnnotations = Collections.emptyList();
        private boolean isNested = false;
        private boolean isCollection = false;
        
        public Builder fieldName(String fieldName) {
            this.fieldName = fieldName;
            return this;
        }
        
        public Builder fieldType(String fieldType) {
            this.fieldType = fieldType;
            return this;
        }
        
        public Builder propertyPath(String propertyPath) {
            this.propertyPath = propertyPath;
            return this;
        }
        
        public Builder isRequired(boolean isRequired) {
            this.isRequired = isRequired;
            return this;
        }
        
        public Builder defaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }
        
        public Builder validationAnnotations(List<String> validationAnnotations) {
            this.validationAnnotations = validationAnnotations != null ? validationAnnotations : Collections.emptyList();
            return this;
        }
        
        public Builder isNested(boolean isNested) {
            this.isNested = isNested;
            return this;
        }
        
        public Builder isCollection(boolean isCollection) {
            this.isCollection = isCollection;
            return this;
        }
        
        public PropertyFieldInfo build() {
            return new PropertyFieldInfo(fieldName, fieldType, propertyPath, 
                                       isRequired, defaultValue, validationAnnotations, 
                                       isNested, isCollection);
        }
    }
}