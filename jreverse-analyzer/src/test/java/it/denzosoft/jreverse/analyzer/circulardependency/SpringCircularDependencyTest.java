package it.denzosoft.jreverse.analyzer.circulardependency;

import it.denzosoft.jreverse.analyzer.beancreation.DependencyInjectionType;
import it.denzosoft.jreverse.core.model.CircularDependency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for SpringCircularDependency model class.
 */
class SpringCircularDependencyTest {

    private SpringCircularDependency circularDependency;

    @BeforeEach
    void setUp() {
        circularDependency = new SpringCircularDependency();
    }

    @Test
    @DisplayName("Should create empty circular dependency")
    void shouldCreateEmptyCircularDependency() {
        assertNotNull(circularDependency);
        assertTrue(circularDependency.getCycle().isEmpty());
        assertEquals(0, circularDependency.getCycleLength());
        assertEquals(0, circularDependency.getUniqueClassCount());
    }

    @Test
    @DisplayName("Should create circular dependency with cycle")
    void shouldCreateCircularDependencyWithCycle() {
        List<String> cycle = Arrays.asList("com.example.ServiceA", "com.example.ServiceB", "com.example.ServiceA");
        SpringCircularDependency cd = new SpringCircularDependency(cycle);

        assertEquals(cycle, cd.getCycle());
        assertEquals(2, cd.getCycleLength()); // Excludes duplicate end node
        assertEquals(2, cd.getUniqueClassCount());
    }

    @Test
    @DisplayName("Should set and get all properties correctly")
    void shouldSetAndGetAllPropertiesCorrectly() {
        List<String> cycle = Arrays.asList("com.example.ServiceA", "com.example.ServiceB", "com.example.ServiceA");
        circularDependency.setCycle(cycle);
        circularDependency.setSeverity(CircularDependency.CircularDependencySeverity.HIGH);
        circularDependency.setType(SpringCircularDependencyType.CONSTRUCTOR_ONLY);
        circularDependency.setRisk(SpringCircularDependencyRisk.BEAN_CREATION_EXCEPTION);
        circularDependency.setHasLazyResolution(true);
        circularDependency.setDescription("Test circular dependency");

        assertEquals(cycle, circularDependency.getCycle());
        assertEquals(CircularDependency.CircularDependencySeverity.HIGH, circularDependency.getSeverity());
        assertEquals(SpringCircularDependencyType.CONSTRUCTOR_ONLY, circularDependency.getType());
        assertEquals(SpringCircularDependencyRisk.BEAN_CREATION_EXCEPTION, circularDependency.getRisk());
        assertTrue(circularDependency.isHasLazyResolution());
        assertEquals("Test circular dependency", circularDependency.getDescription());
    }

    @Test
    @DisplayName("Should generate correct cycle description")
    void shouldGenerateCorrectCycleDescription() {
        List<String> cycle = Arrays.asList("com.example.ServiceA", "com.example.ServiceB", "com.example.ServiceA");
        circularDependency.setCycle(cycle);

        String description = circularDependency.getCycleDescription();
        assertEquals("ServiceA -> ServiceB -> ServiceA", description);
    }

    @Test
    @DisplayName("Should return involved classes without duplicate")
    void shouldReturnInvolvedClassesWithoutDuplicate() {
        List<String> cycle = Arrays.asList("com.example.ServiceA", "com.example.ServiceB", "com.example.ServiceA");
        circularDependency.setCycle(cycle);

        List<String> involved = circularDependency.getInvolvedClasses();
        assertEquals(2, involved.size());
        assertTrue(involved.contains("com.example.ServiceA"));
        assertTrue(involved.contains("com.example.ServiceB"));
    }

    @Test
    @DisplayName("Should detect if class is involved in cycle")
    void shouldDetectIfClassIsInvolvedInCycle() {
        List<String> cycle = Arrays.asList("com.example.ServiceA", "com.example.ServiceB", "com.example.ServiceA");
        circularDependency.setCycle(cycle);

        assertTrue(circularDependency.involvesClass("com.example.ServiceA"));
        assertTrue(circularDependency.involvesClass("com.example.ServiceB"));
        assertFalse(circularDependency.involvesClass("com.example.ServiceC"));
    }

