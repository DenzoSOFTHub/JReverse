package it.denzosoft.jreverse.core.model;

import java.util.List;
import java.util.Objects;

/**
 * Information about a repository component (@Repository annotated class).
 */
public class RepositoryComponentInfo {
    
    private final String className;
    private final String packageName;
    private final String repositoryName;
    private final String entityType;
    private final List<String> customMethods;
    private final boolean isJpaRepository;
    private final boolean isTransactional;
    private final String scope;
    
    private RepositoryComponentInfo(Builder builder) {
        this.className = builder.className;
        this.packageName = builder.packageName;
        this.repositoryName = builder.repositoryName;
        this.entityType = builder.entityType;
        this.customMethods = List.copyOf(builder.customMethods);
        this.isJpaRepository = builder.isJpaRepository;
        this.isTransactional = builder.isTransactional;
        this.scope = builder.scope;
    }
    
    public String getClassName() { return className; }
    public String getPackageName() { return packageName; }
    public String getRepositoryName() { return repositoryName; }
    public String getEntityType() { return entityType; }
    public List<String> getCustomMethods() { return customMethods; }
    public boolean isJpaRepository() { return isJpaRepository; }
    public boolean isTransactional() { return isTransactional; }
    public String getScope() { return scope; }
    
    /**
     * Gets the simple class name (without package).
     */
    public String getSimpleClassName() {
        if (className == null) return null;
        int lastDot = className.lastIndexOf('.');
        return lastDot >= 0 ? className.substring(lastDot + 1) : className;
    }
    
    /**
     * Gets the simple entity type name (without package).
     */
    public String getSimpleEntityType() {
        if (entityType == null) return null;
        int lastDot = entityType.lastIndexOf('.');
        return lastDot >= 0 ? entityType.substring(lastDot + 1) : entityType;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String className;
        private String packageName;
        private String repositoryName;
        private String entityType;
        private List<String> customMethods = List.of();
        private boolean isJpaRepository = false;
        private boolean isTransactional = true;
        private String scope = "singleton";
        
        public Builder className(String className) {
            this.className = className;
            return this;
        }
        
        public Builder packageName(String packageName) {
            this.packageName = packageName;
            return this;
        }
        
        public Builder repositoryName(String repositoryName) {
            this.repositoryName = repositoryName;
            return this;
        }
        
        public Builder entityType(String entityType) {
            this.entityType = entityType;
            return this;
        }
        
        public Builder customMethods(List<String> customMethods) {
            this.customMethods = customMethods != null ? customMethods : List.of();
            return this;
        }
        
        public Builder isJpaRepository(boolean isJpaRepository) {
            this.isJpaRepository = isJpaRepository;
            return this;
        }
        
        public Builder isTransactional(boolean isTransactional) {
            this.isTransactional = isTransactional;
            return this;
        }
        
        public Builder scope(String scope) {
            this.scope = scope;
            return this;
        }
        
        public RepositoryComponentInfo build() {
            Objects.requireNonNull(className, "Class name is required");
            return new RepositoryComponentInfo(this);
        }
    }
    
    @Override
    public String toString() {
        return String.format("RepositoryComponentInfo{class='%s', entity='%s', jpa=%b, methods=%d}",
            getSimpleClassName(), getSimpleEntityType(), isJpaRepository, customMethods.size());
    }
}