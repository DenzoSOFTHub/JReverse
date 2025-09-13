package it.denzosoft.jreverse.analyzer.restcontroller;

import it.denzosoft.jreverse.core.model.JarContent;

/**
 * Analyzer for extracting and analyzing @RequestMapping and HTTP method annotations 
 * from REST controller methods. This analyzer identifies individual REST endpoints,
 * their HTTP methods, paths, and content type configurations.
 * 
 * This analyzer works in conjunction with RestControllerAnalyzer to provide complete
 * REST endpoint analysis by examining method-level mappings within identified controllers.
 * 
 * The analyzer detects and analyzes:
 * - @RequestMapping annotations on methods and classes
 * - HTTP method-specific annotations (@GetMapping, @PostMapping, @PutMapping, @DeleteMapping, @PatchMapping)
 * - Request paths and path variable patterns ({id}, {userId}, etc.)
 * - Content-Type handling (produces/consumes attributes)
 * - HTTP method combinations and overrides
 * - Path pattern matching and resolution
 * - Integration with controller base paths
 * 
 * Technical capabilities:
 * - Supports Spring Framework 2.0+ request mapping annotations
 * - Handles complex path patterns and wildcards
 * - Combines class-level and method-level path configurations
 * - Extracts path variable patterns and names
 * - Analyzes content type negotiation
 * - Provides endpoint hierarchy and organization
 */
public interface RequestMappingAnalyzer {
    
    /**
     * Analyzes the JAR content to extract REST endpoint mappings from controller methods.
     * This method examines all methods in REST controllers identified by RestControllerAnalyzer
     * and extracts their request mapping configurations.
     *
     * @param jarContent the JAR content to analyze
     * @param restControllerResult the result from RestControllerAnalyzer containing controller information
     * @return the analysis result containing information about found REST endpoints
     */
    RequestMappingAnalysisResult analyzeRequestMappings(JarContent jarContent, RestControllerAnalysisResult restControllerResult);
    
    /**
     * Checks if this analyzer can analyze the given JAR content.
     * The analyzer requires both valid JAR content and REST controller analysis results.
     *
     * @param jarContent the JAR content to check
     * @return true if the analyzer can process this content
     */
    default boolean canAnalyze(JarContent jarContent) {
        return jarContent != null && jarContent.getClassCount() > 0;
    }
    
    /**
     * Checks if this analyzer can analyze the given JAR content with REST controller context.
     *
     * @param jarContent the JAR content to check
     * @param restControllerResult the REST controller analysis result
     * @return true if the analyzer can process this content with the provided context
     */
    default boolean canAnalyze(JarContent jarContent, RestControllerAnalysisResult restControllerResult) {
        return canAnalyze(jarContent) && restControllerResult != null && !restControllerResult.getRestControllers().isEmpty();
    }
}