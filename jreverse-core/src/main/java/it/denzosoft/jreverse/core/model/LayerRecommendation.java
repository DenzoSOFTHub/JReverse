package it.denzosoft.jreverse.core.model;

import java.util.*;

/**
 * Represents a recommendation for improving layered architecture.
 * This is an immutable value object following Clean Architecture principles.
 */
public final class LayerRecommendation {
    
    private final String title;
    private final String description;
    private final RecommendationType type;
    private final Priority priority;
    private final LayerType affectedLayer;
    private final Set<String> affectedComponents;
    private final String rationale;
    private final Set<String> benefits;
    private final List<String> implementationSteps;
    private final double impactScore; // 0.0 - 1.0
    private final double effortEstimate; // 0.0 - 1.0 (implementation effort)
    private final Set<LayerViolation> relatedViolations;
    private final Map<String, String> additionalContext;
    
    private LayerRecommendation(Builder builder) {
        this.title = requireNonEmpty(builder.title, "title");
        this.description = requireNonEmpty(builder.description, "description");
        this.type = Objects.requireNonNull(builder.type, "type cannot be null");
        this.priority = Objects.requireNonNull(builder.priority, "priority cannot be null");
        this.affectedLayer = builder.affectedLayer;
        this.affectedComponents = Collections.unmodifiableSet(new HashSet<>(builder.affectedComponents));
        this.rationale = builder.rationale;
        this.benefits = Collections.unmodifiableSet(new HashSet<>(builder.benefits));
        this.implementationSteps = Collections.unmodifiableList(new ArrayList<>(builder.implementationSteps));
        this.impactScore = Math.min(1.0, Math.max(0.0, builder.impactScore));
        this.effortEstimate = Math.min(1.0, Math.max(0.0, builder.effortEstimate));
        this.relatedViolations = Collections.unmodifiableSet(new HashSet<>(builder.relatedViolations));
        this.additionalContext = Collections.unmodifiableMap(new HashMap<>(builder.additionalContext));
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public RecommendationType getType() {
        return type;
    }
    
    public Priority getPriority() {
        return priority;
    }
    
    public LayerType getAffectedLayer() {
        return affectedLayer;
    }
    
    public Set<String> getAffectedComponents() {
        return affectedComponents;
    }
    
    public String getRationale() {
        return rationale;
    }
    
    public Set<String> getBenefits() {
        return benefits;
    }
    
    public List<String> getImplementationSteps() {
        return implementationSteps;
    }
    
    public double getImpactScore() {
        return impactScore;
    }
    
    public double getEffortEstimate() {
        return effortEstimate;
    }
    
    public Set<LayerViolation> getRelatedViolations() {
        return relatedViolations;
    }
    
    public Map<String, String> getAdditionalContext() {
        return additionalContext;
    }
    
    public boolean hasAffectedLayer() {
        return affectedLayer != null;
    }
    
    public boolean hasRationale() {
        return rationale != null && !rationale.isEmpty();
    }
    
    public boolean isHighPriority() {
        return priority == Priority.CRITICAL || priority == Priority.HIGH;
    }
    
    public boolean isMediumPriority() {
        return priority == Priority.MEDIUM;
    }
    
    public boolean isLowPriority() {
        return priority == Priority.LOW;
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
    
    public boolean isHighEffort() {
        return effortEstimate >= 0.7;
    }
    
    public boolean isMediumEffort() {
        return effortEstimate >= 0.4 && effortEstimate < 0.7;
    }
    
    public boolean isLowEffort() {
        return effortEstimate < 0.4;
    }
    
    public int getComponentCount() {
        return affectedComponents.size();
    }
    
    public int getBenefitCount() {
        return benefits.size();
    }
    
    public int getImplementationStepCount() {
        return implementationSteps.size();
    }
    
    public int getRelatedViolationCount() {
        return relatedViolations.size();
    }
    
    public boolean affectsComponent(String component) {
        return affectedComponents.contains(component);
    }
    
    public boolean hasBenefit(String benefit) {
        return benefits.contains(benefit);
    }
    
    public boolean hasContext(String key) {
        return additionalContext.containsKey(key);
    }
    
    public String getContext(String key) {
        return additionalContext.get(key);
    }
    
    public double getValueScore() {
        // Value = Impact / Effort (higher is better)
        if (effortEstimate == 0.0) return impactScore;
        return impactScore / effortEstimate;
    }
    
    public String getValueLevel() {
        double value = getValueScore();
        if (value >= 2.0) return "Excellent";
        if (value >= 1.0) return "Good";
        if (value >= 0.5) return "Fair";
        return "Poor";
    }
    
    public String getImpactLevel() {
        if (isHighImpact()) return "High";
        if (isMediumImpact()) return "Medium";
        return "Low";
    }
    
    public String getEffortLevel() {
        if (isHighEffort()) return "High";
        if (isMediumEffort()) return "Medium";
        return "Low";
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
        LayerRecommendation that = (LayerRecommendation) obj;
        return Double.compare(that.impactScore, impactScore) == 0 &&
               Double.compare(that.effortEstimate, effortEstimate) == 0 &&
               Objects.equals(title, that.title) &&
               Objects.equals(description, that.description) &&
               type == that.type &&
               priority == that.priority &&
               affectedLayer == that.affectedLayer &&
               Objects.equals(affectedComponents, that.affectedComponents) &&
               Objects.equals(rationale, that.rationale) &&
               Objects.equals(benefits, that.benefits) &&
               Objects.equals(implementationSteps, that.implementationSteps) &&
               Objects.equals(relatedViolations, that.relatedViolations) &&
               Objects.equals(additionalContext, that.additionalContext);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(title, description, type, priority, affectedLayer, affectedComponents,
                rationale, benefits, implementationSteps, impactScore, effortEstimate,
                relatedViolations, additionalContext);
    }
    
    @Override
    public String toString() {
        return "LayerRecommendation{" +
                "title='" + title + '\'' +
                ", type=" + type +
                ", priority=" + priority +
                ", layer=" + affectedLayer +
                ", impact=" + String.format("%.2f", impactScore) +
                ", effort=" + String.format("%.2f", effortEstimate) +
                ", value=" + String.format("%.2f", getValueScore()) +
                '}';
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public enum RecommendationType {
        LAYER_RESTRUCTURING("Layer Restructuring", "Reorganize classes into proper layers"),
        DEPENDENCY_REFACTORING("Dependency Refactoring", "Fix dependency violations between layers"),
        SEPARATION_OF_CONCERNS("Separation of Concerns", "Improve separation of responsibilities"),
        INTERFACE_INTRODUCTION("Interface Introduction", "Introduce interfaces to decouple layers"),
        CLASS_RELOCATION("Class Relocation", "Move classes to appropriate layers"),
        LAYER_CONSOLIDATION("Layer Consolidation", "Consolidate similar or redundant layers"),
        ABSTRACTION_IMPROVEMENT("Abstraction Improvement", "Improve abstraction levels"),
        COUPLING_REDUCTION("Coupling Reduction", "Reduce coupling between layers"),
        COHESION_IMPROVEMENT("Cohesion Improvement", "Improve cohesion within layers"),
        NAMING_CONVENTION("Naming Convention", "Improve naming conventions for layers"),
        DOCUMENTATION("Documentation", "Document layer responsibilities and boundaries");
        
        private final String displayName;
        private final String description;
        
        RecommendationType(String displayName, String description) {
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
    
    public enum Priority {
        CRITICAL("Critical", 5, "Must be addressed immediately"),
        HIGH("High", 4, "Should be addressed soon"),
        MEDIUM("Medium", 3, "Should be considered"),
        LOW("Low", 2, "Nice to have improvement"),
        SUGGESTION("Suggestion", 1, "Optional improvement");
        
        private final String displayName;
        private final int level;
        private final String description;
        
        Priority(String displayName, int level, String description) {
            this.displayName = displayName;
            this.level = level;
            this.description = description;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public int getLevel() {
            return level;
        }
        
        public String getDescription() {
            return description;
        }
        
        public boolean isHigherThan(Priority other) {
            return this.level > other.level;
        }
        
        public boolean isLowerThan(Priority other) {
            return this.level < other.level;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
    
    public static class Builder {
        private String title;
        private String description;
        private RecommendationType type;
        private Priority priority = Priority.MEDIUM;
        private LayerType affectedLayer;
        private Set<String> affectedComponents = new HashSet<>();
        private String rationale;
        private Set<String> benefits = new HashSet<>();
        private List<String> implementationSteps = new ArrayList<>();
        private double impactScore = 0.5;
        private double effortEstimate = 0.5;
        private Set<LayerViolation> relatedViolations = new HashSet<>();
        private Map<String, String> additionalContext = new HashMap<>();
        
        public Builder title(String title) {
            this.title = title;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder type(RecommendationType type) {
            this.type = type;
            return this;
        }
        
        public Builder priority(Priority priority) {
            this.priority = priority;
            return this;
        }
        
        public Builder affectedLayer(LayerType affectedLayer) {
            this.affectedLayer = affectedLayer;
            return this;
        }
        
        public Builder addAffectedComponent(String component) {
            if (component != null && !component.trim().isEmpty()) {
                affectedComponents.add(component.trim());
            }
            return this;
        }
        
        public Builder affectedComponents(Set<String> components) {
            this.affectedComponents = new HashSet<>(components != null ? components : Collections.emptySet());
            return this;
        }
        
        public Builder rationale(String rationale) {
            this.rationale = rationale;
            return this;
        }
        
        public Builder addBenefit(String benefit) {
            if (benefit != null && !benefit.trim().isEmpty()) {
                benefits.add(benefit.trim());
            }
            return this;
        }
        
        public Builder benefits(Set<String> benefits) {
            this.benefits = new HashSet<>(benefits != null ? benefits : Collections.emptySet());
            return this;
        }
        
        public Builder addImplementationStep(String step) {
            if (step != null && !step.trim().isEmpty()) {
                implementationSteps.add(step.trim());
            }
            return this;
        }
        
        public Builder implementationSteps(List<String> steps) {
            this.implementationSteps = new ArrayList<>(steps != null ? steps : Collections.emptyList());
            return this;
        }
        
        public Builder impactScore(double impactScore) {
            this.impactScore = impactScore;
            return this;
        }
        
        public Builder effortEstimate(double effortEstimate) {
            this.effortEstimate = effortEstimate;
            return this;
        }
        
        public Builder addRelatedViolation(LayerViolation violation) {
            if (violation != null) {
                relatedViolations.add(violation);
            }
            return this;
        }
        
        public Builder relatedViolations(Set<LayerViolation> violations) {
            this.relatedViolations = new HashSet<>(violations != null ? violations : Collections.emptySet());
            return this;
        }
        
        public Builder addContext(String key, String value) {
            if (key != null && value != null) {
                additionalContext.put(key, value);
            }
            return this;
        }
        
        public Builder additionalContext(Map<String, String> context) {
            this.additionalContext = new HashMap<>(context != null ? context : Collections.emptyMap());
            return this;
        }
        
        public LayerRecommendation build() {
            // Set default title if not provided
            if (title == null && type != null) {
                title = type.getDisplayName();
            }
            
            // Set default description if not provided
            if (description == null && type != null) {
                description = type.getDescription();
            }
            
            return new LayerRecommendation(this);
        }
    }
}