package it.denzosoft.jreverse.core.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Result of service layer analysis containing information about @Service components
 * and service layer architecture patterns.
 */
public class ServiceLayerAnalysisResult {
    
    private final List<ServiceComponentInfo> serviceComponents;
    private final Map<String, List<ServiceComponentInfo>> servicesByPackage;
    private final ServiceLayerMetrics metrics;
    private final List<ServiceLayerIssue> issues;
    private final ServiceLayerSummary summary;
    
    private ServiceLayerAnalysisResult(Builder builder) {
        this.serviceComponents = List.copyOf(builder.serviceComponents);
        this.servicesByPackage = Map.copyOf(builder.servicesByPackage);
        this.metrics = builder.metrics;
        this.issues = List.copyOf(builder.issues);
        this.summary = builder.summary;
    }
    
    public List<ServiceComponentInfo> getServiceComponents() {
        return serviceComponents;
    }
    
    public Map<String, List<ServiceComponentInfo>> getServicesByPackage() {
        return servicesByPackage;
    }
    
    public ServiceLayerMetrics getMetrics() {
        return metrics;
    }
    
    public List<ServiceLayerIssue> getIssues() {
        return issues;
    }
    
    public ServiceLayerSummary getSummary() {
        return summary;
    }
    
    /**
     * Gets services for a specific package.
     */
    public List<ServiceComponentInfo> getServicesInPackage(String packageName) {
        return servicesByPackage.getOrDefault(packageName, Collections.emptyList());
    }
    
    /**
     * Gets all packages that contain services.
     */
    public java.util.Set<String> getPackagesWithServices() {
        return servicesByPackage.keySet();
    }
    
    /**
     * Checks if any service layer issues were found.
     */
    public boolean hasIssues() {
        return !issues.isEmpty();
    }
    
    /**
     * Gets the total number of service components.
     */
    public int getTotalServiceCount() {
        return serviceComponents.size();
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private List<ServiceComponentInfo> serviceComponents = Collections.emptyList();
        private Map<String, List<ServiceComponentInfo>> servicesByPackage = Collections.emptyMap();
        private ServiceLayerMetrics metrics;
        private List<ServiceLayerIssue> issues = Collections.emptyList();
        private ServiceLayerSummary summary;
        
        public Builder serviceComponents(List<ServiceComponentInfo> serviceComponents) {
            this.serviceComponents = serviceComponents != null ? serviceComponents : Collections.emptyList();
            return this;
        }
        
        public Builder servicesByPackage(Map<String, List<ServiceComponentInfo>> servicesByPackage) {
            this.servicesByPackage = servicesByPackage != null ? servicesByPackage : Collections.emptyMap();
            return this;
        }
        
        public Builder metrics(ServiceLayerMetrics metrics) {
            this.metrics = metrics;
            return this;
        }
        
        public Builder issues(List<ServiceLayerIssue> issues) {
            this.issues = issues != null ? issues : Collections.emptyList();
            return this;
        }
        
        public Builder summary(ServiceLayerSummary summary) {
            this.summary = summary;
            return this;
        }
        
        public ServiceLayerAnalysisResult build() {
            if (summary == null) {
                throw new IllegalStateException("Summary is required");
            }
            return new ServiceLayerAnalysisResult(this);
        }
    }
    
    @Override
    public String toString() {
        return String.format("ServiceLayerAnalysisResult{services=%d, packages=%d, issues=%d}",
            serviceComponents.size(), servicesByPackage.size(), issues.size());
    }
}