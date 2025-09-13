package it.denzosoft.jreverse.analyzer.async;

import it.denzosoft.jreverse.core.model.AnnotationInfo;

import java.util.*;

/**
 * Immutable value object representing detailed information about an async entrypoint.
 * Contains metadata about async operations, executors, and return types.
 */
public class AsyncEntrypointInfo {
    
    private final String methodName;
    private final String declaringClass;
    private final AsyncEntrypointType asyncType;
    private final String returnType;
    private final String executorName;
    private final boolean hasTimeout;
    private final long timeoutMillis;
    private final boolean hasErrorHandler;
    private final String errorHandlerMethod;
    private final Set<String> exceptions;
    private final AnnotationInfo sourceAnnotation;
    private final boolean isVoid;
    private final boolean isFuture;
    private final boolean isCompletableFuture;
    private final Map<String, Object> metadata;
    
    private AsyncEntrypointInfo(Builder builder) {
        this.methodName = Objects.requireNonNull(builder.methodName, "methodName cannot be null");
        this.declaringClass = Objects.requireNonNull(builder.declaringClass, "declaringClass cannot be null");
        this.asyncType = Objects.requireNonNull(builder.asyncType, "asyncType cannot be null");
        this.returnType = builder.returnType;
        this.executorName = builder.executorName;
        this.hasTimeout = builder.hasTimeout;
        this.timeoutMillis = builder.timeoutMillis;
        this.hasErrorHandler = builder.hasErrorHandler;
        this.errorHandlerMethod = builder.errorHandlerMethod;
        this.exceptions = Collections.unmodifiableSet(new LinkedHashSet<>(builder.exceptions));
        this.sourceAnnotation = builder.sourceAnnotation;
        this.isVoid = builder.isVoid;
        this.isFuture = builder.isFuture;
        this.isCompletableFuture = builder.isCompletableFuture;
        this.metadata = Collections.unmodifiableMap(new HashMap<>(builder.metadata));
    }
    
    // Getters
    public String getMethodName() { return methodName; }
    public String getDeclaringClass() { return declaringClass; }
    public AsyncEntrypointType getAsyncType() { return asyncType; }
    public String getReturnType() { return returnType; }
    public String getExecutorName() { return executorName; }
    public boolean hasTimeout() { return hasTimeout; }
    public long getTimeoutMillis() { return timeoutMillis; }
    public boolean hasErrorHandler() { return hasErrorHandler; }
    public String getErrorHandlerMethod() { return errorHandlerMethod; }
    public Set<String> getExceptions() { return exceptions; }
    public AnnotationInfo getSourceAnnotation() { return sourceAnnotation; }
    public boolean isVoid() { return isVoid; }
    public boolean isFuture() { return isFuture; }
    public boolean isCompletableFuture() { return isCompletableFuture; }
    public Map<String, Object> getMetadata() { return metadata; }
    
    /**
     * Gets the fully qualified method identifier.
     */
    public String getMethodIdentifier() {
        return declaringClass + "." + methodName;
    }
    
    /**
     * Checks if this async operation has proper error handling.
     */
    public boolean hasProperErrorHandling() {
        return hasErrorHandler || !exceptions.isEmpty();
    }
    
    /**
     * Checks if this async operation may have threading issues.
     */
    public boolean hasThreadingRisk() {
        // Void async methods without error handling are risky
        return isVoid && !hasProperErrorHandling();
    }
    
    /**
     * Gets a risk score for this async operation (0-100).
     */
    public int getRiskScore() {
        int score = 0;
        
        // Base score
        if (asyncType == AsyncEntrypointType.ASYNC_METHOD) {
            score = 30;
        }
        
        // Add risk for void returns
        if (isVoid) {
            score += 20;
        }
        
        // Add risk for no error handling
        if (!hasProperErrorHandling()) {
            score += 30;
        }
        
        // Add risk for no timeout
        if (!hasTimeout && asyncType.requiresExecutor()) {
            score += 20;
        }
        
        return Math.min(100, score);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AsyncEntrypointInfo that = (AsyncEntrypointInfo) obj;
        return Objects.equals(methodName, that.methodName) &&
               Objects.equals(declaringClass, that.declaringClass) &&
               Objects.equals(asyncType, that.asyncType);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(methodName, declaringClass, asyncType);
    }
    
    @Override
    public String toString() {
        return "AsyncEntrypointInfo{" +
                "method='" + getMethodIdentifier() + '\'' +
                ", type=" + asyncType +
                ", returnType='" + returnType + '\'' +
                ", executor='" + executorName + '\'' +
                ", risk=" + getRiskScore() +
                '}';
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String methodName;
        private String declaringClass;
        private AsyncEntrypointType asyncType;
        private String returnType;
        private String executorName = "default";
        private boolean hasTimeout;
        private long timeoutMillis;
        private boolean hasErrorHandler;
        private String errorHandlerMethod;
        private Set<String> exceptions = new LinkedHashSet<>();
        private AnnotationInfo sourceAnnotation;
        private boolean isVoid;
        private boolean isFuture;
        private boolean isCompletableFuture;
        private Map<String, Object> metadata = new HashMap<>();
        
        public Builder methodName(String methodName) {
            this.methodName = methodName;
            return this;
        }
        
        public Builder declaringClass(String declaringClass) {
            this.declaringClass = declaringClass;
            return this;
        }
        
        public Builder asyncType(AsyncEntrypointType asyncType) {
            this.asyncType = asyncType;
            return this;
        }
        
        public Builder returnType(String returnType) {
            this.returnType = returnType;
            this.isVoid = "void".equals(returnType);
            this.isFuture = returnType != null && returnType.contains("Future");
            this.isCompletableFuture = returnType != null && returnType.contains("CompletableFuture");
            return this;
        }
        
        public Builder executorName(String executorName) {
            this.executorName = executorName;
            return this;
        }
        
        public Builder timeout(long timeoutMillis) {
            this.hasTimeout = true;
            this.timeoutMillis = timeoutMillis;
            return this;
        }
        
        public Builder errorHandler(String errorHandlerMethod) {
            this.hasErrorHandler = true;
            this.errorHandlerMethod = errorHandlerMethod;
            return this;
        }
        
        public Builder addException(String exception) {
            if (exception != null && !exception.trim().isEmpty()) {
                this.exceptions.add(exception.trim());
            }
            return this;
        }
        
        public Builder exceptions(Set<String> exceptions) {
            this.exceptions = new LinkedHashSet<>(exceptions != null ? exceptions : Collections.emptySet());
            return this;
        }
        
        public Builder sourceAnnotation(AnnotationInfo sourceAnnotation) {
            this.sourceAnnotation = sourceAnnotation;
            return this;
        }
        
        public Builder addMetadata(String key, Object value) {
            if (key != null && value != null) {
                this.metadata.put(key, value);
            }
            return this;
        }
        
        public AsyncEntrypointInfo build() {
            return new AsyncEntrypointInfo(this);
        }
    }
}