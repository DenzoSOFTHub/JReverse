package it.denzosoft.jreverse.analyzer.configuration;

import it.denzosoft.jreverse.core.logging.JReverseLogger;
import it.denzosoft.jreverse.core.model.AnnotationInfo;
import it.denzosoft.jreverse.core.model.ClassInfo;
import it.denzosoft.jreverse.core.model.MethodInfo;

import java.util.*;

/**
 * Analyzer for Spring Boot conditional annotations.
 * Handles @ConditionalOnProperty, @ConditionalOnClass, @ConditionalOnBean, etc.
 */
public class ConditionalAnalyzer {

    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(ConditionalAnalyzer.class);

    // Map of conditional annotation types to their analysis strategies
    private static final Map<String, ConditionalType> CONDITIONAL_TYPES = Map.of(
        "org.springframework.boot.autoconfigure.condition.ConditionalOnProperty", ConditionalType.PROPERTY,
        "org.springframework.boot.autoconfigure.condition.ConditionalOnClass", ConditionalType.CLASS,
        "org.springframework.boot.autoconfigure.condition.ConditionalOnBean", ConditionalType.BEAN,
        "org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean", ConditionalType.MISSING_BEAN,
        "org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass", ConditionalType.MISSING_CLASS,
        "org.springframework.boot.autoconfigure.condition.ConditionalOnExpression", ConditionalType.EXPRESSION,
        "org.springframework.boot.autoconfigure.condition.ConditionalOnJava", ConditionalType.JAVA_VERSION,
        "org.springframework.boot.autoconfigure.condition.ConditionalOnResource", ConditionalType.RESOURCE,
        "org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication", ConditionalType.WEB_APPLICATION,
        "org.springframework.context.annotation.Conditional", ConditionalType.CUSTOM
    );

    /**
     * Analyzes conditional annotations on a configuration class.
     */
    public ConditionalInfo analyzeConditionals(ClassInfo classInfo) {
        if (classInfo == null) {
            return null;
        }

        List<ConditionalCondition> conditions = new ArrayList<>();

        // Check class-level conditional annotations
        for (AnnotationInfo annotation : classInfo.getAnnotations()) {
            ConditionalCondition condition = analyzeConditionalAnnotation(annotation);
            if (condition != null) {
                conditions.add(condition);
            }
        }

        // Check method-level conditional annotations for @Bean methods
        for (MethodInfo method : classInfo.getMethods()) {
            boolean isBeanMethod = method.getAnnotations().stream()
                .anyMatch(ann -> "org.springframework.context.annotation.Bean".equals(ann.getType()));

            if (isBeanMethod) {
                for (AnnotationInfo annotation : method.getAnnotations()) {
                    ConditionalCondition condition = analyzeConditionalAnnotation(annotation);
                    if (condition != null) {
                        condition = condition.withMethodContext(method.getName());
                        conditions.add(condition);
                    }
                }
            }
        }

        return conditions.isEmpty() ? null : new ConditionalInfo(classInfo.getFullyQualifiedName(), conditions);
    }

