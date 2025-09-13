package it.denzosoft.jreverse.analyzer.restcontroller;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Container for REST endpoint response analysis results.
 * Provides comprehensive analysis data and statistics about response patterns.
 */
public class ResponseAnalysisResult {
    
    private final List<ResponseInfo> responses;
    private final Map<String, List<ResponseInfo>> responsesByClass;
    private final Map<ResponseType, List<ResponseInfo>> responsesByType;
    private final long analysisTimeMs;
    private final String jarFileName;
    
    public ResponseAnalysisResult(List<ResponseInfo> responses, long analysisTimeMs, String jarFileName) {
        this.responses = Collections.unmodifiableList(new ArrayList<>(responses != null ? responses : Collections.emptyList()));
        this.responsesByClass = groupResponsesByClass(this.responses);
        this.responsesByType = groupResponsesByType(this.responses);
        this.analysisTimeMs = analysisTimeMs;
        this.jarFileName = jarFileName != null ? jarFileName : "";
    }
    
    // Core data accessors
    public List<ResponseInfo> getResponses() { return responses; }
    public Map<String, List<ResponseInfo>> getResponsesByClass() { return responsesByClass; }
    public Map<ResponseType, List<ResponseInfo>> getResponsesByType() { return responsesByType; }
    public long getAnalysisTimeMs() { return analysisTimeMs; }
    public String getJarFileName() { return jarFileName; }
    
    // Basic statistics
    public int getTotalResponses() { return responses.size(); }
    public int getUniqueClasses() { return responsesByClass.size(); }
    public int getUniqueResponseTypes() { return responsesByType.size(); }
    
    // Response body analysis
    public long getResponsesWithBody() {
        return responses.stream().filter(ResponseInfo::hasResponseBody).count();
    }
    
    public long getResponsesWithoutBody() {
        return responses.stream().filter(r -> !r.hasResponseBody()).count();
    }
    
    public double getResponseBodyPercentage() {
        return getTotalResponses() > 0 ? (getResponsesWithBody() * 100.0) / getTotalResponses() : 0.0;
    }
    
    // Response type distribution
    public long getVoidResponses() {
        return getResponsesByType().getOrDefault(ResponseType.VOID, Collections.emptyList()).size();
    }
    
    public long getPrimitiveResponses() {
        return getResponsesByType().getOrDefault(ResponseType.PRIMITIVE, Collections.emptyList()).size() +
               getResponsesByType().getOrDefault(ResponseType.STRING, Collections.emptyList()).size();
    }
    
    public long getObjectResponses() {
        return getResponsesByType().getOrDefault(ResponseType.OBJECT, Collections.emptyList()).size();
    }
    
    public long getCollectionResponses() {
        return getResponsesByType().getOrDefault(ResponseType.COLLECTION, Collections.emptyList()).size() +
               getResponsesByType().getOrDefault(ResponseType.ARRAY, Collections.emptyList()).size();
    }
    
    public long getWrappedResponses() {
        return responses.stream().filter(ResponseInfo::isWrappedResponse).count();
    }
    
    public long getAsyncResponses() {
        return responses.stream().filter(ResponseInfo::isAsync).count();
    }
    
    // HTTP status analysis
    public long getResponsesWithExplicitStatus() {
        return responses.stream().filter(ResponseInfo::hasExplicitStatus).count();
    }
    
    public Set<String> getUniqueHttpStatuses() {
        return responses.stream()
            .filter(ResponseInfo::hasExplicitStatus)
            .map(ResponseInfo::getHttpStatus)
            .collect(Collectors.toSet());
    }
    
    // Content type analysis
    public long getResponsesWithContentTypeRestrictions() {
        return responses.stream().filter(ResponseInfo::hasContentTypeRestrictions).count();
    }
    
    public Set<String> getUniqueContentTypes() {
        return responses.stream()
            .flatMap(r -> r.getProducesContentTypes().stream())
            .collect(Collectors.toSet());
    }
    
    public long getJsonSupportingResponses() {
        return responses.stream().filter(ResponseInfo::supportsJson).count();
    }
    
    public long getXmlSupportingResponses() {
        return responses.stream().filter(ResponseInfo::supportsXml).count();
    }
    
    // Complexity analysis
    public Map<ResponseInfo.ResponseComplexity, Long> getComplexityDistribution() {
        return responses.stream()
            .collect(Collectors.groupingBy(
                ResponseInfo::getComplexity,
                Collectors.counting()
            ));
    }
    
    public long getComplexResponses() {
        return responses.stream().filter(ResponseInfo::isComplexResponse).count();
    }
    
    // Class-level analysis
    public List<String> getClassesWithMostResponses(int limit) {
        return responsesByClass.entrySet().stream()
            .sorted((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size()))
            .limit(limit)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
    
    public Map<String, Long> getResponseCountsByClass() {
        return responsesByClass.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> (long) entry.getValue().size()
            ));
    }
    
    // Most common patterns
    public List<ResponseType> getMostCommonResponseTypes(int limit) {
        return responsesByType.entrySet().stream()
            .sorted((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size()))
            .limit(limit)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
    
    public List<String> getMostCommonReturnTypes(int limit) {
        return responses.stream()
            .collect(Collectors.groupingBy(ResponseInfo::getReturnType, Collectors.counting()))
            .entrySet().stream()
            .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
            .limit(limit)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
    
    // Quality metrics
    public double getResponseConsistencyScore() {
        if (responses.isEmpty()) return 100.0;
        
        // Calculate consistency based on content type usage and status code usage
        double contentTypeConsistency = getResponsesWithContentTypeRestrictions() * 100.0 / getTotalResponses();
        double statusCodeConsistency = getResponsesWithExplicitStatus() * 100.0 / getTotalResponses();
        
        return (contentTypeConsistency + statusCodeConsistency) / 2.0;
    }
    
    public boolean hasGoodResponsePatterns() {
        // Good patterns: proper use of ResponseEntity, explicit status codes, content type restrictions
        double wrappedPercentage = getWrappedResponses() * 100.0 / getTotalResponses();
        double explicitStatusPercentage = getResponsesWithExplicitStatus() * 100.0 / getTotalResponses();
        
        return wrappedPercentage > 50.0 || explicitStatusPercentage > 30.0;
    }
    
    // Helper methods
    private Map<String, List<ResponseInfo>> groupResponsesByClass(List<ResponseInfo> responses) {
        return responses.stream()
            .collect(Collectors.groupingBy(ResponseInfo::getDeclaringClass));
    }
    
    private Map<ResponseType, List<ResponseInfo>> groupResponsesByType(List<ResponseInfo> responses) {
        return responses.stream()
            .collect(Collectors.groupingBy(ResponseInfo::getResponseType));
    }
    
    @Override
    public String toString() {
        return "ResponseAnalysisResult{" +
                "totalResponses=" + getTotalResponses() +
                ", uniqueClasses=" + getUniqueClasses() +
                ", responseTypes=" + getUniqueResponseTypes() +
                ", withBody=" + getResponsesWithBody() +
                ", wrapped=" + getWrappedResponses() +
                ", async=" + getAsyncResponses() +
                ", analysisTime=" + analysisTimeMs + "ms" +
                '}';
    }
}