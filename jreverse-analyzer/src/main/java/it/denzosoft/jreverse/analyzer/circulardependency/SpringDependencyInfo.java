package it.denzosoft.jreverse.analyzer.circulardependency;

import it.denzosoft.jreverse.analyzer.beancreation.DependencyInjectionType;

import java.util.Objects;

/**
 * Represents detailed information about a specific dependency injection
 * within a Spring circular dependency cycle.
 *
 * @author JReverse Development Team
 * @since 1.1.0 (Phase 3 - T3.3.1)
 */
public class SpringDependencyInfo {

    private String sourceClass;
    private String targetClass;
    private DependencyInjectionType injectionType;
    private String injectionPoint; // field name, method name, or parameter name
    private boolean isRequired = true;
    private String qualifier;
    private boolean isLazy = false;
    private boolean isPrimary = false;

    // Additional context information
    private String sourceComponentType;
    private String targetComponentType;
    private int parameterIndex = -1; // for constructor/method parameters

    public SpringDependencyInfo() {
    }

    public SpringDependencyInfo(String sourceClass, String targetClass, DependencyInjectionType injectionType) {
        this.sourceClass = sourceClass;
        this.targetClass = targetClass;
        this.injectionType = injectionType;
    }

    // Getters and Setters
    public String getSourceClass() {
        return sourceClass;
    }

    public void setSourceClass(String sourceClass) {
        this.sourceClass = sourceClass;
    }

    public String getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(String targetClass) {
        this.targetClass = targetClass;
    }

    public DependencyInjectionType getInjectionType() {
        return injectionType;
    }

    public void setInjectionType(DependencyInjectionType injectionType) {
        this.injectionType = injectionType;
    }

    public String getInjectionPoint() {
        return injectionPoint;
    }

    public void setInjectionPoint(String injectionPoint) {
        this.injectionPoint = injectionPoint;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public void setRequired(boolean required) {
        isRequired = required;
    }

    public String getQualifier() {
        return qualifier;
    }

    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
    }

    public boolean isLazy() {
        return isLazy;
    }

    public void setLazy(boolean lazy) {
        isLazy = lazy;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public void setPrimary(boolean primary) {
        isPrimary = primary;
    }

    public String getSourceComponentType() {
        return sourceComponentType;
    }

    public void setSourceComponentType(String sourceComponentType) {
        this.sourceComponentType = sourceComponentType;
    }

    public String getTargetComponentType() {
        return targetComponentType;
    }

    public void setTargetComponentType(String targetComponentType) {
        this.targetComponentType = targetComponentType;
    }

    public int getParameterIndex() {
        return parameterIndex;
    }

    public void setParameterIndex(int parameterIndex) {
        this.parameterIndex = parameterIndex;
    }

    // Utility methods

    /**
     * Gets simple class name for source class.
     */
    public String getSourceSimpleName() {
        return getSimpleClassName(sourceClass);
    }

    /**
     * Gets simple class name for target class.
     */
    public String getTargetSimpleName() {
        return getSimpleClassName(targetClass);
    }

    /**
     * Checks if this dependency has a qualifier.
     */
    public boolean hasQualifier() {
        return qualifier != null && !qualifier.trim().isEmpty();
    }

    /**
     * Checks if this dependency is constructor-based.
     */
    public boolean isConstructorInjection() {
        return injectionType == DependencyInjectionType.CONSTRUCTOR;
    }

    /**
     * Checks if this dependency is field-based.
     */
    public boolean isFieldInjection() {
        return injectionType == DependencyInjectionType.FIELD;
    }

    /**
     * Checks if this dependency is method-based.
     */
    public boolean isMethodInjection() {
        return injectionType == DependencyInjectionType.SETTER ||
               injectionType == DependencyInjectionType.METHOD;
    }

    /**
     * Gets the severity level of this dependency in a circular context.
     */
    public String getSeverityInCircularContext() {
        if (isFieldInjection() && !isLazy) {
            return "CRITICAL - Field injection without @Lazy";
        } else if (isConstructorInjection() && !isLazy) {
            return "HIGH - Constructor injection without @Lazy";
        } else if (isMethodInjection() && !isLazy) {
            return "MEDIUM - Method injection without @Lazy";
        } else if (isLazy) {
            return "LOW - Resolved with @Lazy";
        } else {
            return "MEDIUM - Requires analysis";
        }
    }

    /**
     * Gets recommended resolution for this dependency in a circular context.
     */
    public String getRecommendedResolution() {
        if (isConstructorInjection() && !isLazy) {
            return "Add @Lazy annotation to constructor parameter";
        } else if (isFieldInjection()) {
            return "Convert to constructor injection with @Lazy annotation";
        } else if (isMethodInjection()) {
            return "Consider constructor injection with @Lazy for better practices";
        } else if (isLazy) {
            return "Already resolved with @Lazy";
        } else {
            return "Analyze specific injection pattern";
        }
    }

    /**
     * Checks if this dependency can break a circular dependency cycle.
     */
    public boolean canBreakCircularDependency() {
        return isLazy || isMethodInjection() || !isRequired;
    }

    /**
     * Gets a human-readable description of this dependency.
     */
    public String getDescription() {
        StringBuilder desc = new StringBuilder();
        desc.append(getSourceSimpleName())
            .append(" -> ")
            .append(getTargetSimpleName());

        if (injectionType != null) {
            desc.append(" [").append(injectionType.getDescription()).append("]");
        }

        if (injectionPoint != null) {
            desc.append(" (").append(injectionPoint);
            if (parameterIndex >= 0) {
                desc.append("[").append(parameterIndex).append("]");
            }
            desc.append(")");
        }

        if (hasQualifier()) {
            desc.append(" @Qualifier(\"").append(qualifier).append("\")");
        }

        if (isLazy) {
            desc.append(" @Lazy");
        }

        if (isPrimary) {
            desc.append(" @Primary");
        }

        if (!isRequired) {
            desc.append(" [Optional]");
        }

        return desc.toString();
    }

    /**
     * Gets the injection location description.
     */
    public String getInjectionLocationDescription() {
        if (injectionPoint == null) {
            return "Unknown location";
        }

        StringBuilder location = new StringBuilder();
        if (isConstructorInjection()) {
            location.append("Constructor parameter");
        } else if (isFieldInjection()) {
            location.append("Field");
        } else if (isMethodInjection()) {
            location.append("Method parameter");
        } else {
            location.append("Unknown injection type");
        }

        location.append(": ").append(injectionPoint);

        if (parameterIndex >= 0) {
            location.append(" (parameter ").append(parameterIndex).append(")");
        }

        return location.toString();
    }

    private String getSimpleClassName(String fullyQualifiedName) {
        if (fullyQualifiedName == null) return "";
        int lastDot = fullyQualifiedName.lastIndexOf('.');
        return lastDot != -1 ? fullyQualifiedName.substring(lastDot + 1) : fullyQualifiedName;
    }

    @Override
    public String toString() {
        return String.format("SpringDependencyInfo{%s -> %s [%s] at %s}",
            getSourceSimpleName(), getTargetSimpleName(),
            injectionType != null ? injectionType.name() : "UNKNOWN",
            injectionPoint != null ? injectionPoint : "unknown");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SpringDependencyInfo that = (SpringDependencyInfo) obj;
        return Objects.equals(sourceClass, that.sourceClass) &&
               Objects.equals(targetClass, that.targetClass) &&
               Objects.equals(injectionType, that.injectionType) &&
               Objects.equals(injectionPoint, that.injectionPoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceClass, targetClass, injectionType, injectionPoint);
    }
}