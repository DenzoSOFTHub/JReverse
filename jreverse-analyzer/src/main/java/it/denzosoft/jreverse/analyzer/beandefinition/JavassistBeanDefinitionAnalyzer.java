package it.denzosoft.jreverse.analyzer.beandefinition;

import it.denzosoft.jreverse.analyzer.beancreation.BeanCreationAnalyzer;
import it.denzosoft.jreverse.analyzer.beancreation.BeanCreationResult;
import it.denzosoft.jreverse.analyzer.beancreation.BeanInfo;
import it.denzosoft.jreverse.analyzer.beancreation.BeanCreationType;
import it.denzosoft.jreverse.analyzer.beancreation.BeanScope;
import it.denzosoft.jreverse.analyzer.beancreation.BeanDependency;
import it.denzosoft.jreverse.analyzer.beancreation.DependencyInjectionType;
import it.denzosoft.jreverse.core.logging.JReverseLogger;
import it.denzosoft.jreverse.core.model.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Javassist-based analyzer for Spring Bean definitions and @Bean methods.
 * Provides granular analysis of @Bean method lifecycle, scoping, and dependencies.
 */
public class JavassistBeanDefinitionAnalyzer {

    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(JavassistBeanDefinitionAnalyzer.class);

    private final BeanCreationAnalyzer beanCreationAnalyzer;

    // Bean definition specific annotations
    private static final Set<String> BEAN_ANNOTATIONS = Set.of(
        "org.springframework.context.annotation.Bean",
        "org.springframework.context.annotation.Primary",
        "org.springframework.context.annotation.Profile",
        "org.springframework.context.annotation.Conditional"
    );

    private static final Set<String> LIFECYCLE_ANNOTATIONS = Set.of(
        "javax.annotation.PostConstruct",
        "javax.annotation.PreDestroy",
        "org.springframework.context.annotation.DependsOn"
    );

    public JavassistBeanDefinitionAnalyzer(BeanCreationAnalyzer beanCreationAnalyzer) {
        this.beanCreationAnalyzer = beanCreationAnalyzer;
        LOGGER.info("JavassistBeanDefinitionAnalyzer initialized with BeanCreationAnalyzer");
    }

    /**
     * Analyzes JAR content for bean definitions and @Bean methods.
     */
    public BeanDefinitionResult analyze(JarContent jarContent) {
        LOGGER.info("Starting bean definition analysis for JAR: {}", jarContent.getLocation().getFileName());

        try {
            // Get all classes with @Configuration annotation
            Set<ClassInfo> configurationClasses = findConfigurationClasses(jarContent);

            // Analyze @Bean methods in configuration classes
            List<BeanMethodInfo> beanMethods = analyzeBeanMethods(configurationClasses);

            // Analyze bean lifecycle and dependencies
            Map<String, BeanLifecycleInfo> lifecycleInfo = analyzeBeanLifecycle(beanMethods);

            // Build dependency graph between beans
            Map<String, Set<String>> dependencyGraph = buildBeanDependencyGraph(beanMethods);

            LOGGER.info("Bean definition analysis completed. Found {} configuration classes with {} @Bean methods",
                       configurationClasses.size(), beanMethods.size());

            return BeanDefinitionResult.builder()
                .configurationClasses(configurationClasses)
                .beanMethods(beanMethods)
                .lifecycleInfo(lifecycleInfo)
                .dependencyGraph(dependencyGraph)
                .totalBeanDefinitions(beanMethods.size())
                .build();

        } catch (Exception e) {
            LOGGER.error("Error during bean definition analysis", e);
            return BeanDefinitionResult.error("Bean definition analysis failed: " + e.getMessage());
        }
    }

    private Set<ClassInfo> findConfigurationClasses(JarContent jarContent) {
        return jarContent.getClasses().stream()
            .filter(this::isConfigurationClass)
            .collect(Collectors.toSet());
    }

