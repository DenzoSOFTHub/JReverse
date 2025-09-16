package it.denzosoft.jreverse.analyzer.uml;

import it.denzosoft.jreverse.core.model.*;
import it.denzosoft.jreverse.core.port.UMLGenerator;
import it.denzosoft.jreverse.core.logging.JReverseLogger;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * PlantUML implementation of the UML generator.
 * Generates UML diagrams in PlantUML syntax.
 */
public class PlantUMLGenerator implements UMLGenerator {
    
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(PlantUMLGenerator.class);
    
    private final PlantUMLClassRenderer classRenderer;
    private final PlantUMLRelationshipRenderer relationshipRenderer;
    private final PlantUMLPackageRenderer packageRenderer;
    private final PlantUMLStyleManager styleManager;
    
    public PlantUMLGenerator() {
        this.classRenderer = new PlantUMLClassRenderer();
        this.relationshipRenderer = new PlantUMLRelationshipRenderer();
        this.packageRenderer = new PlantUMLPackageRenderer();
        this.styleManager = new PlantUMLStyleManager();
        
        LOGGER.info("PlantUMLGenerator initialized");
    }
    
    @Override
    public UMLGenerationResult generateClassDiagram(UMLGenerationRequest request) {
        Objects.requireNonNull(request, "UML generation request cannot be null");
        
        LOGGER.info("Starting class diagram generation: {}", request.getTitle());
        long startTime = System.currentTimeMillis();
        
        try {
            PlantUMLDocument.Builder documentBuilder = PlantUMLDocument.builder()
                .title(request.getTitle())
                .addHeader("@startuml");
            
            // Apply custom styles
            if (request.getStyleOptions() != null) {
                String styles = styleManager.generateStyleDirectives(request.getStyleOptions());
                documentBuilder.addSection(styles);
            }
            
            // Render classes
            String classesSection = classRenderer.renderClasses(
                request.getClassesToInclude(), 
                request.getDetailLevel()
            );
            documentBuilder.addSection(classesSection);
            
            // Render relationships if available
            if (request.getClassRelationships() != null && request.getClassRelationships().isSuccessful()) {
                String relationshipsSection = relationshipRenderer.renderRelationships(
                    request.getClassRelationships(),
                    request.getClassesToInclude()
                );
                documentBuilder.addSection(relationshipsSection);
            }
            
            // Include packages if requested
            if (request.isIncludePackages() && request.getPackageAnalysis() != null) {
                String packageSection = packageRenderer.renderPackageStructure(
                    request.getPackageAnalysis().getHierarchy(),
                    request.getClassesToInclude()
                );
                documentBuilder.addSection(packageSection);
            }
            
            // Highlight patterns if requested
            if (request.isHighlightPatterns() && !request.getDetectedPatterns().isEmpty()) {
                String patternHighlights = renderPatternHighlights(
                    request.getDetectedPatterns(),
                    request.getClassesToInclude()
                );
                documentBuilder.addSection(patternHighlights);
            }
            
            documentBuilder.addFooter("@enduml");
            PlantUMLDocument document = documentBuilder.build();
            
            long generationTime = System.currentTimeMillis() - startTime;
            UMLMetadata metadata = UMLMetadata.forClassDiagram(
                request.getClassesToInclude().size(),
                request.getClassRelationships() != null ? 
                    request.getClassRelationships().getDependencies().size() : 0
            );
            
            LOGGER.info("Class diagram generation completed in {}ms", generationTime);
            return UMLGenerationResult.success(document.getContent(), metadata, generationTime);
            
        } catch (Exception e) {
            LOGGER.error("UML generation failed", e);
            return UMLGenerationResult.failed("Generation failed: " + e.getMessage());
        }
    }
    
    @Override
    public UMLGenerationResult generatePackageDiagram(PackageAnalysisResult packageAnalysis) {
        Objects.requireNonNull(packageAnalysis, "Package analysis result cannot be null");
        
        if (!packageAnalysis.isSuccessful()) {
            return UMLGenerationResult.failed("Package analysis was not successful: " + 
                packageAnalysis.getMetadata().getMessage());
        }
        
        LOGGER.info("Generating package diagram for {} packages", 
            packageAnalysis.getHierarchy().getPackageCount());
        
        try {
            PlantUMLDocument.Builder documentBuilder = PlantUMLDocument.builder()
                .title("Package Structure Diagram")
                .addHeader("@startuml");
            
            // Generate package structure
            String packageStructure = packageRenderer.renderFullPackageHierarchy(
                packageAnalysis.getHierarchy()
            );
            documentBuilder.addSection(packageStructure);
            
            // Add anti-pattern highlights if any
            if (packageAnalysis.hasIssues()) {
                String antiPatternHighlights = renderAntiPatternHighlights(
                    packageAnalysis.getAntiPatterns()
                );
                documentBuilder.addSection(antiPatternHighlights);
            }
            
            documentBuilder.addFooter("@enduml");
            PlantUMLDocument document = documentBuilder.build();
            
            UMLMetadata metadata = UMLMetadata.forPackageDiagram(
                packageAnalysis.getHierarchy().getPackageCount()
            );
            
            return UMLGenerationResult.success(document.getContent(), metadata, 0);
            
        } catch (Exception e) {
            LOGGER.error("Package diagram generation failed", e);
            return UMLGenerationResult.failed("Package diagram generation failed: " + e.getMessage());
        }
    }
    
