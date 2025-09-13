package it.denzosoft.jreverse.core.observer;

import java.util.Objects;

/**
 * Event representing progress update during analysis or report generation.
 * Immutable value object for observer pattern.
 */
public final class ProgressEvent {
    
    private final int percentage;
    private final String currentTask;
    private final String operationType;
    private final long timestamp;
    private final Object details;
    
    private ProgressEvent(Builder builder) {
        this.percentage = validatePercentage(builder.percentage);
        this.currentTask = Objects.requireNonNull(builder.currentTask, "currentTask cannot be null");
        this.operationType = Objects.requireNonNull(builder.operationType, "operationType cannot be null");
        this.timestamp = builder.timestamp > 0 ? builder.timestamp : System.currentTimeMillis();
        this.details = builder.details;
    }
    
    public int getPercentage() {
        return percentage;
    }
    
    public String getCurrentTask() {
        return currentTask;
    }
    
    public String getOperationType() {
        return operationType;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public Object getDetails() {
        return details;
    }
    
    public boolean isCompleted() {
        return percentage >= 100;
    }
    
    public boolean hasDetails() {
        return details != null;
    }
    
    private int validatePercentage(int percentage) {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("Percentage must be between 0 and 100, got: " + percentage);
        }
        return percentage;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ProgressEvent that = (ProgressEvent) obj;
        return percentage == that.percentage &&
               timestamp == that.timestamp &&
               Objects.equals(currentTask, that.currentTask) &&
               Objects.equals(operationType, that.operationType);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(percentage, currentTask, operationType, timestamp);
    }
    
    @Override
    public String toString() {
        return "ProgressEvent{" +
                "percentage=" + percentage +
                ", currentTask='" + currentTask + '\'' +
                ", operationType='" + operationType + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static ProgressEvent simple(int percentage, String currentTask, String operationType) {
        return builder()
            .percentage(percentage)
            .currentTask(currentTask)
            .operationType(operationType)
            .build();
    }
    
    public static class Builder {
        private int percentage;
        private String currentTask;
        private String operationType;
        private long timestamp;
        private Object details;
        
        public Builder percentage(int percentage) {
            this.percentage = percentage;
            return this;
        }
        
        public Builder currentTask(String currentTask) {
            this.currentTask = currentTask;
            return this;
        }
        
        public Builder operationType(String operationType) {
            this.operationType = operationType;
            return this;
        }
        
        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public Builder details(Object details) {
            this.details = details;
            return this;
        }
        
        public ProgressEvent build() {
            return new ProgressEvent(this);
        }
    }
}