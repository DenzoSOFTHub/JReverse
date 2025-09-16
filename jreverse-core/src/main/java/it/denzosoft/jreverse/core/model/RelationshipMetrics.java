package it.denzosoft.jreverse.core.model;

/**
 * Metrics about class relationships.
 * Comprehensive metrics for analyzing class relationships and architectural quality.
 */
public final class RelationshipMetrics {
    
    private final int totalRelationships;
    private final int inheritanceRelationships;
    private final int implementationRelationships;
    private final int compositionRelationships;
    private final int aggregationRelationships;
    private final int associationRelationships;
    private final int dependencyRelationships;
    private final double averageRelationshipsPerClass;
    private final double inheritanceDepth;
    private final double couplingIndex;
    private final double cohesionIndex;
    private final int totalHierarchies;
    private final int deepestHierarchy;
    private final int widestHierarchy;
    private final int abstractClasses;
    private final int interfaces;
    private final ArchitecturalQuality architecturalQuality;
    
    public RelationshipMetrics(int totalRelationships, int inheritanceRelationships, 
                              int implementationRelationships, int compositionRelationships, 
                              int aggregationRelationships, int associationRelationships,
                              int dependencyRelationships, double averageRelationshipsPerClass, 
                              double inheritanceDepth, double couplingIndex, double cohesionIndex,
                              int totalHierarchies, int deepestHierarchy, int widestHierarchy,
                              int abstractClasses, int interfaces, ArchitecturalQuality architecturalQuality) {
        this.totalRelationships = totalRelationships;
        this.inheritanceRelationships = inheritanceRelationships;
        this.implementationRelationships = implementationRelationships;
        this.compositionRelationships = compositionRelationships;
        this.aggregationRelationships = aggregationRelationships;
        this.associationRelationships = associationRelationships;
        this.dependencyRelationships = dependencyRelationships;
        this.averageRelationshipsPerClass = averageRelationshipsPerClass;
        this.inheritanceDepth = inheritanceDepth;
        this.couplingIndex = couplingIndex;
        this.cohesionIndex = cohesionIndex;
        this.totalHierarchies = totalHierarchies;
        this.deepestHierarchy = deepestHierarchy;
        this.widestHierarchy = widestHierarchy;
        this.abstractClasses = abstractClasses;
        this.interfaces = interfaces;
        this.architecturalQuality = architecturalQuality;
    }
    
    // Constructor for backward compatibility
    public RelationshipMetrics(int totalRelationships, int inheritanceRelationships, 
                              int compositionRelationships, int associationRelationships,
                              double averageRelationshipsPerClass, double couplingFactor) {
        this(totalRelationships, inheritanceRelationships, 0, compositionRelationships,
             0, associationRelationships, 0, averageRelationshipsPerClass, 0.0, 
             couplingFactor, 0.0, 0, 0, 0, 0, 0, ArchitecturalQuality.UNKNOWN);
    }
    
    public int getTotalRelationships() {
        return totalRelationships;
    }
    
    public int getInheritanceRelationships() {
        return inheritanceRelationships;
    }
    
    public int getImplementationRelationships() {
        return implementationRelationships;
    }
    
    public int getCompositionRelationships() {
        return compositionRelationships;
    }
    
    public int getAggregationRelationships() {
        return aggregationRelationships;
    }
    
    public int getAssociationRelationships() {
        return associationRelationships;
    }
    
    public int getDependencyRelationships() {
        return dependencyRelationships;
    }
    
    public double getAverageRelationshipsPerClass() {
        return averageRelationshipsPerClass;
    }
    
    public double getInheritanceDepth() {
        return inheritanceDepth;
    }
    
    public double getCouplingIndex() {
        return couplingIndex;
    }
    
    public double getCohesionIndex() {
        return cohesionIndex;
    }
    
    public int getTotalHierarchies() {
        return totalHierarchies;
    }
    
    public int getDeepestHierarchy() {
        return deepestHierarchy;
    }
    
    public int getWidestHierarchy() {
        return widestHierarchy;
    }
    
    public int getAbstractClasses() {
        return abstractClasses;
    }
    
    public int getInterfaces() {
        return interfaces;
    }
    
