package it.denzosoft.jreverse.analyzer.scheduling;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Container for scheduling entrypoint analysis results.
 * Provides comprehensive analysis data and statistics about scheduling patterns.
 */
public class SchedulingAnalysisResult {
    
    private final List<SchedulingEntrypointInfo> schedulingEntrypoints;
    private final Map<String, List<SchedulingEntrypointInfo>> entrypointsByClass;
    private final Map<SchedulingEntrypointType, List<SchedulingEntrypointInfo>> entrypointsByType;
    private final Set<String> schedulerNames;
    private final long analysisTimeMs;
    private final String jarFileName;
    private final SchedulingQualityMetrics qualityMetrics;
    
    public SchedulingAnalysisResult(List<SchedulingEntrypointInfo> schedulingEntrypoints,
                                  long analysisTimeMs,
                                  String jarFileName) {
        this.schedulingEntrypoints = Collections.unmodifiableList(new ArrayList<>(
            schedulingEntrypoints != null ? schedulingEntrypoints : Collections.emptyList()));
        this.entrypointsByClass = groupByClass(this.schedulingEntrypoints);
        this.entrypointsByType = groupByType(this.schedulingEntrypoints);
        this.schedulerNames = extractSchedulerNames(this.schedulingEntrypoints);
        this.analysisTimeMs = analysisTimeMs;
        this.jarFileName = jarFileName != null ? jarFileName : "";
        this.qualityMetrics = calculateQualityMetrics();
    }
    
    // Core data accessors
    public List<SchedulingEntrypointInfo> getSchedulingEntrypoints() { return schedulingEntrypoints; }
    public Map<String, List<SchedulingEntrypointInfo>> getEntrypointsByClass() { return entrypointsByClass; }
    public Map<SchedulingEntrypointType, List<SchedulingEntrypointInfo>> getEntrypointsByType() { return entrypointsByType; }
    public Set<String> getSchedulerNames() { return schedulerNames; }
    public long getAnalysisTimeMs() { return analysisTimeMs; }
    public String getJarFileName() { return jarFileName; }
    public SchedulingQualityMetrics getQualityMetrics() { return qualityMetrics; }
    
    // Basic statistics
    public int getTotalSchedulingEntrypoints() { return schedulingEntrypoints.size(); }
    public int getUniqueClasses() { return entrypointsByClass.size(); }
    public int getUniqueSchedulingTypes() { return entrypointsByType.size(); }
    public int getSchedulerCount() { return schedulerNames.size(); }
    
    // Type-specific counts
    public long getScheduledMethodCount() {
        return getCountByType(SchedulingEntrypointType.SCHEDULED_METHOD);
    }
    
    public long getCronExpressionCount() {
        return schedulingEntrypoints.stream()
            .filter(SchedulingEntrypointInfo::hasCronExpression)
            .count();
    }
    
    public long getFixedRateCount() {
        return schedulingEntrypoints.stream()
            .filter(SchedulingEntrypointInfo::hasFixedRate)
            .count();
    }
    
    public long getFixedDelayCount() {
        return schedulingEntrypoints.stream()
            .filter(SchedulingEntrypointInfo::hasFixedDelay)
            .count();
    }
    
    public long getAsyncScheduledCount() {
        return schedulingEntrypoints.stream()
            .filter(SchedulingEntrypointInfo::isAsync)
            .count();
    }
    
    public long getVoidScheduledCount() {
        return schedulingEntrypoints.stream()
            .filter(SchedulingEntrypointInfo::isVoid)
            .count();
    }
    
    // Risk analysis
    public List<SchedulingEntrypointInfo> getHighRiskEntrypoints() {
        return schedulingEntrypoints.stream()
            .filter(ep -> ep.getRiskScore() >= 70)
            .collect(Collectors.toList());
    }
    
    public List<SchedulingEntrypointInfo> getComplexEntrypoints() {
        return schedulingEntrypoints.stream()
            .filter(ep -> ep.getComplexityScore() >= 70)
            .collect(Collectors.toList());
    }
    
    public List<SchedulingEntrypointInfo> getShortIntervalEntrypoints() {
        return schedulingEntrypoints.stream()
            .filter(ep -> (ep.hasFixedRate() && ep.getFixedRate() < 1000) ||
                         (ep.hasFixedDelay() && ep.getFixedDelay() < 1000))
            .collect(Collectors.toList());
    }
    
    // Scheduler analysis
    public Map<String, Long> getSchedulerUsage() {
        return schedulingEntrypoints.stream()
            .collect(Collectors.groupingBy(
                SchedulingEntrypointInfo::getSchedulerName,
                Collectors.counting()
            ));
    }
    
    public boolean hasCustomSchedulers() {
        return schedulerNames.stream()
            .anyMatch(name -> !"default".equals(name));
    }
    
