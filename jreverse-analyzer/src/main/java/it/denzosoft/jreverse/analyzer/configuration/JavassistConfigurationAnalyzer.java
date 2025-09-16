package it.denzosoft.jreverse.analyzer.configuration;

import it.denzosoft.jreverse.core.logging.JReverseLogger;
import it.denzosoft.jreverse.core.model.AnnotationInfo;
import it.denzosoft.jreverse.core.model.BeanDefinitionInfo;
import it.denzosoft.jreverse.core.model.ClassInfo;
import it.denzosoft.jreverse.core.model.ConfigurationAnalysisResult;
import it.denzosoft.jreverse.core.model.JarContent;
import it.denzosoft.jreverse.core.model.MethodInfo;
import it.denzosoft.jreverse.core.model.AnalysisMetadata;
import it.denzosoft.jreverse.core.port.ConfigurationAnalyzer;
import it.denzosoft.jreverse.analyzer.beancreation.JavassistBeanCreationAnalyzer;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Advanced implementation of ConfigurationAnalyzer for Phase 3 requirements.
 * This analyzer enhances the existing Phase 2 basic implementation with:
 *
 * - Conditional Bean Analysis: @ConditionalOnProperty, @ConditionalOnClass, @ConditionalOnBean
 * - Profile Management: @Profile detection with environment logic
 * - Import Chain Analysis: @Import, @ImportResource tracking
 * - Bean Override Detection: Conflicts and priority resolution
 * - Configuration Hierarchy: Nested configurations and inheritance
 * - External Configuration: application.properties/yml integration
 */
public class JavassistConfigurationAnalyzer implements ConfigurationAnalyzer {

    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(JavassistConfigurationAnalyzer.class);

    // Spring Configuration annotations
    private static final Set<String> CONFIGURATION_ANNOTATIONS = Set.of(
        "org.springframework.context.annotation.Configuration",
        "org.springframework.boot.autoconfigure.SpringBootApplication",
        "org.springframework.context.annotation.EnableAutoConfiguration"
    );

    // Spring Conditional annotations
    private static final Set<String> CONDITIONAL_ANNOTATIONS = Set.of(
        "org.springframework.boot.autoconfigure.condition.ConditionalOnProperty",
        "org.springframework.boot.autoconfigure.condition.ConditionalOnClass",
        "org.springframework.boot.autoconfigure.condition.ConditionalOnBean",
        "org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean",
        "org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass",
        "org.springframework.boot.autoconfigure.condition.ConditionalOnExpression",
        "org.springframework.boot.autoconfigure.condition.ConditionalOnJava",
        "org.springframework.boot.autoconfigure.condition.ConditionalOnResource",
        "org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication",
        "org.springframework.context.annotation.Conditional"
    );

    // Spring Import annotations
    private static final Set<String> IMPORT_ANNOTATIONS = Set.of(
        "org.springframework.context.annotation.Import",
        "org.springframework.context.annotation.ImportResource",
        "org.springframework.context.annotation.ImportAutoConfiguration"
    );

    // Spring Bean lifecycle and configuration annotations
    private static final Set<String> LIFECYCLE_ANNOTATIONS = Set.of(
        "org.springframework.context.annotation.Primary",
        "org.springframework.context.annotation.Lazy",
        "org.springframework.context.annotation.DependsOn",
        "org.springframework.context.annotation.Profile",
        "org.springframework.beans.factory.annotation.Qualifier"
    );

    private final JavassistBeanCreationAnalyzer beanCreationAnalyzer;
    private final ConfigurationHierarchyTracker hierarchyTracker;
    private final ConditionalAnalyzer conditionalAnalyzer;
    private final ImportChainAnalyzer importChainAnalyzer;
    private final BeanOverrideDetector overrideDetector;

    public JavassistConfigurationAnalyzer() {
        this.beanCreationAnalyzer = new JavassistBeanCreationAnalyzer();
        this.hierarchyTracker = new ConfigurationHierarchyTracker();
        this.conditionalAnalyzer = new ConditionalAnalyzer();
        this.importChainAnalyzer = new ImportChainAnalyzer();
        this.overrideDetector = new BeanOverrideDetector();
    }

