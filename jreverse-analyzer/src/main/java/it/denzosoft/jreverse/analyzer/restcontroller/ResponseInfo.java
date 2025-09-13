package it.denzosoft.jreverse.analyzer.restcontroller;

import it.denzosoft.jreverse.core.model.AnnotationInfo;

import java.util.*;

/**
 * Immutable value object representing detailed information about a REST endpoint response.
 * Contains comprehensive metadata about response types, status codes, and content types
 * extracted from method signatures and Spring MVC annotations.
 */
public class ResponseInfo {
    
    private final String methodName;
    private final String declaringClass;
    private final String returnType;
    private final ResponseType responseType;
    private final boolean hasResponseBody;
    private final String httpStatus;
    private final String httpStatusReason;
    private final Set<String> producesContentTypes;
    private final String wrappedType;
    private final String elementType;
    private final String keyType;
    private final String valueType;
    private final Set<AnnotationInfo> responseAnnotations;
    private final boolean isAsync;
    private final String description;
    
    private ResponseInfo(Builder builder) {
        this.methodName = Objects.requireNonNull(builder.methodName, "methodName cannot be null");
        this.declaringClass = Objects.requireNonNull(builder.declaringClass, "declaringClass cannot be null");
        this.returnType = Objects.requireNonNull(builder.returnType, "returnType cannot be null");
        this.responseType = Objects.requireNonNull(builder.responseType, "responseType cannot be null");
        this.hasResponseBody = builder.hasResponseBody;
        this.httpStatus = builder.httpStatus;
        this.httpStatusReason = builder.httpStatusReason;
        this.producesContentTypes = Collections.unmodifiableSet(new LinkedHashSet<>(builder.producesContentTypes));
        this.wrappedType = builder.wrappedType;
        this.elementType = builder.elementType;
        this.keyType = builder.keyType;
        this.valueType = builder.valueType;
        this.responseAnnotations = Collections.unmodifiableSet(new LinkedHashSet<>(builder.responseAnnotations));
        this.isAsync = builder.isAsync;
        this.description = builder.description;
    }
    
    // Core response information
    public String getMethodName() { return methodName; }
    public String getDeclaringClass() { return declaringClass; }
    public String getReturnType() { return returnType; }
    public ResponseType getResponseType() { return responseType; }
    public boolean hasResponseBody() { return hasResponseBody; }
    public String getHttpStatus() { return httpStatus; }
    public String getHttpStatusReason() { return httpStatusReason; }
    public Set<String> getProducesContentTypes() { return producesContentTypes; }
    
    // Type analysis
    public String getWrappedType() { return wrappedType; }
    public String getElementType() { return elementType; }
    public String getKeyType() { return keyType; }
    public String getValueType() { return valueType; }
    public Set<AnnotationInfo> getResponseAnnotations() { return responseAnnotations; }
    public boolean isAsync() { return isAsync; }
    public String getDescription() { return description; }
    
    /**
     * Gets the fully qualified method identifier.
     */
    public String getMethodIdentifier() {
        return declaringClass + "." + methodName;
    }
    
    /**
     * Gets the simple return type name without package.
     */
    public String getSimpleReturnType() {
        int lastDotIndex = returnType.lastIndexOf('.');
        return lastDotIndex >= 0 ? returnType.substring(lastDotIndex + 1) : returnType;
    }
    
    /**
     * Checks if the response has explicit HTTP status configuration.
     */
    public boolean hasExplicitStatus() {
        return httpStatus != null && !httpStatus.trim().isEmpty();
    }
    
    /**
     * Checks if the response produces specific content types.
     */
    public boolean hasContentTypeRestrictions() {
        return !producesContentTypes.isEmpty();
    }
    
    /**
     * Checks if the response is wrapped in a container type.
     */
    public boolean isWrappedResponse() {
        return responseType.isWrapped() || wrappedType != null;
    }
    
    /**
     * Checks if the response is a complex type (object, collection, map).
     */
    public boolean isComplexResponse() {
        return responseType.isComplexType();
    }
    
    /**
     * Checks if the response supports JSON serialization based on content types.
     */
    public boolean supportsJson() {
        return producesContentTypes.isEmpty() || // Default includes JSON
               producesContentTypes.stream().anyMatch(ct -> 
                   ct.contains("json") || ct.contains("application/json"));
    }
    
    /**
     * Checks if the response supports XML serialization based on content types.
     */
    public boolean supportsXml() {
        return producesContentTypes.stream().anyMatch(ct -> 
            ct.contains("xml") || ct.contains("application/xml") || ct.contains("text/xml"));
    }
    
