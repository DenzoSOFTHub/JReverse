package it.denzosoft.jreverse.analyzer.uml;

import it.denzosoft.jreverse.core.model.*;
import it.denzosoft.jreverse.core.logging.JReverseLogger;

import java.util.Set;

/**
 * Renders class relationships in PlantUML syntax.
 */
public class PlantUMLRelationshipRenderer {
    
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(PlantUMLRelationshipRenderer.class);
    
    public String renderRelationships(ClassRelationshipResult relationships, 
                                    Set<ClassInfo> includedClasses) {
        if (relationships == null || !relationships.isSuccessful()) {
            LOGGER.debug("No relationship data available");
            return "";
        }
        
        StringBuilder relationshipBuilder = new StringBuilder();
        relationshipBuilder.append("\n' Relationships\n");
        
        // Render inheritance relationships
        String inheritanceSection = renderInheritanceRelationships(
            relationships.getInheritanceHierarchy(), includedClasses
        );
        relationshipBuilder.append(inheritanceSection);
        
        // Render composition relationships
        String compositionSection = renderCompositionRelationships(
            relationships.getCompositionGraph(), includedClasses
        );
        relationshipBuilder.append(compositionSection);
        
        return relationshipBuilder.toString();
    }
    
    public String renderInheritanceRelationships(InheritanceHierarchy hierarchy, 
                                               Set<ClassInfo> includedClasses) {
        if (hierarchy == null) {
            return "";
        }
        
        StringBuilder inheritanceBuilder = new StringBuilder();
        inheritanceBuilder.append("\n' Inheritance\n");
        
        for (InheritanceRelation relation : hierarchy.getAllRelations()) {
            if (isIncluded(relation.getParent(), includedClasses) && 
                isIncluded(relation.getChild(), includedClasses)) {
                
                String plantUMLRelation = convertToPlantUMLInheritance(relation);
                inheritanceBuilder.append(plantUMLRelation).append("\n");
            }
        }
        
        return inheritanceBuilder.toString();
    }
    
    public String renderCompositionRelationships(CompositionGraph compositionGraph, 
                                               Set<ClassInfo> includedClasses) {
        if (compositionGraph == null) {
            return "";
        }
        
        StringBuilder compositionBuilder = new StringBuilder();
        compositionBuilder.append("\n' Composition and Aggregation\n");
        
        for (CompositionRelationship relation : compositionGraph.getAllRelationships()) {
            if (isIncluded(relation.getSource(), includedClasses) &&
                isIncluded(relation.getTarget(), includedClasses)) {
                
                String plantUMLRelation = convertToPlantUMLComposition(relation);
                compositionBuilder.append(plantUMLRelation).append("\n");
            }
        }
        
        return compositionBuilder.toString();
    }
    
    public String renderPatternRelationships(DetectedDesignPattern pattern, 
                                           ClassRelationshipResult relationships) {
        if (pattern == null || relationships == null || !relationships.isSuccessful()) {
            return "";
        }
        
        StringBuilder patternRelationships = new StringBuilder();
        patternRelationships.append("\n' Pattern-specific relationships\n");
        
        // Generate pattern-specific relationship styles based on pattern type
        switch (pattern.getPatternType()) {
            case STRATEGY:
                patternRelationships.append(renderStrategyPatternRelationships(pattern, relationships));
                break;
            case OBSERVER:
                patternRelationships.append(renderObserverPatternRelationships(pattern, relationships));
                break;
            case FACTORY_METHOD:
                patternRelationships.append(renderFactoryPatternRelationships(pattern, relationships));
                break;
            case DECORATOR:
                patternRelationships.append(renderDecoratorPatternRelationships(pattern, relationships));
                break;
            default:
                // Generic pattern relationships
                patternRelationships.append("' Generic pattern relationships for ")
                                   .append(pattern.getPatternType().getDisplayName())
                                   .append("\n");
                break;
        }
        
        return patternRelationships.toString();
    }
    
