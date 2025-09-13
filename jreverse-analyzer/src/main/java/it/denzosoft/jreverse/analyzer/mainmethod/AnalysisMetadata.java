package it.denzosoft.jreverse.analyzer.mainmethod;

import java.util.ArrayList;
import java.util.List;

/**
 * Metadata about the analysis process, including warnings, errors, and performance information.
 */
public class AnalysisMetadata {
    
    private final AnalysisStatus status;
    private final List<String> warnings;
    private final List<String> errors;
    private final long analysisTimeMs;
    private final String message;
    
    private AnalysisMetadata(AnalysisStatus status, 
                           List<String> warnings, 
                           List<String> errors,
                           long analysisTimeMs,
                           String message) {
        this.status = status;
        this.warnings = List.copyOf(warnings);
        this.errors = List.copyOf(errors);
        this.analysisTimeMs = analysisTimeMs;
        this.message = message;
    }
    
    /**
     * Creates successful analysis metadata.
     */
    public static AnalysisMetadata successful() {
        return new AnalysisMetadata(
            AnalysisStatus.SUCCESS,
            List.of(),
            List.of(),
            0,
            "Analysis completed successfully"
        );
    }
    
    /**
     * Creates warning analysis metadata.
     */
    public static AnalysisMetadata warning(String message) {
        return new AnalysisMetadata(
            AnalysisStatus.WARNING,
            List.of(message),
            List.of(),
            0,
            message
        );
    }
    
    /**
     * Creates error analysis metadata.
     */
    public static AnalysisMetadata error(String message) {
        return new AnalysisMetadata(
            AnalysisStatus.ERROR,
            List.of(),
            List.of(message),
            0,
            message
        );
    }
    
    /**
     * Creates metadata with custom timing information.
     */
    public static AnalysisMetadata withTiming(AnalysisStatus status, String message, long analysisTimeMs) {
        return new AnalysisMetadata(
            status,
            List.of(),
            List.of(),
            analysisTimeMs,
            message
        );
    }
    
    // Getters
    public AnalysisStatus getStatus() { return status; }
    public List<String> getWarnings() { return warnings; }
    public List<String> getErrors() { return errors; }
    public long getAnalysisTimeMs() { return analysisTimeMs; }
    public String getMessage() { return message; }
    
    public boolean hasWarnings() { return !warnings.isEmpty(); }
    public boolean hasErrors() { return !errors.isEmpty(); }
    public boolean isSuccessful() { return status == AnalysisStatus.SUCCESS; }
    
    /**
     * Analysis status enumeration.
     */
    public enum AnalysisStatus {
        SUCCESS,
        WARNING,
        ERROR
    }
}