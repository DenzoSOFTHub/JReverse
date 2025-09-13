package it.denzosoft.jreverse.core.model.springboot;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Result of Spring Boot detection analysis.
 * Contains confidence score, detected features, and detailed evidence.
 */
public final class SpringBootDetectionResult {
    
    private final boolean isSpringBootApplication;
    private final double overallConfidence;
    private final SpringBootVersion detectedVersion;
    private final Map<SpringBootIndicatorType, IndicatorResult> indicatorResults;
    private final Set<String> detectedFeatures;
    private final long analysisTimestamp;
    
    private SpringBootDetectionResult(Builder builder) {
        this.overallConfidence = Math.max(0.0, Math.min(1.0, builder.overallConfidence));
        this.isSpringBootApplication = builder.isSpringBootApplication;
        this.detectedVersion = builder.detectedVersion;
        this.indicatorResults = Collections.unmodifiableMap(new HashMap<>(builder.indicatorResults));
        this.detectedFeatures = Collections.unmodifiableSet(builder.detectedFeatures);
        this.analysisTimestamp = builder.analysisTimestamp > 0 ? builder.analysisTimestamp : System.currentTimeMillis();
    }
    
    public boolean isSpringBootApplication() {
        return isSpringBootApplication;
    }
    
    public double getOverallConfidence() {
        return overallConfidence;
    }
    
    public SpringBootVersion getDetectedVersion() {
        return detectedVersion;
    }
    
    public Map<SpringBootIndicatorType, IndicatorResult> getIndicatorResults() {
        return indicatorResults;
    }
    
    public Set<String> getDetectedFeatures() {
        return detectedFeatures;
    }
    
    public long getAnalysisTimestamp() {
        return analysisTimestamp;
    }
    
    /**
     * Gets the confidence level as a human-readable string.
     */
    public String getConfidenceLevel() {
        if (overallConfidence >= 0.9) return "VERY_HIGH";
        if (overallConfidence >= 0.75) return "HIGH";
        if (overallConfidence >= 0.6) return "MEDIUM";
        if (overallConfidence >= 0.4) return "LOW";
        return "VERY_LOW";
    }
    
    /**
     * Gets the number of successful indicators.
     */
    public long getSuccessfulIndicatorCount() {
        return indicatorResults.values().stream()
            .filter(IndicatorResult::isSuccessful)
            .count();
    }
    
    /**
     * Gets the evidence summary for reporting.
     */
    public Map<String, Object> getEvidenceSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("confidence", overallConfidence);
        summary.put("confidenceLevel", getConfidenceLevel());
        summary.put("isSpringBoot", isSpringBootApplication);
        summary.put("version", detectedVersion != null ? detectedVersion.getVersionString() : null);
        summary.put("indicators", indicatorResults.size());
        summary.put("successfulIndicators", getSuccessfulIndicatorCount());
        summary.put("detectedFeatures", detectedFeatures);
        return Collections.unmodifiableMap(summary);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SpringBootDetectionResult that = (SpringBootDetectionResult) obj;
        return isSpringBootApplication == that.isSpringBootApplication &&
               Double.compare(that.overallConfidence, overallConfidence) == 0 &&
               Objects.equals(detectedVersion, that.detectedVersion) &&
               Objects.equals(indicatorResults, that.indicatorResults);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(isSpringBootApplication, overallConfidence, detectedVersion, indicatorResults);
    }
    
    @Override
    public String toString() {
        return "SpringBootDetectionResult{" +
                "isSpringBoot=" + isSpringBootApplication +
                ", confidence=" + String.format("%.2f", overallConfidence) +
                ", version=" + (detectedVersion != null ? detectedVersion.getVersionString() : "unknown") +
                ", indicators=" + indicatorResults.size() +
                '}';
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static SpringBootDetectionResult noEvidence() {
        return builder()
            .isSpringBootApplication(false)
            .overallConfidence(0.0)
            .build();
    }
    
    public static SpringBootDetectionResult definiteSpringBoot(SpringBootVersion version) {
        return builder()
            .isSpringBootApplication(true)
            .overallConfidence(1.0)
            .detectedVersion(version)
            .build();
    }
    
    public static class Builder {
        private boolean isSpringBootApplication = false;
        private double overallConfidence = 0.0;
        private SpringBootVersion detectedVersion;
        private Map<SpringBootIndicatorType, IndicatorResult> indicatorResults = new HashMap<>();
        private Set<String> detectedFeatures = new HashSet<>();
        private long analysisTimestamp = 0;
        
        public Builder isSpringBootApplication(boolean isSpringBootApplication) {
            this.isSpringBootApplication = isSpringBootApplication;
            return this;
        }
        
        public Builder overallConfidence(double confidence) {
            this.overallConfidence = confidence;
            return this;
        }
        
        public Builder detectedVersion(SpringBootVersion version) {
            this.detectedVersion = version;
            return this;
        }
        
        public Builder addIndicatorResult(SpringBootIndicatorType type, IndicatorResult result) {
            if (type != null && result != null) {
                this.indicatorResults.put(type, result);
            }
            return this;
        }
        
        public Builder indicatorResults(Map<SpringBootIndicatorType, IndicatorResult> results) {
            this.indicatorResults = new HashMap<>(results != null ? results : Collections.emptyMap());
            return this;
        }
        
        public Builder addDetectedFeature(String feature) {
            if (feature != null && !feature.trim().isEmpty()) {
                this.detectedFeatures.add(feature.trim());
            }
            return this;
        }
        
        public Builder detectedFeatures(Set<String> features) {
            this.detectedFeatures = new HashSet<>(features != null ? features : Collections.emptySet());
            return this;
        }
        
        public Builder analysisTimestamp(long timestamp) {
            this.analysisTimestamp = timestamp;
            return this;
        }
        
        public SpringBootDetectionResult build() {
            // Auto-calculate confidence if not set and we have indicator results
            if (overallConfidence == 0.0 && !indicatorResults.isEmpty()) {
                this.overallConfidence = calculateWeightedConfidence();
                this.isSpringBootApplication = overallConfidence >= 0.6; // Default threshold
            }
            
            return new SpringBootDetectionResult(this);
        }
        
        private double calculateWeightedConfidence() {
            double totalWeight = 0.0;
            double weightedSum = 0.0;
            
            for (Map.Entry<SpringBootIndicatorType, IndicatorResult> entry : indicatorResults.entrySet()) {
                IndicatorResult result = entry.getValue();
                if (result.isSuccessful()) {
                    double weight = entry.getKey().getWeight();
                    double confidence = result.getConfidence();
                    
                    totalWeight += weight;
                    weightedSum += weight * confidence;
                }
            }
            
            return totalWeight > 0 ? weightedSum / totalWeight : 0.0;
        }
    }
}