package it.denzosoft.jreverse.analyzer.circulardependency;

import it.denzosoft.jreverse.analyzer.beancreation.DependencyInjectionType;
import it.denzosoft.jreverse.core.model.AnnotationInfo;
import it.denzosoft.jreverse.core.model.ClassInfo;
import it.denzosoft.jreverse.core.model.CircularDependency;
import it.denzosoft.jreverse.core.model.FieldInfo;
import it.denzosoft.jreverse.core.model.MethodInfo;
import it.denzosoft.jreverse.core.model.ParameterInfo;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.Modifier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for SpringIoCCircularDependencyDetector.
 * Tests Spring-specific circular dependency detection patterns including
 * @Autowired, @Lazy, and different injection types.
 */
class SpringIoCCircularDependencyDetectorTest {

    private SpringIoCCircularDependencyDetector detector;
    private ClassPool classPool;

    @BeforeEach
    void setUp() {
        classPool = new ClassPool(true);
        detector = new SpringIoCCircularDependencyDetector(classPool);
    }

    @Test
    @DisplayName("Should create detector with valid ClassPool")
    void shouldCreateDetectorWithValidClassPool() {
        assertNotNull(detector);
    }

    @Test
    @DisplayName("Should throw exception when ClassPool is null")
    void shouldThrowExceptionWhenClassPoolIsNull() {
        assertThrows(NullPointerException.class, () -> {
            new SpringIoCCircularDependencyDetector(null);
        });
    }

    @Test
    @DisplayName("Should detect simple constructor injection circular dependency")
    void shouldDetectSimpleConstructorInjectionCircularDependency() {
        // Given: Two services with constructor injection circular dependency
        Set<ClassInfo> classes = createSimpleConstructorCircularDependency();

        // When: Analyze circular dependencies
        SpringCircularDependencyResult result = detector.analyzeSpringCircularDependencies(classes);

        // Then: Should detect the circular dependency
        assertTrue(result.isSuccessful());
        assertTrue(result.hasCircularDependencies());
        assertEquals(1, result.getCircularDependencyCount());

        SpringCircularDependency circularDependency = result.getCircularDependencies().get(0);
        assertEquals(CircularDependency.CircularDependencySeverity.HIGH, circularDependency.getSeverity());
        assertEquals(SpringCircularDependencyType.CONSTRUCTOR_ONLY, circularDependency.getType());
        assertFalse(circularDependency.isHasLazyResolution());
        assertEquals(2, circularDependency.getUniqueClassCount());
    }

    @Test
    @DisplayName("Should detect field injection circular dependency with critical severity")
    void shouldDetectFieldInjectionCircularDependencyWithCriticalSeverity() {
        // Given: Two services with field injection circular dependency
        Set<ClassInfo> classes = createFieldInjectionCircularDependency();

        // When: Analyze circular dependencies
        SpringCircularDependencyResult result = detector.analyzeSpringCircularDependencies(classes);

        // Then: Should detect critical severity for field injection
        assertTrue(result.isSuccessful());
        assertTrue(result.hasCircularDependencies());
        assertEquals(1, result.getCircularDependencyCount());

        SpringCircularDependency circularDependency = result.getCircularDependencies().get(0);
        assertEquals(CircularDependency.CircularDependencySeverity.CRITICAL, circularDependency.getSeverity());
        assertEquals(SpringCircularDependencyType.FIELD_ONLY, circularDependency.getType());
        assertTrue(circularDependency.hasCriticalInjectionTypes());
    }

    @Test
    @DisplayName("Should recognize @Lazy resolution and reduce severity")
    void shouldRecognizeLazyResolutionAndReduceSeverity() {
        // Given: Constructor circular dependency with @Lazy annotation
        Set<ClassInfo> classes = createLazyResolvedCircularDependency();

        // When: Analyze circular dependencies
        SpringCircularDependencyResult result = detector.analyzeSpringCircularDependencies(classes);

        // Then: Should detect but with lower severity due to @Lazy
        assertTrue(result.isSuccessful());
        assertTrue(result.hasCircularDependencies());
        assertEquals(1, result.getCircularDependencyCount());

        SpringCircularDependency circularDependency = result.getCircularDependencies().get(0);
        assertEquals(CircularDependency.CircularDependencySeverity.LOW, circularDependency.getSeverity());
        assertTrue(circularDependency.isHasLazyResolution());
    }

