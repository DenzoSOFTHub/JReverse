package it.denzosoft.jreverse.analyzer.messaging;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Container for messaging entrypoint analysis results.
 * Provides comprehensive analysis data and statistics about messaging patterns.
 */
public class MessagingAnalysisResult {
    
    private final List<MessagingEntrypointInfo> messagingEntrypoints;
    private final Map<String, List<MessagingEntrypointInfo>> entrypointsByClass;
    private final Map<MessagingEntrypointType, List<MessagingEntrypointInfo>> entrypointsByType;
    private final Map<String, List<MessagingEntrypointInfo>> entrypointsByCategory;
    private final Set<String> destinations;
    private final long analysisTimeMs;
    private final String jarFileName;
    private final MessagingQualityMetrics qualityMetrics;
    
    public MessagingAnalysisResult(List<MessagingEntrypointInfo> messagingEntrypoints,
                                 long analysisTimeMs,
                                 String jarFileName) {
        this.messagingEntrypoints = Collections.unmodifiableList(new ArrayList<>(
            messagingEntrypoints != null ? messagingEntrypoints : Collections.emptyList()));
        this.entrypointsByClass = groupByClass(this.messagingEntrypoints);
        this.entrypointsByType = groupByType(this.messagingEntrypoints);
        this.entrypointsByCategory = groupByCategory(this.messagingEntrypoints);
        this.destinations = extractDestinations(this.messagingEntrypoints);
        this.analysisTimeMs = analysisTimeMs;
        this.jarFileName = jarFileName != null ? jarFileName : "";
        this.qualityMetrics = calculateQualityMetrics();
    }
    
    // Core data accessors
    public List<MessagingEntrypointInfo> getMessagingEntrypoints() { return messagingEntrypoints; }
    public Map<String, List<MessagingEntrypointInfo>> getEntrypointsByClass() { return entrypointsByClass; }
    public Map<MessagingEntrypointType, List<MessagingEntrypointInfo>> getEntrypointsByType() { return entrypointsByType; }
    public Map<String, List<MessagingEntrypointInfo>> getEntrypointsByCategory() { return entrypointsByCategory; }
    public Set<String> getDestinations() { return destinations; }
    public long getAnalysisTimeMs() { return analysisTimeMs; }
    public String getJarFileName() { return jarFileName; }
    public MessagingQualityMetrics getQualityMetrics() { return qualityMetrics; }
    
    // Basic statistics
    public int getTotalMessagingEntrypoints() { return messagingEntrypoints.size(); }
    public int getUniqueClasses() { return entrypointsByClass.size(); }
    public int getUniqueMessagingTypes() { return entrypointsByType.size(); }
    public int getUniqueCategories() { return entrypointsByCategory.size(); }
    public int getDestinationCount() { return destinations.size(); }
    
    // Type-specific counts
    public long getListenerCount() {
        return messagingEntrypoints.stream()
            .filter(ep -> ep.getMessagingType().isListener())
            .count();
    }
    
    public long getProducerCount() {
        return messagingEntrypoints.stream()
            .filter(ep -> ep.getMessagingType().isProducer())
            .count();
    }
    
    public long getJmsCount() {
        return entrypointsByCategory.getOrDefault("JMS", Collections.emptyList()).size();
    }
    
    public long getKafkaCount() {
        return entrypointsByCategory.getOrDefault("Kafka", Collections.emptyList()).size();
    }
    
    public long getRabbitMqCount() {
        return entrypointsByCategory.getOrDefault("RabbitMQ", Collections.emptyList()).size();
    }
    
    public long getEventBasedCount() {
        return messagingEntrypoints.stream()
            .filter(ep -> ep.getMessagingType().isEventBased())
            .count();
    }
    
    public long getWebSocketCount() {
        return messagingEntrypoints.stream()
            .filter(ep -> ep.getMessagingType().isWebSocket())
            .count();
    }
    
    public long getVoidMessagingCount() {
        return messagingEntrypoints.stream()
            .filter(MessagingEntrypointInfo::isVoid)
            .count();
    }
    
