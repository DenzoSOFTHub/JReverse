package it.denzosoft.jreverse.analyzer.circulardependency;

import it.denzosoft.jreverse.analyzer.beancreation.BeanDependency;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Contains comprehensive information about a Spring component including
 * its dependencies, injection patterns, and Spring-specific characteristics.
 *
 * @author JReverse Development Team
 * @since 1.1.0 (Phase 3 - T3.3.1)
 */
public class SpringComponentInfo {

    private String className;
    private SpringComponentType componentType;
    private List<BeanDependency> dependencies = new ArrayList<>();
    private List<String> errors = new ArrayList<>();

    // Spring-specific characteristics
    private boolean isLazyInitialized = false;
    private boolean isPrimary = false;
    private boolean hasConstructorInjection = false;
    private boolean hasFieldInjection = false;
    private boolean hasMethodInjection = false;
    private boolean hasLazyDependencies = false;

    // Bean scope information
    private String scope = "singleton"; // Default Spring scope

    // Configuration details
    private String beanName;
    private List<String> qualifiers = new ArrayList<>();

    public SpringComponentInfo() {
    }

    public SpringComponentInfo(String className, SpringComponentType componentType) {
        this.className = Objects.requireNonNull(className, "Class name cannot be null");
        this.componentType = Objects.requireNonNull(componentType, "Component type cannot be null");
    }

    // Getters and Setters
    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public SpringComponentType getComponentType() {
        return componentType;
    }

    public void setComponentType(SpringComponentType componentType) {
        this.componentType = componentType;
    }

    public List<BeanDependency> getDependencies() {
        return new ArrayList<>(dependencies);
    }

    public void setDependencies(List<BeanDependency> dependencies) {
        this.dependencies = dependencies != null ? new ArrayList<>(dependencies) : new ArrayList<>();
    }

    public void addDependency(BeanDependency dependency) {
        if (dependency != null) {
            this.dependencies.add(dependency);
        }
    }

    public List<String> getErrors() {
        return new ArrayList<>(errors);
    }

    public void setErrors(List<String> errors) {
        this.errors = errors != null ? new ArrayList<>(errors) : new ArrayList<>();
    }

    public void addError(String error) {
        if (error != null && !error.trim().isEmpty()) {
            this.errors.add(error.trim());
        }
    }

    public boolean isLazyInitialized() {
        return isLazyInitialized;
    }

    public void setLazyInitialized(boolean lazyInitialized) {
        isLazyInitialized = lazyInitialized;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public void setPrimary(boolean primary) {
        isPrimary = primary;
    }

    public boolean isHasConstructorInjection() {
        return hasConstructorInjection;
    }

    public void setHasConstructorInjection(boolean hasConstructorInjection) {
        this.hasConstructorInjection = hasConstructorInjection;
    }

    public boolean isHasFieldInjection() {
        return hasFieldInjection;
    }

    public void setHasFieldInjection(boolean hasFieldInjection) {
        this.hasFieldInjection = hasFieldInjection;
    }

    public boolean isHasMethodInjection() {
        return hasMethodInjection;
    }

    public void setHasMethodInjection(boolean hasMethodInjection) {
        this.hasMethodInjection = hasMethodInjection;
    }

    public boolean isHasLazyDependencies() {
        return hasLazyDependencies;
    }

    public void setHasLazyDependencies(boolean hasLazyDependencies) {
        this.hasLazyDependencies = hasLazyDependencies;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope != null ? scope : "singleton";
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public List<String> getQualifiers() {
        return new ArrayList<>(qualifiers);
    }

    public void setQualifiers(List<String> qualifiers) {
        this.qualifiers = qualifiers != null ? new ArrayList<>(qualifiers) : new ArrayList<>();
    }

    public void addQualifier(String qualifier) {
        if (qualifier != null && !qualifier.trim().isEmpty() && !this.qualifiers.contains(qualifier.trim())) {
            this.qualifiers.add(qualifier.trim());
        }
    }

    // Utility methods

    /**
     * Gets the simple class name without package.
     */
    public String getSimpleClassName() {
        if (className == null) return "";
        int lastDot = className.lastIndexOf('.');
        return lastDot != -1 ? className.substring(lastDot + 1) : className;
    }

    /**
     * Checks if this component has any dependency injection.
     */
    public boolean hasDependencyInjection() {
        return hasConstructorInjection || hasFieldInjection || hasMethodInjection;
    }

    /**
     * Checks if this component uses multiple injection types.
     */
    public boolean usesMixedInjectionTypes() {
        int injectionTypeCount = 0;
        if (hasConstructorInjection) injectionTypeCount++;
        if (hasFieldInjection) injectionTypeCount++;
        if (hasMethodInjection) injectionTypeCount++;
        return injectionTypeCount > 1;
    }

    /**
     * Gets the number of dependencies this component has.
     */
    public int getDependencyCount() {
        return dependencies.size();
    }

    /**
     * Checks if this component has any analysis errors.
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Checks if this component is singleton scoped.
     */
    public boolean isSingleton() {
        return "singleton".equals(scope);
    }

    /**
     * Checks if this component is prototype scoped.
     */
    public boolean isPrototype() {
        return "prototype".equals(scope);
    }

    /**
     * Gets a summary description of this Spring component.
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(componentType.getDisplayName())
               .append(" component with ")
               .append(dependencies.size())
               .append(" dependencies");

        if (isLazyInitialized) {
            summary.append(" (lazy)");
        }
        if (isPrimary) {
            summary.append(" (primary)");
        }
        if (!isSingleton()) {
            summary.append(" (").append(scope).append(")");
        }

        return summary.toString();
    }

    @Override
    public String toString() {
        return String.format("SpringComponentInfo{className='%s', type=%s, dependencies=%d, lazy=%s, primary=%s}",
            className, componentType, dependencies.size(), isLazyInitialized, isPrimary);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SpringComponentInfo that = (SpringComponentInfo) obj;
        return Objects.equals(className, that.className);
    }

    @Override
    public int hashCode() {
        return Objects.hash(className);
    }
}