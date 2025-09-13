package it.denzosoft.jreverse.core.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Result of autowiring analysis containing information about dependency injection patterns
 * and potential autowiring issues in a Spring Boot application.
 */
public class AutowiredAnalysisResult {
    
    private final List<AutowiredDependency> dependencies;
    private final Map<String, List<AutowiredDependency>> dependenciesByClass;
    private final Map<String, Integer> injectionTypeStatistics;
    private final List<AutowiringIssue> issues;
    private final AutowiredSummary summary;
    
    private AutowiredAnalysisResult(Builder builder) {
        this.dependencies = List.copyOf(builder.dependencies);
        this.dependenciesByClass = Map.copyOf(builder.dependenciesByClass);
        this.injectionTypeStatistics = Map.copyOf(builder.injectionTypeStatistics);
        this.issues = List.copyOf(builder.issues);
        this.summary = builder.summary;
    }
    
    /**
     * Gets all autowired dependencies found in the analysis.
     */
    public List<AutowiredDependency> getDependencies() {
        return dependencies;
    }
    
    /**
     * Gets dependencies grouped by class name.
     */
    public Map<String, List<AutowiredDependency>> getDependenciesByClass() {
        return dependenciesByClass;
    }
    
    /**
     * Gets statistics about injection types used.
     */
    public Map<String, Integer> getInjectionTypeStatistics() {
        return injectionTypeStatistics;
    }
    
    /**
     * Gets potential autowiring issues discovered during analysis.
     */
    public List<AutowiringIssue> getIssues() {
        return issues;
    }
    
    /**
     * Gets the summary of the autowiring analysis.
     */
    public AutowiredSummary getSummary() {
        return summary;
    }
    
    /**
     * Gets dependencies for a specific class.
     */
    public List<AutowiredDependency> getDependenciesForClass(String className) {
        return dependenciesByClass.getOrDefault(className, Collections.emptyList());
    }
    
    /**
     * Gets all classes that have autowired dependencies.
     */
    public Set<String> getClassesWithDependencies() {
        return dependenciesByClass.keySet();
    }
    
    /**
     * Checks if any autowiring issues were found.
     */
    public boolean hasIssues() {
        return !issues.isEmpty();
    }
    
    /**
     * Gets the total number of autowired dependencies.
     */
    public int getTotalDependencies() {
        return dependencies.size();
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private List<AutowiredDependency> dependencies = Collections.emptyList();
        private Map<String, List<AutowiredDependency>> dependenciesByClass = Collections.emptyMap();
        private Map<String, Integer> injectionTypeStatistics = Collections.emptyMap();
        private List<AutowiringIssue> issues = Collections.emptyList();
        private AutowiredSummary summary;
        
        public Builder dependencies(List<AutowiredDependency> dependencies) {
            this.dependencies = dependencies != null ? dependencies : Collections.emptyList();
            return this;
        }
        
        public Builder dependenciesByClass(Map<String, List<AutowiredDependency>> dependenciesByClass) {
            this.dependenciesByClass = dependenciesByClass != null ? dependenciesByClass : Collections.emptyMap();
            return this;
        }
        
        public Builder injectionTypeStatistics(Map<String, Integer> injectionTypeStatistics) {
            this.injectionTypeStatistics = injectionTypeStatistics != null ? injectionTypeStatistics : Collections.emptyMap();
            return this;
        }
        
        public Builder issues(List<AutowiringIssue> issues) {
            this.issues = issues != null ? issues : Collections.emptyList();
            return this;
        }
        
        public Builder summary(AutowiredSummary summary) {
            this.summary = summary;
            return this;
        }
        
        public AutowiredAnalysisResult build() {
            if (summary == null) {
                throw new IllegalStateException("Summary is required");
            }
            return new AutowiredAnalysisResult(this);
        }
    }
}