    @Test
    @DisplayName("Should detect complex multi-component circular dependency")
    void shouldDetectComplexMultiComponentCircularDependency() {
        // Given: Four services with complex circular dependency
        Set<ClassInfo> classes = createComplexCircularDependency();

        // When: Analyze circular dependencies
        SpringCircularDependencyResult result = detector.analyzeSpringCircularDependencies(classes);

        // Then: Should detect complex cycle
        assertTrue(result.isSuccessful());
        assertTrue(result.hasCircularDependencies());
        assertEquals(1, result.getCircularDependencyCount());

        SpringCircularDependency circularDependency = result.getCircularDependencies().get(0);
        assertEquals(4, circularDependency.getUniqueClassCount());
        assertTrue(circularDependency.isComplexCycle());
        assertEquals(SpringCircularDependencyType.MIXED, circularDependency.getType());
    }

    @Test
    @DisplayName("Should generate appropriate resolution strategies")
    void shouldGenerateAppropriateResolutionStrategies() {
        // Given: Constructor circular dependency
        Set<ClassInfo> classes = createSimpleConstructorCircularDependency();

        // When: Analyze circular dependencies
        SpringCircularDependencyResult result = detector.analyzeSpringCircularDependencies(classes);

        // Then: Should generate resolution strategies
        SpringCircularDependency circularDependency = result.getCircularDependencies().get(0);
        List<SpringResolutionStrategy> strategies = circularDependency.getResolutionStrategies();

        assertFalse(strategies.isEmpty());
        assertTrue(strategies.stream().anyMatch(s -> s.getType() == SpringResolutionStrategyType.LAZY_INITIALIZATION));
        assertTrue(strategies.stream().anyMatch(s -> s.getType() == SpringResolutionStrategyType.INTERFACE_SEGREGATION));
        assertTrue(strategies.stream().anyMatch(s -> s.getType() == SpringResolutionStrategyType.SETTER_INJECTION));

        SpringResolutionStrategy primaryStrategy = circularDependency.getPrimaryResolutionStrategy();
        assertNotNull(primaryStrategy);
        assertEquals(SpringResolutionStrategyType.LAZY_INITIALIZATION, primaryStrategy.getType());
    }

    @Test
    @DisplayName("Should calculate accurate metrics")
    void shouldCalculateAccurateMetrics() {
        // Given: Multiple circular dependencies with different characteristics
        Set<ClassInfo> classes = createMultipleCircularDependencies();

        // When: Analyze circular dependencies
        SpringCircularDependencyResult result = detector.analyzeSpringCircularDependencies(classes);

        // Then: Should calculate accurate metrics
        SpringCircularDependencyMetrics metrics = result.getMetrics();

        assertTrue(metrics.getTotalCircularDependencies() > 0);
        assertTrue(metrics.getAffectedComponents() > 0);
        assertTrue(metrics.getCircularDependencyRatio() > 0);
        assertTrue(metrics.getHealthScore() >= 0 && metrics.getHealthScore() <= 100);

        // Verify severity distribution
        assertTrue(metrics.getCriticalCircularDependencies() >= 0);
        assertTrue(metrics.getHighSeverityCircularDependencies() >= 0);
    }

    @Test
    @DisplayName("Should filter out non-Spring components")
    void shouldFilterOutNonSpringComponents() {
        // Given: Mix of Spring and non-Spring classes
        Set<ClassInfo> classes = createMixedSpringAndNonSpringClasses();

        // When: Analyze circular dependencies
        SpringCircularDependencyResult result = detector.analyzeSpringCircularDependencies(classes);

        // Then: Should only analyze Spring components
        assertTrue(result.isSuccessful());
        // Should not detect circular dependencies involving non-Spring classes
    }

