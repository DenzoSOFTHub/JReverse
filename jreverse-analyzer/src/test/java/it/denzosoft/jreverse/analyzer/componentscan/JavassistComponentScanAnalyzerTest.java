package it.denzosoft.jreverse.analyzer.componentscan;

import it.denzosoft.jreverse.core.model.*;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.BooleanMemberValue;
import javassist.bytecode.annotation.StringMemberValue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JavassistComponentScanAnalyzer.
 * Tests component scan detection and configuration parsing.
 */
@ExtendWith(MockitoExtension.class)
class JavassistComponentScanAnalyzerTest {
    
    @Mock
    private JarContent jarContent;
    
    @Mock
    private ClassPool classPool;
    
    private JavassistComponentScanAnalyzer analyzer;
    
    @BeforeEach
    void setUp() {
        analyzer = new JavassistComponentScanAnalyzer();
    }
    
    @Test
    void testCanAnalyze_ValidJarContent_ReturnsTrue() {
        when(jarContent.getClassPool()).thenReturn(classPool);
        when(jarContent.getClassCount()).thenReturn(5);
        
        boolean result = analyzer.canAnalyze(jarContent);
        
        assertTrue(result);
    }
    
    @Test
    void testCanAnalyze_NullJarContent_ReturnsFalse() {
        boolean result = analyzer.canAnalyze(null);
        
        assertFalse(result);
    }
    
    @Test
    void testCanAnalyze_NullClassPool_ReturnsFalse() {
        when(jarContent.getClassPool()).thenReturn(null);
        
        boolean result = analyzer.canAnalyze(jarContent);
        
        assertFalse(result);
    }
    
    @Test
    void testCanAnalyze_EmptyJar_ReturnsFalse() {
        when(jarContent.getClassPool()).thenReturn(classPool);
        when(jarContent.getClassCount()).thenReturn(0);
        
        boolean result = analyzer.canAnalyze(jarContent);
        
        assertFalse(result);
    }
    
    @Test
    void testAnalyzeComponentScan_NoConfigurations_ReturnsWarning() {
        setupMockJarContent();
        when(jarContent.getClassNames()).thenReturn(Arrays.asList("com.example.TestClass"));
        
        CtClass ctClass = createMockClass("com.example.TestClass", null);
        setupClassPoolWithClass("com.example.TestClass", ctClass);
        
        ComponentScanAnalysisResult result = analyzer.analyzeComponentScan(jarContent);
        
        assertNotNull(result);
        assertFalse(result.hasConfigurations());
        assertEquals(0, result.getConfigurationCount());
        assertFalse(result.getMetadata().isSuccessful());
        assertTrue(result.getMetadata().getMessage().contains("No @ComponentScan configurations found"));
    }
    
    @Test
    void testAnalyzeComponentScan_ComponentScanWithBasePackages_ExtractsConfiguration() {
        setupMockJarContent();
        when(jarContent.getClassNames()).thenReturn(Arrays.asList("com.example.ConfigClass"));
        
        Annotation componentScanAnnotation = createComponentScanAnnotation(
            new String[]{"com.example.service", "com.example.repository"}, 
            true, false);
        CtClass ctClass = createMockClass("com.example.ConfigClass", componentScanAnnotation);
        setupClassPoolWithClass("com.example.ConfigClass", ctClass);
        
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
        setupMockJarContent();
        when(jarContent.getClassNames()).thenReturn(Arrays.asList("com.example.service.ConfigClass"));
        
        Annotation componentScanAnnotation = createComponentScanAnnotation(new String[]{}, true, false);
        CtClass ctClass = createMockClass("com.example.service.ConfigClass", componentScanAnnotation);
        setupClassPoolWithClass("com.example.service.ConfigClass", ctClass);
        
        ComponentScanAnalysisResult result = analyzer.analyzeComponentScan(jarContent);
        
        assertNotNull(result);
        assertTrue(result.hasConfigurations());
        assertEquals(1, result.getConfigurationCount());
        
        // Should use the package of the annotated class
        assertTrue(result.getEffectivePackages().contains("com.example.service"));
    }
    
