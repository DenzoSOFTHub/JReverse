package it.denzosoft.jreverse.analyzer.configuration;

import it.denzosoft.jreverse.core.model.BeanDefinitionInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BeanOverrideDetector.
 */
class BeanOverrideDetectorTest {

    private BeanOverrideDetector detector;

    @BeforeEach
    void setUp() {
        detector = new BeanOverrideDetector();
    }

    @Test
    void testDetectOverrides_WithNullList_ReturnsEmpty() {
        BeanOverrideAnalysisResult result = detector.detectOverrides(null);

        assertNotNull(result);
        assertFalse(result.hasOverrides());
        assertFalse(result.hasConflicts());
    }

    @Test
    void testDetectOverrides_WithEmptyList_ReturnsEmpty() {
        BeanOverrideAnalysisResult result = detector.detectOverrides(Collections.emptyList());

        assertNotNull(result);
        assertFalse(result.hasOverrides());
        assertFalse(result.hasConflicts());
    }

    @Test
    void testDetectOverrides_WithUniqueBeansNoOverrides() {
        List<BeanDefinitionInfo> beans = Arrays.asList(
            buildBean("service1", "com.example.Service1", "com.example.Config1"),
            buildBean("service2", "com.example.Service2", "com.example.Config2")
        );

        BeanOverrideAnalysisResult result = detector.detectOverrides(beans);

        assertNotNull(result);
        assertFalse(result.hasOverrides());
        assertFalse(result.hasConflicts());
        assertEquals(0, result.getTotalOverrideCount());
        assertEquals(0, result.getTotalConflictCount());
    }

    @Test
    void testDetectOverrides_WithNameOverride_DetectsOverride() {
        List<BeanDefinitionInfo> beans = Arrays.asList(
            buildBean("dataSource", "com.example.DataSource1", "com.example.Config1"),
            buildBean("dataSource", "com.example.DataSource2", "com.example.Config2")
        );

        BeanOverrideAnalysisResult result = detector.detectOverrides(beans);

        assertNotNull(result);
        assertTrue(result.hasOverrides());
        assertEquals(1, result.getNameOverrides().size());
        assertEquals(0, result.getTypeOverrides().size());

        BeanOverride override = result.getNameOverrides().get(0);
        assertEquals(BeanOverride.OverrideType.NAME, override.getType());
        assertEquals("dataSource", override.getIdentifier());
        assertEquals(1, override.getOverriddenCount());
        assertNotNull(override.getWinningBean());
        assertEquals(1, override.getOverriddenBeans().size());
    }

    @Test
    void testDetectOverrides_WithPrimaryBean_WinsPrimary() {
        BeanDefinitionInfo primaryBean = createBean("dataSource", "com.example.PrimaryDataSource", "com.example.Config1")
            .isPrimary(true)
            .build();

        BeanDefinitionInfo normalBean = createBean("dataSource", "com.example.NormalDataSource", "com.example.Config2")
            .build();

        List<BeanDefinitionInfo> beans = Arrays.asList(normalBean, primaryBean); // Order shouldn't matter

        BeanOverrideAnalysisResult result = detector.detectOverrides(beans);

        assertNotNull(result);
        assertTrue(result.hasOverrides());
        assertEquals(1, result.getNameOverrides().size());

        BeanOverride override = result.getNameOverrides().get(0);
        assertEquals(primaryBean, override.getWinningBean());
        assertEquals(BeanOverrideDetector.OverrideReason.PRIMARY_ANNOTATION, override.getReason());
        assertTrue(override.isExplicitOverride());
        assertFalse(override.isPotentialProblem());
    }

    @Test
    void testDetectOverrides_WithTypeOverride_DetectsTypeOverride() {
        List<BeanDefinitionInfo> beans = Arrays.asList(
            buildBean("service1", "com.example.Service", "com.example.Config1"),
            buildBean("service2", "com.example.Service", "com.example.Config2")
        );

        BeanOverrideAnalysisResult result = detector.detectOverrides(beans);

        assertNotNull(result);
        assertTrue(result.hasOverrides());
        assertEquals(0, result.getNameOverrides().size());
        assertEquals(1, result.getTypeOverrides().size());

        BeanOverride override = result.getTypeOverrides().get(0);
        assertEquals(BeanOverride.OverrideType.TYPE, override.getType());
        assertEquals("com.example.Service", override.getIdentifier());
        assertEquals(1, override.getOverriddenCount());
    }

    @Test
    void testDetectOverrides_WithMultiplePrimaryBeans_DetectsConflict() {
        BeanDefinitionInfo primaryBean1 = createBean("service1", "com.example.Service", "com.example.Config1")
            .isPrimary(true)
            .build();

        BeanDefinitionInfo primaryBean2 = createBean("service2", "com.example.Service", "com.example.Config2")
            .isPrimary(true)
            .build();

        List<BeanDefinitionInfo> beans = Arrays.asList(primaryBean1, primaryBean2);

        BeanOverrideAnalysisResult result = detector.detectOverrides(beans);

        assertNotNull(result);
        assertTrue(result.hasConflicts());
        assertEquals(1, result.getConflicts().size());

        BeanConflict conflict = result.getConflicts().get(0);
        assertEquals(BeanConflict.ConflictType.MULTIPLE_PRIMARY, conflict.getType());
        assertEquals(BeanConflict.Severity.HIGH, conflict.getSeverity());
        assertEquals(2, conflict.getConflictingBeanCount());
        assertEquals("com.example.Service", conflict.getIdentifier());
    }

