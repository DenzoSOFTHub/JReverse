package it.denzosoft.jreverse.core.model;

import java.util.*;

/**
 * Result of class relationship analysis containing inheritance, composition, and detected patterns.
 * This is a placeholder implementation - will be fully implemented in Phase 3.
 */
public final class ClassRelationshipResult {
    
    private final boolean successful;
    private final String errorMessage;
    private final InheritanceHierarchy inheritanceHierarchy;
    private final CompositionGraph compositionGraph;
    private final List<DetectedDesignPattern> designPatterns;
    private final Map<String, List<String>> dependencies;
    private final CouplingMetrics couplingMetrics;
    private final AnalysisMetadata metadata;
    
    // Additional fields for test compatibility
    private final java.util.Set<ClassRelationship> relationships;
    private final java.util.Map<String, ClassHierarchy> hierarchies;
    private final RelationshipMetrics relationshipMetrics;
    
    private ClassRelationshipResult(Builder builder) {
        this.successful = builder.successful;
        this.errorMessage = builder.errorMessage;
        this.inheritanceHierarchy = builder.inheritanceHierarchy;
        this.compositionGraph = builder.compositionGraph;
        this.designPatterns = Collections.unmodifiableList(new ArrayList<>(builder.designPatterns));
        this.dependencies = Collections.unmodifiableMap(new HashMap<>(builder.dependencies));
        this.couplingMetrics = builder.couplingMetrics;
        this.metadata = builder.metadata;
        this.relationships = Collections.unmodifiableSet(new HashSet<>(builder.relationships));
        this.hierarchies = Collections.unmodifiableMap(new HashMap<>(builder.hierarchies));
        this.relationshipMetrics = builder.relationshipMetrics;
    }
    
    public static ClassRelationshipResult success(InheritanceHierarchy inheritance,
                                                 CompositionGraph composition,
                                                 CouplingMetrics metrics) {
        return builder()
                .successful(true)
                .inheritanceHierarchy(inheritance)
                .compositionGraph(composition)
                .couplingMetrics(metrics)
                .metadata(AnalysisMetadata.successful())
                .build();
    }
    
    public static ClassRelationshipResult failed(String errorMessage) {
        return builder()
                .successful(false)
                .errorMessage(errorMessage)
                .metadata(AnalysisMetadata.error(errorMessage))
                .build();
    }
    
