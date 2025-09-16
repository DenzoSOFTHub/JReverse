package it.denzosoft.jreverse.analyzer.configuration;

import it.denzosoft.jreverse.core.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ConditionalAnalyzer.
 */
class ConditionalAnalyzerTest {

    private ConditionalAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new ConditionalAnalyzer();
    }

    @Test
    void testAnalyzeConditionals_WithNullClass_ReturnsNull() {
        ConditionalInfo result = analyzer.analyzeConditionals(null);
        assertNull(result);
    }

    @Test
    void testAnalyzeConditionals_WithNoConditionals_ReturnsNull() {
        ClassInfo classInfo = createClassInfo("com.example.Config")
            .addAnnotation(createAnnotation("org.springframework.context.annotation.Configuration"))
            .build();

        ConditionalInfo result = analyzer.analyzeConditionals(classInfo);
        assertNull(result);
    }

    @Test
    void testAnalyzeConditionals_WithConditionalOnProperty_ExtractsCondition() {
        ClassInfo classInfo = createClassInfo("com.example.ConditionalConfig")
            .addAnnotation(createConditionalOnPropertyAnnotation("feature.enabled", "true"))
            .build();

        ConditionalInfo result = analyzer.analyzeConditionals(classInfo);

        assertNotNull(result);
        assertEquals("com.example.ConditionalConfig", result.getClassName());
        assertEquals(1, result.getConditionCount());
        assertTrue(result.hasPropertyConditions());

        ConditionalCondition condition = result.getConditions().get(0);
        assertEquals(ConditionalAnalyzer.ConditionalType.PROPERTY, condition.getType());
        assertTrue(condition.getDescription().contains("Property 'feature.enabled'"));
        assertTrue(condition.getDescription().contains("equal 'true'"));
    }

    @Test
    void testAnalyzeConditionals_WithConditionalOnClass_ExtractsCondition() {
        ClassInfo classInfo = createClassInfo("com.example.ConditionalConfig")
            .addAnnotation(createConditionalOnClassAnnotation("com.example.RequiredClass"))
            .build();

        ConditionalInfo result = analyzer.analyzeConditionals(classInfo);

        assertNotNull(result);
        assertEquals(1, result.getConditionCount());
        assertTrue(result.hasClassConditions());

        ConditionalCondition condition = result.getConditions().get(0);
        assertEquals(ConditionalAnalyzer.ConditionalType.CLASS, condition.getType());
        assertTrue(condition.getDescription().contains("Classes must be present"));
        assertTrue(condition.getDescription().contains("com.example.RequiredClass"));
    }

    @Test
    void testAnalyzeConditionals_WithConditionalOnBean_ExtractsCondition() {
        ClassInfo classInfo = createClassInfo("com.example.ConditionalConfig")
            .addAnnotation(createConditionalOnBeanAnnotation("com.example.RequiredBean"))
            .build();

        ConditionalInfo result = analyzer.analyzeConditionals(classInfo);

        assertNotNull(result);
        assertEquals(1, result.getConditionCount());
        assertTrue(result.hasBeanConditions());

        ConditionalCondition condition = result.getConditions().get(0);
        assertEquals(ConditionalAnalyzer.ConditionalType.BEAN, condition.getType());
        assertTrue(condition.getDescription().contains("Bean must be present"));
        assertTrue(condition.getDescription().contains("com.example.RequiredBean"));
    }

    @Test
    void testAnalyzeConditionals_WithConditionalOnMissingBean_ExtractsCondition() {
        ClassInfo classInfo = createClassInfo("com.example.ConditionalConfig")
            .addAnnotation(createConditionalOnMissingBeanAnnotation("com.example.ExcludedBean"))
            .build();

        ConditionalInfo result = analyzer.analyzeConditionals(classInfo);

        assertNotNull(result);
        assertEquals(1, result.getConditionCount());
        assertTrue(result.hasBeanConditions());

        ConditionalCondition condition = result.getConditions().get(0);
        assertEquals(ConditionalAnalyzer.ConditionalType.MISSING_BEAN, condition.getType());
        assertTrue(condition.getDescription().contains("Bean must NOT be present"));
        assertTrue(condition.getDescription().contains("com.example.ExcludedBean"));
    }

    @Test
    void testAnalyzeConditionals_WithConditionalOnExpression_ExtractsCondition() {
        ClassInfo classInfo = createClassInfo("com.example.ConditionalConfig")
            .addAnnotation(createConditionalOnExpressionAnnotation("#{environment.acceptsProfiles('dev')}"))
            .build();

        ConditionalInfo result = analyzer.analyzeConditionals(classInfo);

        assertNotNull(result);
        assertEquals(1, result.getConditionCount());

        ConditionalCondition condition = result.getConditions().get(0);
        assertEquals(ConditionalAnalyzer.ConditionalType.EXPRESSION, condition.getType());
        assertTrue(condition.getDescription().contains("Expression must evaluate to true"));
        assertTrue(condition.getDescription().contains("environment.acceptsProfiles('dev')"));
    }

    @Test
    void testAnalyzeConditionals_WithMethodLevelConditionals_ExtractsWithContext() {
        MethodInfo beanMethod = createBeanMethod("conditionalBean")
            .addAnnotation(createConditionalOnPropertyAnnotation("method.feature.enabled", "true"))
            .build();

        ClassInfo classInfo = createClassInfo("com.example.Config")
            .addAnnotation(createAnnotation("org.springframework.context.annotation.Configuration"))
            .addMethod(beanMethod)
            .build();

        ConditionalInfo result = analyzer.analyzeConditionals(classInfo);

        assertNotNull(result);
        assertEquals(1, result.getConditionCount());

        ConditionalCondition condition = result.getConditions().get(0);
        assertTrue(condition.hasMethodContext());
        assertEquals("conditionalBean", condition.getMethodContext());
        assertTrue(condition.getFullDescription().contains("[Method: conditionalBean]"));
    }

    @Test
    void testAnalyzeConditionals_WithMultipleConditionals_ExtractsAll() {
        ClassInfo classInfo = createClassInfo("com.example.MultiConditionalConfig")
            .addAnnotation(createConditionalOnPropertyAnnotation("feature.enabled", "true"))
            .addAnnotation(createConditionalOnClassAnnotation("com.example.RequiredClass"))
            .build();

        ConditionalInfo result = analyzer.analyzeConditionals(classInfo);

        assertNotNull(result);
        assertEquals(2, result.getConditionCount());
        assertTrue(result.hasPropertyConditions());
        assertTrue(result.hasClassConditions());

        assertTrue(result.getConditionSummary().contains("Property 'feature.enabled'"));
        assertTrue(result.getConditionSummary().contains("Classes must be present"));
    }

    @Test
    void testAnalyzeConditionals_WithInvalidAnnotation_IgnoresGracefully() {
        // Create an annotation that looks conditional but has invalid structure
        AnnotationInfo invalidAnnotation = AnnotationInfo.builder()
            .type("org.springframework.boot.autoconfigure.condition.ConditionalOnProperty")
            .build(); // No attributes

        ClassInfo classInfo = createClassInfo("com.example.Config")
            .addAnnotation(invalidAnnotation)
            .build();

        ConditionalInfo result = analyzer.analyzeConditionals(classInfo);

        // Should handle gracefully - either return null or return info with valid conditions only
        if (result != null) {
            // If conditions are extracted, they should be valid
            result.getConditions().forEach(condition -> {
                assertNotNull(condition.getType());
                assertNotNull(condition.getDescription());
            });
        }
    }

    // Helper methods

    private ClassInfo.Builder createClassInfo(String fullyQualifiedName) {
        return ClassInfo.builder()
            .fullyQualifiedName(fullyQualifiedName)
            .classType(ClassType.CLASS);
    }

    private MethodInfo.Builder createBeanMethod(String methodName) {
        return MethodInfo.builder()
            .name(methodName)
            .returnType("java.lang.Object")
            .declaringClassName("com.example.Config")
            .addAnnotation(createAnnotation("org.springframework.context.annotation.Bean"));
    }

    private AnnotationInfo createAnnotation(String type) {
        return AnnotationInfo.builder()
            .type(type)
            .build();
    }

    private AnnotationInfo createConditionalOnPropertyAnnotation(String property, String value) {
        return AnnotationInfo.builder()
            .type("org.springframework.boot.autoconfigure.condition.ConditionalOnProperty")
            .addAttribute("name", property)
            .addAttribute("havingValue", value)
            .build();
    }

    private AnnotationInfo createConditionalOnClassAnnotation(String... classes) {
        return AnnotationInfo.builder()
            .type("org.springframework.boot.autoconfigure.condition.ConditionalOnClass")
            .addAttribute("value", classes)
            .build();
    }

    private AnnotationInfo createConditionalOnBeanAnnotation(String... beanTypes) {
        return AnnotationInfo.builder()
            .type("org.springframework.boot.autoconfigure.condition.ConditionalOnBean")
            .addAttribute("value", beanTypes)
            .build();
    }

    private AnnotationInfo createConditionalOnMissingBeanAnnotation(String... beanTypes) {
        return AnnotationInfo.builder()
            .type("org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean")
            .addAttribute("value", beanTypes)
            .build();
    }

    private AnnotationInfo createConditionalOnExpressionAnnotation(String expression) {
        return AnnotationInfo.builder()
            .type("org.springframework.boot.autoconfigure.condition.ConditionalOnExpression")
            .addAttribute("value", expression)
            .build();
    }
}