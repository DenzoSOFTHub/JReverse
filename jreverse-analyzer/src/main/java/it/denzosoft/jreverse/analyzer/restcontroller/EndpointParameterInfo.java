package it.denzosoft.jreverse.analyzer.restcontroller;

import it.denzosoft.jreverse.core.model.AnnotationInfo;
import it.denzosoft.jreverse.core.model.ParameterInfo;

import java.util.*;

/**
 * Immutable value object representing detailed information about a REST endpoint parameter.
 * Contains comprehensive metadata about parameter binding, validation, and constraints
 * extracted from Spring MVC annotations.
 */
public class EndpointParameterInfo {
    
    private final ParameterInfo parameterInfo;
    private final ParameterType parameterType;
    private final String bindingName;
    private final boolean required;
    private final String defaultValue;
    private final Set<String> validationAnnotations;
    private final boolean hasValidation;
    private final String description;
    private final boolean isCollection;
    private final boolean isMap;
    private final String collectionElementType;
    private final String mapKeyType;
    private final String mapValueType;
    private final Set<String> produces;
    private final Set<String> consumes;
    private final List<String> pathVariableNames;
    
    private EndpointParameterInfo(Builder builder) {
        this.parameterInfo = Objects.requireNonNull(builder.parameterInfo, "parameterInfo cannot be null");
        this.parameterType = Objects.requireNonNull(builder.parameterType, "parameterType cannot be null");
        this.bindingName = builder.bindingName;
        this.required = builder.required;
        this.defaultValue = builder.defaultValue;
        this.validationAnnotations = Collections.unmodifiableSet(new LinkedHashSet<>(builder.validationAnnotations));
        this.hasValidation = !this.validationAnnotations.isEmpty() || builder.hasValidation;
        this.description = builder.description;
        this.isCollection = builder.isCollection;
        this.isMap = builder.isMap;
        this.collectionElementType = builder.collectionElementType;
        this.mapKeyType = builder.mapKeyType;
        this.mapValueType = builder.mapValueType;
        this.produces = Collections.unmodifiableSet(new LinkedHashSet<>(builder.produces));
        this.consumes = Collections.unmodifiableSet(new LinkedHashSet<>(builder.consumes));
        this.pathVariableNames = Collections.unmodifiableList(new ArrayList<>(builder.pathVariableNames));
    }
    
    // Core parameter information
    public ParameterInfo getParameterInfo() { return parameterInfo; }
    public ParameterType getParameterType() { return parameterType; }
    public String getBindingName() { return bindingName; }
    public boolean isRequired() { return required; }
    public String getDefaultValue() { return defaultValue; }
    public Set<String> getValidationAnnotations() { return validationAnnotations; }
    public boolean hasValidation() { return hasValidation; }
    public String getDescription() { return description; }
    
    // Type analysis
    public boolean isCollection() { return isCollection; }
    public boolean isMap() { return isMap; }
    public String getCollectionElementType() { return collectionElementType; }
    public String getMapKeyType() { return mapKeyType; }
    public String getMapValueType() { return mapValueType; }
    
    // Content type restrictions
    public Set<String> getProduces() { return produces; }
    public Set<String> getConsumes() { return consumes; }
    public List<String> getPathVariableNames() { return pathVariableNames; }
    
    // Convenience methods from ParameterInfo
    public String getName() { return parameterInfo.getName(); }
    public String getType() { return parameterInfo.getType(); }
    public int getIndex() { return parameterInfo.getIndex(); }
    public Set<AnnotationInfo> getAnnotations() { return parameterInfo.getAnnotations(); }
    public String getDisplayName() { return parameterInfo.getDisplayName(); }
    public String getSimpleType() { return parameterInfo.getSimpleType(); }
    
    /**
     * Gets the effective name used for parameter binding.
     * Returns bindingName if specified, otherwise falls back to parameter name.
     * 
     * @return the effective binding name
     */
    public String getEffectiveName() {
        if (bindingName != null && !bindingName.trim().isEmpty()) {
            return bindingName;
        }
        return parameterInfo.getDisplayName();
    }
    
