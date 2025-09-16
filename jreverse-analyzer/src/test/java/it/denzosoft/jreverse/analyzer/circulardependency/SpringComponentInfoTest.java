package it.denzosoft.jreverse.analyzer.circulardependency;

import it.denzosoft.jreverse.analyzer.beancreation.BeanDependency;
import it.denzosoft.jreverse.analyzer.beancreation.DependencyInjectionType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for SpringComponentInfo model class.
 */
class SpringComponentInfoTest {

    private SpringComponentInfo componentInfo;

    @BeforeEach
    void setUp() {
        componentInfo = new SpringComponentInfo();
    }

    @Test
    @DisplayName("Should create empty component info")
    void shouldCreateEmptyComponentInfo() {
        assertNotNull(componentInfo);
        assertNull(componentInfo.getClassName());
        assertNull(componentInfo.getComponentType());
        assertTrue(componentInfo.getDependencies().isEmpty());
        assertTrue(componentInfo.getErrors().isEmpty());
    }

    @Test
    @DisplayName("Should create component info with parameters")
    void shouldCreateComponentInfoWithParameters() {
        SpringComponentInfo info = new SpringComponentInfo("com.example.TestService", SpringComponentType.SERVICE);

        assertEquals("com.example.TestService", info.getClassName());
        assertEquals(SpringComponentType.SERVICE, info.getComponentType());
        assertTrue(info.getDependencies().isEmpty());
        assertTrue(info.getErrors().isEmpty());
    }

    @Test
    @DisplayName("Should throw exception for null parameters")
    void shouldThrowExceptionForNullParameters() {
        assertThrows(NullPointerException.class, () -> {
            new SpringComponentInfo(null, SpringComponentType.SERVICE);
        });

        assertThrows(NullPointerException.class, () -> {
            new SpringComponentInfo("com.example.TestService", null);
        });
    }

    @Test
    @DisplayName("Should set and get all properties correctly")
    void shouldSetAndGetAllPropertiesCorrectly() {
        componentInfo.setClassName("com.example.TestService");
        componentInfo.setComponentType(SpringComponentType.SERVICE);
        componentInfo.setLazyInitialized(true);
        componentInfo.setPrimary(true);
        componentInfo.setHasConstructorInjection(true);
        componentInfo.setHasFieldInjection(false);
        componentInfo.setHasMethodInjection(true);
        componentInfo.setHasLazyDependencies(true);
        componentInfo.setScope("prototype");
        componentInfo.setBeanName("testService");

        assertEquals("com.example.TestService", componentInfo.getClassName());
        assertEquals(SpringComponentType.SERVICE, componentInfo.getComponentType());
        assertTrue(componentInfo.isLazyInitialized());
        assertTrue(componentInfo.isPrimary());
        assertTrue(componentInfo.isHasConstructorInjection());
        assertFalse(componentInfo.isHasFieldInjection());
        assertTrue(componentInfo.isHasMethodInjection());
        assertTrue(componentInfo.isHasLazyDependencies());
        assertEquals("prototype", componentInfo.getScope());
        assertEquals("testService", componentInfo.getBeanName());
    }

    @Test
    @DisplayName("Should handle dependencies correctly")
    void shouldHandleDependenciesCorrectly() {
        BeanDependency dependency1 = BeanDependency.builder()
            .type("com.example.Repository")
            .name("repository")
            .injectionType(DependencyInjectionType.CONSTRUCTOR)
            .build();

        BeanDependency dependency2 = BeanDependency.builder()
            .type("com.example.OtherService")
            .name("otherService")
            .injectionType(DependencyInjectionType.FIELD)
            .build();

        componentInfo.addDependency(dependency1);
        componentInfo.addDependency(dependency2);

        assertEquals(2, componentInfo.getDependencies().size());
        assertEquals(2, componentInfo.getDependencyCount());
        assertTrue(componentInfo.getDependencies().contains(dependency1));
        assertTrue(componentInfo.getDependencies().contains(dependency2));

        // Test null dependency
        componentInfo.addDependency(null);
        assertEquals(2, componentInfo.getDependencies().size()); // Should remain unchanged
    }

    @Test
    @DisplayName("Should handle errors correctly")
    void shouldHandleErrorsCorrectly() {
        componentInfo.addError("First error");
        componentInfo.addError("Second error");
        componentInfo.addError(""); // Should be ignored
        componentInfo.addError(null); // Should be ignored
        componentInfo.addError("   "); // Should be ignored (empty after trim)

        assertEquals(2, componentInfo.getErrors().size());
        assertTrue(componentInfo.hasErrors());
        assertTrue(componentInfo.getErrors().contains("First error"));
        assertTrue(componentInfo.getErrors().contains("Second error"));
    }

    @Test
    @DisplayName("Should handle qualifiers correctly")
    void shouldHandleQualifiersCorrectly() {
        componentInfo.addQualifier("primary");
        componentInfo.addQualifier("secondary");
        componentInfo.addQualifier("primary"); // Duplicate should be ignored
        componentInfo.addQualifier(""); // Should be ignored
        componentInfo.addQualifier(null); // Should be ignored

        assertEquals(2, componentInfo.getQualifiers().size());
        assertTrue(componentInfo.getQualifiers().contains("primary"));
        assertTrue(componentInfo.getQualifiers().contains("secondary"));
    }

    @Test
    @DisplayName("Should get simple class name correctly")
    void shouldGetSimpleClassNameCorrectly() {
        componentInfo.setClassName("com.example.service.TestService");
        assertEquals("TestService", componentInfo.getSimpleClassName());

        componentInfo.setClassName("TestService");
        assertEquals("TestService", componentInfo.getSimpleClassName());

        componentInfo.setClassName(null);
        assertEquals("", componentInfo.getSimpleClassName());
    }

