package it.denzosoft.jreverse.analyzer.packageanalyzer;

import it.denzosoft.jreverse.core.model.*;
import it.denzosoft.jreverse.core.port.PackageAnalyzer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for JavassistPackageAnalyzer.
 * Tests package hierarchy analysis, naming conventions, and organizational metrics.
 *
 * @author JReverse Development Team
 * @since 1.1.0 (Phase 3)
 */
@DisplayName("JavassistPackageAnalyzer Tests")
class JavassistPackageAnalyzerTest {

    private JavassistPackageAnalyzer packageAnalyzer;
    private JarContent testJarContent;

    @BeforeEach
    void setUp() {
        packageAnalyzer = new JavassistPackageAnalyzer();
        testJarContent = createTestJarContent();
    }

    @AfterEach
    void tearDown() {
        // PackageAnalyzer doesn't have shutdown method
    }

    @Test
    @DisplayName("Should analyze package structure successfully")
    void testAnalyzePackageStructure_BasicSuccess() {
        // When
        PackageAnalysisResult result = packageAnalyzer.analyzePackageStructure(testJarContent);

        // Then
        assertNotNull(result);
        assertNotNull(result.getHierarchy());
        assertTrue(result.getHierarchy().getPackageCount() > 0);
    }

    @Test
    @DisplayName("Should handle null input with proper exception")
    void testAnalyzePackageStructure_NullInput() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            packageAnalyzer.analyzePackageStructure(null);
        });
    }

    @Test
    @DisplayName("Should handle empty JAR content gracefully")
    void testAnalyzePackageStructure_EmptyJar() {
        // Given
        JarContent emptyJar = createEmptyJarContent();

        // When
        PackageAnalysisResult result = packageAnalyzer.analyzePackageStructure(emptyJar);

        // Then
        assertNotNull(result);
        assertNotNull(result.getHierarchy());
        assertEquals(0, result.getHierarchy().getPackageCount());
    }

    @Test
    @DisplayName("Should extract package hierarchy correctly")
    void testAnalyzePackageStructure_PackageHierarchy() {
        // When
        PackageAnalysisResult result = packageAnalyzer.analyzePackageStructure(testJarContent);

        // Then
        PackageHierarchy hierarchy = result.getHierarchy();
        assertNotNull(hierarchy);
        assertTrue(hierarchy.getPackageCount() > 0);

        // Verify specific packages exist
        Set<String> packageNames = hierarchy.getPackages().keySet();
        assertTrue(packageNames.contains("com.example"));
        assertTrue(packageNames.contains("com.example.service"));
        assertTrue(packageNames.contains("com.example.repository"));
    }

    @Test
    @DisplayName("Should analyze package depth correctly")
    void testAnalyzePackageStructure_PackageDepth() {
        // When
        PackageAnalysisResult result = packageAnalyzer.analyzePackageStructure(testJarContent);

        // Then
        PackageHierarchy hierarchy = result.getHierarchy();
        assertTrue(hierarchy.getMaxDepth() > 1); // Should have nested packages
        assertTrue(hierarchy.getAverageDepth() > 0);
    }

    @Test
    @DisplayName("Should validate JAR content correctly")
    void testCanAnalyze() {
        // Test valid JAR content
        assertTrue(packageAnalyzer.canAnalyze(testJarContent));

        // Test null input
        assertFalse(packageAnalyzer.canAnalyze(null));

        // Test empty JAR content
        JarContent emptyJar = createEmptyJarContent();
        assertFalse(packageAnalyzer.canAnalyze(emptyJar));
    }

    @Test
    @DisplayName("Should provide analyzer name")
    void testGetAnalyzerName() {
        // When
        String analyzerName = packageAnalyzer.getAnalyzerName();

        // Then
        assertNotNull(analyzerName);
        assertFalse(analyzerName.isEmpty());
    }

    @Test
    @DisplayName("Should handle large package structures")
    void testAnalyzePackageStructure_LargePackages() {
        // Given
        JarContent largeJar = createLargeJarContent();

        // When
        PackageAnalysisResult result = packageAnalyzer.analyzePackageStructure(largeJar);

        // Then
        assertNotNull(result);

        PackageHierarchy hierarchy = result.getHierarchy();
        assertTrue(hierarchy.getPackageCount() >= 10); // Large structure
    }

    @Test
    @DisplayName("Should handle invalid package names")
    void testAnalyzePackageStructure_InvalidNames() {
        // Given
        JarContent invalidNamesJar = createJarWithInvalidPackageNames();

        // When
        PackageAnalysisResult result = packageAnalyzer.analyzePackageStructure(invalidNamesJar);

        // Then
        assertNotNull(result);
        assertNotNull(result.getHierarchy());

        // Just verify it handles invalid names without crashing
        assertTrue(result.getHierarchy().getPackageCount() > 0);
    }

    @Test
    @DisplayName("Should handle error scenarios gracefully")
    void testAnalyzePackageStructure_ErrorHandling() {
        // Test that the analyzer handles various edge cases without throwing
        // This is important for robustness
        assertDoesNotThrow(() -> {
            PackageAnalysisResult result = packageAnalyzer.analyzePackageStructure(testJarContent);
            assertNotNull(result);
        });
    }

    // Helper methods for creating test data

    private JarContent createTestJarContent() {
        Set<ClassInfo> classes = new HashSet<>();

        // Add classes with well-structured package hierarchy
        classes.add(ClassInfo.builder()
            .fullyQualifiedName("com.example.Application")
            .classType(ClassType.CLASS)
            .build());

        classes.add(ClassInfo.builder()
            .fullyQualifiedName("com.example.service.UserService")
            .classType(ClassType.CLASS)
            .build());

        classes.add(ClassInfo.builder()
            .fullyQualifiedName("com.example.service.PaymentService")
            .classType(ClassType.CLASS)
            .build());

        classes.add(ClassInfo.builder()
            .fullyQualifiedName("com.example.repository.UserRepository")
            .classType(ClassType.INTERFACE)
            .build());

        classes.add(ClassInfo.builder()
            .fullyQualifiedName("com.example.repository.PaymentRepository")
            .classType(ClassType.INTERFACE)
            .build());

        classes.add(ClassInfo.builder()
            .fullyQualifiedName("com.example.controller.UserController")
            .classType(ClassType.CLASS)
            .build());

        return JarContent.builder()
            .location(new JarLocation("/test/example.jar"))
            .classes(classes)
            .jarType(JarType.SPRING_BOOT)
            .build();
    }

    private JarContent createEmptyJarContent() {
        return JarContent.builder()
            .location(new JarLocation("/test/empty.jar"))
            .classes(Collections.emptySet())
            .jarType(JarType.REGULAR)
            .build();
    }

    private JarContent createLargeJarContent() {
        Set<ClassInfo> classes = new HashSet<>();

        // Create classes distributed across many packages
        String[] packages = {
            "com.company.core", "com.company.api", "com.company.service",
            "com.company.repository", "com.company.controller", "com.company.dto",
            "com.company.config", "com.company.security", "com.company.util",
            "com.company.exception", "com.company.validation", "com.company.converter"
        };

        for (String packageName : packages) {
            for (int i = 0; i < 5; i++) {
                classes.add(ClassInfo.builder()
                    .fullyQualifiedName(packageName + ".Class" + i)
                    .classType(i % 2 == 0 ? ClassType.CLASS : ClassType.INTERFACE)
                    .build());
            }
        }

        return JarContent.builder()
            .location(new JarLocation("/test/large.jar"))
            .classes(classes)
            .jarType(JarType.SPRING_BOOT)
            .build();
    }

    private JarContent createJarWithInvalidPackageNames() {
        Set<ClassInfo> classes = new HashSet<>();

        // Add classes with poor naming conventions
        classes.add(ClassInfo.builder()
            .fullyQualifiedName("BADPACKAGE.MyClass")
            .classType(ClassType.CLASS)
            .build());

        classes.add(ClassInfo.builder()
            .fullyQualifiedName("package_with_underscores.AnotherClass")
            .classType(ClassType.CLASS)
            .build());

        classes.add(ClassInfo.builder()
            .fullyQualifiedName("package123.NumberInPackage")
            .classType(ClassType.CLASS)
            .build());

        return JarContent.builder()
            .location(new JarLocation("/test/invalid.jar"))
            .classes(classes)
            .jarType(JarType.REGULAR)
            .build();
    }
}