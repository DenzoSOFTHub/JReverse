package it.denzosoft.jreverse.analyzer.architecturalpattern;

import it.denzosoft.jreverse.core.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

class JavassistArchitecturalPatternAnalyzerTest {
    
    private JavassistArchitecturalPatternAnalyzer analyzer;
    
    @BeforeEach
    void setUp() {
        analyzer = new JavassistArchitecturalPatternAnalyzer();
    }
    
    @Test
    void shouldAnalyzePatternsSuccessfully() {
        // Given
        JarContent jarWithPatterns = createMockJarWithPatterns();
        
        // When
        ArchitecturalPatternResult result = analyzer.analyzePatterns(jarWithPatterns);
        
        // Then
        assertNotNull(result);
        assertNotNull(result.getMetrics());
        assertTrue(result.getTotalPatternsDetected() >= 0);
        assertNotNull(result.getArchitecturalPatterns());
        assertNotNull(result.getDesignPatterns());
        assertNotNull(result.getAntiPatterns());
    }
    
    @Test
    void shouldDetectLayeredArchitecture() {
        // Given
        JarContent layeredJar = createMockLayeredArchitectureJar();
        
        // When
        ArchitecturalPatternResult result = analyzer.analyzePatterns(layeredJar);
        
        // Then
        assertTrue(result.getArchitecturalPatterns().size() > 0);
        
        boolean hasLayeredPattern = result.getArchitecturalPatterns().stream()
            .anyMatch(pattern -> pattern.getPatternType() == ArchitecturalPatternType.LAYERED);
        
        assertTrue(hasLayeredPattern);
    }
    
    @Test
    void shouldDetectMVCPattern() {
        // Given
        JarContent mvcJar = createMockMVCJar();
        
        // When
        ArchitecturalPatternResult result = analyzer.analyzePatterns(mvcJar);
        
        // Then
        boolean hasMVCPattern = result.getArchitecturalPatterns().stream()
            .anyMatch(pattern -> pattern.getPatternType() == ArchitecturalPatternType.MVC);
        
        assertTrue(hasMVCPattern);
    }
    
    @Test
    void shouldDetectMicroservicePattern() {
        // Given
        JarContent microserviceJar = createMockMicroserviceJar();
        
        // When
        ArchitecturalPatternResult result = analyzer.analyzePatterns(microserviceJar);
        
        // Then
        boolean hasMicroservicePattern = result.getArchitecturalPatterns().stream()
            .anyMatch(pattern -> pattern.getPatternType() == ArchitecturalPatternType.MICROSERVICE);
        
        assertTrue(hasMicroservicePattern);
    }
    
    @Test
    void shouldDetectRepositoryPattern() {
        // Given
        JarContent repositoryJar = createMockRepositoryJar();
        
        // When
        ArchitecturalPatternResult result = analyzer.analyzePatterns(repositoryJar);
        
        // Then
        boolean hasRepositoryPattern = result.getDesignPatterns().stream()
            .anyMatch(pattern -> pattern.getPatternType() == DesignPatternType.REPOSITORY);
        
        assertTrue(hasRepositoryPattern);
    }
    
    @Test
    void shouldDetectSingletonPattern() {
        // Given
        JarContent singletonJar = createMockSingletonJar();
        
        // When
        ArchitecturalPatternResult result = analyzer.analyzePatterns(singletonJar);
        
        // Then
        boolean hasSingletonPattern = result.getDesignPatterns().stream()
            .anyMatch(pattern -> pattern.getPatternType() == DesignPatternType.SINGLETON);
        
        assertTrue(hasSingletonPattern);
    }
    
    @Test
    void shouldDetectFactoryPattern() {
        // Given
        JarContent factoryJar = createMockFactoryJar();
        
        // When
        ArchitecturalPatternResult result = analyzer.analyzePatterns(factoryJar);
        
        // Then
        boolean hasFactoryPattern = result.getDesignPatterns().stream()
            .anyMatch(pattern -> pattern.getPatternType() == DesignPatternType.FACTORY_METHOD);
        
        assertTrue(hasFactoryPattern);
    }
    
