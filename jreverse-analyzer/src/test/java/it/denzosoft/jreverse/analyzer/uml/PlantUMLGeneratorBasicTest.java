package it.denzosoft.jreverse.analyzer.uml;

import it.denzosoft.jreverse.core.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic test suite for PlantUMLGenerator to verify core functionality.
 * This is a simplified test that focuses on the main generation capabilities.
 */
class PlantUMLGeneratorBasicTest {

    private PlantUMLGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new PlantUMLGenerator();
    }

    @Test
    @DisplayName("Should generate valid class diagram with basic setup")
    void shouldGenerateValidClassDiagramWithBasicSetup() {
        // Given
        Set<ClassInfo> classes = createBasicClassSet();
        UMLGenerationRequest request = UMLGenerationRequest.builder()
                .title("Basic Test Diagram")
                .includeClasses(classes)
                .detailLevel(DetailLevel.SUMMARY)
                .build();

        // When
        UMLGenerationResult result = generator.generateClassDiagram(request);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccessful());
        assertNotNull(result.getContent());
        assertFalse(result.getContent().trim().isEmpty());

        // Validate basic PlantUML structure
        String content = result.getContent();
        assertTrue(content.contains("@startuml"));
        assertTrue(content.contains("@enduml"));
        assertTrue(content.contains("title Basic Test Diagram"));
    }

    @Test
    @DisplayName("Should handle null request gracefully")
    void shouldHandleNullRequestGracefully() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            generator.generateClassDiagram(null);
        });
    }

    @Test
    @DisplayName("Should generate package diagram successfully")
    void shouldGeneratePackageDiagramSuccessfully() {
        // Given
        PackageAnalysisResult packageAnalysis = createBasicPackageAnalysis();

        // When
        UMLGenerationResult result = generator.generatePackageDiagram(packageAnalysis);

        // Then
        assertTrue(result.isSuccessful());
        assertNotNull(result.getContent());
        assertTrue(result.getContent().contains("@startuml"));
        assertTrue(result.getContent().contains("@enduml"));
    }

    @Test
    @DisplayName("Should generate pattern diagram with detected patterns")
    void shouldGeneratePatternDiagramWithDetectedPatterns() {
        // Given
        List<DetectedDesignPattern> patterns = createBasicPatterns();

        // When
        UMLGenerationResult result = generator.generatePatternDiagram(patterns, null);

        // Then
        assertTrue(result.isSuccessful());
        assertNotNull(result.getContent());
        assertTrue(result.getContent().contains("@startuml"));
        assertTrue(result.getContent().contains("@enduml"));
    }

    @Test
    @DisplayName("Should return correct generator name")
    void shouldReturnCorrectGeneratorName() {
        // When
        String name = generator.getGeneratorName();

        // Then
        assertEquals("PlantUMLGenerator", name);
    }

    @Test
    @DisplayName("Should correctly identify generation capability")
    void shouldCorrectlyIdentifyGenerationCapability() {
        // Given
        JarContent validContent = createValidJarContent();
        JarContent emptyContent = createEmptyJarContent();

        // When & Then
        assertTrue(generator.canGenerate(validContent));
        assertFalse(generator.canGenerate(emptyContent));
        assertFalse(generator.canGenerate(null));
    }

    // ===================== Helper Methods =====================

    private Set<ClassInfo> createBasicClassSet() {
        Set<ClassInfo> classes = new HashSet<>();

        classes.add(ClassInfo.builder()
                .fullyQualifiedName("com.example.UserService")
                .classType(ClassType.CLASS)
                .build());

        classes.add(ClassInfo.builder()
                .fullyQualifiedName("com.example.UserRepository")
                .classType(ClassType.INTERFACE)
                .build());

        classes.add(ClassInfo.builder()
                .fullyQualifiedName("com.example.User")
                .classType(ClassType.CLASS)
                .build());

        return classes;
    }

    private PackageAnalysisResult createBasicPackageAnalysis() {
        PackageInfo servicePackage = PackageInfo.builder().name("com.example.service").build();
        PackageInfo repositoryPackage = PackageInfo.builder().name("com.example.repository").build();
        PackageInfo entityPackage = PackageInfo.builder().name("com.example.entity").build();

        PackageHierarchy hierarchy = PackageHierarchy.builder()
                .addPackage(servicePackage)
                .addPackage(repositoryPackage)
                .addPackage(entityPackage)
                .build();

        return PackageAnalysisResult.builder()
                .hierarchy(hierarchy)
                .metadata(AnalysisMetadata.successful())
                .build();
    }

    private List<DetectedDesignPattern> createBasicPatterns() {
        List<DetectedDesignPattern> patterns = new ArrayList<>();

        patterns.add(DetectedDesignPattern.builder()
                .patternType(DesignPatternType.SINGLETON)
                .confidence(0.9)
                .participatingClasses(new HashSet<>(Arrays.asList("com.example.DatabaseConnection")))
                .description("Singleton pattern for database connection")
                .build());

        patterns.add(DetectedDesignPattern.builder()
                .patternType(DesignPatternType.REPOSITORY)
                .confidence(0.85)
                .participatingClasses(new HashSet<>(Arrays.asList("com.example.UserRepository")))
                .description("Repository pattern for data access")
                .build());

        return patterns;
    }

    private JarContent createValidJarContent() {
        Set<ClassInfo> classes = createBasicClassSet();
        return JarContent.builder()
                .location(new JarLocation("/test/example.jar"))
                .jarType(JarType.REGULAR)
                .classes(classes)
                .build();
    }

    private JarContent createEmptyJarContent() {
        return JarContent.builder()
                .location(new JarLocation("/test/empty.jar"))
                .jarType(JarType.REGULAR)
                .classes(Collections.emptySet())
                .build();
    }
}