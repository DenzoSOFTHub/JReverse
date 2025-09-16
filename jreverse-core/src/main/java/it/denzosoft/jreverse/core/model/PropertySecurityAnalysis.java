package it.denzosoft.jreverse.core.model;

import java.util.*;

/**
 * Security analysis of property configurations, identifying sensitive properties,
 * hardcoded secrets, and properties that should be externalized.
 */
public class PropertySecurityAnalysis {

    private final Set<String> sensitiveProperties;
    private final Set<String> hardcodedSecrets;
    private final Set<String> externalizableProperties;
    private final Map<String, SecurityRiskLevel> riskAssessment;

    private PropertySecurityAnalysis(Builder builder) {
        this.sensitiveProperties = Collections.unmodifiableSet(new HashSet<>(builder.sensitiveProperties));
        this.hardcodedSecrets = Collections.unmodifiableSet(new HashSet<>(builder.hardcodedSecrets));
        this.externalizableProperties = Collections.unmodifiableSet(new HashSet<>(builder.externalizableProperties));
        this.riskAssessment = Collections.unmodifiableMap(new HashMap<>(builder.riskAssessment));
    }

    public Set<String> getSensitiveProperties() {
        return sensitiveProperties;
    }

    public Set<String> getHardcodedSecrets() {
        return hardcodedSecrets;
    }

    public Set<String> getExternalizableProperties() {
        return externalizableProperties;
    }

    public Map<String, SecurityRiskLevel> getRiskAssessment() {
        return riskAssessment;
    }

    /**
     * Checks if a property is considered sensitive.
     */
    public boolean isSensitive(String propertyName) {
        return sensitiveProperties.contains(propertyName);
    }

    /**
     * Checks if a property contains hardcoded secrets.
     */
    public boolean hasHardcodedSecret(String propertyName) {
        return hardcodedSecrets.contains(propertyName);
    }

    /**
     * Checks if a property should be externalized.
     */
    public boolean shouldBeExternalized(String propertyName) {
        return externalizableProperties.contains(propertyName);
    }

    /**
     * Gets the security risk level for a property.
     */
    public SecurityRiskLevel getRiskLevel(String propertyName) {
        return riskAssessment.getOrDefault(propertyName, SecurityRiskLevel.LOW);
    }

    /**
     * Gets all properties with a specific risk level.
     */
    public Set<String> getPropertiesWithRiskLevel(SecurityRiskLevel riskLevel) {
        Set<String> result = new HashSet<>();
        for (Map.Entry<String, SecurityRiskLevel> entry : riskAssessment.entrySet()) {
            if (entry.getValue() == riskLevel) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    /**
     * Calculates an overall security score (0-100, higher is more secure).
     */
    public double calculateSecurityScore() {
        int totalProperties = sensitiveProperties.size() + externalizableProperties.size();
        if (totalProperties == 0) return 100.0;

        int securityIssues = hardcodedSecrets.size();
        int highRiskProperties = getPropertiesWithRiskLevel(SecurityRiskLevel.HIGH).size();
        int mediumRiskProperties = getPropertiesWithRiskLevel(SecurityRiskLevel.MEDIUM).size();

        double penalty = (securityIssues * 30.0) + (highRiskProperties * 20.0) + (mediumRiskProperties * 10.0);
        double score = 100.0 - (penalty / totalProperties * 100.0);

        return Math.max(0.0, Math.min(100.0, score));
    }

    /**
     * Checks if there are any security concerns.
     */
    public boolean hasSecurityConcerns() {
        return !hardcodedSecrets.isEmpty() ||
               !getPropertiesWithRiskLevel(SecurityRiskLevel.HIGH).isEmpty();
    }

    public enum SecurityRiskLevel {
        LOW("Low risk - standard configuration property"),
        MEDIUM("Medium risk - environment-specific or sensitive configuration"),
        HIGH("High risk - contains secrets or credentials"),
        CRITICAL("Critical risk - hardcoded secrets or private keys");

        private final String description;

        SecurityRiskLevel(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Set<String> sensitiveProperties = new HashSet<>();
        private Set<String> hardcodedSecrets = new HashSet<>();
        private Set<String> externalizableProperties = new HashSet<>();
        private Map<String, SecurityRiskLevel> riskAssessment = new HashMap<>();

        public Builder sensitiveProperties(Set<String> sensitiveProperties) {
            this.sensitiveProperties = new HashSet<>(sensitiveProperties != null ? sensitiveProperties : Collections.emptySet());
            return this;
        }

        public Builder addSensitiveProperty(String propertyName) {
            if (propertyName != null) {
                this.sensitiveProperties.add(propertyName);
            }
            return this;
        }

        public Builder hardcodedSecrets(Set<String> hardcodedSecrets) {
            this.hardcodedSecrets = new HashSet<>(hardcodedSecrets != null ? hardcodedSecrets : Collections.emptySet());
            return this;
        }

        public Builder addHardcodedSecret(String propertyName) {
            if (propertyName != null) {
                this.hardcodedSecrets.add(propertyName);
            }
            return this;
        }

        public Builder externalizableProperties(Set<String> externalizableProperties) {
            this.externalizableProperties = new HashSet<>(externalizableProperties != null ? externalizableProperties : Collections.emptySet());
            return this;
        }

        public Builder addExternalizableProperty(String propertyName) {
            if (propertyName != null) {
                this.externalizableProperties.add(propertyName);
            }
            return this;
        }

        public Builder riskAssessment(Map<String, SecurityRiskLevel> riskAssessment) {
            this.riskAssessment = new HashMap<>(riskAssessment != null ? riskAssessment : Collections.emptyMap());
            return this;
        }

        public Builder addRiskAssessment(String propertyName, SecurityRiskLevel riskLevel) {
            if (propertyName != null && riskLevel != null) {
                this.riskAssessment.put(propertyName, riskLevel);
            }
            return this;
        }

        public PropertySecurityAnalysis build() {
            // Auto-assign risk levels based on categorization
            for (String property : hardcodedSecrets) {
                riskAssessment.put(property, SecurityRiskLevel.CRITICAL);
            }
            for (String property : sensitiveProperties) {
                riskAssessment.putIfAbsent(property, SecurityRiskLevel.HIGH);
            }
            for (String property : externalizableProperties) {
                riskAssessment.putIfAbsent(property, SecurityRiskLevel.MEDIUM);
            }

            return new PropertySecurityAnalysis(this);
        }
    }

    @Override
    public String toString() {
        return String.format("PropertySecurityAnalysis{sensitive=%d, secrets=%d, externalizable=%d, score=%.1f}",
            sensitiveProperties.size(), hardcodedSecrets.size(), externalizableProperties.size(), calculateSecurityScore());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PropertySecurityAnalysis that = (PropertySecurityAnalysis) obj;
        return Objects.equals(sensitiveProperties, that.sensitiveProperties) &&
               Objects.equals(hardcodedSecrets, that.hardcodedSecrets) &&
               Objects.equals(externalizableProperties, that.externalizableProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sensitiveProperties, hardcodedSecrets, externalizableProperties);
    }
}