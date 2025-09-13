package it.denzosoft.jreverse.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Result of Spring configuration analysis containing all detected configurations and beans.
 */
public class ConfigurationAnalysisResult {
    
    private final List<String> configurationClasses;
    private final List<BeanDefinitionInfo> beanDefinitions;
    private final Map<String, List<BeanDefinitionInfo>> beansByConfiguration;
    private final Map<BeanDefinitionInfo.BeanScope, Integer> beansByScope;
    private final AnalysisMetadata metadata;
    private final long analysisTimeMs;
    
    private ConfigurationAnalysisResult(List<String> configurationClasses,
                                      List<BeanDefinitionInfo> beanDefinitions,
                                      Map<String, List<BeanDefinitionInfo>> beansByConfiguration,
                                      Map<BeanDefinitionInfo.BeanScope, Integer> beansByScope,
                                      AnalysisMetadata metadata,
                                      long analysisTimeMs) {
        this.configurationClasses = Collections.unmodifiableList(new ArrayList<>(configurationClasses));
        this.beanDefinitions = Collections.unmodifiableList(new ArrayList<>(beanDefinitions));
        this.beansByConfiguration = Collections.unmodifiableMap(new HashMap<>(beansByConfiguration));
        this.beansByScope = Collections.unmodifiableMap(new HashMap<>(beansByScope));
        this.metadata = metadata;
        this.analysisTimeMs = analysisTimeMs;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static ConfigurationAnalysisResult noConfigurations() {
        return builder()
            .metadata(AnalysisMetadata.warning("No Spring configuration classes found"))
            .build();
    }
    
    public static ConfigurationAnalysisResult error(String errorMessage) {
        return builder()
            .metadata(AnalysisMetadata.error(errorMessage))
            .build();
    }
    
    public List<String> getConfigurationClasses() {
        return configurationClasses;
    }
    
    public List<BeanDefinitionInfo> getBeanDefinitions() {
        return beanDefinitions;
    }
    
    public Map<String, List<BeanDefinitionInfo>> getBeansByConfiguration() {
        return beansByConfiguration;
    }
    
    public Map<BeanDefinitionInfo.BeanScope, Integer> getBeansByScope() {
        return beansByScope;
    }
    
    public AnalysisMetadata getMetadata() {
        return metadata;
    }
    
    public long getAnalysisTimeMs() {
        return analysisTimeMs;
    }
    
    public int getConfigurationCount() {
        return configurationClasses.size();
    }
    
    public int getBeanDefinitionCount() {
        return beanDefinitions.size();
    }
    
    public boolean hasConfigurations() {
        return !configurationClasses.isEmpty();
    }
    
    public boolean hasBeanDefinitions() {
        return !beanDefinitions.isEmpty();
    }
    
    public boolean isSuccessful() {
        return metadata.isSuccessful();
    }
    
    public List<BeanDefinitionInfo> getBeansForConfiguration(String configurationClass) {
        return beansByConfiguration.getOrDefault(configurationClass, Collections.emptyList());
    }
    
    public List<BeanDefinitionInfo> getBeansByScope(BeanDefinitionInfo.BeanScope scope) {
        return beanDefinitions.stream()
                .filter(bean -> bean.getScope() == scope)
                .collect(Collectors.toList());
    }
    
    public List<BeanDefinitionInfo> getPrimaryBeans() {
        return beanDefinitions.stream()
                .filter(BeanDefinitionInfo::isPrimary)
                .collect(Collectors.toList());
    }
    
    public List<BeanDefinitionInfo> getLazyBeans() {
        return beanDefinitions.stream()
                .filter(BeanDefinitionInfo::isLazy)
                .collect(Collectors.toList());
    }
    
    public List<BeanDefinitionInfo> getFactoryBeans() {
        return beanDefinitions.stream()
                .filter(BeanDefinitionInfo::isFactoryBean)
                .collect(Collectors.toList());
    }
    
    public List<BeanDefinitionInfo> getBeansWithLifecycleCallbacks() {
        return beanDefinitions.stream()
                .filter(BeanDefinitionInfo::hasLifecycleCallbacks)
                .collect(Collectors.toList());
    }
    
    public List<BeanDefinitionInfo> getBeansForProfile(String profile) {
        return beanDefinitions.stream()
                .filter(bean -> bean.isActiveInProfile(profile))
                .collect(Collectors.toList());
    }
    
    public ConfigurationAnalysisResult withAnalysisTime(long analysisTimeMs) {
        return new ConfigurationAnalysisResult(configurationClasses, beanDefinitions, 
                                              beansByConfiguration, beansByScope, metadata, analysisTimeMs);
    }
    
    @Override
    public String toString() {
        return "ConfigurationAnalysisResult{" +
                "configurations=" + configurationClasses.size() +
                ", beans=" + beanDefinitions.size() +
                ", analysisTime=" + analysisTimeMs + "ms" +
                '}';
    }
    
    public static class Builder {
        private List<String> configurationClasses = new ArrayList<>();
        private List<BeanDefinitionInfo> beanDefinitions = new ArrayList<>();
        private AnalysisMetadata metadata = AnalysisMetadata.successful();
        private long analysisTimeMs = 0L;
        
        public Builder addConfigurationClass(String configurationClass) {
            if (configurationClass != null && !configurationClass.trim().isEmpty()) {
                this.configurationClasses.add(configurationClass.trim());
            }
            return this;
        }
        
        public Builder configurationClasses(List<String> configurationClasses) {
            this.configurationClasses = new ArrayList<>(configurationClasses != null ? configurationClasses : Collections.emptyList());
            return this;
        }
        
        public Builder addBeanDefinition(BeanDefinitionInfo beanDefinition) {
            if (beanDefinition != null) {
                this.beanDefinitions.add(beanDefinition);
            }
            return this;
        }
        
        public Builder beanDefinitions(List<BeanDefinitionInfo> beanDefinitions) {
            this.beanDefinitions = new ArrayList<>(beanDefinitions != null ? beanDefinitions : Collections.emptyList());
            return this;
        }
        
        public Builder metadata(AnalysisMetadata metadata) {
            this.metadata = metadata != null ? metadata : AnalysisMetadata.successful();
            return this;
        }
        
        public Builder analysisTimeMs(long analysisTimeMs) {
            this.analysisTimeMs = Math.max(0L, analysisTimeMs);
            return this;
        }
        
        public ConfigurationAnalysisResult build() {
            // Group beans by configuration
            Map<String, List<BeanDefinitionInfo>> beansByConfiguration = beanDefinitions.stream()
                    .filter(bean -> bean.getDeclaringClass() != null)
                    .collect(Collectors.groupingBy(BeanDefinitionInfo::getDeclaringClass));
            
            // Count beans by scope
            Map<BeanDefinitionInfo.BeanScope, Integer> beansByScope = new HashMap<>();
            for (BeanDefinitionInfo bean : beanDefinitions) {
                beansByScope.merge(bean.getScope(), 1, Integer::sum);
            }
            
            return new ConfigurationAnalysisResult(configurationClasses, beanDefinitions, 
                                                  beansByConfiguration, beansByScope, metadata, analysisTimeMs);
        }
    }
}