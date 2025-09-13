package it.denzosoft.jreverse.core.model;

import java.util.Objects;

/**
 * Represents an issue found in repository layer analysis.
 */
public class RepositoryIssue {
    
    public enum IssueType {
        TOO_MANY_QUERY_METHODS("Too Many Query Methods"),
        MISSING_TRANSACTIONAL("Missing @Transactional Annotation"),
        NATIVE_QUERY_OVERUSE("Excessive Native Queries"),
        NO_CUSTOM_METHODS("No Custom Repository Methods"),
        INAPPROPRIATE_RETURN_TYPE("Inappropriate Return Type"),
        MISSING_JPA_ANNOTATIONS("Missing JPA Entity Annotations");
        
        private final String description;
        
        IssueType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    public enum Severity {
        INFO("Info"),
        WARNING("Warning"),
        ERROR("Error");
        
        private final String displayName;
        
        Severity(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    private final IssueType type;
    private final Severity severity;
    private final String repositoryName;
    private final String description;
    private final String recommendation;
    
    private RepositoryIssue(Builder builder) {
        this.type = builder.type;
        this.severity = builder.severity;
        this.repositoryName = builder.repositoryName;
        this.description = builder.description;
        this.recommendation = builder.recommendation;
    }
    
    public IssueType getType() { return type; }
    public Severity getSeverity() { return severity; }
    public String getRepositoryName() { return repositoryName; }
    public String getDescription() { return description; }
    public String getRecommendation() { return recommendation; }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private IssueType type;
        private Severity severity;
        private String repositoryName;
        private String description;
        private String recommendation;
        
        public Builder type(IssueType type) {
            this.type = type;
            return this;
        }
        
        public Builder severity(Severity severity) {
            this.severity = severity;
            return this;
        }
        
        public Builder repositoryName(String repositoryName) {
            this.repositoryName = repositoryName;
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
        
        public RepositoryIssue build() {
            Objects.requireNonNull(type, "Issue type is required");
            Objects.requireNonNull(severity, "Severity is required");
            return new RepositoryIssue(this);
        }
    }
    
    @Override
    public String toString() {
        return String.format("[%s] %s in %s", severity.getDisplayName(), type.getDescription(), repositoryName);
    }
}