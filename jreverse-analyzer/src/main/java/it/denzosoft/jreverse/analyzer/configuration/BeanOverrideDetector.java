package it.denzosoft.jreverse.analyzer.configuration;

import it.denzosoft.jreverse.core.logging.JReverseLogger;
import it.denzosoft.jreverse.core.model.BeanDefinitionInfo;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Detects bean overrides and conflicts in Spring configuration.
 */
public class BeanOverrideDetector {

    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(BeanOverrideDetector.class);

    /**
     * Detects bean overrides and conflicts in the provided bean definitions.
     */
    public BeanOverrideAnalysisResult detectOverrides(List<BeanDefinitionInfo> beanDefinitions) {
        if (beanDefinitions == null || beanDefinitions.isEmpty()) {
            return new BeanOverrideAnalysisResult(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        }

        List<BeanOverride> nameOverrides = detectNameOverrides(beanDefinitions);
        List<BeanOverride> typeOverrides = detectTypeOverrides(beanDefinitions);
        List<BeanConflict> conflicts = detectConflicts(beanDefinitions, nameOverrides, typeOverrides);

        LOGGER.info("Bean override detection: %d name overrides, %d type overrides, %d conflicts",
            nameOverrides.size(), typeOverrides.size(), conflicts.size());

        return new BeanOverrideAnalysisResult(nameOverrides, typeOverrides, conflicts);
    }

    /**
     * Detects beans with the same name from different configurations.
     */
    private List<BeanOverride> detectNameOverrides(List<BeanDefinitionInfo> beanDefinitions) {
        Map<String, List<BeanDefinitionInfo>> beansByName = beanDefinitions.stream()
            .filter(bean -> bean.getBeanName() != null)
            .collect(Collectors.groupingBy(BeanDefinitionInfo::getBeanName));

        List<BeanOverride> overrides = new ArrayList<>();

        for (Map.Entry<String, List<BeanDefinitionInfo>> entry : beansByName.entrySet()) {
            String beanName = entry.getKey();
            List<BeanDefinitionInfo> beans = entry.getValue();

            if (beans.size() > 1) {
                // Multiple beans with the same name - potential override
                BeanOverride override = analyzeBeanNameOverride(beanName, beans);
                overrides.add(override);

                LOGGER.debug("Detected name override for bean '%s': %d definitions", beanName, beans.size());
            }
        }

        return overrides;
    }

    /**
     * Detects beans with the same type from different configurations.
     */
    private List<BeanOverride> detectTypeOverrides(List<BeanDefinitionInfo> beanDefinitions) {
        Map<String, List<BeanDefinitionInfo>> beansByType = beanDefinitions.stream()
            .filter(bean -> bean.getBeanClass() != null)
            .collect(Collectors.groupingBy(BeanDefinitionInfo::getBeanClass));

        List<BeanOverride> overrides = new ArrayList<>();

        for (Map.Entry<String, List<BeanDefinitionInfo>> entry : beansByType.entrySet()) {
            String beanType = entry.getKey();
            List<BeanDefinitionInfo> beans = entry.getValue();

            if (beans.size() > 1) {
                // Multiple beans of the same type - check if they have different names
                Set<String> beanNames = beans.stream()
                    .map(BeanDefinitionInfo::getBeanName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

                if (beanNames.size() > 1) {
                    // Multiple beans of same type with different names
                    BeanOverride override = analyzeBeanTypeOverride(beanType, beans);
                    overrides.add(override);

                    LOGGER.debug("Detected type override for type '%s': %d definitions", beanType, beans.size());
                }
            }
        }

        return overrides;
    }

    /**
     * Detects conflicts that may cause runtime issues.
     */
    private List<BeanConflict> detectConflicts(List<BeanDefinitionInfo> beanDefinitions,
                                              List<BeanOverride> nameOverrides,
                                              List<BeanOverride> typeOverrides) {
        List<BeanConflict> conflicts = new ArrayList<>();

        // Check for primary bean conflicts
        conflicts.addAll(detectPrimaryBeanConflicts(beanDefinitions));

        // Check for qualifier conflicts
        conflicts.addAll(detectQualifierConflicts(beanDefinitions));

        // Check for scope conflicts in overrides
        conflicts.addAll(detectScopeConflicts(nameOverrides));

        return conflicts;
    }

    /**
     * Analyzes a bean name override situation.
     */
    private BeanOverride analyzeBeanNameOverride(String beanName, List<BeanDefinitionInfo> beans) {
        // Sort beans by primary status and declaration order
        List<BeanDefinitionInfo> sortedBeans = beans.stream()
            .sorted((b1, b2) -> {
                // Primary beans come first
                if (b1.isPrimary() != b2.isPrimary()) {
                    return b1.isPrimary() ? -1 : 1;
                }
                // Then by declaring class name (for consistency)
                return b1.getDeclaringClass().compareTo(b2.getDeclaringClass());
            })
            .collect(Collectors.toList());

        BeanDefinitionInfo winningBean = sortedBeans.get(0);
        List<BeanDefinitionInfo> overriddenBeans = sortedBeans.subList(1, sortedBeans.size());

        OverrideReason reason = determineOverrideReason(winningBean, overriddenBeans);

        return new BeanOverride(
            BeanOverride.OverrideType.NAME,
            beanName,
            winningBean,
            overriddenBeans,
            reason,
            String.format("Bean '%s' is overridden: %s wins", beanName, winningBean.getDeclaringClass())
        );
    }

    /**
     * Analyzes a bean type override situation.
     */
    private BeanOverride analyzeBeanTypeOverride(String beanType, List<BeanDefinitionInfo> beans) {
        List<BeanDefinitionInfo> primaryBeans = beans.stream()
            .filter(BeanDefinitionInfo::isPrimary)
            .collect(Collectors.toList());

        BeanDefinitionInfo winningBean;
        List<BeanDefinitionInfo> overriddenBeans;

        if (primaryBeans.size() == 1) {
            // Clear winner: the primary bean
            winningBean = primaryBeans.get(0);
            overriddenBeans = beans.stream()
                .filter(bean -> !bean.equals(winningBean))
                .collect(Collectors.toList());
        } else {
            // No clear winner or multiple primary beans
            winningBean = beans.get(0); // First one found
            overriddenBeans = beans.subList(1, beans.size());
        }

        OverrideReason reason = determineOverrideReason(winningBean, overriddenBeans);

        return new BeanOverride(
            BeanOverride.OverrideType.TYPE,
            beanType,
            winningBean,
            overriddenBeans,
            reason,
            String.format("Type '%s' has multiple beans: %s is primary", beanType, winningBean.getBeanName())
        );
    }

    /**
     * Determines the reason for a bean override.
     */
    private OverrideReason determineOverrideReason(BeanDefinitionInfo winningBean, List<BeanDefinitionInfo> overriddenBeans) {
        if (winningBean.isPrimary()) {
            return OverrideReason.PRIMARY_ANNOTATION;
        }

        if (overriddenBeans.stream().anyMatch(BeanDefinitionInfo::isPrimary)) {
            return OverrideReason.MULTIPLE_PRIMARY;
        }

        return OverrideReason.DECLARATION_ORDER;
    }

    /**
     * Detects conflicts with primary bean declarations.
     */
    private List<BeanConflict> detectPrimaryBeanConflicts(List<BeanDefinitionInfo> beanDefinitions) {
        Map<String, List<BeanDefinitionInfo>> beansByType = beanDefinitions.stream()
            .filter(bean -> bean.getBeanClass() != null)
            .collect(Collectors.groupingBy(BeanDefinitionInfo::getBeanClass));

        List<BeanConflict> conflicts = new ArrayList<>();

        for (Map.Entry<String, List<BeanDefinitionInfo>> entry : beansByType.entrySet()) {
            String beanType = entry.getKey();
            List<BeanDefinitionInfo> beans = entry.getValue();

            if (beans.size() > 1) {
                List<BeanDefinitionInfo> primaryBeans = beans.stream()
                    .filter(BeanDefinitionInfo::isPrimary)
                    .collect(Collectors.toList());

                if (primaryBeans.size() > 1) {
                    // Multiple primary beans of the same type - conflict!
                    conflicts.add(new BeanConflict(
                        BeanConflict.ConflictType.MULTIPLE_PRIMARY,
                        beanType,
                        primaryBeans,
                        String.format("Multiple @Primary beans of type '%s'", beanType)
                    ));
                }
            }
        }

        return conflicts;
    }

    /**
     * Detects conflicts with qualifier usage.
     */
    private List<BeanConflict> detectQualifierConflicts(List<BeanDefinitionInfo> beanDefinitions) {
        Map<String, List<BeanDefinitionInfo>> beansByQualifier = new HashMap<>();

        for (BeanDefinitionInfo bean : beanDefinitions) {
            for (String qualifier : bean.getQualifiers()) {
                beansByQualifier.computeIfAbsent(qualifier, k -> new ArrayList<>()).add(bean);
            }
        }

        List<BeanConflict> conflicts = new ArrayList<>();

        for (Map.Entry<String, List<BeanDefinitionInfo>> entry : beansByQualifier.entrySet()) {
            String qualifier = entry.getKey();
            List<BeanDefinitionInfo> beans = entry.getValue();

            if (beans.size() > 1) {
                // Multiple beans with the same qualifier - potential conflict
                conflicts.add(new BeanConflict(
                    BeanConflict.ConflictType.DUPLICATE_QUALIFIER,
                    qualifier,
                    beans,
                    String.format("Multiple beans with qualifier '%s'", qualifier)
                ));
            }
        }

        return conflicts;
    }

    /**
     * Detects scope conflicts in bean overrides.
     */
    private List<BeanConflict> detectScopeConflicts(List<BeanOverride> nameOverrides) {
        List<BeanConflict> conflicts = new ArrayList<>();

        for (BeanOverride override : nameOverrides) {
            Set<BeanDefinitionInfo.BeanScope> scopes = new HashSet<>();
            scopes.add(override.getWinningBean().getScope());
            override.getOverriddenBeans().forEach(bean -> scopes.add(bean.getScope()));

            if (scopes.size() > 1) {
                // Different scopes for the same bean name - potential issue
                List<BeanDefinitionInfo> allBeans = new ArrayList<>();
                allBeans.add(override.getWinningBean());
                allBeans.addAll(override.getOverriddenBeans());

                conflicts.add(new BeanConflict(
                    BeanConflict.ConflictType.SCOPE_MISMATCH,
                    override.getIdentifier(),
                    allBeans,
                    String.format("Bean '%s' has different scopes: %s", override.getIdentifier(), scopes)
                ));
            }
        }

        return conflicts;
    }

    /**
     * Enum for override reasons.
     */
    public enum OverrideReason {
        PRIMARY_ANNOTATION,
        MULTIPLE_PRIMARY,
        DECLARATION_ORDER,
        QUALIFIER_MATCH
    }
}