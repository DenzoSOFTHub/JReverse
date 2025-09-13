package it.denzosoft.jreverse.analyzer.restcontroller;

import it.denzosoft.jreverse.core.model.JarContent;

/**
 * Analyzer for identifying and analyzing REST controllers in JAR files,
 * with focus on Spring MVC @RestController and @Controller with @ResponseBody patterns.
 * 
 * This analyzer detects:
 * - @RestController annotated classes
 * - @Controller classes with @ResponseBody (hybrid REST controllers)
 * - Class-level @RequestMapping for base paths
 * - Controller inheritance hierarchies
 * - Package organization of REST endpoints
 * - Cross-origin configuration (@CrossOrigin)
 * - Security annotations on controller level
 * - Performance and caching annotations
 */
public interface RestControllerAnalyzer {
    
    /**
     * Analyzes the JAR content to find and analyze REST controllers.
     *
     * @param jarContent the JAR content to analyze
     * @return the analysis result containing information about found REST controllers
     */
    RestControllerAnalysisResult analyzeRestControllers(JarContent jarContent);
    
    /**
     * Checks if this analyzer can analyze the given JAR content.
     *
     * @param jarContent the JAR content to check
     * @return true if the analyzer can process this content
     */
    default boolean canAnalyze(JarContent jarContent) {
        return jarContent != null && jarContent.getClassCount() > 0;
    }
}