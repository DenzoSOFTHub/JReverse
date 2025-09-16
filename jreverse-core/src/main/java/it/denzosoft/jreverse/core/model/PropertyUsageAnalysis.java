package it.denzosoft.jreverse.core.model;

import java.util.*;

/**
 * Analysis of property usage patterns, cross-referencing property definitions
 * with their usage in code through @Value annotations and @ConfigurationProperties.
 */
public class PropertyUsageAnalysis {

    private final Set<String> definedProperties;
    private final Set<String> usedProperties;
    private final Set<String> unusedProperties;
    private final Set<String> undefinedProperties;
    private final double usageComplexity;
    private final Map<String, Integer> propertyUsageCount;

    private PropertyUsageAnalysis(Builder builder) {
        this.definedProperties = Collections.unmodifiableSet(new HashSet<>(builder.definedProperties));
        this.usedProperties = Collections.unmodifiableSet(new HashSet<>(builder.usedProperties));
        this.unusedProperties = Collections.unmodifiableSet(new HashSet<>(builder.unusedProperties));
        this.undefinedProperties = Collections.unmodifiableSet(new HashSet<>(builder.undefinedProperties));
        this.usageComplexity = builder.usageComplexity;
        this.propertyUsageCount = Collections.unmodifiableMap(new HashMap<>(builder.propertyUsageCount));
    }

    public Set<String> getDefinedProperties() {
        return definedProperties;
    }

    public Set<String> getUsedProperties() {
        return usedProperties;
    }

    public Set<String> getUnusedProperties() {
        return unusedProperties;
    }

    public Set<String> getUndefinedProperties() {
        return undefinedProperties;
    }

    public double getUsageComplexity() {
        return usageComplexity;
    }

    public Map<String, Integer> getPropertyUsageCount() {
        return propertyUsageCount;
    }

    /**
     * Calculates the property usage efficiency (0-100%, higher is better).
     */
    public double calculateUsageEfficiency() {
        if (definedProperties.isEmpty()) return 100.0;

        int usedCount = usedProperties.size();
        int totalCount = definedProperties.size();

        return (double) usedCount / totalCount * 100.0;
    }

    /**
     * Calculates the property definition coverage (0-100%, higher is better).
     */
    public double calculateDefinitionCoverage() {
        int totalUsedProperties = usedProperties.size() + undefinedProperties.size();
        if (totalUsedProperties == 0) return 100.0;

        int definedUsedProperties = usedProperties.size();
        return (double) definedUsedProperties / totalUsedProperties * 100.0;
    }

    /**
     * Gets usage count for a specific property.
     */
    public int getUsageCount(String propertyName) {
        return propertyUsageCount.getOrDefault(propertyName, 0);
    }

    /**
     * Checks if a property is defined but unused.
     */
    public boolean isUnused(String propertyName) {
        return unusedProperties.contains(propertyName);
    }

    /**
     * Checks if a property is used but undefined.
     */
    public boolean isUndefined(String propertyName) {
        return undefinedProperties.contains(propertyName);
    }

