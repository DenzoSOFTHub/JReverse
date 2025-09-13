package it.denzosoft.jreverse.analyzer.security;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Container for security entrypoint analysis results.
 * Provides comprehensive analysis data and statistics about security patterns and vulnerabilities.
 */
public class SecurityAnalysisResult {
    
    private final List<SecurityEntrypointInfo> securityEntrypoints;
    private final Map<String, List<SecurityEntrypointInfo>> entrypointsByClass;
    private final Map<SecurityEntrypointType, List<SecurityEntrypointInfo>> entrypointsByType;
    private final List<SecurityVulnerability> allVulnerabilities;
    private final long analysisTimeMs;
    private final String jarFileName;
    private final SecurityQualityMetrics qualityMetrics;
    
    public SecurityAnalysisResult(List<SecurityEntrypointInfo> securityEntrypoints, 
                                 long analysisTimeMs, 
                                 String jarFileName) {
        this.securityEntrypoints = Collections.unmodifiableList(new ArrayList<>(securityEntrypoints != null ? securityEntrypoints : Collections.emptyList()));
        this.entrypointsByClass = groupEntrypointsByClass(this.securityEntrypoints);
        this.entrypointsByType = groupEntrypointsByType(this.securityEntrypoints);
        this.allVulnerabilities = extractAllVulnerabilities(this.securityEntrypoints);
        this.analysisTimeMs = analysisTimeMs;
        this.jarFileName = jarFileName != null ? jarFileName : "";
        this.qualityMetrics = calculateQualityMetrics();
    }
    
    // Core data accessors
    public List<SecurityEntrypointInfo> getSecurityEntrypoints() { return securityEntrypoints; }
    public Map<String, List<SecurityEntrypointInfo>> getEntrypointsByClass() { return entrypointsByClass; }
    public Map<SecurityEntrypointType, List<SecurityEntrypointInfo>> getEntrypointsByType() { return entrypointsByType; }
    public List<SecurityVulnerability> getAllVulnerabilities() { return allVulnerabilities; }
    public long getAnalysisTimeMs() { return analysisTimeMs; }
    public String getJarFileName() { return jarFileName; }
    public SecurityQualityMetrics getQualityMetrics() { return qualityMetrics; }
    
    // Basic statistics
    public int getTotalSecurityEntrypoints() { return securityEntrypoints.size(); }
    public int getUniqueClasses() { return entrypointsByClass.size(); }
    public int getUniqueSecurityTypes() { return entrypointsByType.size(); }
    public int getTotalVulnerabilities() { return allVulnerabilities.size(); }
    
    // Security type distribution
    public long getPreAuthorizeCount() {
        return getCountByType(SecurityEntrypointType.PRE_AUTHORIZE);
    }
    
    public long getPostAuthorizeCount() {
        return getCountByType(SecurityEntrypointType.POST_AUTHORIZE);
    }
    
    public long getSecuredCount() {
        return getCountByType(SecurityEntrypointType.SECURED);
    }
    
    public long getRolesAllowedCount() {
        return getCountByType(SecurityEntrypointType.ROLES_ALLOWED);
    }
    
    public long getPermitAllCount() {
        return getCountByType(SecurityEntrypointType.PERMIT_ALL);
    }
    
    public long getDenyAllCount() {
        return getCountByType(SecurityEntrypointType.DENY_ALL);
    }
    
    // Vulnerability analysis
    public long getCriticalVulnerabilities() {
        return getVulnerabilitiesBySeverity(SecurityVulnerability.Severity.CRITICAL);
    }
    
    public long getHighVulnerabilities() {
        return getVulnerabilitiesBySeverity(SecurityVulnerability.Severity.HIGH);
    }
    
    public long getMediumVulnerabilities() {
        return getVulnerabilitiesBySeverity(SecurityVulnerability.Severity.MEDIUM);
    }
    
    public long getLowVulnerabilities() {
        return getVulnerabilitiesBySeverity(SecurityVulnerability.Severity.LOW);
    }
    
    // SpEL analysis
    public long getSpELEntrypoints() {
        return securityEntrypoints.stream().filter(SecurityEntrypointInfo::hasSpELExpression).count();
    }
    
    public long getComplexSpELEntrypoints() {
        return securityEntrypoints.stream().filter(SecurityEntrypointInfo::hasComplexSpEL).count();
    }
    
    // Security level distribution
    public Map<SecurityEntrypointInfo.SecurityLevel, Long> getSecurityLevelDistribution() {
        return securityEntrypoints.stream()
            .collect(Collectors.groupingBy(
                SecurityEntrypointInfo::getSecurityLevel,
                Collectors.counting()
            ));
    }
    
    // Risk analysis
    public List<SecurityEntrypointInfo> getHighRiskEntrypoints() {
        return securityEntrypoints.stream()
            .filter(ep -> ep.hasHighSeverityVulnerabilities() || 
                         ep.getSecurityCoverageScore() < 50 ||
                         ep.isOverPermissive())
            .collect(Collectors.toList());
    }
    
