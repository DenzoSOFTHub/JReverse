package it.denzosoft.jreverse.analyzer.beandefinition;

import it.denzosoft.jreverse.analyzer.beancreation.BeanScope;

import java.util.*;

/**
 * Information about a @Bean method in a Spring configuration class.
 */
public class BeanMethodInfo {

    private final String beanName;
    private final String methodName;
    private final String returnType;
    private final String declaringClass;
    private final BeanScope scope;
    private final List<String> dependencies;
    private final Set<String> lifecycleAnnotations;
    private final boolean isPrimary;
    private final Set<String> profiles;

    private BeanMethodInfo(String beanName,
                          String methodName,
                          String returnType,
                          String declaringClass,
                          BeanScope scope,
                          List<String> dependencies,
                          Set<String> lifecycleAnnotations,
                          boolean isPrimary,
                          Set<String> profiles) {
        this.beanName = beanName;
        this.methodName = methodName;
        this.returnType = returnType;
        this.declaringClass = declaringClass;
        this.scope = scope;
        this.dependencies = dependencies != null ? List.copyOf(dependencies) : List.of();
        this.lifecycleAnnotations = lifecycleAnnotations != null ? Set.copyOf(lifecycleAnnotations) : Set.of();
        this.isPrimary = isPrimary;
        this.profiles = profiles != null ? Set.copyOf(profiles) : Set.of();
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public String getBeanName() { return beanName; }
    public String getMethodName() { return methodName; }
    public String getReturnType() { return returnType; }
    public String getDeclaringClass() { return declaringClass; }
    public BeanScope getScope() { return scope; }
    public List<String> getDependencies() { return dependencies; }
    public Set<String> getLifecycleAnnotations() { return lifecycleAnnotations; }
    public boolean isPrimary() { return isPrimary; }
    public Set<String> getProfiles() { return profiles; }

    public static class Builder {
        private String beanName;
        private String methodName;
        private String returnType;
        private String declaringClass;
        private BeanScope scope = BeanScope.SINGLETON;
        private List<String> dependencies = new ArrayList<>();
        private Set<String> lifecycleAnnotations = new HashSet<>();
        private boolean isPrimary = false;
        private Set<String> profiles = new HashSet<>();

        public Builder beanName(String beanName) {
            this.beanName = beanName;
            return this;
        }

        public Builder methodName(String methodName) {
            this.methodName = methodName;
            return this;
        }

        public Builder returnType(String returnType) {
            this.returnType = returnType;
            return this;
        }

        public Builder declaringClass(String declaringClass) {
            this.declaringClass = declaringClass;
            return this;
        }

        public Builder scope(BeanScope scope) {
            this.scope = scope;
            return this;
        }

        public Builder dependencies(List<String> dependencies) {
            this.dependencies = dependencies;
            return this;
        }

        public Builder lifecycleAnnotations(Set<String> lifecycleAnnotations) {
            this.lifecycleAnnotations = lifecycleAnnotations;
            return this;
        }

        public Builder isPrimary(boolean isPrimary) {
            this.isPrimary = isPrimary;
            return this;
        }

        public Builder profiles(Set<String> profiles) {
            this.profiles = profiles;
            return this;
        }

        public BeanMethodInfo build() {
            return new BeanMethodInfo(
                beanName,
                methodName,
                returnType,
                declaringClass,
                scope,
                dependencies,
                lifecycleAnnotations,
                isPrimary,
                profiles
            );
        }
    }
}