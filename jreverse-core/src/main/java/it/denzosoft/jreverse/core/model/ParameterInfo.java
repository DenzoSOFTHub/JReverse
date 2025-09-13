package it.denzosoft.jreverse.core.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Domain entity representing a method parameter with its metadata.
 * Immutable value object following Clean Architecture principles.
 */
public final class ParameterInfo {
    
    private final String name;
    private final String type;
    private final int index;
    private final Set<AnnotationInfo> annotations;
    private final boolean isFinal;
    private final boolean isVarArgs;
    
    private ParameterInfo(Builder builder) {
        this.name = builder.name; // Can be null for parameters without debug info
        this.type = requireNonEmpty(builder.type, "type");
        this.index = requireNonNegative(builder.index, "index");
        this.annotations = Collections.unmodifiableSet(new HashSet<>(builder.annotations));
        this.isFinal = builder.isFinal;
        this.isVarArgs = builder.isVarArgs;
    }
    
    public String getName() {
        return name;
    }
    
    public String getType() {
        return type;
    }
    
    public int getIndex() {
        return index;
    }
    
    public Set<AnnotationInfo> getAnnotations() {
        return annotations;
    }
    
    public boolean isFinal() {
        return isFinal;
    }
    
    public boolean isVarArgs() {
        return isVarArgs;
    }
    
    public boolean hasAnnotation(String annotationType) {
        return annotations.stream()
            .anyMatch(annotation -> annotation.getType().equals(annotationType));
    }
    
    public String getDisplayName() {
        return name != null ? name : "param" + index;
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
    
    private String requireNonEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }
        return value.trim();
    }
    
    private int requireNonNegative(int value, String fieldName) {
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " cannot be negative");
        }
        return value;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ParameterInfo that = (ParameterInfo) obj;
        return index == that.index &&
               Objects.equals(type, that.type) &&
               Objects.equals(name, that.name);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, type, index);
    }
    
    @Override
    public String toString() {
        return "ParameterInfo{" +
                "name='" + getDisplayName() + '\'' +
                ", type='" + type + '\'' +
                ", index=" + index +
                ", annotationCount=" + annotations.size() +
                '}';
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String name;
        private String type;
        private int index;
        private Set<AnnotationInfo> annotations = new HashSet<>();
        private boolean isFinal = false;
        private boolean isVarArgs = false;
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder type(String type) {
            this.type = type;
            return this;
        }
        
        public Builder index(int index) {
            this.index = index;
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
        
        public Builder isFinal(boolean isFinal) {
            this.isFinal = isFinal;
            return this;
        }
        
        public Builder isVarArgs(boolean isVarArgs) {
            this.isVarArgs = isVarArgs;
            return this;
        }
        
        public ParameterInfo build() {
            return new ParameterInfo(this);
        }
    }
}