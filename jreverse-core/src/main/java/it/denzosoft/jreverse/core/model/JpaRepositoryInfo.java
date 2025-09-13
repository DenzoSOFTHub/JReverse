package it.denzosoft.jreverse.core.model;

import java.util.List;
import java.util.Objects;

/**
 * Information about a JPA repository (interface extending JpaRepository).
 */
public class JpaRepositoryInfo {
    
    private final String interfaceName;
    private final String packageName;
    private final String entityType;
    private final String idType;
    private final List<String> queryMethods;
    private final List<String> customQueries;
    private final boolean hasNativeQueries;
    private final boolean hasNamedQueries;
    
    private JpaRepositoryInfo(Builder builder) {
        this.interfaceName = builder.interfaceName;
        this.packageName = builder.packageName;
        this.entityType = builder.entityType;
        this.idType = builder.idType;
        this.queryMethods = List.copyOf(builder.queryMethods);
        this.customQueries = List.copyOf(builder.customQueries);
        this.hasNativeQueries = builder.hasNativeQueries;
        this.hasNamedQueries = builder.hasNamedQueries;
    }
    
    public String getInterfaceName() { return interfaceName; }
    public String getPackageName() { return packageName; }
    public String getEntityType() { return entityType; }
    public String getIdType() { return idType; }
    public List<String> getQueryMethods() { return queryMethods; }
    public List<String> getCustomQueries() { return customQueries; }
    public boolean hasNativeQueries() { return hasNativeQueries; }
    public boolean hasNamedQueries() { return hasNamedQueries; }
    
    /**
     * Gets the simple interface name (without package).
     */
    public String getSimpleInterfaceName() {
        if (interfaceName == null) return null;
        int lastDot = interfaceName.lastIndexOf('.');
        return lastDot >= 0 ? interfaceName.substring(lastDot + 1) : interfaceName;
    }
    
    /**
     * Gets the simple entity type name (without package).
     */
    public String getSimpleEntityType() {
        if (entityType == null) return null;
        int lastDot = entityType.lastIndexOf('.');
        return lastDot >= 0 ? entityType.substring(lastDot + 1) : entityType;
    }
    
    /**
     * Gets the simple ID type name (without package).
     */
    public String getSimpleIdType() {
        if (idType == null) return null;
        int lastDot = idType.lastIndexOf('.');
        return lastDot >= 0 ? idType.substring(lastDot + 1) : idType;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String interfaceName;
        private String packageName;
        private String entityType;
        private String idType;
        private List<String> queryMethods = List.of();
        private List<String> customQueries = List.of();
        private boolean hasNativeQueries = false;
        private boolean hasNamedQueries = false;
        
        public Builder interfaceName(String interfaceName) {
            this.interfaceName = interfaceName;
            return this;
        }
        
        public Builder packageName(String packageName) {
            this.packageName = packageName;
            return this;
        }
        
        public Builder entityType(String entityType) {
            this.entityType = entityType;
            return this;
        }
        
        public Builder idType(String idType) {
            this.idType = idType;
            return this;
        }
        
        public Builder queryMethods(List<String> queryMethods) {
            this.queryMethods = queryMethods != null ? queryMethods : List.of();
            return this;
        }
        
        public Builder customQueries(List<String> customQueries) {
            this.customQueries = customQueries != null ? customQueries : List.of();
            return this;
        }
        
        public Builder hasNativeQueries(boolean hasNativeQueries) {
            this.hasNativeQueries = hasNativeQueries;
            return this;
        }
        
        public Builder hasNamedQueries(boolean hasNamedQueries) {
            this.hasNamedQueries = hasNamedQueries;
            return this;
        }
        
        public JpaRepositoryInfo build() {
            Objects.requireNonNull(interfaceName, "Interface name is required");
            return new JpaRepositoryInfo(this);
        }
    }
    
    @Override
    public String toString() {
        return String.format("JpaRepositoryInfo{interface='%s', entity='%s', id='%s', methods=%d}",
            getSimpleInterfaceName(), getSimpleEntityType(), getSimpleIdType(), queryMethods.size());
    }
}