    @Test
    @DisplayName("Should detect self-referencing cycle")
    void shouldDetectSelfReferencingCycle() {
        List<String> cycle = Arrays.asList("com.example.ServiceA", "com.example.ServiceA");
        circularDependency.setCycle(cycle);

        assertTrue(circularDependency.isSelfReferencing());
        assertEquals(1, circularDependency.getUniqueClassCount());
    }

    @Test
    @DisplayName("Should detect complex cycle")
    void shouldDetectComplexCycle() {
        List<String> complexCycle = Arrays.asList(
            "com.example.ServiceA",
            "com.example.ServiceB",
            "com.example.ServiceC",
            "com.example.ServiceD",
            "com.example.ServiceE",
            "com.example.ServiceA"
        );
        circularDependency.setCycle(complexCycle);

        assertTrue(circularDependency.isComplexCycle());
        assertEquals(5, circularDependency.getUniqueClassCount());
    }

    @Test
    @DisplayName("Should handle cycle injections")
    void shouldHandleCycleInjections() {
        SpringDependencyInfo injection1 = new SpringDependencyInfo(
            "com.example.ServiceA",
            "com.example.ServiceB",
            DependencyInjectionType.CONSTRUCTOR
        );

        SpringDependencyInfo injection2 = new SpringDependencyInfo(
            "com.example.ServiceB",
            "com.example.ServiceA",
            DependencyInjectionType.FIELD
        );

        circularDependency.addCycleInjection(injection1);
        circularDependency.addCycleInjection(injection2);

        assertEquals(2, circularDependency.getCycleInjections().size());
        assertTrue(circularDependency.hasCriticalInjectionTypes()); // Field injection is critical
    }

    @Test
    @DisplayName("Should handle resolution strategies")
    void shouldHandleResolutionStrategies() {
        SpringResolutionStrategy strategy1 = SpringResolutionStrategy.builder()
            .type(SpringResolutionStrategyType.LAZY_INITIALIZATION)
            .description("Add @Lazy annotation")
            .complexity(SpringResolutionComplexity.LOW)
            .build();

        SpringResolutionStrategy strategy2 = SpringResolutionStrategy.builder()
            .type(SpringResolutionStrategyType.INTERFACE_SEGREGATION)
            .description("Extract interfaces")
            .complexity(SpringResolutionComplexity.MEDIUM)
            .build();

        circularDependency.addResolutionStrategy(strategy1);
        circularDependency.addResolutionStrategy(strategy2);

        assertEquals(2, circularDependency.getResolutionStrategies().size());

        SpringResolutionStrategy primaryStrategy = circularDependency.getPrimaryResolutionStrategy();
        assertNotNull(primaryStrategy);
        assertEquals(SpringResolutionStrategyType.LAZY_INITIALIZATION, primaryStrategy.getType());
    }

    @Test
    @DisplayName("Should get injection types")
    void shouldGetInjectionTypes() {
        SpringDependencyInfo injection1 = new SpringDependencyInfo(
            "com.example.ServiceA",
            "com.example.ServiceB",
            DependencyInjectionType.CONSTRUCTOR
        );

        SpringDependencyInfo injection2 = new SpringDependencyInfo(
            "com.example.ServiceB",
            "com.example.ServiceA",
            DependencyInjectionType.FIELD
        );

        circularDependency.addCycleInjection(injection1);
        circularDependency.addCycleInjection(injection2);

        List<String> injectionTypes = circularDependency.getInjectionTypes();
        assertEquals(2, injectionTypes.size());
        assertTrue(injectionTypes.contains("Constructor injection"));
        assertTrue(injectionTypes.contains("Field injection"));
    }