    public long getAsyncMessagingCount() {
        return messagingEntrypoints.stream()
            .filter(MessagingEntrypointInfo::isAsync)
            .count();
    }
    
    public long getTransactionalCount() {
        return messagingEntrypoints.stream()
            .filter(MessagingEntrypointInfo::hasTransaction)
            .count();
    }
    
    // Risk analysis
    public List<MessagingEntrypointInfo> getHighRiskEntrypoints() {
        return messagingEntrypoints.stream()
            .filter(ep -> ep.getRiskScore() >= 70)
            .collect(Collectors.toList());
    }
    
    public List<MessagingEntrypointInfo> getComplexEntrypoints() {
        return messagingEntrypoints.stream()
            .filter(ep -> ep.getComplexityScore() >= 70)
            .collect(Collectors.toList());
    }
    
    public List<MessagingEntrypointInfo> getEntrypointsWithoutErrorHandling() {
        return messagingEntrypoints.stream()
            .filter(ep -> !ep.hasProperErrorHandling())
            .collect(Collectors.toList());
    }
    
    public List<MessagingEntrypointInfo> getTransactionalWithoutErrorHandling() {
        return messagingEntrypoints.stream()
            .filter(ep -> ep.hasTransaction() && !ep.hasProperErrorHandling())
            .collect(Collectors.toList());
    }
    
