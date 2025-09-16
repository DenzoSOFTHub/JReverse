package it.denzosoft.jreverse.core.model;

/**
 * Metrics related to bean definition analysis, providing quantitative insights
 * into bean patterns, lifecycle hooks, scoping, and dependency relationships.
 */
public class BeanDefinitionMetrics {

    private final int totalBeanDefinitions;
    private final int factoryBeans;
    private final int componentBeans;
    private final int singletonBeans;
    private final int prototypeBeans;
    private final int requestScopedBeans;
    private final int sessionScopedBeans;
    private final int customScopedBeans;
    private final int beansWithInitMethods;
    private final int beansWithDestroyMethods;
    private final int primaryBeans;
    private final int lazyBeans;
    private final int conditionalBeans;
    private final int qualifiedBeans;
    private final int totalDependencies;
    private final int circularDependencies;
    private final double averageDependenciesPerBean;

    private BeanDefinitionMetrics(Builder builder) {
        this.totalBeanDefinitions = builder.totalBeanDefinitions;
        this.factoryBeans = builder.factoryBeans;
        this.componentBeans = builder.componentBeans;
        this.singletonBeans = builder.singletonBeans;
        this.prototypeBeans = builder.prototypeBeans;
        this.requestScopedBeans = builder.requestScopedBeans;
        this.sessionScopedBeans = builder.sessionScopedBeans;
        this.customScopedBeans = builder.customScopedBeans;
        this.beansWithInitMethods = builder.beansWithInitMethods;
        this.beansWithDestroyMethods = builder.beansWithDestroyMethods;
        this.primaryBeans = builder.primaryBeans;
        this.lazyBeans = builder.lazyBeans;
        this.conditionalBeans = builder.conditionalBeans;
        this.qualifiedBeans = builder.qualifiedBeans;
        this.totalDependencies = builder.totalDependencies;
        this.circularDependencies = builder.circularDependencies;
        this.averageDependenciesPerBean = builder.averageDependenciesPerBean;
    }

    // Getters
    public int getTotalBeanDefinitions() { return totalBeanDefinitions; }
    public int getFactoryBeans() { return factoryBeans; }
    public int getComponentBeans() { return componentBeans; }
    public int getSingletonBeans() { return singletonBeans; }
    public int getPrototypeBeans() { return prototypeBeans; }
    public int getRequestScopedBeans() { return requestScopedBeans; }
    public int getSessionScopedBeans() { return sessionScopedBeans; }
    public int getCustomScopedBeans() { return customScopedBeans; }
    public int getBeansWithInitMethods() { return beansWithInitMethods; }
    public int getBeansWithDestroyMethods() { return beansWithDestroyMethods; }
    public int getPrimaryBeans() { return primaryBeans; }
    public int getLazyBeans() { return lazyBeans; }
    public int getConditionalBeans() { return conditionalBeans; }
    public int getQualifiedBeans() { return qualifiedBeans; }
    public int getTotalDependencies() { return totalDependencies; }
    public int getCircularDependencies() { return circularDependencies; }
    public double getAverageDependenciesPerBean() { return averageDependenciesPerBean; }

    /**
     * Calculates the percentage of factory beans vs component beans.
     */
    public double getFactoryBeanPercentage() {
        return totalBeanDefinitions > 0 ? (factoryBeans * 100.0) / totalBeanDefinitions : 0.0;
    }

    /**
     * Calculates the percentage of singleton beans.
     */
    public double getSingletonPercentage() {
        return totalBeanDefinitions > 0 ? (singletonBeans * 100.0) / totalBeanDefinitions : 0.0;
    }

    /**
     * Calculates the percentage of beans with lifecycle callbacks.
     */
    public double getLifecycleBeansPercentage() {
        int lifecycleBeans = beansWithInitMethods + beansWithDestroyMethods;
        return totalBeanDefinitions > 0 ? (lifecycleBeans * 100.0) / totalBeanDefinitions : 0.0;
    }