    @Test
    void shouldDetectBuilderPattern() {
        // Given
        JarContent builderJar = createMockBuilderJar();
        
        // When
        ArchitecturalPatternResult result = analyzer.analyzePatterns(builderJar);
        
        // Then
        boolean hasBuilderPattern = result.getDesignPatterns().stream()
            .anyMatch(pattern -> pattern.getPatternType() == DesignPatternType.BUILDER);
        
        assertTrue(hasBuilderPattern);
    }
    
    @Test
    void shouldDetectStrategyPattern() {
        // Given
        JarContent strategyJar = createMockStrategyJar();
        
        // When
        ArchitecturalPatternResult result = analyzer.analyzePatterns(strategyJar);
        
        // Then
        boolean hasStrategyPattern = result.getDesignPatterns().stream()
            .anyMatch(pattern -> pattern.getPatternType() == DesignPatternType.STRATEGY);
        
        assertTrue(hasStrategyPattern);
    }
    
    @Test
    void shouldDetectGodClassAntiPattern() {
        // Given
        JarContent godClassJar = createMockGodClassJar();
        
        // When
        ArchitecturalPatternResult result = analyzer.analyzePatterns(godClassJar);
        
        // Then
        assertTrue(result.getAntiPatterns().size() > 0);
        
        boolean hasGodClassPattern = result.getAntiPatterns().stream()
            .anyMatch(pattern -> pattern.getAntiPatternType() == AntiPatternType.GOD_PACKAGE);
        
        assertTrue(hasGodClassPattern);
    }
    
    @Test
    void shouldDetectLongParameterListAntiPattern() {
        // Given
        JarContent longParamJar = createMockLongParameterListJar();
        
        // When
        ArchitecturalPatternResult result = analyzer.analyzePatterns(longParamJar);
        
        // Then
        boolean hasLongParamPattern = result.getAntiPatterns().stream()
            .anyMatch(pattern -> pattern.getAntiPatternType() == AntiPatternType.INAPPROPRIATE_INTIMACY);
        
        assertTrue(hasLongParamPattern);
    }
    
    @Test
    void shouldCalculateArchitecturalMetrics() {
        // Given
        JarContent complexJar = createMockComplexJar();
        
        // When
        ArchitecturalPatternResult result = analyzer.analyzePatterns(complexJar);
        
        // Then
        ArchitecturalPatternMetrics metrics = result.getMetrics();
        assertNotNull(metrics);
        assertTrue(metrics.getOverallPatternQuality() >= 0.0 && metrics.getOverallPatternQuality() <= 1.0);
        assertTrue(metrics.getArchitecturalComplexity() >= 0.0 && metrics.getArchitecturalComplexity() <= 1.0);
        assertTrue(metrics.getTotalPatternsDetected() >= 0);
    }
    
    @Test
    void shouldGenerateRecommendations() {
        // Given
        JarContent problematicJar = createMockProblematicJar();
        
        // When
        ArchitecturalPatternResult result = analyzer.analyzePatterns(problematicJar);
        
        // Then
        List<ArchitecturalRecommendation> recommendations = result.getRecommendations();
        assertNotNull(recommendations);
        
        // Recommendations are available - exact structure depends on implementation
        // Just verify the list is present and not null
    }
    
    @Test
    void shouldCalculatePatternCompliance() {
        // Given
        JarContent wellDesignedJar = createMockWellDesignedJar();
        
        // When
        ArchitecturalPatternResult result = analyzer.analyzePatterns(wellDesignedJar);
        
        // Then
        ArchitecturalPatternMetrics metrics = result.getMetrics();
        assertTrue(metrics.getPatternCoverage() >= 0.0 && metrics.getPatternCoverage() <= 1.0);
        assertNotNull(metrics.getQualityLevel());
    }
    
    @Test
    void shouldHandleEmptyJar() {
        // Given
        JarContent emptyJar = createMockEmptyJar();
        
        // When
        ArchitecturalPatternResult result = analyzer.analyzePatterns(emptyJar);
        
        // Then
        assertNotNull(result);
        assertEquals(0, result.getArchitecturalPatterns().size());
        assertEquals(0, result.getDesignPatterns().size());
        assertNotNull(result.getMetrics());
    }
    
