package it.denzosoft.jreverse.analyzer.configuration;

import it.denzosoft.jreverse.core.model.BeanDefinitionInfo;

import java.util.Collections;
import java.util.List;

/**
 * Represents a bean configuration conflict that may cause runtime issues.
 */
public class BeanConflict {

    private final ConflictType type;
    private final String identifier; // Bean name, type, or qualifier
    private final List<BeanDefinitionInfo> conflictingBeans;
    private final String description;

    public BeanConflict(ConflictType type,
                       String identifier,
                       List<BeanDefinitionInfo> conflictingBeans,
                       String description) {
        this.type = type;
        this.identifier = identifier;
        this.conflictingBeans = Collections.unmodifiableList(conflictingBeans);
        this.description = description;
    }

    public ConflictType getType() {
        return type;
    }

    public String getIdentifier() {
        return identifier;
    }

    public List<BeanDefinitionInfo> getConflictingBeans() {
        return conflictingBeans;
    }

    public String getDescription() {
        return description;
    }

    public int getConflictingBeanCount() {
        return conflictingBeans.size();
    }

    public Severity getSeverity() {
        switch (type) {
            case MULTIPLE_PRIMARY:
                return Severity.HIGH;
            case SCOPE_MISMATCH:
                return Severity.MEDIUM;
            case DUPLICATE_QUALIFIER:
                return Severity.LOW;
            default:
                return Severity.LOW;
        }
    }

    /**
     * Gets detailed information about the conflicting beans.
     */
    public String getConflictDetails() {
        StringBuilder details = new StringBuilder();
        details.append(description).append(":\n");

        for (int i = 0; i < conflictingBeans.size(); i++) {
            BeanDefinitionInfo bean = conflictingBeans.get(i);
            details.append("  ").append(i + 1).append(". ")
                   .append(bean.getBeanName())
                   .append(" (").append(bean.getDeclaringClass()).append(")")
                   .append(" - Scope: ").append(bean.getScope())
                   .append(bean.isPrimary() ? " [PRIMARY]" : "")
                   .append(bean.isLazy() ? " [LAZY]" : "");

            if (!bean.getQualifiers().isEmpty()) {
                details.append(" - Qualifiers: ").append(bean.getQualifiers());
            }

            if (!bean.getProfiles().isEmpty()) {
                details.append(" - Profiles: ").append(bean.getProfiles());
            }

            if (i < conflictingBeans.size() - 1) {
                details.append("\n");
            }
        }

        return details.toString();
    }

    /**
     * Gets resolution suggestions for this conflict.
     */
    public String getResolutionSuggestion() {
        switch (type) {
            case MULTIPLE_PRIMARY:
                return "Remove @Primary from all but one bean, or use @Qualifier to distinguish them.";
            case DUPLICATE_QUALIFIER:
                return "Use unique @Qualifier values or ensure only one bean should have this qualifier.";
            case SCOPE_MISMATCH:
                return "Ensure all beans with the same name have the same scope, or use different names.";
            default:
                return "Review bean configuration to resolve the conflict.";
        }
    }

    @Override
    public String toString() {
        return "BeanConflict{" +
                "type=" + type +
                ", identifier='" + identifier + '\'' +
                ", severity=" + getSeverity() +
                ", conflictingBeans=" + conflictingBeans.size() +
                '}';
    }

    /**
     * Type of bean conflict.
     */
    public enum ConflictType {
        MULTIPLE_PRIMARY,    // Multiple @Primary beans of the same type
        DUPLICATE_QUALIFIER, // Multiple beans with the same @Qualifier
        SCOPE_MISMATCH      // Same bean name with different scopes
    }

    /**
     * Severity of the conflict.
     */
    public enum Severity {
        LOW,     // Warning - may work but could be confusing
        MEDIUM,  // Potential runtime issue in some scenarios
        HIGH     // Likely to cause runtime exceptions
    }
}