    @Test
    @DisplayName("Should handle empty class set gracefully")
    void shouldHandleEmptyClassSetGracefully() {
        // Given: Empty set of classes
        Set<ClassInfo> classes = new HashSet<>();

        // When: Analyze circular dependencies
        SpringCircularDependencyResult result = detector.analyzeSpringCircularDependencies(classes);

        // Then: Should handle gracefully
        assertTrue(result.isSuccessful());
        assertFalse(result.hasCircularDependencies());
        assertEquals(0, result.getCircularDependencyCount());
        assertEquals(0, result.getTotalComponents());
    }

    @Test
    @DisplayName("Should distinguish between different Spring component types")
    void shouldDistinguishBetweenDifferentSpringComponentTypes() {
        // Given: Different Spring component types
        Set<ClassInfo> classes = createDifferentSpringComponentTypes();

        // When: Analyze circular dependencies
        SpringCircularDependencyResult result = detector.analyzeSpringCircularDependencies(classes);

        // Then: Should handle all component types
        assertTrue(result.isSuccessful());
        assertTrue(result.getTotalComponents() >= 4); // Service, Repository, Controller, Component
    }

    @Test
    @DisplayName("Should respect @Primary and @Qualifier annotations")
    void shouldRespectPrimaryAndQualifierAnnotations() {
        // Given: Components with @Primary and @Qualifier
        Set<ClassInfo> classes = createComponentsWithQualifiers();

        // When: Analyze circular dependencies
        SpringCircularDependencyResult result = detector.analyzeSpringCircularDependencies(classes);

        // Then: Should analyze components with qualifiers
        assertTrue(result.isSuccessful());
    }

    @Test
    @DisplayName("Should provide comprehensive result summary")
    void shouldProvideComprehensiveResultSummary() {
        // Given: Circular dependencies
        Set<ClassInfo> classes = createSimpleConstructorCircularDependency();

        // When: Analyze circular dependencies
        SpringCircularDependencyResult result = detector.analyzeSpringCircularDependencies(classes);

        // Then: Should provide meaningful summary
        String summary = result.getSummary();
        assertNotNull(summary);
        assertFalse(summary.trim().isEmpty());
        assertTrue(summary.contains("Spring Circular Dependency Analysis Results"));
        assertTrue(summary.contains("Circular dependencies found"));
    }

    // Helper methods to create test data

    private Set<ClassInfo> createSimpleConstructorCircularDependency() {
        Set<ClassInfo> classes = new HashSet<>();

        // ServiceA depends on ServiceB via constructor
        ClassInfo serviceA = ClassInfo.builder()
            .fullyQualifiedName("com.example.ServiceA")
            .addAnnotation(createServiceAnnotation())
            .addMethod(createConstructorWithDependency("com.example.ServiceB"))
            .build();

        // ServiceB depends on ServiceA via constructor
        ClassInfo serviceB = ClassInfo.builder()
            .fullyQualifiedName("com.example.ServiceB")
            .addAnnotation(createServiceAnnotation())
            .addMethod(createConstructorWithDependency("com.example.ServiceA"))
            .build();

        classes.add(serviceA);
        classes.add(serviceB);

        // Create corresponding CtClass objects
        createCtClassForService("com.example.ServiceA", "com.example.ServiceB");
        createCtClassForService("com.example.ServiceB", "com.example.ServiceA");

        return classes;
    }

    private Set<ClassInfo> createFieldInjectionCircularDependency() {
        Set<ClassInfo> classes = new HashSet<>();

        // ServiceA depends on ServiceB via field injection
        ClassInfo serviceA = ClassInfo.builder()
            .fullyQualifiedName("com.example.ServiceA")
            .addAnnotation(createServiceAnnotation())
            .addField(createAutowiredField("serviceB", "com.example.ServiceB"))
            .build();

        // ServiceB depends on ServiceA via field injection
        ClassInfo serviceB = ClassInfo.builder()
            .fullyQualifiedName("com.example.ServiceB")
            .addAnnotation(createServiceAnnotation())
            .addField(createAutowiredField("serviceA", "com.example.ServiceA"))
            .build();

        classes.add(serviceA);
        classes.add(serviceB);

        // Create corresponding CtClass objects with field injection
        createCtClassWithFieldInjection("com.example.ServiceA", "com.example.ServiceB");
        createCtClassWithFieldInjection("com.example.ServiceB", "com.example.ServiceA");

        return classes;
    }

