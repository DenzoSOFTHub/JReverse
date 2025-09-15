package it.denzosoft.jreverse.core.model;

import java.util.*;

/**
 * Represents an architectural pattern detected in the package structure.
 */
public final class ArchitecturalPattern {
    
    private final ArchitecturalPatternType patternType;
    private final String name;
    private final String description;
    private final Set<String> involvedPackages;
    private final double confidenceScore; // 0.0 - 1.0
    private final Map<String, String> characteristics;
    
    private ArchitecturalPattern(Builder builder) {
        this.patternType = Objects.requireNonNull(builder.patternType, "patternType cannot be null");
        this.name = requireNonEmpty(builder.name, "name");
        this.description = builder.description;
        this.involvedPackages = Collections.unmodifiableSet(new HashSet<>(builder.involvedPackages));
        this.confidenceScore = Math.min(1.0, Math.max(0.0, builder.confidenceScore));
        this.characteristics = Collections.unmodifiableMap(new HashMap<>(builder.characteristics));
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
    
    public double getConfidenceScore() {
        return confidenceScore;
    }
    
    public Map<String, String> getCharacteristics() {
        return characteristics;
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
        ArchitecturalPattern that = (ArchitecturalPattern) obj;
        return Double.compare(that.confidenceScore, confidenceScore) == 0 &&
               patternType == that.patternType &&
               Objects.equals(name, that.name) &&
               Objects.equals(description, that.description) &&
               Objects.equals(involvedPackages, that.involvedPackages) &&
               Objects.equals(characteristics, that.characteristics);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(patternType, name, description, involvedPackages, confidenceScore, characteristics);
    }
    
    @Override
    public String toString() {
        return "ArchitecturalPattern{" +
                "type=" + patternType +
                ", name='" + name + '\'' +
                ", confidence=" + String.format("%.2f", confidenceScore) +
                ", packages=" + involvedPackages.size() +
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
        private double confidenceScore = 0.0;
        private Map<String, String> characteristics = new HashMap<>();
        
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
        
        public Builder confidenceScore(double confidenceScore) {
            this.confidenceScore = confidenceScore;
            return this;
        }
        
        public Builder addCharacteristic(String key, String value) {
            if (key != null && value != null) {
                characteristics.put(key, value);
            }
            return this;
        }
        
        public Builder characteristics(Map<String, String> characteristics) {
            this.characteristics = new HashMap<>(characteristics != null ? characteristics : Collections.emptyMap());
            return this;
        }
        
        public ArchitecturalPattern build() {
            return new ArchitecturalPattern(this);
        }
    }
}