package it.denzosoft.jreverse.core.model;

import java.util.Objects;

/**
 * Result of UML diagram generation containing the PlantUML content and metadata.
 */
public final class UMLGenerationResult {
    
    private final boolean successful;
    private final String content;
    private final String errorMessage;
    private final UMLMetadata metadata;
    private final long generationTimeMs;
    
    private UMLGenerationResult(boolean successful, String content, String errorMessage, 
                               UMLMetadata metadata, long generationTimeMs) {
        this.successful = successful;
        this.content = content;
        this.errorMessage = errorMessage;
        this.metadata = metadata;
        this.generationTimeMs = generationTimeMs;
    }
    
    public static UMLGenerationResult success(String content, UMLMetadata metadata, long generationTimeMs) {
        return new UMLGenerationResult(true, content, null, metadata, generationTimeMs);
    }
    
    public static UMLGenerationResult failed(String errorMessage) {
        return new UMLGenerationResult(false, null, errorMessage, null, 0);
    }
    
    public boolean isSuccessful() {
        return successful;
    }
    
    public String getContent() {
        return content;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public UMLMetadata getMetadata() {
        return metadata;
    }
    
    public long getGenerationTimeMs() {
        return generationTimeMs;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        UMLGenerationResult that = (UMLGenerationResult) obj;
        return successful == that.successful &&
               generationTimeMs == that.generationTimeMs &&
               Objects.equals(content, that.content) &&
               Objects.equals(errorMessage, that.errorMessage) &&
               Objects.equals(metadata, that.metadata);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(successful, content, errorMessage, metadata, generationTimeMs);
    }
    
    @Override
    public String toString() {
        if (successful) {
            return "UMLGenerationResult{successful=true, contentLength=" + 
                   (content != null ? content.length() : 0) + 
                   ", generationTime=" + generationTimeMs + "ms}";
        } else {
            return "UMLGenerationResult{successful=false, error='" + errorMessage + "'}";
        }
    }
}