    @Test
    @DisplayName("Should generate correct summary")
    void shouldGenerateCorrectSummary() {
        List<String> cycle = Arrays.asList("com.example.ServiceA", "com.example.ServiceB", "com.example.ServiceA");
        circularDependency.setCycle(cycle);
        circularDependency.setSeverity(CircularDependency.CircularDependencySeverity.HIGH);
        circularDependency.setType(SpringCircularDependencyType.CONSTRUCTOR_ONLY);
        circularDependency.setHasLazyResolution(true);

        String summary = circularDependency.getSummary();
        assertTrue(summary.contains("Circular dependency involving 2 classes"));
        assertTrue(summary.contains("HIGH severity"));
        assertTrue(summary.contains("Constructor Only"));
        assertTrue(summary.contains("Resolved with @Lazy"));
    }

    @Test
    @DisplayName("Should convert to generic CircularDependency")
    void shouldConvertToGenericCircularDependency() {
        List<String> cycle = Arrays.asList("com.example.ServiceA", "com.example.ServiceB", "com.example.ServiceA");
        circularDependency.setCycle(cycle);
        circularDependency.setSeverity(CircularDependency.CircularDependencySeverity.HIGH);
        circularDependency.setDescription("Test circular dependency");

        SpringResolutionStrategy strategy = SpringResolutionStrategy.builder()
            .type(SpringResolutionStrategyType.LAZY_INITIALIZATION)
            .description("Add @Lazy annotation")
            .complexity(SpringResolutionComplexity.LOW)
            .build();
        circularDependency.addResolutionStrategy(strategy);

        CircularDependency genericCircularDependency = circularDependency.toCircularDependency();

        assertNotNull(genericCircularDependency);
        assertEquals(CircularDependency.CircularDependencySeverity.HIGH, genericCircularDependency.getSeverity());
        assertEquals("Test circular dependency", genericCircularDependency.getDescription());
        assertEquals(2, genericCircularDependency.getCyclePath().size());
        assertFalse(genericCircularDependency.getSuggestions().isEmpty());
    }

    @Test
    @DisplayName("Should handle warnings")
    void shouldHandleWarnings() {
        circularDependency.addWarning("First warning");
        circularDependency.addWarning("Second warning");
        circularDependency.addWarning(""); // Should be ignored
        circularDependency.addWarning(null); // Should be ignored

        assertEquals(2, circularDependency.getWarnings().size());
        assertTrue(circularDependency.getWarnings().contains("First warning"));
        assertTrue(circularDependency.getWarnings().contains("Second warning"));
    }

    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void shouldImplementEqualsAndHashCodeCorrectly() {
        List<String> cycle = Arrays.asList("com.example.ServiceA", "com.example.ServiceB", "com.example.ServiceA");

        SpringCircularDependency cd1 = new SpringCircularDependency(cycle);
        SpringCircularDependency cd2 = new SpringCircularDependency(cycle);
        SpringCircularDependency cd3 = new SpringCircularDependency(Arrays.asList("com.example.ServiceC", "com.example.ServiceC"));

        assertEquals(cd1, cd2);
        assertEquals(cd1.hashCode(), cd2.hashCode());
        assertNotEquals(cd1, cd3);
        assertNotEquals(cd1.hashCode(), cd3.hashCode());
    }

    @Test
    @DisplayName("Should provide meaningful toString")
    void shouldProvideMeaningfulToString() {
        List<String> cycle = Arrays.asList("com.example.ServiceA", "com.example.ServiceB", "com.example.ServiceA");
        circularDependency.setCycle(cycle);
        circularDependency.setSeverity(CircularDependency.CircularDependencySeverity.HIGH);
        circularDependency.setType(SpringCircularDependencyType.CONSTRUCTOR_ONLY);
        circularDependency.setHasLazyResolution(false);

        String toString = circularDependency.toString();

        assertTrue(toString.contains("SpringCircularDependency"));
        assertTrue(toString.contains("classes=2"));
        assertTrue(toString.contains("severity=HIGH"));
        assertTrue(toString.contains("type=Constructor Only"));
        assertTrue(toString.contains("resolved=false"));
    }
}