package it.denzosoft.jreverse.analyzer.restcontroller;

import it.denzosoft.jreverse.core.model.ClassInfo;
import it.denzosoft.jreverse.core.model.MethodInfo;

import java.util.List;

/**
 * Interface for analyzing REST endpoint response patterns.
 * Analyzes method return types, @ResponseBody annotations, and response configuration.
 */
public interface ResponseAnalyzer {
    
    /**
     * Analyzes all response patterns in the given REST controller classes.
     * 
     * @param restControllers List of controller classes to analyze
     * @return Comprehensive response analysis results
     */
    ResponseAnalysisResult analyzeResponses(List<ClassInfo> restControllers);
    
    /**
     * Analyzes response patterns for a specific REST controller class.
     * 
     * @param controllerClass The controller class to analyze
     * @return List of response information for the class methods
     */
    List<ResponseInfo> analyzeClassResponses(ClassInfo controllerClass);
    
    /**
     * Analyzes the response pattern for a specific method.
     * 
     * @param method The method to analyze
     * @param controllerClass The controller class containing the method
     * @return Response information for the method
     */
    ResponseInfo analyzeMethodResponse(MethodInfo method, ClassInfo controllerClass);
    
    /**
     * Determines the response type category for a given return type.
     * 
     * @param returnType The fully qualified return type string
     * @return The categorized response type
     */
    ResponseType categorizeReturnType(String returnType);
    
    /**
     * Checks if a method has @ResponseBody annotation or is in a @RestController.
     * 
     * @param method The method to check
     * @param controllerClass The controller class containing the method
     * @return True if method produces response body
     */
    boolean hasResponseBody(MethodInfo method, ClassInfo controllerClass);
}