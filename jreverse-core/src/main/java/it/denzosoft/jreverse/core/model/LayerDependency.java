package it.denzosoft.jreverse.core.model;

import java.util.*;

/**
 * Represents a dependency relationship between architectural layers.
 * This is an immutable value object following Clean Architecture principles.
 */
public final class LayerDependency {
    
    private final LayerType sourceLayer;
    private final LayerType targetLayer;
    private final String sourcePackage;
    private final String targetPackage;
    private final Set<String> dependentClasses;
    private final DependencyType dependencyType;
    private final int dependencyCount;
    private final double dependencyStrength; // 0.0 - 1.0
    private final boolean isViolation;
    private final String violationReason;
    
    private LayerDependency(Builder builder) {
        this.sourceLayer = Objects.requireNonNull(builder.sourceLayer, "sourceLayer cannot be null");
        this.targetLayer = Objects.requireNonNull(builder.targetLayer, "targetLayer cannot be null");
        this.sourcePackage = requireNonEmpty(builder.sourcePackage, "sourcePackage");
        this.targetPackage = requireNonEmpty(builder.targetPackage, "targetPackage");
        this.dependentClasses = Collections.unmodifiableSet(new HashSet<>(builder.dependentClasses));
        this.dependencyType = Objects.requireNonNull(builder.dependencyType, "dependencyType cannot be null");
        this.dependencyCount = Math.max(0, builder.dependencyCount);
        this.dependencyStrength = Math.min(1.0, Math.max(0.0, builder.dependencyStrength));
        this.isViolation = builder.isViolation;
        this.violationReason = builder.violationReason;
    }
    
    public LayerType getSourceLayer() {
        return sourceLayer;
    }
    
    public LayerType getTargetLayer() {
        return targetLayer;
    }
    
    public String getSourcePackage() {
        return sourcePackage;
    }
    
    public String getTargetPackage() {
        return targetPackage;
    }
    
    public Set<String> getDependentClasses() {
        return dependentClasses;
    }
    
    public DependencyType getDependencyType() {
        return dependencyType;
    }
    
    public int getDependencyCount() {
        return dependencyCount;
    }
    
    public double getDependencyStrength() {
        return dependencyStrength;
    }
    
    public boolean isViolation() {
        return isViolation;
    }
    
    public String getViolationReason() {
        return violationReason;
    }
    
    public boolean hasViolationReason() {
        return violationReason != null && !violationReason.isEmpty();
    }
    
    public boolean isUpwardDependency() {
        return sourceLayer.isBelow(targetLayer);
    }
    
    public boolean isDownwardDependency() {
        return sourceLayer.isAbove(targetLayer);
    }
    
    public boolean isSameLevelDependency() {
        return sourceLayer.isSameLevel(targetLayer);
    }
    
    public boolean isWeakDependency() {
        return dependencyStrength < 0.3;
    }
    
    public boolean isModerateDependency() {
        return dependencyStrength >= 0.3 && dependencyStrength < 0.7;
    }
    
    public boolean isStrongDependency() {
        return dependencyStrength >= 0.7;
    }
    
    public int getParticipatingClassCount() {
        return dependentClasses.size();
    }
    
    public boolean involvesClass(String className) {
        return dependentClasses.contains(className);
    }
    
    public String getDependencyDirection() {
        if (isUpwardDependency()) return "Upward";
        if (isDownwardDependency()) return "Downward";
        return "Same Level";
    }
    
    public String getStrengthLevel() {
        if (isWeakDependency()) return "Weak";
        if (isModerateDependency()) return "Moderate";
        return "Strong";
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
        LayerDependency that = (LayerDependency) obj;
        return dependencyCount == that.dependencyCount &&
               Double.compare(that.dependencyStrength, dependencyStrength) == 0 &&
               isViolation == that.isViolation &&
               sourceLayer == that.sourceLayer &&
               targetLayer == that.targetLayer &&
               Objects.equals(sourcePackage, that.sourcePackage) &&
               Objects.equals(targetPackage, that.targetPackage) &&
               Objects.equals(dependentClasses, that.dependentClasses) &&
               dependencyType == that.dependencyType &&
               Objects.equals(violationReason, that.violationReason);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(sourceLayer, targetLayer, sourcePackage, targetPackage, dependentClasses,
                dependencyType, dependencyCount, dependencyStrength, isViolation, violationReason);
    }
    
    @Override
    public String toString() {
        return "LayerDependency{" +
                "from=" + sourceLayer +
                ", to=" + targetLayer +
                ", type=" + dependencyType +
                ", count=" + dependencyCount +
                ", strength=" + String.format("%.2f", dependencyStrength) +
                ", violation=" + isViolation +
                '}';
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public enum DependencyType {
        DIRECT("Direct", "Direct class-to-class dependency"),
        COMPOSITION("Composition", "Class composition or aggregation"),
        INHERITANCE("Inheritance", "Inheritance or interface implementation"),
        ANNOTATION("Annotation", "Annotation-based dependency"),
        METHOD_CALL("Method Call", "Method invocation dependency"),
        FIELD_ACCESS("Field Access", "Field access dependency"),
        PARAMETER("Parameter", "Method parameter dependency"),
        RETURN_TYPE("Return Type", "Return type dependency"),
        EXCEPTION("Exception", "Exception handling dependency"),
        IMPORT("Import", "Package import dependency");
        
        private final String displayName;
        private final String description;
        
        DependencyType(String displayName, String description) {
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
    
    public static class Builder {
        private LayerType sourceLayer;
        private LayerType targetLayer;
        private String sourcePackage;
        private String targetPackage;
        private Set<String> dependentClasses = new HashSet<>();
        private DependencyType dependencyType = DependencyType.DIRECT;
        private int dependencyCount = 1;
        private double dependencyStrength = 0.5;
        private boolean isViolation = false;
        private String violationReason;
        
        public Builder sourceLayer(LayerType sourceLayer) {
            this.sourceLayer = sourceLayer;
            return this;
        }
        
        public Builder targetLayer(LayerType targetLayer) {
            this.targetLayer = targetLayer;
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
        
        public Builder addDependentClass(String className) {
            if (className != null && !className.trim().isEmpty()) {
                dependentClasses.add(className.trim());
            }
            return this;
        }
        
        public Builder dependentClasses(Set<String> classes) {
            this.dependentClasses = new HashSet<>(classes != null ? classes : Collections.emptySet());
            return this;
        }
        
        public Builder dependencyType(DependencyType dependencyType) {
            this.dependencyType = dependencyType;
            return this;
        }
        
        public Builder dependencyCount(int dependencyCount) {
            this.dependencyCount = dependencyCount;
            return this;
        }
        
        public Builder dependencyStrength(double dependencyStrength) {
            this.dependencyStrength = dependencyStrength;
            return this;
        }
        
        public Builder violation(boolean isViolation) {
            this.isViolation = isViolation;
            return this;
        }
        
        public Builder violationReason(String violationReason) {
            this.violationReason = violationReason;
            return this;
        }
        
        public LayerDependency build() {
            // Auto-detect violation if not explicitly set
            if (!isViolation && sourceLayer != null && targetLayer != null) {
                if (!sourceLayer.canDependOn(targetLayer)) {
                    isViolation = true;
                    if (violationReason == null) {
                        violationReason = String.format("%s layer should not depend on %s layer", 
                                sourceLayer.getDisplayName(), targetLayer.getDisplayName());
                    }
                }
            }
            
            return new LayerDependency(this);
        }
    }
}