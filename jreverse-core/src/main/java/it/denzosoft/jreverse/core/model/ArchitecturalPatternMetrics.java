package it.denzosoft.jreverse.core.model;

import java.util.*;

/**
 * Metrics related to architectural pattern analysis.
 * This is an immutable value object following Clean Architecture principles.
 */
public final class ArchitecturalPatternMetrics {
    
    private final int totalPatternsDetected;
    private final int totalAntiPatternsDetected;
    private final Map<ArchitecturalPatternType, Integer> patternsByType;
    private final Map<AntiPatternType, Integer> antiPatternsByType;
    private final Map<DesignPatternType, Integer> designPatternsByType;
    private final double overallPatternQuality; // 0.0 - 1.0
    private final double architecturalComplexity; // 0.0 - 1.0
    private final int totalRecommendations;
    private final Map<ViolationSeverity, Integer> violationsBySeverity;
    private final double patternCoverage; // 0.0 - 1.0 (% of code following patterns)
    private final Set<String> mostUsedPatterns;
    private final Set<String> mostProblematicAreas;
    
    private ArchitecturalPatternMetrics(Builder builder) {
        this.totalPatternsDetected = Math.max(0, builder.totalPatternsDetected);
        this.totalAntiPatternsDetected = Math.max(0, builder.totalAntiPatternsDetected);
        this.patternsByType = Collections.unmodifiableMap(new HashMap<>(builder.patternsByType));
        this.antiPatternsByType = Collections.unmodifiableMap(new HashMap<>(builder.antiPatternsByType));
        this.designPatternsByType = Collections.unmodifiableMap(new HashMap<>(builder.designPatternsByType));
        this.overallPatternQuality = Math.min(1.0, Math.max(0.0, builder.overallPatternQuality));
        this.architecturalComplexity = Math.min(1.0, Math.max(0.0, builder.architecturalComplexity));
        this.totalRecommendations = Math.max(0, builder.totalRecommendations);
        this.violationsBySeverity = Collections.unmodifiableMap(new HashMap<>(builder.violationsBySeverity));
        this.patternCoverage = Math.min(1.0, Math.max(0.0, builder.patternCoverage));
        this.mostUsedPatterns = Collections.unmodifiableSet(new HashSet<>(builder.mostUsedPatterns));
        this.mostProblematicAreas = Collections.unmodifiableSet(new HashSet<>(builder.mostProblematicAreas));
    }
    
    public int getTotalPatternsDetected() {
        return totalPatternsDetected;
    }
    
    public int getTotalAntiPatternsDetected() {
        return totalAntiPatternsDetected;
    }
    
    public Map<ArchitecturalPatternType, Integer> getPatternsByType() {
        return patternsByType;
    }
    
    public Map<AntiPatternType, Integer> getAntiPatternsByType() {
        return antiPatternsByType;
    }
    
    public Map<DesignPatternType, Integer> getDesignPatternsByType() {
        return designPatternsByType;
    }
    
    public double getOverallPatternQuality() {
        return overallPatternQuality;
    }
    
    public double getArchitecturalComplexity() {
        return architecturalComplexity;
    }
    
    public int getTotalRecommendations() {
        return totalRecommendations;
    }
    
    public Map<ViolationSeverity, Integer> getViolationsBySeverity() {
        return violationsBySeverity;
    }
    
    public double getPatternCoverage() {
        return patternCoverage;
    }
    
    public Set<String> getMostUsedPatterns() {
        return mostUsedPatterns;
    }
    
    public Set<String> getMostProblematicAreas() {
        return mostProblematicAreas;
    }
    
    public int getTotalDesignPatternsDetected() {
        return designPatternsByType.values().stream().mapToInt(Integer::intValue).sum();
    }
    
    public int getPatternCount(ArchitecturalPatternType type) {
        return patternsByType.getOrDefault(type, 0);
    }
    
    public int getAntiPatternCount(AntiPatternType type) {
        return antiPatternsByType.getOrDefault(type, 0);
    }
    
    public int getDesignPatternCount(DesignPatternType type) {
        return designPatternsByType.getOrDefault(type, 0);
    }
    
    public int getViolationCount(ViolationSeverity severity) {
        return violationsBySeverity.getOrDefault(severity, 0);
    }
    
