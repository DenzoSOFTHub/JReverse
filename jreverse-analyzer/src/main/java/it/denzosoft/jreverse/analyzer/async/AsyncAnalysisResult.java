package it.denzosoft.jreverse.analyzer.async;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Container for async entrypoint analysis results.
 * Provides comprehensive analysis data and statistics about async patterns.
 */
public class AsyncAnalysisResult {
    
    private final List<AsyncEntrypointInfo> asyncEntrypoints;
    private final Map<String, List<AsyncEntrypointInfo>> entrypointsByClass;
    private final Map<AsyncEntrypointType, List<AsyncEntrypointInfo>> entrypointsByType;
    private final Set<String> executorNames;
    private final long analysisTimeMs;
    private final String jarFileName;
    private final AsyncQualityMetrics qualityMetrics;
    
    public AsyncAnalysisResult(List<AsyncEntrypointInfo> asyncEntrypoints,
                               long analysisTimeMs,
                               String jarFileName) {
        this.asyncEntrypoints = Collections.unmodifiableList(new ArrayList<>(
            asyncEntrypoints != null ? asyncEntrypoints : Collections.emptyList()));
        this.entrypointsByClass = groupByClass(this.asyncEntrypoints);
        this.entrypointsByType = groupByType(this.asyncEntrypoints);
        this.executorNames = extractExecutorNames(this.asyncEntrypoints);
        this.analysisTimeMs = analysisTimeMs;
        this.jarFileName = jarFileName != null ? jarFileName : "";
        this.qualityMetrics = calculateQualityMetrics();
    }
    
    // Core data accessors
    public List<AsyncEntrypointInfo> getAsyncEntrypoints() { return asyncEntrypoints; }
    public Map<String, List<AsyncEntrypointInfo>> getEntrypointsByClass() { return entrypointsByClass; }
    public Map<AsyncEntrypointType, List<AsyncEntrypointInfo>> getEntrypointsByType() { return entrypointsByType; }
    public Set<String> getExecutorNames() { return executorNames; }
    public long getAnalysisTimeMs() { return analysisTimeMs; }
    public String getJarFileName() { return jarFileName; }
    public AsyncQualityMetrics getQualityMetrics() { return qualityMetrics; }
    
    // Basic statistics
    public int getTotalAsyncEntrypoints() { return asyncEntrypoints.size(); }
    public int getUniqueClasses() { return entrypointsByClass.size(); }
    public int getUniqueAsyncTypes() { return entrypointsByType.size(); }
    public int getExecutorCount() { return executorNames.size(); }
    
    // Type-specific counts
    public long getAsyncMethodCount() {
        return getCountByType(AsyncEntrypointType.ASYNC_METHOD);
    }
    
    public long getCompletableFutureCount() {
        return asyncEntrypoints.stream()
            .filter(AsyncEntrypointInfo::isCompletableFuture)
            .count();
    }
    
    public long getReactiveCount() {
        return asyncEntrypoints.stream()
            .filter(ep -> ep.getAsyncType().isReactive())
            .count();
    }
    
    public long getVoidAsyncCount() {
        return asyncEntrypoints.stream()
            .filter(AsyncEntrypointInfo::isVoid)
            .count();
    }
    
    // Risk analysis
    public List<AsyncEntrypointInfo> getHighRiskEntrypoints() {
        return asyncEntrypoints.stream()
            .filter(ep -> ep.getRiskScore() >= 70)
            .collect(Collectors.toList());
    }
    
    public List<AsyncEntrypointInfo> getEntrypointsWithoutErrorHandling() {
        return asyncEntrypoints.stream()
            .filter(ep -> !ep.hasProperErrorHandling())
            .collect(Collectors.toList());
    }
    
    public List<AsyncEntrypointInfo> getEntrypointsWithThreadingRisk() {
        return asyncEntrypoints.stream()
            .filter(AsyncEntrypointInfo::hasThreadingRisk)
            .collect(Collectors.toList());
    }
    
    // Executor analysis
    public Map<String, Long> getExecutorUsage() {
        return asyncEntrypoints.stream()
            .collect(Collectors.groupingBy(
                AsyncEntrypointInfo::getExecutorName,
                Collectors.counting()
            ));
    }
    
