package it.denzosoft.jreverse.analyzer.circulardependency;

import it.denzosoft.jreverse.analyzer.beancreation.BeanDependency;
import it.denzosoft.jreverse.analyzer.beancreation.BeanDependencyAnalyzer;
import it.denzosoft.jreverse.analyzer.beancreation.DependencyInjectionType;
import it.denzosoft.jreverse.core.logging.JReverseLogger;
import it.denzosoft.jreverse.core.model.AnnotationInfo;
import it.denzosoft.jreverse.core.model.ClassInfo;
import it.denzosoft.jreverse.core.model.MethodInfo;
import it.denzosoft.jreverse.core.model.FieldInfo;
import it.denzosoft.jreverse.core.model.ParameterInfo;
import it.denzosoft.jreverse.core.model.CircularDependency;
import it.denzosoft.jreverse.core.model.DependencyNode;
import it.denzosoft.jreverse.core.model.DependencyNodeType;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Specialized circular dependency detector for Spring IoC dependency injection.
 * Focuses specifically on Spring-specific patterns like @Autowired, @Lazy,
 * and different injection types (constructor, field, setter) rather than generic cycles.
 *
 * Key Features:
 * - Spring component analysis (@Service, @Repository, @Controller, @Component)
 * - Injection type analysis (Constructor, Field, Method injection)
 * - @Lazy annotation impact assessment
 * - Spring-specific resolution strategies
 * - Severity assessment based on injection types
 *
 * @author JReverse Development Team
 * @since 1.1.0 (Phase 3 - T3.3.1)
 */
public class SpringIoCCircularDependencyDetector {

    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(SpringIoCCircularDependencyDetector.class);

    // Spring stereotype annotations
    private static final Set<String> SPRING_COMPONENT_ANNOTATIONS = Set.of(
        "org.springframework.stereotype.Component",
        "org.springframework.stereotype.Service",
        "org.springframework.stereotype.Repository",
        "org.springframework.stereotype.Controller",
        "org.springframework.web.bind.annotation.RestController",
        "org.springframework.context.annotation.Configuration"
    );

    // Spring dependency injection annotations
    private static final Set<String> AUTOWIRED_ANNOTATIONS = Set.of(
        "org.springframework.beans.factory.annotation.Autowired",
        "javax.inject.Inject"
    );

    private static final String LAZY_ANNOTATION = "org.springframework.context.annotation.Lazy";
    private static final String QUALIFIER_ANNOTATION = "org.springframework.beans.factory.annotation.Qualifier";
    private static final String PRIMARY_ANNOTATION = "org.springframework.context.annotation.Primary";

    // Analysis components
    private final ClassPool classPool;
    private final BeanDependencyAnalyzer beanDependencyAnalyzer;

    // Analysis state
    private final Map<String, SpringComponentInfo> springComponents = new HashMap<>();
    private final Map<String, Set<String>> dependencyGraph = new HashMap<>();
    private final List<SpringCircularDependency> detectedCycles = new ArrayList<>();

    // Performance limits
    private static final int MAX_CYCLE_LENGTH = 20;
    private static final int MAX_CYCLES_TO_DETECT = 50;
    private static final int MAX_DFS_DEPTH = 25;

    public SpringIoCCircularDependencyDetector(ClassPool classPool) {
        this.classPool = Objects.requireNonNull(classPool, "ClassPool cannot be null");
        this.beanDependencyAnalyzer = new BeanDependencyAnalyzer();
    }

    /**
     * Analyzes Spring IoC circular dependencies in the given classes.
     *
     * @param classes classes to analyze
     * @return result containing detected circular dependencies with Spring-specific analysis
     */
    public SpringCircularDependencyResult analyzeSpringCircularDependencies(Set<ClassInfo> classes) {
        LOGGER.startOperation("Spring IoC circular dependency analysis");
        long startTime = System.currentTimeMillis();

        try {
            // Clear previous analysis state
            clearAnalysisState();

            // Phase 1: Identify Spring components
            identifySpringComponents(classes);
            LOGGER.info("Identified {} Spring components", springComponents.size());

            // Phase 2: Build Spring dependency graph
            buildSpringDependencyGraph();
            LOGGER.info("Built dependency graph with {} nodes", dependencyGraph.size());

            // Phase 3: Detect circular dependencies using DFS
            detectCircularDependenciesWithDFS();
            LOGGER.info("Detected {} potential circular dependencies", detectedCycles.size());

            // Phase 4: Analyze each cycle for Spring-specific characteristics
            List<SpringCircularDependency> analyzedCycles = analyzeDetectedCycles();

            // Phase 5: Generate Spring-specific resolution strategies
            generateResolutionStrategies(analyzedCycles);

            // Phase 6: Calculate impact metrics
            SpringCircularDependencyMetrics metrics = calculateImpactMetrics(analyzedCycles);

            long analysisTime = System.currentTimeMillis() - startTime;
            LOGGER.info("Spring circular dependency analysis completed in {}ms", analysisTime);

            return SpringCircularDependencyResult.builder()
                .circularDependencies(analyzedCycles)
                .metrics(metrics)
                .totalComponents(springComponents.size())
                .analyzedComponents(springComponents.values().stream()
                    .mapToInt(component -> component.getDependencies().size())
                    .sum())
                .analysisTimeMs(analysisTime)
                .build();

        } catch (Exception e) {
            LOGGER.error("Spring circular dependency analysis failed", e);
            return SpringCircularDependencyResult.builder()
                .circularDependencies(Collections.emptyList())
                .metrics(SpringCircularDependencyMetrics.empty())
                .totalComponents(0)
                .analyzedComponents(0)
                .analysisTimeMs(System.currentTimeMillis() - startTime)
                .error("Analysis failed: " + e.getMessage())
                .build();
        } finally {
            LOGGER.endOperation("Spring IoC circular dependency analysis",
                System.currentTimeMillis() - startTime);
        }
    }

