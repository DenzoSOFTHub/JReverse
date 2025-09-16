package it.denzosoft.jreverse.core.model;

import java.util.Objects;

/**
 * Represents a dependency relationship between beans, particularly for @Bean method parameter dependencies.
 */
public class BeanDependencyRelation {

    private final String dependentBeanName;
    private final String dependentBeanClass;
    private final String dependencyBeanName;
    private final String dependencyBeanClass;
    private final DependencyType dependencyType;
    private final String injectionPoint;
    private final String qualifier;
    private final boolean isOptional;
    private final boolean isLazy;

    private BeanDependencyRelation(Builder builder) {
        this.dependentBeanName = builder.dependentBeanName;
        this.dependentBeanClass = builder.dependentBeanClass;
        this.dependencyBeanName = builder.dependencyBeanName;
        this.dependencyBeanClass = builder.dependencyBeanClass;
        this.dependencyType = builder.dependencyType;
        this.injectionPoint = builder.injectionPoint;
        this.qualifier = builder.qualifier;
        this.isOptional = builder.isOptional;
        this.isLazy = builder.isLazy;
    }

    // Getters
    public String getDependentBeanName() { return dependentBeanName; }
    public String getDependentBeanClass() { return dependentBeanClass; }
    public String getDependencyBeanName() { return dependencyBeanName; }
    public String getDependencyBeanClass() { return dependencyBeanClass; }
    public DependencyType getDependencyType() { return dependencyType; }
    public String getInjectionPoint() { return injectionPoint; }
    public String getQualifier() { return qualifier; }
    public boolean isOptional() { return isOptional; }
    public boolean isLazy() { return isLazy; }

    /**
     * Checks if this dependency has a qualifier.
     */
    public boolean hasQualifier() {
        return qualifier != null && !qualifier.trim().isEmpty();
    }

    /**
     * Checks if this is a circular dependency (bean depends on itself).
     */
    public boolean isCircular() {
        return Objects.equals(dependentBeanName, dependencyBeanName) ||
               Objects.equals(dependentBeanClass, dependencyBeanClass);
    }

    /**
     * Gets a human-readable description of this dependency relationship.
     */
    public String getRelationshipDescription() {
        StringBuilder desc = new StringBuilder();
        desc.append(dependentBeanName).append(" -> ").append(dependencyBeanName);

        if (hasQualifier()) {
            desc.append(" (@Qualifier(\"").append(qualifier).append("\"))");
        }

        if (isOptional) {
            desc.append(" (optional)");
        }

        if (isLazy) {
            desc.append(" (lazy)");
        }

        return desc.toString();
    }

    public static Builder builder() {
        return new Builder();
    }

    public enum DependencyType {
        CONSTRUCTOR_PARAMETER("Constructor parameter injection"),
        BEAN_METHOD_PARAMETER("@Bean method parameter injection"),
        FIELD_INJECTION("Field injection (@Autowired)"),
        SETTER_INJECTION("Setter method injection"),
        QUALIFIER_INJECTION("Qualified dependency injection"),
        OPTIONAL_INJECTION("Optional dependency injection");

        private final String description;

        DependencyType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public static class Builder {
        private String dependentBeanName;
        private String dependentBeanClass;
        private String dependencyBeanName;
        private String dependencyBeanClass;
        private DependencyType dependencyType;
        private String injectionPoint;
        private String qualifier;
        private boolean isOptional;
        private boolean isLazy;

        public Builder dependentBeanName(String dependentBeanName) {
            this.dependentBeanName = dependentBeanName;
            return this;
        }

        public Builder dependentBeanClass(String dependentBeanClass) {
            this.dependentBeanClass = dependentBeanClass;
            return this;
        }

        public Builder dependencyBeanName(String dependencyBeanName) {
            this.dependencyBeanName = dependencyBeanName;
            return this;
        }

        public Builder dependencyBeanClass(String dependencyBeanClass) {
            this.dependencyBeanClass = dependencyBeanClass;
            return this;
        }

        public Builder dependencyType(DependencyType dependencyType) {
            this.dependencyType = dependencyType;
            return this;
        }

        public Builder injectionPoint(String injectionPoint) {
            this.injectionPoint = injectionPoint;
            return this;
        }

        public Builder qualifier(String qualifier) {
            this.qualifier = qualifier;
            return this;
        }

        public Builder isOptional(boolean isOptional) {
            this.isOptional = isOptional;
            return this;
        }

        public Builder isLazy(boolean isLazy) {
            this.isLazy = isLazy;
            return this;
        }

        public BeanDependencyRelation build() {
            Objects.requireNonNull(dependentBeanName, "Dependent bean name is required");
            Objects.requireNonNull(dependentBeanClass, "Dependent bean class is required");
            Objects.requireNonNull(dependencyBeanClass, "Dependency bean class is required");
            Objects.requireNonNull(dependencyType, "Dependency type is required");

            return new BeanDependencyRelation(this);
        }
    }

    @Override
    public String toString() {
        return String.format("BeanDependencyRelation{%s -> %s (%s)}",
            dependentBeanName, dependencyBeanName, dependencyType);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BeanDependencyRelation that = (BeanDependencyRelation) obj;
        return Objects.equals(dependentBeanName, that.dependentBeanName) &&
               Objects.equals(dependencyBeanName, that.dependencyBeanName) &&
               dependencyType == that.dependencyType &&
               Objects.equals(injectionPoint, that.injectionPoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dependentBeanName, dependencyBeanName, dependencyType, injectionPoint);
    }
}