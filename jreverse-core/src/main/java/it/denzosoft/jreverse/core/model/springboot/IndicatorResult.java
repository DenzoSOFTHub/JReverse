package it.denzosoft.jreverse.core.model.springboot;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Result of a single Spring Boot indicator analysis.
 * Contains confidence score, evidence, and analysis metadata.
 */
public final class IndicatorResult {
    
    private final double confidence;
    private final AnalysisStatus status;
    private final Map<String, Object> evidence;
    private final String errorMessage;
    private final long analysisTimeMs;
    
    private IndicatorResult(Builder builder) {
        this.confidence = Math.max(0.0, Math.min(1.0, builder.confidence));
        this.status = Objects.requireNonNull(builder.status, "status cannot be null");
        this.evidence = Collections.unmodifiableMap(new HashMap<>(builder.evidence));
        this.errorMessage = builder.errorMessage;
        this.analysisTimeMs = builder.analysisTimeMs;
    }
    
    public double getConfidence() {
        return confidence;
    }
    
    public AnalysisStatus getStatus() {
        return status;
    }
    
    public Map<String, Object> getEvidence() {
        return evidence;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public long getAnalysisTimeMs() {
        return analysisTimeMs;
    }
    
    public boolean isSuccessful() {
        return status == AnalysisStatus.SUCCESS;
    }
    
    public boolean hasEvidence() {
        return !evidence.isEmpty();
    }
    
    public boolean hasError() {
        return status == AnalysisStatus.ERROR;
    }
    
    /**
     * Gets a specific evidence value with type casting.
     */
    @SuppressWarnings("unchecked")
    public <T> T getEvidence(String key, Class<T> type, T defaultValue) {
        Object value = evidence.get(key);
        if (value != null && type.isAssignableFrom(value.getClass())) {
            return (T) value;
        }
        return defaultValue;
    }
    
    /**
     * Gets a string evidence value.
     */
    public String getStringEvidence(String key) {
        return getEvidence(key, String.class, "");
    }
    
    /**
     * Gets a boolean evidence value.
     */
    public boolean getBooleanEvidence(String key) {
        return getEvidence(key, Boolean.class, false);
    }
    
    /**
     * Gets an integer evidence value.
     */
    public int getIntEvidence(String key) {
        return getEvidence(key, Integer.class, 0);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        IndicatorResult that = (IndicatorResult) obj;
        return Double.compare(that.confidence, confidence) == 0 &&
               status == that.status &&
               Objects.equals(evidence, that.evidence);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(confidence, status, evidence);
    }
    
    @Override
    public String toString() {
        return "IndicatorResult{" +
                "confidence=" + String.format("%.2f", confidence) +
                ", status=" + status +
                ", evidenceKeys=" + evidence.keySet() +
                (errorMessage != null ? ", error='" + errorMessage + "'" : "") +
                ", timeMs=" + analysisTimeMs +
                '}';
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static IndicatorResult success(double confidence) {
        return builder()
            .confidence(confidence)
            .status(AnalysisStatus.SUCCESS)
            .build();
    }
    
    public static IndicatorResult notFound(String reason) {
        return builder()
            .confidence(0.0)
            .status(AnalysisStatus.NOT_FOUND)
            .evidence("reason", reason)
            .build();
    }
    
    public static IndicatorResult error(String errorMessage) {
        return builder()
            .confidence(0.0)
            .status(AnalysisStatus.ERROR)
            .errorMessage(errorMessage)
            .build();
    }
    
    public static class Builder {
        private double confidence = 0.0;
        private AnalysisStatus status = AnalysisStatus.NOT_FOUND;
        private Map<String, Object> evidence = new HashMap<>();
        private String errorMessage;
        private long analysisTimeMs = 0;
        
        public Builder confidence(double confidence) {
            this.confidence = confidence;
            return this;
        }
        
        public Builder status(AnalysisStatus status) {
            this.status = status;
            return this;
        }
        
        public Builder evidence(String key, Object value) {
            if (key != null && value != null) {
                this.evidence.put(key, value);
            }
            return this;
        }
        
        public Builder evidence(Map<String, Object> evidence) {
            if (evidence != null) {
                this.evidence.putAll(evidence);
            }
            return this;
        }
        
        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }
        
        public Builder analysisTimeMs(long timeMs) {
            this.analysisTimeMs = timeMs;
            return this;
        }
        
        public IndicatorResult build() {
            // Auto-set status based on confidence if not explicitly set
            if (status == AnalysisStatus.NOT_FOUND && confidence > 0.0) {
                this.status = AnalysisStatus.SUCCESS;
            }
            
            return new IndicatorResult(this);
        }
    }
    
    public enum AnalysisStatus {
        SUCCESS,    // Analysis completed successfully
        NOT_FOUND,  // No evidence found (not necessarily an error)
        ERROR       // Analysis failed due to an error
    }
}