    /**
     * Checks if this parameter has a custom binding name different from the parameter name.
     * 
     * @return true if binding name is explicitly set and different from parameter name
     */
    public boolean hasCustomBindingName() {
        return bindingName != null && 
               !bindingName.trim().isEmpty() && 
               !bindingName.equals(parameterInfo.getName());
    }
    
    /**
     * Checks if this parameter has a default value specified.
     * 
     * @return true if default value is configured
     */
    public boolean hasDefaultValue() {
        return defaultValue != null && !defaultValue.trim().isEmpty();
    }
    
    /**
     * Checks if this parameter is optional (not required or has default value).
     * 
     * @return true if parameter is optional
     */
    public boolean isOptional() {
        return !required || hasDefaultValue();
    }
    
    /**
     * Checks if this parameter represents complex type binding.
     * 
     * @return true if parameter is collection, map, or custom object
     */
    public boolean isComplexType() {
        return isCollection || isMap || 
               (!parameterInfo.isPrimitive() && 
                !parameterInfo.getType().startsWith("java.lang") &&
                parameterType != ParameterType.SERVLET_API);
    }
    
    /**
     * Checks if this parameter is bound from the request URL (path or query).
     * 
     * @return true if parameter comes from URL
     */
    public boolean isUrlBound() {
        return parameterType.isUrlBased();
    }
    
    /**
     * Checks if this parameter is bound from request headers or cookies.
     * 
     * @return true if parameter comes from headers/cookies
     */
    public boolean isHeaderBound() {
        return parameterType.isHeaderBased();
    }
    
    /**
     * Checks if this parameter is bound from request content (body or parts).
     * 
     * @return true if parameter comes from request content
     */
    public boolean isContentBound() {
        return parameterType.isRequestContent();
    }
    
    /**
     * Checks if this parameter is bound implicitly without explicit annotations.
     * 
     * @return true if parameter uses implicit binding
     */
    public boolean isImplicitlyBound() {
        return parameterType == ParameterType.IMPLICIT || 
               parameterType == ParameterType.SERVLET_API || 
               parameterType == ParameterType.PRINCIPAL;
    }
    
    /**
     * Checks if this parameter requires validation against path variables.
     * 
     * @return true if path variable validation is needed
     */
    public boolean requiresPathVariableValidation() {
        return parameterType == ParameterType.PATH_VARIABLE && !pathVariableNames.isEmpty();
    }
    
    /**
     * Checks if the parameter's binding name matches any of the expected path variables.
     * 
     * @return true if binding name matches a path variable
     */
    public boolean isValidPathVariable() {
        if (parameterType != ParameterType.PATH_VARIABLE) {
            return true; // Not a path variable, no validation needed
        }
        
        if (pathVariableNames.isEmpty()) {
            return true; // No path variables to validate against
        }
        
        String effectiveName = getEffectiveName();
        return pathVariableNames.contains(effectiveName);
    }
    
    /**
     * Gets the validation level for this parameter.
     * 
     * @return validation level based on annotations and constraints
     */
    public ValidationLevel getValidationLevel() {
        if (!hasValidation) {
            return ValidationLevel.NONE;
        }
        
        boolean hasStandardValidation = validationAnnotations.stream()
            .anyMatch(ann -> ann.startsWith("javax.validation") || ann.startsWith("jakarta.validation"));
        boolean hasCustomValidation = validationAnnotations.stream()
            .anyMatch(ann -> !ann.startsWith("javax.validation") && !ann.startsWith("jakarta.validation"));
        
        if (hasStandardValidation && hasCustomValidation) {
            return ValidationLevel.COMPREHENSIVE;
        } else if (hasStandardValidation) {
            return ValidationLevel.STANDARD;
        } else {
            return ValidationLevel.CUSTOM;
        }
    }
    
    /**
     * Enumeration of validation levels for parameters.
     */
    public enum ValidationLevel {
        NONE("No Validation"),
        STANDARD("Standard Validation"),
        CUSTOM("Custom Validation"),
        COMPREHENSIVE("Comprehensive Validation");
        
        private final String description;
        
        ValidationLevel(String description) {
            this.description = description;
        }
        