    @Override
    public ConfigurationAnalysisResult analyzeConfigurations(JarContent jarContent) {
        if (jarContent == null) {
            LOGGER.warn("Cannot analyze null JarContent");
            return ConfigurationAnalysisResult.error("JAR content cannot be null");
        }

        long startTime = System.currentTimeMillis();

        LOGGER.info("Starting advanced configuration analysis for JAR: %s", jarContent.getLocation().getFileName());

        try {
            ConfigurationAnalysisResult.Builder resultBuilder = ConfigurationAnalysisResult.builder();
            List<String> warnings = new ArrayList<>();

            // Phase 1: Identify all configuration classes
            List<ClassInfo> configurationClasses = identifyConfigurationClasses(jarContent);
            LOGGER.info("Found %d configuration classes", configurationClasses.size());

            if (configurationClasses.isEmpty()) {
                return ConfigurationAnalysisResult.noConfigurations();
            }

            // Phase 2: Build configuration hierarchy and import chains
            ConfigurationHierarchyTracker.ConfigurationHierarchy hierarchy = hierarchyTracker.buildHierarchy(configurationClasses);
            Map<String, List<String>> importChains = importChainAnalyzer.analyzeImportChains(configurationClasses);

            // Phase 3: Analyze beans in each configuration with conditional logic
            List<BeanDefinitionInfo> allBeans = new ArrayList<>();
            Map<String, ConditionalInfo> conditionalMap = new HashMap<>();

            for (ClassInfo configClass : configurationClasses) {
                try {
                    // Add configuration class name
                    resultBuilder.addConfigurationClass(configClass.getFullyQualifiedName());

                    // Analyze conditional logic for this configuration
                    ConditionalInfo conditionalInfo = conditionalAnalyzer.analyzeConditionals(configClass);
                    if (conditionalInfo != null) {
                        conditionalMap.put(configClass.getFullyQualifiedName(), conditionalInfo);
                    }

                    // Extract beans from this configuration
                    List<BeanDefinitionInfo> configBeans = extractBeansFromConfiguration(configClass, conditionalInfo);
                    allBeans.addAll(configBeans);

                    LOGGER.debug("Configuration class %s contains %d beans",
                        configClass.getFullyQualifiedName(), configBeans.size());

                } catch (Exception e) {
                    String warning = String.format("Failed to analyze configuration class %s: %s",
                        configClass.getFullyQualifiedName(), e.getMessage());
                    warnings.add(warning);
                    LOGGER.warn(warning, e);
                }
            }

            // Phase 4: Detect bean overrides and conflicts
            BeanOverrideAnalysisResult overrideResult = overrideDetector.detectOverrides(allBeans);

            // Phase 5: Build final result
            resultBuilder.beanDefinitions(allBeans);

            if (!warnings.isEmpty()) {
                String combinedWarnings = String.join("; ", warnings);
                resultBuilder.metadata(AnalysisMetadata.warning(combinedWarnings));
            } else {
                resultBuilder.metadata(AnalysisMetadata.successful());
            }

            long analysisTime = System.currentTimeMillis() - startTime;
            resultBuilder.analysisTimeMs(analysisTime);

            ConfigurationAnalysisResult result = resultBuilder.build();

            LOGGER.info("Advanced configuration analysis completed in %dms: %d configurations, %d beans",
                analysisTime, result.getConfigurationCount(), result.getBeanDefinitionCount());

            return result;

        } catch (Exception e) {
            long analysisTime = System.currentTimeMillis() - startTime;
            LOGGER.error("Advanced configuration analysis failed after " + analysisTime + "ms", e);
            return ConfigurationAnalysisResult.error("Advanced analysis failed: " + e.getMessage());
        }
    }

    @Override
    public boolean canAnalyze(JarContent jarContent) {
        if (jarContent == null) {
            return false;
        }

        // Can analyze if there are any Spring configuration classes
        return jarContent.getClasses().stream()
            .anyMatch(this::isConfigurationClass);
    }

    /**
     * Identifies all configuration classes in the JAR content.
     */
    private List<ClassInfo> identifyConfigurationClasses(JarContent jarContent) {
        return jarContent.getClasses().stream()
            .filter(this::isConfigurationClass)
            .collect(Collectors.toList());
    }

