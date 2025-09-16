package it.denzosoft.jreverse.core.model;

import java.util.Objects;

/**
 * Information about bean lifecycle hooks including @PostConstruct, @PreDestroy,
 * and @Bean method initMethod/destroyMethod attributes.
 */
public class BeanLifecycleInfo {

    private final String beanName;
    private final String beanClass;
    private final String declaringClass;
    private final LifecycleHookType hookType;
    private final String methodName;
    private final LifecycleHookSource hookSource;
    private final String annotationType;
    private final String parameterTypes;

    private BeanLifecycleInfo(Builder builder) {
        this.beanName = builder.beanName;
        this.beanClass = builder.beanClass;
        this.declaringClass = builder.declaringClass;
        this.hookType = builder.hookType;
        this.methodName = builder.methodName;
        this.hookSource = builder.hookSource;
        this.annotationType = builder.annotationType;
        this.parameterTypes = builder.parameterTypes;
    }

    // Getters
    public String getBeanName() { return beanName; }
    public String getBeanClass() { return beanClass; }
    public String getDeclaringClass() { return declaringClass; }
    public LifecycleHookType getHookType() { return hookType; }
    public String getMethodName() { return methodName; }
    public LifecycleHookSource getHookSource() { return hookSource; }
    public String getAnnotationType() { return annotationType; }
    public String getParameterTypes() { return parameterTypes; }

    /**
     * Gets the full method signature.
     */
    public String getMethodSignature() {
        if (parameterTypes != null && !parameterTypes.trim().isEmpty()) {
            return String.format("%s(%s)", methodName, parameterTypes);
        }
        return String.format("%s()", methodName);
    }

    /**
     * Checks if this lifecycle hook is annotation-based.
     */
    public boolean isAnnotationBased() {
        return hookSource == LifecycleHookSource.ANNOTATION;
    }

    /**
     * Checks if this lifecycle hook is @Bean attribute-based.
     */
    public boolean isBeanAttributeBased() {
        return hookSource == LifecycleHookSource.BEAN_ATTRIBUTE;
    }

    public static Builder builder() {
        return new Builder();
    }

    public enum LifecycleHookType {
        INIT("Initialization hook"),
        DESTROY("Destruction hook");

        private final String description;

        LifecycleHookType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum LifecycleHookSource {
        ANNOTATION("Annotation-based (@PostConstruct, @PreDestroy)"),
        BEAN_ATTRIBUTE("@Bean method attribute (initMethod, destroyMethod)");

        private final String description;

        LifecycleHookSource(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public static class Builder {
        private String beanName;
        private String beanClass;
        private String declaringClass;
        private LifecycleHookType hookType;
        private String methodName;
        private LifecycleHookSource hookSource;
        private String annotationType;
        private String parameterTypes;

        public Builder beanName(String beanName) {
            this.beanName = beanName;
            return this;
        }

        public Builder beanClass(String beanClass) {
            this.beanClass = beanClass;
            return this;
        }

        public Builder declaringClass(String declaringClass) {
            this.declaringClass = declaringClass;
            return this;
        }

        public Builder hookType(LifecycleHookType hookType) {
            this.hookType = hookType;
            return this;
        }

        public Builder methodName(String methodName) {
            this.methodName = methodName;
            return this;
        }

        public Builder hookSource(LifecycleHookSource hookSource) {
            this.hookSource = hookSource;
            return this;
        }

        public Builder annotationType(String annotationType) {
            this.annotationType = annotationType;
            return this;
        }

        public Builder parameterTypes(String parameterTypes) {
            this.parameterTypes = parameterTypes;
            return this;
        }

        public BeanLifecycleInfo build() {
            Objects.requireNonNull(beanName, "Bean name is required");
            Objects.requireNonNull(beanClass, "Bean class is required");
            Objects.requireNonNull(declaringClass, "Declaring class is required");
            Objects.requireNonNull(hookType, "Hook type is required");
            Objects.requireNonNull(methodName, "Method name is required");
            Objects.requireNonNull(hookSource, "Hook source is required");

            return new BeanLifecycleInfo(this);
        }
    }

    @Override
    public String toString() {
        return String.format("BeanLifecycleInfo{bean='%s', method='%s', type=%s, source=%s}",
            beanName, methodName, hookType, hookSource);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BeanLifecycleInfo that = (BeanLifecycleInfo) obj;
        return Objects.equals(beanName, that.beanName) &&
               Objects.equals(methodName, that.methodName) &&
               hookType == that.hookType &&
               hookSource == that.hookSource;
    }

    @Override
    public int hashCode() {
        return Objects.hash(beanName, methodName, hookType, hookSource);
    }
}