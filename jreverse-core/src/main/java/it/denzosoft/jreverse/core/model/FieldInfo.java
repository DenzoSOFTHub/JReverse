package it.denzosoft.jreverse.core.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Domain entity representing a Java field with its metadata.
 * Immutable value object following Clean Architecture principles.
 */
public final class FieldInfo {
    
    private final String name;
    private final String type;
    private final String declaringClassName;
    private final Set<AnnotationInfo> annotations;
    private final boolean isPublic;
    private final boolean isPrivate;
    private final boolean isProtected;
    private final boolean isStatic;
    private final boolean isFinal;
    private final boolean isTransient;
    private final boolean isVolatile;
    private final Object defaultValue;
    
    private FieldInfo(Builder builder) {
        this.name = requireNonEmpty(builder.name, "name");
        this.type = requireNonEmpty(builder.type, "type");
        this.declaringClassName = requireNonEmpty(builder.declaringClassName, "declaringClassName");
        this.annotations = Collections.unmodifiableSet(new HashSet<>(builder.annotations));
        this.isPublic = builder.isPublic;
        this.isPrivate = builder.isPrivate;
        this.isProtected = builder.isProtected;
        this.isStatic = builder.isStatic;
        this.isFinal = builder.isFinal;
        this.isTransient = builder.isTransient;
        this.isVolatile = builder.isVolatile;
        this.defaultValue = builder.defaultValue;
    }
    
    public String getName() {
        return name;
    }
    
    public String getType() {
        return type;
    }
    
    public String getDeclaringClassName() {
        return declaringClassName;
    }
    
    public Set<AnnotationInfo> getAnnotations() {
        return annotations;
    }
    
    public boolean isPublic() {
        return isPublic;
    }
    
    public boolean isPrivate() {
        return isPrivate;
    }
    
    public boolean isProtected() {
        return isProtected;
    }
    
    public boolean isStatic() {
        return isStatic;
    }
    
    public boolean isFinal() {
        return isFinal;
    }
    
    public boolean isTransient() {
        return isTransient;
    }
    
    public boolean isVolatile() {
        return isVolatile;
    }
    
    public Object getDefaultValue() {
        return defaultValue;
    }
    
    public boolean hasAnnotation(String annotationType) {
        return annotations.stream()
            .anyMatch(annotation -> annotation.getType().equals(annotationType));
    }
    
    public String getSimpleType() {
        int lastDotIndex = type.lastIndexOf('.');
        return lastDotIndex >= 0 ? type.substring(lastDotIndex + 1) : type;
    }
    
    public boolean isPrimitive() {
        return "boolean".equals(type) || "byte".equals(type) || "char".equals(type) ||
               "short".equals(type) || "int".equals(type) || "long".equals(type) ||
               "float".equals(type) || "double".equals(type);
    }
    
    public boolean isArray() {
        return type.endsWith("[]");
    }
    
    public boolean isConstant() {
        return isStatic && isFinal && isPrimitive() && defaultValue != null;
    }
    
    public boolean isCollection() {
        return type.contains("List") || type.contains("Set") || type.contains("Map") ||
               type.contains("Collection") || type.contains("Queue");
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
        FieldInfo fieldInfo = (FieldInfo) obj;
        return Objects.equals(name, fieldInfo.name) &&
               Objects.equals(declaringClassName, fieldInfo.declaringClassName);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, declaringClassName);
    }
    
    @Override
    public String toString() {
        return "FieldInfo{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", declaringClassName='" + declaringClassName + '\'' +
                ", annotationCount=" + annotations.size() +
                '}';
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String name;
        private String type;
        private String declaringClassName;
        private Set<AnnotationInfo> annotations = new HashSet<>();
        private boolean isPublic = false;
        private boolean isPrivate = false;
        private boolean isProtected = false;
        private boolean isStatic = false;
        private boolean isFinal = false;
        private boolean isTransient = false;
        private boolean isVolatile = false;
        private Object defaultValue;
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder type(String type) {
            this.type = type;
            return this;
        }
        
        public Builder declaringClassName(String declaringClassName) {
            this.declaringClassName = declaringClassName;
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
        
        public Builder isPublic(boolean isPublic) {
            this.isPublic = isPublic;
            return this;
        }
        
        public Builder isPrivate(boolean isPrivate) {
            this.isPrivate = isPrivate;
            return this;
        }
        
        public Builder isProtected(boolean isProtected) {
            this.isProtected = isProtected;
            return this;
        }
        
        public Builder isStatic(boolean isStatic) {
            this.isStatic = isStatic;
            return this;
        }
        
        public Builder isFinal(boolean isFinal) {
            this.isFinal = isFinal;
            return this;
        }
        
        public Builder isTransient(boolean isTransient) {
            this.isTransient = isTransient;
            return this;
        }
        
        public Builder isVolatile(boolean isVolatile) {
            this.isVolatile = isVolatile;
            return this;
        }
        
        public Builder defaultValue(Object defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }
        
        public FieldInfo build() {
            return new FieldInfo(this);
        }
    }
}