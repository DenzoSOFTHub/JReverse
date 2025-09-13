package it.denzosoft.jreverse.core.model;

/**
 * Information about Spring Boot bean lifecycle callbacks and methods.
 */
public class BeanLifecycleInfo {
    
    public enum LifecycleType {
        POST_CONSTRUCT,
        PRE_DESTROY,
        INIT_METHOD,
        DESTROY_METHOD,
        AFTER_PROPERTIES_SET,
        DISPOSABLE_BEAN
    }
    
    private final String beanClassName;
    private final String methodName;
    private final LifecycleType lifecycleType;
    private final String annotationType;
    private final boolean isCustomMethod;
    
    private BeanLifecycleInfo(String beanClassName,
                             String methodName,
                             LifecycleType lifecycleType,
                             String annotationType,
                             boolean isCustomMethod) {
        this.beanClassName = beanClassName;
        this.methodName = methodName;
        this.lifecycleType = lifecycleType;
        this.annotationType = annotationType;
        this.isCustomMethod = isCustomMethod;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public String getBeanClassName() {
        return beanClassName;
    }
    
    public String getMethodName() {
        return methodName;
    }
    
    public LifecycleType getLifecycleType() {
        return lifecycleType;
    }
    
    public String getAnnotationType() {
        return annotationType;
    }
    
    public boolean isCustomMethod() {
        return isCustomMethod;
    }
    
    @Override
    public String toString() {
        return "BeanLifecycleInfo{" +
                "beanClassName='" + beanClassName + '\'' +
                ", methodName='" + methodName + '\'' +
                ", lifecycleType=" + lifecycleType +
                ", isCustomMethod=" + isCustomMethod +
                '}';
    }
    
    public static class Builder {
        private String beanClassName;
        private String methodName;
        private LifecycleType lifecycleType;
        private String annotationType;
        private boolean isCustomMethod = false;
        
        public Builder beanClassName(String beanClassName) {
            this.beanClassName = beanClassName;
            return this;
        }
        
        public Builder methodName(String methodName) {
            this.methodName = methodName;
            return this;
        }
        
        public Builder lifecycleType(LifecycleType lifecycleType) {
            this.lifecycleType = lifecycleType;
            return this;
        }
        
        public Builder annotationType(String annotationType) {
            this.annotationType = annotationType;
            return this;
        }
        
        public Builder isCustomMethod(boolean isCustomMethod) {
            this.isCustomMethod = isCustomMethod;
            return this;
        }
        
        public BeanLifecycleInfo build() {
            return new BeanLifecycleInfo(beanClassName, methodName, lifecycleType, 
                                       annotationType, isCustomMethod);
        }
    }
}