package it.denzosoft.jreverse.core.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Analysis result containing information about Spring Boot bean creation patterns and lifecycle.
 */
public class BeanCreationAnalysisResult {
    
    private final List<BeanFactoryInfo> beanFactories;
    private final List<BeanLifecycleInfo> lifecycleCallbacks;
    private final List<BeanCreationPattern> creationPatterns;
    private final Map<String, List<String>> beanDependencies;
    private final AnalysisMetadata metadata;
    
    private BeanCreationAnalysisResult(List<BeanFactoryInfo> beanFactories,
                                     List<BeanLifecycleInfo> lifecycleCallbacks,
                                     List<BeanCreationPattern> creationPatterns,
                                     Map<String, List<String>> beanDependencies,
                                     AnalysisMetadata metadata) {
        this.beanFactories = Collections.unmodifiableList(beanFactories);
        this.lifecycleCallbacks = Collections.unmodifiableList(lifecycleCallbacks);
        this.creationPatterns = Collections.unmodifiableList(creationPatterns);
        this.beanDependencies = Collections.unmodifiableMap(beanDependencies);
        this.metadata = metadata;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public List<BeanFactoryInfo> getBeanFactories() {
        return beanFactories;
    }
    
    public List<BeanLifecycleInfo> getLifecycleCallbacks() {
        return lifecycleCallbacks;
    }
    
    public List<BeanCreationPattern> getCreationPatterns() {
        return creationPatterns;
    }
    
    public Map<String, List<String>> getBeanDependencies() {
        return beanDependencies;
    }
    
    public AnalysisMetadata getMetadata() {
        return metadata;
    }
    
    public int getBeanFactoryCount() {
        return beanFactories.size();
    }
    
    public int getLifecycleCallbackCount() {
        return lifecycleCallbacks.size();
    }
    
    public int getCreationPatternCount() {
        return creationPatterns.size();
    }
    
    public int getTotalBeanDependencies() {
        return beanDependencies.values().stream()
                .mapToInt(List::size)
                .sum();
    }
    
    @Override
    public String toString() {
        return "BeanCreationAnalysisResult{" +
                "beanFactories=" + beanFactories.size() +
                ", lifecycleCallbacks=" + lifecycleCallbacks.size() +
                ", creationPatterns=" + creationPatterns.size() +
                ", beanDependencies=" + beanDependencies.size() +
                '}';
    }
    
    public static class Builder {
        private List<BeanFactoryInfo> beanFactories = Collections.emptyList();
        private List<BeanLifecycleInfo> lifecycleCallbacks = Collections.emptyList();
        private List<BeanCreationPattern> creationPatterns = Collections.emptyList();
        private Map<String, List<String>> beanDependencies = Collections.emptyMap();
        private AnalysisMetadata metadata = AnalysisMetadata.successful();
        
        public Builder beanFactories(List<BeanFactoryInfo> beanFactories) {
            this.beanFactories = beanFactories != null ? beanFactories : Collections.emptyList();
            return this;
        }
        
        public Builder lifecycleCallbacks(List<BeanLifecycleInfo> lifecycleCallbacks) {
            this.lifecycleCallbacks = lifecycleCallbacks != null ? lifecycleCallbacks : Collections.emptyList();
            return this;
        }
        
        public Builder creationPatterns(List<BeanCreationPattern> creationPatterns) {
            this.creationPatterns = creationPatterns != null ? creationPatterns : Collections.emptyList();
            return this;
        }
        
        public Builder beanDependencies(Map<String, List<String>> beanDependencies) {
            this.beanDependencies = beanDependencies != null ? beanDependencies : Collections.emptyMap();
            return this;
        }
        
        public Builder metadata(AnalysisMetadata metadata) {
            this.metadata = metadata != null ? metadata : AnalysisMetadata.successful();
            return this;
        }
        
        public BeanCreationAnalysisResult build() {
            return new BeanCreationAnalysisResult(beanFactories, lifecycleCallbacks, 
                                                creationPatterns, beanDependencies, metadata);
        }
    }
}