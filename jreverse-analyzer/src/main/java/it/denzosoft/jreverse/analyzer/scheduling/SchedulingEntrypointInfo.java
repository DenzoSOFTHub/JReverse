package it.denzosoft.jreverse.analyzer.scheduling;

import it.denzosoft.jreverse.core.model.AnnotationInfo;

import java.util.*;

/**
 * Immutable value object representing detailed information about a scheduling entrypoint.
 * Contains metadata about scheduled operations, timers, and execution patterns.
 */
public class SchedulingEntrypointInfo {
    
    private final String methodName;
    private final String declaringClass;
    private final SchedulingEntrypointType schedulingType;
    private final String cronExpression;
    private final long fixedRate;
    private final long fixedDelay;
    private final long initialDelay;
    private final String timeUnit;
    private final String zone;
    private final boolean isVoid;
    private final boolean isAsync;
    private final String schedulerName;
    private final Set<String> dependencies;
    private final AnnotationInfo sourceAnnotation;
    private final Map<String, Object> metadata;
    
    private SchedulingEntrypointInfo(Builder builder) {
        this.methodName = Objects.requireNonNull(builder.methodName, "methodName cannot be null");
        this.declaringClass = Objects.requireNonNull(builder.declaringClass, "declaringClass cannot be null");
        this.schedulingType = Objects.requireNonNull(builder.schedulingType, "schedulingType cannot be null");
        this.cronExpression = builder.cronExpression;
        this.fixedRate = builder.fixedRate;
        this.fixedDelay = builder.fixedDelay;
        this.initialDelay = builder.initialDelay;
        this.timeUnit = builder.timeUnit;
        this.zone = builder.zone;
        this.isVoid = builder.isVoid;
        this.isAsync = builder.isAsync;
        this.schedulerName = builder.schedulerName;
        this.dependencies = Collections.unmodifiableSet(new LinkedHashSet<>(builder.dependencies));
        this.sourceAnnotation = builder.sourceAnnotation;
        this.metadata = Collections.unmodifiableMap(new HashMap<>(builder.metadata));
    }
    
    // Getters
    public String getMethodName() { return methodName; }
    public String getDeclaringClass() { return declaringClass; }
    public SchedulingEntrypointType getSchedulingType() { return schedulingType; }
    public String getCronExpression() { return cronExpression; }
    public long getFixedRate() { return fixedRate; }
    public long getFixedDelay() { return fixedDelay; }
    public long getInitialDelay() { return initialDelay; }
    public String getTimeUnit() { return timeUnit; }
    public String getZone() { return zone; }
    public boolean isVoid() { return isVoid; }
    public boolean isAsync() { return isAsync; }
    public String getSchedulerName() { return schedulerName; }
    public Set<String> getDependencies() { return dependencies; }
    public AnnotationInfo getSourceAnnotation() { return sourceAnnotation; }
    public Map<String, Object> getMetadata() { return metadata; }
    
    /**
     * Gets the fully qualified method identifier.
     */
    public String getMethodIdentifier() {
        return declaringClass + "." + methodName;
    }
    
    /**
     * Determines if this is a cron-based schedule.
     */
    public boolean hasCronExpression() {
        return cronExpression != null && !cronExpression.trim().isEmpty();
    }
    
    /**
     * Determines if this uses fixed rate scheduling.
     */
    public boolean hasFixedRate() {
        return fixedRate > 0;
    }
    
    /**
     * Determines if this uses fixed delay scheduling.
     */
    public boolean hasFixedDelay() {
        return fixedDelay > 0;
    }
    
    /**
     * Determines if this has an initial delay.
     */
    public boolean hasInitialDelay() {
        return initialDelay > 0;
    }
    
    /**
     * Gets the execution frequency description.
     */
    public String getExecutionFrequency() {
        if (hasCronExpression()) {
            return "Cron: " + cronExpression;
        } else if (hasFixedRate()) {
            return "Fixed Rate: " + fixedRate + (timeUnit != null ? " " + timeUnit : "ms");
        } else if (hasFixedDelay()) {
            return "Fixed Delay: " + fixedDelay + (timeUnit != null ? " " + timeUnit : "ms");
        }
        return "Unknown";
    }
    
    /**
     * Gets a complexity score for this scheduling operation (0-100).
     */
    public int getComplexityScore() {
        int score = 10; // Base score
        
        // Add complexity for cron expressions
        if (hasCronExpression()) {
            score += 30;
            // Complex cron expressions (multiple fields)
            if (cronExpression.split("\\s+").length >= 6) {
                score += 20;
            }
        }
        
        // Add complexity for multiple timing configurations
        int timingConfigs = 0;
        if (hasFixedRate()) timingConfigs++;
        if (hasFixedDelay()) timingConfigs++;
        if (hasInitialDelay()) timingConfigs++;
        score += timingConfigs * 10;
        
        // Add complexity for async scheduling
        if (isAsync) {
            score += 15;
        }
        
        // Add complexity for custom scheduler
        if (schedulerName != null && !"default".equals(schedulerName)) {
            score += 10;
        }
        
        return Math.min(100, score);
    }
    
