package it.denzosoft.jreverse.core.model;

/**
 * Performance and complexity metrics for a method call in a call graph.
 */
public class CallMetrics {
    
    private final int executionOrder;
    private final long estimatedExecutionTime;
    private final int cyclomaticComplexity;
    private final int parameterCount;
    private final boolean isRecursive;
    private final boolean isAsync;
    private final boolean isCached;
    private final boolean hasExceptionHandling;
    private final int dependencyCount;
    private final double performanceRisk;
    
    private CallMetrics(Builder builder) {
        this.executionOrder = builder.executionOrder;
        this.estimatedExecutionTime = builder.estimatedExecutionTime;
        this.cyclomaticComplexity = builder.cyclomaticComplexity;
        this.parameterCount = builder.parameterCount;
        this.isRecursive = builder.isRecursive;
        this.isAsync = builder.isAsync;
        this.isCached = builder.isCached;
        this.hasExceptionHandling = builder.hasExceptionHandling;
        this.dependencyCount = builder.dependencyCount;
        this.performanceRisk = builder.performanceRisk;
    }
    
    public int getExecutionOrder() {
        return executionOrder;
    }
    
    public long getEstimatedExecutionTime() {
        return estimatedExecutionTime;
    }
    
    public int getCyclomaticComplexity() {
        return cyclomaticComplexity;
    }
    
    public int getParameterCount() {
        return parameterCount;
    }
    
    public boolean isRecursive() {
        return isRecursive;
    }
    
    public boolean isAsync() {
        return isAsync;
    }
    
    public boolean isCached() {
        return isCached;
    }
    
    public boolean hasExceptionHandling() {
        return hasExceptionHandling;
    }
    
    public int getDependencyCount() {
        return dependencyCount;
    }
    
    public double getPerformanceRisk() {
        return performanceRisk;
    }
    
    /**
     * Gets the complexity level based on cyclomatic complexity.
     */
    public ComplexityLevel getComplexityLevel() {
        if (cyclomaticComplexity <= 5) {
            return ComplexityLevel.LOW;
        } else if (cyclomaticComplexity <= 10) {
            return ComplexityLevel.MEDIUM;
        } else if (cyclomaticComplexity <= 20) {
            return ComplexityLevel.HIGH;
        } else {
            return ComplexityLevel.VERY_HIGH;
        }
    }
    
    /**
     * Gets the performance risk level.
     */
    public PerformanceRiskLevel getPerformanceRiskLevel() {
        if (performanceRisk <= 0.3) {
            return PerformanceRiskLevel.LOW;
        } else if (performanceRisk <= 0.6) {
            return PerformanceRiskLevel.MEDIUM;
        } else if (performanceRisk <= 0.8) {
            return PerformanceRiskLevel.HIGH;
        } else {
            return PerformanceRiskLevel.CRITICAL;
        }
    }
    
    /**
     * Checks if this call has potential performance issues.
     */
    public boolean hasPerformanceIssues() {
        return performanceRisk > 0.6 || 
               cyclomaticComplexity > 15 || 
               dependencyCount > 10;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private int executionOrder = 0;
        private long estimatedExecutionTime = 0;
        private int cyclomaticComplexity = 1;
        private int parameterCount = 0;
        private boolean isRecursive = false;
        private boolean isAsync = false;
        private boolean isCached = false;
        private boolean hasExceptionHandling = false;
        private int dependencyCount = 0;
        private double performanceRisk = 0.0;
        
        public Builder executionOrder(int executionOrder) {
            this.executionOrder = executionOrder;
            return this;
        }
        
        public Builder estimatedExecutionTime(long estimatedExecutionTime) {
            this.estimatedExecutionTime = estimatedExecutionTime;
            return this;
        }
        
        public Builder cyclomaticComplexity(int cyclomaticComplexity) {
            this.cyclomaticComplexity = Math.max(1, cyclomaticComplexity);
            return this;
        }
        
        public Builder parameterCount(int parameterCount) {
            this.parameterCount = Math.max(0, parameterCount);
            return this;
        }
        
        public Builder isRecursive(boolean isRecursive) {
            this.isRecursive = isRecursive;
            return this;
        }
        
        public Builder isAsync(boolean isAsync) {
            this.isAsync = isAsync;
            return this;
        }
        
        public Builder isCached(boolean isCached) {
            this.isCached = isCached;
            return this;
        }
        
        public Builder hasExceptionHandling(boolean hasExceptionHandling) {
            this.hasExceptionHandling = hasExceptionHandling;
            return this;
        }
        
        public Builder dependencyCount(int dependencyCount) {
            this.dependencyCount = Math.max(0, dependencyCount);
            return this;
        }
        
        public Builder performanceRisk(double performanceRisk) {
            this.performanceRisk = Math.max(0.0, Math.min(1.0, performanceRisk));
            return this;
        }
        
        public CallMetrics build() {
            return new CallMetrics(this);
        }
    }
    
    public enum ComplexityLevel {
        LOW("Low", "Simple logic"),
        MEDIUM("Medium", "Moderate complexity"),
        HIGH("High", "Complex logic"),
        VERY_HIGH("Very High", "Highly complex logic");
        
        private final String displayName;
        private final String description;
        
        ComplexityLevel(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    public enum PerformanceRiskLevel {
        LOW("Low", "No significant performance concerns"),
        MEDIUM("Medium", "Some performance considerations"),
        HIGH("High", "Likely performance bottleneck"),
        CRITICAL("Critical", "Critical performance issues");
        
        private final String displayName;
        private final String description;
        
        PerformanceRiskLevel(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    @Override
    public String toString() {
        return String.format("CallMetrics{complexity=%d, risk=%.2f, deps=%d, async=%b}",
            cyclomaticComplexity, performanceRisk, dependencyCount, isAsync);
    }
}