    public List<SecurityEntrypointInfo> getOverPermissiveEntrypoints() {
        return securityEntrypoints.stream()
            .filter(SecurityEntrypointInfo::isOverPermissive)
            .collect(Collectors.toList());
    }
    
    // Class-level analysis
    public List<String> getClassesWithMostSecurityEntrypoints(int limit) {
        return entrypointsByClass.entrySet().stream()
            .sorted((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size()))
            .limit(limit)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
    
    public Map<String, Long> getSecurityCoverageByClass() {
        return entrypointsByClass.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> (long) entry.getValue().size()
            ));
    }
    
    // Quality metrics
    public double getOverallSecurityScore() {
        if (securityEntrypoints.isEmpty()) return 100.0;
        
        return securityEntrypoints.stream()
            .mapToInt(SecurityEntrypointInfo::getSecurityCoverageScore)
            .average()
            .orElse(0.0);
    }
    
    public double getVulnerabilityDensity() {
        if (securityEntrypoints.isEmpty()) return 0.0;
        return (double) getTotalVulnerabilities() / getTotalSecurityEntrypoints();
    }
    
    // Helper methods
    private long getCountByType(SecurityEntrypointType type) {
        return entrypointsByType.getOrDefault(type, Collections.emptyList()).size();
    }
    
    private long getVulnerabilitiesBySeverity(SecurityVulnerability.Severity severity) {
        return allVulnerabilities.stream()
            .filter(v -> v.getSeverity() == severity)
            .count();
    }
    
    private Map<String, List<SecurityEntrypointInfo>> groupEntrypointsByClass(List<SecurityEntrypointInfo> entrypoints) {
        return entrypoints.stream()
            .collect(Collectors.groupingBy(SecurityEntrypointInfo::getDeclaringClass));
    }
    
    private Map<SecurityEntrypointType, List<SecurityEntrypointInfo>> groupEntrypointsByType(List<SecurityEntrypointInfo> entrypoints) {
        return entrypoints.stream()
            .collect(Collectors.groupingBy(SecurityEntrypointInfo::getSecurityType));
    }
    
    private List<SecurityVulnerability> extractAllVulnerabilities(List<SecurityEntrypointInfo> entrypoints) {
        return entrypoints.stream()
            .flatMap(ep -> ep.getVulnerabilities().stream())
            .collect(Collectors.toList());
    }
    
    private SecurityQualityMetrics calculateQualityMetrics() {
        if (securityEntrypoints.isEmpty()) {
            return new SecurityQualityMetrics(0.0, 0.0, 0.0, 0.0, 100.0);
        }
        
        double spelCoverage = (double) getSpELEntrypoints() / getTotalSecurityEntrypoints() * 100;
        double jsr250Coverage = securityEntrypoints.stream()
            .filter(ep -> ep.getSecurityType().isJSR250())
            .count() * 100.0 / getTotalSecurityEntrypoints();
        double springSeCoverage = securityEntrypoints.stream()
            .filter(ep -> ep.getSecurityType().isSpringSecurityAnnotation())
            .count() * 100.0 / getTotalSecurityEntrypoints();
        double vulnerabilityRate = getVulnerabilityDensity() * 100;
        double overallScore = getOverallSecurityScore();
        
        return new SecurityQualityMetrics(spelCoverage, jsr250Coverage, springSeCoverage, vulnerabilityRate, overallScore);
    }
    
    /**
     * Quality metrics for security analysis.
     */
    public static class SecurityQualityMetrics {
        private final double spelCoverage;
        private final double jsr250Coverage;
        private final double springSecurityCoverage;
        private final double vulnerabilityRate;
        private final double overallScore;
        
        public SecurityQualityMetrics(double spelCoverage, double jsr250Coverage, 
                                    double springSecurityCoverage, double vulnerabilityRate, 
                                    double overallScore) {
            this.spelCoverage = spelCoverage;
            this.jsr250Coverage = jsr250Coverage;
            this.springSecurityCoverage = springSecurityCoverage;
            this.vulnerabilityRate = vulnerabilityRate;
            this.overallScore = overallScore;
        }
        
        public double getSpelCoverage() { return spelCoverage; }
        public double getJsr250Coverage() { return jsr250Coverage; }
        public double getSpringSecurityCoverage() { return springSecurityCoverage; }
        public double getVulnerabilityRate() { return vulnerabilityRate; }
        public double getOverallScore() { return overallScore; }
        
        public String getSecurityGrade() {
            if (overallScore >= 90) return "A";
            if (overallScore >= 80) return "B";
            if (overallScore >= 70) return "C";
            if (overallScore >= 60) return "D";
            return "F";
        }
    }
    
    @Override
    public String toString() {
        return "SecurityAnalysisResult{" +
                "totalEntrypoints=" + getTotalSecurityEntrypoints() +
                ", uniqueClasses=" + getUniqueClasses() +
                ", vulnerabilities=" + getTotalVulnerabilities() +
                ", overallScore=" + String.format("%.1f", getOverallSecurityScore()) +
                ", grade=" + qualityMetrics.getSecurityGrade() +
                ", analysisTime=" + analysisTimeMs + "ms" +
                '}';
    }
}