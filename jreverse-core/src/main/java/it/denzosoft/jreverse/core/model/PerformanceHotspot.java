package it.denzosoft.jreverse.core.model;

/**
 * Represents a performance hotspot in the call graph.
 */
public class PerformanceHotspot {
    
    public enum RiskLevel {
        LOW("Low", "#4CAF50"),
        MEDIUM("Medium", "#FF9800"),
        HIGH("High", "#F44336");
        
        private final String displayName;
        private final String color;
        
        RiskLevel(String displayName, String color) {
            this.displayName = displayName;
            this.color = color;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getColor() {
            return color;
        }
    }
    
    private final String componentName;
    private final String methodName;
    private final int usageCount;
    private final double performanceImpact;
    private final String description;
    private final int callCount;
    private final int complexity;
    private final RiskLevel riskLevel;
    
    private PerformanceHotspot(Builder builder) {
        this.componentName = builder.componentName;
        this.methodName = builder.methodName;
        this.usageCount = builder.usageCount;
        this.performanceImpact = builder.performanceImpact;
        this.description = builder.description;
        this.callCount = builder.callCount;
        this.complexity = builder.complexity;
        this.riskLevel = builder.riskLevel;
    }
    
    // Constructor diretto per compatibilità
    public PerformanceHotspot(String componentName, String methodName, int callCount, int complexity, RiskLevel riskLevel) {
        this.componentName = componentName;
        this.methodName = methodName;
        this.callCount = callCount;
        this.complexity = complexity;
        this.riskLevel = riskLevel;
        this.usageCount = callCount;
        this.performanceImpact = complexity * callCount;
        this.description = "Hotspot: " + callCount + " calls, complexity " + complexity;
    }
    
    public String getComponentName() { return componentName; }
    public String getMethodName() { return methodName; }
    public int getUsageCount() { return usageCount; }
    public double getPerformanceImpact() { return performanceImpact; }
    public String getDescription() { return description; }
    public int getCallCount() { return callCount; }
    public int getComplexity() { return complexity; }
    public RiskLevel getRiskLevel() { return riskLevel; }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String componentName;
        private String methodName;
        private int usageCount;
        private double performanceImpact;
        private String description;
        private int callCount;
        private int complexity;
        private RiskLevel riskLevel;
        
        public Builder componentName(String componentName) {
            this.componentName = componentName;
            return this;
        }
        
        public Builder methodName(String methodName) {
            this.methodName = methodName;
            return this;
        }
        
        public Builder usageCount(int usageCount) {
            this.usageCount = usageCount;
            return this;
        }
        
        public Builder performanceImpact(double performanceImpact) {
            this.performanceImpact = performanceImpact;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder callCount(int callCount) {
            this.callCount = callCount;
            return this;
        }
        
        public Builder complexity(int complexity) {
            this.complexity = complexity;
            return this;
        }
        
        public Builder riskLevel(RiskLevel riskLevel) {
            this.riskLevel = riskLevel;
            return this;
        }
        
        public PerformanceHotspot build() {
            return new PerformanceHotspot(this);
        }
    }
}