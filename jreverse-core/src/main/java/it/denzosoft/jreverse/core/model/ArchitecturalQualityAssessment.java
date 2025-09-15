package it.denzosoft.jreverse.core.model;

/**
 * Assessment of overall architectural quality based on violation analysis.
 *
 * @author JReverse Development Team
 * @since 1.1.0 (Phase 3)
 */
public class ArchitecturalQualityAssessment {
    private final double overallScore;
    private final String grade;

    public ArchitecturalQualityAssessment(double overallScore, String grade) {
        this.overallScore = Math.max(0.0, Math.min(100.0, overallScore));
        this.grade = grade != null ? grade : "F";
    }

    public double getOverallScore() {
        return overallScore;
    }

    public String getGrade() {
        return grade;
    }

    public boolean isHighQuality() {
        return overallScore >= 80.0;
    }

    public boolean isAcceptableQuality() {
        return overallScore >= 70.0;
    }

    @Override
    public String toString() {
        return "ArchitecturalQualityAssessment{" +
                "overallScore=" + String.format("%.1f", overallScore) +
                ", grade='" + grade + '\'' +
                '}';
    }
}