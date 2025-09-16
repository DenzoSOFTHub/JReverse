package it.denzosoft.jreverse.analyzer.beandefinition;

import java.util.List;
import java.util.Set;

/**
 * Legacy information about a single bean definition (for test compatibility).
 */
public class BeanDefinitionInfo {

    public enum BeanScope {
        SINGLETON("singleton"),
        PROTOTYPE("prototype"),
        REQUEST("request"),
        SESSION("session"),
        CUSTOM("custom");

        private final String scopeName;

        BeanScope(String scopeName) {
            this.scopeName = scopeName;
        }

        public String getScopeName() {
            return scopeName;
        }

        public static BeanScope fromString(String scope) {
            if (scope == null) return SINGLETON;
            switch (scope.toLowerCase()) {
                case "prototype": return PROTOTYPE;
                case "request": return REQUEST;
                case "session": return SESSION;
                case "singleton":
                default: return SINGLETON;
            }
        }
    }

    private final String beanName;
    private final String beanClass;
    private final BeanScope scope;
    private final boolean isPrimary;
    private final Set<String> profiles;
    private final List<String> dependencies;
    private final boolean isLazy;
    private final String declaringClass;
    private final String factoryMethod;
    private final boolean isFactoryBean;
    private final String initMethod;
    private final String destroyMethod;
    private final boolean hasLifecycleCallbacks;

    private BeanDefinitionInfo(String beanName, String beanClass, BeanScope scope,
                              boolean isPrimary, Set<String> profiles, List<String> dependencies,
                              boolean isLazy, String declaringClass, String factoryMethod,
                              boolean isFactoryBean, String initMethod, String destroyMethod,
                              boolean hasLifecycleCallbacks) {
        this.beanName = beanName;
        this.beanClass = beanClass;
        this.scope = scope != null ? scope : BeanScope.SINGLETON;
        this.isPrimary = isPrimary;
        this.profiles = profiles != null ? Set.copyOf(profiles) : Set.of();
        this.dependencies = dependencies != null ? List.copyOf(dependencies) : List.of();
        this.isLazy = isLazy;
        this.declaringClass = declaringClass;
        this.factoryMethod = factoryMethod;
        this.isFactoryBean = isFactoryBean;
        this.initMethod = initMethod;
        this.destroyMethod = destroyMethod;
        this.hasLifecycleCallbacks = hasLifecycleCallbacks;
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public String getBeanName() { return beanName; }
    public String getBeanClass() { return beanClass; }
    public BeanScope getScope() { return scope; }
    public boolean isPrimary() { return isPrimary; }
    public Set<String> getProfiles() { return profiles; }
    public List<String> getDependencies() { return dependencies; }
    public boolean isLazy() { return isLazy; }
    public String getDeclaringClass() { return declaringClass; }
    public String getFactoryMethod() { return factoryMethod; }
    public boolean isFactoryBean() { return isFactoryBean; }
    public String getInitMethod() { return initMethod; }
    public String getDestroyMethod() { return destroyMethod; }
    public boolean hasLifecycleCallbacks() { return hasLifecycleCallbacks; }

    public static class Builder {
        private String beanName;
        private String beanClass;
        private BeanScope scope = BeanScope.SINGLETON;
        private boolean isPrimary = false;
        private Set<String> profiles = Set.of();
        private List<String> dependencies = List.of();
        private boolean isLazy = false;
        private String declaringClass;
        private String factoryMethod;
        private boolean isFactoryBean = false;
        private String initMethod;
        private String destroyMethod;
        private boolean hasLifecycleCallbacks = false;

        public Builder beanName(String beanName) {
            this.beanName = beanName;
            return this;
        }

        public Builder beanClass(String beanClass) {
            this.beanClass = beanClass;
            return this;
        }

        public Builder scope(String scope) {
            this.scope = BeanScope.fromString(scope);
            return this;
        }

        public Builder scope(BeanScope scope) {
            this.scope = scope;
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

        public Builder dependencies(List<String> dependencies) {
            this.dependencies = dependencies;
            return this;
        }

        public Builder isLazy(boolean isLazy) {
            this.isLazy = isLazy;
            return this;
        }

        public Builder declaringClass(String declaringClass) {
            this.declaringClass = declaringClass;
            return this;
        }

        public Builder factoryMethod(String factoryMethod) {
            this.factoryMethod = factoryMethod;
            return this;
        }

        public Builder isFactoryBean(boolean isFactoryBean) {
            this.isFactoryBean = isFactoryBean;
            return this;
        }

        public Builder initMethod(String initMethod) {
            this.initMethod = initMethod;
            return this;
        }

        public Builder destroyMethod(String destroyMethod) {
            this.destroyMethod = destroyMethod;
            return this;
        }

        public Builder hasLifecycleCallbacks(boolean hasLifecycleCallbacks) {
            this.hasLifecycleCallbacks = hasLifecycleCallbacks;
            return this;
        }

        public BeanDefinitionInfo build() {
            return new BeanDefinitionInfo(beanName, beanClass, scope, isPrimary, profiles, dependencies,
                isLazy, declaringClass, factoryMethod, isFactoryBean, initMethod, destroyMethod, hasLifecycleCallbacks);
        }
    }
}