    @Test
    void testDetectOverrides_WithDuplicateQualifier_DetectsConflict() {
        BeanDefinitionInfo bean1 = createBean("service1", "com.example.Service1", "com.example.Config1")
            .addQualifier("special")
            .build();

        BeanDefinitionInfo bean2 = createBean("service2", "com.example.Service2", "com.example.Config2")
            .addQualifier("special")
            .build();

        List<BeanDefinitionInfo> beans = Arrays.asList(bean1, bean2);

        BeanOverrideAnalysisResult result = detector.detectOverrides(beans);

        assertNotNull(result);
        assertTrue(result.hasConflicts());
        assertEquals(1, result.getConflicts().size());

        BeanConflict conflict = result.getConflicts().get(0);
        assertEquals(BeanConflict.ConflictType.DUPLICATE_QUALIFIER, conflict.getType());
        assertEquals(BeanConflict.Severity.LOW, conflict.getSeverity());
        assertEquals("special", conflict.getIdentifier());
    }

    @Test
    void testDetectOverrides_WithScopeMismatch_DetectsConflict() {
        BeanDefinitionInfo singletonBean = createBean("service", "com.example.Service", "com.example.Config1")
            .scope(BeanDefinitionInfo.BeanScope.SINGLETON)
            .build();

        BeanDefinitionInfo prototypeBean = createBean("service", "com.example.Service", "com.example.Config2")
            .scope(BeanDefinitionInfo.BeanScope.PROTOTYPE)
            .build();

        List<BeanDefinitionInfo> beans = Arrays.asList(singletonBean, prototypeBean);

        BeanOverrideAnalysisResult result = detector.detectOverrides(beans);

        assertNotNull(result);
        assertTrue(result.hasOverrides());
        assertTrue(result.hasConflicts());

        // Should have both name override and scope conflict
        assertEquals(1, result.getNameOverrides().size());

        List<BeanConflict> scopeConflicts = result.getConflictsBySeverity(BeanConflict.Severity.MEDIUM);
        assertFalse(scopeConflicts.isEmpty());

        BeanConflict scopeConflict = scopeConflicts.get(0);
        assertEquals(BeanConflict.ConflictType.SCOPE_MISMATCH, scopeConflict.getType());
    }

    @Test
    void testDetectOverrides_WithComplexScenario_HandlesCorrectly() {
        // Complex scenario with multiple types of overrides and conflicts
        List<BeanDefinitionInfo> beans = Arrays.asList(
            // Name override with primary
            createBean("dataSource", "com.example.PrimaryDataSource", "com.example.Config1")
                .isPrimary(true).build(),
            createBean("dataSource", "com.example.SecondaryDataSource", "com.example.Config2")
                .build(),

            // Type override
            createBean("service1", "com.example.Service", "com.example.Config1").build(),
            createBean("service2", "com.example.Service", "com.example.Config2").build(),

            // Multiple primary conflict
            createBean("conflicted1", "com.example.ConflictedService", "com.example.Config1")
                .isPrimary(true).build(),
            createBean("conflicted2", "com.example.ConflictedService", "com.example.Config2")
                .isPrimary(true).build()
        );

        BeanOverrideAnalysisResult result = detector.detectOverrides(beans);

        assertNotNull(result);
        assertTrue(result.hasOverrides());
        assertTrue(result.hasConflicts());

        // Should detect name and type overrides
        assertTrue(result.getNameOverrides().size() >= 1);
        assertTrue(result.getTypeOverrides().size() >= 1);

        // Should detect high-severity conflicts
        List<BeanConflict> highSeverityConflicts = result.getHighSeverityConflicts();
        assertFalse(highSeverityConflicts.isEmpty());

        // Analysis summary should be meaningful
        String summary = result.getSummary();
        assertNotNull(summary);
        assertTrue(summary.contains("Bean Override Analysis Summary"));
        assertTrue(summary.contains("Name Overrides"));
        assertTrue(summary.contains("Conflicts"));
    }

    @Test
    void testDetectOverrides_ProblematicOverrides_IdentifiedCorrectly() {
        // Create beans with declaration order override (potentially problematic)
        BeanDefinitionInfo bean1 = createBean("ambiguous", "com.example.Service", "com.example.Config1").build();
        BeanDefinitionInfo bean2 = createBean("ambiguous", "com.example.Service", "com.example.Config2").build();

        List<BeanDefinitionInfo> beans = Arrays.asList(bean1, bean2);

        BeanOverrideAnalysisResult result = detector.detectOverrides(beans);

        assertNotNull(result);
        assertTrue(result.hasOverrides());

        List<BeanOverride> problematicOverrides = result.getProblematicOverrides();
        assertFalse(problematicOverrides.isEmpty());

        BeanOverride problematic = problematicOverrides.get(0);
        assertTrue(problematic.isPotentialProblem());
        assertEquals(BeanOverrideDetector.OverrideReason.DECLARATION_ORDER, problematic.getReason());
    }

    // Helper methods

    private BeanDefinitionInfo.Builder createBean(String name, String beanClass, String declaringClass) {
        return BeanDefinitionInfo.builder()
            .beanName(name)
            .beanClass(beanClass)
            .declaringClass(declaringClass);
    }

    private BeanDefinitionInfo buildBean(String name, String beanClass, String declaringClass) {
        return BeanDefinitionInfo.builder()
            .beanName(name)
            .beanClass(beanClass)
            .declaringClass(declaringClass)
            .build();
    }
}