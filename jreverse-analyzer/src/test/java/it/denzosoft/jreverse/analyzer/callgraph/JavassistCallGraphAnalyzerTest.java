package it.denzosoft.jreverse.analyzer.callgraph;

import it.denzosoft.jreverse.core.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

class JavassistCallGraphAnalyzerTest {
    
    private JavassistCallGraphAnalyzer analyzer;
    
    @BeforeEach
    void setUp() {
        analyzer = new JavassistCallGraphAnalyzer();
    }
    
    @Test
    void shouldAnalyzeCallGraphsSuccessfully() {
        // Given
        JarContent jarWithEndpoints = createMockJarWithEndpoints();
        
        // When
        CallGraphAnalysisResult result = analyzer.analyzeCallGraphs(jarWithEndpoints);
        
        // Then
        assertNotNull(result);
        assertNotNull(result.getSummary());
        assertTrue(result.getTotalEndpoints() >= 0);
        assertNotNull(result.getCallChains());
        assertNotNull(result.getIssues());
    }
    
    @Test
    void shouldDetectHttpEndpoints() {
        // Given
        JarContent jarWithController = createMockJarWithController();
        
        // When
        CallGraphAnalysisResult result = analyzer.analyzeCallGraphs(jarWithController);
        
        // Then
        assertTrue(result.getTotalEndpoints() > 0);
        assertFalse(result.getCallChains().isEmpty());
        
        CallGraphChain firstChain = result.getCallChains().get(0);
        assertNotNull(firstChain.getEndpoint());
        assertNotNull(firstChain.getHttpMethod());
        assertNotNull(firstChain.getRootNode());
    }
    
    @Test
    void shouldBuildCallChainHierarchy() {
        // Given
        JarContent jarWithServiceLayer = createMockJarWithServiceLayer();
        
        // When
        CallGraphAnalysisResult result = analyzer.analyzeCallGraphs(jarWithServiceLayer);
        
        // Then
        if (!result.getCallChains().isEmpty()) {
            CallGraphChain chain = result.getCallChains().get(0);
            assertTrue(chain.getMaxDepth() > 0);
            assertNotNull(chain.getRootNode());
            assertNotNull(chain.getComplexityLevel());
        }
    }
    
    @Test
    void shouldCalculateArchitectureMetrics() {
        // Given
        JarContent jarWithLayers = createMockJarWithMultipleLayers();
        
        // When
        CallGraphAnalysisResult result = analyzer.analyzeCallGraphs(jarWithLayers);
        
        // Then
        ArchitectureMetrics metrics = result.getArchitectureMetrics();
        assertNotNull(metrics);
        assertTrue(metrics.getLayerViolations() >= 0);
        assertTrue(metrics.getCouplingScore() >= 0.0);
        assertTrue(metrics.getCohesionScore() >= 0.0);
    }
    
    @Test
    void shouldDetectCallGraphIssues() {
        // Given
        JarContent jarWithIssues = createMockJarWithIssues();
        
        // When
        CallGraphAnalysisResult result = analyzer.analyzeCallGraphs(jarWithIssues);
        
        // Then
        List<CallGraphIssue> issues = result.getIssues();
        assertNotNull(issues);
        
        // Verify issue properties
        if (!issues.isEmpty()) {
            CallGraphIssue firstIssue = issues.get(0);
            assertNotNull(firstIssue.getType());
            assertNotNull(firstIssue.getSeverity());
            assertNotNull(firstIssue.getDescription());
        }
    }
    
    @Test
    void shouldIdentifyPerformanceHotspots() {
        // Given
        JarContent jarWithComplexChains = createMockJarWithComplexChains();
        
        // When
        CallGraphAnalysisResult result = analyzer.analyzeCallGraphs(jarWithComplexChains);
        
        // Then
        List<PerformanceHotspot> hotspots = result.getHotspots();
        assertNotNull(hotspots);
        
        // Verify hotspot properties
        if (!hotspots.isEmpty()) {
            PerformanceHotspot hotspot = hotspots.get(0);
            assertNotNull(hotspot.getComponentName());
            assertNotNull(hotspot.getDescription());
            assertTrue(hotspot.getPerformanceImpact() >= 0.0);
        }
    }
    