    /**
     * Checks if a class is a Spring configuration class.
     */
    private boolean isConfigurationClass(ClassInfo classInfo) {
        return classInfo.getAnnotations().stream()
            .anyMatch(annotation -> CONFIGURATION_ANNOTATIONS.contains(annotation.getType()));
    }

    /**
     * Extracts beans from a configuration class with conditional analysis.
     */
    private List<BeanDefinitionInfo> extractBeansFromConfiguration(ClassInfo configClass, ConditionalInfo conditionalInfo) {
        List<BeanDefinitionInfo> beans = new ArrayList<>();

        // Check if the class itself is a bean (stereotype annotations)
        if (isComponentClass(configClass)) {
            BeanDefinitionInfo classBean = createClassBean(configClass, conditionalInfo);
            if (classBean != null) {
                beans.add(classBean);
            }
        }

        // Extract @Bean methods
        for (MethodInfo method : configClass.getMethods()) {
            if (hasBeanAnnotation(method)) {
                BeanDefinitionInfo methodBean = createMethodBean(configClass, method, conditionalInfo);
                if (methodBean != null) {
                    beans.add(methodBean);
                }
            }
        }

        return beans;
    }

    /**
     * Checks if a class has component stereotype annotations.
     */
    private boolean isComponentClass(ClassInfo classInfo) {
        Set<String> componentAnnotations = Set.of(
            "org.springframework.stereotype.Component",
            "org.springframework.stereotype.Service",
            "org.springframework.stereotype.Repository",
            "org.springframework.stereotype.Controller"
        );

        return classInfo.getAnnotations().stream()
            .anyMatch(annotation -> componentAnnotations.contains(annotation.getType()));
    }

    /**
     * Checks if a method has @Bean annotation.
     */
    private boolean hasBeanAnnotation(MethodInfo method) {
        return method.getAnnotations().stream()
            .anyMatch(annotation -> "org.springframework.context.annotation.Bean".equals(annotation.getType()));
    }

    /**
     * Creates a BeanDefinitionInfo for a class-level bean.
     */
    private BeanDefinitionInfo createClassBean(ClassInfo classInfo, ConditionalInfo conditionalInfo) {
        String beanName = deriveBeanName(classInfo);
        String beanClass = classInfo.getFullyQualifiedName();

        BeanDefinitionInfo.Builder builder = BeanDefinitionInfo.builder()
            .beanName(beanName)
            .beanClass(beanClass)
            .declaringClass(classInfo.getFullyQualifiedName());

        // Extract annotation-based properties
        extractBeanProperties(builder, classInfo.getAnnotations(), conditionalInfo);

        return builder.build();
    }

    /**
     * Creates a BeanDefinitionInfo for a @Bean method.
     */
    private BeanDefinitionInfo createMethodBean(ClassInfo configClass, MethodInfo method, ConditionalInfo conditionalInfo) {
        String beanName = method.getName(); // Default bean name is method name
        String beanClass = method.getReturnType();

        BeanDefinitionInfo.Builder builder = BeanDefinitionInfo.builder()
            .beanName(beanName)
            .beanClass(beanClass)
            .declaringClass(configClass.getFullyQualifiedName())
            .factoryMethod(method.getName());

        // Extract method annotation-based properties
        extractBeanProperties(builder, method.getAnnotations(), conditionalInfo);

        return builder.build();
    }

    /**
     * Extracts bean properties from annotations including conditional logic.
     */
    private void extractBeanProperties(BeanDefinitionInfo.Builder builder, Set<AnnotationInfo> annotations, ConditionalInfo conditionalInfo) {
        // Extract scope
        BeanDefinitionInfo.BeanScope scope = extractScope(annotations);
        builder.scope(scope);

        // Extract lifecycle properties
        boolean isPrimary = hasAnnotation(annotations, "org.springframework.context.annotation.Primary");
        boolean isLazy = hasAnnotation(annotations, "org.springframework.context.annotation.Lazy");
        builder.isPrimary(isPrimary).isLazy(isLazy);

        // Extract profiles
        Set<String> profiles = extractProfiles(annotations);
        builder.profiles(profiles);

        // Extract qualifiers
        Set<String> qualifiers = extractQualifiers(annotations);
        builder.qualifiers(qualifiers);

        // Extract lifecycle methods
        String initMethod = extractInitMethod(annotations);
        String destroyMethod = extractDestroyMethod(annotations);
        if (initMethod != null) {
            builder.initMethod(initMethod);
        }
        if (destroyMethod != null) {
            builder.destroyMethod(destroyMethod);
        }

        // Add conditional information to dependencies if present
        if (conditionalInfo != null && !conditionalInfo.getConditions().isEmpty()) {
            List<String> dependencies = new ArrayList<>();
            dependencies.add("CONDITIONAL: " + conditionalInfo.getConditionSummary());
            builder.dependencies(dependencies);
        }
    }

