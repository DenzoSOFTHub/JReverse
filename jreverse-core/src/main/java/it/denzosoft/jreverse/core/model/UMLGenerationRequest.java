package it.denzosoft.jreverse.core.model;

import java.util.*;

/**
 * Request object for UML diagram generation containing all configuration options.
 */
public final class UMLGenerationRequest {
    
    private final String title;
    private final Set<ClassInfo> classesToInclude;
    private final DetailLevel detailLevel;
    private final boolean includePackages;
    private final boolean highlightPatterns;
    private final PackageAnalysisResult packageAnalysis;
    private final ClassRelationshipResult classRelationships;
    private final List<DetectedDesignPattern> detectedPatterns;
    private final UMLStyleOptions styleOptions;
    
    private UMLGenerationRequest(Builder builder) {
        this.title = builder.title != null ? builder.title : "Class Diagram";
        this.classesToInclude = Collections.unmodifiableSet(new HashSet<>(builder.classesToInclude));
        this.detailLevel = builder.detailLevel != null ? builder.detailLevel : DetailLevel.SUMMARY;
        this.includePackages = builder.includePackages;
        this.highlightPatterns = builder.highlightPatterns;
        this.packageAnalysis = builder.packageAnalysis;
        this.classRelationships = builder.classRelationships;
        this.detectedPatterns = Collections.unmodifiableList(new ArrayList<>(builder.detectedPatterns));
        this.styleOptions = builder.styleOptions;
    }
    
    public String getTitle() {
        return title;
    }
    
    public Set<ClassInfo> getClassesToInclude() {
        return classesToInclude;
    }
    
    public DetailLevel getDetailLevel() {
        return detailLevel;
    }
    
    public boolean isIncludePackages() {
        return includePackages;
    }
    
    public boolean isHighlightPatterns() {
        return highlightPatterns;
    }
    
    public PackageAnalysisResult getPackageAnalysis() {
        return packageAnalysis;
    }
    
    public ClassRelationshipResult getClassRelationships() {
        return classRelationships;
    }
    
    public List<DetectedDesignPattern> getDetectedPatterns() {
        return detectedPatterns;
    }
    
    public UMLStyleOptions getStyleOptions() {
        return styleOptions;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String title = "Class Diagram";
        private Set<ClassInfo> classesToInclude = new HashSet<>();
        private DetailLevel detailLevel = DetailLevel.SUMMARY;
        private boolean includePackages = false;
        private boolean highlightPatterns = false;
        private PackageAnalysisResult packageAnalysis;
        private ClassRelationshipResult classRelationships;
        private List<DetectedDesignPattern> detectedPatterns = new ArrayList<>();
        private UMLStyleOptions styleOptions;
        
        public Builder title(String title) {
            this.title = title;
            return this;
        }
        
        public Builder includeClass(ClassInfo classInfo) {
            this.classesToInclude.add(classInfo);
            return this;
        }
        
        public Builder includeClasses(Set<ClassInfo> classes) {
            this.classesToInclude.addAll(classes);
            return this;
        }
        
        public Builder detailLevel(DetailLevel detailLevel) {
            this.detailLevel = detailLevel;
            return this;
        }
        
        public Builder includePackages(boolean includePackages) {
            this.includePackages = includePackages;
            return this;
        }
        
        public Builder includePackages(PackageAnalysisResult packageAnalysis) {
            this.includePackages = true;
            this.packageAnalysis = packageAnalysis;
            return this;
        }
        
        public Builder highlightPatterns(boolean highlightPatterns) {
            this.highlightPatterns = highlightPatterns;
            return this;
        }
        
        public Builder highlightPatterns(List<DetectedDesignPattern> patterns) {
            this.highlightPatterns = true;
            this.detectedPatterns = new ArrayList<>(patterns);
            return this;
        }
        
        public Builder packageAnalysis(PackageAnalysisResult packageAnalysis) {
            this.packageAnalysis = packageAnalysis;
            return this;
        }
        
        public Builder relationshipData(ClassRelationshipResult relationships) {
            this.classRelationships = relationships;
            return this;
        }
        
        public Builder styleOptions(UMLStyleOptions options) {
            this.styleOptions = options;
            return this;
        }
        
        public UMLGenerationRequest build() {
            return new UMLGenerationRequest(this);
        }
    }
    
    @Override
    public String toString() {
        return "UMLGenerationRequest{" +
                "title='" + title + '\'' +
                ", classCount=" + classesToInclude.size() +
                ", detailLevel=" + detailLevel +
                ", includePackages=" + includePackages +
                ", highlightPatterns=" + highlightPatterns +
                ", patternsCount=" + detectedPatterns.size() +
                '}';
    }
}