    /**
     * Gets properties ordered by usage frequency.
     */
    public List<Map.Entry<String, Integer>> getPropertiesByUsageFrequency() {
        return propertyUsageCount.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Categorizes usage complexity level.
     */
    public UsageComplexityLevel getComplexityLevel() {
        if (usageComplexity < 5.0) return UsageComplexityLevel.LOW;
        if (usageComplexity < 15.0) return UsageComplexityLevel.MEDIUM;
        if (usageComplexity < 30.0) return UsageComplexityLevel.HIGH;
        return UsageComplexityLevel.VERY_HIGH;
    }

    /**
     * Checks if there are configuration issues.
     */
    public boolean hasConfigurationIssues() {
        return !undefinedProperties.isEmpty() ||
               calculateUsageEfficiency() < 50.0 ||
               calculateDefinitionCoverage() < 80.0;
    }

    /**
     * Gets a summary of configuration health.
     */
    public ConfigurationHealth getConfigurationHealth() {
        double efficiency = calculateUsageEfficiency();
        double coverage = calculateDefinitionCoverage();
        boolean hasIssues = hasConfigurationIssues();

        if (!hasIssues && efficiency >= 80.0 && coverage >= 95.0) {
            return ConfigurationHealth.EXCELLENT;
        } else if (efficiency >= 60.0 && coverage >= 85.0) {
            return ConfigurationHealth.GOOD;
        } else if (efficiency >= 40.0 && coverage >= 70.0) {
            return ConfigurationHealth.FAIR;
        } else {
            return ConfigurationHealth.POOR;
        }
    }

    public enum UsageComplexityLevel {
        LOW("Simple property configuration"),
        MEDIUM("Moderate property complexity"),
        HIGH("Complex property configuration"),
        VERY_HIGH("Very complex property configuration with many dependencies");

        private final String description;

        UsageComplexityLevel(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum ConfigurationHealth {
        EXCELLENT("Configuration is well-organized and efficiently used"),
        GOOD("Configuration is mostly well-organized with minor issues"),
        FAIR("Configuration has some issues that should be addressed"),
        POOR("Configuration has significant issues requiring attention");

        private final String description;

        ConfigurationHealth(String description) {
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
        private Set<String> definedProperties = new HashSet<>();
        private Set<String> usedProperties = new HashSet<>();
        private Set<String> unusedProperties = new HashSet<>();
        private Set<String> undefinedProperties = new HashSet<>();
        private double usageComplexity = 0.0;
        private Map<String, Integer> propertyUsageCount = new HashMap<>();

        public Builder definedProperties(Set<String> definedProperties) {
            this.definedProperties = new HashSet<>(definedProperties != null ? definedProperties : Collections.emptySet());
            return this;
        }

        public Builder addDefinedProperty(String propertyName) {
            if (propertyName != null) {
                this.definedProperties.add(propertyName);
            }
            return this;
        }

        public Builder usedProperties(Set<String> usedProperties) {
            this.usedProperties = new HashSet<>(usedProperties != null ? usedProperties : Collections.emptySet());
            return this;
        }

        public Builder addUsedProperty(String propertyName) {
            if (propertyName != null) {
                this.usedProperties.add(propertyName);
                this.propertyUsageCount.put(propertyName,
                    this.propertyUsageCount.getOrDefault(propertyName, 0) + 1);
            }
            return this;
        }

        public Builder unusedProperties(Set<String> unusedProperties) {
            this.unusedProperties = new HashSet<>(unusedProperties != null ? unusedProperties : Collections.emptySet());
            return this;
        }

        public Builder addUnusedProperty(String propertyName) {
            if (propertyName != null) {
                this.unusedProperties.add(propertyName);
            }
            return this;
        }

        public Builder undefinedProperties(Set<String> undefinedProperties) {
            this.undefinedProperties = new HashSet<>(undefinedProperties != null ? undefinedProperties : Collections.emptySet());
            return this;
        }

        public Builder addUndefinedProperty(String propertyName) {
            if (propertyName != null) {
                this.undefinedProperties.add(propertyName);
            }
            return this;
        }

        public Builder usageComplexity(double usageComplexity) {
            this.usageComplexity = Math.max(0.0, usageComplexity);
            return this;
        }

        public Builder propertyUsageCount(Map<String, Integer> propertyUsageCount) {
            this.propertyUsageCount = new HashMap<>(propertyUsageCount != null ? propertyUsageCount : Collections.emptyMap());
            return this;
        }

        public Builder addPropertyUsage(String propertyName, int count) {
            if (propertyName != null && count > 0) {
                this.propertyUsageCount.put(propertyName, count);
            }
            return this;
        }

        public PropertyUsageAnalysis build() {
            return new PropertyUsageAnalysis(this);
        }
    }

    @Override
    public String toString() {
        return String.format("PropertyUsageAnalysis{defined=%d, used=%d, unused=%d, undefined=%d, efficiency=%.1f%%, coverage=%.1f%%, health=%s}",
            definedProperties.size(), usedProperties.size(), unusedProperties.size(), undefinedProperties.size(),
            calculateUsageEfficiency(), calculateDefinitionCoverage(), getConfigurationHealth());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PropertyUsageAnalysis that = (PropertyUsageAnalysis) obj;
        return Double.compare(that.usageComplexity, usageComplexity) == 0 &&
               Objects.equals(definedProperties, that.definedProperties) &&
               Objects.equals(usedProperties, that.usedProperties) &&
               Objects.equals(unusedProperties, that.unusedProperties) &&
               Objects.equals(undefinedProperties, that.undefinedProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(definedProperties, usedProperties, unusedProperties, undefinedProperties, usageComplexity);
    }
}