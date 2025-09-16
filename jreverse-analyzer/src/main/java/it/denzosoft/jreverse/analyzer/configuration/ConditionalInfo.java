package it.denzosoft.jreverse.analyzer.configuration;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Information about conditional logic applied to a configuration class or bean.
 */
public class ConditionalInfo {

    private final String className;
    private final List<ConditionalCondition> conditions;

    public ConditionalInfo(String className, List<ConditionalCondition> conditions) {
        this.className = className;
        this.conditions = Collections.unmodifiableList(conditions);
    }

    public String getClassName() {
        return className;
    }

    public List<ConditionalCondition> getConditions() {
        return conditions;
    }

    public boolean hasConditions() {
        return !conditions.isEmpty();
    }

    public int getConditionCount() {
        return conditions.size();
    }

    /**
     * Gets a summary of all conditions as a single string.
     */
    public String getConditionSummary() {
        return conditions.stream()
            .map(ConditionalCondition::getDescription)
            .collect(Collectors.joining(" AND "));
    }

    /**
     * Gets conditions of a specific type.
     */
    public List<ConditionalCondition> getConditionsOfType(ConditionalAnalyzer.ConditionalType type) {
        return conditions.stream()
            .filter(condition -> condition.getType() == type)
            .collect(Collectors.toList());
    }

    /**
     * Checks if this configuration has property-based conditions.
     */
    public boolean hasPropertyConditions() {
        return conditions.stream()
            .anyMatch(condition -> condition.getType() == ConditionalAnalyzer.ConditionalType.PROPERTY);
    }

    /**
     * Checks if this configuration has class-based conditions.
     */
    public boolean hasClassConditions() {
        return conditions.stream()
            .anyMatch(condition -> condition.getType() == ConditionalAnalyzer.ConditionalType.CLASS ||
                                 condition.getType() == ConditionalAnalyzer.ConditionalType.MISSING_CLASS);
    }

    /**
     * Checks if this configuration has bean-based conditions.
     */
    public boolean hasBeanConditions() {
        return conditions.stream()
            .anyMatch(condition -> condition.getType() == ConditionalAnalyzer.ConditionalType.BEAN ||
                                 condition.getType() == ConditionalAnalyzer.ConditionalType.MISSING_BEAN);
    }

    /**
     * Gets all property names referenced in property conditions.
     */
    public List<String> getReferencedProperties() {
        return conditions.stream()
            .filter(condition -> condition.getType() == ConditionalAnalyzer.ConditionalType.PROPERTY)
            .map(condition -> condition.getAttributes().get("property"))
            .filter(property -> property != null && !property.toString().isEmpty())
            .map(Object::toString)
            .collect(Collectors.toList());
    }

    /**
     * Gets all class names referenced in class conditions.
     */
    public List<String> getReferencedClasses() {
        return conditions.stream()
            .filter(condition -> condition.getType() == ConditionalAnalyzer.ConditionalType.CLASS ||
                               condition.getType() == ConditionalAnalyzer.ConditionalType.MISSING_CLASS)
            .map(condition -> {
                String classes = condition.getAttributes().get("requiredClasses");
                if (classes == null) {
                    classes = condition.getAttributes().get("excludedClasses");
                }
                return classes != null ? classes.toString() : "";
            })
            .filter(classes -> !classes.isEmpty())
            .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "ConditionalInfo{" +
                "className='" + className + '\'' +
                ", conditionCount=" + conditions.size() +
                '}';
    }
}