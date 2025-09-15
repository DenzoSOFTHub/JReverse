package it.denzosoft.jreverse.core.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Result of architectural pattern analysis containing detected patterns, anti-patterns, and recommendations.
 */
public class ArchitecturalPatternResult {
    
    private final List<DetectedArchitecturalPattern> architecturalPatterns;
    private final List<DetectedDesignPattern> designPatterns;
    private final List<DetectedAntiPattern> antiPatterns;
    private final List<ArchitecturalRecommendation> recommendations;
    private final ArchitecturalPatternMetrics metrics;
    private final Map<String, Double> patternConfidenceScores;
    
    private ArchitecturalPatternResult(Builder builder) {
        this.architecturalPatterns = List.copyOf(builder.architecturalPatterns);
        this.designPatterns = List.copyOf(builder.designPatterns);
        this.antiPatterns = List.copyOf(builder.antiPatterns);
        this.recommendations = List.copyOf(builder.recommendations);
        this.metrics = builder.metrics;
        this.patternConfidenceScores = Map.copyOf(builder.patternConfidenceScores);
    }
    
    public List<DetectedArchitecturalPattern> getArchitecturalPatterns() {
        return architecturalPatterns;
    }
    
    public List<DetectedDesignPattern> getDesignPatterns() {
        return designPatterns;
    }
    
    public List<DetectedAntiPattern> getAntiPatterns() {
        return antiPatterns;
    }
    
    public List<ArchitecturalRecommendation> getRecommendations() {
        return recommendations;
    }
    
    public ArchitecturalPatternMetrics getMetrics() {
        return metrics;
    }
    
    public Map<String, Double> getPatternConfidenceScores() {
        return patternConfidenceScores;
    }
    
    /**
     * Gets architectural patterns by type.
     */
    public List<DetectedArchitecturalPattern> getArchitecturalPatternsByType(ArchitecturalPatternType type) {
        return architecturalPatterns.stream()
            .filter(pattern -> type.equals(pattern.getPatternType()))
            .toList();
    }
    
    /**
     * Gets design patterns by category.
     */
    public List<DetectedDesignPattern> getDesignPatternsByType(DesignPatternType type) {
        return designPatterns.stream()
            .filter(pattern -> type.equals(pattern.getPatternType()))
            .toList();
    }
    
    /**
     * Gets anti-patterns by severity.
     */
    public List<DetectedAntiPattern> getAntiPatternsBySeverity(String severity) {
        return antiPatterns.stream()
            .filter(antiPattern -> severity.equals(antiPattern.getSeverity()))
            .toList();
    }
    
    /**
     * Checks if any critical anti-patterns were detected.
     */
    public boolean hasCriticalAntiPatterns() {
        return antiPatterns.stream()
            .anyMatch(antiPattern -> "CRITICAL".equals(antiPattern.getSeverity()));
    }
    
    /**
     * Gets the total number of patterns detected.
     */
    public int getTotalPatternsDetected() {
        return architecturalPatterns.size() + designPatterns.size();
    }
    
    /**
     * Gets the pattern with highest confidence score.
     */
    public String getMostConfidentPattern() {
        return patternConfidenceScores.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private List<DetectedArchitecturalPattern> architecturalPatterns = Collections.emptyList();
        private List<DetectedDesignPattern> designPatterns = Collections.emptyList();
        private List<DetectedAntiPattern> antiPatterns = Collections.emptyList();
        private List<ArchitecturalRecommendation> recommendations = Collections.emptyList();
        private ArchitecturalPatternMetrics metrics;
        private Map<String, Double> patternConfidenceScores = Collections.emptyMap();
        
        public Builder architecturalPatterns(List<DetectedArchitecturalPattern> architecturalPatterns) {
            this.architecturalPatterns = architecturalPatterns != null ? architecturalPatterns : Collections.emptyList();
            return this;
        }
        
        public Builder designPatterns(List<DetectedDesignPattern> designPatterns) {
            this.designPatterns = designPatterns != null ? designPatterns : Collections.emptyList();
            return this;
        }
        
        public Builder antiPatterns(List<DetectedAntiPattern> antiPatterns) {
            this.antiPatterns = antiPatterns != null ? antiPatterns : Collections.emptyList();
            return this;
        }
        
        public Builder recommendations(List<ArchitecturalRecommendation> recommendations) {
            this.recommendations = recommendations != null ? recommendations : Collections.emptyList();
            return this;
        }
        
        public Builder metrics(ArchitecturalPatternMetrics metrics) {
            this.metrics = metrics;
            return this;
        }
        
        public Builder patternConfidenceScores(Map<String, Double> patternConfidenceScores) {
            this.patternConfidenceScores = patternConfidenceScores != null ? patternConfidenceScores : Collections.emptyMap();
            return this;
        }
        
        public ArchitecturalPatternResult build() {
            if (metrics == null) {
                throw new IllegalStateException("Metrics is required");
            }
            return new ArchitecturalPatternResult(this);
        }
    }
    
    @Override
    public String toString() {
        return String.format("ArchitecturalPatternResult{archPatterns=%d, designPatterns=%d, antiPatterns=%d}",
            architecturalPatterns.size(), designPatterns.size(), antiPatterns.size());
    }
}