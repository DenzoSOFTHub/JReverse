package it.denzosoft.jreverse.analyzer.beandefinition;

/**
 * Metrics for bean definition analysis.
 */
public class BeanDefinitionMetrics {

    private final int totalBeanDefinitions;
    private final int totalSingletonBeans;
    private final int totalPrototypeBeans;
    private final int totalPrimaryBeans;
    private final int totalConditionalBeans;
    private final int totalProfileSpecificBeans;
    private final int averageDependenciesPerBean;
    private final int factoryBeans;
    private final int componentBeans;
    private final int singletonBeans;

    public BeanDefinitionMetrics() {
        this(0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    public BeanDefinitionMetrics(int totalBeanDefinitions, int totalSingletonBeans,
                                int totalPrototypeBeans, int totalPrimaryBeans,
                                int totalConditionalBeans, int totalProfileSpecificBeans,
                                int averageDependenciesPerBean, int factoryBeans,
                                int componentBeans, int singletonBeans) {
        this.totalBeanDefinitions = totalBeanDefinitions;
        this.totalSingletonBeans = totalSingletonBeans;
        this.totalPrototypeBeans = totalPrototypeBeans;
        this.totalPrimaryBeans = totalPrimaryBeans;
        this.totalConditionalBeans = totalConditionalBeans;
        this.totalProfileSpecificBeans = totalProfileSpecificBeans;
        this.averageDependenciesPerBean = averageDependenciesPerBean;
        this.factoryBeans = factoryBeans;
        this.componentBeans = componentBeans;
        this.singletonBeans = singletonBeans;
    }

    // Getters
    public int getTotalBeanDefinitions() { return totalBeanDefinitions; }
    public int getTotalSingletonBeans() { return totalSingletonBeans; }
    public int getTotalPrototypeBeans() { return totalPrototypeBeans; }
    public int getTotalPrimaryBeans() { return totalPrimaryBeans; }
    public int getTotalConditionalBeans() { return totalConditionalBeans; }
    public int getTotalProfileSpecificBeans() { return totalProfileSpecificBeans; }
    public int getAverageDependenciesPerBean() { return averageDependenciesPerBean; }
    public int getFactoryBeans() { return factoryBeans; }
    public int getComponentBeans() { return componentBeans; }
    public int getSingletonBeans() { return singletonBeans; }
    public int getPrimaryBeans() { return totalPrimaryBeans; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int totalBeanDefinitions;
        private int totalSingletonBeans;
        private int totalPrototypeBeans;
        private int totalPrimaryBeans;
        private int totalConditionalBeans;
        private int totalProfileSpecificBeans;
        private int averageDependenciesPerBean;
        private int factoryBeans;
        private int componentBeans;
        private int singletonBeans;

        public Builder totalBeanDefinitions(int totalBeanDefinitions) {
            this.totalBeanDefinitions = totalBeanDefinitions;
            return this;
        }

        public Builder totalSingletonBeans(int totalSingletonBeans) {
            this.totalSingletonBeans = totalSingletonBeans;
            return this;
        }

        public Builder totalPrototypeBeans(int totalPrototypeBeans) {
            this.totalPrototypeBeans = totalPrototypeBeans;
            return this;
        }

        public Builder totalPrimaryBeans(int totalPrimaryBeans) {
            this.totalPrimaryBeans = totalPrimaryBeans;
            return this;
        }

        public Builder totalConditionalBeans(int totalConditionalBeans) {
            this.totalConditionalBeans = totalConditionalBeans;
            return this;
        }

        public Builder totalProfileSpecificBeans(int totalProfileSpecificBeans) {
            this.totalProfileSpecificBeans = totalProfileSpecificBeans;
            return this;
        }

        public Builder averageDependenciesPerBean(int averageDependenciesPerBean) {
            this.averageDependenciesPerBean = averageDependenciesPerBean;
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

        public BeanDefinitionMetrics build() {
            return new BeanDefinitionMetrics(totalBeanDefinitions, totalSingletonBeans,
                totalPrototypeBeans, totalPrimaryBeans, totalConditionalBeans,
                totalProfileSpecificBeans, averageDependenciesPerBean,
                factoryBeans, componentBeans, singletonBeans);
        }
    }
}