    // Frequency analysis
    public Map<String, Long> getFrequencyPatterns() {
        Map<String, Long> patterns = new HashMap<>();
        
        long cronCount = getCronExpressionCount();
        long fixedRateCount = getFixedRateCount();
        long fixedDelayCount = getFixedDelayCount();
        
        if (cronCount > 0) patterns.put("Cron", cronCount);
        if (fixedRateCount > 0) patterns.put("Fixed Rate", fixedRateCount);
        if (fixedDelayCount > 0) patterns.put("Fixed Delay", fixedDelayCount);
        
        return patterns;
    }
    
    // Quality metrics
    public double getAverageComplexityScore() {
        if (schedulingEntrypoints.isEmpty()) return 0.0;
        return schedulingEntrypoints.stream()
            .mapToInt(SchedulingEntrypointInfo::getComplexityScore)
            .average()
            .orElse(0.0);
    }
    
    public double getAverageRiskScore() {
        if (schedulingEntrypoints.isEmpty()) return 0.0;
        return schedulingEntrypoints.stream()
            .mapToInt(SchedulingEntrypointInfo::getRiskScore)
            .average()
            .orElse(0.0);
    }
    
    public double getAsyncPercentage() {
        if (schedulingEntrypoints.isEmpty()) return 0.0;
        return (double) getAsyncScheduledCount() / getTotalSchedulingEntrypoints() * 100;
    }
    
    public double getVoidPercentage() {
        if (schedulingEntrypoints.isEmpty()) return 0.0;
        return (double) getVoidScheduledCount() / getTotalSchedulingEntrypoints() * 100;
    }
    
    // Helper methods
    private long getCountByType(SchedulingEntrypointType type) {
        return entrypointsByType.getOrDefault(type, Collections.emptyList()).size();
    }
    
    private Map<String, List<SchedulingEntrypointInfo>> groupByClass(List<SchedulingEntrypointInfo> entrypoints) {
        return entrypoints.stream()
            .collect(Collectors.groupingBy(SchedulingEntrypointInfo::getDeclaringClass));
    }
    
    private Map<SchedulingEntrypointType, List<SchedulingEntrypointInfo>> groupByType(List<SchedulingEntrypointInfo> entrypoints) {
        return entrypoints.stream()
            .collect(Collectors.groupingBy(SchedulingEntrypointInfo::getSchedulingType));
    }
    
    private Set<String> extractSchedulerNames(List<SchedulingEntrypointInfo> entrypoints) {
        return entrypoints.stream()
            .map(SchedulingEntrypointInfo::getSchedulerName)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }
    
    private SchedulingQualityMetrics calculateQualityMetrics() {
        if (schedulingEntrypoints.isEmpty()) {
            return new SchedulingQualityMetrics(0.0, 0.0, 0.0, 0.0, "A");
        }
        
        double avgComplexity = getAverageComplexityScore();
        double avgRisk = getAverageRiskScore();
        double asyncPercentage = getAsyncPercentage();
        double voidPercentage = getVoidPercentage();
        
        String grade = calculateGrade(avgComplexity, avgRisk);
        
        return new SchedulingQualityMetrics(avgComplexity, avgRisk, asyncPercentage, 
                                          voidPercentage, grade);
    }
    
    private String calculateGrade(double complexity, double risk) {
        double score = Math.max(0, 100 - (complexity * 0.4 + risk * 0.6));
        if (score >= 90) return "A";
        if (score >= 80) return "B";
        if (score >= 70) return "C";
        if (score >= 60) return "D";
        return "F";
    }
    
    /**
     * Quality metrics for scheduling analysis.
     */
    public static class SchedulingQualityMetrics {
        private final double averageComplexityScore;
        private final double averageRiskScore;
        private final double asyncPercentage;
        private final double voidPercentage;
        private final String grade;
        
        public SchedulingQualityMetrics(double averageComplexityScore, double averageRiskScore,
                                      double asyncPercentage, double voidPercentage, String grade) {
            this.averageComplexityScore = averageComplexityScore;
            this.averageRiskScore = averageRiskScore;
            this.asyncPercentage = asyncPercentage;
            this.voidPercentage = voidPercentage;
            this.grade = grade;
        }
        
        public double getAverageComplexityScore() { return averageComplexityScore; }
        public double getAverageRiskScore() { return averageRiskScore; }
        public double getAsyncPercentage() { return asyncPercentage; }
        public double getVoidPercentage() { return voidPercentage; }
        public String getGrade() { return grade; }
    }
    
    @Override
    public String toString() {
        return "SchedulingAnalysisResult{" +
                "totalEntrypoints=" + getTotalSchedulingEntrypoints() +
                ", uniqueClasses=" + getUniqueClasses() +
                ", schedulers=" + getSchedulerCount() +
                ", avgComplexity=" + String.format("%.1f", getAverageComplexityScore()) +
                ", avgRisk=" + String.format("%.1f", getAverageRiskScore()) +
                ", grade=" + qualityMetrics.getGrade() +
                ", analysisTime=" + analysisTimeMs + "ms" +
                '}';
    }
}