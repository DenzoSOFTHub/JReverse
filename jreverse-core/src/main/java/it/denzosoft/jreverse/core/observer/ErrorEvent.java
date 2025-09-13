package it.denzosoft.jreverse.core.observer;

import java.util.Objects;

/**
 * Event representing an error that occurred during processing.
 * Immutable value object for observer pattern.
 */
public final class ErrorEvent {
    
    private final String operationType;
    private final long timestamp;
    private final String errorMessage;
    private final Throwable cause;
    private final String errorCode;
    private final boolean recoverable;
    
    private ErrorEvent(Builder builder) {
        this.operationType = Objects.requireNonNull(builder.operationType, "operationType cannot be null");
        this.timestamp = builder.timestamp > 0 ? builder.timestamp : System.currentTimeMillis();
        this.errorMessage = Objects.requireNonNull(builder.errorMessage, "errorMessage cannot be null");
        this.cause = builder.cause;
        this.errorCode = builder.errorCode;
        this.recoverable = builder.recoverable;
    }
    
    public String getOperationType() {
        return operationType;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public Throwable getCause() {
        return cause;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public boolean isRecoverable() {
        return recoverable;
    }
    
    public boolean hasCause() {
        return cause != null;
    }
    
    public boolean hasErrorCode() {
        return errorCode != null && !errorCode.trim().isEmpty();
    }
    
    public String getFullErrorMessage() {
        StringBuilder sb = new StringBuilder(errorMessage);
        if (hasCause() && cause.getMessage() != null) {
            sb.append(": ").append(cause.getMessage());
        }
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ErrorEvent that = (ErrorEvent) obj;
        return timestamp == that.timestamp &&
               recoverable == that.recoverable &&
               Objects.equals(operationType, that.operationType) &&
               Objects.equals(errorMessage, that.errorMessage) &&
               Objects.equals(errorCode, that.errorCode);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(operationType, timestamp, errorMessage, errorCode, recoverable);
    }
    
    @Override
    public String toString() {
        return "ErrorEvent{" +
                "operationType='" + operationType + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", errorCode='" + errorCode + '\'' +
                ", recoverable=" + recoverable +
                ", hasCause=" + hasCause() +
                '}';
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static ErrorEvent simple(String operationType, String errorMessage) {
        return builder()
            .operationType(operationType)
            .errorMessage(errorMessage)
            .build();
    }
    
    public static ErrorEvent withCause(String operationType, String errorMessage, Throwable cause) {
        return builder()
            .operationType(operationType)
            .errorMessage(errorMessage)
            .cause(cause)
            .build();
    }
    
    public static class Builder {
        private String operationType;
        private long timestamp;
        private String errorMessage;
        private Throwable cause;
        private String errorCode;
        private boolean recoverable = false;
        
        public Builder operationType(String operationType) {
            this.operationType = operationType;
            return this;
        }
        
        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }
        
        public Builder message(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }
        
        public Builder throwable(Throwable throwable) {
            this.cause = throwable;
            return this;
        }
        
        public Builder cause(Throwable cause) {
            this.cause = cause;
            return this;
        }
        
        public Builder errorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }
        
        public Builder recoverable(boolean recoverable) {
            this.recoverable = recoverable;
            return this;
        }
        
        public ErrorEvent build() {
            return new ErrorEvent(this);
        }
    }
}