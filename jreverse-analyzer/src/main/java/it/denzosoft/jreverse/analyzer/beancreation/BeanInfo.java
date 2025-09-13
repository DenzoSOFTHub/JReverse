package it.denzosoft.jreverse.analyzer.beancreation;

import it.denzosoft.jreverse.core.model.ClassInfo;
import it.denzosoft.jreverse.core.model.MethodInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Information about a discovered Spring bean including its type, creation mechanism,
 * annotations, dependencies, and lifecycle characteristics.
 */
public class BeanInfo {
    
    private final String beanName;
    private final String beanType;
    private final BeanCreationType creationType;
    private final ClassInfo declaringClass;
    private final MethodInfo creationMethod; // null for component-style beans
    private final List<String> annotations;
    private final List<BeanDependency> dependencies;
    private final BeanScope scope;
    private final boolean isLazy;
    private final boolean isPrimary;
    private final Set<String> profiles;
    private final String qualifier;
    
    private BeanInfo(Builder builder) {
        this.beanName = builder.beanName;
        this.beanType = builder.beanType;
        this.creationType = builder.creationType;
        this.declaringClass = builder.declaringClass;
        this.creationMethod = builder.creationMethod;
        this.annotations = Collections.unmodifiableList(new ArrayList<>(builder.annotations));
        this.dependencies = Collections.unmodifiableList(new ArrayList<>(builder.dependencies));
        this.scope = builder.scope;
        this.isLazy = builder.isLazy;
        this.isPrimary = builder.isPrimary;
        this.profiles = Collections.unmodifiableSet(new HashSet<>(builder.profiles));
        this.qualifier = builder.qualifier;
    }
    
    // Getters
    public String getBeanName() { return beanName; }
    public String getBeanType() { return beanType; }
    public BeanCreationType getCreationType() { return creationType; }
    public ClassInfo getDeclaringClass() { return declaringClass; }
    public MethodInfo getCreationMethod() { return creationMethod; }
    public List<String> getAnnotations() { return annotations; }
    public List<BeanDependency> getDependencies() { return dependencies; }
    public BeanScope getScope() { return scope; }
    public boolean isLazy() { return isLazy; }
    public boolean isPrimary() { return isPrimary; }
    public Set<String> getProfiles() { return profiles; }
    public String getQualifier() { return qualifier; }
    
    /**
     * Gets the fully qualified name of the declaring class.
     */
    public String getDeclaringClassName() {
        return declaringClass.getFullyQualifiedName();
    }
    
    /**
     * Gets a human-readable signature for this bean.
     */
    public String getSignature() {
        if (creationType == BeanCreationType.BEAN_METHOD && creationMethod != null) {
            return String.format("%s.%s() -> %s",
                declaringClass.getFullyQualifiedName(),
                creationMethod.getName(),
                beanType);
        } else {
            return String.format("%s (%s)",
                beanType,
                creationType.toString().toLowerCase());
        }
    }
    
    /**
     * Checks if this bean has a specific annotation.
     */
    public boolean hasAnnotation(String annotationType) {
        return annotations.stream()
            .anyMatch(annotation -> annotation.equals(annotationType));
    }
    
    /**
     * Checks if this bean is conditional (has @Profile or @Conditional annotations).
     */
    public boolean isConditional() {
        return !profiles.isEmpty() || 
               annotations.stream().anyMatch(ann -> ann.contains("Conditional"));
    }
    
    /**
     * Gets the number of dependencies this bean has.
     */
    public int getDependencyCount() {
        return dependencies.size();
    }
    
    /**
     * Checks if this bean has circular dependencies.
     */
    public boolean hasCircularDependencies() {
        return dependencies.stream()
            .anyMatch(dep -> dep.getType().equals(beanType));
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String beanName;
        private String beanType;
        private BeanCreationType creationType;
        private ClassInfo declaringClass;
        private MethodInfo creationMethod;
        private List<String> annotations = new ArrayList<>();
        private List<BeanDependency> dependencies = new ArrayList<>();
        private BeanScope scope = BeanScope.SINGLETON;
        private boolean isLazy = false;
        private boolean isPrimary = false;
        private Set<String> profiles = new HashSet<>();
        private String qualifier;
        
        public Builder beanName(String beanName) {
            this.beanName = beanName;
            return this;
        }
        
        public Builder beanType(String beanType) {
            this.beanType = beanType;
            return this;
        }
        
        public Builder creationType(BeanCreationType creationType) {
            this.creationType = creationType;
            return this;
        }
        
        public Builder declaringClass(ClassInfo declaringClass) {
            this.declaringClass = declaringClass;
            return this;
        }
        
        public Builder creationMethod(MethodInfo creationMethod) {
            this.creationMethod = creationMethod;
            return this;
        }
        
        public Builder annotations(List<String> annotations) {
            this.annotations = new ArrayList<>(annotations);
            return this;
        }
        
        public Builder addAnnotation(String annotation) {
            this.annotations.add(annotation);
            return this;
        }
        
        public Builder dependencies(List<BeanDependency> dependencies) {
            this.dependencies = new ArrayList<>(dependencies);
            return this;
        }
        
        public Builder addDependency(BeanDependency dependency) {
            this.dependencies.add(dependency);
            return this;
        }
        
        public Builder scope(BeanScope scope) {
            this.scope = scope;
            return this;
        }
        
        public Builder isLazy(boolean isLazy) {
            this.isLazy = isLazy;
            return this;
        }
        
        public Builder isPrimary(boolean isPrimary) {
            this.isPrimary = isPrimary;
            return this;
        }
        
        public Builder profiles(Set<String> profiles) {
            this.profiles = new HashSet<>(profiles);
            return this;
        }
        
        public Builder addProfile(String profile) {
            this.profiles.add(profile);
            return this;
        }
        
        public Builder qualifier(String qualifier) {
            this.qualifier = qualifier;
            return this;
        }
        
        public BeanInfo build() {
            Objects.requireNonNull(beanName, "Bean name is required");
            Objects.requireNonNull(beanType, "Bean type is required");
            Objects.requireNonNull(creationType, "Creation type is required");
            Objects.requireNonNull(declaringClass, "Declaring class is required");
            
            return new BeanInfo(this);
        }
    }
    
    @Override
    public String toString() {
        return String.format("BeanInfo{name='%s', type='%s', creationType=%s, scope=%s}",
            beanName, beanType, creationType, scope);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BeanInfo beanInfo = (BeanInfo) obj;
        return Objects.equals(beanName, beanInfo.beanName) &&
               Objects.equals(beanType, beanInfo.beanType) &&
               Objects.equals(declaringClass.getFullyQualifiedName(), beanInfo.declaringClass.getFullyQualifiedName());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(beanName, beanType, declaringClass.getFullyQualifiedName());
    }
}