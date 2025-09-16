package it.denzosoft.jreverse.analyzer.beandefinition;

import java.util.List;

/**
 * Legacy result class for bean definition analysis (for test compatibility).
 */
public class BeanDefinitionAnalysisResult {

    private final boolean successful;
    private final String errorMessage;
    private final List<BeanDefinitionInfo> beanDefinitions;
    private final List<String> errors;
    private final BeanDefinitionMetrics metrics;
    private final List<BeanLifecycleInfo> lifecycleHooks;

    private BeanDefinitionAnalysisResult(boolean successful, String errorMessage,
                                        List<BeanDefinitionInfo> beanDefinitions, List<String> errors,
                                        BeanDefinitionMetrics metrics, List<BeanLifecycleInfo> lifecycleHooks) {
        this.successful = successful;
        this.errorMessage = errorMessage;
        this.beanDefinitions = beanDefinitions != null ? List.copyOf(beanDefinitions) : List.of();
        this.errors = errors != null ? List.copyOf(errors) : List.of();
        this.metrics = metrics != null ? metrics : new BeanDefinitionMetrics();
        this.lifecycleHooks = lifecycleHooks != null ? List.copyOf(lifecycleHooks) : List.of();
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public boolean isSuccessful() { return successful; }
    public String getErrorMessage() { return errorMessage; }
    public List<BeanDefinitionInfo> getBeanDefinitions() { return beanDefinitions; }
    public List<String> getErrors() { return errors; }
    public BeanDefinitionMetrics getMetrics() { return metrics; }
    public List<BeanLifecycleInfo> getLifecycleHooks() { return lifecycleHooks; }

    public static class Builder {
        private boolean successful = true;
        private String errorMessage;
        private List<BeanDefinitionInfo> beanDefinitions = List.of();
        private List<String> errors = List.of();
        private BeanDefinitionMetrics metrics;
        private List<BeanLifecycleInfo> lifecycleHooks = List.of();

        public Builder successful(boolean successful) {
            this.successful = successful;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder beanDefinitions(List<BeanDefinitionInfo> beanDefinitions) {
            this.beanDefinitions = beanDefinitions;
            return this;
        }

        public Builder errors(List<String> errors) {
            this.errors = errors;
            return this;
        }

        public Builder metrics(BeanDefinitionMetrics metrics) {
            this.metrics = metrics;
            return this;
        }

        public Builder lifecycleHooks(List<BeanLifecycleInfo> lifecycleHooks) {
            this.lifecycleHooks = lifecycleHooks;
            return this;
        }

        public BeanDefinitionAnalysisResult build() {
            if (metrics == null) {
                metrics = new BeanDefinitionMetrics();
            }
            return new BeanDefinitionAnalysisResult(successful, errorMessage, beanDefinitions, errors, metrics, lifecycleHooks);
        }
    }
}