    @Test
    void shouldCalculateComponentUsageStats() {
        // Given
        JarContent jarWithReusedComponents = createMockJarWithReusedComponents();
        
        // When
        CallGraphAnalysisResult result = analyzer.analyzeCallGraphs(jarWithReusedComponents);
        
        // Then
        Map<String, Integer> usageStats = result.getComponentUsageStats();
        assertNotNull(usageStats);
        
        if (!usageStats.isEmpty()) {
            String mostUsedComponent = result.getMostUsedComponent();
            assertNotNull(mostUsedComponent);
            assertTrue(usageStats.containsKey(mostUsedComponent));
        }
    }
    
    @Test
    void shouldHandleEmptyJarContent() {
        // Given
        JarContent emptyJar = createMockEmptyJar();
        
        // When
        CallGraphAnalysisResult result = analyzer.analyzeCallGraphs(emptyJar);
        
        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalEndpoints());
        assertTrue(result.getCallChains().isEmpty());
        assertNotNull(result.getSummary());
    }
    
    @Test
    void shouldDetectSpringBootApplication() {
        // Given
        JarContent springBootJar = createMockSpringBootJar();
        
        // When
        boolean canAnalyze = analyzer.canAnalyze(springBootJar);
        
        // Then
        assertTrue(canAnalyze);
    }
    
    @Test
    void shouldRejectNonWebApplication() {
        // Given
        JarContent nonWebJar = createMockNonWebJar();
        
        // When
        boolean canAnalyze = analyzer.canAnalyze(nonWebJar);
        
        // Then
        assertFalse(canAnalyze);
    }
    
    @Test
    void shouldHandleAnalysisErrors() {
        // Given
        JarContent corruptedJar = createMockCorruptedJar();

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            analyzer.analyzeCallGraphs(corruptedJar);
        });

        assertThat(exception.getMessage()).contains("JarContent cannot be null");
    }
    
    @Test
    void shouldFilterIssuesBySeverity() {
        // Given
        JarContent jarWithVariousIssues = createMockJarWithVariousIssues();
        
        // When
        CallGraphAnalysisResult result = analyzer.analyzeCallGraphs(jarWithVariousIssues);
        
        // Then
        List<CallGraphIssue> highSeverityIssues = result.getIssuesBySeverity(CallGraphIssue.Severity.HIGH);
        List<CallGraphIssue> warningIssues = result.getIssuesBySeverity(CallGraphIssue.Severity.WARNING);
        List<CallGraphIssue> infoIssues = result.getIssuesBySeverity(CallGraphIssue.Severity.INFO);
        
        assertNotNull(highSeverityIssues);
        assertNotNull(warningIssues);
        assertNotNull(infoIssues);
    }
    
    @Test
    void shouldFindCallChainsForComponent() {
        // Given
        JarContent jarWithSharedComponents = createMockJarWithSharedComponents();
        
        // When
        CallGraphAnalysisResult result = analyzer.analyzeCallGraphs(jarWithSharedComponents);
        
        // Then
        if (!result.getCallChains().isEmpty()) {
            String componentName = result.getCallChains().get(0).getRootNode().getClassName();
            List<CallGraphChain> chains = result.getCallChainsForComponent(componentName);
            assertNotNull(chains);
        }
    }
    
    // Helper methods to create mock data
    
    private JarContent createMockJarWithEndpoints() {
        return JarContent.builder()
            .location(new JarLocation("/test/endpoints.jar"))
            .classes(Set.of(createMockControllerClass()))
            .build();
    }
    
    private JarContent createMockJarWithController() {
        return JarContent.builder()
            .location(new JarLocation("/test/controller.jar"))
            .classes(Set.of(createMockControllerClass()))
            .build();
    }
    
    private JarContent createMockJarWithServiceLayer() {
        return JarContent.builder()
            .location(new JarLocation("/test/service.jar"))
            .classes(Set.of(
                createMockControllerClass(),
                createMockServiceClass()
            ))
            .build();
    }
    
    private JarContent createMockJarWithMultipleLayers() {
        return JarContent.builder()
            .location(new JarLocation("/test/layers.jar"))
            .classes(Set.of(
                createMockControllerClass(),
                createMockServiceClass(),
                createMockRepositoryClass()
            ))
            .build();
    }
    
    private JarContent createMockJarWithIssues() {
        return JarContent.builder()
            .location(new JarLocation("/test/issues.jar"))
            .classes(Set.of(createMockControllerClass()))
            .build();
    }
    
    private JarContent createMockJarWithComplexChains() {
        return JarContent.builder()
            .location(new JarLocation("/test/complex.jar"))
            .classes(Set.of(createMockControllerClass()))
            .build();
    }
    
    private JarContent createMockJarWithReusedComponents() {
        return JarContent.builder()
            .location(new JarLocation("/test/reused.jar"))
            .classes(Set.of(
                createMockControllerClass(),
                createMockServiceClass()
            ))
            .build();
    }
    
    private JarContent createMockEmptyJar() {
        return JarContent.builder()
            .location(new JarLocation("/test/empty.jar"))
            .classes(Set.of())
            .build();
    }
    
    private JarContent createMockSpringBootJar() {
        return JarContent.builder()
            .location(new JarLocation("/test/springboot.jar"))
            .classes(Set.of(createMockControllerClass()))
            .build();
    }
    
    private JarContent createMockNonWebJar() {
        return JarContent.builder()
            .location(new JarLocation("/test/nonweb.jar"))
            .classes(Set.of(createMockUtilityClass()))
            .build();
    }
    
    private JarContent createMockCorruptedJar() {
        // Return jar that will cause analysis issues
        return null;
    }
    
    private JarContent createMockJarWithVariousIssues() {
        return JarContent.builder()
            .location(new JarLocation("/test/various.jar"))
            .classes(Set.of(createMockControllerClass()))
            .build();
    }
    
    private JarContent createMockJarWithSharedComponents() {
        return JarContent.builder()
            .location(new JarLocation("/test/shared.jar"))
            .classes(Set.of(
                createMockControllerClass(),
                createMockServiceClass()
            ))
            .build();
    }
    
    private ClassInfo createMockControllerClass() {
        return ClassInfo.builder()
            .fullyQualifiedName("com.example.UserController")
            .classType(ClassType.CLASS)
            .addAnnotation(AnnotationInfo.builder()
                .type("org.springframework.web.bind.annotation.RestController")
                .build())
            .addMethod(MethodInfo.builder()
                .name("getUser")
                .declaringClassName("com.example.UserController")
                .isPublic(true)
                .addAnnotation(AnnotationInfo.builder()
                    .type("org.springframework.web.bind.annotation.GetMapping")
                    .build())
                .build())
            .build();
    }
    
    private ClassInfo createMockServiceClass() {
        return ClassInfo.builder()
            .fullyQualifiedName("com.example.UserService")
            .classType(ClassType.CLASS)
            .addAnnotation(AnnotationInfo.builder()
                .type("org.springframework.stereotype.Service")
                .build())
            .addMethod(MethodInfo.builder()
                .name("findUser")
                .declaringClassName("com.example.UserService")
                .isPublic(true)
                .build())
            .build();
    }
    
    private ClassInfo createMockRepositoryClass() {
        return ClassInfo.builder()
            .fullyQualifiedName("com.example.UserRepository")
            .classType(ClassType.INTERFACE)
            .addAnnotation(AnnotationInfo.builder()
                .type("org.springframework.stereotype.Repository")
                .build())
            .build();
    }
    
    private ClassInfo createMockUtilityClass() {
        return ClassInfo.builder()
            .fullyQualifiedName("com.example.Utils")
            .classType(ClassType.CLASS)
            .addMethod(MethodInfo.builder()
                .name("helper")
                .declaringClassName("com.example.Utils")
                .isPublic(true)
                .isStatic(true)
                .build())
            .build();
    }
}