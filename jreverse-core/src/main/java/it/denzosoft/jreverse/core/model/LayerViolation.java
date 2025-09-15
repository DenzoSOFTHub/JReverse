package it.denzosoft.jreverse.core.model;

import java.util.*;

/**
 * Represents a violation of layered architecture principles.
 * This is an immutable value object following Clean Architecture principles.
 */
public final class LayerViolation {
    
    private final Type violationType;
    private final Severity severity;
    private final String description;
    private final LayerType sourceLayer;
    private final LayerType targetLayer;
    private final String sourceClass;
    private final String targetClass;
    private final String sourcePackage;
    private final String targetPackage;
    private final Set<String> affectedClasses;
    private final String violationDetail;
    private final String recommendedAction;
    private final double impactScore; // 0.0 - 1.0
    
    private LayerViolation(Builder builder) {
        this.violationType = Objects.requireNonNull(builder.violationType, "violationType cannot be null");
        this.severity = Objects.requireNonNull(builder.severity, "severity cannot be null");
        this.description = requireNonEmpty(builder.description, "description");
        this.sourceLayer = builder.sourceLayer;
        this.targetLayer = builder.targetLayer;
        this.sourceClass = builder.sourceClass;
        this.targetClass = builder.targetClass;
        this.sourcePackage = builder.sourcePackage;
        this.targetPackage = builder.targetPackage;
        this.affectedClasses = Collections.unmodifiableSet(new HashSet<>(builder.affectedClasses));
        this.violationDetail = builder.violationDetail;
        this.recommendedAction = builder.recommendedAction;
        this.impactScore = Math.min(1.0, Math.max(0.0, builder.impactScore));
    }
    
    public Type getViolationType() {
        return violationType;
    }
    
    public Severity getSeverity() {
        return severity;
    }
    
    public String getDescription() {
        return description;
    }
    
    public LayerType getSourceLayer() {
        return sourceLayer;
    }
    
    public LayerType getTargetLayer() {
        return targetLayer;
    }
    
    public String getSourceClass() {
        return sourceClass;
    }
    
    public String getTargetClass() {
        return targetClass;
    }
    
    public String getSourcePackage() {
        return sourcePackage;
    }
    
    public String getTargetPackage() {
        return targetPackage;
    }
    
    public Set<String> getAffectedClasses() {
        return affectedClasses;
    }
    
    public String getViolationDetail() {
        return violationDetail;
    }
    
    public String getRecommendedAction() {
        return recommendedAction;
    }
    
    public double getImpactScore() {
        return impactScore;
    }
    
    public boolean hasSourceClass() {
        return sourceClass != null && !sourceClass.isEmpty();
    }
    
    public boolean hasTargetClass() {
        return targetClass != null && !targetClass.isEmpty();
    }
    
    public boolean hasSourcePackage() {
        return sourcePackage != null && !sourcePackage.isEmpty();
    }
    
    public boolean hasTargetPackage() {
        return targetPackage != null && !targetPackage.isEmpty();
    }
    
    public boolean hasViolationDetail() {
        return violationDetail != null && !violationDetail.isEmpty();
    }
    
    public boolean hasRecommendedAction() {
        return recommendedAction != null && !recommendedAction.isEmpty();
    }
    
    public boolean isCriticalSeverity() {
        return severity == Severity.CRITICAL;
    }
    
    public boolean isHighSeverity() {
        return severity == Severity.HIGH;
    }
    
    public boolean isMediumSeverity() {
        return severity == Severity.MEDIUM;
    }
    
    public boolean isLowSeverity() {
        return severity == Severity.LOW;
    }
    
    public boolean isHighImpact() {
        return impactScore >= 0.7;
    }
    
    public boolean isMediumImpact() {
        return impactScore >= 0.4 && impactScore < 0.7;
    }
    
    public boolean isLowImpact() {
        return impactScore < 0.4;
    }
    
    public int getAffectedClassCount() {
        return affectedClasses.size();
    }
    
    public boolean involvesClass(String className) {
        return affectedClasses.contains(className);
    }
    
    public boolean isLayerDependencyViolation() {
        return violationType == Type.UPWARD_DEPENDENCY || 
               violationType == Type.SKIP_LAYER_DEPENDENCY ||
               violationType == Type.CIRCULAR_DEPENDENCY;
    }
    
    public boolean isLayerOrganizationViolation() {
        return violationType == Type.MISPLACED_CLASS || 
               violationType == Type.MIXED_CONCERNS ||
               violationType == Type.WRONG_LAYER_NAMING;
    }
    
