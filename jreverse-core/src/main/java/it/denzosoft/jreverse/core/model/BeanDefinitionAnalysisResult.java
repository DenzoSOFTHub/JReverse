package it.denzosoft.jreverse.core.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Result of detailed Spring bean definition analysis, providing comprehensive information
 * about bean factory methods, lifecycle hooks, scoping patterns, and dependency relationships.
 */
public class BeanDefinitionAnalysisResult {

    private final List<BeanDefinitionInfo> beanDefinitions;
    private final BeanDefinitionMetrics metrics;
    private final List<BeanLifecycleInfo> lifecycleHooks;
    private final List<BeanScopeInfo> customScopes;
    private final List<BeanDependencyRelation> dependencyRelations;
    private final List<String> warnings;
    private final List<String> errors;
    private final boolean successful;
    private final LocalDateTime analysisTime;
    private final long analysisTimeMs;

    private BeanDefinitionAnalysisResult(Builder builder) {
        this.beanDefinitions = Collections.unmodifiableList(new ArrayList<>(builder.beanDefinitions));
        this.metrics = builder.metrics;
        this.lifecycleHooks = Collections.unmodifiableList(new ArrayList<>(builder.lifecycleHooks));
        this.customScopes = Collections.unmodifiableList(new ArrayList<>(builder.customScopes));
        this.dependencyRelations = Collections.unmodifiableList(new ArrayList<>(builder.dependencyRelations));
        this.warnings = Collections.unmodifiableList(new ArrayList<>(builder.warnings));
        this.errors = Collections.unmodifiableList(new ArrayList<>(builder.errors));
        this.successful = builder.successful;
        this.analysisTime = builder.analysisTime;
        this.analysisTimeMs = builder.analysisTimeMs;
    }

    // Getters
    public List<BeanDefinitionInfo> getBeanDefinitions() { return beanDefinitions; }
    public BeanDefinitionMetrics getMetrics() { return metrics; }
    public List<BeanLifecycleInfo> getLifecycleHooks() { return lifecycleHooks; }
    public List<BeanScopeInfo> getCustomScopes() { return customScopes; }
    public List<BeanDependencyRelation> getDependencyRelations() { return dependencyRelations; }
    public List<String> getWarnings() { return warnings; }
    public List<String> getErrors() { return errors; }
    public boolean isSuccessful() { return successful; }
    public LocalDateTime getAnalysisTime() { return analysisTime; }
    public long getAnalysisTimeMs() { return analysisTimeMs; }

    /**
     * Gets bean definitions filtered by scope.
     */
    public List<BeanDefinitionInfo> getBeanDefinitionsByScope(BeanDefinitionInfo.BeanScope scope) {
        return beanDefinitions.stream()
            .filter(bean -> bean.getScope() == scope)
            .collect(Collectors.toList());
    }

    /**
     * Gets bean definitions that are factory beans (have factoryMethod).
     */
    public List<BeanDefinitionInfo> getFactoryBeans() {
        return beanDefinitions.stream()
            .filter(BeanDefinitionInfo::isFactoryBean)
            .collect(Collectors.toList());
    }

    /**
     * Gets bean definitions with lifecycle callbacks.
     */
    public List<BeanDefinitionInfo> getBeansWithLifecycleCallbacks() {
        return beanDefinitions.stream()
            .filter(BeanDefinitionInfo::hasLifecycleCallbacks)
            .collect(Collectors.toList());
    }

    /**
     * Gets bean definitions that are conditional (profile or conditional annotations).
     */
    public List<BeanDefinitionInfo> getConditionalBeans() {
        return beanDefinitions.stream()
            .filter(bean -> !bean.getProfiles().isEmpty())
            .collect(Collectors.toList());
    }

    /**
     * Checks if analysis found any beans.
     */
    public boolean hasBeansFound() {
        return !beanDefinitions.isEmpty();
    }

    /**
     * Checks if analysis has warnings or errors.
     */
    public boolean hasIssues() {
        return !warnings.isEmpty() || !errors.isEmpty();
    }

    // Static factory methods
    public static BeanDefinitionAnalysisResult success(List<BeanDefinitionInfo> beanDefinitions,
                                                     BeanDefinitionMetrics metrics,
                                                     List<BeanLifecycleInfo> lifecycleHooks,
                                                     List<BeanScopeInfo> customScopes,
                                                     List<BeanDependencyRelation> dependencyRelations,
                                                     long analysisTimeMs) {
        return builder()
            .beanDefinitions(beanDefinitions)
            .metrics(metrics)
            .lifecycleHooks(lifecycleHooks)
            .customScopes(customScopes)
            .dependencyRelations(dependencyRelations)
            .successful(true)
            .analysisTime(LocalDateTime.now())
            .analysisTimeMs(analysisTimeMs)
            .build();
    }

    public static BeanDefinitionAnalysisResult withWarnings(List<BeanDefinitionInfo> beanDefinitions,
                                                          BeanDefinitionMetrics metrics,
                                                          List<BeanLifecycleInfo> lifecycleHooks,
                                                          List<BeanScopeInfo> customScopes,
                                                          List<BeanDependencyRelation> dependencyRelations,
                                                          List<String> warnings,
                                                          long analysisTimeMs) {
        return builder()
            .beanDefinitions(beanDefinitions)
            .metrics(metrics)
            .lifecycleHooks(lifecycleHooks)
            .customScopes(customScopes)
            .dependencyRelations(dependencyRelations)
            .warnings(warnings)
            .successful(true)
            .analysisTime(LocalDateTime.now())
            .analysisTimeMs(analysisTimeMs)
            .build();
    }

