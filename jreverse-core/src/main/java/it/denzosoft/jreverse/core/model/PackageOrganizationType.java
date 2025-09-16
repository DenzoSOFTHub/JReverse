package it.denzosoft.jreverse.core.model;

/**
 * Types of package organization patterns.
 */
public enum PackageOrganizationType {
    
    FEATURE_BASED("Feature-Based", "Packages organized by features/functionality"),
    LAYER_BASED("Layer-Based", "Packages organized by technical layers"),
    DOMAIN_BASED("Domain-Based", "Packages organized by business domains"),
    COMPONENT_BASED("Component-Based", "Packages organized by components"),
    HYBRID("Hybrid", "Mixed organization approach"),
    FLAT("Flat", "Minimal package hierarchy"),
    TECHNICAL("Technical", "Purely technical organization"),
    UNKNOWN("Unknown", "Organization pattern could not be determined");
    
    private final String displayName;
    private final String description;
    
    PackageOrganizationType(String displayName, String description) {
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