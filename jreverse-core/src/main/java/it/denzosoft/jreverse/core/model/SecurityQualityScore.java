package it.denzosoft.jreverse.core.model;

/**
 * Security quality scoring model for comprehensive assessment.
 */
public class SecurityQualityScore {
    private final double overallScore;
    private final String securityGrade;
    private final double configurationScore;
    private final double vulnerabilityScore;

    public SecurityQualityScore(double overallScore, String securityGrade,
                               double configurationScore, double vulnerabilityScore) {
        this.overallScore = Math.max(0.0, Math.min(100.0, overallScore));
        this.securityGrade = securityGrade != null ? securityGrade : "F";
        this.configurationScore = Math.max(0.0, Math.min(100.0, configurationScore));
        this.vulnerabilityScore = Math.max(0.0, Math.min(100.0, vulnerabilityScore));
    }

    public double getOverallScore() { return overallScore; }
    public String getSecurityGrade() { return securityGrade; }
    public double getConfigurationScore() { return configurationScore; }
    public double getVulnerabilityScore() { return vulnerabilityScore; }
}