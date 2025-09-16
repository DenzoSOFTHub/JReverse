package it.denzosoft.jreverse.analyzer.configuration;

import it.denzosoft.jreverse.core.model.BeanDefinitionInfo;

import java.util.Collections;
import java.util.List;

/**
 * Represents a bean override situation where one bean definition overrides others.
 */
public class BeanOverride {

    private final OverrideType type;
    private final String identifier; // Bean name or type
    private final BeanDefinitionInfo winningBean;
    private final List<BeanDefinitionInfo> overriddenBeans;
    private final BeanOverrideDetector.OverrideReason reason;
    private final String description;

    public BeanOverride(OverrideType type,
                       String identifier,
                       BeanDefinitionInfo winningBean,
                       List<BeanDefinitionInfo> overriddenBeans,
                       BeanOverrideDetector.OverrideReason reason,
                       String description) {
        this.type = type;
        this.identifier = identifier;
        this.winningBean = winningBean;
        this.overriddenBeans = Collections.unmodifiableList(overriddenBeans);
        this.reason = reason;
        this.description = description;
    }

    public OverrideType getType() {
        return type;
    }

    public String getIdentifier() {
        return identifier;
    }

    public BeanDefinitionInfo getWinningBean() {
        return winningBean;
    }

    public List<BeanDefinitionInfo> getOverriddenBeans() {
        return overriddenBeans;
    }

    public BeanOverrideDetector.OverrideReason getReason() {
        return reason;
    }

    public String getDescription() {
        return description;
    }

    public int getOverriddenCount() {
        return overriddenBeans.size();
    }

    public boolean isExplicitOverride() {
        return reason == BeanOverrideDetector.OverrideReason.PRIMARY_ANNOTATION ||
               reason == BeanOverrideDetector.OverrideReason.QUALIFIER_MATCH;
    }

    public boolean isPotentialProblem() {
        return reason == BeanOverrideDetector.OverrideReason.MULTIPLE_PRIMARY ||
               reason == BeanOverrideDetector.OverrideReason.DECLARATION_ORDER;
    }

    /**
     * Gets a summary of all involved beans.
     */
    public String getBeanSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Winner: ").append(winningBean.getBeanName())
               .append(" (").append(winningBean.getDeclaringClass()).append(")");

        if (!overriddenBeans.isEmpty()) {
            summary.append(", Overridden: ");
            for (int i = 0; i < overriddenBeans.size(); i++) {
                if (i > 0) summary.append(", ");
                BeanDefinitionInfo bean = overriddenBeans.get(i);
                summary.append(bean.getBeanName())
                       .append(" (").append(bean.getDeclaringClass()).append(")");
            }
        }

        return summary.toString();
    }

    @Override
    public String toString() {
        return "BeanOverride{" +
                "type=" + type +
                ", identifier='" + identifier + '\'' +
                ", reason=" + reason +
                ", overriddenCount=" + overriddenBeans.size() +
                '}';
    }

    /**
     * Type of bean override.
     */
    public enum OverrideType {
        NAME,  // Same bean name, different definitions
        TYPE   // Same bean type, different names
    }
}