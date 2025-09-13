package it.denzosoft.jreverse.analyzer.beancreation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Result of bean creation analysis containing comprehensive information about
 * discovered Spring beans, their creation patterns, dependencies, and relationships.
 */
public class BeanCreationResult {
    
    private final List<BeanInfo> allBeans;
    private final BeanAnalysisMetadata metadata;
    private final BeanCreationStatistics statistics;
    
    private BeanCreationResult(List<BeanInfo> allBeans, 
                              BeanAnalysisMetadata metadata,
                              BeanCreationStatistics statistics) {
        this.allBeans = Collections.unmodifiableList(new ArrayList<>(allBeans));
        this.metadata = metadata;
        this.statistics = statistics;
    }
    
    /**
     * Creates a successful analysis result.
     */
    public static BeanCreationResult success(List<BeanInfo> beans) {
        BeanCreationStatistics stats = BeanCreationStatistics.from(beans);
        return new BeanCreationResult(
            beans,
            BeanAnalysisMetadata.successful(beans.size()),
            stats
        );
    }
    
    /**
     * Creates a result with warnings.
     */
    public static BeanCreationResult withWarnings(List<BeanInfo> beans, List<String> warnings) {
        BeanCreationStatistics stats = BeanCreationStatistics.from(beans);
        return new BeanCreationResult(
            beans,
            BeanAnalysisMetadata.withWarnings(beans.size(), warnings),
            stats
        );
    }
    
    /**
     * Creates a result for analysis failure.
     */
    public static BeanCreationResult error(String errorMessage) {
        return new BeanCreationResult(
            Collections.emptyList(),
            BeanAnalysisMetadata.error(errorMessage),
            BeanCreationStatistics.empty()
        );
    }
    
    /**
     * Creates a result when no beans are found.
     */
    public static BeanCreationResult noBeans() {
        return new BeanCreationResult(
            Collections.emptyList(),
            BeanAnalysisMetadata.warning("No Spring beans found in the analyzed JAR"),
            BeanCreationStatistics.empty()
        );
    }
    
    // Getters
    public List<BeanInfo> getAllBeans() { return allBeans; }
    public BeanAnalysisMetadata getMetadata() { return metadata; }
    public BeanCreationStatistics getStatistics() { return statistics; }
    
    /**
     * Gets beans filtered by creation type.
     */
    public List<BeanInfo> getBeansByType(BeanCreationType creationType) {
        return allBeans.stream()
            .filter(bean -> bean.getCreationType() == creationType)
            .collect(Collectors.toList());
    }
    
    /**
     * Gets beans filtered by scope.
     */
    public List<BeanInfo> getBeansByScope(BeanScope scope) {
        return allBeans.stream()
            .filter(bean -> bean.getScope() == scope)
            .collect(Collectors.toList());
    }
    
    /**
     * Gets all @Service beans.
     */
    public List<BeanInfo> getServiceBeans() {
        return getBeansByType(BeanCreationType.SERVICE);
    }
    
    /**
     * Gets all @Repository beans.
     */
    public List<BeanInfo> getRepositoryBeans() {
        return getBeansByType(BeanCreationType.REPOSITORY);
    }
    
    /**
     * Gets all web controller beans (@Controller and @RestController).
     */
    public List<BeanInfo> getControllerBeans() {
        return allBeans.stream()
            .filter(bean -> bean.getCreationType().isWebComponent())
            .collect(Collectors.toList());
    }
    
    /**
     * Gets all configuration beans.
     */
    public List<BeanInfo> getConfigurationBeans() {
        return getBeansByType(BeanCreationType.CONFIGURATION);
    }
    
    /**
     * Gets all @Bean method beans.
     */
    public List<BeanInfo> getBeanMethodBeans() {
        return getBeansByType(BeanCreationType.BEAN_METHOD);
    }
    
    /**
     * Gets beans with circular dependencies.
     */
    public List<BeanInfo> getBeansWithCircularDependencies() {
        return allBeans.stream()
            .filter(BeanInfo::hasCircularDependencies)
            .collect(Collectors.toList());
    }
    
    /**
     * Gets all lazy beans.
     */
    public List<BeanInfo> getLazyBeans() {
        return allBeans.stream()
            .filter(BeanInfo::isLazy)
            .collect(Collectors.toList());
    }
    
    /**
     * Gets all primary beans.
     */
    public List<BeanInfo> getPrimaryBeans() {
        return allBeans.stream()
            .filter(BeanInfo::isPrimary)
            .collect(Collectors.toList());
    }
    
    /**
     * Gets all conditional beans (with @Profile or @Conditional).
     */
    public List<BeanInfo> getConditionalBeans() {
        return allBeans.stream()
            .filter(BeanInfo::isConditional)
            .collect(Collectors.toList());
    }
    
    /**
     * Gets beans grouped by their declaring class.
     */
    public Map<String, List<BeanInfo>> getBeansByDeclaringClass() {
        return allBeans.stream()
            .collect(Collectors.groupingBy(BeanInfo::getDeclaringClassName));
    }
    
    /**
     * Gets all unique profiles used by beans.
     */
    public Set<String> getAllProfiles() {
        return allBeans.stream()
            .flatMap(bean -> bean.getProfiles().stream())
            .collect(Collectors.toSet());
    }
    
    /**
     * Gets beans that belong to a specific profile.
     */
    public List<BeanInfo> getBeansByProfile(String profile) {
        return allBeans.stream()
            .filter(bean -> bean.getProfiles().contains(profile))
            .collect(Collectors.toList());
    }
    
    /**
     * Checks if analysis was successful.
     */
    public boolean isSuccessful() {
        return metadata.isSuccessful();
    }
    
    /**
     * Checks if analysis has warnings.
     */
    public boolean hasWarnings() {
        return metadata.hasWarnings();
    }
    
    /**
     * Checks if analysis has errors.
     */
    public boolean hasErrors() {
        return metadata.hasErrors();
    }
    
    /**
     * Gets the total number of discovered beans.
     */
    public int getTotalBeanCount() {
        return allBeans.size();
    }
    
    /**
     * Checks if any beans were found.
     */
    public boolean hasBeans() {
        return !allBeans.isEmpty();
    }
    
    @Override
    public String toString() {
        return String.format("BeanCreationResult{beans=%d, successful=%s, warnings=%d, errors=%d}",
            allBeans.size(),
            metadata.isSuccessful(),
            metadata.getWarnings().size(),
            metadata.hasErrors() ? 1 : 0);
    }
}