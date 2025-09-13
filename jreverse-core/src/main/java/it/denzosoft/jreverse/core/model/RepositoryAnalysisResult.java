package it.denzosoft.jreverse.core.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Result of repository analysis containing information about @Repository components,
 * JPA repositories, and data access layer patterns.
 */
public class RepositoryAnalysisResult {
    
    private final List<RepositoryComponentInfo> repositories;
    private final List<JpaRepositoryInfo> jpaRepositories;
    private final Map<String, List<RepositoryComponentInfo>> repositoriesByPackage;
    private final RepositoryMetrics metrics;
    private final List<RepositoryIssue> issues;
    private final RepositorySummary summary;
    
    private RepositoryAnalysisResult(Builder builder) {
        this.repositories = List.copyOf(builder.repositories);
        this.jpaRepositories = List.copyOf(builder.jpaRepositories);
        this.repositoriesByPackage = Map.copyOf(builder.repositoriesByPackage);
        this.metrics = builder.metrics;
        this.issues = List.copyOf(builder.issues);
        this.summary = builder.summary;
    }
    
    public List<RepositoryComponentInfo> getRepositories() {
        return repositories;
    }
    
    public List<JpaRepositoryInfo> getJpaRepositories() {
        return jpaRepositories;
    }
    
    public Map<String, List<RepositoryComponentInfo>> getRepositoriesByPackage() {
        return repositoriesByPackage;
    }
    
    public RepositoryMetrics getMetrics() {
        return metrics;
    }
    
    public List<RepositoryIssue> getIssues() {
        return issues;
    }
    
    public RepositorySummary getSummary() {
        return summary;
    }
    
    /**
     * Gets repositories for a specific package.
     */
    public List<RepositoryComponentInfo> getRepositoriesInPackage(String packageName) {
        return repositoriesByPackage.getOrDefault(packageName, Collections.emptyList());
    }
    
    /**
     * Gets all packages that contain repositories.
     */
    public java.util.Set<String> getPackagesWithRepositories() {
        return repositoriesByPackage.keySet();
    }
    
    /**
     * Checks if any repository issues were found.
     */
    public boolean hasIssues() {
        return !issues.isEmpty();
    }
    
    /**
     * Gets the total number of repositories (including JPA).
     */
    public int getTotalRepositoryCount() {
        return repositories.size() + jpaRepositories.size();
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private List<RepositoryComponentInfo> repositories = Collections.emptyList();
        private List<JpaRepositoryInfo> jpaRepositories = Collections.emptyList();
        private Map<String, List<RepositoryComponentInfo>> repositoriesByPackage = Collections.emptyMap();
        private RepositoryMetrics metrics;
        private List<RepositoryIssue> issues = Collections.emptyList();
        private RepositorySummary summary;
        
        public Builder repositories(List<RepositoryComponentInfo> repositories) {
            this.repositories = repositories != null ? repositories : Collections.emptyList();
            return this;
        }
        
        public Builder jpaRepositories(List<JpaRepositoryInfo> jpaRepositories) {
            this.jpaRepositories = jpaRepositories != null ? jpaRepositories : Collections.emptyList();
            return this;
        }
        
        public Builder repositoriesByPackage(Map<String, List<RepositoryComponentInfo>> repositoriesByPackage) {
            this.repositoriesByPackage = repositoriesByPackage != null ? repositoriesByPackage : Collections.emptyMap();
            return this;
        }
        
        public Builder metrics(RepositoryMetrics metrics) {
            this.metrics = metrics;
            return this;
        }
        
        public Builder issues(List<RepositoryIssue> issues) {
            this.issues = issues != null ? issues : Collections.emptyList();
            return this;
        }
        
        public Builder summary(RepositorySummary summary) {
            this.summary = summary;
            return this;
        }
        
        public RepositoryAnalysisResult build() {
            if (summary == null) {
                throw new IllegalStateException("Summary is required");
            }
            return new RepositoryAnalysisResult(this);
        }
    }
    
    @Override
    public String toString() {
        return String.format("RepositoryAnalysisResult{repositories=%d, jpa=%d, packages=%d, issues=%d}",
            repositories.size(), jpaRepositories.size(), repositoriesByPackage.size(), issues.size());
    }
}