    public int getHighSeverityViolations() {
        return getViolationCount(ViolationSeverity.HIGH);
    }
    
    public int getMediumSeverityViolations() {
        return getViolationCount(ViolationSeverity.MEDIUM);
    }
    
    public int getLowSeverityViolations() {
        return getViolationCount(ViolationSeverity.LOW);
    }
    
    public boolean hasHighQuality() {
        return overallPatternQuality >= 0.8;
    }
    
    public boolean hasMediumQuality() {
        return overallPatternQuality >= 0.6 && overallPatternQuality < 0.8;
    }
    
    public boolean hasLowQuality() {
        return overallPatternQuality < 0.6;
    }
    
    public boolean hasHighComplexity() {
        return architecturalComplexity >= 0.7;
    }
    
    public boolean hasMediumComplexity() {
        return architecturalComplexity >= 0.4 && architecturalComplexity < 0.7;
    }
    
    public boolean hasLowComplexity() {
        return architecturalComplexity < 0.4;
    }
    
    public boolean hasGoodPatternCoverage() {
        return patternCoverage >= 0.7;
    }
    
    public boolean hasDecentPatternCoverage() {
        return patternCoverage >= 0.5 && patternCoverage < 0.7;
    }
    
    public boolean hasPoorPatternCoverage() {
        return patternCoverage < 0.5;
    }
    
    public double getPatternToAntiPatternRatio() {
        if (totalAntiPatternsDetected == 0) {
            return totalPatternsDetected > 0 ? Double.MAX_VALUE : 0.0;
        }
        return (double) totalPatternsDetected / totalAntiPatternsDetected;
    }
    
    public String getQualityLevel() {
        if (hasHighQuality()) return "High";
        if (hasMediumQuality()) return "Medium";
        return "Low";
    }
    
    public String getComplexityLevel() {
        if (hasHighComplexity()) return "High";
        if (hasMediumComplexity()) return "Medium";
        return "Low";
    }
    
    public String getCoverageLevel() {
        if (hasGoodPatternCoverage()) return "Good";
        if (hasDecentPatternCoverage()) return "Decent";
        return "Poor";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ArchitecturalPatternMetrics that = (ArchitecturalPatternMetrics) obj;
        return totalPatternsDetected == that.totalPatternsDetected &&
               totalAntiPatternsDetected == that.totalAntiPatternsDetected &&
               Double.compare(that.overallPatternQuality, overallPatternQuality) == 0 &&
               Double.compare(that.architecturalComplexity, architecturalComplexity) == 0 &&
               totalRecommendations == that.totalRecommendations &&
               Double.compare(that.patternCoverage, patternCoverage) == 0 &&
               Objects.equals(patternsByType, that.patternsByType) &&
               Objects.equals(antiPatternsByType, that.antiPatternsByType) &&
               Objects.equals(designPatternsByType, that.designPatternsByType) &&
               Objects.equals(violationsBySeverity, that.violationsBySeverity) &&
               Objects.equals(mostUsedPatterns, that.mostUsedPatterns) &&
               Objects.equals(mostProblematicAreas, that.mostProblematicAreas);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(totalPatternsDetected, totalAntiPatternsDetected, patternsByType,
                antiPatternsByType, designPatternsByType, overallPatternQuality, architecturalComplexity,
                totalRecommendations, violationsBySeverity, patternCoverage, mostUsedPatterns,
                mostProblematicAreas);
    }
    
