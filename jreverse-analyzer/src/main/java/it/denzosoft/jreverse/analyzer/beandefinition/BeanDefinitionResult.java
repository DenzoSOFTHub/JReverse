package it.denzosoft.jreverse.analyzer.beandefinition;

import it.denzosoft.jreverse.core.model.ClassInfo;

import java.util.*;

/**
 * Result of bean definition analysis containing information about @Bean methods,
 * configuration classes, and dependency relationships.
 */
public class BeanDefinitionResult {

    private final Set<ClassInfo> configurationClasses;
    private final List<BeanMethodInfo> beanMethods;
    private final Map<String, BeanLifecycleInfo> lifecycleInfo;
    private final Map<String, Set<String>> dependencyGraph;
    private final int totalBeanDefinitions;
    private final boolean successful;
    private final String errorMessage;

    private BeanDefinitionResult(Set<ClassInfo> configurationClasses,
                                List<BeanMethodInfo> beanMethods,
                                Map<String, BeanLifecycleInfo> lifecycleInfo,
                                Map<String, Set<String>> dependencyGraph,
                                int totalBeanDefinitions,
                                boolean successful,
                                String errorMessage) {
        this.configurationClasses = configurationClasses != null ? Set.copyOf(configurationClasses) : Set.of();
        this.beanMethods = beanMethods != null ? List.copyOf(beanMethods) : List.of();
        this.lifecycleInfo = lifecycleInfo != null ? Map.copyOf(lifecycleInfo) : Map.of();
        this.dependencyGraph = dependencyGraph != null ? Map.copyOf(dependencyGraph) : Map.of();
        this.totalBeanDefinitions = totalBeanDefinitions;
        this.successful = successful;
        this.errorMessage = errorMessage;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static BeanDefinitionResult error(String errorMessage) {
        return new BeanDefinitionResult(null, null, null, null, 0, false, errorMessage);
    }

    // Getters
    public Set<ClassInfo> getConfigurationClasses() { return configurationClasses; }
    public List<BeanMethodInfo> getBeanMethods() { return beanMethods; }
    public Map<String, BeanLifecycleInfo> getLifecycleInfo() { return lifecycleInfo; }
    public Map<String, Set<String>> getDependencyGraph() { return dependencyGraph; }
    public int getTotalBeanDefinitions() { return totalBeanDefinitions; }
    public boolean isSuccessful() { return successful; }
    public String getErrorMessage() { return errorMessage; }

    public static class Builder {
        private Set<ClassInfo> configurationClasses = new HashSet<>();
        private List<BeanMethodInfo> beanMethods = new ArrayList<>();
        private Map<String, BeanLifecycleInfo> lifecycleInfo = new HashMap<>();
        private Map<String, Set<String>> dependencyGraph = new HashMap<>();
        private int totalBeanDefinitions = 0;

        public Builder configurationClasses(Set<ClassInfo> configurationClasses) {
            this.configurationClasses = configurationClasses;
            return this;
        }

        public Builder beanMethods(List<BeanMethodInfo> beanMethods) {
            this.beanMethods = beanMethods;
            return this;
        }

        public Builder lifecycleInfo(Map<String, BeanLifecycleInfo> lifecycleInfo) {
            this.lifecycleInfo = lifecycleInfo;
            return this;
        }

        public Builder dependencyGraph(Map<String, Set<String>> dependencyGraph) {
            this.dependencyGraph = dependencyGraph;
            return this;
        }

        public Builder totalBeanDefinitions(int totalBeanDefinitions) {
            this.totalBeanDefinitions = totalBeanDefinitions;
            return this;
        }

        public BeanDefinitionResult build() {
            return new BeanDefinitionResult(
                configurationClasses,
                beanMethods,
                lifecycleInfo,
                dependencyGraph,
                totalBeanDefinitions,
                true,
                null
            );
        }
    }
}