    @Test
    @DisplayName("Should detect dependency injection patterns")
    void shouldDetectDependencyInjectionPatterns() {
        // Initially no dependency injection
        assertFalse(componentInfo.hasDependencyInjection());
        assertFalse(componentInfo.usesMixedInjectionTypes());

        // Add constructor injection
        componentInfo.setHasConstructorInjection(true);
        assertTrue(componentInfo.hasDependencyInjection());
        assertFalse(componentInfo.usesMixedInjectionTypes());

        // Add field injection (mixed types)
        componentInfo.setHasFieldInjection(true);
        assertTrue(componentInfo.hasDependencyInjection());
        assertTrue(componentInfo.usesMixedInjectionTypes());
    }

    @Test
    @DisplayName("Should handle scope correctly")
    void shouldHandleScopeCorrectly() {
        // Default scope
        assertEquals("singleton", componentInfo.getScope());
        assertTrue(componentInfo.isSingleton());
        assertFalse(componentInfo.isPrototype());

        // Set prototype scope
        componentInfo.setScope("prototype");
        assertEquals("prototype", componentInfo.getScope());
        assertFalse(componentInfo.isSingleton());
        assertTrue(componentInfo.isPrototype());

        // Set null scope (should default to singleton)
        componentInfo.setScope(null);
        assertEquals("singleton", componentInfo.getScope());
        assertTrue(componentInfo.isSingleton());
    }

    @Test
    @DisplayName("Should generate correct summary")
    void shouldGenerateCorrectSummary() {
        componentInfo.setComponentType(SpringComponentType.SERVICE);
        componentInfo.setLazyInitialized(true);
        componentInfo.setPrimary(true);
        componentInfo.setScope("prototype");

        BeanDependency dependency = BeanDependency.builder()
            .type("com.example.Repository")
            .name("repository")
            .injectionType(DependencyInjectionType.CONSTRUCTOR)
            .build();
        componentInfo.addDependency(dependency);

        String summary = componentInfo.getSummary();
        assertTrue(summary.contains("Service component"));
        assertTrue(summary.contains("1 dependencies"));
        assertTrue(summary.contains("(lazy)"));
        assertTrue(summary.contains("(primary)"));
        assertTrue(summary.contains("(prototype)"));
    }

    @Test
    @DisplayName("Should provide meaningful toString")
    void shouldProvideMeaningfulToString() {
        componentInfo.setClassName("com.example.TestService");
        componentInfo.setComponentType(SpringComponentType.SERVICE);
        componentInfo.setLazyInitialized(true);
        componentInfo.setPrimary(false);

        BeanDependency dependency = BeanDependency.builder()
            .type("com.example.Repository")
            .name("repository")
            .injectionType(DependencyInjectionType.CONSTRUCTOR)
            .build();
        componentInfo.addDependency(dependency);

        String toString = componentInfo.toString();

        assertTrue(toString.contains("SpringComponentInfo"));
        assertTrue(toString.contains("className='com.example.TestService'"));
        assertTrue(toString.contains("type=Service"));
        assertTrue(toString.contains("dependencies=1"));
        assertTrue(toString.contains("lazy=true"));
        assertTrue(toString.contains("primary=false"));
    }

    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void shouldImplementEqualsAndHashCodeCorrectly() {
        SpringComponentInfo info1 = new SpringComponentInfo("com.example.TestService", SpringComponentType.SERVICE);
        SpringComponentInfo info2 = new SpringComponentInfo("com.example.TestService", SpringComponentType.SERVICE);
        SpringComponentInfo info3 = new SpringComponentInfo("com.example.OtherService", SpringComponentType.SERVICE);

        assertEquals(info1, info2);
        assertEquals(info1.hashCode(), info2.hashCode());
        assertNotEquals(info1, info3);
        assertNotEquals(info1.hashCode(), info3.hashCode());

        // Test with null
        SpringComponentInfo info4 = new SpringComponentInfo();
        SpringComponentInfo info5 = new SpringComponentInfo();
        assertEquals(info4, info5);
    }

    @Test
    @DisplayName("Should handle defensive copying of collections")
    void shouldHandleDefensiveCopyingOfCollections() {
        BeanDependency dependency = BeanDependency.builder()
            .type("com.example.Repository")
            .name("repository")
            .injectionType(DependencyInjectionType.CONSTRUCTOR)
            .build();

        componentInfo.setDependencies(Arrays.asList(dependency));
        componentInfo.setErrors(Arrays.asList("Error 1", "Error 2"));
        componentInfo.setQualifiers(Arrays.asList("qualifier1", "qualifier2"));

        // Modify returned collections should not affect internal state
        componentInfo.getDependencies().clear();
        componentInfo.getErrors().clear();
        componentInfo.getQualifiers().clear();

        assertEquals(1, componentInfo.getDependencyCount());
        assertEquals(2, componentInfo.getErrors().size());
        assertEquals(2, componentInfo.getQualifiers().size());
    }

    @Test
    @DisplayName("Should handle null collections in setters")
    void shouldHandleNullCollectionsInSetters() {
        componentInfo.setDependencies(null);
        componentInfo.setErrors(null);
        componentInfo.setQualifiers(null);

        assertNotNull(componentInfo.getDependencies());
        assertNotNull(componentInfo.getErrors());
        assertNotNull(componentInfo.getQualifiers());
        assertTrue(componentInfo.getDependencies().isEmpty());
        assertTrue(componentInfo.getErrors().isEmpty());
        assertTrue(componentInfo.getQualifiers().isEmpty());
    }
}