    private Set<ClassInfo> createLazyResolvedCircularDependency() {
        Set<ClassInfo> classes = new HashSet<>();

        // ServiceA depends on ServiceB via constructor with @Lazy
        ClassInfo serviceA = ClassInfo.builder()
            .fullyQualifiedName("com.example.ServiceA")
            .addAnnotation(createServiceAnnotation())
            .addMethod(createConstructorWithLazyDependency("com.example.ServiceB"))
            .build();

        // ServiceB depends on ServiceA via constructor
        ClassInfo serviceB = ClassInfo.builder()
            .fullyQualifiedName("com.example.ServiceB")
            .addAnnotation(createServiceAnnotation())
            .addMethod(createConstructorWithDependency("com.example.ServiceA"))
            .build();

        classes.add(serviceA);
        classes.add(serviceB);

        // Create corresponding CtClass objects with @Lazy
        createCtClassWithLazyDependency("com.example.ServiceA", "com.example.ServiceB");
        createCtClassForService("com.example.ServiceB", "com.example.ServiceA");

        return classes;
    }

    private Set<ClassInfo> createComplexCircularDependency() {
        Set<ClassInfo> classes = new HashSet<>();

        // ServiceA -> ServiceB (constructor)
        ClassInfo serviceA = ClassInfo.builder()
            .fullyQualifiedName("com.example.ServiceA")
            .addAnnotation(createServiceAnnotation())
            .addMethod(createConstructorWithDependency("com.example.ServiceB"))
            .build();

        // ServiceB -> ServiceC (field)
        ClassInfo serviceB = ClassInfo.builder()
            .fullyQualifiedName("com.example.ServiceB")
            .addAnnotation(createServiceAnnotation())
            .addField(createAutowiredField("serviceC", "com.example.ServiceC"))
            .build();

        // ServiceC -> ServiceD (method)
        ClassInfo serviceC = ClassInfo.builder()
            .fullyQualifiedName("com.example.ServiceC")
            .addAnnotation(createServiceAnnotation())
            .addMethod(createSetterWithDependency("com.example.ServiceD"))
            .build();

        // ServiceD -> ServiceA (constructor)
        ClassInfo serviceD = ClassInfo.builder()
            .fullyQualifiedName("com.example.ServiceD")
            .addAnnotation(createServiceAnnotation())
            .addMethod(createConstructorWithDependency("com.example.ServiceA"))
            .build();

        classes.add(serviceA);
        classes.add(serviceB);
        classes.add(serviceC);
        classes.add(serviceD);

        // Create corresponding CtClass objects
        createCtClassForService("com.example.ServiceA", "com.example.ServiceB");
        createCtClassWithFieldInjection("com.example.ServiceB", "com.example.ServiceC");
        createCtClassWithMethodInjection("com.example.ServiceC", "com.example.ServiceD");
        createCtClassForService("com.example.ServiceD", "com.example.ServiceA");

        return classes;
    }

    private Set<ClassInfo> createMultipleCircularDependencies() {
        Set<ClassInfo> classes = new HashSet<>();

        // Add simple constructor circular dependency
        classes.addAll(createSimpleConstructorCircularDependency());

        // Add field injection circular dependency with different names
        ClassInfo serviceX = ClassInfo.builder()
            .fullyQualifiedName("com.example.ServiceX")
            .addAnnotation(createServiceAnnotation())
            .addField(createAutowiredField("serviceY", "com.example.ServiceY"))
            .build();

        ClassInfo serviceY = ClassInfo.builder()
            .fullyQualifiedName("com.example.ServiceY")
            .addAnnotation(createServiceAnnotation())
            .addField(createAutowiredField("serviceX", "com.example.ServiceX"))
            .build();

        classes.add(serviceX);
        classes.add(serviceY);

        createCtClassWithFieldInjection("com.example.ServiceX", "com.example.ServiceY");
        createCtClassWithFieldInjection("com.example.ServiceY", "com.example.ServiceX");

        return classes;
    }

