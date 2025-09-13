package it.denzosoft.jreverse.core.observer;

import java.util.Objects;

/**
 * Event representing successful completion of an operation.
 * Immutable value object for observer pattern.
 */
public final class CompletionEvent {
    
    private final String operationType;
    private final long startTime;
    private final long endTime;
    private final Object result;
    private final String message;
    
    private CompletionEvent(Builder builder) {
        this.operationType = Objects.requireNonNull(builder.operationType, "operationType cannot be null");
        this.startTime = builder.startTime;
        this.endTime = builder.endTime > 0 ? builder.endTime : System.currentTimeMillis();
        this.result = builder.result;
        this.message = builder.message;
        
        if (endTime < startTime) {
            throw new IllegalArgumentException("End time cannot be before start time");
        }
    }
    
    public String getOperationType() {
        return operationType;
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public long getEndTime() {
        return endTime;
    }
    
    public long getDurationMs() {
        return endTime - startTime;
    }
    
    public Object getResult() {
        return result;
    }
    
    public String getMessage() {
        return message;
    }
    
    public boolean hasResult() {
        return result != null;
    }
    
    public boolean hasMessage() {
        return message != null && !message.trim().isEmpty();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CompletionEvent that = (CompletionEvent) obj;
        return startTime == that.startTime &&
               endTime == that.endTime &&
               Objects.equals(operationType, that.operationType) &&
               Objects.equals(result, that.result);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(operationType, startTime, endTime, result);
    }
    
    @Override
    public String toString() {
        return "CompletionEvent{" +
                "operationType='" + operationType + '\'' +
                ", durationMs=" + getDurationMs() +
                ", hasResult=" + hasResult() +
                ", message='" + message + '\'' +
                '}';
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static CompletionEvent simple(String operationType, long startTime, Object result) {
        return builder()
            .operationType(operationType)
            .startTime(startTime)
            .result(result)
            .build();
    }
    
    public static class Builder {
        private String operationType;
        private long startTime = System.currentTimeMillis();
        private long endTime;
        private Object result;
        private String message;
        
        public Builder operationType(String operationType) {
            this.operationType = operationType;
            return this;
        }
        
        public Builder startTime(long startTime) {
            this.startTime = startTime;
            return this;
        }
        
        public Builder endTime(long endTime) {
            this.endTime = endTime;
            return this;
        }
        
        public Builder result(Object result) {
            this.result = result;
            return this;
        }
        
        public Builder message(String message) {
            this.message = message;
            return this;
        }
        
        public Builder durationMs(long durationMs) {
            this.endTime = this.startTime + durationMs;
            return this;
        }
        
        public Builder success(boolean success) {
            // For future extensibility - completion events assume success
            return this;
        }
        
        public CompletionEvent build() {
            return new CompletionEvent(this);
        }
    }
}