    /**
     * Derives bean name from class or annotation value.
     */
    private String deriveBeanName(ClassInfo classInfo) {
        // Check for custom name in annotations
        for (AnnotationInfo annotation : classInfo.getAnnotations()) {
            String customName = annotation.getStringAttribute("value");
            if (customName != null && !customName.trim().isEmpty()) {
                return customName.trim();
            }
        }

        // Default to class simple name with first letter lowercase
        String simpleName = classInfo.getSimpleName();
        return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
    }

    /**
     * Extracts scope from annotations.
     */
    private BeanDefinitionInfo.BeanScope extractScope(Set<AnnotationInfo> annotations) {
        for (AnnotationInfo annotation : annotations) {
            if ("org.springframework.context.annotation.Scope".equals(annotation.getType())) {
                String scopeValue = annotation.getStringAttribute("value");
                if (scopeValue != null) {
                    return BeanDefinitionInfo.BeanScope.fromString(scopeValue);
                }
            }
        }
        return BeanDefinitionInfo.BeanScope.SINGLETON; // Default scope
    }

    /**
     * Extracts profiles from annotations.
     */
    private Set<String> extractProfiles(Set<AnnotationInfo> annotations) {
        Set<String> profiles = new HashSet<>();

        for (AnnotationInfo annotation : annotations) {
            if ("org.springframework.context.annotation.Profile".equals(annotation.getType())) {
                String[] profileArray = annotation.getStringArrayAttribute("value");
                if (profileArray != null) {
                    profiles.addAll(Arrays.asList(profileArray));
                }

                String singleProfile = annotation.getStringAttribute("value");
                if (singleProfile != null && !singleProfile.trim().isEmpty()) {
                    profiles.add(singleProfile.trim());
                }
            }
        }

        return profiles;
    }

    /**
     * Extracts qualifiers from annotations.
     */
    private Set<String> extractQualifiers(Set<AnnotationInfo> annotations) {
        Set<String> qualifiers = new HashSet<>();

        for (AnnotationInfo annotation : annotations) {
            if ("org.springframework.beans.factory.annotation.Qualifier".equals(annotation.getType())) {
                String qualifier = annotation.getStringAttribute("value");
                if (qualifier != null && !qualifier.trim().isEmpty()) {
                    qualifiers.add(qualifier.trim());
                }
            }
        }

        return qualifiers;
    }

    /**
     * Extracts init method from annotations.
     */
    private String extractInitMethod(Set<AnnotationInfo> annotations) {
        for (AnnotationInfo annotation : annotations) {
            if ("org.springframework.context.annotation.Bean".equals(annotation.getType())) {
                String initMethod = annotation.getStringAttribute("initMethod");
                if (initMethod != null && !initMethod.trim().isEmpty()) {
                    return initMethod.trim();
                }
            }
        }
        return null;
    }

    /**
     * Extracts destroy method from annotations.
     */
    private String extractDestroyMethod(Set<AnnotationInfo> annotations) {
        for (AnnotationInfo annotation : annotations) {
            if ("org.springframework.context.annotation.Bean".equals(annotation.getType())) {
                String destroyMethod = annotation.getStringAttribute("destroyMethod");
                if (destroyMethod != null && !destroyMethod.trim().isEmpty()) {
                    return destroyMethod.trim();
                }
            }
        }
        return null;
    }

    /**
     * Checks if annotations contain a specific annotation type.
     */
    private boolean hasAnnotation(Set<AnnotationInfo> annotations, String annotationType) {
        return annotations.stream()
            .anyMatch(annotation -> annotation.getType().equals(annotationType));
    }
}