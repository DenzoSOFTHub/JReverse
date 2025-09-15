package it.denzosoft.jreverse.core.model;

/**
 * Metrics for architectural violation detection analysis.
 *
 * @author JReverse Development Team
 * @since 1.1.0 (Phase 3)
 */
public class ArchitecturalViolationMetrics {
    private final int totalViolations;
    private final int criticalViolations;
    private final int highViolations;
    private final int mediumViolations;
    private final int lowViolations;
    private final double violationDensity;
    private final double qualityScore;

    public ArchitecturalViolationMetrics(int totalViolations, int criticalViolations,
                                       int highViolations, int mediumViolations, int lowViolations,
                                       double violationDensity, double qualityScore) {
        this.totalViolations = totalViolations;
        this.criticalViolations = criticalViolations;
        this.highViolations = highViolations;
        this.mediumViolations = mediumViolations;
        this.lowViolations = lowViolations;
        this.violationDensity = violationDensity;
        this.qualityScore = qualityScore;
    }

    public int getTotalViolations() { return totalViolations; }
    public int getCriticalViolations() { return criticalViolations; }
    public int getHighViolations() { return highViolations; }
    public int getMediumViolations() { return mediumViolations; }
    public int getLowViolations() { return lowViolations; }
    public double getViolationDensity() { return violationDensity; }
    public double getQualityScore() { return qualityScore; }
}