    /**
     * Clears analysis state for fresh analysis.
     */
    private void clearAnalysisState() {
        springComponents.clear();
        dependencyGraph.clear();
        detectedCycles.clear();
    }

    /**
     * Phase 1: Identifies Spring components and analyzes their injection characteristics.
     */
    private void identifySpringComponents(Set<ClassInfo> classes) {
        for (ClassInfo classInfo : classes) {
            if (isSpringComponent(classInfo)) {
                try {
                    SpringComponentInfo componentInfo = analyzeSpringComponent(classInfo);
                    springComponents.put(classInfo.getFullyQualifiedName(), componentInfo);

                    LOGGER.debug("Analyzed Spring component: {} with {} dependencies",
                        classInfo.getSimpleName(), componentInfo.getDependencies().size());

                } catch (Exception e) {
                    LOGGER.warn("Failed to analyze Spring component {}: {}",
                        classInfo.getFullyQualifiedName(), e.getMessage());
                }
            }
        }
    }

    /**
     * Checks if a class is a Spring component.
     */
    private boolean isSpringComponent(ClassInfo classInfo) {
        return classInfo.getAnnotations().stream()
            .anyMatch(annotation -> SPRING_COMPONENT_ANNOTATIONS.contains(annotation.getType()));
    }

    /**
     * Analyzes a Spring component to extract dependency injection information.
     */
    private SpringComponentInfo analyzeSpringComponent(ClassInfo classInfo) {
        SpringComponentInfo componentInfo = new SpringComponentInfo();
        componentInfo.setClassName(classInfo.getFullyQualifiedName());
        componentInfo.setComponentType(determineComponentType(classInfo));

        try {
            // Try to load CtClass for detailed Javassist analysis
            CtClass ctClass = classPool.get(classInfo.getFullyQualifiedName());

            // Analyze dependencies using existing BeanDependencyAnalyzer
            List<BeanDependency> dependencies = beanDependencyAnalyzer.analyzeDependencies(classInfo);
            componentInfo.setDependencies(dependencies);

            // Analyze Spring-specific characteristics
            analyzeSpringSpecificFeatures(ctClass, componentInfo);

        } catch (NotFoundException e) {
            LOGGER.debug("CtClass not found, using fallback ClassInfo analysis: {}", classInfo.getFullyQualifiedName());
            // Fallback: Extract dependencies directly from ClassInfo
            List<BeanDependency> fallbackDependencies = extractDependenciesFromClassInfo(classInfo);
            componentInfo.setDependencies(fallbackDependencies);
            // Analyze Spring features from ClassInfo
            analyzeSpringFeaturesFromClassInfo(classInfo, componentInfo);
        } catch (Exception e) {
            LOGGER.warn("Error analyzing Spring component {}: {}", classInfo.getFullyQualifiedName(), e.getMessage());
            // Use fallback analysis
            List<BeanDependency> fallbackDependencies = extractDependenciesFromClassInfo(classInfo);
            componentInfo.setDependencies(fallbackDependencies);
        }

        return componentInfo;
    }