    public ArchitecturalQuality getArchitecturalQuality() {
        return architecturalQuality;
    }
    
    // Backward compatibility
    public double getCouplingFactor() {
        return couplingIndex;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private int totalRelationships = 0;
        private int inheritanceRelationships = 0;
        private int implementationRelationships = 0;
        private int compositionRelationships = 0;
        private int aggregationRelationships = 0;
        private int associationRelationships = 0;
        private int dependencyRelationships = 0;
        private double averageRelationshipsPerClass = 0.0;
        private double inheritanceDepth = 0.0;
        private double couplingIndex = 0.0;
        private double cohesionIndex = 0.0;
        private int totalHierarchies = 0;
        private int deepestHierarchy = 0;
        private int widestHierarchy = 0;
        private int abstractClasses = 0;
        private int interfaces = 0;
        private ArchitecturalQuality architecturalQuality = ArchitecturalQuality.UNKNOWN;
        
        public Builder totalRelationships(int totalRelationships) {
            this.totalRelationships = totalRelationships;
            return this;
        }
        
        public Builder inheritanceRelationships(int inheritanceRelationships) {
            this.inheritanceRelationships = inheritanceRelationships;
            return this;
        }
        
        public Builder implementationRelationships(int implementationRelationships) {
            this.implementationRelationships = implementationRelationships;
            return this;
        }
        
        public Builder compositionRelationships(int compositionRelationships) {
            this.compositionRelationships = compositionRelationships;
            return this;
        }
        
        public Builder aggregationRelationships(int aggregationRelationships) {
            this.aggregationRelationships = aggregationRelationships;
            return this;
        }
        
        public Builder associationRelationships(int associationRelationships) {
            this.associationRelationships = associationRelationships;
            return this;
        }
        
        public Builder dependencyRelationships(int dependencyRelationships) {
            this.dependencyRelationships = dependencyRelationships;
            return this;
        }
        
        public Builder averageRelationshipsPerClass(double averageRelationshipsPerClass) {
            this.averageRelationshipsPerClass = averageRelationshipsPerClass;
            return this;
        }
        
        public Builder inheritanceDepth(double inheritanceDepth) {
            this.inheritanceDepth = inheritanceDepth;
            return this;
        }
        
        public Builder couplingIndex(double couplingIndex) {
            this.couplingIndex = couplingIndex;
            return this;
        }
        
        public Builder cohesionIndex(double cohesionIndex) {
            this.cohesionIndex = cohesionIndex;
            return this;
        }
        
        public Builder totalHierarchies(int totalHierarchies) {
            this.totalHierarchies = totalHierarchies;
            return this;
        }
        
        public Builder deepestHierarchy(int deepestHierarchy) {
            this.deepestHierarchy = deepestHierarchy;
            return this;
        }
        
        public Builder widestHierarchy(int widestHierarchy) {
            this.widestHierarchy = widestHierarchy;
            return this;
        }
        
        public Builder abstractClasses(int abstractClasses) {
            this.abstractClasses = abstractClasses;
            return this;
        }
        
        public Builder interfaces(int interfaces) {
            this.interfaces = interfaces;
            return this;
        }
        
        public Builder architecturalQuality(ArchitecturalQuality architecturalQuality) {
            this.architecturalQuality = architecturalQuality;
            return this;
        }
        
        public RelationshipMetrics build() {
            return new RelationshipMetrics(totalRelationships, inheritanceRelationships, 
                                         implementationRelationships, compositionRelationships,
                                         aggregationRelationships, associationRelationships,
                                         dependencyRelationships, averageRelationshipsPerClass,
                                         inheritanceDepth, couplingIndex, cohesionIndex,
                                         totalHierarchies, deepestHierarchy, widestHierarchy,
                                         abstractClasses, interfaces, architecturalQuality);
        }
    }
    
    @Override
    public String toString() {
        return "RelationshipMetrics{" +
                "total=" + totalRelationships +
                ", inheritance=" + inheritanceRelationships +
                ", composition=" + compositionRelationships +
                ", association=" + associationRelationships +
                ", avgPerClass=" + String.format("%.2f", averageRelationshipsPerClass) +
                ", coupling=" + String.format("%.2f", couplingIndex) +
                '}';
    }
}