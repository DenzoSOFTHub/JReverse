package it.denzosoft.jreverse.analyzer.configuration;

import it.denzosoft.jreverse.core.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for JavassistConfigurationAnalyzer.
 * Tests the advanced Phase 3 configuration analysis features including
 * conditional analysis, import chains, and bean override detection.
 */
class JavassistConfigurationAnalyzerTest {

    private JavassistConfigurationAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new JavassistConfigurationAnalyzer();
    }

    @Test
    void testCanAnalyze_WithNullContent_ReturnsFalse() {
        assertFalse(analyzer.canAnalyze(null));
    }

    @Test
    void testCanAnalyze_WithNoConfigurationClasses_ReturnsFalse() {
        JarContent jarContent = createJarContent(
            buildClassInfo("com.example.Service", ClassType.CLASS)
        );

        assertFalse(analyzer.canAnalyze(jarContent));
    }

    @Test
    void testCanAnalyze_WithConfigurationClass_ReturnsTrue() {
        ClassInfo configClass = createClassInfo("com.example.Config", ClassType.CLASS)
            .addAnnotation(createAnnotation("org.springframework.context.annotation.Configuration"))
            .build();

        JarContent jarContent = createJarContent(configClass);

        assertTrue(analyzer.canAnalyze(jarContent));
    }

    @Test
    void testCanAnalyze_WithSpringBootApplication_ReturnsTrue() {
        ClassInfo appClass = createClassInfo("com.example.Application", ClassType.CLASS)
            .addAnnotation(createAnnotation("org.springframework.boot.autoconfigure.SpringBootApplication"))
            .build();

        JarContent jarContent = createJarContent(appClass);

        assertTrue(analyzer.canAnalyze(jarContent));
    }

    @Test
    void testAnalyzeConfigurations_WithNullContent_ReturnsError() {
        ConfigurationAnalysisResult result = analyzer.analyzeConfigurations(null);

        assertNotNull(result);
        assertFalse(result.isSuccessful());
        assertTrue(result.getMetadata().getMessage().contains("cannot be null"));
    }

    @Test
    void testAnalyzeConfigurations_WithNoConfigurations_ReturnsNoConfigurations() {
        JarContent jarContent = createJarContent(
            buildClassInfo("com.example.Service", ClassType.CLASS)
        );

        ConfigurationAnalysisResult result = analyzer.analyzeConfigurations(jarContent);

        assertNotNull(result);
        assertFalse(result.hasConfigurations());
        assertEquals(0, result.getConfigurationCount());
    }

    @Test
    void testAnalyzeConfigurations_WithSimpleConfiguration_Success() {
        ClassInfo configClass = createClassInfo("com.example.Config", ClassType.CLASS)
            .addAnnotation(createAnnotation("org.springframework.context.annotation.Configuration"))
            .addMethod(createBeanMethod("dataSource", "javax.sql.DataSource").build())
            .build();

        JarContent jarContent = createJarContent(configClass);

        ConfigurationAnalysisResult result = analyzer.analyzeConfigurations(jarContent);

        assertNotNull(result);
        assertTrue(result.isSuccessful());
        assertEquals(1, result.getConfigurationCount());
        assertTrue(result.getConfigurationClasses().contains("com.example.Config"));
        assertEquals(1, result.getBeanDefinitionCount());

        BeanDefinitionInfo bean = result.getBeanDefinitions().get(0);
        assertEquals("dataSource", bean.getBeanName());
        assertEquals("javax.sql.DataSource", bean.getBeanClass());
        assertEquals("com.example.Config", bean.getDeclaringClass());
        assertEquals("dataSource", bean.getFactoryMethod());
    }

    @Test
    void testAnalyzeConfigurations_WithConditionalBeans_ExtractsConditions() {
        MethodInfo conditionalBeanMethod = createBeanMethod("conditionalBean", "com.example.Service")
            .addAnnotation(createConditionalOnPropertyAnnotation("feature.enabled", "true"))
            .build();

        ClassInfo configClass = createClassInfo("com.example.ConditionalConfig", ClassType.CLASS)
            .addAnnotation(createAnnotation("org.springframework.context.annotation.Configuration"))
            .addMethod(conditionalBeanMethod)
            .build();

        JarContent jarContent = createJarContent(configClass);

        ConfigurationAnalysisResult result = analyzer.analyzeConfigurations(jarContent);

        assertNotNull(result);
        assertTrue(result.isSuccessful());
        assertEquals(1, result.getBeanDefinitionCount());

        BeanDefinitionInfo bean = result.getBeanDefinitions().get(0);
        assertFalse(bean.getDependencies().isEmpty());
        assertTrue(bean.getDependencies().get(0).contains("CONDITIONAL"));
    }

    @Test
    void testAnalyzeConfigurations_WithProfileBeans_ExtractsProfiles() {
        MethodInfo profileBeanMethod = createBeanMethod("profileBean", "com.example.Service")
            .addAnnotation(createProfileAnnotation("dev", "test"))
            .build();

        ClassInfo configClass = createClassInfo("com.example.ProfileConfig", ClassType.CLASS)
            .addAnnotation(createAnnotation("org.springframework.context.annotation.Configuration"))
            .addMethod(profileBeanMethod)
            .build();

        JarContent jarContent = createJarContent(configClass);

        ConfigurationAnalysisResult result = analyzer.analyzeConfigurations(jarContent);

        assertNotNull(result);
        assertTrue(result.isSuccessful());
        assertEquals(1, result.getBeanDefinitionCount());

        BeanDefinitionInfo bean = result.getBeanDefinitions().get(0);
        assertTrue(bean.getProfiles().contains("dev"));
        assertTrue(bean.getProfiles().contains("test"));
    }

    @Test
    void testAnalyzeConfigurations_WithPrimaryBeans_ExtractsPrimaryFlag() {
        MethodInfo primaryBeanMethod = createBeanMethod("primaryService", "com.example.Service")
            .addAnnotation(createAnnotation("org.springframework.context.annotation.Primary"))
            .build();

        ClassInfo configClass = createClassInfo("com.example.PrimaryConfig", ClassType.CLASS)
            .addAnnotation(createAnnotation("org.springframework.context.annotation.Configuration"))
            .addMethod(primaryBeanMethod)
            .build();

        JarContent jarContent = createJarContent(configClass);

        ConfigurationAnalysisResult result = analyzer.analyzeConfigurations(jarContent);

        assertNotNull(result);
        assertTrue(result.isSuccessful());
        assertEquals(1, result.getBeanDefinitionCount());

        BeanDefinitionInfo bean = result.getBeanDefinitions().get(0);
        assertTrue(bean.isPrimary());
    }

    @Test
    void testAnalyzeConfigurations_WithLazyBeans_ExtractsLazyFlag() {
        MethodInfo lazyBeanMethod = createBeanMethod("lazyService", "com.example.Service")
            .addAnnotation(createAnnotation("org.springframework.context.annotation.Lazy"))
            .build();

        ClassInfo configClass = createClassInfo("com.example.LazyConfig", ClassType.CLASS)
            .addAnnotation(createAnnotation("org.springframework.context.annotation.Configuration"))
            .addMethod(lazyBeanMethod)
            .build();

        JarContent jarContent = createJarContent(configClass);

        ConfigurationAnalysisResult result = analyzer.analyzeConfigurations(jarContent);

        assertNotNull(result);
        assertTrue(result.isSuccessful());
        assertEquals(1, result.getBeanDefinitionCount());

        BeanDefinitionInfo bean = result.getBeanDefinitions().get(0);
        assertTrue(bean.isLazy());
    }

    @Test
    void testAnalyzeConfigurations_WithScopedBeans_ExtractsScope() {
        MethodInfo scopedBeanMethod = createBeanMethod("prototypeService", "com.example.Service")
            .addAnnotation(createScopeAnnotation("prototype"))
            .build();

        ClassInfo configClass = createClassInfo("com.example.ScopeConfig", ClassType.CLASS)
            .addAnnotation(createAnnotation("org.springframework.context.annotation.Configuration"))
            .addMethod(scopedBeanMethod)
            .build();

        JarContent jarContent = createJarContent(configClass);

        ConfigurationAnalysisResult result = analyzer.analyzeConfigurations(jarContent);

        assertNotNull(result);
        assertTrue(result.isSuccessful());
        assertEquals(1, result.getBeanDefinitionCount());

        BeanDefinitionInfo bean = result.getBeanDefinitions().get(0);
        assertEquals(BeanDefinitionInfo.BeanScope.PROTOTYPE, bean.getScope());
    }

    @Test
    void testAnalyzeConfigurations_WithQualifiedBeans_ExtractsQualifiers() {
        MethodInfo qualifiedBeanMethod = createBeanMethod("qualifiedService", "com.example.Service")
            .addAnnotation(createQualifierAnnotation("special"))
            .build();

        ClassInfo configClass = createClassInfo("com.example.QualifierConfig", ClassType.CLASS)
            .addAnnotation(createAnnotation("org.springframework.context.annotation.Configuration"))
            .addMethod(qualifiedBeanMethod)
            .build();

        JarContent jarContent = createJarContent(configClass);

        ConfigurationAnalysisResult result = analyzer.analyzeConfigurations(jarContent);

        assertNotNull(result);
        assertTrue(result.isSuccessful());
        assertEquals(1, result.getBeanDefinitionCount());

        BeanDefinitionInfo bean = result.getBeanDefinitions().get(0);
        assertTrue(bean.getQualifiers().contains("special"));
    }

    @Test
    void testAnalyzeConfigurations_WithLifecycleMethods_ExtractsLifecycleCallbacks() {
        MethodInfo lifecycleBeanMethod = createBeanMethod("lifecycleService", "com.example.Service")
            .addAnnotation(createBeanAnnotationWithLifecycle("init", "destroy"))
            .build();

        ClassInfo configClass = createClassInfo("com.example.LifecycleConfig", ClassType.CLASS)
            .addAnnotation(createAnnotation("org.springframework.context.annotation.Configuration"))
            .addMethod(lifecycleBeanMethod)
            .build();

        JarContent jarContent = createJarContent(configClass);

        ConfigurationAnalysisResult result = analyzer.analyzeConfigurations(jarContent);

        assertNotNull(result);
        assertTrue(result.isSuccessful());
        assertEquals(1, result.getBeanDefinitionCount());

        BeanDefinitionInfo bean = result.getBeanDefinitions().get(0);
        assertEquals("init", bean.getInitMethod());
        assertEquals("destroy", bean.getDestroyMethod());
        assertTrue(bean.hasLifecycleCallbacks());
    }

    @Test
    void testAnalyzeConfigurations_WithMultipleConfigurations_AnalyzesAll() {
        ClassInfo config1 = createClassInfo("com.example.Config1", ClassType.CLASS)
            .addAnnotation(createAnnotation("org.springframework.context.annotation.Configuration"))
            .addMethod(createBeanMethod("service1", "com.example.Service1").build())
            .build();

        ClassInfo config2 = createClassInfo("com.example.Config2", ClassType.CLASS)
            .addAnnotation(createAnnotation("org.springframework.context.annotation.Configuration"))
            .addMethod(createBeanMethod("service2", "com.example.Service2").build())
            .build();

        JarContent jarContent = createJarContent(config1, config2);

        ConfigurationAnalysisResult result = analyzer.analyzeConfigurations(jarContent);

        assertNotNull(result);
        assertTrue(result.isSuccessful());
        assertEquals(2, result.getConfigurationCount());
        assertEquals(2, result.getBeanDefinitionCount());
        assertTrue(result.getConfigurationClasses().contains("com.example.Config1"));
        assertTrue(result.getConfigurationClasses().contains("com.example.Config2"));
    }

    @Test
    void testAnalyzeConfigurations_WithComponentClass_CreatesClassBean() {
        ClassInfo componentClass = createClassInfo("com.example.MyService", ClassType.CLASS)
            .addAnnotation(createAnnotation("org.springframework.stereotype.Service"))
            .addAnnotation(createAnnotation("org.springframework.context.annotation.Configuration"))
            .build();

        JarContent jarContent = createJarContent(componentClass);

        ConfigurationAnalysisResult result = analyzer.analyzeConfigurations(jarContent);

        assertNotNull(result);
        assertTrue(result.isSuccessful());
        assertEquals(1, result.getConfigurationCount());
        assertEquals(1, result.getBeanDefinitionCount());

        BeanDefinitionInfo bean = result.getBeanDefinitions().get(0);
        assertEquals("myService", bean.getBeanName()); // Derived from class name
        assertEquals("com.example.MyService", bean.getBeanClass());
        assertEquals("com.example.MyService", bean.getDeclaringClass());
        assertNull(bean.getFactoryMethod()); // Class bean, not factory method
    }

    @Test
    void testAnalyzeConfigurations_MeasuresAnalysisTime() {
        ClassInfo configClass = createClassInfo("com.example.Config", ClassType.CLASS)
            .addAnnotation(createAnnotation("org.springframework.context.annotation.Configuration"))
            .build();

        JarContent jarContent = createJarContent(configClass);

        ConfigurationAnalysisResult result = analyzer.analyzeConfigurations(jarContent);

        assertNotNull(result);
        assertTrue(result.getAnalysisTimeMs() > 0);
    }

    // Helper methods for creating test objects

    private JarContent createJarContent(ClassInfo... classes) {
        return JarContent.builder()
            .location(new JarLocation("/test/app.jar"))
            .classes(Set.of(classes))
            .jarType(JarType.SPRING_BOOT)
            .build();
    }

    private ClassInfo.Builder createClassInfo(String fullyQualifiedName, ClassType classType) {
        return ClassInfo.builder()
            .fullyQualifiedName(fullyQualifiedName)
            .classType(classType);
    }

    private ClassInfo buildClassInfo(String fullyQualifiedName, ClassType classType) {
        return ClassInfo.builder()
            .fullyQualifiedName(fullyQualifiedName)
            .classType(classType)
            .build();
    }

    private MethodInfo.Builder createBeanMethod(String methodName, String returnType) {
        return MethodInfo.builder()
            .name(methodName)
            .returnType(returnType)
            .declaringClassName("com.example.Config")
            .addAnnotation(createAnnotation("org.springframework.context.annotation.Bean"));
    }

    private AnnotationInfo createAnnotation(String type) {
        return AnnotationInfo.builder()
            .type(type)
            .build();
    }

    private AnnotationInfo createConditionalOnPropertyAnnotation(String property, String value) {
        return AnnotationInfo.builder()
            .type("org.springframework.boot.autoconfigure.condition.ConditionalOnProperty")
            .addAttribute("name", property)
            .addAttribute("havingValue", value)
            .build();
    }

    private AnnotationInfo createProfileAnnotation(String... profiles) {
        return AnnotationInfo.builder()
            .type("org.springframework.context.annotation.Profile")
            .addAttribute("value", profiles)
            .build();
    }

    private AnnotationInfo createScopeAnnotation(String scope) {
        return AnnotationInfo.builder()
            .type("org.springframework.context.annotation.Scope")
            .addAttribute("value", scope)
            .build();
    }

    private AnnotationInfo createQualifierAnnotation(String qualifier) {
        return AnnotationInfo.builder()
            .type("org.springframework.beans.factory.annotation.Qualifier")
            .addAttribute("value", qualifier)
            .build();
    }

    private AnnotationInfo createBeanAnnotationWithLifecycle(String initMethod, String destroyMethod) {
        return AnnotationInfo.builder()
            .type("org.springframework.context.annotation.Bean")
            .addAttribute("initMethod", initMethod)
            .addAttribute("destroyMethod", destroyMethod)
            .build();
    }
}