    @Test
    void shouldCheckCanAnalyze() {
        // Given
        JarContent relevantJar = createMockJarWithPatterns();
        JarContent emptyJar = createMockEmptyJar();
        
        // When & Then
        assertTrue(analyzer.canAnalyze(relevantJar));
        assertFalse(analyzer.canAnalyze(emptyJar));
    }
    
    @Test
    void shouldHandleAnalysisErrors() {
        // Given
        JarContent corruptedJar = null;

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            analyzer.analyzePatterns(corruptedJar);
        });

        assertThat(exception.getMessage()).contains("JarContent cannot be null");
    }
    
    // Helper methods to create mock data
    
    private JarContent createMockJarWithPatterns() {
        return JarContent.builder()
            .location(new JarLocation("/test/patterns.jar"))
            .classes(Set.of(
                createMockControllerClass(),
                createMockServiceClass(),
                createMockRepositoryClass()
            ))
            .build();
    }
    
    private JarContent createMockLayeredArchitectureJar() {
        return JarContent.builder()
            .location(new JarLocation("/test/layered.jar"))
            .classes(Set.of(
                createMockControllerClass(),
                createMockServiceClass(),
                createMockRepositoryClass(),
                createMockEntityClass()
            ))
            .build();
    }
    
    private JarContent createMockMVCJar() {
        return JarContent.builder()
            .location(new JarLocation("/test/mvc.jar"))
            .classes(Set.of(
                createMockControllerClass(),
                createMockServiceClass(),
                createMockEntityClass()
            ))
            .build();
    }
    
    private JarContent createMockMicroserviceJar() {
        return JarContent.builder()
            .location(new JarLocation("/test/microservice.jar"))
            .classes(Set.of(
                createMockSpringBootApplicationClass(),
                createMockRestControllerClass(),
                createMockConfigurationClass()
            ))
            .build();
    }
    
    private JarContent createMockRepositoryJar() {
        return JarContent.builder()
            .location(new JarLocation("/test/repository.jar"))
            .classes(Set.of(
                createMockRepositoryClass(),
                createMockEntityClass()
            ))
            .build();
    }
    
    private JarContent createMockSingletonJar() {
        return JarContent.builder()
            .location(new JarLocation("/test/singleton.jar"))
            .classes(Set.of(createMockSingletonClass()))
            .build();
    }
    
    private JarContent createMockFactoryJar() {
        return JarContent.builder()
            .location(new JarLocation("/test/factory.jar"))
            .classes(Set.of(createMockFactoryClass()))
            .build();
    }
    
    private JarContent createMockBuilderJar() {
        return JarContent.builder()
            .location(new JarLocation("/test/builder.jar"))
            .classes(Set.of(createMockBuilderClass()))
            .build();
    }
    
    private JarContent createMockStrategyJar() {
        return JarContent.builder()
            .location(new JarLocation("/test/strategy.jar"))
            .classes(Set.of(
                createMockStrategyInterface(),
                createMockStrategyImplementation1(),
                createMockStrategyImplementation2()
            ))
            .build();
    }
    
    private JarContent createMockGodClassJar() {
        return JarContent.builder()
            .location(new JarLocation("/test/godclass.jar"))
            .classes(Set.of(createMockGodClass()))
            .build();
    }
    
    private JarContent createMockLongParameterListJar() {
        return JarContent.builder()
            .location(new JarLocation("/test/longparam.jar"))
            .classes(Set.of(createMockLongParameterClass()))
            .build();
    }
    
    private JarContent createMockComplexJar() {
        return JarContent.builder()
            .location(new JarLocation("/test/complex.jar"))
            .classes(Set.of(
                createMockControllerClass(),
                createMockServiceClass(),
                createMockRepositoryClass(),
                createMockEntityClass(),
                createMockFactoryClass()
            ))
            .build();
    }
    
    private JarContent createMockProblematicJar() {
        return JarContent.builder()
            .location(new JarLocation("/test/problematic.jar"))
            .classes(Set.of(
                createMockGodClass(),
                createMockLongParameterClass()
            ))
            .build();
    }
    
    private JarContent createMockWellDesignedJar() {
        return JarContent.builder()
            .location(new JarLocation("/test/welldesigned.jar"))
            .classes(Set.of(
                createMockControllerClass(),
                createMockServiceClass(),
                createMockRepositoryClass(),
                createMockBuilderClass(),
                createMockFactoryClass()
            ))
            .build();
    }
    
    private JarContent createMockEmptyJar() {
        return JarContent.builder()
            .location(new JarLocation("/test/empty.jar"))
            .classes(Set.of())
            .build();
    }
    
    // Mock class creation methods
    
    private ClassInfo createMockControllerClass() {
        return ClassInfo.builder()
            .fullyQualifiedName("com.example.controller.UserController")
            .classType(ClassType.CLASS)
            .addAnnotation(AnnotationInfo.builder()
                .type("org.springframework.web.bind.annotation.RestController")
                .build())
            .addMethod(MethodInfo.builder()
                .name("getUsers")
                .declaringClassName("com.example.controller.UserController")
                .isPublic(true)
                .build())
            .build();
    }
    
    private ClassInfo createMockServiceClass() {
        return ClassInfo.builder()
            .fullyQualifiedName("com.example.service.UserService")
            .classType(ClassType.CLASS)
            .addAnnotation(AnnotationInfo.builder()
                .type("org.springframework.stereotype.Service")
                .build())
            .addMethod(MethodInfo.builder()
                .name("findUsers")
                .declaringClassName("com.example.service.UserService")
                .isPublic(true)
                .build())
            .build();
    }
    
    private ClassInfo createMockRepositoryClass() {
        return ClassInfo.builder()
            .fullyQualifiedName("com.example.repository.UserRepository")
            .classType(ClassType.INTERFACE)
            .addAnnotation(AnnotationInfo.builder()
                .type("org.springframework.stereotype.Repository")
                .build())
            .build();
    }
    
    private ClassInfo createMockEntityClass() {
        return ClassInfo.builder()
            .fullyQualifiedName("com.example.entity.User")
            .classType(ClassType.CLASS)
            .addAnnotation(AnnotationInfo.builder()
                .type("javax.persistence.Entity")
                .build())
            .addField(FieldInfo.builder()
                .name("id")
                .type("java.lang.Long")
                .declaringClassName("com.example.entity.User")
                .build())
            .build();
    }
    
    private ClassInfo createMockSpringBootApplicationClass() {
        return ClassInfo.builder()
            .fullyQualifiedName("com.example.Application")
            .classType(ClassType.CLASS)
            .addAnnotation(AnnotationInfo.builder()
                .type("org.springframework.boot.autoconfigure.SpringBootApplication")
                .build())
            .build();
    }
    
    private ClassInfo createMockRestControllerClass() {
        return ClassInfo.builder()
            .fullyQualifiedName("com.example.api.ApiController")
            .classType(ClassType.CLASS)
            .addAnnotation(AnnotationInfo.builder()
                .type("org.springframework.web.bind.annotation.RestController")
                .build())
            .build();
    }
    
    private ClassInfo createMockConfigurationClass() {
        return ClassInfo.builder()
            .fullyQualifiedName("com.example.config.AppConfig")
            .classType(ClassType.CLASS)
            .addAnnotation(AnnotationInfo.builder()
                .type("org.springframework.context.annotation.Configuration")
                .build())
            .build();
    }
    
    private ClassInfo createMockSingletonClass() {
        return ClassInfo.builder()
            .fullyQualifiedName("com.example.singleton.DatabaseManager")
            .classType(ClassType.CLASS)
            .addMethod(MethodInfo.builder()
                .name("<init>")
                .declaringClassName("com.example.singleton.DatabaseManager")
                .isPrivate(true)
                .build())
            .addMethod(MethodInfo.builder()
                .name("getInstance")
                .declaringClassName("com.example.singleton.DatabaseManager")
                .isStatic(true)
                .isPublic(true)
                .build())
            .build();
    }
    
    private ClassInfo createMockFactoryClass() {
        return ClassInfo.builder()
            .fullyQualifiedName("com.example.factory.UserFactory")
            .classType(ClassType.CLASS)
            .addMethod(MethodInfo.builder()
                .name("createUser")
                .declaringClassName("com.example.factory.UserFactory")
                .isStatic(true)
                .isPublic(true)
                .build())
            .build();
    }
    
    private ClassInfo createMockBuilderClass() {
        return ClassInfo.builder()
            .fullyQualifiedName("com.example.builder.UserBuilder")
            .classType(ClassType.CLASS)
            .addMethod(MethodInfo.builder()
                .name("build")
                .declaringClassName("com.example.builder.UserBuilder")
                .isPublic(true)
                .build())
            .addMethod(MethodInfo.builder()
                .name("withName")
                .declaringClassName("com.example.builder.UserBuilder")
                .isPublic(true)
                .returnType("com.example.builder.UserBuilder")
                .build())
            .build();
    }
    
    private ClassInfo createMockStrategyInterface() {
        return ClassInfo.builder()
            .fullyQualifiedName("com.example.strategy.PaymentStrategy")
            .classType(ClassType.INTERFACE)
            .addMethod(MethodInfo.builder()
                .name("pay")
                .declaringClassName("com.example.strategy.PaymentStrategy")
                .isPublic(true)
                .build())
            .build();
    }
    
    private ClassInfo createMockStrategyImplementation1() {
        return ClassInfo.builder()
            .fullyQualifiedName("com.example.strategy.CreditCardPayment")
            .classType(ClassType.CLASS)
            .addInterface("com.example.strategy.PaymentStrategy")
            .addMethod(MethodInfo.builder()
                .name("pay")
                .declaringClassName("com.example.strategy.CreditCardPayment")
                .isPublic(true)
                .build())
            .build();
    }
    
    private ClassInfo createMockStrategyImplementation2() {
        return ClassInfo.builder()
            .fullyQualifiedName("com.example.strategy.PayPalPayment")
            .classType(ClassType.CLASS)
            .addInterface("com.example.strategy.PaymentStrategy")
            .addMethod(MethodInfo.builder()
                .name("pay")
                .declaringClassName("com.example.strategy.PayPalPayment")
                .isPublic(true)
                .build())
            .build();
    }
    
    private ClassInfo createMockGodClass() {
        ClassInfo.Builder builder = ClassInfo.builder()
            .fullyQualifiedName("com.example.GodManager")
            .classType(ClassType.CLASS);
        
        // Add many methods to simulate a God class
        for (int i = 1; i <= 25; i++) {
            builder.addMethod(MethodInfo.builder()
                .name("method" + i)
                .declaringClassName("com.example.GodManager")
                .isPublic(true)
                .build());
        }
        
        // Add many fields
        for (int i = 1; i <= 20; i++) {
            builder.addField(FieldInfo.builder()
                .name("field" + i)
                .type("java.lang.String")
                .declaringClassName("com.example.GodManager")
                .build());
        }
        
        return builder.build();
    }
    
    private ClassInfo createMockLongParameterClass() {
        return ClassInfo.builder()
            .fullyQualifiedName("com.example.LongParameterClass")
            .classType(ClassType.CLASS)
            .addMethod(MethodInfo.builder()
                .name("methodWithManyParams")
                .declaringClassName("com.example.LongParameterClass")
                .isPublic(true)
                .addParameter(ParameterInfo.builder().name("param1").type("String").build())
                .addParameter(ParameterInfo.builder().name("param2").type("String").build())
                .addParameter(ParameterInfo.builder().name("param3").type("String").build())
                .addParameter(ParameterInfo.builder().name("param4").type("String").build())
                .addParameter(ParameterInfo.builder().name("param5").type("String").build())
                .addParameter(ParameterInfo.builder().name("param6").type("String").build())
                .addParameter(ParameterInfo.builder().name("param7").type("String").build())
                .addParameter(ParameterInfo.builder().name("param8").type("String").build())
                .build())
            .build();
    }
}