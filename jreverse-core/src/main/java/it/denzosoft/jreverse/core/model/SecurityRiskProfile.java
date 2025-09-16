package it.denzosoft.jreverse.core.model;

/**
 * Security risk profile for overall security assessment.
 */
public class SecurityRiskProfile {
    private final SecurityRiskLevel riskLevel;
    private final int riskScore;

    public SecurityRiskProfile(SecurityRiskLevel riskLevel, int riskScore) {
        this.riskLevel = riskLevel != null ? riskLevel : SecurityRiskLevel.MEDIUM;
        this.riskScore = Math.max(0, Math.min(100, riskScore));
    }

    public SecurityRiskLevel getRiskLevel() { return riskLevel; }
    public int getRiskScore() { return riskScore; }
}