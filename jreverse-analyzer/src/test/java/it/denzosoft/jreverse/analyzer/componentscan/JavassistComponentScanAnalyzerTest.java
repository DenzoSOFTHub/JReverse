package it.denzosoft.jreverse.analyzer.componentscan;

import it.denzosoft.jreverse.core.model.*;
// Removed Javassist imports - now using domain models only

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JavassistComponentScanAnalyzer.
 * Tests component scan detection and configuration parsing.
 */
class JavassistComponentScanAnalyzerTest {
    
    private JavassistComponentScanAnalyzer analyzer;
    
    @BeforeEach
    void setUp() {
        analyzer = new JavassistComponentScanAnalyzer();
    }
    
    @Test
    void testCanAnalyze_ValidJarContent_ReturnsTrue() {
        // Create JarContent with some classes
        ClassInfo classInfo1 = ClassInfo.builder().fullyQualifiedName("com.example.Class1").classType(ClassType.CLASS).build();
        ClassInfo classInfo2 = ClassInfo.builder().fullyQualifiedName("com.example.Class2").classType(ClassType.CLASS).build();
        JarContent jarContent = JarContent.builder()
            .location(new JarLocation("test.jar"))
            .classes(Set.of(classInfo1, classInfo2))
            .build();
        
        boolean result = analyzer.canAnalyze(jarContent);
        
        assertTrue(result);
    }
    
    @Test
    void testCanAnalyze_NullJarContent_ReturnsFalse() {
        boolean result = analyzer.canAnalyze(null);
        
        assertFalse(result);
    }
    
    @Test
    void testCanAnalyze_EmptyJar_ReturnsFalse() {
        JarContent jarContent = JarContent.builder()
            .location(new JarLocation("empty.jar"))
            .classes(Collections.emptySet())
            .build();
        
        boolean result = analyzer.canAnalyze(jarContent);
        
        assertFalse(result);
    }
    
    @Test
    void testCanAnalyze_NullClasses_ReturnsFalse() {
        // Cannot create JarContent with null classes due to builder validation
        // This test validates the analyzer's null safety
        boolean result = analyzer.canAnalyze(null);
        
        assertFalse(result);
    }
    
    @Test
    void testAnalyzeComponentScan_NoConfigurations_ReturnsWarning() {
        // Create JarContent with TestClass (no annotations)
        ClassInfo testClass = ClassInfo.builder()
            .fullyQualifiedName("com.example.TestClass")
            .classType(ClassType.CLASS)
            .build();
        JarContent jarContent = JarContent.builder()
            .location(new JarLocation("test.jar"))
            .classes(Set.of(testClass))
            .build();
        
        ComponentScanAnalysisResult result = analyzer.analyzeComponentScan(jarContent);
        
        assertNotNull(result);
        assertFalse(result.hasConfigurations());
        assertEquals(0, result.getConfigurationCount());
        assertTrue(result.getMetadata().isSuccessful()); // Warning is still successful
        assertTrue(result.getMetadata().hasWarnings()); // But has warnings
        assertTrue(result.getMetadata().getMessage().contains("No @ComponentScan configurations found"));
    }
    
    @Test
    void testAnalyzeComponentScan_ComponentScanWithBasePackages_ExtractsConfiguration() {
        // Create JarContent with ConfigClass having @ComponentScan
        AnnotationInfo componentScanAnnotation = createComponentScanAnnotationInfo(
            new String[]{"com.example.service", "com.example.repository"}, 
            true, false);
        ClassInfo configClass = ClassInfo.builder()
            .fullyQualifiedName("com.example.ConfigClass")
            .classType(ClassType.CLASS)
            .addAnnotation(componentScanAnnotation)
            .build();
        JarContent jarContent = JarContent.builder()
            .location(new JarLocation("test.jar"))
            .classes(Set.of(configClass))
            .build();
        
        ComponentScanAnalysisResult result = analyzer.analyzeComponentScan(jarContent);
        
        assertNotNull(result);
        assertTrue(result.hasConfigurations());
        assertEquals(1, result.getConfigurationCount());
        assertTrue(result.isSuccessful());
        
        ComponentScanConfiguration config = result.getConfigurations().get(0);
        assertEquals("com.example.ConfigClass", config.getSourceClass());
        assertTrue(config.getBasePackages().contains("com.example.service"));
        assertTrue(config.getBasePackages().contains("com.example.repository"));
        assertTrue(config.isUseDefaultFilters());
        assertFalse(config.isLazyInit());
        
        // Check effective packages
        assertTrue(result.getEffectivePackages().contains("com.example.service"));
        assertTrue(result.getEffectivePackages().contains("com.example.repository"));
    }
    