    @Override
    public String toString() {
        return "ArchitecturalPatternMetrics{" +
                "patterns=" + totalPatternsDetected +
                ", antiPatterns=" + totalAntiPatternsDetected +
                ", quality=" + String.format("%.2f", overallPatternQuality) +
                ", complexity=" + String.format("%.2f", architecturalComplexity) +
                ", coverage=" + String.format("%.2f", patternCoverage) +
                ", recommendations=" + totalRecommendations +
                '}';
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private int totalPatternsDetected = 0;
        private int totalAntiPatternsDetected = 0;
        private Map<ArchitecturalPatternType, Integer> patternsByType = new HashMap<>();
        private Map<AntiPatternType, Integer> antiPatternsByType = new HashMap<>();
        private Map<DesignPatternType, Integer> designPatternsByType = new HashMap<>();
        private double overallPatternQuality = 0.5;
        private double architecturalComplexity = 0.5;
        private int totalRecommendations = 0;
        private Map<ViolationSeverity, Integer> violationsBySeverity = new HashMap<>();
        private double patternCoverage = 0.0;
        private Set<String> mostUsedPatterns = new HashSet<>();
        private Set<String> mostProblematicAreas = new HashSet<>();
        
        public Builder totalPatternsDetected(int totalPatternsDetected) {
            this.totalPatternsDetected = totalPatternsDetected;
            return this;
        }
        
        public Builder totalAntiPatternsDetected(int totalAntiPatternsDetected) {
            this.totalAntiPatternsDetected = totalAntiPatternsDetected;
            return this;
        }
        
        public Builder addPattern(ArchitecturalPatternType type, int count) {
            patternsByType.put(type, count);
            return this;
        }
        
        public Builder incrementPattern(ArchitecturalPatternType type) {
            patternsByType.merge(type, 1, Integer::sum);
            totalPatternsDetected++;
            return this;
        }
        
        public Builder patternsByType(Map<ArchitecturalPatternType, Integer> patterns) {
            this.patternsByType = new HashMap<>(patterns != null ? patterns : Collections.emptyMap());
            return this;
        }
        
        public Builder addAntiPattern(AntiPatternType type, int count) {
            antiPatternsByType.put(type, count);
            return this;
        }
        
        public Builder incrementAntiPattern(AntiPatternType type) {
            antiPatternsByType.merge(type, 1, Integer::sum);
            totalAntiPatternsDetected++;
            return this;
        }
        
        public Builder antiPatternsByType(Map<AntiPatternType, Integer> antiPatterns) {
            this.antiPatternsByType = new HashMap<>(antiPatterns != null ? antiPatterns : Collections.emptyMap());
            return this;
        }
        
        public Builder addDesignPattern(DesignPatternType type, int count) {
            designPatternsByType.put(type, count);
            return this;
        }
        
        public Builder incrementDesignPattern(DesignPatternType type) {
            designPatternsByType.merge(type, 1, Integer::sum);
            return this;
        }
        
        public Builder designPatternsByType(Map<DesignPatternType, Integer> designPatterns) {
            this.designPatternsByType = new HashMap<>(designPatterns != null ? designPatterns : Collections.emptyMap());
            return this;
        }
        
        public Builder overallPatternQuality(double overallPatternQuality) {
            this.overallPatternQuality = overallPatternQuality;
            return this;
        }
        
        public Builder architecturalComplexity(double architecturalComplexity) {
            this.architecturalComplexity = architecturalComplexity;
            return this;
        }
        
        public Builder totalRecommendations(int totalRecommendations) {
            this.totalRecommendations = totalRecommendations;
            return this;
        }
        
        public Builder addViolation(ViolationSeverity severity, int count) {
            violationsBySeverity.put(severity, count);
            return this;
        }
        
        public Builder incrementViolation(ViolationSeverity severity) {
            violationsBySeverity.merge(severity, 1, Integer::sum);
            return this;
        }
        
        public Builder violationsBySeverity(Map<ViolationSeverity, Integer> violations) {
            this.violationsBySeverity = new HashMap<>(violations != null ? violations : Collections.emptyMap());
            return this;
        }
        
        public Builder patternCoverage(double patternCoverage) {
            this.patternCoverage = patternCoverage;
            return this;
        }
        
        public Builder addMostUsedPattern(String pattern) {
            if (pattern != null && !pattern.trim().isEmpty()) {
                mostUsedPatterns.add(pattern.trim());
            }
            return this;
        }
        
        public Builder mostUsedPatterns(Set<String> patterns) {
            this.mostUsedPatterns = new HashSet<>(patterns != null ? patterns : Collections.emptySet());
            return this;
        }
        
        public Builder addProblematicArea(String area) {
            if (area != null && !area.trim().isEmpty()) {
                mostProblematicAreas.add(area.trim());
            }
            return this;
        }
        
        public Builder mostProblematicAreas(Set<String> areas) {
            this.mostProblematicAreas = new HashSet<>(areas != null ? areas : Collections.emptySet());
            return this;
        }
        
        public ArchitecturalPatternMetrics build() {
            return new ArchitecturalPatternMetrics(this);
        }
    }
}