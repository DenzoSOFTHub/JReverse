package it.denzosoft.jreverse.core.model;

/**
 * Summary statistics for call graph analysis.
 */
public class CallGraphSummary {
    
    private final int totalEndpoints;
    private final int totalCallChains;
    private final double averageDepth;
    private final int maxDepth;
    private final int totalIssues;
    private final int criticalIssues;
    private final int performanceHotspots;
    private final long analysisTimeMs;
    private final String statusMessage;
    
    private CallGraphSummary(Builder builder) {
        this.totalEndpoints = builder.totalEndpoints;
        this.totalCallChains = builder.totalCallChains;
        this.averageDepth = builder.averageDepth;
        this.maxDepth = builder.maxDepth;
        this.totalIssues = builder.totalIssues;
        this.criticalIssues = builder.criticalIssues;
        this.performanceHotspots = builder.performanceHotspots;
        this.analysisTimeMs = builder.analysisTimeMs;
        this.statusMessage = builder.statusMessage;
    }
    
    // Constructor diretto per compatibilità
    public CallGraphSummary(int totalEndpoints, int totalCallChains, int totalIssues, int criticalIssues, 
                           int performanceHotspots, long analysisTimeMs, String statusMessage) {
        this.totalEndpoints = totalEndpoints;
        this.totalCallChains = totalCallChains;
        this.averageDepth = 0.0;
        this.maxDepth = 0;
        this.totalIssues = totalIssues;
        this.criticalIssues = criticalIssues;
        this.performanceHotspots = performanceHotspots;
        this.analysisTimeMs = analysisTimeMs;
        this.statusMessage = statusMessage;
    }
    
    public int getTotalEndpoints() { return totalEndpoints; }
    public int getTotalCallChains() { return totalCallChains; }
    public double getAverageDepth() { return averageDepth; }
    public int getMaxDepth() { return maxDepth; }
    public int getTotalIssues() { return totalIssues; }
    public int getCriticalIssues() { return criticalIssues; }
    public int getPerformanceHotspots() { return performanceHotspots; }
    public long getAnalysisTimeMs() { return analysisTimeMs; }
    public String getStatusMessage() { return statusMessage; }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private int totalEndpoints;
        private int totalCallChains;
        private double averageDepth;
        private int maxDepth;
        private int totalIssues;
        private int criticalIssues;
        private int performanceHotspots;
        private long analysisTimeMs;
        private String statusMessage;
        
        public Builder totalEndpoints(int totalEndpoints) {
            this.totalEndpoints = totalEndpoints;
            return this;
        }
        
        public Builder totalCallChains(int totalCallChains) {
            this.totalCallChains = totalCallChains;
            return this;
        }
        
        public Builder averageDepth(double averageDepth) {
            this.averageDepth = averageDepth;
            return this;
        }
        
        public Builder maxDepth(int maxDepth) {
            this.maxDepth = maxDepth;
            return this;
        }
        
        public Builder totalIssues(int totalIssues) {
            this.totalIssues = totalIssues;
            return this;
        }
        
        public Builder criticalIssues(int criticalIssues) {
            this.criticalIssues = criticalIssues;
            return this;
        }
        
        public Builder performanceHotspots(int performanceHotspots) {
            this.performanceHotspots = performanceHotspots;
            return this;
        }
        
        public Builder analysisTimeMs(long analysisTimeMs) {
            this.analysisTimeMs = analysisTimeMs;
            return this;
        }
        
        public Builder statusMessage(String statusMessage) {
            this.statusMessage = statusMessage;
            return this;
        }
        
        public CallGraphSummary build() {
            return new CallGraphSummary(this);
        }
    }
}