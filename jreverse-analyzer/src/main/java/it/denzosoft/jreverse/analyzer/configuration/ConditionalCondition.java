package it.denzosoft.jreverse.analyzer.configuration;

import java.util.Collections;
import java.util.Map;

/**
 * Represents a single conditional condition found in a configuration class.
 */
public class ConditionalCondition {

    private final ConditionalAnalyzer.ConditionalType type;
    private final String annotationType;
    private final String description;
    private final Map<String, String> attributes;
    private final String methodContext; // For method-level conditions

    public ConditionalCondition(ConditionalAnalyzer.ConditionalType type,
                               String annotationType,
                               String description,
                               Map<String, String> attributes) {
        this(type, annotationType, description, attributes, null);
    }

    public ConditionalCondition(ConditionalAnalyzer.ConditionalType type,
                               String annotationType,
                               String description,
                               Map<String, String> attributes,
                               String methodContext) {
        this.type = type;
        this.annotationType = annotationType;
        this.description = description;
        this.attributes = Collections.unmodifiableMap(attributes);
        this.methodContext = methodContext;
    }

    public ConditionalAnalyzer.ConditionalType getType() {
        return type;
    }

    public String getAnnotationType() {
        return annotationType;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public String getMethodContext() {
        return methodContext;
    }

    public boolean hasMethodContext() {
        return methodContext != null && !methodContext.trim().isEmpty();
    }

    /**
     * Creates a new condition with method context.
     */
    public ConditionalCondition withMethodContext(String methodName) {
        return new ConditionalCondition(type, annotationType, description, attributes, methodName);
    }

    /**
     * Gets a human-readable summary including context.
     */
    public String getFullDescription() {
        if (hasMethodContext()) {
            return String.format("[Method: %s] %s", methodContext, description);
        }
        return description;
    }

    /**
     * Gets the simple annotation name (without package).
     */
    public String getSimpleAnnotationType() {
        int lastDotIndex = annotationType.lastIndexOf('.');
        return lastDotIndex >= 0 ? annotationType.substring(lastDotIndex + 1) : annotationType;
    }

    @Override
    public String toString() {
        return "ConditionalCondition{" +
                "type=" + type +
                ", annotation='" + getSimpleAnnotationType() + '\'' +
                ", description='" + description + '\'' +
                (hasMethodContext() ? ", method='" + methodContext + '\'' : "") +
                '}';
    }
}