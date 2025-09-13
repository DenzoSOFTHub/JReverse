package it.denzosoft.jreverse.analyzer.beancreation;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Statistical information about discovered Spring beans and their characteristics.
 */
public class BeanCreationStatistics {
    
    private final int totalBeans;
    private final Map<BeanCreationType, Long> beansByType;
    private final Map<BeanScope, Long> beansByScope;
    private final Map<DependencyInjectionType, Long> injectionTypes;
    private final int lazyBeans;
    private final int primaryBeans;
    private final int conditionalBeans;
    private final int beansWithDependencies;
    private final int beansWithCircularDependencies;
    private final double averageDependenciesPerBean;
    
    private BeanCreationStatistics(int totalBeans,
                                  Map<BeanCreationType, Long> beansByType,
                                  Map<BeanScope, Long> beansByScope,
                                  Map<DependencyInjectionType, Long> injectionTypes,
                                  int lazyBeans,
                                  int primaryBeans,
                                  int conditionalBeans,
                                  int beansWithDependencies,
                                  int beansWithCircularDependencies,
                                  double averageDependenciesPerBean) {
        this.totalBeans = totalBeans;
        this.beansByType = beansByType;
        this.beansByScope = beansByScope;
        this.injectionTypes = injectionTypes;
        this.lazyBeans = lazyBeans;
        this.primaryBeans = primaryBeans;
        this.conditionalBeans = conditionalBeans;
        this.beansWithDependencies = beansWithDependencies;
        this.beansWithCircularDependencies = beansWithCircularDependencies;
        this.averageDependenciesPerBean = averageDependenciesPerBean;
    }
    
    /**
     * Creates statistics from a list of beans.
     */
    public static BeanCreationStatistics from(List<BeanInfo> beans) {
        if (beans.isEmpty()) {
            return empty();
        }
        
        // Group beans by creation type
        Map<BeanCreationType, Long> beansByType = beans.stream()
            .collect(Collectors.groupingBy(BeanInfo::getCreationType, Collectors.counting()));
        
        // Group beans by scope
        Map<BeanScope, Long> beansByScope = beans.stream()
            .collect(Collectors.groupingBy(BeanInfo::getScope, Collectors.counting()));
        
        // Group dependencies by injection type
        Map<DependencyInjectionType, Long> injectionTypes = beans.stream()
            .flatMap(bean -> bean.getDependencies().stream())
            .collect(Collectors.groupingBy(BeanDependency::getInjectionType, Collectors.counting()));
        
        // Calculate various metrics
        int lazyBeans = (int) beans.stream().filter(BeanInfo::isLazy).count();
        int primaryBeans = (int) beans.stream().filter(BeanInfo::isPrimary).count();
        int conditionalBeans = (int) beans.stream().filter(BeanInfo::isConditional).count();
        int beansWithDependencies = (int) beans.stream().filter(bean -> !bean.getDependencies().isEmpty()).count();
        int beansWithCircularDependencies = (int) beans.stream().filter(BeanInfo::hasCircularDependencies).count();
        
        double averageDependenciesPerBean = beans.stream()
            .mapToInt(BeanInfo::getDependencyCount)
            .average()
            .orElse(0.0);
        
        return new BeanCreationStatistics(
            beans.size(),
            beansByType,
            beansByScope,
            injectionTypes,
            lazyBeans,
            primaryBeans,
            conditionalBeans,
            beansWithDependencies,
            beansWithCircularDependencies,
            averageDependenciesPerBean
        );
    }
    
    /**
     * Creates empty statistics.
     */
    public static BeanCreationStatistics empty() {
        return new BeanCreationStatistics(
            0,
            Map.of(),
            Map.of(),
            Map.of(),
            0, 0, 0, 0, 0, 0.0
        );
    }
    
    // Getters
    public int getTotalBeans() { return totalBeans; }
    public Map<BeanCreationType, Long> getBeansByType() { return beansByType; }
    public Map<BeanScope, Long> getBeansByScope() { return beansByScope; }
    public Map<DependencyInjectionType, Long> getInjectionTypes() { return injectionTypes; }
    public int getLazyBeans() { return lazyBeans; }
    public int getPrimaryBeans() { return primaryBeans; }
    public int getConditionalBeans() { return conditionalBeans; }
    public int getBeansWithDependencies() { return beansWithDependencies; }
    public int getBeansWithCircularDependencies() { return beansWithCircularDependencies; }
    public double getAverageDependenciesPerBean() { return averageDependenciesPerBean; }
    
    /**
     * Gets the count of beans for a specific creation type.
     */
    public long getBeanCount(BeanCreationType type) {
        return beansByType.getOrDefault(type, 0L);
    }
    
    /**
     * Gets the count of beans for a specific scope.
     */
    public long getBeanCount(BeanScope scope) {
        return beansByScope.getOrDefault(scope, 0L);
    }
    
    /**
     * Gets the count of dependencies using a specific injection type.
     */
    public long getInjectionCount(DependencyInjectionType type) {
        return injectionTypes.getOrDefault(type, 0L);
    }
    
    /**
     * Gets the percentage of beans that are lazy.
     */
    public double getLazyBeanPercentage() {
        return totalBeans > 0 ? (double) lazyBeans / totalBeans * 100.0 : 0.0;
    }
    
    /**
     * Gets the percentage of beans that are primary.
     */
    public double getPrimaryBeanPercentage() {
        return totalBeans > 0 ? (double) primaryBeans / totalBeans * 100.0 : 0.0;
    }
    
    /**
     * Gets the percentage of beans that are conditional.
     */
    public double getConditionalBeanPercentage() {
        return totalBeans > 0 ? (double) conditionalBeans / totalBeans * 100.0 : 0.0;
    }
    
    /**
     * Gets the percentage of beans with circular dependencies.
     */
    public double getCircularDependencyPercentage() {
        return totalBeans > 0 ? (double) beansWithCircularDependencies / totalBeans * 100.0 : 0.0;
    }
    
    /**
     * Gets the most common bean creation type.
     */
    public BeanCreationType getMostCommonCreationType() {
        return beansByType.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }
    
    /**
     * Gets the most common bean scope.
     */
    public BeanScope getMostCommonScope() {
        return beansByScope.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }
    
    /**
     * Gets the most common injection type.
     */
    public DependencyInjectionType getMostCommonInjectionType() {
        return injectionTypes.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }
    
    @Override
    public String toString() {
        return String.format("BeanCreationStatistics{total=%d, lazy=%.1f%%, primary=%.1f%%, conditional=%.1f%%, avgDeps=%.1f}",
            totalBeans,
            getLazyBeanPercentage(),
            getPrimaryBeanPercentage(),
            getConditionalBeanPercentage(),
            averageDependenciesPerBean);
    }
}