    /**
     * Fallback method to extract dependencies directly from ClassInfo when CtClass is not available.
     */
    private List<BeanDependency> extractDependenciesFromClassInfo(ClassInfo classInfo) {
        List<BeanDependency> dependencies = new ArrayList<>();

        // Extract from constructor parameters
        for (MethodInfo method : classInfo.getMethods()) {
            if ("<init>".equals(method.getName()) && !method.getParameters().isEmpty()) {
                for (ParameterInfo param : method.getParameters()) {
                    String paramType = param.getType();
                    if (isSpringComponentType(paramType)) {
                        dependencies.add(BeanDependency.builder()
                            .type(paramType)
                            .injectionType(DependencyInjectionType.CONSTRUCTOR)
                            .injectionPoint("constructor parameter")
                            .build());
                    }
                }
            }
        }

        // Extract from fields with @Autowired
        for (FieldInfo field : classInfo.getFields()) {
            if (hasAutowiredAnnotation(field) && isSpringComponentType(field.getType())) {
                dependencies.add(BeanDependency.builder()
                    .type(field.getType())
                    .injectionType(DependencyInjectionType.FIELD)
                    .injectionPoint("field " + field.getName())
                    .build());
            }
        }

        // Extract from setter methods with @Autowired
        for (MethodInfo method : classInfo.getMethods()) {
            if (hasAutowiredAnnotation(method) && method.getName().startsWith("set") &&
                method.getParameters().size() == 1) {
                String paramType = method.getParameters().get(0).getType();
                if (isSpringComponentType(paramType)) {
                    dependencies.add(BeanDependency.builder()
                        .type(paramType)
                        .injectionType(DependencyInjectionType.SETTER)
                        .injectionPoint("setter " + method.getName())
                        .build());
                }
            }
        }

        return dependencies;
    }

    private boolean isSpringComponentType(String typeName) {
        // Check if it's a potential Spring component based on package or name
        return typeName != null &&
               !typeName.startsWith("java.") &&
               !typeName.startsWith("javax.") &&
               (typeName.contains("Service") || typeName.contains("Repository") ||
                typeName.contains("Controller") || typeName.contains("Component"));
    }

    private boolean hasAutowiredAnnotation(FieldInfo field) {
        return field.getAnnotations().stream()
            .anyMatch(ann -> AUTOWIRED_ANNOTATIONS.contains(ann.getType()));
    }

    private boolean hasAutowiredAnnotation(MethodInfo method) {
        return method.getAnnotations().stream()
            .anyMatch(ann -> AUTOWIRED_ANNOTATIONS.contains(ann.getType()));
    }

    private void analyzeSpringFeaturesFromClassInfo(ClassInfo classInfo, SpringComponentInfo componentInfo) {
        // Check for @Lazy annotation on class
        boolean hasLazy = classInfo.getAnnotations().stream()
            .anyMatch(ann -> LAZY_ANNOTATION.equals(ann.getType()));
        componentInfo.setLazyInitialized(hasLazy);

        // Check for constructor injection
        boolean hasConstructorInjection = classInfo.getMethods().stream()
            .anyMatch(method -> "<init>".equals(method.getName()) && !method.getParameters().isEmpty());
        componentInfo.setHasConstructorInjection(hasConstructorInjection);

        // Check for field injection
        boolean hasFieldInjection = classInfo.getFields().stream()
            .anyMatch(field -> hasAutowiredAnnotation(field));
        componentInfo.setHasFieldInjection(hasFieldInjection);

        // Check for method injection
        boolean hasMethodInjection = classInfo.getMethods().stream()
            .anyMatch(method -> hasAutowiredAnnotation(method) && method.getName().startsWith("set"));
        componentInfo.setHasMethodInjection(hasMethodInjection);
    }

    /**
     * Determines the Spring component type from annotations.
     */
    private SpringComponentType determineComponentType(ClassInfo classInfo) {
        for (AnnotationInfo annotation : classInfo.getAnnotations()) {
            switch (annotation.getType()) {
                case "org.springframework.stereotype.Service":
                    return SpringComponentType.SERVICE;
                case "org.springframework.stereotype.Repository":
                    return SpringComponentType.REPOSITORY;
                case "org.springframework.stereotype.Controller":
                    return SpringComponentType.CONTROLLER;
                case "org.springframework.web.bind.annotation.RestController":
                    return SpringComponentType.REST_CONTROLLER;
                case "org.springframework.context.annotation.Configuration":
                    return SpringComponentType.CONFIGURATION;
                case "org.springframework.stereotype.Component":
                    return SpringComponentType.COMPONENT;
            }
        }
        return SpringComponentType.COMPONENT; // Default fallback
    }

    /**
     * Analyzes Spring-specific features like @Lazy, @Primary, qualifiers.
     */
    private void analyzeSpringSpecificFeatures(CtClass ctClass, SpringComponentInfo componentInfo) {
        try {
            // Check for @Lazy on class level
            if (ctClass.hasAnnotation(LAZY_ANNOTATION)) {
                componentInfo.setLazyInitialized(true);
            }

            // Check for @Primary
            if (ctClass.hasAnnotation(PRIMARY_ANNOTATION)) {
                componentInfo.setPrimary(true);
            }

            // Analyze constructor injection patterns
            analyzeConstructorInjectionPatterns(ctClass, componentInfo);

            // Analyze field injection patterns
            analyzeFieldInjectionPatterns(ctClass, componentInfo);

            // Analyze method injection patterns
            analyzeMethodInjectionPatterns(ctClass, componentInfo);

        } catch (Exception e) {
            LOGGER.warn("Error analyzing Spring-specific features for {}: {}",
                ctClass.getName(), e.getMessage());
            componentInfo.addError("Spring features analysis error: " + e.getMessage());
        }
    }

