package it.denzosoft.jreverse.core.model;

/**
 * Represents the overall architectural quality assessment.
 */
public enum ArchitecturalQuality {
    
    EXCELLENT("Excellent architectural quality"),
    GOOD("Good architectural quality"),
    FAIR("Fair architectural quality"),
    POOR("Poor architectural quality"),
    UNKNOWN("Unknown architectural quality");
    
    private final String description;
    
    ArchitecturalQuality(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    @Override
    public String toString() {
        return name() + " (" + description + ")";
    }
}