    private String convertToPlantUMLInheritance(InheritanceRelation relation) {
        String parent = sanitizeClassName(relation.getParent());
        String child = sanitizeClassName(relation.getChild());
        
        switch (relation.getType()) {
            case EXTENDS:
                return parent + " <|-- " + child;
            case IMPLEMENTS:
                return parent + " <|.. " + child;
            case INTERFACE_EXTENDS:
                return parent + " <|-- " + child;
            default:
                return parent + " -- " + child;
        }
    }
    
    private String convertToPlantUMLComposition(CompositionRelationship relation) {
        String source = sanitizeClassName(relation.getSource());
        String target = sanitizeClassName(relation.getTarget());
        String multiplicity = convertMultiplicity(relation.getMultiplicity());
        
        String relationSymbol;
        switch (relation.getCompositionType()) {
            case COMPOSITION:
                relationSymbol = " *-- ";
                break;
            case AGGREGATION:
                relationSymbol = " o-- ";
                break;
            case ASSOCIATION:
                relationSymbol = " --> ";
                break;
            default:
                relationSymbol = " -- ";
                break;
        }
        
        return source + relationSymbol + "\"" + multiplicity + "\" " + target;
    }
    
    private String renderStrategyPatternRelationships(DetectedDesignPattern pattern, 
                                                    ClassRelationshipResult relationships) {
        StringBuilder strategy = new StringBuilder();
        strategy.append("' Strategy Pattern relationships\n");
        
        // In Strategy pattern, context uses strategy interface, and concrete strategies implement it
        Set<String> participantNames = pattern.getParticipatingClasses();
        for (String participantName : participantNames) {
            ClassInfo participant = findClassInfoByName(participantName, relationships);
            if (participant != null && participant.getSimpleName().toLowerCase().contains("context")) {
                strategy.append("' Context uses Strategy\n");
            } else if (participant != null && participant.isInterface() && 
                      participant.getSimpleName().toLowerCase().contains("strategy")) {
                strategy.append("' Strategy interface\n");
            }
        }
        
        return strategy.toString();
    }
    
    private String renderObserverPatternRelationships(DetectedDesignPattern pattern,
                                                    ClassRelationshipResult relationships) {
        StringBuilder observer = new StringBuilder();
        observer.append("' Observer Pattern relationships\n");
        observer.append("' Subject notifies observers\n");
        return observer.toString();
    }
    
    private String renderFactoryPatternRelationships(DetectedDesignPattern pattern,
                                                   ClassRelationshipResult relationships) {
        StringBuilder factory = new StringBuilder();
        factory.append("' Factory Pattern relationships\n");
        factory.append("' Factory creates products\n");
        return factory.toString();
    }
    
    private String renderDecoratorPatternRelationships(DetectedDesignPattern pattern,
                                                     ClassRelationshipResult relationships) {
        StringBuilder decorator = new StringBuilder();
        decorator.append("' Decorator Pattern relationships\n");
        decorator.append("' Decorators wrap components\n");
        return decorator.toString();
    }
    
    private boolean isIncluded(String className, Set<ClassInfo> includedClasses) {
        if (className == null || includedClasses == null) {
            return false;
        }
        
        return includedClasses.stream()
                .anyMatch(classInfo -> className.equals(classInfo.getFullyQualifiedName()));
    }
    
    private String convertMultiplicity(String multiplicity) {
        if (multiplicity == null || multiplicity.trim().isEmpty()) {
            return "1";
        }
        return multiplicity;
    }
    
    private String sanitizeClassName(String className) {
        if (className == null) return "";
        return className.replace("$", "_").replace(" ", "_");
    }
    
    private ClassInfo findClassInfoByName(String className, ClassRelationshipResult relationships) {
        if (className == null || relationships == null) {
            return null;
        }
        
        // Since we don't have access to all classes from relationships,
        // we'll create a minimal ClassInfo for basic operations
        return ClassInfo.builder()
                       .fullyQualifiedName(className)
                       .build();
    }
}