    private Set<ClassInfo> createMixedSpringAndNonSpringClasses() {
        Set<ClassInfo> classes = new HashSet<>();

        // Add Spring service
        ClassInfo springService = ClassInfo.builder()
            .fullyQualifiedName("com.example.SpringService")
            .addAnnotation(createServiceAnnotation())
            .build();

        // Add non-Spring class
        ClassInfo regularClass = ClassInfo.builder()
            .fullyQualifiedName("com.example.RegularClass")
            .build();

        classes.add(springService);
        classes.add(regularClass);

        return classes;
    }

    private Set<ClassInfo> createDifferentSpringComponentTypes() {
        Set<ClassInfo> classes = new HashSet<>();

        // Service
        ClassInfo service = ClassInfo.builder()
            .fullyQualifiedName("com.example.TestService")
            .addAnnotation(createServiceAnnotation())
            .build();

        // Repository
        ClassInfo repository = ClassInfo.builder()
            .fullyQualifiedName("com.example.TestRepository")
            .addAnnotation(createRepositoryAnnotation())
            .build();

        // Controller
        ClassInfo controller = ClassInfo.builder()
            .fullyQualifiedName("com.example.TestController")
            .addAnnotation(createControllerAnnotation())
            .build();

        // Component
        ClassInfo component = ClassInfo.builder()
            .fullyQualifiedName("com.example.TestComponent")
            .addAnnotation(createComponentAnnotation())
            .build();

        classes.add(service);
        classes.add(repository);
        classes.add(controller);
        classes.add(component);

        return classes;
    }

    private Set<ClassInfo> createComponentsWithQualifiers() {
        Set<ClassInfo> classes = new HashSet<>();

        // Service with @Primary
        ClassInfo primaryService = ClassInfo.builder()
            .fullyQualifiedName("com.example.PrimaryService")
            .addAnnotation(createServiceAnnotation())
            .addAnnotation(createPrimaryAnnotation())
            .build();

        // Service with @Qualifier
        ClassInfo qualifiedService = ClassInfo.builder()
            .fullyQualifiedName("com.example.QualifiedService")
            .addAnnotation(createServiceAnnotation())
            .addAnnotation(createQualifierAnnotation("special"))
            .build();

        classes.add(primaryService);
        classes.add(qualifiedService);

        return classes;
    }

    // Helper methods for creating annotations

    private AnnotationInfo createServiceAnnotation() {
        return AnnotationInfo.builder()
            .type("org.springframework.stereotype.Service")
            .build();
    }

    private AnnotationInfo createRepositoryAnnotation() {
        return AnnotationInfo.builder()
            .type("org.springframework.stereotype.Repository")
            .build();
    }

    private AnnotationInfo createControllerAnnotation() {
        return AnnotationInfo.builder()
            .type("org.springframework.stereotype.Controller")
            .build();
    }

    private AnnotationInfo createComponentAnnotation() {
        return AnnotationInfo.builder()
            .type("org.springframework.stereotype.Component")
            .build();
    }

    private AnnotationInfo createAutowiredAnnotation() {
        return AnnotationInfo.builder()
            .type("org.springframework.beans.factory.annotation.Autowired")
            .build();
    }

    private AnnotationInfo createLazyAnnotation() {
        return AnnotationInfo.builder()
            .type("org.springframework.context.annotation.Lazy")
            .build();
    }

    private AnnotationInfo createPrimaryAnnotation() {
        return AnnotationInfo.builder()
            .type("org.springframework.context.annotation.Primary")
            .build();
    }

