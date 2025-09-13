package it.denzosoft.jreverse.core.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Result of call graph analysis containing all call graphs, metrics, and identified issues.
 */
public class CallGraphAnalysisResult {
    
    private final List<CallGraphChain> callChains;
    private final Map<String, CallGraphNode> rootNodes;
    private final CallGraphSummary summary;
    private final List<CallGraphIssue> issues;
    private final Map<String, Integer> componentUsageStats;
    private final List<PerformanceHotspot> hotspots;
    private final ArchitectureMetrics architectureMetrics;
    
    private CallGraphAnalysisResult(Builder builder) {
        this.callChains = List.copyOf(builder.callChains);
        this.rootNodes = Map.copyOf(builder.rootNodes);
        this.summary = builder.summary;
        this.issues = List.copyOf(builder.issues);
        this.componentUsageStats = Map.copyOf(builder.componentUsageStats);
        this.hotspots = List.copyOf(builder.hotspots);
        this.architectureMetrics = builder.architectureMetrics;
    }
    
    public List<CallGraphChain> getCallChains() {
        return callChains;
    }
    
    public Map<String, CallGraphNode> getRootNodes() {
        return rootNodes;
    }
    
    public CallGraphSummary getSummary() {
        return summary;
    }
    
    public List<CallGraphIssue> getIssues() {
        return issues;
    }
    
    public Map<String, Integer> getComponentUsageStats() {
        return componentUsageStats;
    }
    
    public List<PerformanceHotspot> getHotspots() {
        return hotspots;
    }
    
    public ArchitectureMetrics getArchitectureMetrics() {
        return architectureMetrics;
    }
    
    /**
     * Gets call chain for a specific endpoint.
     */
    public CallGraphChain getCallChainForEndpoint(String endpoint) {
        return callChains.stream()
            .filter(chain -> endpoint.equals(chain.getEndpoint()))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Gets all call chains that involve a specific component.
     */
    public List<CallGraphChain> getCallChainsForComponent(String componentName) {
        return callChains.stream()
            .filter(chain -> chain.involvesComponent(componentName))
            .toList();
    }
    
    /**
     * Gets issues filtered by severity.
     */
    public List<CallGraphIssue> getIssuesBySeverity(CallGraphIssue.Severity severity) {
        return issues.stream()
            .filter(issue -> issue.getSeverity() == severity)
            .toList();
    }
    
    /**
     * Checks if any critical issues were found.
     */
    public boolean hasCriticalIssues() {
        return issues.stream()
            .anyMatch(issue -> issue.getSeverity() == CallGraphIssue.Severity.CRITICAL);
    }
    
    /**
     * Gets the total number of endpoints analyzed.
     */
    public int getTotalEndpoints() {
        return callChains.size();
    }
    
    /**
     * Gets the most used component.
     */
    public String getMostUsedComponent() {
        return componentUsageStats.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private List<CallGraphChain> callChains = Collections.emptyList();
        private Map<String, CallGraphNode> rootNodes = Collections.emptyMap();
        private CallGraphSummary summary;
        private List<CallGraphIssue> issues = Collections.emptyList();
        private Map<String, Integer> componentUsageStats = Collections.emptyMap();
        private List<PerformanceHotspot> hotspots = Collections.emptyList();
        private ArchitectureMetrics architectureMetrics;
        
        public Builder callChains(List<CallGraphChain> callChains) {
            this.callChains = callChains != null ? callChains : Collections.emptyList();
            return this;
        }
        
        public Builder rootNodes(Map<String, CallGraphNode> rootNodes) {
            this.rootNodes = rootNodes != null ? rootNodes : Collections.emptyMap();
            return this;
        }
        
        public Builder summary(CallGraphSummary summary) {
            this.summary = summary;
            return this;
        }
        
        public Builder issues(List<CallGraphIssue> issues) {
            this.issues = issues != null ? issues : Collections.emptyList();
            return this;
        }
        
        public Builder componentUsageStats(Map<String, Integer> componentUsageStats) {
            this.componentUsageStats = componentUsageStats != null ? componentUsageStats : Collections.emptyMap();
            return this;
        }
        
        public Builder hotspots(List<PerformanceHotspot> hotspots) {
            this.hotspots = hotspots != null ? hotspots : Collections.emptyList();
            return this;
        }
        
        public Builder architectureMetrics(ArchitectureMetrics architectureMetrics) {
            this.architectureMetrics = architectureMetrics;
            return this;
        }
        
        public CallGraphAnalysisResult build() {
            if (summary == null) {
                throw new IllegalStateException("Summary is required");
            }
            return new CallGraphAnalysisResult(this);
        }
    }
    
    @Override
    public String toString() {
        return String.format("CallGraphAnalysisResult{chains=%d, issues=%d, hotspots=%d}",
            callChains.size(), issues.size(), hotspots.size());
    }
}