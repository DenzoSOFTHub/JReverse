package it.denzosoft.jreverse.core.model;

import java.util.Objects;

/**
 * Represents an organizational issue in package structure.
 */
public final class OrganizationIssue {
    
    private final String packageName;
    private final OrganizationIssueType issueType;
    private final ViolationSeverity severity;
    private final String description;
    private final String recommendation;
    
    private OrganizationIssue(Builder builder) {
        this.packageName = requireNonEmpty(builder.packageName, "packageName");
        this.issueType = Objects.requireNonNull(builder.issueType, "issueType cannot be null");
        this.severity = Objects.requireNonNull(builder.severity, "severity cannot be null");
        this.description = requireNonEmpty(builder.description, "description");
        this.recommendation = builder.recommendation;
    }
    
    public String getPackageName() {
        return packageName;
    }
    
    public OrganizationIssueType getIssueType() {
        return issueType;
    }
    
    public ViolationSeverity getSeverity() {
        return severity;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getRecommendation() {
        return recommendation;
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
        OrganizationIssue that = (OrganizationIssue) obj;
        return Objects.equals(packageName, that.packageName) &&
               issueType == that.issueType &&
               severity == that.severity &&
               Objects.equals(description, that.description) &&
               Objects.equals(recommendation, that.recommendation);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(packageName, issueType, severity, description, recommendation);
    }
    
    @Override
    public String toString() {
        return "OrganizationIssue{" +
                "package='" + packageName + '\'' +
                ", type=" + issueType +
                ", severity=" + severity +
                '}';
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String packageName;
        private OrganizationIssueType issueType;
        private ViolationSeverity severity = ViolationSeverity.MEDIUM;
        private String description;
        private String recommendation;
        
        public Builder packageName(String packageName) {
            this.packageName = packageName;
            return this;
        }
        
        public Builder issueType(OrganizationIssueType issueType) {
            this.issueType = issueType;
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
        
        public Builder recommendation(String recommendation) {
            this.recommendation = recommendation;
            return this;
        }
        
        public OrganizationIssue build() {
            return new OrganizationIssue(this);
        }
    }
}