        public String getDescription() { return description; }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        EndpointParameterInfo that = (EndpointParameterInfo) obj;
        return Objects.equals(parameterInfo, that.parameterInfo) &&
               Objects.equals(parameterType, that.parameterType) &&
               Objects.equals(bindingName, that.bindingName);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(parameterInfo, parameterType, bindingName);
    }
    
    @Override
    public String toString() {
        return "EndpointParameterInfo{" +
                "name='" + getEffectiveName() + '\'' +
                ", type='" + parameterInfo.getType() + '\'' +
                ", parameterType=" + parameterType +
                ", required=" + required +
                ", hasValidation=" + hasValidation +
                ", index=" + parameterInfo.getIndex() +
                '}';
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private ParameterInfo parameterInfo;
        private ParameterType parameterType = ParameterType.UNKNOWN;
        private String bindingName;
        private boolean required = true;
        private String defaultValue;
        private Set<String> validationAnnotations = new LinkedHashSet<>();
        private boolean hasValidation = false;
        private String description;
        private boolean isCollection = false;
        private boolean isMap = false;
        private String collectionElementType;
        private String mapKeyType;
        private String mapValueType;
        private Set<String> produces = new LinkedHashSet<>();
        private Set<String> consumes = new LinkedHashSet<>();
        private List<String> pathVariableNames = new ArrayList<>();
        
        public Builder parameterInfo(ParameterInfo parameterInfo) {
            this.parameterInfo = parameterInfo;
            return this;
        }
        
        public Builder parameterType(ParameterType parameterType) {
            this.parameterType = parameterType;
            return this;
        }
        
        public Builder bindingName(String bindingName) {
            this.bindingName = bindingName;
            return this;
        }
        
        public Builder required(boolean required) {
            this.required = required;
            return this;
        }
        
        public Builder defaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }
        
        public Builder addValidationAnnotation(String validationAnnotation) {
            if (validationAnnotation != null && !validationAnnotation.trim().isEmpty()) {
                this.validationAnnotations.add(validationAnnotation.trim());
            }
            return this;
        }
        
        public Builder validationAnnotations(Set<String> validationAnnotations) {
            this.validationAnnotations = new LinkedHashSet<>(validationAnnotations != null ? validationAnnotations : Collections.emptySet());
            return this;
        }
        
        public Builder hasValidation(boolean hasValidation) {
            this.hasValidation = hasValidation;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder isCollection(boolean isCollection) {
            this.isCollection = isCollection;
            return this;
        }
        
        public Builder isMap(boolean isMap) {
            this.isMap = isMap;
            return this;
        }
        
        public Builder collectionElementType(String collectionElementType) {
            this.collectionElementType = collectionElementType;
            return this;
        }
        
        public Builder mapKeyType(String mapKeyType) {
            this.mapKeyType = mapKeyType;
            return this;
        }
        
        public Builder mapValueType(String mapValueType) {
            this.mapValueType = mapValueType;
            return this;
        }
        
        public Builder addProduces(String contentType) {
            if (contentType != null && !contentType.trim().isEmpty()) {
                this.produces.add(contentType.trim());
            }
            return this;
        }
        
        public Builder produces(Set<String> produces) {
            this.produces = new LinkedHashSet<>(produces != null ? produces : Collections.emptySet());
            return this;
        }
        
        public Builder addConsumes(String contentType) {
            if (contentType != null && !contentType.trim().isEmpty()) {
                this.consumes.add(contentType.trim());
            }
            return this;
        }
        
        public Builder consumes(Set<String> consumes) {
            this.consumes = new LinkedHashSet<>(consumes != null ? consumes : Collections.emptySet());
            return this;
        }
        
        public Builder addPathVariableName(String pathVariableName) {
            if (pathVariableName != null && !pathVariableName.trim().isEmpty()) {
                this.pathVariableNames.add(pathVariableName.trim());
            }
            return this;
        }
        
        public Builder pathVariableNames(List<String> pathVariableNames) {
            this.pathVariableNames = new ArrayList<>(pathVariableNames != null ? pathVariableNames : Collections.emptyList());
            return this;
        }
        
        public EndpointParameterInfo build() {
            return new EndpointParameterInfo(this);
        }
    }
}