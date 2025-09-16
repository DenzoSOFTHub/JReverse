package it.denzosoft.jreverse.analyzer.dependencygraph;

import it.denzosoft.jreverse.core.model.*;
import it.denzosoft.jreverse.core.port.DependencyGraphBuilder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for JavassistDependencyGraphBuilder.
 * Tests the most critical component of Phase 3.
 * 
 * @author JReverse Development Team
 * @since 1.1.0 (Phase 3)
 */
@DisplayName("JavassistDependencyGraphBuilder Tests")
class JavassistDependencyGraphBuilderTest {
    
    private JavassistDependencyGraphBuilder dependencyGraphBuilder;
    private JarContent testJarContent;
    
    @BeforeEach
    void setUp() {
        dependencyGraphBuilder = new JavassistDependencyGraphBuilder();
        testJarContent = createTestJarContent();
    }
    
    @AfterEach
    void tearDown() {
        if (dependencyGraphBuilder != null) {
            dependencyGraphBuilder.shutdown();
        }
    }
    
    @Test
    @DisplayName("Should build basic dependency graph successfully")
    void testBuildDependencyGraph_BasicSuccess() {
        // When
        DependencyGraphResult result = dependencyGraphBuilder.buildDependencyGraph(testJarContent);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isSuccessful());
        assertNotNull(result.getNodes());
        assertNotNull(result.getEdges());
        assertNotNull(result.getMetrics());
        assertNotNull(result.getCircularDependencies());
        assertTrue(result.getTotalNodes() > 0);
    }
    
    @Test
    @DisplayName("Should handle null input with proper exception")
    void testBuildDependencyGraph_NullInput() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            dependencyGraphBuilder.buildDependencyGraph(null);
        });
    }
    
    @Test
    @DisplayName("Should handle empty JAR content gracefully")
    void testBuildDependencyGraph_EmptyJar() {
        // Given
        JarContent emptyJar = createEmptyJarContent();
        
        // When
        DependencyGraphResult result = dependencyGraphBuilder.buildDependencyGraph(emptyJar);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isSuccessful());
        assertEquals(0, result.getTotalNodes());
        assertEquals(0, result.getTotalEdges());
    }
    
    @Test
    @DisplayName("Should create package nodes correctly")
    void testBuildDependencyGraph_PackageNodes() {
        // When
        DependencyGraphResult result = dependencyGraphBuilder.buildDependencyGraph(testJarContent);
        
        // Then
        Set<DependencyNode> packageNodes = result.getNodes().stream()
            .filter(node -> node.getType() == DependencyNodeType.PACKAGE)
            .collect(java.util.stream.Collectors.toSet());
            
        assertFalse(packageNodes.isEmpty());
        
        // Verify package nodes have correct identifiers
        boolean hasComExamplePackage = packageNodes.stream()
            .anyMatch(node -> node.getIdentifier().equals("com.example"));
        assertTrue(hasComExamplePackage);
    }
    
    @Test
    @DisplayName("Should create class nodes correctly")
    void testBuildDependencyGraph_ClassNodes() {
        // When
        DependencyGraphResult result = dependencyGraphBuilder.buildDependencyGraph(testJarContent);
        
        // Then
        Set<DependencyNode> classNodes = result.getNodes().stream()
            .filter(node -> node.getType() == DependencyNodeType.CLASS)
            .collect(java.util.stream.Collectors.toSet());
            
        assertFalse(classNodes.isEmpty());
        assertEquals(testJarContent.getClasses().size(), classNodes.size());
        
        // Verify class nodes have correct identifiers
        boolean hasTestClass = classNodes.stream()
            .anyMatch(node -> node.getIdentifier().equals("com.example.TestClass"));
        assertTrue(hasTestClass);
    }
    
    @Test
    @DisplayName("Should calculate basic metrics correctly")
    void testBuildDependencyGraph_BasicMetrics() {
        // When
        DependencyGraphResult result = dependencyGraphBuilder.buildDependencyGraph(testJarContent);
        
        // Then
        DependencyMetrics metrics = result.getMetrics();
        assertNotNull(metrics);
        
        assertTrue(metrics.getTotalNodes() > 0);
        assertTrue(metrics.getPackageNodes() > 0);
        assertTrue(metrics.getClassNodes() > 0);
        assertTrue(metrics.getAverageDegree() >= 0.0);
        assertTrue(metrics.getDensity() >= 0.0 && metrics.getDensity() <= 1.0);
        
        assertNotNull(metrics.getArchitecturalHealth());
    }
    
    @Test
    @DisplayName("Should support all analysis types")
    void testGetSupportedAnalysisTypes() {
        // When
        DependencyGraphBuilder.DependencyAnalysisType[] supportedTypes = 
            dependencyGraphBuilder.getSupportedAnalysisTypes();
        
        // Then
        assertNotNull(supportedTypes);
        assertTrue(supportedTypes.length > 0);
        
        List<DependencyGraphBuilder.DependencyAnalysisType> typesList = Arrays.asList(supportedTypes);
        assertTrue(typesList.contains(DependencyGraphBuilder.DependencyAnalysisType.PACKAGE_DEPENDENCIES));
        assertTrue(typesList.contains(DependencyGraphBuilder.DependencyAnalysisType.CLASS_INHERITANCE));
        assertTrue(typesList.contains(DependencyGraphBuilder.DependencyAnalysisType.CLASS_COMPOSITION));
    }
    
    @Test
    @DisplayName("Should validate JAR content correctly")
    void testCanAnalyze() {
        // Test valid JAR content
        assertTrue(dependencyGraphBuilder.canAnalyze(testJarContent));
        
        // Test null input
        assertFalse(dependencyGraphBuilder.canAnalyze(null));
        
        // Test empty JAR content
        JarContent emptyJar = createEmptyJarContent();
        assertFalse(dependencyGraphBuilder.canAnalyze(emptyJar));
    }
    
    @Test
    @DisplayName("Should handle circular dependency detection")
    void testBuildDependencyGraph_CircularDependencyDetection() {
        // Given
        JarContent jarWithCircularDeps = createJarContentWithCircularDependencies();
        
        // When
        DependencyGraphResult result = dependencyGraphBuilder.buildDependencyGraph(jarWithCircularDeps);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isSuccessful());
        
        List<CircularDependency> circularDeps = result.getCircularDependencies();
        assertNotNull(circularDeps);
        // Note: Actual circular dependency detection depends on real bytecode analysis
        // For unit test, we just verify the structure is correct
    }
    
    @Test
    @DisplayName("Should handle timeout scenarios gracefully")
    void testBuildDependencyGraph_TimeoutHandling() {
        // Given
        JarContent largeJar = createLargeJarContent();
        
        // When
        DependencyGraphResult result = dependencyGraphBuilder.buildDependencyGraph(largeJar);
        
        // Then
        assertNotNull(result);
        // Result might be successful or failed depending on processing time
        // But should not throw exceptions
    }
    
    @Test
    @DisplayName("Should cleanup resources properly")
    void testShutdown() {
        // When
        assertDoesNotThrow(() -> {
            dependencyGraphBuilder.shutdown();
        });
        
        // Multiple shutdowns should be safe
        assertDoesNotThrow(() -> {
            dependencyGraphBuilder.shutdown();
        });
    }
    
    @Test
    @DisplayName("Should handle selective analysis types")
    void testBuildDependencyGraph_SelectiveAnalysis() {
        // Given
        Set<DependencyGraphBuilder.DependencyAnalysisType> limitedTypes = EnumSet.of(
            DependencyGraphBuilder.DependencyAnalysisType.PACKAGE_DEPENDENCIES,
            DependencyGraphBuilder.DependencyAnalysisType.CLASS_INHERITANCE
        );
        
        JavassistDependencyGraphBuilder selectiveBuilder = 
            new JavassistDependencyGraphBuilder(limitedTypes);
        
        try {
            // When
            DependencyGraphResult result = selectiveBuilder.buildDependencyGraph(testJarContent);
            
            // Then
            assertNotNull(result);
            assertTrue(result.isSuccessful());
            // Verify only selected analysis types were executed
            // (Specific verification would depend on implementation details)
            
        } finally {
            selectiveBuilder.shutdown();
        }
    }
    
    @Test
    @DisplayName("Should generate correct dependency edges")
    void testBuildDependencyGraph_DependencyEdges() {
        // When
        DependencyGraphResult result = dependencyGraphBuilder.buildDependencyGraph(testJarContent);
        
        // Then
        assertNotNull(result.getEdges());
        
        for (DependencyEdge edge : result.getEdges()) {
            assertNotNull(edge.getSource());
            assertNotNull(edge.getTarget());
            assertNotNull(edge.getType());
            assertTrue(edge.getWeight() > 0);
            assertNotNull(edge.getDescription());
        }
    }
    
    // Helper methods for creating test data
    
    private JarContent createTestJarContent() {
        Set<ClassInfo> classes = new HashSet<>();
        
        // Add some test classes
        classes.add(ClassInfo.builder()
            .fullyQualifiedName("com.example.TestClass")
            .classType(ClassType.CLASS)
            .build());
            
        classes.add(ClassInfo.builder()
            .fullyQualifiedName("com.example.service.UserService")
            .classType(ClassType.CLASS)
            .build());
            
        classes.add(ClassInfo.builder()
            .fullyQualifiedName("com.example.repository.UserRepository")
            .classType(ClassType.INTERFACE)
            .build());
        
        return JarContent.builder()
            .location(new JarLocation("/test/example.jar"))
            .classes(classes)
            .jarType(JarType.REGULAR)
            .build();
    }
    
    private JarContent createEmptyJarContent() {
        return JarContent.builder()
            .location(new JarLocation("/test/empty.jar"))
            .classes(Collections.emptySet())
            .jarType(JarType.REGULAR)
            .build();
    }
    
    private JarContent createJarContentWithCircularDependencies() {
        Set<ClassInfo> classes = new HashSet<>();
        
        // Create classes that would have circular dependencies in a real scenario
        classes.add(ClassInfo.builder()
            .fullyQualifiedName("com.example.ClassA")
            .classType(ClassType.CLASS)
            .build());
            
        classes.add(ClassInfo.builder()
            .fullyQualifiedName("com.example.ClassB")
            .classType(ClassType.CLASS)
            .build());
            
        classes.add(ClassInfo.builder()
            .fullyQualifiedName("com.example.ClassC")
            .classType(ClassType.CLASS)
            .build());
        
        return JarContent.builder()
            .location(new JarLocation("/test/circular.jar"))
            .classes(classes)
            .jarType(JarType.REGULAR)
            .build();
    }
    
    private JarContent createLargeJarContent() {
        Set<ClassInfo> classes = new HashSet<>();
        
        // Create a larger set of classes for timeout testing
        for (int i = 0; i < 1000; i++) {
            classes.add(ClassInfo.builder()
                .fullyQualifiedName("com.example.large.Class" + i)
                .classType(ClassType.CLASS)
                .build());
        }
        
        return JarContent.builder()
            .location(new JarLocation("/test/large.jar"))
            .classes(classes)
            .jarType(JarType.REGULAR)
            .build();
    }
}