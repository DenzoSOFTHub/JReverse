package it.denzosoft.jreverse.core.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Information about a Spring bean definition.
 * Captures bean metadata, dependencies, and configuration details.
 */
public class BeanDefinitionInfo {
    
    private final String beanName;
    private final String beanClass;
    private final String declaringClass;
    private final String factoryMethod;
    private final BeanScope scope;
    private final boolean isPrimary;
    private final boolean isLazy;
    private final Set<String> qualifiers;
    private final Set<String> profiles;
    private final String initMethod;
    private final String destroyMethod;
    private final List<String> dependencies;
    
    private BeanDefinitionInfo(String beanName,
                              String beanClass,
                              String declaringClass,
                              String factoryMethod,
                              BeanScope scope,
                              boolean isPrimary,
                              boolean isLazy,
                              Set<String> qualifiers,
                              Set<String> profiles,
                              String initMethod,
                              String destroyMethod,
                              List<String> dependencies) {
        this.beanName = beanName;
        this.beanClass = beanClass;
        this.declaringClass = declaringClass;
        this.factoryMethod = factoryMethod;
        this.scope = scope != null ? scope : BeanScope.SINGLETON;
        this.isPrimary = isPrimary;
        this.isLazy = isLazy;
        this.qualifiers = Collections.unmodifiableSet(new HashSet<>(qualifiers));
        this.profiles = Collections.unmodifiableSet(new HashSet<>(profiles));
        this.initMethod = initMethod;
        this.destroyMethod = destroyMethod;
        this.dependencies = Collections.unmodifiableList(dependencies);
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public String getBeanName() {
        return beanName;
    }
    
    public String getBeanClass() {
        return beanClass;
    }
    
    public String getDeclaringClass() {
        return declaringClass;
    }
    
    public String getFactoryMethod() {
        return factoryMethod;
    }
    
    public BeanScope getScope() {
        return scope;
    }
    
    public boolean isPrimary() {
        return isPrimary;
    }
    
    public boolean isLazy() {
        return isLazy;
    }
    
    public Set<String> getQualifiers() {
        return qualifiers;
    }
    
    public Set<String> getProfiles() {
        return profiles;
    }
    
    public String getInitMethod() {
        return initMethod;
    }
    
    public String getDestroyMethod() {
        return destroyMethod;
    }
    
    public List<String> getDependencies() {
        return dependencies;
    }
    
    public boolean hasQualifier(String qualifier) {
        return qualifiers.contains(qualifier);
    }
    
    public boolean isActiveInProfile(String profile) {
        return profiles.isEmpty() || profiles.contains(profile);
    }
    
    public boolean isFactoryBean() {
        return factoryMethod != null;
    }
    
    public boolean hasLifecycleCallbacks() {
        return initMethod != null || destroyMethod != null;
    }
    
    @Override
    public String toString() {
        return "BeanDefinitionInfo{" +
                "name='" + beanName + '\'' +
                ", class='" + beanClass + '\'' +
                ", scope=" + scope +
                ", primary=" + isPrimary +
                '}';
    }
    
    public enum BeanScope {
        SINGLETON("singleton"),
        PROTOTYPE("prototype"),
        REQUEST("request"),
        SESSION("session"),
        APPLICATION("application"),
        WEBSOCKET("websocket");
        
        private final String value;
        
        BeanScope(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
        
        public static BeanScope fromString(String value) {
            if (value == null) {
                return SINGLETON;
            }
            for (BeanScope scope : values()) {
                if (scope.value.equalsIgnoreCase(value) || scope.name().equalsIgnoreCase(value)) {
                    return scope;
                }
            }
            return SINGLETON;
        }
    }
    
    public static class Builder {
        private String beanName;
        private String beanClass;
        private String declaringClass;
        private String factoryMethod;
        private BeanScope scope = BeanScope.SINGLETON;
        private boolean isPrimary = false;
        private boolean isLazy = false;
        private Set<String> qualifiers = new HashSet<>();
        private Set<String> profiles = new HashSet<>();
        private String initMethod;
        private String destroyMethod;
        private List<String> dependencies = Collections.emptyList();
        
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
        
        public Builder factoryMethod(String factoryMethod) {
            this.factoryMethod = factoryMethod;
            return this;
        }
        
        public Builder scope(BeanScope scope) {
            this.scope = scope;
            return this;
        }
        
        public Builder scope(String scopeValue) {
            this.scope = BeanScope.fromString(scopeValue);
            return this;
        }
        
        public Builder isPrimary(boolean isPrimary) {
            this.isPrimary = isPrimary;
            return this;
        }
        
        public Builder isLazy(boolean isLazy) {
            this.isLazy = isLazy;
            return this;
        }
        
        public Builder addQualifier(String qualifier) {
            if (qualifier != null && !qualifier.trim().isEmpty()) {
                this.qualifiers.add(qualifier.trim());
            }
            return this;
        }
        
        public Builder qualifiers(Set<String> qualifiers) {
            this.qualifiers = new HashSet<>(qualifiers != null ? qualifiers : Collections.emptySet());
            return this;
        }
        
        public Builder addProfile(String profile) {
            if (profile != null && !profile.trim().isEmpty()) {
                this.profiles.add(profile.trim());
            }
            return this;
        }
        
        public Builder profiles(Set<String> profiles) {
            this.profiles = new HashSet<>(profiles != null ? profiles : Collections.emptySet());
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
        
        public Builder dependencies(List<String> dependencies) {
            this.dependencies = dependencies != null ? dependencies : Collections.emptyList();
            return this;
        }
        
        public BeanDefinitionInfo build() {
            return new BeanDefinitionInfo(beanName, beanClass, declaringClass, factoryMethod,
                                         scope, isPrimary, isLazy, qualifiers, profiles,
                                         initMethod, destroyMethod, dependencies);
        }
    }
}