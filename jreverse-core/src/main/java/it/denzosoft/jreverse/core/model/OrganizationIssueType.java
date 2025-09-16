package it.denzosoft.jreverse.core.model;

/**
 * Types of organizational issues in package structures.
 */
public enum OrganizationIssueType {
    
    POOR_COHESION("Poor Cohesion", "Package has low internal cohesion"),
    HIGH_COUPLING("High Coupling", "Package is tightly coupled to others"),
    MISSING_ABSTRACTION("Missing Abstraction", "Package lacks proper abstractions"),
    INAPPROPRIATE_SIZE("Inappropriate Size", "Package is too large or too small"),
    UNCLEAR_RESPONSIBILITY("Unclear Responsibility", "Package responsibility is not clear"),
    DEPENDENCY_VIOLATION("Dependency Violation", "Package violates dependency rules"),
    LAYERING_ISSUE("Layering Issue", "Package doesn't fit architectural layers"),
    DOMAIN_MIXING("Domain Mixing", "Package mixes multiple domain concerns"),
    TECHNICAL_DEBT("Technical Debt", "Package has accumulated technical debt"),
    STRUCTURAL_INCONSISTENCY("Structural Inconsistency", "Package structure is inconsistent");
    
    private final String displayName;
    private final String description;
    
    OrganizationIssueType(String displayName, String description) {
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