package it.denzosoft.jreverse.core.model;

import java.util.*;

/**
 * Represents a detected architectural pattern in the analyzed codebase.
 * This is an immutable value object following Clean Architecture principles.
 */
public final class DetectedArchitecturalPattern {
    
    private final ArchitecturalPatternType patternType;
    private final String name;
    private final String description;
    private final Set<String> involvedPackages;
    private final Set<String> involvedClasses;
    private final double confidenceScore; // 0.0 - 1.0
    private final Map<String, String> evidence;
    private final Set<String> keyCharacteristics;
    private final String location;
    
    private DetectedArchitecturalPattern(Builder builder) {
        this.patternType = Objects.requireNonNull(builder.patternType, "patternType cannot be null");
        this.name = requireNonEmpty(builder.name, "name");
        this.description = builder.description;
        this.involvedPackages = Collections.unmodifiableSet(new HashSet<>(builder.involvedPackages));
        this.involvedClasses = Collections.unmodifiableSet(new HashSet<>(builder.involvedClasses));
        this.confidenceScore = Math.min(1.0, Math.max(0.0, builder.confidenceScore));
        this.evidence = Collections.unmodifiableMap(new HashMap<>(builder.evidence));
        this.keyCharacteristics = Collections.unmodifiableSet(new HashSet<>(builder.keyCharacteristics));
        this.location = builder.location;
    }
    
    public ArchitecturalPatternType getPatternType() {
        return patternType;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public Set<String> getInvolvedPackages() {
        return involvedPackages;
    }
    
    public Set<String> getInvolvedClasses() {
        return involvedClasses;
    }
    
    public double getConfidenceScore() {
        return confidenceScore;
    }
    
    public Map<String, String> getEvidence() {
        return evidence;
    }
    
    public Set<String> getKeyCharacteristics() {
        return keyCharacteristics;
    }
    
    public String getLocation() {
        return location;
    }
    
    public boolean hasLocation() {
        return location != null && !location.isEmpty();
    }
    
    public boolean isHighConfidence() {
        return confidenceScore >= 0.8;
    }
    
    public boolean isMediumConfidence() {
        return confidenceScore >= 0.6 && confidenceScore < 0.8;
    }
    
    public boolean isLowConfidence() {
        return confidenceScore < 0.6;
    }
    
    public int getTotalInvolvedElements() {
        return involvedPackages.size() + involvedClasses.size();
    }
    
    public boolean hasEvidence(String evidenceKey) {
        return evidence.containsKey(evidenceKey);
    }
    
    public String getEvidenceValue(String evidenceKey) {
        return evidence.get(evidenceKey);
    }
    
    public boolean hasCharacteristic(String characteristic) {
        return keyCharacteristics.contains(characteristic);
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
        DetectedArchitecturalPattern that = (DetectedArchitecturalPattern) obj;
        return Double.compare(that.confidenceScore, confidenceScore) == 0 &&
               patternType == that.patternType &&
               Objects.equals(name, that.name) &&
               Objects.equals(description, that.description) &&
               Objects.equals(involvedPackages, that.involvedPackages) &&
               Objects.equals(involvedClasses, that.involvedClasses) &&
               Objects.equals(evidence, that.evidence) &&
               Objects.equals(keyCharacteristics, that.keyCharacteristics) &&
               Objects.equals(location, that.location);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(patternType, name, description, involvedPackages, involvedClasses,
                confidenceScore, evidence, keyCharacteristics, location);
    }
    
    @Override
    public String toString() {
        return "DetectedArchitecturalPattern{" +
                "type=" + patternType +
                ", name='" + name + '\'' +
                ", confidence=" + String.format("%.2f", confidenceScore) +
                ", packages=" + involvedPackages.size() +
                ", classes=" + involvedClasses.size() +
                ", characteristics=" + keyCharacteristics.size() +
                '}';
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private ArchitecturalPatternType patternType;
        private String name;
        private String description;
        private Set<String> involvedPackages = new HashSet<>();
        private Set<String> involvedClasses = new HashSet<>();
        private double confidenceScore = 0.0;
        private Map<String, String> evidence = new HashMap<>();
        private Set<String> keyCharacteristics = new HashSet<>();
        private String location;
        
        public Builder patternType(ArchitecturalPatternType patternType) {
            this.patternType = patternType;
            return this;
        }
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder addInvolvedPackage(String packageName) {
            if (packageName != null && !packageName.trim().isEmpty()) {
                involvedPackages.add(packageName.trim());
            }
            return this;
        }
        
        public Builder involvedPackages(Set<String> packages) {
            this.involvedPackages = new HashSet<>(packages != null ? packages : Collections.emptySet());
            return this;
        }
        
        public Builder addInvolvedClass(String className) {
            if (className != null && !className.trim().isEmpty()) {
                involvedClasses.add(className.trim());
            }
            return this;
        }
        
        public Builder involvedClasses(Set<String> classes) {
            this.involvedClasses = new HashSet<>(classes != null ? classes : Collections.emptySet());
            return this;
        }
        
        public Builder confidenceScore(double confidenceScore) {
            this.confidenceScore = confidenceScore;
            return this;
        }
        
        public Builder addEvidence(String key, String value) {
            if (key != null && value != null) {
                evidence.put(key, value);
            }
            return this;
        }
        
        public Builder evidence(Map<String, String> evidence) {
            this.evidence = new HashMap<>(evidence != null ? evidence : Collections.emptyMap());
            return this;
        }
        
        public Builder addCharacteristic(String characteristic) {
            if (characteristic != null && !characteristic.trim().isEmpty()) {
                keyCharacteristics.add(characteristic.trim());
            }
            return this;
        }
        
        public Builder keyCharacteristics(Set<String> characteristics) {
            this.keyCharacteristics = new HashSet<>(characteristics != null ? characteristics : Collections.emptySet());
            return this;
        }
        
        public Builder location(String location) {
            this.location = location;
            return this;
        }
        
        public DetectedArchitecturalPattern build() {
            return new DetectedArchitecturalPattern(this);
        }
    }
}