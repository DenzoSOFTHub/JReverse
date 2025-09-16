package it.denzosoft.jreverse.core.model;

import java.util.*;

/**
 * Represents a design pattern (legacy model for test compatibility).
 * This is for backward compatibility - use DetectedDesignPattern for new code.
 */
public final class DesignPattern {
    
    public enum Complexity {
        LOW, MEDIUM, HIGH
    }
    
    private final String patternName;
    private final Set<String> participatingClasses;
    private final double confidence;
    private final String description;
    private final Complexity complexity;
    private final String evidence;
    
    public DesignPattern(String patternName, Set<String> participatingClasses, double confidence, String description) {
        this(patternName, participatingClasses, confidence, description, Complexity.MEDIUM, "Pattern detected by static analysis");
    }
    
    public DesignPattern(String patternName, Set<String> participatingClasses, double confidence, String description, Complexity complexity, String evidence) {
        this.patternName = Objects.requireNonNull(patternName, "Pattern name cannot be null");
        this.participatingClasses = Collections.unmodifiableSet(new HashSet<>(participatingClasses));
        this.confidence = Math.max(0.0, Math.min(1.0, confidence));
        this.description = description;
        this.complexity = complexity != null ? complexity : Complexity.MEDIUM;
        this.evidence = evidence != null ? evidence : "Pattern detected by static analysis";
    }
    
    public String getPatternName() {
        return patternName;
    }
    
    public String getType() {
        return patternName;
    }
    
    public String getName() {
        return patternName;
    }
    
    public Set<String> getParticipatingClasses() {
        return participatingClasses;
    }
    
    public double getConfidence() {
        return confidence;
    }
    
    public double getConfidenceLevel() {
        return confidence;
    }
    
    public Complexity getComplexity() {
        return complexity;
    }
    
    public String getEvidence() {
        return evidence;
    }
    
    public String getDescription() {
        return description;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DesignPattern that = (DesignPattern) obj;
        return Objects.equals(patternName, that.patternName) &&
               Objects.equals(participatingClasses, that.participatingClasses);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(patternName, participatingClasses);
    }
    
    // Factory methods
    public static DesignPattern singleton(String className, double confidence) {
        return new DesignPattern("Singleton", Set.of(className), confidence, 
                               "Singleton pattern implementation", Complexity.LOW, 
                               "Class with singleton characteristics detected");
    }
    
    public static DesignPattern factory(String factoryClass, Set<String> createdClasses, double confidence) {
        Set<String> allClasses = new HashSet<>(createdClasses);
        allClasses.add(factoryClass);
        return new DesignPattern("Factory", allClasses, confidence,
                               "Factory pattern implementation", Complexity.MEDIUM,
                               "Factory class creating multiple related objects");
    }
    
    public static DesignPattern observer(String subjectClass, Set<String> observerClasses, double confidence) {
        Set<String> allClasses = new HashSet<>(observerClasses);
        allClasses.add(subjectClass);
        return new DesignPattern("Observer", allClasses, confidence,
                               "Observer pattern implementation", Complexity.HIGH,
                               "Subject-observer relationship detected");
    }
    
    @Override
    public String toString() {
        return "DesignPattern{" +
                "name='" + patternName + '\'' +
                ", classes=" + participatingClasses.size() +
                ", confidence=" + String.format("%.2f", confidence) +
                ", complexity=" + complexity +
                '}';
    }
}