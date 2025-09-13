package it.denzosoft.jreverse.core.model;

import java.util.Objects;

/**
 * Represents an issue found during call graph analysis.
 */
public class CallGraphIssue {
    
    public enum Severity {
        INFO("Info"),
        WARNING("Warning"),
        HIGH("High"),
        ERROR("Error"),
        CRITICAL("Critical");
        
        private final String displayName;
        
        Severity(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum IssueType {
        N_PLUS_ONE_QUERY("N+1 Query Pattern", "Performance", true, false),
        CIRCULAR_DEPENDENCY("Circular Dependency", "Architecture", false, true),
        LAYER_VIOLATION("Layer Architecture Violation", "Architecture", false, true),
        EXCESSIVE_DEPTH("Excessive Call Depth", "Performance", true, false),
        EXCESSIVE_DB_CALLS("Excessive Database Calls", "Performance", true, false),
        MISSING_TRANSACTION("Missing Transaction Boundary", "Data Integrity", true, false),
        HIGH_COUPLING("High Component Coupling", "Architecture", false, true),
        UNHANDLED_EXCEPTION("Unhandled Exception Path", "Reliability", false, false),
        PERFORMANCE_HOTSPOT("Performance Hotspot", "Performance", true, false),
        EXTERNAL_CALL_WITHOUT_TIMEOUT("External Call Without Timeout", "Reliability", true, false),
        CLASS_LOADING_ERROR("Class Loading Error", "Technical", false, false),
        ENDPOINT_ANALYSIS_ERROR("Endpoint Analysis Error", "Technical", false, false),
        CALL_GRAPH_BUILD_ERROR("Call Graph Build Error", "Technical", false, false),
        METHOD_ANALYSIS_ERROR("Method Analysis Error", "Technical", false, false),
        DEEP_CALL_CHAIN("Deep Call Chain", "Performance", true, false),
        ANALYSIS_FAILURE("Analysis Failure", "Technical", false, false),
        ANALYSIS_ERROR("Analysis Error", "Technical", false, false);
        
        private final String displayName;
        private final String category;
        private final boolean performanceRelated;
        private final boolean architecturalViolation;
        
        IssueType(String displayName, String category, boolean performanceRelated, boolean architecturalViolation) {
            this.displayName = displayName;
            this.category = category;
            this.performanceRelated = performanceRelated;
            this.architecturalViolation = architecturalViolation;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getCategory() {
            return category;
        }
        
        public boolean isPerformanceRelated() {
            return performanceRelated;
        }
        
        public boolean isArchitecturalViolation() {
            return architecturalViolation;
        }
    }
    
    private final IssueType type;
    private final Severity severity;
    private final String location;
    private final String description;
    private final String recommendation;
    private final String endpointContext;
    private final int impactScore;
    
    private CallGraphIssue(Builder builder) {
        this.type = builder.type;
        this.severity = builder.severity;
        this.location = builder.location;
        this.description = builder.description;
        this.recommendation = builder.recommendation;
        this.endpointContext = builder.endpointContext;
        this.impactScore = builder.impactScore;
    }
    
    // Constructor semplice per backwards compatibility
    public CallGraphIssue(IssueType type, Severity severity, String description, String location) {
        this.type = type;
        this.severity = severity;
        this.description = description;
        this.location = location;
        this.recommendation = null;
        this.endpointContext = null;
        this.impactScore = 0;
    }
    
    public IssueType getType() {
        return type;
    }
    
    public Severity getSeverity() {
        return severity;
    }
    
    public String getLocation() {
        return location;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getRecommendation() {
        return recommendation;
    }
    
    public String getEndpointContext() {
        return endpointContext;
    }
    
    public int getImpactScore() {
        return impactScore;
    }
    
    /**
     * Gets a formatted display string for this issue.
     */
    public String getDisplayString() {
        StringBuilder display = new StringBuilder();
        display.append("[").append(severity.getDisplayName()).append("] ");
        display.append(type.getDisplayName());
        
        if (location != null) {
            display.append(" at ").append(location);
        }
        
        return display.toString();
    }
    
    /**
     * Gets the issue priority based on severity and impact.
     */
    public int getPriority() {
        int severityWeight;
        switch (severity) {
            case CRITICAL:
                severityWeight = 100;
                break;
            case HIGH:
                severityWeight = 75;
                break;
            case WARNING:
                severityWeight = 50;
                break;
            case INFO:
                severityWeight = 25;
                break;
            default:
                severityWeight = 0;
                break;
        }
        
        return severityWeight + impactScore;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private IssueType type;
        private Severity severity;
        private String location;
        private String description;
        private String recommendation;
        private String endpointContext;
        private int impactScore = 0;
        
        public Builder type(IssueType type) {
            this.type = type;
            return this;
        }
        
        public Builder severity(Severity severity) {
            this.severity = severity;
            return this;
        }
        
        public Builder location(String location) {
            this.location = location;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder message(String message) {
            this.description = message;
            return this;
        }
        
        public Builder recommendation(String recommendation) {
            this.recommendation = recommendation;
            return this;
        }
        
        public Builder endpointContext(String endpointContext) {
            this.endpointContext = endpointContext;
            return this;
        }
        
        public Builder impactScore(int impactScore) {
            this.impactScore = Math.max(0, Math.min(100, impactScore));
            return this;
        }
        
        public CallGraphIssue build() {
            Objects.requireNonNull(type, "Issue type is required");
            Objects.requireNonNull(severity, "Severity is required");
            Objects.requireNonNull(description, "Description is required");
            
            return new CallGraphIssue(this);
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
        CallGraphIssue that = (CallGraphIssue) obj;
        return type == that.type &&
               Objects.equals(location, that.location) &&
               Objects.equals(endpointContext, that.endpointContext);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(type, location, endpointContext);
    }
}