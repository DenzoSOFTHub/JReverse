package it.denzosoft.jreverse.core.port;

import it.denzosoft.jreverse.core.model.*;
import java.util.List;

/**
 * Port interface for UML diagram generation.
 * Generates UML diagrams in PlantUML syntax based on analyzed JAR content.
 */
public interface UMLGenerator {
    
    /**
     * Generates a class diagram showing classes, their relationships, and patterns.
     * 
     * @param request the UML generation request with configuration options
     * @return the generation result with PlantUML content
     */
    UMLGenerationResult generateClassDiagram(UMLGenerationRequest request);
    
    /**
     * Generates a package diagram showing package structure and organization.
     * 
     * @param packageAnalysis the package analysis results
     * @return the generation result with PlantUML content
     */
    UMLGenerationResult generatePackageDiagram(PackageAnalysisResult packageAnalysis);
    
    /**
     * Generates diagrams focused on specific design patterns.
     * 
     * @param patterns the detected design patterns
     * @param relationships the class relationship analysis
     * @return the generation result with PlantUML content
     */
    UMLGenerationResult generatePatternDiagram(List<DetectedDesignPattern> patterns, 
                                             ClassRelationshipResult relationships);
    
    /**
     * Gets the generator name for identification purposes.
     * 
     * @return the generator name
     */
    String getGeneratorName();
    
    /**
     * Checks if the generator can handle the given input data.
     * 
     * @param jarContent the JAR content to check
     * @return true if the generator can process this content
     */
    boolean canGenerate(JarContent jarContent);
}