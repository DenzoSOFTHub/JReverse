package it.denzosoft.jreverse.core.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Metadata for generated UML diagrams containing generation statistics and configuration.
 */
public final class UMLMetadata {
    
    private final String diagramType;
    private final int classCount;
    private final int relationshipCount;
    private final int packageCount;
    private final int patternCount;
    private final LocalDateTime generatedAt;
    private final String generatorVersion;
    
    private UMLMetadata(Builder builder) {
        this.diagramType = builder.diagramType;
        this.classCount = builder.classCount;
        this.relationshipCount = builder.relationshipCount;
        this.packageCount = builder.packageCount;
        this.patternCount = builder.patternCount;
        this.generatedAt = LocalDateTime.now();
        this.generatorVersion = "1.1.0";
    }
    
    public static UMLMetadata forClassDiagram(int classCount, int relationshipCount) {
        return builder()
                .diagramType("Class Diagram")
                .classCount(classCount)
                .relationshipCount(relationshipCount)
                .build();
    }
    
    public static UMLMetadata forPackageDiagram(int packageCount) {
        return builder()
                .diagramType("Package Diagram")
                .packageCount(packageCount)
                .build();
    }
    
    public static UMLMetadata forPatternDiagram(int patternCount) {
        return builder()
                .diagramType("Pattern Diagram")
                .patternCount(patternCount)
                .build();
    }
    
    public static UMLMetadata forSinglePattern(DetectedDesignPattern pattern) {
        return builder()
                .diagramType("Pattern Diagram - " + pattern.getPatternType().getDisplayName())
                .patternCount(1)
                .build();
    }
    
    public String getDiagramType() {
        return diagramType;
    }
    
    public int getClassCount() {
        return classCount;
    }
    
    public int getRelationshipCount() {
        return relationshipCount;
    }
    
    public int getPackageCount() {
        return packageCount;
    }
    
    public int getPatternCount() {
        return patternCount;
    }
    
    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }
    
    public String getGeneratorVersion() {
        return generatorVersion;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String diagramType = "Unknown";
        private int classCount = 0;
        private int relationshipCount = 0;
        private int packageCount = 0;
        private int patternCount = 0;
        
        public Builder diagramType(String diagramType) {
            this.diagramType = diagramType;
            return this;
        }
        
        public Builder classCount(int classCount) {
            this.classCount = classCount;
            return this;
        }
        
        public Builder relationshipCount(int relationshipCount) {
            this.relationshipCount = relationshipCount;
            return this;
        }
        
        public Builder packageCount(int packageCount) {
            this.packageCount = packageCount;
            return this;
        }
        
        public Builder patternCount(int patternCount) {
            this.patternCount = patternCount;
            return this;
        }
        
        public UMLMetadata build() {
            return new UMLMetadata(this);
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        UMLMetadata that = (UMLMetadata) obj;
        return classCount == that.classCount &&
               relationshipCount == that.relationshipCount &&
               packageCount == that.packageCount &&
               patternCount == that.patternCount &&
               Objects.equals(diagramType, that.diagramType) &&
               Objects.equals(generatorVersion, that.generatorVersion);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(diagramType, classCount, relationshipCount, 
                          packageCount, patternCount, generatorVersion);
    }
    
    @Override
    public String toString() {
        return "UMLMetadata{" +
                "diagramType='" + diagramType + '\'' +
                ", classCount=" + classCount +
                ", relationshipCount=" + relationshipCount +
                ", packageCount=" + packageCount +
                ", patternCount=" + patternCount +
                ", generatedAt=" + generatedAt +
                ", generatorVersion='" + generatorVersion + '\'' +
                '}';
    }
}