package it.denzosoft.jreverse.core.model;

/**
 * Summary of security analysis results for external libraries.
 *
 * @author JReverse Development Team
 * @since 1.1.0 (Phase 3)
 */
public class SecuritySummary {

    private final int totalLibraries;
    private final int vulnerableLibraries;
    private final int criticalIssues;
    private final int highIssues;
    private final int mediumIssues;
    private final int lowIssues;
    private final double overallScore;

    public SecuritySummary(int totalLibraries, int vulnerableLibraries,
                          int criticalIssues, int highIssues, int mediumIssues, int lowIssues) {
        this.totalLibraries = totalLibraries;
        this.vulnerableLibraries = vulnerableLibraries;
        this.criticalIssues = criticalIssues;
        this.highIssues = highIssues;
        this.mediumIssues = mediumIssues;
        this.lowIssues = lowIssues;
        this.overallScore = calculateOverallScore();
    }

    public static SecuritySummary noIssues() {
        return new SecuritySummary(0, 0, 0, 0, 0, 0);
    }

    private double calculateOverallScore() {
        if (totalLibraries == 0) return 100.0;

        double penalty = (criticalIssues * 40) + (highIssues * 20) + (mediumIssues * 10) + (lowIssues * 5);
        double maxPenalty = totalLibraries * 40.0;

        return Math.max(0.0, 100.0 - (penalty / maxPenalty * 100.0));
    }

    // Getters
    public int getTotalLibraries() { return totalLibraries; }
    public int getVulnerableLibraries() { return vulnerableLibraries; }
    public int getCriticalIssues() { return criticalIssues; }
    public int getHighIssues() { return highIssues; }
    public int getMediumIssues() { return mediumIssues; }
    public int getLowIssues() { return lowIssues; }
    public double getOverallScore() { return overallScore; }

    public boolean hasIssues() {
        return vulnerableLibraries > 0;
    }

    public boolean hasCriticalIssues() {
        return criticalIssues > 0;
    }
}