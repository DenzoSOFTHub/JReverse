package it.denzosoft.jreverse.core.model;

import java.util.*;

/**
 * Represents a detected design pattern instance in the analyzed codebase.
 * This is an immutable value object following Clean Architecture principles.
 */
public final class DetectedDesignPattern {
    
    private final DesignPatternType patternType;
    private final String name;
    private final String description;
    private final Set<String> participatingClasses;
    private final Map<String, String> roleAssignments; // className -> role in pattern
    private final double confidenceScore; // 0.0 - 1.0
    private final Set<String> evidenceIndicators;
    private final String packageLocation;
    private final Map<String, String> patternElements; // element type -> element name
    
    private DetectedDesignPattern(Builder builder) {
        this.patternType = Objects.requireNonNull(builder.patternType, "patternType cannot be null");
        this.name = requireNonEmpty(builder.name, "name");
        this.description = builder.description;
        this.participatingClasses = Collections.unmodifiableSet(new HashSet<>(builder.participatingClasses));
        this.roleAssignments = Collections.unmodifiableMap(new HashMap<>(builder.roleAssignments));
        this.confidenceScore = Math.min(1.0, Math.max(0.0, builder.confidenceScore));
        this.evidenceIndicators = Collections.unmodifiableSet(new HashSet<>(builder.evidenceIndicators));
        this.packageLocation = builder.packageLocation;
        this.patternElements = Collections.unmodifiableMap(new HashMap<>(builder.patternElements));
    }
    
    public DesignPatternType getPatternType() {
        return patternType;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public Set<String> getParticipatingClasses() {
        return participatingClasses;
    }
    
    public Map<String, String> getRoleAssignments() {
        return roleAssignments;
    }
    
    public double getConfidenceScore() {
        return confidenceScore;
    }
    
    public Set<String> getEvidenceIndicators() {
        return evidenceIndicators;
    }
    
    public String getPackageLocation() {
        return packageLocation;
    }
    
    public Map<String, String> getPatternElements() {
        return patternElements;
    }
    
    public boolean hasPackageLocation() {
        return packageLocation != null && !packageLocation.isEmpty();
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
    
    public int getParticipantCount() {
        return participatingClasses.size();
    }
    
    public boolean hasRole(String className) {
        return roleAssignments.containsKey(className);
    }
    
    public String getRoleForClass(String className) {
        return roleAssignments.get(className);
    }
    
    public boolean hasEvidence(String indicator) {
        return evidenceIndicators.contains(indicator);
    }
    
    public boolean hasPatternElement(String elementType) {
        return patternElements.containsKey(elementType);
    }
    
    public String getPatternElement(String elementType) {
        return patternElements.get(elementType);
    }
    
    public Set<String> getClassesWithRole(String role) {
        return roleAssignments.entrySet().stream()
                .filter(entry -> role.equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .collect(HashSet::new, HashSet::add, HashSet::addAll);
    }
    
    // Legacy methods for backward compatibility
    public double getConfidence() {
        return confidenceScore;
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
        DetectedDesignPattern that = (DetectedDesignPattern) obj;
        return Double.compare(that.confidenceScore, confidenceScore) == 0 &&
               patternType == that.patternType &&
               Objects.equals(name, that.name) &&
               Objects.equals(description, that.description) &&
               Objects.equals(participatingClasses, that.participatingClasses) &&
               Objects.equals(roleAssignments, that.roleAssignments) &&
               Objects.equals(evidenceIndicators, that.evidenceIndicators) &&
               Objects.equals(packageLocation, that.packageLocation) &&
               Objects.equals(patternElements, that.patternElements);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(patternType, name, description, participatingClasses, roleAssignments,
                confidenceScore, evidenceIndicators, packageLocation, patternElements);
    }
    
    @Override
    public String toString() {
        return "DetectedDesignPattern{" +
                "type=" + patternType +
                ", name='" + name + '\'' +
                ", confidence=" + String.format("%.2f", confidenceScore) +
                ", participants=" + participatingClasses.size() +
                ", location='" + packageLocation + '\'' +
                '}';
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private DesignPatternType patternType;
        private String name;
        private String description;
        private Set<String> participatingClasses = new HashSet<>();
        private Map<String, String> roleAssignments = new HashMap<>();
        private double confidenceScore = 0.0;
        private Set<String> evidenceIndicators = new HashSet<>();
        private String packageLocation;
        private Map<String, String> patternElements = new HashMap<>();
        
        public Builder patternType(DesignPatternType patternType) {
            this.patternType = patternType;
            if (patternType != null) {
                this.name = patternType.getDisplayName();
                this.description = patternType.getDescription();
            }
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
        
        public Builder addParticipatingClass(String className) {
            if (className != null && !className.trim().isEmpty()) {
                participatingClasses.add(className.trim());
            }
            return this;
        }
        
        public Builder participatingClasses(Set<String> classes) {
            this.participatingClasses = new HashSet<>(classes != null ? classes : Collections.emptySet());
            return this;
        }
        
        public Builder assignRole(String className, String role) {
            if (className != null && role != null) {
                roleAssignments.put(className.trim(), role.trim());
                // Also add to participating classes
                addParticipatingClass(className);
            }
            return this;
        }
        
        public Builder roleAssignments(Map<String, String> roleAssignments) {
            this.roleAssignments = new HashMap<>(roleAssignments != null ? roleAssignments : Collections.emptyMap());
            // Also add all classes to participating classes
            this.roleAssignments.keySet().forEach(this::addParticipatingClass);
            return this;
        }
        
        public Builder confidenceScore(double confidenceScore) {
            this.confidenceScore = confidenceScore;
            return this;
        }
        
        // Legacy method for backward compatibility
        public Builder confidence(double confidence) {
            return confidenceScore(confidence);
        }
        
        public Builder addEvidenceIndicator(String indicator) {
            if (indicator != null && !indicator.trim().isEmpty()) {
                evidenceIndicators.add(indicator.trim());
            }
            return this;
        }
        
        public Builder evidenceIndicators(Set<String> indicators) {
            this.evidenceIndicators = new HashSet<>(indicators != null ? indicators : Collections.emptySet());
            return this;
        }
        
        public Builder packageLocation(String packageLocation) {
            this.packageLocation = packageLocation;
            return this;
        }
        
        public Builder addPatternElement(String elementType, String elementName) {
            if (elementType != null && elementName != null) {
                patternElements.put(elementType.trim(), elementName.trim());
            }
            return this;
        }
        
        public Builder patternElements(Map<String, String> elements) {
            this.patternElements = new HashMap<>(elements != null ? elements : Collections.emptyMap());
            return this;
        }
        
        public DetectedDesignPattern build() {
            // Set default name if not provided
            if (name == null && patternType != null) {
                name = patternType.getDisplayName();
            }
            return new DetectedDesignPattern(this);
        }
    }
}