    /**
     * Gets the effective content type for the response.
     */
    public String getEffectiveContentType() {
        if (producesContentTypes.isEmpty()) {
            return hasResponseBody ? "application/json" : ""; // Default REST content type
        }
        return producesContentTypes.iterator().next(); // First content type
    }
    
    /**
     * Gets response complexity level based on type structure.
     */
    public ResponseComplexity getComplexity() {
        if (responseType == ResponseType.VOID) {
            return ResponseComplexity.NONE;
        } else if (responseType == ResponseType.PRIMITIVE || responseType == ResponseType.STRING) {
            return ResponseComplexity.SIMPLE;
        } else if (responseType == ResponseType.COLLECTION || responseType == ResponseType.MAP || responseType == ResponseType.ARRAY) {
            return ResponseComplexity.COLLECTION;
        } else if (isWrappedResponse() || isAsync) {
            return ResponseComplexity.WRAPPED;
        } else {
            return ResponseComplexity.OBJECT;
        }
    }
    
    /**
     * Enumeration of response complexity levels.
     */
    public enum ResponseComplexity {
        NONE("No Response"),
        SIMPLE("Simple Value"),
        OBJECT("Single Object"),
        COLLECTION("Collection/Array"),
        WRAPPED("Wrapped Response");
        
        private final String description;
        
        ResponseComplexity(String description) {
            this.description = description;
        }
        
        public String getDescription() { return description; }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ResponseInfo that = (ResponseInfo) obj;
        return Objects.equals(methodName, that.methodName) &&
               Objects.equals(declaringClass, that.declaringClass) &&
               Objects.equals(returnType, that.returnType);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(methodName, declaringClass, returnType);
    }
    
    @Override
    public String toString() {
        return "ResponseInfo{" +
                "method='" + getMethodIdentifier() + '\'' +
                ", returnType='" + getSimpleReturnType() + '\'' +
                ", responseType=" + responseType +
                ", hasResponseBody=" + hasResponseBody +
                ", status=" + httpStatus +
                ", complexity=" + getComplexity() +
                '}';
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String methodName;
        private String declaringClass;
        private String returnType;
        private ResponseType responseType = ResponseType.UNKNOWN;
        private boolean hasResponseBody = false;
        private String httpStatus;
        private String httpStatusReason;
        private Set<String> producesContentTypes = new LinkedHashSet<>();
        private String wrappedType;
        private String elementType;
        private String keyType;
        private String valueType;
        private Set<AnnotationInfo> responseAnnotations = new LinkedHashSet<>();
        private boolean isAsync = false;
        private String description;
        
        public Builder methodName(String methodName) {
            this.methodName = methodName;
            return this;
        }
        
        public Builder declaringClass(String declaringClass) {
            this.declaringClass = declaringClass;
            return this;
        }
        
        public Builder returnType(String returnType) {
            this.returnType = returnType;
            return this;
        }
        
        public Builder responseType(ResponseType responseType) {
            this.responseType = responseType;
            return this;
        }
        
        public Builder hasResponseBody(boolean hasResponseBody) {
            this.hasResponseBody = hasResponseBody;
            return this;
        }
        
        public Builder httpStatus(String httpStatus) {
            this.httpStatus = httpStatus;
            return this;
        }
        
        public Builder httpStatusReason(String httpStatusReason) {
            this.httpStatusReason = httpStatusReason;
            return this;
        }
        
        public Builder addProducesContentType(String contentType) {
            if (contentType != null && !contentType.trim().isEmpty()) {
                this.producesContentTypes.add(contentType.trim());
            }
            return this;
        }
        
        public Builder producesContentTypes(Set<String> producesContentTypes) {
            this.producesContentTypes = new LinkedHashSet<>(producesContentTypes != null ? producesContentTypes : Collections.emptySet());
            return this;
        }
        
        public Builder wrappedType(String wrappedType) {
            this.wrappedType = wrappedType;
            return this;
        }
        
        public Builder elementType(String elementType) {
            this.elementType = elementType;
            return this;
        }
        
        public Builder keyType(String keyType) {
            this.keyType = keyType;
            return this;
        }
        
        public Builder valueType(String valueType) {
            this.valueType = valueType;
            return this;
        }
        
        public Builder addResponseAnnotation(AnnotationInfo annotation) {
            if (annotation != null) {
                this.responseAnnotations.add(annotation);
            }
            return this;
        }
        
        public Builder responseAnnotations(Set<AnnotationInfo> responseAnnotations) {
            this.responseAnnotations = new LinkedHashSet<>(responseAnnotations != null ? responseAnnotations : Collections.emptySet());
            return this;
        }
        
        public Builder isAsync(boolean isAsync) {
            this.isAsync = isAsync;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public ResponseInfo build() {
            return new ResponseInfo(this);
        }
    }
}