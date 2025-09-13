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
    
    private PropertyAnalysisResult(List<PropertyUsageInfo> valueInjections,
                                 List<ConfigurationPropertiesInfo> configurationProperties,
                                 List<PropertySourceInfo> propertySources,
                                 Map<String, List<String>> propertyReferences,
                                 AnalysisMetadata metadata) {
        this.valueInjections = Collections.unmodifiableList(valueInjections);
        this.configurationProperties = Collections.unmodifiableList(configurationProperties);
        this.propertySources = Collections.unmodifiableList(propertySources);
        this.propertyReferences = Collections.unmodifiableMap(propertyReferences);
        this.metadata = metadata;
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
        
        public PropertyAnalysisResult build() {
            return new PropertyAnalysisResult(valueInjections, configurationProperties, 
                                            propertySources, propertyReferences, metadata);
        }
    }
}