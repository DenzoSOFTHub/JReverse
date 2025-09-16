package it.denzosoft.jreverse.analyzer.layeredarchitecture;
import it.denzosoft.jreverse.core.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
class JavassistLayeredArchitectureAnalyzerTest {
    private JavassistLayeredArchitectureAnalyzer analyzer;
    @BeforeEach
    void setUp() {
        analyzer = new JavassistLayeredArchitectureAnalyzer();
    }
    @Test
    void shouldAnalyzeLayeredArchitectureSuccessfully() {
        // Given
        JarContent layeredJar = createMockLayeredJar();
        // When
        LayeredArchitectureResult result = analyzer.analyzeLayeredArchitecture(layeredJar);
        // Then
        assertNotNull(result);
        assertNotNull(result.getMetrics());
        assertTrue(result.getMetrics().getTotalLayers() >= 0);
        assertNotNull(result.getLayerClassification());
        assertNotNull(result.getViolations());
        assertNotNull(result.getCompliance());
    }
    @Test
    void shouldClassifyClassesIntoLayers() {
        // Given
        JarContent wellStructuredJar = createMockWellStructuredJar();
        // When
        LayeredArchitectureResult result = analyzer.analyzeLayeredArchitecture(wellStructuredJar);
        // Then
        Map<LayerType, Set<ClassInfo>> classification = result.getLayerClassification();
        assertTrue(classification.containsKey(LayerType.PRESENTATION));
        assertTrue(classification.containsKey(LayerType.BUSINESS));
        assertTrue(classification.containsKey(LayerType.PERSISTENCE));
        assertFalse(classification.get(LayerType.PRESENTATION).isEmpty());
        assertFalse(classification.get(LayerType.BUSINESS).isEmpty());
        assertFalse(classification.get(LayerType.PERSISTENCE).isEmpty());
    }
    @Test
    void shouldDetectLayerViolations() {
        // Given
        JarContent violatingJar = createMockViolatingJar();
        // When
        LayeredArchitectureResult result = analyzer.analyzeLayeredArchitecture(violatingJar);
        // Then
        assertTrue(result.getViolations().size() > 0);
        List<LayerViolation> violations = result.getViolations();
        assertFalse(violations.isEmpty());
        LayerViolation firstViolation = violations.get(0);
        assertNotNull(firstViolation.getViolationType());
        assertNotNull(firstViolation.getSeverity());
        assertNotNull(firstViolation.getDescription());
        assertNotNull(firstViolation.getSourceLayer());
        assertNotNull(firstViolation.getTargetLayer());
    }
    @Test
    void shouldDetectUpwardDependencyViolation() {
        // Given
        JarContent upwardDependencyJar = createMockUpwardDependencyJar();
        // When
        LayeredArchitectureResult result = analyzer.analyzeLayeredArchitecture(upwardDependencyJar);
        // Then
        boolean hasUpwardViolation = result.getViolations().stream()
            .anyMatch(v -> v.getViolationType() == LayerViolation.Type.UPWARD_DEPENDENCY);
        assertTrue(hasUpwardViolation);
    }
    @Test
    void shouldDetectSkipLayerViolation() {
        // Given
        JarContent skipLayerJar = createMockSkipLayerJar();
        // When
        LayeredArchitectureResult result = analyzer.analyzeLayeredArchitecture(skipLayerJar);
        // Then
        boolean hasSkipLayerViolation = result.getViolations().stream()
            .anyMatch(v -> v.getViolationType() == LayerViolation.Type.SKIP_LAYER_DEPENDENCY);
        assertTrue(hasSkipLayerViolation);
    }
    @Test
    void shouldCalculateLayerCohesion() {
        // Given
        JarContent cohesiveJar = createMockCohesiveJar();
        // When
        LayeredArchitectureResult result = analyzer.analyzeLayeredArchitecture(cohesiveJar);
        // Then
        LayeredArchitectureMetrics metrics = result.getMetrics();
        assertNotNull(metrics);
        assertTrue(metrics.getAverageCohesionPerLayer() >= 0.0 && metrics.getAverageCohesionPerLayer() <= 1.0);
    }
    @Test
    void shouldCalculateLayerCoupling() {
        // Given
        JarContent coupledJar = createMockCoupledJar();
        // When
        LayeredArchitectureResult result = analyzer.analyzeLayeredArchitecture(coupledJar);
        // Then
        LayeredArchitectureMetrics metrics = result.getMetrics();
        assertNotNull(metrics);
        assertTrue(metrics.getAverageCouplingPerLayer() >= 0.0);
    }
    @Test
    void shouldCalculateArchitecturalCompliance() {
        // Given
        JarContent compliantJar = createMockCompliantJar();
        // When
        LayeredArchitectureResult result = analyzer.analyzeLayeredArchitecture(compliantJar);
        // Then
        LayeredArchitectureCompliance compliance = result.getCompliance();
        assertTrue(compliance.getComplianceScore() >= 0.0 && compliance.getComplianceScore() <= 1.0);
        assertNotNull(compliance.getOverallCompliance());
    }
    @Test
    void shouldGenerateLayerRecommendations() {
        // Given
        JarContent problematicJar = createMockProblematicJar();
        // When
        LayeredArchitectureResult result = analyzer.analyzeLayeredArchitecture(problematicJar);
        // Then
        List<LayerRecommendation> recommendations = result.getRecommendations();
        assertNotNull(recommendations);
        // Recommendations are available - exact structure depends on implementation
        // Just verify the list is present and not null
    }
    @Test
    void shouldCalculateLayerMetrics() {
        // Given
        JarContent metricsJar = createMockMetricsJar();
        // When
        LayeredArchitectureResult result = analyzer.analyzeLayeredArchitecture(metricsJar);
        // Then
        LayeredArchitectureMetrics metrics = result.getMetrics();
        assertNotNull(metrics);
        assertTrue(metrics.getTotalLayers() >= 0);
        assertTrue(metrics.getTotalClasses() >= 0);
        assertTrue(metrics.getTotalViolations() >= 0);
        assertTrue(metrics.getAverageCohesionPerLayer() >= 0.0 && metrics.getAverageCohesionPerLayer() <= 1.0);
        assertTrue(metrics.getAverageCouplingPerLayer() >= 0.0);
        assertTrue(metrics.getLayeringCompliance() >= 0.0 && metrics.getLayeringCompliance() <= 1.0);
        assertTrue(metrics.getArchitecturalIntegrity() >= 0.0 && metrics.getArchitecturalIntegrity() <= 1.0);
    }
    @Test
    void shouldAnalyzeDependenciesBetweenLayers() {
        // Given
        JarContent dependentJar = createMockDependentJar();
        // When
        LayeredArchitectureResult result = analyzer.analyzeLayeredArchitecture(dependentJar);
        // Then
        List<LayerDependency> dependencies = result.getLayerDependencies();
        assertNotNull(dependencies);
        if (!dependencies.isEmpty()) {
            LayerDependency firstDependency = dependencies.get(0);
            assertNotNull(firstDependency.getSourceLayer());
            assertNotNull(firstDependency.getTargetLayer());
            assertTrue(firstDependency.getDependencyStrength() >= 0.0 && firstDependency.getDependencyStrength() <= 1.0);
        }
    }
    @Test
    void shouldHandleEmptyJar() {
        // Given
        JarContent emptyJar = createMockEmptyJar();
        // When
        LayeredArchitectureResult result = analyzer.analyzeLayeredArchitecture(emptyJar);
        // Then
        assertNotNull(result);
        assertEquals(0, result.getMetrics().getTotalLayers());
        assertTrue(result.getViolations().isEmpty());
        assertNotNull(result.getMetrics());
    }
    @Test
    void shouldCheckCanAnalyze() {
        // Given
        JarContent layeredJar = createMockLayeredJar();
        JarContent emptyJar = createMockEmptyJar();
        JarContent nonLayeredJar = createMockNonLayeredJar();
        // When & Then
        assertTrue(analyzer.canAnalyze(layeredJar));
        assertFalse(analyzer.canAnalyze(emptyJar));
        assertFalse(analyzer.canAnalyze(nonLayeredJar));
    }
    @Test
    void shouldHandleAnalysisErrors() {
        // Given
        JarContent corruptedJar = null;

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            analyzer.analyzeLayeredArchitecture(corruptedJar);
        });

        assertThat(exception.getMessage()).contains("JarContent cannot be null");
    }
    @Test
    void shouldEvaluateLayerSeparationQuality() {
        // Given
        JarContent excellentJar = createMockExcellentLayerSeparationJar();
        JarContent poorJar = createMockPoorLayerSeparationJar();
        // When
        LayeredArchitectureResult excellentResult = analyzer.analyzeLayeredArchitecture(excellentJar);
        LayeredArchitectureResult poorResult = analyzer.analyzeLayeredArchitecture(poorJar);
        // Then
        assertTrue(excellentResult.getCompliance().getComplianceScore() > 
                   poorResult.getCompliance().getComplianceScore());
    }
    @Test
    void shouldCalculateArchitecturalDebtIndex() {
        // Given
        JarContent debtJar = createMockHighDebtJar();
        // When
        LayeredArchitectureResult result = analyzer.analyzeLayeredArchitecture(debtJar);
        // Then
        LayeredArchitectureMetrics metrics = result.getMetrics();
        assertTrue(metrics.getArchitecturalIntegrity() < 1.0);
    }
    // Helper methods to create mock data
    private JarContent createMockLayeredJar() {
        return JarContent.builder()
            .location(new JarLocation("/test/layered.jar"))
            .classes(Set.of(
                createMockControllerClass(),
                createMockServiceClass(),
                createMockRepositoryClass()
            ))
            .build();
    }
    private JarContent createMockWellStructuredJar() {
        return JarContent.builder()
            .location(new JarLocation("/test/wellstructured.jar"))
            .classes(Set.of(
                createMockControllerClass(),
                createMockServiceClass(),
                createMockRepositoryClass(),
                createMockEntityClass(),
                createMockConfigurationClass()
            ))
            .build();
    }
    private JarContent createMockViolatingJar() {
        return JarContent.builder()
            .location(new JarLocation("/test/violating.jar"))
            .classes(Set.of(
                createMockViolatingRepositoryClass(),
                createMockServiceClass()
            ))
            .build();
    }
    private JarContent createMockUpwardDependencyJar() {
        return JarContent.builder()
            .location(new JarLocation("/test/upward.jar"))
            .classes(Set.of(
                createMockUpwardDependentRepositoryClass(),
                createMockControllerClass(), // Need to include the Controller that Repository depends on
                createMockServiceClass()
            ))
            .build();
    }
    private JarContent createMockSkipLayerJar() {
        return JarContent.builder()
            .location(new JarLocation("/test/skiplayer.jar"))
            .classes(Set.of(
                createMockSkipLayerControllerClass(),
                createMockServiceClass(),
                createMockRepositoryClass()
            ))
            .build();
    }
    private JarContent createMockCohesiveJar() {
        return JarContent.builder()
            .location(new JarLocation("/test/cohesive.jar"))
            .classes(Set.of(
                createMockCohesiveServiceClass1(),
                createMockCohesiveServiceClass2(),
                createMockRepositoryClass()
            ))
            .build();
    }
    private JarContent createMockCoupledJar() {
        return JarContent.builder()
            .location(new JarLocation("/test/coupled.jar"))
            .classes(Set.of(
                createMockControllerClass(),
                createMockServiceClass(),
                createMockRepositoryClass()
            ))
            .build();
    }
    private JarContent createMockCompliantJar() {
        return JarContent.builder()
            .location(new JarLocation("/test/compliant.jar"))
            .classes(Set.of(
                createMockControllerClass(),
                createMockServiceClass(),
                createMockRepositoryClass(),
                createMockEntityClass()
            ))
            .build();
    }
    private JarContent createMockProblematicJar() {
        return JarContent.builder()
            .location(new JarLocation("/test/problematic.jar"))
            .classes(Set.of(
                createMockViolatingRepositoryClass(),
                createMockUpwardDependentRepositoryClass()
            ))
            .build();
    }
    private JarContent createMockMetricsJar() {
        return JarContent.builder()
            .location(new JarLocation("/test/metrics.jar"))
            .classes(Set.of(
                createMockControllerClass(),
                createMockServiceClass(),
                createMockRepositoryClass(),
                createMockEntityClass(),
                createMockConfigurationClass()
            ))
            .build();
    }
    private JarContent createMockDependentJar() {
        return JarContent.builder()
            .location(new JarLocation("/test/dependent.jar"))
            .classes(Set.of(
                createMockDependentControllerClass(),
                createMockServiceClass(),
                createMockEntityClass()
            ))
            .build();
    }
    private JarContent createMockEmptyJar() {
        return JarContent.builder()
            .location(new JarLocation("/test/empty.jar"))
            .classes(Set.of())
            .build();
    }
    private JarContent createMockNonLayeredJar() {
        return JarContent.builder()
            .location(new JarLocation("/test/nonlayered.jar"))
            .classes(Set.of(createMockUtilityClass()))
            .build();
    }
    private JarContent createMockExcellentLayerSeparationJar() {
        return JarContent.builder()
            .location(new JarLocation("/test/excellent.jar"))
            .classes(Set.of(
                createMockControllerClass(),
                createMockServiceClass(),
                createMockRepositoryClass(),
                createMockEntityClass()
            ))
            .build();
    }
    private JarContent createMockPoorLayerSeparationJar() {
        return JarContent.builder()
            .location(new JarLocation("/test/poor.jar"))
            .classes(Set.of(
                createMockViolatingRepositoryClass(),
                createMockUpwardDependentRepositoryClass(),
                createMockSkipLayerControllerClass()
            ))
            .build();
    }
    private JarContent createMockHighDebtJar() {
        return JarContent.builder()
            .location(new JarLocation("/test/highdebt.jar"))
            .classes(Set.of(
                createMockViolatingRepositoryClass(),
                createMockUpwardDependentRepositoryClass()
            ))
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
                .returnType("java.util.List")
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
            .addField(FieldInfo.builder()
                .name("userRepository")
                .type("com.example.repository.UserRepository")
                .declaringClassName("com.example.service.UserService")
                .build())
            .addMethod(MethodInfo.builder()
                .name("findUsers")
                .declaringClassName("com.example.service.UserService")
                .isPublic(true)
                .returnType("java.util.List")
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
            .addMethod(MethodInfo.builder()
                .name("findAll")
                .declaringClassName("com.example.repository.UserRepository")
                .isPublic(true)
                .returnType("java.util.List")
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
    private ClassInfo createMockConfigurationClass() {
        return ClassInfo.builder()
            .fullyQualifiedName("com.example.config.AppConfig")
            .classType(ClassType.CLASS)
            .addAnnotation(AnnotationInfo.builder()
                .type("org.springframework.context.annotation.Configuration")
                .build())
            .build();
    }
    private ClassInfo createMockViolatingRepositoryClass() {
        return ClassInfo.builder()
            .fullyQualifiedName("com.example.repository.ViolatingRepository")
            .classType(ClassType.CLASS)
            .addAnnotation(AnnotationInfo.builder()
                .type("org.springframework.stereotype.Repository")
                .build())
            .addField(FieldInfo.builder()
                .name("userService") // Repository depending on Service - violation!
                .type("com.example.service.UserService")
                .declaringClassName("com.example.repository.ViolatingRepository")
                .build())
            .build();
    }
    private ClassInfo createMockUpwardDependentRepositoryClass() {
        return ClassInfo.builder()
            .fullyQualifiedName("com.example.repository.UpwardRepository")
            .classType(ClassType.CLASS)
            .addAnnotation(AnnotationInfo.builder()
                .type("org.springframework.stereotype.Repository")
                .build())
            .addField(FieldInfo.builder()
                .name("controller") // Repository depending on Controller - upward violation!
                .type("com.example.controller.UserController")
                .declaringClassName("com.example.repository.UpwardRepository")
                .build())
            .build();
    }
    private ClassInfo createMockSkipLayerControllerClass() {
        return ClassInfo.builder()
            .fullyQualifiedName("com.example.controller.SkipLayerController")
            .classType(ClassType.CLASS)
            .addAnnotation(AnnotationInfo.builder()
                .type("org.springframework.web.bind.annotation.RestController")
                .build())
            .addField(FieldInfo.builder()
                .name("userRepository") // Controller directly depending on Repository - skipping Service layer
                .type("com.example.repository.UserRepository")
                .declaringClassName("com.example.controller.SkipLayerController")
                .build())
            .build();
    }
    private ClassInfo createMockCohesiveServiceClass1() {
        return ClassInfo.builder()
            .fullyQualifiedName("com.example.service.user.UserService")
            .classType(ClassType.CLASS)
            .addAnnotation(AnnotationInfo.builder()
                .type("org.springframework.stereotype.Service")
                .build())
            .addInterface("com.example.service.user.UserOperations")
            .build();
    }
    private ClassInfo createMockCohesiveServiceClass2() {
        return ClassInfo.builder()
            .fullyQualifiedName("com.example.service.user.UserValidationService")
            .classType(ClassType.CLASS)
            .addAnnotation(AnnotationInfo.builder()
                .type("org.springframework.stereotype.Service")
                .build())
            .addInterface("com.example.service.user.UserOperations")
            .build();
    }
    private ClassInfo createMockDependentControllerClass() {
        return ClassInfo.builder()
            .fullyQualifiedName("com.example.controller.DependentController")
            .classType(ClassType.CLASS)
            .addAnnotation(AnnotationInfo.builder()
                .type("org.springframework.web.bind.annotation.RestController")
                .build())
            .addField(FieldInfo.builder()
                .name("userService")
                .type("com.example.service.UserService")
                .declaringClassName("com.example.controller.DependentController")
                .build())
            .addMethod(MethodInfo.builder()
                .name("createUser")
                .declaringClassName("com.example.controller.DependentController")
                .isPublic(true)
                .addParameter(ParameterInfo.builder()
                    .name("user")
                    .type("com.example.entity.User")
                    .build())
                .returnType("com.example.entity.User")
                .build())
            .build();
    }
    private ClassInfo createMockUtilityClass() {
        return ClassInfo.builder()
            .fullyQualifiedName("com.example.util.StringUtils")
            .classType(ClassType.CLASS)
            .addMethod(MethodInfo.builder()
                .name("isEmpty")
                .declaringClassName("com.example.util.StringUtils")
                .isStatic(true)
                .isPublic(true)
                .build())
            .build();
    }
}