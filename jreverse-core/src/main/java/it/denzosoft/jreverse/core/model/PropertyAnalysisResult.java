package it.denzosoft.jreverse.core.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Analysis result containing information about Spring Boot property configurations and usage.
 */
public class PropertyAnalysisResult {
    
    private final List<PropertyUsageInfo> valueInjections;
    private final List<ConfigurationPropertiesInfo> configurationProperties;
    private final List<PropertySourceInfo> propertySources;
    private final Map<String, List<String>> propertyReferences;
    private final AnalysisMetadata metadata;

    // Advanced analysis fields
    private final Map<String, PropertyFileContent> propertyFiles;
    private final PropertyHierarchyAnalysis hierarchyAnalysis;
    private final PropertySecurityAnalysis securityAnalysis;
    private final PropertyUsageAnalysis usageAnalysis;
    
    private PropertyAnalysisResult(Builder builder) {
        this.valueInjections = Collections.unmodifiableList(builder.valueInjections);
        this.configurationProperties = Collections.unmodifiableList(builder.configurationProperties);
        this.propertySources = Collections.unmodifiableList(builder.propertySources);
        this.propertyReferences = Collections.unmodifiableMap(builder.propertyReferences);
        this.metadata = builder.metadata;

        // Advanced analysis fields
        this.propertyFiles = Collections.unmodifiableMap(builder.propertyFiles);
        this.hierarchyAnalysis = builder.hierarchyAnalysis;
        this.securityAnalysis = builder.securityAnalysis;
        this.usageAnalysis = builder.usageAnalysis;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public List<PropertyUsageInfo> getValueInjections() {
        return valueInjections;
    }
    
    public List<ConfigurationPropertiesInfo> getConfigurationProperties() {
        return configurationProperties;
    }
    
    public List<PropertySourceInfo> getPropertySources() {
        return propertySources;
    }
    
    public Map<String, List<String>> getPropertyReferences() {
        return propertyReferences;
    }
    
    public AnalysisMetadata getMetadata() {
        return metadata;
    }

    // Advanced analysis getters
    public Map<String, PropertyFileContent> getPropertyFiles() {
        return propertyFiles;
    }

    public PropertyHierarchyAnalysis getHierarchyAnalysis() {
        return hierarchyAnalysis;
    }

    public PropertySecurityAnalysis getSecurityAnalysis() {
        return securityAnalysis;
    }

    public PropertyUsageAnalysis getUsageAnalysis() {
        return usageAnalysis;
    }

    /**
     * Checks if advanced analysis was performed.
     */
    public boolean hasAdvancedAnalysis() {
        return propertyFiles != null && !propertyFiles.isEmpty();
    }

    /**
     * Gets the total number of parsed property files.
     */
    public int getPropertyFileCount() {
        return propertyFiles != null ? propertyFiles.size() : 0;
    }
    
    public int getValueInjectionCount() {
        return valueInjections.size();
    }
    
    public int getConfigurationPropertiesCount() {
        return configurationProperties.size();
    }
    
    public int getPropertySourceCount() {
        return propertySources.size();
    }
    
    public int getTotalPropertyReferences() {
        return propertyReferences.values().stream()
                .mapToInt(List::size)
                .sum();
    }
    
    @Override
    public String toString() {
        return "PropertyAnalysisResult{" +
                "valueInjections=" + valueInjections.size() +
                ", configurationProperties=" + configurationProperties.size() +
                ", propertySources=" + propertySources.size() +
                ", propertyReferences=" + propertyReferences.size() +
                '}';
    }
    
    public static class Builder {
        private List<PropertyUsageInfo> valueInjections = Collections.emptyList();
        private List<ConfigurationPropertiesInfo> configurationProperties = Collections.emptyList();
        private List<PropertySourceInfo> propertySources = Collections.emptyList();
        private Map<String, List<String>> propertyReferences = Collections.emptyMap();
        private AnalysisMetadata metadata = AnalysisMetadata.successful();

        // Advanced analysis fields
        private Map<String, PropertyFileContent> propertyFiles = Collections.emptyMap();
        private PropertyHierarchyAnalysis hierarchyAnalysis;
        private PropertySecurityAnalysis securityAnalysis;
        private PropertyUsageAnalysis usageAnalysis;
        
        public Builder valueInjections(List<PropertyUsageInfo> valueInjections) {
            this.valueInjections = valueInjections != null ? valueInjections : Collections.emptyList();
            return this;
        }
        
        public Builder configurationProperties(List<ConfigurationPropertiesInfo> configurationProperties) {
            this.configurationProperties = configurationProperties != null ? configurationProperties : Collections.emptyList();
            return this;
        }
        
        public Builder propertySources(List<PropertySourceInfo> propertySources) {
            this.propertySources = propertySources != null ? propertySources : Collections.emptyList();
            return this;
        }
        
        public Builder propertyReferences(Map<String, List<String>> propertyReferences) {
            this.propertyReferences = propertyReferences != null ? propertyReferences : Collections.emptyMap();
            return this;
        }
        
        public Builder metadata(AnalysisMetadata metadata) {
            this.metadata = metadata != null ? metadata : AnalysisMetadata.successful();
            return this;
        }

        // Advanced analysis builder methods
        public Builder propertyFiles(Map<String, PropertyFileContent> propertyFiles) {
            this.propertyFiles = propertyFiles != null ? propertyFiles : Collections.emptyMap();
            return this;
        }

        public Builder hierarchyAnalysis(PropertyHierarchyAnalysis hierarchyAnalysis) {
            this.hierarchyAnalysis = hierarchyAnalysis;
            return this;
        }

        public Builder securityAnalysis(PropertySecurityAnalysis securityAnalysis) {
            this.securityAnalysis = securityAnalysis;
            return this;
        }

        public Builder usageAnalysis(PropertyUsageAnalysis usageAnalysis) {
            this.usageAnalysis = usageAnalysis;
            return this;
        }

        public PropertyAnalysisResult build() {
            return new PropertyAnalysisResult(this);
        }
    }
}