package it.denzosoft.jreverse.analyzer.bootstrap;

import java.util.Map;

/**
 * Timing information for Spring Boot bootstrap sequence phases.
 * Contains estimated durations for each phase based on static analysis.
 */
public class BootstrapTimingInfo {
    
    private final long totalEstimatedDurationMs;
    private final Map<BootstrapSequencePhase, Long> phaseDurations;
    private final long analysisExecutionTimeMs;
    private final int totalStepCount;
    
    public BootstrapTimingInfo(long totalEstimatedDurationMs,
                              Map<BootstrapSequencePhase, Long> phaseDurations,
                              long analysisExecutionTimeMs,
                              int totalStepCount) {
        this.totalEstimatedDurationMs = totalEstimatedDurationMs;
        this.phaseDurations = Map.copyOf(phaseDurations);
        this.analysisExecutionTimeMs = analysisExecutionTimeMs;
        this.totalStepCount = totalStepCount;
    }
    
    // Getters
    public long getTotalEstimatedDurationMs() { return totalEstimatedDurationMs; }
    public Map<BootstrapSequencePhase, Long> getPhaseDurations() { return phaseDurations; }
    public long getAnalysisExecutionTimeMs() { return analysisExecutionTimeMs; }
    public int getTotalStepCount() { return totalStepCount; }
    
    /**
     * Returns the estimated duration for a specific phase.
     */
    public long getPhaseDuration(BootstrapSequencePhase phase) {
        return phaseDurations.getOrDefault(phase, 0L);
    }
    
    /**
     * Returns the percentage of total time spent in a specific phase.
     */
    public double getPhasePercentage(BootstrapSequencePhase phase) {
        if (totalEstimatedDurationMs == 0) {
            return 0.0;
        }
        return (getPhaseDuration(phase) * 100.0) / totalEstimatedDurationMs;
    }
    
    /**
     * Returns the phase that takes the longest time.
     */
    public BootstrapSequencePhase getLongestPhase() {
        return phaseDurations.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(BootstrapSequencePhase.MAIN_METHOD_EXECUTION);
    }
    
    /**
     * Returns the phase that takes the shortest time.
     */
    public BootstrapSequencePhase getShortestPhase() {
        return phaseDurations.entrySet().stream()
            .filter(entry -> entry.getValue() > 0)
            .min(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(BootstrapSequencePhase.MAIN_METHOD_EXECUTION);
    }
    
    /**
     * Checks if timing information is available.
     */
    public boolean hasTimingData() {
        return totalEstimatedDurationMs > 0 || !phaseDurations.isEmpty();
    }
    
    /**
     * Returns formatted total duration as a human-readable string.
     */
    public String getFormattedTotalDuration() {
        if (totalEstimatedDurationMs < 1000) {
            return totalEstimatedDurationMs + " ms";
        } else if (totalEstimatedDurationMs < 60000) {
            return String.format("%.2f seconds", totalEstimatedDurationMs / 1000.0);
        } else {
            long minutes = totalEstimatedDurationMs / 60000;
            long seconds = (totalEstimatedDurationMs % 60000) / 1000;
            return String.format("%d min %d sec", minutes, seconds);
        }
    }
    
    /**
     * Returns formatted phase duration as a human-readable string.
     */
    public String getFormattedPhaseDuration(BootstrapSequencePhase phase) {
        long duration = getPhaseDuration(phase);
        if (duration < 1000) {
            return duration + " ms";
        } else {
            return String.format("%.2f sec", duration / 1000.0);
        }
    }
    
    /**
     * Factory method for creating empty timing info.
     */
    public static BootstrapTimingInfo empty() {
        return new BootstrapTimingInfo(0, Map.of(), 0, 0);
    }
    
    /**
     * Factory method for creating minimal timing info.
     */
    public static BootstrapTimingInfo minimal() {
        Map<BootstrapSequencePhase, Long> basicPhases = Map.of(
            BootstrapSequencePhase.MAIN_METHOD_EXECUTION, 10L
        );
        return new BootstrapTimingInfo(10, basicPhases, 0, 1);
    }
    
    /**
     * Factory method for creating timing info from sequence steps.
     */
    public static BootstrapTimingInfo fromSequenceSteps(java.util.List<BootstrapSequenceStep> steps, 
                                                        long analysisExecutionTimeMs) {
        if (steps.isEmpty()) {
            return empty();
        }
        
        // Calculate phase durations
        Map<BootstrapSequencePhase, Long> phaseDurations = steps.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                BootstrapSequenceStep::getPhase,
                java.util.stream.Collectors.summingLong(BootstrapSequenceStep::getEstimatedDurationMs)
            ));
        
        // Calculate total duration
        long totalDuration = steps.stream()
            .mapToLong(BootstrapSequenceStep::getEstimatedDurationMs)
            .sum();
        
        return new BootstrapTimingInfo(totalDuration, phaseDurations, analysisExecutionTimeMs, steps.size());
    }
    
    /**
     * Builder for creating BootstrapTimingInfo with default estimates.
     */
    public static class Builder {
        private long totalEstimatedDurationMs = 0;
        private Map<BootstrapSequencePhase, Long> phaseDurations = new java.util.HashMap<>();
        private long analysisExecutionTimeMs = 0;
        private int totalStepCount = 0;
        
        public Builder totalEstimatedDurationMs(long totalEstimatedDurationMs) {
            this.totalEstimatedDurationMs = totalEstimatedDurationMs;
            return this;
        }
        
        public Builder phaseDuration(BootstrapSequencePhase phase, long durationMs) {
            this.phaseDurations.put(phase, durationMs);
            return this;
        }
        
        public Builder analysisExecutionTimeMs(long analysisExecutionTimeMs) {
            this.analysisExecutionTimeMs = analysisExecutionTimeMs;
            return this;
        }
        
        public Builder totalStepCount(int totalStepCount) {
            this.totalStepCount = totalStepCount;
            return this;
        }
        
        public BootstrapTimingInfo build() {
            // If total duration not set, calculate from phase durations
            if (totalEstimatedDurationMs == 0 && !phaseDurations.isEmpty()) {
                totalEstimatedDurationMs = phaseDurations.values().stream()
                    .mapToLong(Long::longValue)
                    .sum();
            }
            
            return new BootstrapTimingInfo(totalEstimatedDurationMs, phaseDurations, 
                                         analysisExecutionTimeMs, totalStepCount);
        }
    }
    
    @Override
    public String toString() {
        return String.format("BootstrapTiming[total=%s, phases=%d, steps=%d]", 
            getFormattedTotalDuration(), phaseDurations.size(), totalStepCount);
    }
}