    /**
     * Analyzes a single conditional annotation.
     */
    private ConditionalCondition analyzeConditionalAnnotation(AnnotationInfo annotation) {
        ConditionalType type = CONDITIONAL_TYPES.get(annotation.getType());
        if (type == null) {
            return null;
        }

        try {
            switch (type) {
                case PROPERTY:
                    return analyzePropertyCondition(annotation);
                case CLASS:
                    return analyzeClassCondition(annotation);
                case BEAN:
                    return analyzeBeanCondition(annotation);
                case MISSING_BEAN:
                    return analyzeMissingBeanCondition(annotation);
                case MISSING_CLASS:
                    return analyzeMissingClassCondition(annotation);
                case EXPRESSION:
                    return analyzeExpressionCondition(annotation);
                case JAVA_VERSION:
                    return analyzeJavaVersionCondition(annotation);
                case RESOURCE:
                    return analyzeResourceCondition(annotation);
                case WEB_APPLICATION:
                    return analyzeWebApplicationCondition(annotation);
                case CUSTOM:
                    return analyzeCustomCondition(annotation);
                default:
                    return null;
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to analyze conditional annotation %s: %s", annotation.getType(), e.getMessage());
            return null;
        }
    }

    private ConditionalCondition analyzePropertyCondition(AnnotationInfo annotation) {
        String property = annotation.getStringAttribute("name");
        if (property == null) {
            property = annotation.getStringAttribute("value");
        }

        String expectedValue = annotation.getStringAttribute("havingValue");
        Boolean matchIfMissing = annotation.getBooleanAttribute("matchIfMissing");

        return new ConditionalCondition(
            ConditionalType.PROPERTY,
            annotation.getType(),
            String.format("Property '%s' must %s%s",
                property != null ? property : "unknown",
                expectedValue != null ? "equal '" + expectedValue + "'" : "be present",
                matchIfMissing != null && matchIfMissing ? " (matches if missing)" : ""
            ),
            Map.of(
                "property", property != null ? property : "",
                "expectedValue", expectedValue != null ? expectedValue : "",
                "matchIfMissing", matchIfMissing != null ? matchIfMissing.toString() : "false"
            )
        );
    }

    private ConditionalCondition analyzeClassCondition(AnnotationInfo annotation) {
        String[] classes = annotation.getStringArrayAttribute("value");
        if (classes == null) {
            classes = annotation.getStringArrayAttribute("name");
        }

        String classNames = classes != null ? String.join(", ", classes) : "unknown";

        return new ConditionalCondition(
            ConditionalType.CLASS,
            annotation.getType(),
            String.format("Classes must be present: [%s]", classNames),
            Map.of("requiredClasses", classNames)
        );
    }

    private ConditionalCondition analyzeBeanCondition(AnnotationInfo annotation) {
        String[] beanTypes = annotation.getStringArrayAttribute("value");
        String[] beanNames = annotation.getStringArrayAttribute("name");

        String description = "Bean must be present";
        if (beanTypes != null && beanTypes.length > 0) {
            description += " of type: [" + String.join(", ", beanTypes) + "]";
        }
        if (beanNames != null && beanNames.length > 0) {
            description += " with name: [" + String.join(", ", beanNames) + "]";
        }

        return new ConditionalCondition(
            ConditionalType.BEAN,
            annotation.getType(),
            description,
            Map.of(
                "requiredTypes", beanTypes != null ? String.join(", ", beanTypes) : "",
                "requiredNames", beanNames != null ? String.join(", ", beanNames) : ""
            )
        );
    }

    private ConditionalCondition analyzeMissingBeanCondition(AnnotationInfo annotation) {
        String[] beanTypes = annotation.getStringArrayAttribute("value");
        String[] beanNames = annotation.getStringArrayAttribute("name");

        String description = "Bean must NOT be present";
        if (beanTypes != null && beanTypes.length > 0) {
            description += " of type: [" + String.join(", ", beanTypes) + "]";
        }
        if (beanNames != null && beanNames.length > 0) {
            description += " with name: [" + String.join(", ", beanNames) + "]";
        }

        return new ConditionalCondition(
            ConditionalType.MISSING_BEAN,
            annotation.getType(),
            description,
            Map.of(
                "excludedTypes", beanTypes != null ? String.join(", ", beanTypes) : "",
                "excludedNames", beanNames != null ? String.join(", ", beanNames) : ""
            )
        );
    }

    private ConditionalCondition analyzeMissingClassCondition(AnnotationInfo annotation) {
        String[] classes = annotation.getStringArrayAttribute("value");
        if (classes == null) {
            classes = annotation.getStringArrayAttribute("name");
        }

        String classNames = classes != null ? String.join(", ", classes) : "unknown";

        return new ConditionalCondition(
            ConditionalType.MISSING_CLASS,
            annotation.getType(),
            String.format("Classes must NOT be present: [%s]", classNames),
            Map.of("excludedClasses", classNames)
        );
    }

    private ConditionalCondition analyzeExpressionCondition(AnnotationInfo annotation) {
        String expression = annotation.getStringAttribute("value");

        return new ConditionalCondition(
            ConditionalType.EXPRESSION,
            annotation.getType(),
            String.format("Expression must evaluate to true: %s", expression != null ? expression : "unknown"),
            Map.of("expression", expression != null ? expression : "")
        );
    }

    private ConditionalCondition analyzeJavaVersionCondition(AnnotationInfo annotation) {
        String javaVersion = annotation.getStringAttribute("value");
        String range = annotation.getStringAttribute("range");

        return new ConditionalCondition(
            ConditionalType.JAVA_VERSION,
            annotation.getType(),
            String.format("Java version must be %s %s",
                range != null ? range : "equal to",
                javaVersion != null ? javaVersion : "unknown"
            ),
            Map.of(
                "javaVersion", javaVersion != null ? javaVersion : "",
                "range", range != null ? range : "EQUAL_OR_NEWER"
            )
        );
    }

    private ConditionalCondition analyzeResourceCondition(AnnotationInfo annotation) {
        String[] resources = annotation.getStringArrayAttribute("resources");

        String resourceNames = resources != null ? String.join(", ", resources) : "unknown";

        return new ConditionalCondition(
            ConditionalType.RESOURCE,
            annotation.getType(),
            String.format("Resources must be present: [%s]", resourceNames),
            Map.of("requiredResources", resourceNames)
        );
    }

    private ConditionalCondition analyzeWebApplicationCondition(AnnotationInfo annotation) {
        String type = annotation.getStringAttribute("type");

        return new ConditionalCondition(
            ConditionalType.WEB_APPLICATION,
            annotation.getType(),
            String.format("Must be a web application%s",
                type != null ? " of type: " + type : ""
            ),
            Map.of("webApplicationType", type != null ? type : "ANY")
        );
    }

    private ConditionalCondition analyzeCustomCondition(AnnotationInfo annotation) {
        String[] conditionClasses = annotation.getStringArrayAttribute("value");

        String classNames = conditionClasses != null ? String.join(", ", conditionClasses) : "unknown";

        return new ConditionalCondition(
            ConditionalType.CUSTOM,
            annotation.getType(),
            String.format("Custom conditions must pass: [%s]", classNames),
            Map.of("conditionClasses", classNames)
        );
    }

    /**
     * Enum for different types of conditional annotations.
     */
    public enum ConditionalType {
        PROPERTY,
        CLASS,
        BEAN,
        MISSING_BEAN,
        MISSING_CLASS,
        EXPRESSION,
        JAVA_VERSION,
        RESOURCE,
        WEB_APPLICATION,
        CUSTOM
    }
}