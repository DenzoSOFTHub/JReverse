package it.denzosoft.jreverse.core.model;

import java.util.Objects;

/**
 * Represents a potential issue with autowiring configuration or usage patterns.
 */
public class AutowiringIssue {
    
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
    
    public enum IssueType {
        FIELD_INJECTION("Field injection discouraged"),
        CIRCULAR_DEPENDENCY("Potential circular dependency"),
        MISSING_QUALIFIER("Multiple beans without qualifier"),
        OPTIONAL_DEPENDENCY("Optional dependency pattern"),
        COLLECTION_INJECTION("Collection injection pattern"),
        DEPRECATED_ANNOTATION("Deprecated injection annotation");
        
        private final String description;
        
        IssueType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    private final IssueType type;
    private final Severity severity;
    private final String className;
    private final String location;
    private final String message;
    private final String recommendation;
    
    private AutowiringIssue(Builder builder) {
        this.type = builder.type;
        this.severity = builder.severity;
        this.className = builder.className;
        this.location = builder.location;
        this.message = builder.message;
        this.recommendation = builder.recommendation;
    }
    
    public IssueType getType() {
        return type;
    }
    
    public Severity getSeverity() {
        return severity;
    }
    
    public String getClassName() {
        return className;
    }
    
    public String getLocation() {
        return location;
    }
    
    public String getMessage() {
        return message;
    }
    
    public String getRecommendation() {
        return recommendation;
    }
    
    /**
     * Gets the simple name of the class (without package).
     */
    public String getSimpleClassName() {
        if (className == null) return null;
        int lastDot = className.lastIndexOf('.');
        return lastDot >= 0 ? className.substring(lastDot + 1) : className;
    }
    
    /**
     * Gets a formatted display string for this issue.
     */
    public String getDisplayString() {
        StringBuilder display = new StringBuilder();
        display.append("[").append(severity.getDisplayName()).append("] ");
        display.append(type.getDescription());
        
        if (className != null) {
            display.append(" in ").append(getSimpleClassName());
        }
        
        if (location != null) {
            display.append(" at ").append(location);
        }
        
        return display.toString();
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private IssueType type;
        private Severity severity;
        private String className;
        private String location;
        private String message;
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
        
        public Builder location(String location) {
            this.location = location;
            return this;
        }
        
        public Builder message(String message) {
            this.message = message;
            return this;
        }
        
        public Builder recommendation(String recommendation) {
            this.recommendation = recommendation;
            return this;
        }
        
        public AutowiringIssue build() {
            Objects.requireNonNull(type, "Issue type is required");
            Objects.requireNonNull(severity, "Severity is required");
            Objects.requireNonNull(message, "Message is required");
            
            return new AutowiringIssue(this);
        }
    }
    
    @Override
    public String toString() {
        return getDisplayString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AutowiringIssue that = (AutowiringIssue) obj;
        return type == that.type &&
               Objects.equals(className, that.className) &&
               Objects.equals(location, that.location);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(type, className, location);
    }
}