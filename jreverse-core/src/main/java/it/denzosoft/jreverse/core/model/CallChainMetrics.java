package it.denzosoft.jreverse.core.model;

/**
 * Performance and complexity metrics for a call chain.
 */
public class CallChainMetrics {
    
    private final int databaseCalls;
    private final int externalCalls;
    private final int serviceCalls;
    private final double estimatedExecutionTime;
    private final double performanceRisk;
    private final boolean hasTransactionBoundary;
    
    private CallChainMetrics(Builder builder) {
        this.databaseCalls = builder.databaseCalls;
        this.externalCalls = builder.externalCalls;
        this.serviceCalls = builder.serviceCalls;
        this.estimatedExecutionTime = builder.estimatedExecutionTime;
        this.performanceRisk = builder.performanceRisk;
        this.hasTransactionBoundary = builder.hasTransactionBoundary;
    }
    
    public int getDatabaseCalls() { return databaseCalls; }
    public int getExternalCalls() { return externalCalls; }
    public int getServiceCalls() { return serviceCalls; }
    public double getEstimatedExecutionTime() { return estimatedExecutionTime; }
    public double getPerformanceRisk() { return performanceRisk; }
    public boolean hasTransactionBoundary() { return hasTransactionBoundary; }
    
    public boolean hasPerformanceRisk() {
        return performanceRisk > 0.6 || databaseCalls > 10 || externalCalls > 5;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private int databaseCalls;
        private int externalCalls;
        private int serviceCalls;
        private double estimatedExecutionTime;
        private double performanceRisk;
        private boolean hasTransactionBoundary;
        
        public Builder databaseCalls(int databaseCalls) {
            this.databaseCalls = databaseCalls;
            return this;
        }
        
        public Builder externalCalls(int externalCalls) {
            this.externalCalls = externalCalls;
            return this;
        }
        
        public Builder serviceCalls(int serviceCalls) {
            this.serviceCalls = serviceCalls;
            return this;
        }
        
        public Builder estimatedExecutionTime(double estimatedExecutionTime) {
            this.estimatedExecutionTime = estimatedExecutionTime;
            return this;
        }
        
        public Builder performanceRisk(double performanceRisk) {
            this.performanceRisk = performanceRisk;
            return this;
        }
        
        public Builder hasTransactionBoundary(boolean hasTransactionBoundary) {
            this.hasTransactionBoundary = hasTransactionBoundary;
            return this;
        }
        
        public CallChainMetrics build() {
            return new CallChainMetrics(this);
        }
    }
}