    private boolean isConfigurationClass(ClassInfo classInfo) {
        return classInfo.getAnnotations().stream()
            .anyMatch(annotation ->
                annotation.getType().equals("org.springframework.context.annotation.Configuration") ||
                annotation.getType().equals("org.springframework.boot.autoconfigure.SpringBootApplication"));
    }

    private List<BeanMethodInfo> analyzeBeanMethods(Set<ClassInfo> configurationClasses) {
        List<BeanMethodInfo> beanMethods = new ArrayList<>();

        for (ClassInfo configClass : configurationClasses) {
            for (MethodInfo method : configClass.getMethods()) {
                if (isBeanMethod(method)) {
                    BeanMethodInfo beanMethodInfo = analyzeBeanMethod(configClass, method);
                    beanMethods.add(beanMethodInfo);
                }
            }
        }

        return beanMethods;
    }

    private boolean isBeanMethod(MethodInfo method) {
        return method.getAnnotations().stream()
            .anyMatch(annotation -> annotation.getType().equals("org.springframework.context.annotation.Bean"));
    }

    private BeanMethodInfo analyzeBeanMethod(ClassInfo configClass, MethodInfo method) {
        String beanName = extractBeanName(method);
        String returnType = method.getReturnType();

        // Extract scope information
        BeanScope scope = extractBeanScope(method);

        // Extract dependency information
        List<String> dependencies = extractMethodDependencies(method);

        // Extract lifecycle annotations
        Set<String> lifecycleAnnotations = extractLifecycleAnnotations(method);

        return BeanMethodInfo.builder()
            .beanName(beanName)
            .methodName(method.getName())
            .returnType(returnType)
            .declaringClass(configClass.getFullyQualifiedName())
            .scope(scope)
            .dependencies(dependencies)
            .lifecycleAnnotations(lifecycleAnnotations)
            .isPrimary(isPrimaryBean(method))
            .profiles(extractProfiles(method))
            .build();
    }

    private String extractBeanName(MethodInfo method) {
        // Look for explicit name in @Bean annotation
        AnnotationInfo beanAnnotation = method.getAnnotations().stream()
            .filter(a -> a.getType().equals("org.springframework.context.annotation.Bean"))
            .findFirst()
            .orElse(null);

        if (beanAnnotation != null && beanAnnotation.getAttributes().containsKey("name")) {
            Object nameValue = beanAnnotation.getAttributes().get("name");
            return nameValue != null ? nameValue.toString() : method.getName();
        }

        // Default to method name
        return method.getName();
    }

    private BeanScope extractBeanScope(MethodInfo method) {
        AnnotationInfo scopeAnnotation = method.getAnnotations().stream()
            .filter(a -> a.getType().equals("org.springframework.context.annotation.Scope"))
            .findFirst()
            .orElse(null);

        if (scopeAnnotation != null && scopeAnnotation.getAttributes().containsKey("value")) {
            Object scopeValue = scopeAnnotation.getAttributes().get("value");
            return BeanScope.fromString(scopeValue != null ? scopeValue.toString() : "singleton");
        }

        return BeanScope.SINGLETON; // Default scope
    }

    private List<String> extractMethodDependencies(MethodInfo method) {
        return method.getParameters().stream()
            .map(param -> param.getType())
            .collect(Collectors.toList());
    }

    private Set<String> extractLifecycleAnnotations(MethodInfo method) {
        return method.getAnnotations().stream()
            .map(AnnotationInfo::getType)
            .filter(LIFECYCLE_ANNOTATIONS::contains)
            .collect(Collectors.toSet());
    }

    private boolean isPrimaryBean(MethodInfo method) {
        return method.getAnnotations().stream()
            .anyMatch(a -> a.getType().equals("org.springframework.context.annotation.Primary"));
    }