    // Technology distribution
    public Map<String, Long> getTechnologyDistribution() {
        return entrypointsByCategory.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> (long) entry.getValue().size()
            ));
    }
    
    public Map<String, Long> getDestinationUsage() {
        return messagingEntrypoints.stream()
            .collect(Collectors.groupingBy(
                MessagingEntrypointInfo::getPrimaryDestination,
                Collectors.counting()
            ));
    }
    
    // Pattern analysis
    public boolean hasCustomContainerFactories() {
        return messagingEntrypoints.stream()
            .anyMatch(MessagingEntrypointInfo::hasCustomContainerFactory);
    }
    
    public boolean hasMixedTechnologies() {
        return entrypointsByCategory.size() > 1;
    }
    
    public Map<String, Long> getPatternUsage() {
        Map<String, Long> patterns = new HashMap<>();
        
        patterns.put("Listeners", getListenerCount());
        patterns.put("Producers", getProducerCount());
        patterns.put("Event-based", getEventBasedCount());
        patterns.put("WebSocket", getWebSocketCount());
        patterns.put("Transactional", getTransactionalCount());
        
        return patterns.entrySet().stream()
            .filter(entry -> entry.getValue() > 0)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
    
    // Quality metrics
    public double getAverageComplexityScore() {
        if (messagingEntrypoints.isEmpty()) return 0.0;
        return messagingEntrypoints.stream()
            .mapToInt(MessagingEntrypointInfo::getComplexityScore)
            .average()
            .orElse(0.0);
    }
    
    public double getAverageRiskScore() {
        if (messagingEntrypoints.isEmpty()) return 0.0;
        return messagingEntrypoints.stream()
            .mapToInt(MessagingEntrypointInfo::getRiskScore)
            .average()
            .orElse(0.0);
    }
    
    public double getErrorHandlingCoverage() {
        if (messagingEntrypoints.isEmpty()) return 100.0;
        long withHandling = messagingEntrypoints.stream()
            .filter(MessagingEntrypointInfo::hasProperErrorHandling)
            .count();
        return (double) withHandling / messagingEntrypoints.size() * 100;
    }
    
    public double getAsyncPercentage() {
        if (messagingEntrypoints.isEmpty()) return 0.0;
        return (double) getAsyncMessagingCount() / getTotalMessagingEntrypoints() * 100;
    }
    
    public double getVoidPercentage() {
        if (messagingEntrypoints.isEmpty()) return 0.0;
        return (double) getVoidMessagingCount() / getTotalMessagingEntrypoints() * 100;
    }
    
    // Helper methods
    private Map<String, List<MessagingEntrypointInfo>> groupByClass(List<MessagingEntrypointInfo> entrypoints) {
        return entrypoints.stream()
            .collect(Collectors.groupingBy(MessagingEntrypointInfo::getDeclaringClass));
    }
    
    private Map<MessagingEntrypointType, List<MessagingEntrypointInfo>> groupByType(List<MessagingEntrypointInfo> entrypoints) {
        return entrypoints.stream()
            .collect(Collectors.groupingBy(MessagingEntrypointInfo::getMessagingType));
    }
    
    private Map<String, List<MessagingEntrypointInfo>> groupByCategory(List<MessagingEntrypointInfo> entrypoints) {
        return entrypoints.stream()
            .collect(Collectors.groupingBy(MessagingEntrypointInfo::getCategory));
    }
    
    private Set<String> extractDestinations(List<MessagingEntrypointInfo> entrypoints) {
        return entrypoints.stream()
            .map(MessagingEntrypointInfo::getPrimaryDestination)
            .filter(dest -> !"unknown".equals(dest))
            .collect(Collectors.toSet());
    }
    
    private MessagingQualityMetrics calculateQualityMetrics() {
        if (messagingEntrypoints.isEmpty()) {
            return new MessagingQualityMetrics(0.0, 0.0, 100.0, 0.0, 0.0, "A");
        }
        
        double avgComplexity = getAverageComplexityScore();
        double avgRisk = getAverageRiskScore();
        double errorCoverage = getErrorHandlingCoverage();
        double asyncPercentage = getAsyncPercentage();
        double voidPercentage = getVoidPercentage();
        
        String grade = calculateGrade(avgComplexity, avgRisk, errorCoverage);
        
        return new MessagingQualityMetrics(avgComplexity, avgRisk, errorCoverage, 
                                         asyncPercentage, voidPercentage, grade);
    }
    
    private String calculateGrade(double complexity, double risk, double errorCoverage) {
        double score = Math.max(0, 100 - (complexity * 0.3 + risk * 0.4 + (100 - errorCoverage) * 0.3));
        if (score >= 90) return "A";
        if (score >= 80) return "B";
        if (score >= 70) return "C";
        if (score >= 60) return "D";
        return "F";
    }
    
    /**
     * Quality metrics for messaging analysis.
     */
    public static class MessagingQualityMetrics {
        private final double averageComplexityScore;
        private final double averageRiskScore;
        private final double errorHandlingCoverage;
        private final double asyncPercentage;
        private final double voidPercentage;
        private final String grade;
        
        public MessagingQualityMetrics(double averageComplexityScore, double averageRiskScore,
                                     double errorHandlingCoverage, double asyncPercentage, 
                                     double voidPercentage, String grade) {
            this.averageComplexityScore = averageComplexityScore;
            this.averageRiskScore = averageRiskScore;
            this.errorHandlingCoverage = errorHandlingCoverage;
            this.asyncPercentage = asyncPercentage;
            this.voidPercentage = voidPercentage;
            this.grade = grade;
        }
        
        public double getAverageComplexityScore() { return averageComplexityScore; }
        public double getAverageRiskScore() { return averageRiskScore; }
        public double getErrorHandlingCoverage() { return errorHandlingCoverage; }
        public double getAsyncPercentage() { return asyncPercentage; }
        public double getVoidPercentage() { return voidPercentage; }
        public String getGrade() { return grade; }
    }
    
    @Override
    public String toString() {
        return "MessagingAnalysisResult{" +
                "totalEntrypoints=" + getTotalMessagingEntrypoints() +
                ", uniqueClasses=" + getUniqueClasses() +
                ", technologies=" + getUniqueCategories() +
                ", destinations=" + getDestinationCount() +
                ", avgComplexity=" + String.format("%.1f", getAverageComplexityScore()) +
                ", avgRisk=" + String.format("%.1f", getAverageRiskScore()) +
                ", errorCoverage=" + String.format("%.1f", getErrorHandlingCoverage()) + "%" +
                ", grade=" + qualityMetrics.getGrade() +
                ", analysisTime=" + analysisTimeMs + "ms" +
                '}';
    }
}