    private String requireNonEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }
        return value.trim();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        LayerViolation that = (LayerViolation) obj;
        return Double.compare(that.impactScore, impactScore) == 0 &&
               violationType == that.violationType &&
               severity == that.severity &&
               Objects.equals(description, that.description) &&
               sourceLayer == that.sourceLayer &&
               targetLayer == that.targetLayer &&
               Objects.equals(sourceClass, that.sourceClass) &&
               Objects.equals(targetClass, that.targetClass) &&
               Objects.equals(sourcePackage, that.sourcePackage) &&
               Objects.equals(targetPackage, that.targetPackage) &&
               Objects.equals(affectedClasses, that.affectedClasses) &&
               Objects.equals(violationDetail, that.violationDetail) &&
               Objects.equals(recommendedAction, that.recommendedAction);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(violationType, severity, description, sourceLayer, targetLayer,
                sourceClass, targetClass, sourcePackage, targetPackage, affectedClasses,
                violationDetail, recommendedAction, impactScore);
    }
    
    @Override
    public String toString() {
        return "LayerViolation{" +
                "type=" + violationType +
                ", severity=" + severity +
                ", source=" + (sourceLayer != null ? sourceLayer : sourceClass) +
                ", target=" + (targetLayer != null ? targetLayer : targetClass) +
                ", impact=" + String.format("%.2f", impactScore) +
                '}';
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Types of layer violations that can be detected.
     */
    public enum Type {
        UPWARD_DEPENDENCY("Upward Dependency", "Lower layer depends on higher layer"),
        SKIP_LAYER_DEPENDENCY("Skip Layer Dependency", "Layer skips intermediate layers"),
        CIRCULAR_DEPENDENCY("Circular Dependency", "Circular dependency between layers"),
        MISPLACED_CLASS("Misplaced Class", "Class is in wrong architectural layer"),
        MIXED_CONCERNS("Mixed Concerns", "Layer contains mixed responsibilities"),
        WRONG_LAYER_NAMING("Wrong Layer Naming", "Layer naming doesn't match content"),
        EXCESSIVE_COUPLING("Excessive Coupling", "Layer has too many dependencies"),
        INSUFFICIENT_COHESION("Insufficient Cohesion", "Layer lacks cohesion"),
        DIRECT_DATABASE_ACCESS("Direct Database Access", "Non-data layer accesses database"),
        UI_BUSINESS_COUPLING("UI Business Coupling", "UI layer contains business logic"),
        PERSISTENCE_LEAKAGE("Persistence Leakage", "Persistence concerns leak to other layers"),
        CROSS_CUTTING_VIOLATION("Cross-cutting Violation", "Cross-cutting concern improperly handled");
        
        private final String displayName;
        private final String description;
        
        Type(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getDescription() {
            return description;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
    
    /**
     * Severity levels for layer violations.
     */
    public enum Severity {
        CRITICAL("Critical", 4, "Severe architectural violation requiring immediate attention"),
        HIGH("High", 3, "Significant architectural issue that should be addressed soon"),
        MEDIUM("Medium", 2, "Moderate architectural concern that should be reviewed"),
        LOW("Low", 1, "Minor architectural issue or suggestion for improvement");
        
        private final String displayName;
        private final int priority;
        private final String description;
        
        Severity(String displayName, int priority, String description) {
            this.displayName = displayName;
            this.priority = priority;
            this.description = description;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public int getPriority() {
            return priority;
        }
        
        public String getDescription() {
            return description;
        }
        
        public boolean isHigherThan(Severity other) {
            return this.priority > other.priority;
        }
        
        public boolean isLowerThan(Severity other) {
            return this.priority < other.priority;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
    
    public static class Builder {
        private Type violationType;
        private Severity severity = Severity.MEDIUM;
        private String description;
        private LayerType sourceLayer;
        private LayerType targetLayer;
        private String sourceClass;
        private String targetClass;
        private String sourcePackage;
        private String targetPackage;
        private Set<String> affectedClasses = new HashSet<>();
        private String violationDetail;
        private String recommendedAction;
        private double impactScore = 0.5;
        
        public Builder violationType(Type violationType) {
            this.violationType = violationType;
            if (violationType != null && description == null) {
                description = violationType.getDescription();
            }
            return this;
        }
        
        public Builder severity(Severity severity) {
            this.severity = severity;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder sourceLayer(LayerType sourceLayer) {
            this.sourceLayer = sourceLayer;
            return this;
        }
        
        public Builder targetLayer(LayerType targetLayer) {
            this.targetLayer = targetLayer;
            return this;
        }
        
        public Builder sourceClass(String sourceClass) {
            this.sourceClass = sourceClass;
            return this;
        }
        
        public Builder targetClass(String targetClass) {
            this.targetClass = targetClass;
            return this;
        }
        
        public Builder sourcePackage(String sourcePackage) {
            this.sourcePackage = sourcePackage;
            return this;
        }
        
        public Builder targetPackage(String targetPackage) {
            this.targetPackage = targetPackage;
            return this;
        }
        
        public Builder addAffectedClass(String className) {
            if (className != null && !className.trim().isEmpty()) {
                affectedClasses.add(className.trim());
            }
            return this;
        }
        
        public Builder affectedClasses(Set<String> classes) {
            this.affectedClasses = new HashSet<>(classes != null ? classes : Collections.emptySet());
            return this;
        }
        
        public Builder violationDetail(String violationDetail) {
            this.violationDetail = violationDetail;
            return this;
        }
        
        public Builder recommendedAction(String recommendedAction) {
            this.recommendedAction = recommendedAction;
            return this;
        }
        
        public Builder impactScore(double impactScore) {
            this.impactScore = impactScore;
            return this;
        }
        
        public LayerViolation build() {
            // Set default description if not provided
            if (description == null && violationType != null) {
                description = violationType.getDescription();
            }
            
            return new LayerViolation(this);
        }
    }
}