    private Set<String> extractProfiles(MethodInfo method) {
        AnnotationInfo profileAnnotation = method.getAnnotations().stream()
            .filter(a -> a.getType().equals("org.springframework.context.annotation.Profile"))
            .findFirst()
            .orElse(null);

        if (profileAnnotation != null && profileAnnotation.getAttributes().containsKey("value")) {
            Object profilesValue = profileAnnotation.getAttributes().get("value");
            String profiles = profilesValue != null ? profilesValue.toString() : "";
            return Arrays.stream(profiles.split(","))
                .map(String::trim)
                .collect(Collectors.toSet());
        }

        return Set.of();
    }

    private Map<String, BeanLifecycleInfo> analyzeBeanLifecycle(List<BeanMethodInfo> beanMethods) {
        Map<String, BeanLifecycleInfo> lifecycleInfo = new HashMap<>();

        for (BeanMethodInfo beanMethod : beanMethods) {
            BeanLifecycleInfo lifecycle = BeanLifecycleInfo.builder()
                .beanName(beanMethod.getBeanName())
                .hasPostConstruct(beanMethod.getLifecycleAnnotations().contains("javax.annotation.PostConstruct"))
                .hasPreDestroy(beanMethod.getLifecycleAnnotations().contains("javax.annotation.PreDestroy"))
                .dependsOn(extractDependsOnBeans(beanMethod))
                .initializationOrder(calculateInitializationOrder(beanMethod))
                .build();

            lifecycleInfo.put(beanMethod.getBeanName(), lifecycle);
        }

        return lifecycleInfo;
    }

    private Set<String> extractDependsOnBeans(BeanMethodInfo beanMethod) {
        // This would need more sophisticated analysis of @DependsOn annotations
        return Set.of();
    }

    private int calculateInitializationOrder(BeanMethodInfo beanMethod) {
        // Simple heuristic based on dependencies
        return beanMethod.getDependencies().size();
    }

    private Map<String, Set<String>> buildBeanDependencyGraph(List<BeanMethodInfo> beanMethods) {
        Map<String, Set<String>> dependencyGraph = new HashMap<>();

        for (BeanMethodInfo beanMethod : beanMethods) {
            Set<String> dependencies = new HashSet<>(beanMethod.getDependencies());
            dependencyGraph.put(beanMethod.getBeanName(), dependencies);
        }

        return dependencyGraph;
    }

    /**
     * Alias method for analyze() for backward compatibility with tests.
     */
    public BeanDefinitionAnalysisResult analyzeBeanDefinitions(JarContent jarContent) {
        BeanDefinitionResult result = analyze(jarContent);

        // Convert BeanDefinitionResult to BeanDefinitionAnalysisResult
        return BeanDefinitionAnalysisResult.builder()
            .successful(result.isSuccessful())
            .errorMessage(result.getErrorMessage())
            .beanDefinitions(convertToLegacyFormat(result.getBeanMethods()))
            .lifecycleHooks(result.getLifecycleInfo().values().stream().collect(java.util.stream.Collectors.toList()))
            .build();
    }

    private List<BeanDefinitionInfo> convertToLegacyFormat(List<BeanMethodInfo> beanMethods) {
        return beanMethods.stream()
            .map(this::convertToLegacyBeanDefinition)
            .collect(Collectors.toList());
    }

    private BeanDefinitionInfo convertToLegacyBeanDefinition(BeanMethodInfo beanMethod) {
        return BeanDefinitionInfo.builder()
            .beanName(beanMethod.getBeanName())
            .beanClass(beanMethod.getReturnType())
            .scope(BeanDefinitionInfo.BeanScope.fromString(beanMethod.getScope().getScopeName()))
            .isPrimary(beanMethod.isPrimary())
            .profiles(beanMethod.getProfiles())
            .dependencies(beanMethod.getDependencies())
            .isLazy(false)  // Default value for now
            .declaringClass(beanMethod.getDeclaringClass())
            .factoryMethod(beanMethod.getMethodName())
            .isFactoryBean(false)  // Default value
            .initMethod(null)  // Default value
            .destroyMethod(null)  // Default value
            .hasLifecycleCallbacks(!beanMethod.getLifecycleAnnotations().isEmpty())
            .build();
    }
}