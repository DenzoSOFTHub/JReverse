package it.denzosoft.jreverse.core.model;

import java.util.Objects;

/**
 * Represents an issue found in service layer analysis.
 */
public class ServiceLayerIssue {
    
    public enum IssueType {
        TOO_MANY_DEPENDENCIES("Too Many Dependencies"),
        MISSING_TRANSACTIONAL("Missing @Transactional Annotation"),
        INAPPROPRIATE_SCOPE("Inappropriate Bean Scope"),
        CIRCULAR_DEPENDENCY("Circular Dependency"),
        SERVICE_WITHOUT_INTERFACE("Service Without Interface"),
        BUSINESS_LOGIC_IN_CONTROLLER("Business Logic in Controller");
        
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
    private final String className;
    private final String description;
    private final String recommendation;
    
    private ServiceLayerIssue(Builder builder) {
        this.type = builder.type;
        this.severity = builder.severity;
        this.className = builder.className;
        this.description = builder.description;
        this.recommendation = builder.recommendation;
    }
    
    public IssueType getType() { return type; }
    public Severity getSeverity() { return severity; }
    public String getClassName() { return className; }
    public String getDescription() { return description; }
    public String getRecommendation() { return recommendation; }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private IssueType type;
        private Severity severity;
        private String className;
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
        
        public Builder className(String className) {
            this.className = className;
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
        
        public ServiceLayerIssue build() {
            Objects.requireNonNull(type, "Issue type is required");
            Objects.requireNonNull(severity, "Severity is required");
            return new ServiceLayerIssue(this);
        }
    }
    
    @Override
    public String toString() {
        return String.format("[%s] %s in %s", severity.getDisplayName(), type.getDescription(), className);
    }
}