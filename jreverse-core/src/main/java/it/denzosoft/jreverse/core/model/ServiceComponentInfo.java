package it.denzosoft.jreverse.core.model;

import java.util.List;
import java.util.Objects;

/**
 * Information about a service component (@Service annotated class).
 */
public class ServiceComponentInfo {
    
    private final String className;
    private final String packageName;
    private final String serviceName;
    private final List<String> dependencies;
    private final boolean isTransactional;
    private final boolean isLazy;
    private final String scope;
    private final List<String> profiles;
    
    private ServiceComponentInfo(Builder builder) {
        this.className = builder.className;
        this.packageName = builder.packageName;
        this.serviceName = builder.serviceName;
        this.dependencies = List.copyOf(builder.dependencies);
        this.isTransactional = builder.isTransactional;
        this.isLazy = builder.isLazy;
        this.scope = builder.scope;
        this.profiles = List.copyOf(builder.profiles);
    }
    
    public String getClassName() { return className; }
    public String getPackageName() { return packageName; }
    public String getServiceName() { return serviceName; }
    public List<String> getDependencies() { return dependencies; }
    public boolean isTransactional() { return isTransactional; }
    public boolean isLazy() { return isLazy; }
    public String getScope() { return scope; }
    public List<String> getProfiles() { return profiles; }
    
    /**
     * Gets the simple class name (without package).
     */
    public String getSimpleClassName() {
        if (className == null) return null;
        int lastDot = className.lastIndexOf('.');
        return lastDot >= 0 ? className.substring(lastDot + 1) : className;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String className;
        private String packageName;
        private String serviceName;
        private List<String> dependencies = List.of();
        private boolean isTransactional;
        private boolean isLazy;
        private String scope = "singleton";
        private List<String> profiles = List.of();
        
        public Builder className(String className) {
            this.className = className;
            return this;
        }
        
        public Builder packageName(String packageName) {
            this.packageName = packageName;
            return this;
        }
        
        public Builder serviceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }
        
        public Builder dependencies(List<String> dependencies) {
            this.dependencies = dependencies != null ? dependencies : List.of();
            return this;
        }
        
        public Builder isTransactional(boolean isTransactional) {
            this.isTransactional = isTransactional;
            return this;
        }
        
        public Builder isLazy(boolean isLazy) {
            this.isLazy = isLazy;
            return this;
        }
        
        public Builder scope(String scope) {
            this.scope = scope;
            return this;
        }
        
        public Builder profiles(List<String> profiles) {
            this.profiles = profiles != null ? profiles : List.of();
            return this;
        }
        
        public ServiceComponentInfo build() {
            Objects.requireNonNull(className, "Class name is required");
            return new ServiceComponentInfo(this);
        }
    }
    
    @Override
    public String toString() {
        return String.format("ServiceComponentInfo{class='%s', service='%s', deps=%d}",
            getSimpleClassName(), serviceName, dependencies.size());
    }
}