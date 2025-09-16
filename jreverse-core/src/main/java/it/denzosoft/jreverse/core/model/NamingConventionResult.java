package it.denzosoft.jreverse.core.model;

import java.util.*;

/**
 * Result of naming convention analysis for packages.
 */
public final class NamingConventionResult {
    
    private final List<NamingViolation> violations;
    private final NamingConventionSummary summary;
    private final Map<String, Integer> violationsByType;
    private final double conformanceScore; // 0.0 - 1.0
    
    private NamingConventionResult(Builder builder) {
        this.violations = Collections.unmodifiableList(new ArrayList<>(builder.violations));
        this.summary = builder.summary;
        this.violationsByType = Collections.unmodifiableMap(new HashMap<>(builder.violationsByType));
        this.conformanceScore = Math.min(1.0, Math.max(0.0, builder.conformanceScore));
    }
    
    public List<NamingViolation> getViolations() {
        return violations;
    }
    
    public NamingConventionSummary getSummary() {
        return summary;
    }
    
    public Map<String, Integer> getViolationsByType() {
        return violationsByType;
    }
    
    public double getConformanceScore() {
        return conformanceScore;
    }
    
    public boolean hasViolations() {
        return !violations.isEmpty();
    }
    
    public int getViolationCount() {
        return violations.size();
    }
    
    public List<NamingViolation> getViolationsByPackage(String packageName) {
        return violations.stream()
                .filter(v -> packageName.equals(v.getPackageName()))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    public List<NamingViolation> getViolationsBySeverity(ViolationSeverity severity) {
        return violations.stream()
                .filter(v -> severity.equals(v.getSeverity()))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    public boolean isGoodConformance() {
        return conformanceScore >= 0.8;
    }
    
    public boolean isFairConformance() {
        return conformanceScore >= 0.6 && conformanceScore < 0.8;
    }
    
    public boolean isPoorConformance() {
        return conformanceScore < 0.6;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        NamingConventionResult that = (NamingConventionResult) obj;
        return Double.compare(that.conformanceScore, conformanceScore) == 0 &&
               Objects.equals(violations, that.violations) &&
               Objects.equals(summary, that.summary) &&
               Objects.equals(violationsByType, that.violationsByType);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(violations, summary, violationsByType, conformanceScore);
    }
    
    @Override
    public String toString() {
        return "NamingConventionResult{" +
                "violationCount=" + violations.size() +
                ", conformanceScore=" + String.format("%.2f", conformanceScore) +
                ", hasViolations=" + hasViolations() +
                '}';
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static NamingConventionResult clean(double conformanceScore) {
        return builder()
                .conformanceScore(conformanceScore)
                .summary(NamingConventionSummary.clean())
                .build();
    }
    
    public static class Builder {
        private List<NamingViolation> violations = new ArrayList<>();
        private NamingConventionSummary summary;
        private Map<String, Integer> violationsByType = new HashMap<>();
        private double conformanceScore = 1.0;
        
        public Builder addViolation(NamingViolation violation) {
            if (violation != null) {
                violations.add(violation);
                String type = violation.getViolationType().toString();
                violationsByType.merge(type, 1, Integer::sum);
            }
            return this;
        }
        
        public Builder violations(List<NamingViolation> violations) {
            this.violations = new ArrayList<>(violations != null ? violations : Collections.emptyList());
            recalculateViolationsByType();
            return this;
        }
        
        public Builder summary(NamingConventionSummary summary) {
            this.summary = summary;
            return this;
        }
        
        public Builder conformanceScore(double conformanceScore) {
            this.conformanceScore = conformanceScore;
            return this;
        }
        
        private void recalculateViolationsByType() {
            violationsByType.clear();
            for (NamingViolation violation : violations) {
                String type = violation.getViolationType().toString();
                violationsByType.merge(type, 1, Integer::sum);
            }
        }
        
        public NamingConventionResult build() {
            if (summary == null) {
                summary = NamingConventionSummary.fromViolations(violations);
            }
            return new NamingConventionResult(this);
        }
    }
}