package it.denzosoft.jreverse.core.model;

import java.util.Objects;

/**
 * Represents a violation of package naming conventions.
 */
public final class NamingViolation {
    
    private final String packageName;
    private final NamingViolationType violationType;
    private final ViolationSeverity severity;
    private final String description;
    private final String expectedPattern;
    private final String actualValue;
    private final String suggestedFix;
    
    private NamingViolation(Builder builder) {
        this.packageName = requireNonEmpty(builder.packageName, "packageName");
        this.violationType = Objects.requireNonNull(builder.violationType, "violationType cannot be null");
        this.severity = Objects.requireNonNull(builder.severity, "severity cannot be null");
        this.description = requireNonEmpty(builder.description, "description");
        this.expectedPattern = builder.expectedPattern;
        this.actualValue = builder.actualValue;
        this.suggestedFix = builder.suggestedFix;
    }
    
    public String getPackageName() {
        return packageName;
    }
    
    public NamingViolationType getViolationType() {
        return violationType;
    }
    
    public ViolationSeverity getSeverity() {
        return severity;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getExpectedPattern() {
        return expectedPattern;
    }
    
    public String getActualValue() {
        return actualValue;
    }
    
    public String getSuggestedFix() {
        return suggestedFix;
    }
    
    public boolean hasSuggestedFix() {
        return suggestedFix != null && !suggestedFix.isEmpty();
    }
    
    public boolean isHighSeverity() {
        return severity == ViolationSeverity.HIGH;
    }
    
    public boolean isMediumSeverity() {
        return severity == ViolationSeverity.MEDIUM;
    }
    
    public boolean isLowSeverity() {
        return severity == ViolationSeverity.LOW;
    }
    
    private String requireNonEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }
        return value.trim();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        NamingViolation that = (NamingViolation) obj;
        return Objects.equals(packageName, that.packageName) &&
               violationType == that.violationType &&
               severity == that.severity &&
               Objects.equals(description, that.description) &&
               Objects.equals(expectedPattern, that.expectedPattern) &&
               Objects.equals(actualValue, that.actualValue) &&
               Objects.equals(suggestedFix, that.suggestedFix);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(packageName, violationType, severity, description, 
                          expectedPattern, actualValue, suggestedFix);
    }
    
    @Override
    public String toString() {
        return "NamingViolation{" +
                "package='" + packageName + '\'' +
                ", type=" + violationType +
                ", severity=" + severity +
                ", description='" + description + '\'' +
                '}';
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String packageName;
        private NamingViolationType violationType;
        private ViolationSeverity severity;
        private String description;
        private String expectedPattern;
        private String actualValue;
        private String suggestedFix;
        
        public Builder packageName(String packageName) {
            this.packageName = packageName;
            return this;
        }
        
        public Builder violationType(NamingViolationType violationType) {
            this.violationType = violationType;
            return this;
        }
        
        public Builder severity(ViolationSeverity severity) {
            this.severity = severity;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder expectedPattern(String expectedPattern) {
            this.expectedPattern = expectedPattern;
            return this;
        }
        
        public Builder actualValue(String actualValue) {
            this.actualValue = actualValue;
            return this;
        }
        
        public Builder suggestedFix(String suggestedFix) {
            this.suggestedFix = suggestedFix;
            return this;
        }
        
        public NamingViolation build() {
            return new NamingViolation(this);
        }
    }
}