    public boolean hasCustomExecutors() {
        return executorNames.stream()
            .anyMatch(name -> !"default".equals(name));
    }
    
    // Quality metrics
    public double getAverageRiskScore() {
        if (asyncEntrypoints.isEmpty()) return 0.0;
        return asyncEntrypoints.stream()
            .mapToInt(AsyncEntrypointInfo::getRiskScore)
            .average()
            .orElse(0.0);
    }
    
    public double getErrorHandlingCoverage() {
        if (asyncEntrypoints.isEmpty()) return 100.0;
        long withHandling = asyncEntrypoints.stream()
            .filter(AsyncEntrypointInfo::hasProperErrorHandling)
            .count();
        return (double) withHandling / asyncEntrypoints.size() * 100;
    }
    
    // Helper methods
    private long getCountByType(AsyncEntrypointType type) {
        return entrypointsByType.getOrDefault(type, Collections.emptyList()).size();
    }
    
    private Map<String, List<AsyncEntrypointInfo>> groupByClass(List<AsyncEntrypointInfo> entrypoints) {
        return entrypoints.stream()
            .collect(Collectors.groupingBy(AsyncEntrypointInfo::getDeclaringClass));
    }
    
    private Map<AsyncEntrypointType, List<AsyncEntrypointInfo>> groupByType(List<AsyncEntrypointInfo> entrypoints) {
        return entrypoints.stream()
            .collect(Collectors.groupingBy(AsyncEntrypointInfo::getAsyncType));
    }
    
    private Set<String> extractExecutorNames(List<AsyncEntrypointInfo> entrypoints) {
        return entrypoints.stream()
            .map(AsyncEntrypointInfo::getExecutorName)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }
    
    private AsyncQualityMetrics calculateQualityMetrics() {
        if (asyncEntrypoints.isEmpty()) {
            return new AsyncQualityMetrics(0.0, 100.0, 0.0, 0.0, "A");
        }
        
        double avgRiskScore = getAverageRiskScore();
        double errorCoverage = getErrorHandlingCoverage();
        double voidPercentage = (double) getVoidAsyncCount() / getTotalAsyncEntrypoints() * 100;
        double reactivePercentage = (double) getReactiveCount() / getTotalAsyncEntrypoints() * 100;
        
        String grade = calculateGrade(avgRiskScore, errorCoverage);
        
        return new AsyncQualityMetrics(avgRiskScore, errorCoverage, voidPercentage, 
                                      reactivePercentage, grade);
    }
    
    private String calculateGrade(double riskScore, double errorCoverage) {
        double score = (100 - riskScore) * 0.6 + errorCoverage * 0.4;
        if (score >= 90) return "A";
        if (score >= 80) return "B";
        if (score >= 70) return "C";
        if (score >= 60) return "D";
        return "F";
    }
    
    /**
     * Quality metrics for async analysis.
     */
    public static class AsyncQualityMetrics {
        private final double averageRiskScore;
        private final double errorHandlingCoverage;
        private final double voidAsyncPercentage;
        private final double reactivePercentage;
        private final String grade;
        
        public AsyncQualityMetrics(double averageRiskScore, double errorHandlingCoverage,
                                  double voidAsyncPercentage, double reactivePercentage, String grade) {
            this.averageRiskScore = averageRiskScore;
            this.errorHandlingCoverage = errorHandlingCoverage;
            this.voidAsyncPercentage = voidAsyncPercentage;
            this.reactivePercentage = reactivePercentage;
            this.grade = grade;
        }
        
        public double getAverageRiskScore() { return averageRiskScore; }
        public double getErrorHandlingCoverage() { return errorHandlingCoverage; }
        public double getVoidAsyncPercentage() { return voidAsyncPercentage; }
        public double getReactivePercentage() { return reactivePercentage; }
        public String getGrade() { return grade; }
    }
    
    @Override
    public String toString() {
        return "AsyncAnalysisResult{" +
                "totalEntrypoints=" + getTotalAsyncEntrypoints() +
                ", uniqueClasses=" + getUniqueClasses() +
                ", executors=" + getExecutorCount() +
                ", avgRisk=" + String.format("%.1f", getAverageRiskScore()) +
                ", grade=" + qualityMetrics.getGrade() +
                ", analysisTime=" + analysisTimeMs + "ms" +
                '}';
    }
}