    /**
     * Gets a risk score for this scheduling operation (0-100).
     */
    public int getRiskScore() {
        int score = 0;
        
        // Risk for void methods (fire and forget)
        if (isVoid) {
            score += 20;
        }
        
        // Risk for cron expressions (parsing issues)
        if (hasCronExpression()) {
            score += 15;
            // High risk for complex cron expressions
            if (cronExpression.contains("?") || cronExpression.contains("L") || cronExpression.contains("W")) {
                score += 15;
            }
        }
        
        // Risk for very short intervals
        if (hasFixedRate() && fixedRate < 1000) { // Less than 1 second
            score += 25;
        }
        if (hasFixedDelay() && fixedDelay < 1000) {
            score += 25;
        }
        
        // Risk for async scheduled tasks (threading complexity)
        if (isAsync) {
            score += 20;
        }
        
        return Math.min(100, score);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SchedulingEntrypointInfo that = (SchedulingEntrypointInfo) obj;
        return Objects.equals(methodName, that.methodName) &&
               Objects.equals(declaringClass, that.declaringClass) &&
               Objects.equals(schedulingType, that.schedulingType);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(methodName, declaringClass, schedulingType);
    }
    
    @Override
    public String toString() {
        return "SchedulingEntrypointInfo{" +
                "method='" + getMethodIdentifier() + '\'' +
                ", type=" + schedulingType +
                ", frequency='" + getExecutionFrequency() + '\'' +
                ", async=" + isAsync +
                ", complexity=" + getComplexityScore() +
                ", risk=" + getRiskScore() +
                '}';
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String methodName;
        private String declaringClass;
        private SchedulingEntrypointType schedulingType;
        private String cronExpression;
        private long fixedRate;
        private long fixedDelay;
        private long initialDelay;
        private String timeUnit = "MILLISECONDS";
        private String zone;
        private boolean isVoid;
        private boolean isAsync;
        private String schedulerName = "default";
        private Set<String> dependencies = new LinkedHashSet<>();
        private AnnotationInfo sourceAnnotation;
        private Map<String, Object> metadata = new HashMap<>();
        
        public Builder methodName(String methodName) {
            this.methodName = methodName;
            return this;
        }
        
        public Builder declaringClass(String declaringClass) {
            this.declaringClass = declaringClass;
            return this;
        }
        
        public Builder schedulingType(SchedulingEntrypointType schedulingType) {
            this.schedulingType = schedulingType;
            return this;
        }
        
        public Builder cronExpression(String cronExpression) {
            this.cronExpression = cronExpression;
            return this;
        }
        
        public Builder fixedRate(long fixedRate) {
            this.fixedRate = fixedRate;
            return this;
        }
        
        public Builder fixedDelay(long fixedDelay) {
            this.fixedDelay = fixedDelay;
            return this;
        }
        
        public Builder initialDelay(long initialDelay) {
            this.initialDelay = initialDelay;
            return this;
        }
        
        public Builder timeUnit(String timeUnit) {
            this.timeUnit = timeUnit;
            return this;
        }
        
        public Builder zone(String zone) {
            this.zone = zone;
            return this;
        }
        
        public Builder isVoid(boolean isVoid) {
            this.isVoid = isVoid;
            return this;
        }
        
        public Builder isAsync(boolean isAsync) {
            this.isAsync = isAsync;
            return this;
        }
        
        public Builder schedulerName(String schedulerName) {
            this.schedulerName = schedulerName;
            return this;
        }
        
        public Builder addDependency(String dependency) {
            if (dependency != null && !dependency.trim().isEmpty()) {
                this.dependencies.add(dependency.trim());
            }
            return this;
        }
        
        public Builder dependencies(Set<String> dependencies) {
            this.dependencies = new LinkedHashSet<>(dependencies != null ? dependencies : Collections.emptySet());
            return this;
        }
        
        public Builder sourceAnnotation(AnnotationInfo sourceAnnotation) {
            this.sourceAnnotation = sourceAnnotation;
            return this;
        }
        
        public Builder addMetadata(String key, Object value) {
            if (key != null && value != null) {
                this.metadata.put(key, value);
            }
            return this;
        }
        
        public SchedulingEntrypointInfo build() {
            return new SchedulingEntrypointInfo(this);
        }
    }
}