package it.denzosoft.jreverse.core.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Domain entity representing a Java class with its metadata.
 * Immutable value object following Clean Architecture principles.
 */
public final class ClassInfo {
    
    private final String fullyQualifiedName;
    private final String packageName;
    private final String simpleName;
    private final ClassType classType;
    private final Set<MethodInfo> methods;
    private final Set<FieldInfo> fields;
    private final Set<AnnotationInfo> annotations;
    private final String superClassName;
    private final Set<String> interfaceNames;
    
    private ClassInfo(Builder builder) {
        this.fullyQualifiedName = requireNonEmpty(builder.fullyQualifiedName, "fullyQualifiedName");
        this.packageName = extractPackageName(this.fullyQualifiedName);
        this.simpleName = extractSimpleName(this.fullyQualifiedName);
        this.classType = Objects.requireNonNull(builder.classType, "classType cannot be null");
        this.methods = Collections.unmodifiableSet(new HashSet<>(builder.methods));
        this.fields = Collections.unmodifiableSet(new HashSet<>(builder.fields));
        this.annotations = Collections.unmodifiableSet(new HashSet<>(builder.annotations));
        this.superClassName = builder.superClassName;
        this.interfaceNames = Collections.unmodifiableSet(new HashSet<>(builder.interfaceNames));
    }
    
    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }
    
    public String getPackageName() {
        return packageName;
    }
    
    public String getSimpleName() {
        return simpleName;
    }
    
    public ClassType getClassType() {
        return classType;
    }
    
    public Set<MethodInfo> getMethods() {
        return methods;
    }
    
    public Set<FieldInfo> getFields() {
        return fields;
    }
    
    public Set<AnnotationInfo> getAnnotations() {
        return annotations;
    }
    
    public String getSuperClassName() {
        return superClassName;
    }
    
    public Set<String> getInterfaceNames() {
        return interfaceNames;
    }
    
    public boolean hasAnnotation(String annotationType) {
        return annotations.stream()
            .anyMatch(annotation -> annotation.getType().equals(annotationType));
    }
    
    public boolean isPublic() {
        return classType == ClassType.PUBLIC_CLASS || 
               classType == ClassType.PUBLIC_INTERFACE ||
               classType == ClassType.PUBLIC_ENUM ||
               classType == ClassType.PUBLIC_ANNOTATION;
    }
    
    public boolean isInterface() {
        return classType == ClassType.INTERFACE || 
               classType == ClassType.PUBLIC_INTERFACE;
    }
    
    public boolean isEnum() {
        return classType == ClassType.ENUM || 
               classType == ClassType.PUBLIC_ENUM;
    }
    
    public boolean isAnnotation() {
        return classType == ClassType.ANNOTATION || 
               classType == ClassType.PUBLIC_ANNOTATION;
    }
    
    public boolean isAbstract() {
        return classType == ClassType.ABSTRACT_CLASS || 
               classType == ClassType.PUBLIC_ABSTRACT_CLASS;
    }
    
    private String extractPackageName(String fullyQualifiedName) {
        int lastDotIndex = fullyQualifiedName.lastIndexOf('.');
        return lastDotIndex > 0 ? fullyQualifiedName.substring(0, lastDotIndex) : "";
    }
    
    private String extractSimpleName(String fullyQualifiedName) {
        int lastDotIndex = fullyQualifiedName.lastIndexOf('.');
        return lastDotIndex >= 0 ? fullyQualifiedName.substring(lastDotIndex + 1) : fullyQualifiedName;
    }
    
    private String requireNonEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }
        return value.trim();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ClassInfo classInfo = (ClassInfo) obj;
        return Objects.equals(fullyQualifiedName, classInfo.fullyQualifiedName);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(fullyQualifiedName);
    }
    
    @Override
    public String toString() {
        return "ClassInfo{" +
                "fullyQualifiedName='" + fullyQualifiedName + '\'' +
                ", classType=" + classType +
                ", methodCount=" + methods.size() +
                ", fieldCount=" + fields.size() +
                ", annotationCount=" + annotations.size() +
                '}';
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String fullyQualifiedName;
        private ClassType classType = ClassType.CLASS;
        private Set<MethodInfo> methods = new HashSet<>();
        private Set<FieldInfo> fields = new HashSet<>();
        private Set<AnnotationInfo> annotations = new HashSet<>();
        private String superClassName;
        private Set<String> interfaceNames = new HashSet<>();
        
        public Builder fullyQualifiedName(String fullyQualifiedName) {
            this.fullyQualifiedName = fullyQualifiedName;
            return this;
        }
        
        public Builder classType(ClassType classType) {
            this.classType = classType;
            return this;
        }
        
        public Builder addMethod(MethodInfo method) {
            if (method != null) {
                this.methods.add(method);
            }
            return this;
        }
        
        public Builder methods(Set<MethodInfo> methods) {
            this.methods = new HashSet<>(methods != null ? methods : Collections.emptySet());
            return this;
        }
        
        public Builder addField(FieldInfo field) {
            if (field != null) {
                this.fields.add(field);
            }
            return this;
        }
        
        public Builder fields(Set<FieldInfo> fields) {
            this.fields = new HashSet<>(fields != null ? fields : Collections.emptySet());
            return this;
        }
        
        public Builder addAnnotation(AnnotationInfo annotation) {
            if (annotation != null) {
                this.annotations.add(annotation);
            }
            return this;
        }
        
        public Builder annotations(Set<AnnotationInfo> annotations) {
            this.annotations = new HashSet<>(annotations != null ? annotations : Collections.emptySet());
            return this;
        }
        
        public Builder superClassName(String superClassName) {
            this.superClassName = superClassName;
            return this;
        }
        
        public Builder addInterface(String interfaceName) {
            if (interfaceName != null && !interfaceName.trim().isEmpty()) {
                this.interfaceNames.add(interfaceName.trim());
            }
            return this;
        }
        
        public Builder interfaceNames(Set<String> interfaceNames) {
            this.interfaceNames = new HashSet<>(interfaceNames != null ? interfaceNames : Collections.emptySet());
            return this;
        }
        
        public ClassInfo build() {
            return new ClassInfo(this);
        }
    }
}