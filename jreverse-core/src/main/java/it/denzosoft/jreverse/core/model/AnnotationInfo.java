package it.denzosoft.jreverse.core.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Domain entity representing a Java annotation with its metadata.
 * Immutable value object following Clean Architecture principles.
 */
public final class AnnotationInfo {
    
    private final String type;
    private final Map<String, Object> attributes;
    
    private AnnotationInfo(Builder builder) {
        this.type = requireNonEmpty(builder.type, "type");
        this.attributes = Collections.unmodifiableMap(new HashMap<>(builder.attributes));
    }
    
    public String getType() {
        return type;
    }
    
    public Map<String, Object> getAttributes() {
        return attributes;
    }
    
    /**
     * Alias for getAttributes() to maintain backward compatibility.
     */
    public Map<String, Object> getValues() {
        return attributes;
    }
    
    public Object getAttribute(String name) {
        return attributes.get(name);
    }
    
    public String getStringAttribute(String name) {
        Object value = attributes.get(name);
        return value instanceof String ? (String) value : null;
    }
    
    public Boolean getBooleanAttribute(String name) {
        Object value = attributes.get(name);
        return value instanceof Boolean ? (Boolean) value : null;
    }
    
    public Integer getIntegerAttribute(String name) {
        Object value = attributes.get(name);
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }
    
    public String[] getStringArrayAttribute(String name) {
        Object value = attributes.get(name);
        return value instanceof String[] ? (String[]) value : null;
    }
    
    public boolean hasAttribute(String name) {
        return attributes.containsKey(name);
    }
    
    public String getSimpleType() {
        int lastDotIndex = type.lastIndexOf('.');
        return lastDotIndex >= 0 ? type.substring(lastDotIndex + 1) : type;
    }
    
    public boolean isSpringAnnotation() {
        return type.startsWith("org.springframework.");
    }
    
    public boolean isJpaAnnotation() {
        return type.startsWith("javax.persistence.") || type.startsWith("jakarta.persistence.");
    }
    
    public boolean isJsr305Annotation() {
        return type.startsWith("javax.annotation.");
    }
    
    public boolean isCustomAnnotation() {
        return !type.startsWith("java.") && 
               !type.startsWith("javax.") && 
               !type.startsWith("jakarta.") &&
               !isSpringAnnotation();
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
        AnnotationInfo that = (AnnotationInfo) obj;
        return Objects.equals(type, that.type) &&
               Objects.equals(attributes, that.attributes);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(type, attributes);
    }
    
    @Override
    public String toString() {
        return "AnnotationInfo{" +
                "type='" + type + '\'' +
                ", attributeCount=" + attributes.size() +
                '}';
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static AnnotationInfo simple(String type) {
        return builder().type(type).build();
    }
    
    public static class Builder {
        private String type;
        private Map<String, Object> attributes = new HashMap<>();
        
        public Builder type(String type) {
            this.type = type;
            return this;
        }
        
        public Builder addAttribute(String name, Object value) {
            if (name != null && !name.trim().isEmpty() && value != null) {
                this.attributes.put(name.trim(), value);
            }
            return this;
        }
        
        public Builder attributes(Map<String, Object> attributes) {
            this.attributes = new HashMap<>(attributes != null ? attributes : Collections.emptyMap());
            return this;
        }
        
        public AnnotationInfo build() {
            return new AnnotationInfo(this);
        }
    }
}