    /**
     * Indicates complexity based on multiple factors.
     */
    public BeanComplexityLevel getComplexityLevel() {
        if (totalBeanDefinitions == 0) {
            return BeanComplexityLevel.NONE;
        }

        double complexityScore = 0;
        complexityScore += circularDependencies * 3; // High penalty for circular deps
        complexityScore += customScopedBeans * 2; // Custom scopes add complexity
        complexityScore += conditionalBeans * 1.5; // Conditional beans add complexity
        complexityScore += (averageDependenciesPerBean - 2) * totalBeanDefinitions; // High dependency complexity

        double normalizedScore = complexityScore / totalBeanDefinitions;

        if (normalizedScore < 2) return BeanComplexityLevel.LOW;
        if (normalizedScore < 5) return BeanComplexityLevel.MEDIUM;
        if (normalizedScore < 10) return BeanComplexityLevel.HIGH;
        return BeanComplexityLevel.VERY_HIGH;
    }

    public static BeanDefinitionMetrics empty() {
        return new Builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public enum BeanComplexityLevel {
        NONE("No beans found"),
        LOW("Simple bean configuration"),
        MEDIUM("Moderate complexity with some advanced patterns"),
        HIGH("Complex configuration with multiple patterns"),
        VERY_HIGH("Very complex with circular dependencies or advanced patterns");

        private final String description;

        BeanComplexityLevel(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public static class Builder {
        private int totalBeanDefinitions = 0;
        private int factoryBeans = 0;
        private int componentBeans = 0;
        private int singletonBeans = 0;
        private int prototypeBeans = 0;
        private int requestScopedBeans = 0;
        private int sessionScopedBeans = 0;
        private int customScopedBeans = 0;
        private int beansWithInitMethods = 0;
        private int beansWithDestroyMethods = 0;
        private int primaryBeans = 0;
        private int lazyBeans = 0;
        private int conditionalBeans = 0;
        private int qualifiedBeans = 0;
        private int totalDependencies = 0;
        private int circularDependencies = 0;
        private double averageDependenciesPerBean = 0.0;

        public Builder totalBeanDefinitions(int totalBeanDefinitions) {
            this.totalBeanDefinitions = totalBeanDefinitions;
            return this;
        }

        public Builder factoryBeans(int factoryBeans) {
            this.factoryBeans = factoryBeans;
            return this;
        }

        public Builder componentBeans(int componentBeans) {
            this.componentBeans = componentBeans;
            return this;
        }

        public Builder singletonBeans(int singletonBeans) {
            this.singletonBeans = singletonBeans;
            return this;
        }

        public Builder prototypeBeans(int prototypeBeans) {
            this.prototypeBeans = prototypeBeans;
            return this;
        }

        public Builder requestScopedBeans(int requestScopedBeans) {
            this.requestScopedBeans = requestScopedBeans;
            return this;
        }

        public Builder sessionScopedBeans(int sessionScopedBeans) {
            this.sessionScopedBeans = sessionScopedBeans;
            return this;
        }

        public Builder customScopedBeans(int customScopedBeans) {
            this.customScopedBeans = customScopedBeans;
            return this;
        }

        public Builder beansWithInitMethods(int beansWithInitMethods) {
            this.beansWithInitMethods = beansWithInitMethods;
            return this;
        }

        public Builder beansWithDestroyMethods(int beansWithDestroyMethods) {
            this.beansWithDestroyMethods = beansWithDestroyMethods;
            return this;
        }

        public Builder primaryBeans(int primaryBeans) {
            this.primaryBeans = primaryBeans;
            return this;
        }

        public Builder lazyBeans(int lazyBeans) {
            this.lazyBeans = lazyBeans;
            return this;
        }

        public Builder conditionalBeans(int conditionalBeans) {
            this.conditionalBeans = conditionalBeans;
            return this;
        }

        public Builder qualifiedBeans(int qualifiedBeans) {
            this.qualifiedBeans = qualifiedBeans;
            return this;
        }

        public Builder totalDependencies(int totalDependencies) {
            this.totalDependencies = totalDependencies;
            return this;
        }

        public Builder circularDependencies(int circularDependencies) {
            this.circularDependencies = circularDependencies;
            return this;
        }

        public Builder averageDependenciesPerBean(double averageDependenciesPerBean) {
            this.averageDependenciesPerBean = averageDependenciesPerBean;
            return this;
        }

        public BeanDefinitionMetrics build() {
            return new BeanDefinitionMetrics(this);
        }
    }

    @Override
    public String toString() {
        return String.format("BeanDefinitionMetrics{total=%d, factory=%d, singleton=%d, complexity=%s}",
            totalBeanDefinitions, factoryBeans, singletonBeans, getComplexityLevel());
    }
}