    @Test
    void testAnalyzeComponentScan_SpringBootApplication_ExtractsConfiguration() {
        setupMockJarContent();
        when(jarContent.getClassNames()).thenReturn(Arrays.asList("com.example.Application"));
        
        Annotation springBootAppAnnotation = createSpringBootApplicationAnnotation(
            new String[]{"com.example.api"});
        CtClass ctClass = createMockClass("com.example.Application", springBootAppAnnotation);
        setupClassPoolWithClass("com.example.Application", ctClass);
        
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
        setupMockJarContent();
        when(jarContent.getClassNames()).thenReturn(Arrays.asList("com.example.web.Application"));
        
        Annotation springBootAppAnnotation = createSpringBootApplicationAnnotation(new String[]{});
        CtClass ctClass = createMockClass("com.example.web.Application", springBootAppAnnotation);
        setupClassPoolWithClass("com.example.web.Application", ctClass);
        
        ComponentScanAnalysisResult result = analyzer.analyzeComponentScan(jarContent);
        
        assertNotNull(result);
        assertTrue(result.hasConfigurations());
        
        // Should use the package of the SpringBootApplication class
        assertTrue(result.getEffectivePackages().contains("com.example.web"));
    }
    
    @Test
    void testAnalyzeComponentScan_MultipleConfigurations_ExtractsAll() {
        setupMockJarContent();
        when(jarContent.getClassNames()).thenReturn(Arrays.asList(
            "com.example.Application", 
            "com.example.config.ServiceConfig"));
        
        // SpringBootApplication
        Annotation springBootAppAnnotation = createSpringBootApplicationAnnotation(
            new String[]{"com.example.api"});
        CtClass appClass = createMockClass("com.example.Application", springBootAppAnnotation);
        setupClassPoolWithClass("com.example.Application", appClass);
        
        // Configuration with ComponentScan
        Annotation componentScanAnnotation = createComponentScanAnnotation(
            new String[]{"com.example.service"}, true, false);
        CtClass configClass = createMockClass("com.example.config.ServiceConfig", componentScanAnnotation);
        setupClassPoolWithClass("com.example.config.ServiceConfig", configClass);
        
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
        setupMockJarContent();
        when(jarContent.getClassNames()).thenReturn(Arrays.asList("com.example.LazyConfig"));
        
        Annotation componentScanAnnotation = createComponentScanAnnotation(
            new String[]{"com.example.service"}, false, true);
        CtClass ctClass = createMockClass("com.example.LazyConfig", componentScanAnnotation);
        setupClassPoolWithClass("com.example.LazyConfig", ctClass);
        
        ComponentScanAnalysisResult result = analyzer.analyzeComponentScan(jarContent);
        
        assertNotNull(result);
        assertTrue(result.hasConfigurations());
        
        ComponentScanConfiguration config = result.getConfigurations().get(0);
        assertFalse(config.isUseDefaultFilters());
        assertTrue(config.isLazyInit());
    }
    
    @Test
    void testAnalyzeComponentScan_ClassNotFound_ContinuesAnalysis() {
        setupMockJarContent();
        when(jarContent.getClassNames()).thenReturn(Arrays.asList(
            "com.example.NonExistent", 
            "com.example.ValidConfig"));
        
        // First class throws NotFoundException
        when(classPool.get("com.example.NonExistent")).thenThrow(new javassist.NotFoundException("Class not found"));
        
        // Second class is valid
        Annotation componentScanAnnotation = createComponentScanAnnotation(
            new String[]{"com.example.service"}, true, false);
        CtClass validClass = createMockClass("com.example.ValidConfig", componentScanAnnotation);
        setupClassPoolWithClass("com.example.ValidConfig", validClass);
        
        ComponentScanAnalysisResult result = analyzer.analyzeComponentScan(jarContent);
        
        assertNotNull(result);
        assertTrue(result.hasConfigurations());
        assertEquals(1, result.getConfigurationCount());
        
        ComponentScanConfiguration config = result.getConfigurations().get(0);
        assertEquals("com.example.ValidConfig", config.getSourceClass());
    }
    
    @Test
    void testAnalyzeComponentScan_NullClassPool_ReturnsError() {
        when(jarContent.getClassPool()).thenReturn(null);
        when(jarContent.getLocation()).thenReturn(Paths.get("test.jar"));
        
        ComponentScanAnalysisResult result = analyzer.analyzeComponentScan(jarContent);
        
        assertNotNull(result);
        assertFalse(result.isSuccessful());
        assertTrue(result.getMetadata().getMessage().contains("ClassPool is null"));
    }
    
