package it.denzosoft.jreverse.analyzer.repository;

import it.denzosoft.jreverse.core.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JavassistRepositoryAnalyzer.
 */
class JavassistRepositoryAnalyzerTest {
    
    private JavassistRepositoryAnalyzer analyzer;
    
    @BeforeEach
    void setUp() {
        analyzer = new JavassistRepositoryAnalyzer();
    }
    
    @Test
    void testCanAnalyze_WithValidJarContent_ReturnsTrue() {
        JarContent jarContent = createJarContentWithClasses("com.example.TestClass");
        boolean result = analyzer.canAnalyze(jarContent);
        assertTrue(result);
    }
    
    @Test
    void testCanAnalyze_WithNullJarContent_ReturnsFalse() {
        boolean result = analyzer.canAnalyze(null);
        assertFalse(result);
    }
    
    @Test
    void testCanAnalyze_WithEmptyClasses_ReturnsFalse() {
        JarContent emptyJar = JarContent.builder()
            .location(new JarLocation(Paths.get("/test/empty.jar")))
            .jarType(JarType.REGULAR_JAR)
            .classes(Set.of())
            .build();
            
        boolean result = analyzer.canAnalyze(emptyJar);
        assertFalse(result);
    }
    
    @Test
    void testAnalyzeRepositories_WithEmptyJar_ReturnsEmptyResult() {
        JarContent emptyJar = JarContent.builder()
            .location(new JarLocation(Paths.get("/test/empty.jar")))
            .jarType(JarType.REGULAR_JAR)
            .classes(Set.of())
            .build();
        
        RepositoryAnalysisResult result = analyzer.analyzeRepositories(emptyJar);
        
        assertNotNull(result);
        assertNotNull(result.getRepositories());
        assertNotNull(result.getJpaRepositories());
        assertNotNull(result.getRepositoriesByPackage());
        assertNotNull(result.getMetrics());
        assertNotNull(result.getIssues());
        assertNotNull(result.getSummary());
        
        // Empty JAR should have no repositories
        assertEquals(0, result.getTotalRepositoryCount());
        assertTrue(result.getRepositories().isEmpty());
        assertTrue(result.getJpaRepositories().isEmpty());
        
        // Metrics should be zero
        assertEquals(0, result.getMetrics().getTotalRepositories());
        assertEquals(0, result.getMetrics().getJpaRepositories());
        assertEquals(0, result.getMetrics().getCustomRepositories());
    }
    
    @Test
    void testAnalyzeRepositories_WithNullJarContent_ThrowsException() {
        assertThrows(NullPointerException.class, () -> {
            analyzer.analyzeRepositories(null);
        });
    }
    
    @Test
    void testAnalyzeRepositories_WithNonExistentClasses_HandlesGracefully() {
        // Test with classes that don't exist in classpath - should handle gracefully
        JarContent jarContent = createJarContentWithClasses(
            "com.nonexistent.UserRepository", 
            "com.nonexistent.ProductDao"
        );
        
        RepositoryAnalysisResult result = analyzer.analyzeRepositories(jarContent);
        
        assertNotNull(result);
        assertNotNull(result.getRepositories());
        assertNotNull(result.getJpaRepositories());
        assertNotNull(result.getIssues());
        
        // Should have analysis error issues for classes that couldn't be loaded
        assertTrue(result.getIssues().size() >= 2, "Should have error issues for failed class analysis");
        
        // Should still return valid metrics even with errors
        assertNotNull(result.getMetrics());
        assertNotNull(result.getSummary());
        assertEquals(0, result.getTotalRepositoryCount()); // No repositories found due to loading failures
    }
    
    @Test
    void testAnalyzeRepositories_ErrorHandling_ReturnsErrorResult() {
        // Test error scenario by analyzing with problematic JAR content
        JarContent jarContent = createJarContentWithClasses("invalid.class.name.");
        
        RepositoryAnalysisResult result = analyzer.analyzeRepositories(jarContent);
        
        assertNotNull(result);
        assertNotNull(result.getIssues());
        
        // Should have at least one issue from failed analysis
        assertFalse(result.getIssues().isEmpty());
        
        // Should still return valid structure even with errors
        assertNotNull(result.getRepositories());
        assertNotNull(result.getJpaRepositories());
        assertNotNull(result.getMetrics());
        assertNotNull(result.getSummary());
    }
    
    @Test
    void testRepositoryMetricsCalculation() {
        // Test that metrics are calculated correctly for empty result
        JarContent emptyJar = createJarContentWithClasses(); // No classes
        
        RepositoryAnalysisResult result = analyzer.analyzeRepositories(emptyJar);
        
        RepositoryMetrics metrics = result.getMetrics();
        assertNotNull(metrics);
        
        assertEquals(0, metrics.getTotalRepositories());
        assertEquals(0, metrics.getJpaRepositories());
        assertEquals(0, metrics.getCustomRepositories());
        assertEquals(0, metrics.getTotalQueryMethods());
        assertEquals(0, metrics.getNativeQueries());
        assertEquals(0, metrics.getCustomQueries());
        assertEquals(0.0, metrics.getAverageMethodsPerRepository(), 0.001);
        
        // Test percentage calculations
        assertEquals(0.0, metrics.getJpaRepositoryPercentage(), 0.001);
        assertEquals(0.0, metrics.getNativeQueryPercentage(), 0.001);
    }
    
    @Test
    void testRepositorySummaryGeneration() {
        JarContent emptyJar = createJarContentWithClasses();
        
        RepositoryAnalysisResult result = analyzer.analyzeRepositories(emptyJar);
        
        RepositorySummary summary = result.getSummary();
        assertNotNull(summary);
        
        assertEquals(0, summary.getTotalRepositories());
        assertEquals(0, summary.getJpaRepositories());
        assertEquals(0, summary.getCustomRepositories());
        assertTrue(summary.getTotalIssues() >= 0);
        assertNotNull(summary.getQualityRating());
        
        // For empty JAR, should have good data access = false (no repositories)
        assertFalse(summary.hasGoodDataAccess());
        
        // Quality rating should be "Excellent" for no issues
        if (summary.getTotalIssues() == 0) {
            assertEquals("Excellent", summary.getQualityRating());
        }
    }
    
    private JarContent createJarContentWithClasses(String... classNames) {
        Set<ClassInfo> classes = Set.of();
        
        if (classNames.length > 0) {
            classes = new java.util.HashSet<>();
            for (String className : classNames) {
                ClassInfo classInfo = ClassInfo.builder()
                    .fullyQualifiedName(className)
                    .classType(ClassType.CLASS)
                    .build();
                classes.add(classInfo);
            }
        }
        
        return JarContent.builder()
            .location(new JarLocation(Paths.get("/test/sample.jar")))
            .jarType(JarType.REGULAR_JAR)
            .classes(classes)
            .build();
    }
}