    public static BeanDefinitionAnalysisResult noBeans() {
        return builder()
            .metrics(BeanDefinitionMetrics.empty())
            .successful(true)
            .analysisTime(LocalDateTime.now())
            .analysisTimeMs(0)
            .build();
    }

    public static BeanDefinitionAnalysisResult error(String errorMessage) {
        return builder()
            .addError(errorMessage)
            .metrics(BeanDefinitionMetrics.empty())
            .successful(false)
            .analysisTime(LocalDateTime.now())
            .analysisTimeMs(0)
            .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<BeanDefinitionInfo> beanDefinitions = new ArrayList<>();
        private BeanDefinitionMetrics metrics = BeanDefinitionMetrics.empty();
        private List<BeanLifecycleInfo> lifecycleHooks = new ArrayList<>();
        private List<BeanScopeInfo> customScopes = new ArrayList<>();
        private List<BeanDependencyRelation> dependencyRelations = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();
        private List<String> errors = new ArrayList<>();
        private boolean successful = false;
        private LocalDateTime analysisTime = LocalDateTime.now();
        private long analysisTimeMs = 0;

        public Builder beanDefinitions(List<BeanDefinitionInfo> beanDefinitions) {
            this.beanDefinitions = new ArrayList<>(beanDefinitions != null ? beanDefinitions : Collections.emptyList());
            return this;
        }

        public Builder addBeanDefinition(BeanDefinitionInfo beanDefinition) {
            if (beanDefinition != null) {
                this.beanDefinitions.add(beanDefinition);
            }
            return this;
        }

        public Builder metrics(BeanDefinitionMetrics metrics) {
            this.metrics = metrics != null ? metrics : BeanDefinitionMetrics.empty();
            return this;
        }

        public Builder lifecycleHooks(List<BeanLifecycleInfo> lifecycleHooks) {
            this.lifecycleHooks = new ArrayList<>(lifecycleHooks != null ? lifecycleHooks : Collections.emptyList());
            return this;
        }

        public Builder addLifecycleHook(BeanLifecycleInfo lifecycleHook) {
            if (lifecycleHook != null) {
                this.lifecycleHooks.add(lifecycleHook);
            }
            return this;
        }

        public Builder customScopes(List<BeanScopeInfo> customScopes) {
            this.customScopes = new ArrayList<>(customScopes != null ? customScopes : Collections.emptyList());
            return this;
        }

        public Builder addCustomScope(BeanScopeInfo customScope) {
            if (customScope != null) {
                this.customScopes.add(customScope);
            }
            return this;
        }

        public Builder dependencyRelations(List<BeanDependencyRelation> dependencyRelations) {
            this.dependencyRelations = new ArrayList<>(dependencyRelations != null ? dependencyRelations : Collections.emptyList());
            return this;
        }

        public Builder addDependencyRelation(BeanDependencyRelation dependencyRelation) {
            if (dependencyRelation != null) {
                this.dependencyRelations.add(dependencyRelation);
            }
            return this;
        }

        public Builder warnings(List<String> warnings) {
            this.warnings = new ArrayList<>(warnings != null ? warnings : Collections.emptyList());
            return this;
        }

        public Builder addWarning(String warning) {
            if (warning != null && !warning.trim().isEmpty()) {
                this.warnings.add(warning.trim());
            }
            return this;
        }

        public Builder errors(List<String> errors) {
            this.errors = new ArrayList<>(errors != null ? errors : Collections.emptyList());
            return this;
        }

        public Builder addError(String error) {
            if (error != null && !error.trim().isEmpty()) {
                this.errors.add(error.trim());
            }
            return this;
        }

        public Builder successful(boolean successful) {
            this.successful = successful;
            return this;
        }

        public Builder analysisTime(LocalDateTime analysisTime) {
            this.analysisTime = analysisTime != null ? analysisTime : LocalDateTime.now();
            return this;
        }

        public Builder analysisTimeMs(long analysisTimeMs) {
            this.analysisTimeMs = analysisTimeMs;
            return this;
        }

        public BeanDefinitionAnalysisResult build() {
            return new BeanDefinitionAnalysisResult(this);
        }
    }

    @Override
    public String toString() {
        return String.format("BeanDefinitionAnalysisResult{beans=%d, successful=%b, warnings=%d, errors=%d}",
            beanDefinitions.size(), successful, warnings.size(), errors.size());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BeanDefinitionAnalysisResult that = (BeanDefinitionAnalysisResult) obj;
        return successful == that.successful &&
               analysisTimeMs == that.analysisTimeMs &&
               Objects.equals(beanDefinitions, that.beanDefinitions) &&
               Objects.equals(metrics, that.metrics) &&
               Objects.equals(warnings, that.warnings) &&
               Objects.equals(errors, that.errors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(beanDefinitions, metrics, warnings, errors, successful, analysisTimeMs);
    }
}