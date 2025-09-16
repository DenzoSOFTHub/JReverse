package it.denzosoft.jreverse.core.model;

/**
 * Security executive summary model.
 */
public class SecurityExecutiveSummary {
    private final String overallAssessment;
    private final int totalIssues;
    private final int criticalIssues;

    public SecurityExecutiveSummary(String overallAssessment, int totalIssues, int criticalIssues) {
        this.overallAssessment = overallAssessment != null ? overallAssessment : "No assessment available";
        this.totalIssues = Math.max(0, totalIssues);
        this.criticalIssues = Math.max(0, criticalIssues);
    }

    public String getOverallAssessment() { return overallAssessment; }
    public int getTotalIssues() { return totalIssues; }
    public int getCriticalIssues() { return criticalIssues; }
}