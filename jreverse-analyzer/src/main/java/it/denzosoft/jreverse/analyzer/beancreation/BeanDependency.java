package it.denzosoft.jreverse.analyzer.beancreation;

import java.util.Objects;

/**
 * Represents a dependency relationship between beans, including injection details.
 */
public class BeanDependency {
    
    private final String type;
    private final String name;
    private final DependencyInjectionType injectionType;
    private final boolean isRequired;
    private final String qualifier;
    private final boolean isCollection;
    private final String injectionPoint; // field name or parameter name
    
    private BeanDependency(Builder builder) {
        this.type = builder.type;
        this.name = builder.name;
        this.injectionType = builder.injectionType;
        this.isRequired = builder.isRequired;
        this.qualifier = builder.qualifier;
        this.isCollection = builder.isCollection;
        this.injectionPoint = builder.injectionPoint;
    }
    
    // Getters
    public String getType() { return type; }
    public String getName() { return name; }
    public DependencyInjectionType getInjectionType() { return injectionType; }
    public boolean isRequired() { return isRequired; }
    public String getQualifier() { return qualifier; }
    public boolean isCollection() { return isCollection; }
    public String getInjectionPoint() { return injectionPoint; }
    
    /**
     * Checks if this dependency has a qualifier.
     */
    public boolean hasQualifier() {
        return qualifier != null && !qualifier.trim().isEmpty();
    }
    
    /**
     * Gets a human-readable description of this dependency.
     */
    public String getDescription() {
        StringBuilder desc = new StringBuilder();
        desc.append(type);
        
        if (hasQualifier()) {
            desc.append(" (").append(qualifier).append(")");
        }
        
        if (isCollection) {
            desc.append(" [Collection]");
        }
        
        if (!isRequired) {
            desc.append(" [Optional]");
        }
        
        return desc.toString();
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String type;
        private String name;
        private DependencyInjectionType injectionType;
        private boolean isRequired = true;
        private String qualifier;
        private boolean isCollection = false;
        private String injectionPoint;
        
        public Builder type(String type) {
            this.type = type;
            return this;
        }
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder injectionType(DependencyInjectionType injectionType) {
            this.injectionType = injectionType;
            return this;
        }
        
        public Builder isRequired(boolean isRequired) {
            this.isRequired = isRequired;
            return this;
        }
        
        public Builder qualifier(String qualifier) {
            this.qualifier = qualifier;
            return this;
        }
        
        public Builder isCollection(boolean isCollection) {
            this.isCollection = isCollection;
            return this;
        }
        
        public Builder injectionPoint(String injectionPoint) {
            this.injectionPoint = injectionPoint;
            return this;
        }
        
        public BeanDependency build() {
            Objects.requireNonNull(type, "Dependency type is required");
            Objects.requireNonNull(injectionType, "Injection type is required");
            
            return new BeanDependency(this);
        }
    }
    
    @Override
    public String toString() {
        return String.format("BeanDependency{type='%s', injectionType=%s, required=%s}",
            type, injectionType, isRequired);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BeanDependency that = (BeanDependency) obj;
        return Objects.equals(type, that.type) &&
               Objects.equals(name, that.name) &&
               Objects.equals(qualifier, that.qualifier) &&
               Objects.equals(injectionPoint, that.injectionPoint);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(type, name, qualifier, injectionPoint);
    }
}