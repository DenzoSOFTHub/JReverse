package it.denzosoft.jreverse.analyzer.restcontroller;

import it.denzosoft.jreverse.core.model.JarContent;

import java.util.List;

/**
 * Interface for analyzing REST endpoint parameters and their binding configurations.
 * Provides comprehensive analysis of Spring MVC parameter annotations and validation.
 * 
 * This analyzer focuses specifically on method parameter analysis for REST endpoints,
 * extracting information about parameter binding (@RequestParam, @PathVariable, @RequestBody, etc.),
 * validation constraints, and parameter consistency checking.
 */
public interface ParameterAnalyzer {
    
    /**
     * Checks if this analyzer can process the given JAR content.
     * 
     * @param jarContent the JAR content to check
     * @return true if the analyzer can process this content
     */
    boolean canAnalyze(JarContent jarContent);
    
    /**
     * Analyzes parameters from a list of REST endpoints.
     * Extracts comprehensive parameter information including binding types,
     * validation annotations, and consistency validation.
     * 
     * @param endpoints the REST endpoints to analyze parameters for
     * @return comprehensive parameter analysis result
     */
    ParameterAnalysisResult analyzeParameters(List<RestEndpointInfo> endpoints);
    
    /**
     * Analyzes parameters for a single REST endpoint.
     * Provides detailed parameter analysis for one specific endpoint method.
     * 
     * @param endpoint the REST endpoint to analyze
     * @return list of parameter information for the endpoint
     */
    List<EndpointParameterInfo> analyzeEndpointParameters(RestEndpointInfo endpoint);
    
    /**
     * Validates parameter consistency within a REST endpoint.
     * Checks for common issues like path variable mismatches,
     * multiple request body parameters, and annotation conflicts.
     * 
     * @param endpoint the REST endpoint to validate
     * @return list of validation error messages, empty if no issues
     */
    List<String> validateParameterConsistency(RestEndpointInfo endpoint);
    
    /**
     * Validates parameter consistency across multiple endpoints.
     * Checks for cross-endpoint issues like parameter naming inconsistencies
     * and conflicting parameter patterns.
     * 
     * @param endpoints the REST endpoints to validate
     * @return list of validation error messages, empty if no issues
     */
    List<String> validateCrossEndpointConsistency(List<RestEndpointInfo> endpoints);
    
    /**
     * Analyzes parameter usage patterns across all endpoints.
     * Identifies common parameter patterns, naming conventions,
     * and potential improvements in parameter design.
     * 
     * @param endpoints the REST endpoints to analyze
     * @return analysis of parameter usage patterns and recommendations
     */
    ParameterPatternAnalysis analyzeParameterPatterns(List<RestEndpointInfo> endpoints);
    
    /**
     * Container for parameter pattern analysis results.
     */
    class ParameterPatternAnalysis {
        private final List<String> commonParameterNames;
        private final List<String> commonParameterTypes;
        private final List<String> namingPatterns;
        private final List<String> recommendations;
        private final List<String> potentialIssues;
        
        public ParameterPatternAnalysis(List<String> commonParameterNames,
                                      List<String> commonParameterTypes,
                                      List<String> namingPatterns,
                                      List<String> recommendations,
                                      List<String> potentialIssues) {
            this.commonParameterNames = commonParameterNames != null ? commonParameterNames : List.of();
            this.commonParameterTypes = commonParameterTypes != null ? commonParameterTypes : List.of();
            this.namingPatterns = namingPatterns != null ? namingPatterns : List.of();
            this.recommendations = recommendations != null ? recommendations : List.of();
            this.potentialIssues = potentialIssues != null ? potentialIssues : List.of();
        }
        
        public List<String> getCommonParameterNames() { return commonParameterNames; }
        public List<String> getCommonParameterTypes() { return commonParameterTypes; }
        public List<String> getNamingPatterns() { return namingPatterns; }
        public List<String> getRecommendations() { return recommendations; }
        public List<String> getPotentialIssues() { return potentialIssues; }
        
        public boolean hasRecommendations() { return !recommendations.isEmpty(); }
        public boolean hasIssues() { return !potentialIssues.isEmpty(); }
        
        @Override
        public String toString() {
            return "ParameterPatternAnalysis{" +
                    "commonNames=" + commonParameterNames.size() +
                    ", commonTypes=" + commonParameterTypes.size() +
                    ", patterns=" + namingPatterns.size() +
                    ", recommendations=" + recommendations.size() +
                    ", issues=" + potentialIssues.size() +
                    '}';
        }
    }
}