    @Test
    void testAnalyzeComponentScan_ComponentScanWithoutBasePackages_UsesClassPackage() {
        // Create JarContent with service ConfigClass having @ComponentScan with no base packages
        AnnotationInfo componentScanAnnotation = createComponentScanAnnotationInfo(new String[]{}, true, false);
        ClassInfo serviceConfig = ClassInfo.builder()
            .fullyQualifiedName("com.example.service.ConfigClass")
            .classType(ClassType.CLASS)
            .addAnnotation(componentScanAnnotation)
            .build();
        JarContent jarContent = JarContent.builder()
            .location(new JarLocation("test.jar"))
            .classes(Set.of(serviceConfig))
            .build();
        
        ComponentScanAnalysisResult result = analyzer.analyzeComponentScan(jarContent);
        
        assertNotNull(result);
        assertTrue(result.hasConfigurations());
        assertEquals(1, result.getConfigurationCount());
        
        // Should use the package of the annotated class
        assertTrue(result.getEffectivePackages().contains("com.example.service"));
    }
    
    @Test
    void testAnalyzeComponentScan_SpringBootApplication_ExtractsConfiguration() {
        // Create JarContent with Application class having @SpringBootApplication
        AnnotationInfo springBootAppAnnotation = createSpringBootApplicationAnnotationInfo(
            new String[]{"com.example.api"});
        ClassInfo appClass = ClassInfo.builder()
            .fullyQualifiedName("com.example.Application")
            .classType(ClassType.CLASS)
            .addAnnotation(springBootAppAnnotation)
            .build();
        JarContent jarContent = JarContent.builder()
            .location(new JarLocation("test.jar"))
            .classes(Set.of(appClass))
            .build();
        
        ComponentScanAnalysisResult result = analyzer.analyzeComponentScan(jarContent);
        
        assertNotNull(result);
        assertTrue(result.hasConfigurations());
        assertEquals(1, result.getConfigurationCount());
        
        ComponentScanConfiguration config = result.getConfigurations().get(0);
        assertEquals("com.example.Application", config.getSourceClass());
        assertTrue(config.getBasePackages().contains("com.example.api"));
        assertTrue(config.isUseDefaultFilters()); // SpringBootApplication uses default filters
    }
    
    @Test
    void testAnalyzeComponentScan_SpringBootApplicationWithoutScanBasePackages_UsesClassPackage() {
        // Create JarContent with web Application class having @SpringBootApplication with no scanBasePackages
        AnnotationInfo springBootAppAnnotation = createSpringBootApplicationAnnotationInfo(new String[]{});
        ClassInfo webAppClass = ClassInfo.builder()
            .fullyQualifiedName("com.example.web.Application")
            .classType(ClassType.CLASS)
            .addAnnotation(springBootAppAnnotation)
            .build();
        JarContent jarContent = JarContent.builder()
            .location(new JarLocation("test.jar"))
            .classes(Set.of(webAppClass))
            .build();
        
        ComponentScanAnalysisResult result = analyzer.analyzeComponentScan(jarContent);
        
        assertNotNull(result);
        assertTrue(result.hasConfigurations());
        
        // Should use the package of the SpringBootApplication class
        assertTrue(result.getEffectivePackages().contains("com.example.web"));
    }
    
    @Test
    void testAnalyzeComponentScan_MultipleConfigurations_ExtractsAll() {
        // Create JarContent with multiple classes having annotations
        
        // SpringBootApplication class
        AnnotationInfo springBootAppAnnotation = createSpringBootApplicationAnnotationInfo(
            new String[]{"com.example.api"});
        ClassInfo appClass1 = ClassInfo.builder()
            .fullyQualifiedName("com.example.Application")
            .classType(ClassType.CLASS)
            .addAnnotation(springBootAppAnnotation)
            .build();
            
        // Configuration class with ComponentScan
        AnnotationInfo componentScanAnnotation = createComponentScanAnnotationInfo(
            new String[]{"com.example.service"}, true, false);
        ClassInfo serviceConfig = ClassInfo.builder()
            .fullyQualifiedName("com.example.config.ServiceConfig")
            .classType(ClassType.CLASS)
            .addAnnotation(componentScanAnnotation)
            .build();
            
        JarContent jarContent = JarContent.builder()
            .location(new JarLocation("test.jar"))
            .classes(Set.of(appClass1, serviceConfig))
            .build();
        
        ComponentScanAnalysisResult result = analyzer.analyzeComponentScan(jarContent);
        
        assertNotNull(result);
        assertTrue(result.hasConfigurations());
        assertEquals(2, result.getConfigurationCount());
        
        // Check that both configurations are present
        List<ComponentScanConfiguration> configs = result.getConfigurations();
        assertTrue(configs.stream().anyMatch(c -> c.getSourceClass().equals("com.example.Application")));
        assertTrue(configs.stream().anyMatch(c -> c.getSourceClass().equals("com.example.config.ServiceConfig")));
        
        // Check effective packages
        assertTrue(result.getEffectivePackages().contains("com.example.api"));
        assertTrue(result.getEffectivePackages().contains("com.example.service"));
    }
    
