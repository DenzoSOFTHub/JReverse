package it.denzosoft.jreverse.core.model;

import java.util.*;

/**
 * Represents an architectural recommendation based on pattern analysis.
 * This is an immutable value object following Clean Architecture principles.
 */
public final class ArchitecturalRecommendation {
    
    private final String title;
    private final String description;
    private final RecommendationType type;
    private final Priority priority;
    private final Set<String> affectedComponents;
    private final String rationale;
    private final Set<String> benefits;
    private final Set<String> implementationSteps;
    private final double impactScore; // 0.0 - 1.0
    private final Map<String, String> relatedPatterns;
    private final Set<String> tags;
    
    private ArchitecturalRecommendation(Builder builder) {
        this.title = requireNonEmpty(builder.title, "title");
        this.description = requireNonEmpty(builder.description, "description");
        this.type = Objects.requireNonNull(builder.type, "type cannot be null");
        this.priority = Objects.requireNonNull(builder.priority, "priority cannot be null");
        this.affectedComponents = Collections.unmodifiableSet(new HashSet<>(builder.affectedComponents));
        this.rationale = builder.rationale;
        this.benefits = Collections.unmodifiableSet(new HashSet<>(builder.benefits));
        this.implementationSteps = Collections.unmodifiableSet(new LinkedHashSet<>(builder.implementationSteps));
        this.impactScore = Math.min(1.0, Math.max(0.0, builder.impactScore));
        this.relatedPatterns = Collections.unmodifiableMap(new HashMap<>(builder.relatedPatterns));
        this.tags = Collections.unmodifiableSet(new HashSet<>(builder.tags));
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
    
    public Set<String> getAffectedComponents() {
        return affectedComponents;
    }
    
    public String getRationale() {
        return rationale;
    }
    
    public Set<String> getBenefits() {
        return benefits;
    }
    
    public Set<String> getImplementationSteps() {
        return implementationSteps;
    }
    
    public double getImpactScore() {
        return impactScore;
    }
    
    public Map<String, String> getRelatedPatterns() {
        return relatedPatterns;
    }
    
    public Set<String> getTags() {
        return tags;
    }
    
    public boolean hasRationale() {
        return rationale != null && !rationale.isEmpty();
    }
    
    public boolean isHighPriority() {
        return priority == Priority.HIGH;
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
    
    public int getComponentCount() {
        return affectedComponents.size();
    }
    
    public int getBenefitCount() {
        return benefits.size();
    }
    
    public int getImplementationStepCount() {
        return implementationSteps.size();
    }
    
    public boolean hasTag(String tag) {
        return tags.contains(tag);
    }
    
    public boolean hasRelatedPattern(String patternName) {
        return relatedPatterns.containsKey(patternName);
    }
    
    public String getRelatedPatternDescription(String patternName) {
        return relatedPatterns.get(patternName);
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
        ArchitecturalRecommendation that = (ArchitecturalRecommendation) obj;
        return Double.compare(that.impactScore, impactScore) == 0 &&
               Objects.equals(title, that.title) &&
               Objects.equals(description, that.description) &&
               type == that.type &&
               priority == that.priority &&
               Objects.equals(affectedComponents, that.affectedComponents) &&
               Objects.equals(rationale, that.rationale) &&
               Objects.equals(benefits, that.benefits) &&
               Objects.equals(implementationSteps, that.implementationSteps) &&
               Objects.equals(relatedPatterns, that.relatedPatterns) &&
               Objects.equals(tags, that.tags);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(title, description, type, priority, affectedComponents, rationale,
                benefits, implementationSteps, impactScore, relatedPatterns, tags);
    }
    
    @Override
    public String toString() {
        return "ArchitecturalRecommendation{" +
                "title='" + title + '\'' +
                ", type=" + type +
                ", priority=" + priority +
                ", impact=" + String.format("%.2f", impactScore) +
                ", components=" + affectedComponents.size() +
                ", steps=" + implementationSteps.size() +
                '}';
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public enum RecommendationType {
        PATTERN_INTRODUCTION("Pattern Introduction", "Introduce a new pattern"),
        PATTERN_IMPROVEMENT("Pattern Improvement", "Improve existing pattern usage"),
        ANTI_PATTERN_REMOVAL("Anti-Pattern Removal", "Remove or fix anti-pattern"),
        ARCHITECTURE_REFACTORING("Architecture Refactoring", "Refactor architectural structure"),
        DEPENDENCY_IMPROVEMENT("Dependency Improvement", "Improve dependency management"),
        SEPARATION_OF_CONCERNS("Separation of Concerns", "Improve separation of concerns"),
        CODE_ORGANIZATION("Code Organization", "Improve code organization"),
        PERFORMANCE_OPTIMIZATION("Performance Optimization", "Optimize for better performance");
        
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
    }
    
    public enum Priority {
        HIGH("High", 3),
        MEDIUM("Medium", 2),
        LOW("Low", 1);
        
        private final String displayName;
        private final int level;
        
        Priority(String displayName, int level) {
            this.displayName = displayName;
            this.level = level;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public int getLevel() {
            return level;
        }
        
        public boolean isHigherThan(Priority other) {
            return this.level > other.level;
        }
        
        public boolean isLowerThan(Priority other) {
            return this.level < other.level;
        }
    }
    
    public static class Builder {
        private String title;
        private String description;
        private RecommendationType type;
        private Priority priority = Priority.MEDIUM;
        private Set<String> affectedComponents = new HashSet<>();
        private String rationale;
        private Set<String> benefits = new HashSet<>();
        private Set<String> implementationSteps = new LinkedHashSet<>();
        private double impactScore = 0.5;
        private Map<String, String> relatedPatterns = new HashMap<>();
        private Set<String> tags = new HashSet<>();
        
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
        
        public Builder implementationSteps(Set<String> steps) {
            this.implementationSteps = new LinkedHashSet<>(steps != null ? steps : Collections.emptySet());
            return this;
        }
        
        public Builder impactScore(double impactScore) {
            this.impactScore = impactScore;
            return this;
        }
        
        public Builder addRelatedPattern(String patternName, String description) {
            if (patternName != null && description != null) {
                relatedPatterns.put(patternName.trim(), description.trim());
            }
            return this;
        }
        
        public Builder relatedPatterns(Map<String, String> patterns) {
            this.relatedPatterns = new HashMap<>(patterns != null ? patterns : Collections.emptyMap());
            return this;
        }
        
        public Builder addTag(String tag) {
            if (tag != null && !tag.trim().isEmpty()) {
                tags.add(tag.trim());
            }
            return this;
        }
        
        public Builder tags(Set<String> tags) {
            this.tags = new HashSet<>(tags != null ? tags : Collections.emptySet());
            return this;
        }
        
        public ArchitecturalRecommendation build() {
            return new ArchitecturalRecommendation(this);
        }
    }
}