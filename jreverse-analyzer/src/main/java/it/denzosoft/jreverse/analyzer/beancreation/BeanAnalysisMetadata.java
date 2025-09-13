package it.denzosoft.jreverse.analyzer.beancreation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Metadata about the bean creation analysis process including
 * success status, warnings, errors, and performance metrics.
 */
public class BeanAnalysisMetadata {
    
    private final boolean successful;
    private final int analyzedBeanCount;
    private final List<String> warnings;
    private final String errorMessage;
    private final LocalDateTime analysisTime;
    private final long processingTimeMs;
    
    private BeanAnalysisMetadata(boolean successful,
                                int analyzedBeanCount,
                                List<String> warnings,
                                String errorMessage,
                                LocalDateTime analysisTime,
                                long processingTimeMs) {
        this.successful = successful;
        this.analyzedBeanCount = analyzedBeanCount;
        this.warnings = Collections.unmodifiableList(new ArrayList<>(warnings));
        this.errorMessage = errorMessage;
        this.analysisTime = analysisTime;
        this.processingTimeMs = processingTimeMs;
    }
    
    /**
     * Creates metadata for successful analysis.
     */
    public static BeanAnalysisMetadata successful(int beanCount) {
        return new BeanAnalysisMetadata(
            true,
            beanCount,
            Collections.emptyList(),
            null,
            LocalDateTime.now(),
            0L
        );
    }
    
    /**
     * Creates metadata for analysis with warnings.
     */
    public static BeanAnalysisMetadata withWarnings(int beanCount, List<String> warnings) {
        return new BeanAnalysisMetadata(
            true,
            beanCount,
            warnings,
            null,
            LocalDateTime.now(),
            0L
        );
    }
    
    /**
     * Creates metadata for failed analysis.
     */
    public static BeanAnalysisMetadata error(String errorMessage) {
        return new BeanAnalysisMetadata(
            false,
            0,
            Collections.emptyList(),
            errorMessage,
            LocalDateTime.now(),
            0L
        );
    }
    
    /**
     * Creates metadata for analysis with warning message.
     */
    public static BeanAnalysisMetadata warning(String warningMessage) {
        List<String> warnings = new ArrayList<>();
        warnings.add(warningMessage);
        return new BeanAnalysisMetadata(
            true,
            0,
            warnings,
            null,
            LocalDateTime.now(),
            0L
        );
    }
    
    // Getters
    public boolean isSuccessful() { return successful; }
    public int getAnalyzedBeanCount() { return analyzedBeanCount; }
    public List<String> getWarnings() { return warnings; }
    public String getErrorMessage() { return errorMessage; }
    public LocalDateTime getAnalysisTime() { return analysisTime; }
    public long getProcessingTimeMs() { return processingTimeMs; }
    
    /**
     * Checks if analysis has warnings.
     */
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }
    
    /**
     * Checks if analysis has errors.
     */
    public boolean hasErrors() {
        return errorMessage != null;
    }
    
    /**
     * Gets the total number of issues (warnings + errors).
     */
    public int getTotalIssueCount() {
        return warnings.size() + (hasErrors() ? 1 : 0);
    }
    
    /**
     * Creates a copy with updated processing time.
     */
    public BeanAnalysisMetadata withProcessingTime(long processingTimeMs) {
        return new BeanAnalysisMetadata(
            successful,
            analyzedBeanCount,
            warnings,
            errorMessage,
            analysisTime,
            processingTimeMs
        );
    }
    
    @Override
    public String toString() {
        return String.format("BeanAnalysisMetadata{successful=%s, beans=%d, warnings=%d, errors=%s, time=%dms}",
            successful,
            analyzedBeanCount,
            warnings.size(),
            hasErrors(),
            processingTimeMs);
    }
}