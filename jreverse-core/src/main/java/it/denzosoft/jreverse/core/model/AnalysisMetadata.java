package it.denzosoft.jreverse.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Metadata about an analysis operation including success status, warnings, and errors.
 */
public class AnalysisMetadata {
    
    private final boolean successful;
    private final List<String> warnings;
    private final List<String> errors;
    private final String message;
    
    private AnalysisMetadata(boolean successful, List<String> warnings, List<String> errors, String message) {
        this.successful = successful;
        this.warnings = Collections.unmodifiableList(new ArrayList<>(warnings));
        this.errors = Collections.unmodifiableList(new ArrayList<>(errors));
        this.message = message;
    }
    
    public static AnalysisMetadata successful() {
        return new AnalysisMetadata(true, Collections.emptyList(), Collections.emptyList(), null);
    }
    
    public static AnalysisMetadata warning(String warningMessage) {
        List<String> warnings = new ArrayList<>();
        warnings.add(warningMessage);
        return new AnalysisMetadata(true, warnings, Collections.emptyList(), warningMessage);
    }
    
    public static AnalysisMetadata error(String errorMessage) {
        List<String> errors = new ArrayList<>();
        errors.add(errorMessage);
        return new AnalysisMetadata(false, Collections.emptyList(), errors, errorMessage);
    }
    
    public boolean isSuccessful() {
        return successful;
    }
    
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }
    
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
    
    public List<String> getWarnings() {
        return warnings;
    }
    
    public List<String> getErrors() {
        return errors;
    }
    
    public String getMessage() {
        return message;
    }
    
    @Override
    public String toString() {
        return "AnalysisMetadata{" +
                "successful=" + successful +
                ", warnings=" + warnings.size() +
                ", errors=" + errors.size() +
                ", message='" + message + '\'' +
                '}';
    }
}