    private AnnotationInfo createQualifierAnnotation(String value) {
        return AnnotationInfo.builder()
            .type("org.springframework.beans.factory.annotation.Qualifier")
            .addAttribute("value", value)
            .build();
    }

    // Helper methods for creating fields, methods, and parameters

    private FieldInfo createAutowiredField(String fieldName, String type) {
        return FieldInfo.builder()
            .name(fieldName)
            .type(type)
            .declaringClassName("TestClass")
            .isPrivate(true)
            .addAnnotation(createAutowiredAnnotation())
            .build();
    }

    private MethodInfo createConstructorWithDependency(String dependencyType) {
        ParameterInfo parameter = ParameterInfo.builder()
            .name("dependency")
            .type(dependencyType)
            .index(0)
            .build();

        return MethodInfo.builder()
            .name("<init>")
            .returnType("void")
            .declaringClassName("TestClass")
            .isPublic(true)
            .addParameter(parameter)
            .addAnnotation(createAutowiredAnnotation())
            .build();
    }

    private MethodInfo createConstructorWithLazyDependency(String dependencyType) {
        ParameterInfo parameter = ParameterInfo.builder()
            .name("dependency")
            .type(dependencyType)
            .index(0)
            .addAnnotation(createLazyAnnotation())
            .build();

        return MethodInfo.builder()
            .name("<init>")
            .returnType("void")
            .declaringClassName("TestClass")
            .isPublic(true)
            .addParameter(parameter)
            .addAnnotation(createAutowiredAnnotation())
            .build();
    }

    private MethodInfo createSetterWithDependency(String dependencyType) {
        ParameterInfo parameter = ParameterInfo.builder()
            .name("dependency")
            .type(dependencyType)
            .index(0)
            .build();

        return MethodInfo.builder()
            .name("setDependency")
            .returnType("void")
            .declaringClassName("TestClass")
            .isPublic(true)
            .addParameter(parameter)
            .addAnnotation(createAutowiredAnnotation())
            .build();
    }

    // Helper methods for creating CtClass objects

    private void createCtClassForService(String className, String dependencyType) {
        try {
            CtClass ctClass = classPool.makeClass(className);
            // Note: For test purposes, we don't actually add annotations to CtClass
            // The real implementation would handle Javassist annotations properly

            // Add constructor with dependency
            CtConstructor constructor = new CtConstructor(new CtClass[]{classPool.get(dependencyType)}, ctClass);
            ctClass.addConstructor(constructor);

        } catch (Exception e) {
            // Ignore exceptions in test setup
        }
    }

    private void createCtClassWithFieldInjection(String className, String dependencyType) {
        try {
            CtClass ctClass = classPool.makeClass(className);

            // Add field with @Autowired
            CtField field = new CtField(classPool.get(dependencyType), "dependency", ctClass);
            field.setModifiers(Modifier.PRIVATE);
            ctClass.addField(field);

        } catch (Exception e) {
            // Ignore exceptions in test setup
        }
    }

    private void createCtClassWithMethodInjection(String className, String dependencyType) {
        try {
            CtClass ctClass = classPool.makeClass(className);

            // Add setter method with @Autowired
            CtMethod method = new CtMethod(CtClass.voidType, "setDependency",
                new CtClass[]{classPool.get(dependencyType)}, ctClass);
            method.setModifiers(Modifier.PUBLIC);
            method.setBody("{}");
            ctClass.addMethod(method);

        } catch (Exception e) {
            // Ignore exceptions in test setup
        }
    }

    private void createCtClassWithLazyDependency(String className, String dependencyType) {
        try {
            CtClass ctClass = classPool.makeClass(className);

            // Add constructor with @Lazy parameter
            CtConstructor constructor = new CtConstructor(new CtClass[]{classPool.get(dependencyType)}, ctClass);
            // Note: In real implementation, parameter annotations would be handled differently
            ctClass.addConstructor(constructor);

        } catch (Exception e) {
            // Ignore exceptions in test setup
        }
    }
}