    private void setupMockJarContent() {
        when(jarContent.getClassPool()).thenReturn(classPool);
        when(jarContent.getClassCount()).thenReturn(1);
        when(jarContent.getLocation()).thenReturn(Paths.get("test.jar"));
    }
    
    private void setupClassPoolWithClass(String className, CtClass ctClass) {
        try {
            when(classPool.get(className)).thenReturn(ctClass);
        } catch (Exception e) {
            throw new RuntimeException("Failed to setup class pool", e);
        }
    }
    
    private CtClass createMockClass(String className, Annotation annotation) {
        try {
            CtClass ctClass = mock(CtClass.class);
            when(ctClass.getName()).thenReturn(className);
            
            ClassFile classFile = mock(ClassFile.class);
            when(ctClass.getClassFile()).thenReturn(classFile);
            
            if (annotation != null) {
                AnnotationsAttribute annotationsAttr = mock(AnnotationsAttribute.class);
                when(classFile.getAttribute(AnnotationsAttribute.visibleTag)).thenReturn(annotationsAttr);
                
                if (annotation.getTypeName().contains("ComponentScan")) {
                    when(annotationsAttr.getAnnotation("org.springframework.context.annotation.ComponentScan"))
                        .thenReturn(annotation);
                } else if (annotation.getTypeName().contains("SpringBootApplication")) {
                    when(annotationsAttr.getAnnotation("org.springframework.boot.autoconfigure.SpringBootApplication"))
                        .thenReturn(annotation);
                }
            } else {
                when(classFile.getAttribute(AnnotationsAttribute.visibleTag)).thenReturn(null);
            }
            
            return ctClass;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create mock class", e);
        }
    }
    
    private Annotation createComponentScanAnnotation(String[] basePackages, boolean useDefaultFilters, boolean lazyInit) {
        Annotation annotation = mock(Annotation.class);
        when(annotation.getTypeName()).thenReturn("org.springframework.context.annotation.ComponentScan");
        
        if (basePackages.length > 0) {
            ArrayMemberValue basePackagesValue = mock(ArrayMemberValue.class);
            javassist.bytecode.annotation.MemberValue[] packageValues = 
                new javassist.bytecode.annotation.MemberValue[basePackages.length];
            
            for (int i = 0; i < basePackages.length; i++) {
                StringMemberValue packageValue = mock(StringMemberValue.class);
                when(packageValue.getValue()).thenReturn(basePackages[i]);
                packageValues[i] = packageValue;
            }
            
            when(basePackagesValue.getValue()).thenReturn(packageValues);
            when(annotation.getMemberValue("basePackages")).thenReturn(basePackagesValue);
            when(annotation.getMemberValue("value")).thenReturn(basePackagesValue);
        }
        
        BooleanMemberValue useDefaultFiltersValue = mock(BooleanMemberValue.class);
        when(useDefaultFiltersValue.getValue()).thenReturn(useDefaultFilters);
        when(annotation.getMemberValue("useDefaultFilters")).thenReturn(useDefaultFiltersValue);
        
        BooleanMemberValue lazyInitValue = mock(BooleanMemberValue.class);
        when(lazyInitValue.getValue()).thenReturn(lazyInit);
        when(annotation.getMemberValue("lazyInit")).thenReturn(lazyInitValue);
        
        return annotation;
    }
    
    private Annotation createSpringBootApplicationAnnotation(String[] scanBasePackages) {
        Annotation annotation = mock(Annotation.class);
        when(annotation.getTypeName()).thenReturn("org.springframework.boot.autoconfigure.SpringBootApplication");
        
        if (scanBasePackages.length > 0) {
            ArrayMemberValue scanBasePackagesValue = mock(ArrayMemberValue.class);
            javassist.bytecode.annotation.MemberValue[] packageValues = 
                new javassist.bytecode.annotation.MemberValue[scanBasePackages.length];
            
            for (int i = 0; i < scanBasePackages.length; i++) {
                StringMemberValue packageValue = mock(StringMemberValue.class);
                when(packageValue.getValue()).thenReturn(scanBasePackages[i]);
                packageValues[i] = packageValue;
            }
            
            when(scanBasePackagesValue.getValue()).thenReturn(packageValues);
            when(annotation.getMemberValue("scanBasePackages")).thenReturn(scanBasePackagesValue);
        }
        
        return annotation;
    }
}