    public boolean isSuccessful() {
        return successful;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public InheritanceHierarchy getInheritanceHierarchy() {
        return inheritanceHierarchy;
    }
    
    public CompositionGraph getCompositionGraph() {
        return compositionGraph;
    }
    
    public List<DetectedDesignPattern> getDesignPatterns() {
        return designPatterns;
    }
    
    public java.util.Set<DesignPattern> getDetectedPatterns() {
        // Convert DetectedDesignPattern to legacy DesignPattern for test compatibility
        return designPatterns.stream()
                .map(dp -> new DesignPattern(dp.getPatternType().name(), 
                                           dp.getParticipatingClasses(),
                                           dp.getConfidence(),
                                           dp.getDescription()))
                .collect(java.util.stream.Collectors.toSet());
    }
    
    public java.util.Set<DesignPattern> getLegacyPatterns() {
        // Convert DetectedDesignPattern to legacy DesignPattern for test compatibility
        return designPatterns.stream()
                .map(dp -> new DesignPattern(dp.getPatternType().name(), 
                                           dp.getParticipatingClasses(),
                                           dp.getConfidence(),
                                           dp.getDescription()))
                .collect(java.util.stream.Collectors.toSet());
    }
    
    private java.util.Set<String> convertClassInfoSetToStringSet(java.util.Set<ClassInfo> classInfos) {
        return classInfos.stream()
                .map(ClassInfo::getFullyQualifiedName)
                .collect(java.util.stream.Collectors.toSet());
    }
    
    public Map<String, List<String>> getDependencies() {
        return dependencies;
    }
    
    public CouplingMetrics getCouplingMetrics() {
        return couplingMetrics;
    }
    
    // Methods expected by existing tests
    public java.util.Set<ClassRelationship> getRelationships() {
        return relationships != null ? relationships : Collections.emptySet();
    }
    
    public java.util.Map<String, ClassHierarchy> getHierarchies() {
        return hierarchies != null ? hierarchies : Collections.emptyMap();
    }
    
    public RelationshipMetrics getMetrics() {
        return relationshipMetrics != null ? relationshipMetrics : createDefaultMetrics();
    }
    
    private RelationshipMetrics createDefaultMetrics() {
        int totalRels = relationships != null ? relationships.size() : 0;
        return new RelationshipMetrics(totalRels, 0, 0, 0, 0, 0, 0, 0.0, 0.0, 0.0, 0.0, 0, 0, 0, 0, 0, ArchitecturalQuality.UNKNOWN);
    }
    
    public int getTotalRelationships() {
        return relationships != null ? relationships.size() : 0;
    }
    
    public java.util.Set<ClassRelationship> getRelationshipsByType(ClassRelationship.RelationshipType relationType) {
        if (relationships == null) {
            return Collections.emptySet();
        }
        return relationships.stream()
                .filter(rel -> rel.getType() == relationType)
                .collect(java.util.stream.Collectors.toSet());
    }
    
    public java.util.Set<ClassRelationship> getRelationshipsForClass(String className) {
        if (relationships == null) {
            return Collections.emptySet();
        }
        return relationships.stream()
                .filter(rel -> rel.getSourceClass().equals(className) || rel.getTargetClass().equals(className))
                .collect(java.util.stream.Collectors.toSet());
    }
    
    public ClassHierarchy getHierarchyForClass(String className) {
        return hierarchies != null ? hierarchies.get(className) : null;
    }
    
    public AnalysisMetadata getMetadata() {
        return metadata;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private boolean successful = false;
        private String errorMessage;
        private InheritanceHierarchy inheritanceHierarchy;
        private CompositionGraph compositionGraph;
        private List<DetectedDesignPattern> designPatterns = new ArrayList<>();
        private Map<String, List<String>> dependencies = new HashMap<>();
        private CouplingMetrics couplingMetrics;
        private AnalysisMetadata metadata;
        private java.util.Set<ClassRelationship> relationships = new HashSet<>();
        private java.util.Map<String, ClassHierarchy> hierarchies = new HashMap<>();
        private RelationshipMetrics relationshipMetrics;
        
        public Builder successful(boolean successful) {
            this.successful = successful;
            return this;
        }
        
        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }
        
        public Builder inheritanceHierarchy(InheritanceHierarchy inheritanceHierarchy) {
            this.inheritanceHierarchy = inheritanceHierarchy;
            return this;
        }
        
        public Builder compositionGraph(CompositionGraph compositionGraph) {
            this.compositionGraph = compositionGraph;
            return this;
        }
        
        public Builder addDesignPattern(DetectedDesignPattern pattern) {
            this.designPatterns.add(pattern);
            return this;
        }
        
        public Builder designPatterns(List<DetectedDesignPattern> patterns) {
            this.designPatterns.addAll(patterns);
            return this;
        }
        
        public Builder dependencies(Map<String, List<String>> dependencies) {
            this.dependencies.putAll(dependencies);
            return this;
        }
        
        public Builder couplingMetrics(CouplingMetrics couplingMetrics) {
            this.couplingMetrics = couplingMetrics;
            return this;
        }
        
        public Builder metadata(AnalysisMetadata metadata) {
            this.metadata = metadata;
            return this;
        }
        
        public Builder relationships(java.util.Set<ClassRelationship> relationships) {
            this.relationships.addAll(relationships);
            return this;
        }
        
        public Builder addRelationship(ClassRelationship relationship) {
            this.relationships.add(relationship);
            return this;
        }
        
        public Builder hierarchies(java.util.Map<String, ClassHierarchy> hierarchies) {
            this.hierarchies.putAll(hierarchies);
            return this;
        }
        
        public Builder addHierarchy(String className, ClassHierarchy hierarchy) {
            this.hierarchies.put(className, hierarchy);
            return this;
        }
        
        public Builder relationshipMetrics(RelationshipMetrics relationshipMetrics) {
            this.relationshipMetrics = relationshipMetrics;
            return this;
        }
        
        public ClassRelationshipResult build() {
            return new ClassRelationshipResult(this);
        }
    }
    
    @Override
    public String toString() {
        if (successful) {
            return "ClassRelationshipResult{" +
                    "patternsFound=" + designPatterns.size() +
                    ", dependenciesCount=" + dependencies.size() +
                    ", successful=true}";
        } else {
            return "ClassRelationshipResult{successful=false, error='" + errorMessage + "'}";
        }
    }
}