    @Override
    public UMLGenerationResult generatePatternDiagram(List<DetectedDesignPattern> patterns,
                                                     ClassRelationshipResult relationships) {
        Objects.requireNonNull(patterns, "Design patterns list cannot be null");
        
        LOGGER.info("Generating pattern diagram for {} detected patterns", patterns.size());
        
        if (patterns.isEmpty()) {
            return UMLGenerationResult.failed("No design patterns to visualize");
        }
        
        try {
            PlantUMLDocument.Builder documentBuilder = PlantUMLDocument.builder()
                .title("Design Patterns Diagram")
                .addHeader("@startuml");
            
            // Apply pattern-specific styling
            String patternStyles = styleManager.generatePatternStyles(patterns);
            documentBuilder.addSection(patternStyles);
            
            // Render each pattern
            for (DetectedDesignPattern pattern : patterns) {
                Set<ClassInfo> patternClasses = extractClassesFromPattern(pattern);
                String patternSection = renderSinglePattern(pattern, relationships, patternClasses);
                documentBuilder.addSection(patternSection);
            }
            
            documentBuilder.addFooter("@enduml");
            PlantUMLDocument document = documentBuilder.build();
            
            UMLMetadata metadata = UMLMetadata.forPatternDiagram(patterns.size());
            
            return UMLGenerationResult.success(document.getContent(), metadata, 0);
            
        } catch (Exception e) {
            LOGGER.error("Pattern diagram generation failed", e);
            return UMLGenerationResult.failed("Pattern diagram generation failed: " + e.getMessage());
        }
    }
    
    @Override
    public String getGeneratorName() {
        return "PlantUMLGenerator";
    }
    
    @Override
    public boolean canGenerate(JarContent jarContent) {
        return jarContent != null && !jarContent.getClasses().isEmpty();
    }
    
    private String renderPatternHighlights(List<DetectedDesignPattern> patterns, 
                                         java.util.Set<ClassInfo> includedClasses) {
        if (patterns.isEmpty()) {
            return "";
        }
        
        StringBuilder highlights = new StringBuilder();
        highlights.append("\n' Pattern Highlights\n");
        
        for (DetectedDesignPattern pattern : patterns) {
            String stereotype = pattern.getPatternType().getUMLStereotype();
            for (String className : pattern.getParticipatingClasses()) {
                ClassInfo classInfo = findClassInfoByName(className, includedClasses);
                if (classInfo != null) {
                    highlights.append("class ")
                             .append(sanitizeClassName(classInfo.getFullyQualifiedName()))
                             .append(" ")
                             .append(stereotype)
                             .append("\n");
                }
            }
        }
        
        return highlights.toString();
    }
    
    private String renderAntiPatternHighlights(List<ArchitecturalAntiPattern> antiPatterns) {
        if (antiPatterns == null || antiPatterns.isEmpty()) {
            return "";
        }
        
        StringBuilder highlights = new StringBuilder();
        highlights.append("\n' Anti-Pattern Highlights\n");
        
        for (ArchitecturalAntiPattern antiPattern : antiPatterns) {
            highlights.append("note as N").append(antiPattern.hashCode()).append("\n");
            highlights.append("  Anti-Pattern: ").append(antiPattern.getPatternType()).append("\n");
            highlights.append("  ").append(antiPattern.getDescription()).append("\n");
            highlights.append("end note\n");
        }
        
        return highlights.toString();
    }
    
    private String renderSinglePattern(DetectedDesignPattern pattern, 
                                     ClassRelationshipResult relationships,
                                     Set<ClassInfo> includedClasses) {
        StringBuilder patternSection = new StringBuilder();
        patternSection.append("\n' ").append(pattern.getPatternType().getDisplayName()).append(" Pattern\n");
        
        // Render classes participating in this pattern
        String stereotype = pattern.getPatternType().getUMLStereotype();
        for (String participantName : pattern.getParticipatingClasses()) {
            ClassInfo classInfo = findClassInfoByName(participantName, includedClasses);
            if (classInfo != null) {
                String className = sanitizeClassName(classInfo.getFullyQualifiedName());
                patternSection.append("class ").append(className);
                
                if (classInfo.isInterface()) {
                    patternSection.append(" <<interface>>");
                } else if (classInfo.isAbstract()) {
                    patternSection.append(" <<abstract>>");
                }
                
                patternSection.append(" ").append(stereotype).append("\n");
            }
        }
        
        // Add pattern-specific relationships if available
        if (relationships != null && relationships.isSuccessful()) {
            String patternRelationships = relationshipRenderer.renderPatternRelationships(
                pattern, relationships
            );
            patternSection.append(patternRelationships);
        }
        
        return patternSection.toString();
    }
    
    private String sanitizeClassName(String className) {
        return className.replace("$", "_").replace(" ", "_");
    }
    
    private ClassInfo findClassInfoByName(String className, Set<ClassInfo> classes) {
        if (className == null) {
            return null;
        }
        
        return classes.stream()
                     .filter(cls -> className.equals(cls.getFullyQualifiedName()))
                     .findFirst()
                     .orElse(null);
    }
    
    private Set<ClassInfo> extractClassesFromPattern(DetectedDesignPattern pattern) {
        Set<ClassInfo> classes = new HashSet<>();
        for (String className : pattern.getParticipatingClasses()) {
            ClassInfo classInfo = ClassInfo.builder()
                                          .fullyQualifiedName(className)
                                          .build();
            classes.add(classInfo);
        }
        return classes;
    }
}