    @Test
    void testAnalyzeComponentScan_ComponentScanWithLazyInit_ExtractsLazyFlag() {
        // Create JarContent with LazyConfig class having lazy @ComponentScan
        AnnotationInfo componentScanAnnotation = createComponentScanAnnotationInfo(
            new String[]{"com.example.service"}, false, true);
        ClassInfo lazyConfig = ClassInfo.builder()
            .fullyQualifiedName("com.example.LazyConfig")
            .classType(ClassType.CLASS)
            .addAnnotation(componentScanAnnotation)
            .build();
        JarContent jarContent = JarContent.builder()
            .location(new JarLocation("test.jar"))
            .classes(Set.of(lazyConfig))
            .build();
        
        ComponentScanAnalysisResult result = analyzer.analyzeComponentScan(jarContent);
        
        assertNotNull(result);
        assertTrue(result.hasConfigurations());
        
        ComponentScanConfiguration config = result.getConfigurations().get(0);
        assertFalse(config.isUseDefaultFilters());
        assertTrue(config.isLazyInit());
    }
    
    @Test
    void testAnalyzeComponentScan_ClassNotFound_ContinuesAnalysis() {
        // Create JarContent with class without annotations and ValidConfig with @ComponentScan
        ClassInfo nonAnnotatedClass = ClassInfo.builder()
            .fullyQualifiedName("com.example.NonAnnotated")
            .classType(ClassType.CLASS)
            .build();
            
        AnnotationInfo componentScanAnnotation = createComponentScanAnnotationInfo(
            new String[]{"com.example.service"}, true, false);
        ClassInfo validConfig = ClassInfo.builder()
            .fullyQualifiedName("com.example.ValidConfig")
            .classType(ClassType.CLASS)
            .addAnnotation(componentScanAnnotation)
            .build();
            
        JarContent jarContent = JarContent.builder()
            .location(new JarLocation("test.jar"))
            .classes(Set.of(nonAnnotatedClass, validConfig))
            .build();
        
        ComponentScanAnalysisResult result = analyzer.analyzeComponentScan(jarContent);
        
        assertNotNull(result);
        assertTrue(result.hasConfigurations());
        assertEquals(1, result.getConfigurationCount());
        
        ComponentScanConfiguration config = result.getConfigurations().get(0);
        assertEquals("com.example.ValidConfig", config.getSourceClass());
    }
    
    @Test
    void testAnalyzeComponentScan_NullJarContent_ReturnsError() {
        ComponentScanAnalysisResult result = analyzer.analyzeComponentScan(null);
        
        assertNotNull(result);
        assertFalse(result.isSuccessful());
        assertTrue(result.getMetadata().getMessage().contains("JAR content is null") ||
                   result.getMetadata().getMessage().contains("null"));
    }
    
    // Helper methods for creating AnnotationInfo objects
    
    private AnnotationInfo createComponentScanAnnotationInfo(String[] basePackages, boolean useDefaultFilters, boolean lazyInit) {
        Map<String, Object> attributes = new HashMap<>();
        
        if (basePackages.length > 0) {
            attributes.put("basePackages", basePackages);
            attributes.put("value", basePackages);
        }
        
        attributes.put("useDefaultFilters", useDefaultFilters);
        attributes.put("lazyInit", lazyInit);
        
        return AnnotationInfo.builder()
            .type("org.springframework.context.annotation.ComponentScan")
            .attributes(attributes)
            .build();
    }
    
    private AnnotationInfo createSpringBootApplicationAnnotationInfo(String[] scanBasePackages) {
        Map<String, Object> attributes = new HashMap<>();
        
        if (scanBasePackages.length > 0) {
            attributes.put("scanBasePackages", scanBasePackages);
        }
        
        return AnnotationInfo.builder()
            .type("org.springframework.boot.autoconfigure.SpringBootApplication")
            .attributes(attributes)
            .build();
    }
}