    /**
     * Analyzes constructor injection patterns and @Lazy usage.
     */
    private void analyzeConstructorInjectionPatterns(CtClass ctClass, SpringComponentInfo componentInfo) {
        try {
            CtConstructor[] constructors = ctClass.getConstructors();

            for (CtConstructor constructor : constructors) {
                if (constructor.hasAnnotation("org.springframework.beans.factory.annotation.Autowired") ||
                    (constructors.length == 1 && constructor.getParameterTypes().length > 0)) {

                    componentInfo.setHasConstructorInjection(true);

                    // Check for @Lazy on constructor parameters
                    Object[][] paramAnnotations = constructor.getParameterAnnotations();
                    for (Object[] annotations : paramAnnotations) {
                        for (Object annotation : annotations) {
                            if (annotation.toString().contains("Lazy")) {
                                componentInfo.setHasLazyDependencies(true);
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Error analyzing constructor injection for {}: {}", ctClass.getName(), e.getMessage());
        }
    }

    /**
     * Analyzes field injection patterns and @Lazy usage.
     */
    private void analyzeFieldInjectionPatterns(CtClass ctClass, SpringComponentInfo componentInfo) {
        try {
            CtField[] fields = ctClass.getDeclaredFields();

            for (CtField field : fields) {
                if (field.hasAnnotation("org.springframework.beans.factory.annotation.Autowired")) {
                    componentInfo.setHasFieldInjection(true);

                    if (field.hasAnnotation(LAZY_ANNOTATION)) {
                        componentInfo.setHasLazyDependencies(true);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Error analyzing field injection for {}: {}", ctClass.getName(), e.getMessage());
        }
    }

    /**
     * Analyzes method injection patterns and @Lazy usage.
     */
    private void analyzeMethodInjectionPatterns(CtClass ctClass, SpringComponentInfo componentInfo) {
        try {
            CtMethod[] methods = ctClass.getDeclaredMethods();

            for (CtMethod method : methods) {
                if (method.hasAnnotation("org.springframework.beans.factory.annotation.Autowired")) {
                    componentInfo.setHasMethodInjection(true);

                    if (method.hasAnnotation(LAZY_ANNOTATION)) {
                        componentInfo.setHasLazyDependencies(true);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Error analyzing method injection for {}: {}", ctClass.getName(), e.getMessage());
        }
    }

    /**
     * Phase 2: Builds Spring dependency graph based on injection relationships.
     */
    private void buildSpringDependencyGraph() {
        for (SpringComponentInfo component : springComponents.values()) {
            Set<String> dependencies = new HashSet<>();

            // Get dependencies from BeanDependencyAnalyzer
            for (BeanDependency dependency : component.getDependencies()) {
                String targetClass = dependency.getType();

                // Only include dependencies on other Spring components
                if (springComponents.containsKey(targetClass)) {
                    dependencies.add(targetClass);
                }
            }

            // Also extract dependencies directly from component analysis
            // This is a fallback for test scenarios where BeanDependencyAnalyzer might not work perfectly
            extractDirectDependenciesFromComponent(component, dependencies);

            dependencyGraph.put(component.getClassName(), dependencies);
        }
    }

    /**
     * Extracts dependencies directly from component information as a fallback.
     */
    private void extractDirectDependenciesFromComponent(SpringComponentInfo component, Set<String> dependencies) {
        try {
            // Try to get the CtClass for more detailed analysis
            CtClass ctClass = classPool.get(component.getClassName());

            // Analyze constructor dependencies
            analyzeConstructorDependencies(ctClass, dependencies);

            // Analyze field dependencies
            analyzeFieldDependencies(ctClass, dependencies);

            // Analyze method dependencies
            analyzeMethodDependencies(ctClass, dependencies);

        } catch (Exception e) {
            LOGGER.debug("Could not extract direct dependencies for {}: {}",
                component.getClassName(), e.getMessage());
        }
    }

    private void analyzeConstructorDependencies(CtClass ctClass, Set<String> dependencies) {
        try {
            CtConstructor[] constructors = ctClass.getConstructors();
            for (CtConstructor constructor : constructors) {
                if (constructor.hasAnnotation("org.springframework.beans.factory.annotation.Autowired") ||
                    (constructors.length == 1 && constructor.getParameterTypes().length > 0)) {

                    CtClass[] paramTypes = constructor.getParameterTypes();
                    for (CtClass paramType : paramTypes) {
                        if (springComponents.containsKey(paramType.getName())) {
                            dependencies.add(paramType.getName());
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Error analyzing constructor dependencies: {}", e.getMessage());
        }
    }

    private void analyzeFieldDependencies(CtClass ctClass, Set<String> dependencies) {
        try {
            CtField[] fields = ctClass.getDeclaredFields();
            for (CtField field : fields) {
                if (field.hasAnnotation("org.springframework.beans.factory.annotation.Autowired")) {
                    String fieldType = field.getType().getName();
                    if (springComponents.containsKey(fieldType)) {
                        dependencies.add(fieldType);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Error analyzing field dependencies: {}", e.getMessage());
        }
    }

    private void analyzeMethodDependencies(CtClass ctClass, Set<String> dependencies) {
        try {
            CtMethod[] methods = ctClass.getDeclaredMethods();
            for (CtMethod method : methods) {
                if (method.hasAnnotation("org.springframework.beans.factory.annotation.Autowired")) {
                    CtClass[] paramTypes = method.getParameterTypes();
                    for (CtClass paramType : paramTypes) {
                        if (springComponents.containsKey(paramType.getName())) {
                            dependencies.add(paramType.getName());
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Error analyzing method dependencies: {}", e.getMessage());
        }
    }

    /**
     * Phase 3: Detects circular dependencies using Depth-First Search.
     */
    private void detectCircularDependenciesWithDFS() {
        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();

        for (String componentClass : springComponents.keySet()) {
            if (!visited.contains(componentClass)) {
                List<String> currentPath = new ArrayList<>();
                detectCyclesDFS(componentClass, visited, recursionStack, currentPath, 0);

                // Limit the number of cycles to prevent excessive analysis
                if (detectedCycles.size() >= MAX_CYCLES_TO_DETECT) {
                    LOGGER.warn("Maximum cycle detection limit reached ({}), stopping analysis", MAX_CYCLES_TO_DETECT);
                    break;
                }
            }
        }
    }

    /**
     * DFS algorithm for cycle detection with Spring context awareness.
     */
    private void detectCyclesDFS(String currentClass, Set<String> visited, Set<String> recursionStack,
                                List<String> currentPath, int depth) {

        if (depth > MAX_DFS_DEPTH) {
            LOGGER.warn("Maximum DFS depth reached for path starting at: {}", currentPath.get(0));
            return;
        }

        visited.add(currentClass);
        recursionStack.add(currentClass);
        currentPath.add(currentClass);

        Set<String> dependencies = dependencyGraph.getOrDefault(currentClass, Collections.emptySet());

        for (String dependency : dependencies) {
            if (!visited.contains(dependency)) {
                // Continue DFS
                detectCyclesDFS(dependency, visited, recursionStack, currentPath, depth + 1);
            } else if (recursionStack.contains(dependency)) {
                // Cycle detected
                int cycleStartIndex = currentPath.indexOf(dependency);
                if (cycleStartIndex != -1) {
                    List<String> cycle = new ArrayList<>(currentPath.subList(cycleStartIndex, currentPath.size()));
                    cycle.add(dependency); // Close the cycle

                    if (cycle.size() <= MAX_CYCLE_LENGTH) {
                        SpringCircularDependency circularDependency = createCircularDependency(cycle);
                        detectedCycles.add(circularDependency);

                        LOGGER.debug("Detected circular dependency: {}",
                            cycle.stream().map(this::getSimpleClassName).collect(Collectors.joining(" -> ")));
                    }
                }
            }
        }

        recursionStack.remove(currentClass);
        currentPath.remove(currentPath.size() - 1);
    }

    /**
     * Creates a SpringCircularDependency from a detected cycle.
     */
    private SpringCircularDependency createCircularDependency(List<String> cycle) {
        SpringCircularDependency circularDependency = new SpringCircularDependency();
        circularDependency.setCycle(new ArrayList<>(cycle));
        circularDependency.setCycleLength(cycle.size() - 1); // Exclude duplicate end node

        // Extract injection information for the cycle
        List<SpringDependencyInfo> cycleInjections = extractCycleInjections(cycle);
        circularDependency.setCycleInjections(cycleInjections);

        return circularDependency;
    }

    /**
     * Extracts injection information for a dependency cycle.
     */
    private List<SpringDependencyInfo> extractCycleInjections(List<String> cycle) {
        List<SpringDependencyInfo> injections = new ArrayList<>();

        for (int i = 0; i < cycle.size() - 1; i++) {
            String sourceClass = cycle.get(i);
            String targetClass = cycle.get(i + 1);

            SpringComponentInfo sourceComponent = springComponents.get(sourceClass);
            if (sourceComponent != null) {
                // Find the specific dependency injection
                BeanDependency dependency = sourceComponent.getDependencies().stream()
                    .filter(dep -> dep.getType().equals(targetClass))
                    .findFirst()
                    .orElse(null);

                if (dependency != null) {
                    SpringDependencyInfo injectionInfo = new SpringDependencyInfo();
                    injectionInfo.setSourceClass(sourceClass);
                    injectionInfo.setTargetClass(targetClass);
                    injectionInfo.setInjectionType(dependency.getInjectionType());
                    injectionInfo.setRequired(dependency.isRequired());
                    injectionInfo.setQualifier(dependency.getQualifier());
                    injectionInfo.setInjectionPoint(dependency.getInjectionPoint());

                    injections.add(injectionInfo);
                }
            }
        }

        return injections;
    }

    /**
     * Phase 4: Analyzes detected cycles for Spring-specific characteristics.
     */
    private List<SpringCircularDependency> analyzeDetectedCycles() {
        List<SpringCircularDependency> analyzedCycles = new ArrayList<>();

        for (SpringCircularDependency cycle : detectedCycles) {
            try {
                // Determine severity based on injection types
                CircularDependency.CircularDependencySeverity severity = determineSeverity(cycle);
                cycle.setSeverity(severity);

                // Determine circular dependency type
                SpringCircularDependencyType type = determineCircularDependencyType(cycle);
                cycle.setType(type);

                // Check for existing @Lazy resolution
                boolean hasLazyResolution = checkForLazyResolution(cycle);
                cycle.setHasLazyResolution(hasLazyResolution);

                // Calculate risk assessment
                SpringCircularDependencyRisk risk = assessRisk(cycle);
                cycle.setRisk(risk);

                analyzedCycles.add(cycle);

            } catch (Exception e) {
                LOGGER.warn("Error analyzing circular dependency cycle: {}", e.getMessage());
            }
        }

        return analyzedCycles;
    }

    /**
     * Determines severity based on Spring injection types and patterns.
     */
    private CircularDependency.CircularDependencySeverity determineSeverity(SpringCircularDependency cycle) {
        boolean hasFieldInjection = cycle.getCycleInjections().stream()
            .anyMatch(injection -> injection.getInjectionType() == DependencyInjectionType.FIELD);

        boolean hasConstructorInjection = cycle.getCycleInjections().stream()
            .anyMatch(injection -> injection.getInjectionType() == DependencyInjectionType.CONSTRUCTOR);

        boolean hasLazyDependency = cycle.getCycleInjections().stream()
            .anyMatch(injection -> {
                SpringComponentInfo component = springComponents.get(injection.getSourceClass());
                return component != null && component.isHasLazyDependencies();
            });

        // CRITICAL: Field injection without @Lazy (Spring cannot resolve)
        if (hasFieldInjection && !hasLazyDependency) {
            return CircularDependency.CircularDependencySeverity.CRITICAL;
        }

        // HIGH: Constructor injection without @Lazy (BeanCurrentlyInCreationException)
        if (hasConstructorInjection && !hasLazyDependency) {
            return CircularDependency.CircularDependencySeverity.HIGH;
        }

        // MEDIUM: Mixed injection types or complex cycles
        if (cycle.getCycleLength() > 4 || (hasFieldInjection && hasConstructorInjection)) {
            return CircularDependency.CircularDependencySeverity.MEDIUM;
        }

        // LOW: Cycles with @Lazy resolution
        if (hasLazyDependency) {
            return CircularDependency.CircularDependencySeverity.LOW;
        }

        return CircularDependency.CircularDependencySeverity.MEDIUM;
    }

    /**
     * Determines the type of circular dependency based on injection patterns.
     */
    private SpringCircularDependencyType determineCircularDependencyType(SpringCircularDependency cycle) {
        Set<DependencyInjectionType> injectionTypes = cycle.getCycleInjections().stream()
            .map(SpringDependencyInfo::getInjectionType)
            .collect(Collectors.toSet());

        if (injectionTypes.size() == 1) {
            DependencyInjectionType singleType = injectionTypes.iterator().next();
            switch (singleType) {
                case CONSTRUCTOR:
                    return SpringCircularDependencyType.CONSTRUCTOR_ONLY;
                case FIELD:
                    return SpringCircularDependencyType.FIELD_ONLY;
                case SETTER:
                case METHOD:
                    return SpringCircularDependencyType.METHOD_ONLY;
                default:
                    return SpringCircularDependencyType.MIXED;
            }
        } else {
            return SpringCircularDependencyType.MIXED;
        }
    }

    /**
     * Checks if the cycle already has @Lazy resolution.
     */
    private boolean checkForLazyResolution(SpringCircularDependency cycle) {
        // First check if any component in the cycle has @Lazy characteristics
        boolean hasLazyComponent = cycle.getCycle().stream()
            .anyMatch(className -> {
                SpringComponentInfo component = springComponents.get(className);
                return component != null && (component.isLazyInitialized() || component.isHasLazyDependencies());
            });

        // Also check if any injection in the cycle has @Lazy annotation
        boolean hasLazyInjection = cycle.getCycleInjections().stream()
            .anyMatch(injection -> {
                // Check if the injection point has @Lazy annotation
                return injection.getInjectionPoint() != null &&
                       injection.getInjectionPoint().contains("Lazy");
            });

        return hasLazyComponent || hasLazyInjection;
    }

    /**
     * Assesses the risk level of a circular dependency.
     */
    private SpringCircularDependencyRisk assessRisk(SpringCircularDependency cycle) {
        if (cycle.getSeverity() == CircularDependency.CircularDependencySeverity.CRITICAL) {
            return SpringCircularDependencyRisk.APPLICATION_STARTUP_FAILURE;
        } else if (cycle.getSeverity() == CircularDependency.CircularDependencySeverity.HIGH) {
            return SpringCircularDependencyRisk.BEAN_CREATION_EXCEPTION;
        } else if (cycle.getCycleLength() > 6) {
            return SpringCircularDependencyRisk.ARCHITECTURE_COMPLEXITY;
        } else {
            return SpringCircularDependencyRisk.MAINTENANCE_DIFFICULTY;
        }
    }

    /**
     * Phase 5: Generates Spring-specific resolution strategies.
     */
    private void generateResolutionStrategies(List<SpringCircularDependency> cycles) {
        for (SpringCircularDependency cycle : cycles) {
            List<SpringResolutionStrategy> strategies = new ArrayList<>();

            // Strategy 1: @Lazy annotation (most common Spring solution)
            if (!cycle.isHasLazyResolution()) {
                SpringResolutionStrategy lazyStrategy = createLazyResolutionStrategy(cycle);
                strategies.add(lazyStrategy);
            }

            // Strategy 2: Interface segregation
            SpringResolutionStrategy interfaceStrategy = createInterfaceSegregationStrategy(cycle);
            strategies.add(interfaceStrategy);

            // Strategy 3: Setter injection conversion (for constructor cycles)
            if (cycle.getType() == SpringCircularDependencyType.CONSTRUCTOR_ONLY) {
                SpringResolutionStrategy setterStrategy = createSetterInjectionStrategy(cycle);
                strategies.add(setterStrategy);
            }

            // Strategy 4: Event-driven decoupling
            SpringResolutionStrategy eventStrategy = createEventDrivenStrategy(cycle);
            strategies.add(eventStrategy);

            // Strategy 5: Architectural refactoring (for complex cycles)
            if (cycle.getCycleLength() > 4) {
                SpringResolutionStrategy refactorStrategy = createArchitecturalRefactoringStrategy(cycle);
                strategies.add(refactorStrategy);
            }

            cycle.setResolutionStrategies(strategies);
        }
    }

    /**
     * Creates @Lazy resolution strategy.
     */
    private SpringResolutionStrategy createLazyResolutionStrategy(SpringCircularDependency cycle) {
        String bestTarget = findBestLazyTarget(cycle);

        return SpringResolutionStrategy.builder()
            .type(SpringResolutionStrategyType.LAZY_INITIALIZATION)
            .description("Add @Lazy annotation to break the circular dependency")
            .complexity(SpringResolutionComplexity.LOW)
            .targetClass(bestTarget)
            .implementation(String.format(
                "Add @Lazy annotation to the dependency injection point in %s",
                getSimpleClassName(bestTarget)))
            .impact("Delays bean initialization until first access, breaking the cycle")
            .build();
    }

    /**
     * Creates interface segregation strategy.
     */
    private SpringResolutionStrategy createInterfaceSegregationStrategy(SpringCircularDependency cycle) {
        return SpringResolutionStrategy.builder()
            .type(SpringResolutionStrategyType.INTERFACE_SEGREGATION)
            .description("Introduce interfaces to eliminate direct class dependencies")
            .complexity(SpringResolutionComplexity.MEDIUM)
            .implementation("Extract interfaces for classes in the cycle and inject interfaces instead of concrete classes")
            .impact("Reduces coupling and makes the system more testable and maintainable")
            .build();
    }

    /**
     * Creates setter injection strategy.
     */
    private SpringResolutionStrategy createSetterInjectionStrategy(SpringCircularDependency cycle) {
        return SpringResolutionStrategy.builder()
            .type(SpringResolutionStrategyType.SETTER_INJECTION)
            .description("Convert constructor injection to setter injection for one dependency")
            .complexity(SpringResolutionComplexity.LOW)
            .implementation("Change @Autowired from constructor parameter to setter method for one dependency in the cycle")
            .impact("Allows Spring to create beans first, then inject dependencies via setters")
            .build();
    }

    /**
     * Creates event-driven strategy.
     */
    private SpringResolutionStrategy createEventDrivenStrategy(SpringCircularDependency cycle) {
        return SpringResolutionStrategy.builder()
            .type(SpringResolutionStrategyType.EVENT_DRIVEN)
            .description("Use ApplicationEventPublisher for loose coupling")
            .complexity(SpringResolutionComplexity.MEDIUM)
            .implementation("Replace direct method calls with Spring application events")
            .impact("Completely decouples components, making them independent of each other")
            .build();
    }

    /**
     * Creates architectural refactoring strategy.
     */
    private SpringResolutionStrategy createArchitecturalRefactoringStrategy(SpringCircularDependency cycle) {
        return SpringResolutionStrategy.builder()
            .type(SpringResolutionStrategyType.ARCHITECTURAL_REFACTORING)
            .description("Refactor architecture to eliminate circular dependencies")
            .complexity(SpringResolutionComplexity.HIGH)
            .implementation("Extract common functionality to separate services or use facade pattern")
            .impact("Improves overall architecture but requires significant code changes")
            .build();
    }

    /**
     * Finds the best target for @Lazy annotation based on component type and complexity.
     */
    private String findBestLazyTarget(SpringCircularDependency cycle) {
        Map<String, Integer> targetPriority = new HashMap<>();

        for (String className : cycle.getCycle()) {
            SpringComponentInfo component = springComponents.get(className);
            if (component != null) {
                int priority = calculateLazyTargetPriority(component);
                targetPriority.put(className, priority);
            }
        }

        return targetPriority.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(cycle.getCycle().get(0));
    }

    /**
     * Calculates priority for @Lazy target selection.
     */
    private int calculateLazyTargetPriority(SpringComponentInfo component) {
        int priority = 0;

        // Priority based on component type (Services are preferred targets)
        switch (component.getComponentType()) {
            case SERVICE:
                priority += 10;
                break;
            case REPOSITORY:
                priority += 8;
                break;
            case COMPONENT:
                priority += 6;
                break;
            case CONTROLLER:
            case REST_CONTROLLER:
                priority += 4;
                break;
            case CONFIGURATION:
                priority += 2;
                break;
        }

        // Penalize components with many dependencies
        priority -= component.getDependencies().size();

        // Bonus for components already using constructor injection (easier to add @Lazy)
        if (component.isHasConstructorInjection()) {
            priority += 3;
        }

        return priority;
    }

    /**
     * Phase 6: Calculates impact metrics for the analysis.
     */
    private SpringCircularDependencyMetrics calculateImpactMetrics(List<SpringCircularDependency> cycles) {
        int totalComponents = springComponents.size();

        Set<String> affectedComponents = cycles.stream()
            .flatMap(cycle -> cycle.getCycle().stream())
            .collect(Collectors.toSet());

        Map<CircularDependency.CircularDependencySeverity, Long> severityDistribution = cycles.stream()
            .collect(Collectors.groupingBy(
                SpringCircularDependency::getSeverity,
                Collectors.counting()
            ));

        Map<SpringCircularDependencyType, Long> typeDistribution = cycles.stream()
            .collect(Collectors.groupingBy(
                SpringCircularDependency::getType,
                Collectors.counting()
            ));

        double averageCycleLength = cycles.stream()
            .mapToInt(SpringCircularDependency::getCycleLength)
            .average()
            .orElse(0.0);

        double complexityScore = calculateComplexityScore(cycles, totalComponents, affectedComponents.size());

        return SpringCircularDependencyMetrics.builder()
            .totalCircularDependencies(cycles.size())
            .totalComponents(totalComponents)
            .affectedComponents(affectedComponents.size())
            .circularDependencyRatio((double) affectedComponents.size() / totalComponents)
            .averageCycleLength(averageCycleLength)
            .complexityScore(complexityScore)
            .severityDistribution(severityDistribution)
            .typeDistribution(typeDistribution)
            .resolvableWithLazy(cycles.stream()
                .mapToInt(cycle -> cycle.isHasLazyResolution() ? 0 : 1)
                .sum())
            .build();
    }

    /**
     * Calculates a complexity score based on various factors.
     */
    private double calculateComplexityScore(List<SpringCircularDependency> cycles, int totalComponents, int affectedComponents) {
        if (cycles.isEmpty()) {
            return 0.0;
        }

        double score = 0.0;

        // Base score from number of cycles
        score += cycles.size() * 10;

        // Add severity weights
        for (SpringCircularDependency cycle : cycles) {
            switch (cycle.getSeverity()) {
                case CRITICAL:
                    score += 40;
                    break;
                case HIGH:
                    score += 25;
                    break;
                case MEDIUM:
                    score += 15;
                    break;
                case LOW:
                    score += 5;
                    break;
            }
        }

        // Add cycle length penalty
        score += cycles.stream()
            .mapToDouble(cycle -> cycle.getCycleLength() * 2)
            .sum();

        // Add affected components ratio
        score += (affectedComponents / (double) totalComponents) * 50;

        return Math.min(100.0, score);
    }

    /**
     * Utility method to get simple class name.
     */
    private String getSimpleClassName(String fullyQualifiedName) {
        int lastDot = fullyQualifiedName.lastIndexOf('.');
        return lastDot != -1 ? fullyQualifiedName.substring(lastDot + 1) : fullyQualifiedName;
    }
}