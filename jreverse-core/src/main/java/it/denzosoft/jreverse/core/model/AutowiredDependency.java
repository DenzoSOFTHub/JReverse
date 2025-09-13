package it.denzosoft.jreverse.core.model;

import java.util.Objects;

/**
 * Represents an autowired dependency with its injection details.
 */
public class AutowiredDependency {
    
    private final String ownerClass;
    private final String dependencyType;
    private final String dependencyName;
    private final String injectionType;
    private final String injectionPoint;
    private final boolean isRequired;
    private final String qualifier;
    private final boolean isCollection;
    
    private AutowiredDependency(Builder builder) {
        this.ownerClass = builder.ownerClass;
        this.dependencyType = builder.dependencyType;
        this.dependencyName = builder.dependencyName;
        this.injectionType = builder.injectionType;
        this.injectionPoint = builder.injectionPoint;
        this.isRequired = builder.isRequired;
        this.qualifier = builder.qualifier;
        this.isCollection = builder.isCollection;
    }
    
    public String getOwnerClass() {
        return ownerClass;
    }
    
    public String getDependencyType() {
        return dependencyType;
    }
    
    public String getDependencyName() {
        return dependencyName;
    }
    
    public String getInjectionType() {
        return injectionType;
    }
    
    public String getInjectionPoint() {
        return injectionPoint;
    }
    
    public boolean isRequired() {
        return isRequired;
    }
    
    public String getQualifier() {
        return qualifier;
    }
    
    public boolean isCollection() {
        return isCollection;
    }
    
    /**
     * Checks if this dependency has a qualifier.
     */
    public boolean hasQualifier() {
        return qualifier != null && !qualifier.trim().isEmpty();
    }
    
    /**
     * Gets the simple name of the dependency type (without package).
     */
    public String getDependencySimpleName() {
        if (dependencyType == null) return null;
        int lastDot = dependencyType.lastIndexOf('.');
        return lastDot >= 0 ? dependencyType.substring(lastDot + 1) : dependencyType;
    }
    
    /**
     * Gets the simple name of the owner class (without package).
     */
    public String getOwnerSimpleName() {
        if (ownerClass == null) return null;
        int lastDot = ownerClass.lastIndexOf('.');
        return lastDot >= 0 ? ownerClass.substring(lastDot + 1) : ownerClass;
    }
    
    /**
     * Gets a human-readable description of this dependency.
     */
    public String getDescription() {
        StringBuilder desc = new StringBuilder();
        desc.append(getDependencySimpleName());
        
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
        private String ownerClass;
        private String dependencyType;
        private String dependencyName;
        private String injectionType;
        private String injectionPoint;
        private boolean isRequired = true;
        private String qualifier;
        private boolean isCollection = false;
        
        public Builder ownerClass(String ownerClass) {
            this.ownerClass = ownerClass;
            return this;
        }
        
        public Builder dependencyType(String dependencyType) {
            this.dependencyType = dependencyType;
            return this;
        }
        
        public Builder dependencyName(String dependencyName) {
            this.dependencyName = dependencyName;
            return this;
        }
        
        public Builder injectionType(String injectionType) {
            this.injectionType = injectionType;
            return this;
        }
        
        public Builder injectionPoint(String injectionPoint) {
            this.injectionPoint = injectionPoint;
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
        
        public AutowiredDependency build() {
            Objects.requireNonNull(ownerClass, "Owner class is required");
            Objects.requireNonNull(dependencyType, "Dependency type is required");
            Objects.requireNonNull(injectionType, "Injection type is required");
            
            return new AutowiredDependency(this);
        }
    }
    
    @Override
    public String toString() {
        return String.format("AutowiredDependency{owner='%s', type='%s', injection='%s', point='%s'}",
            getOwnerSimpleName(), getDependencySimpleName(), injectionType, injectionPoint);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AutowiredDependency that = (AutowiredDependency) obj;
        return Objects.equals(ownerClass, that.ownerClass) &&
               Objects.equals(dependencyType, that.dependencyType) &&
               Objects.equals(injectionPoint, that.injectionPoint);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(ownerClass, dependencyType, injectionPoint);
    }
}