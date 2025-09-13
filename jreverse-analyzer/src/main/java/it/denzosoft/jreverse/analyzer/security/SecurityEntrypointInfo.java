package it.denzosoft.jreverse.analyzer.security;

import it.denzosoft.jreverse.core.model.AnnotationInfo;

import java.util.*;

/**
 * Immutable value object representing detailed information about a security-protected entrypoint.
 * Contains comprehensive metadata about security annotations, roles, and access control
 * extracted from Spring Security and JSR-250 annotations.
 */
public class SecurityEntrypointInfo {
    
    private final String methodName;
    private final String declaringClass;
    private final SecurityEntrypointType securityType;
    private final String securityExpression;
    private final Set<String> requiredRoles;
    private final Set<String> requiredAuthorities;
    private final SecurityLevel securityLevel;
    private final boolean hasSpELExpression;
    private final String spellAnalysis;
    private final Set<SecurityVulnerability> vulnerabilities;
    private final AnnotationInfo sourceAnnotation;
    private final boolean isClassLevel;
    private final boolean isMethodLevel;
    private final String description;
    
    private SecurityEntrypointInfo(Builder builder) {
        this.methodName = Objects.requireNonNull(builder.methodName, "methodName cannot be null");
        this.declaringClass = Objects.requireNonNull(builder.declaringClass, "declaringClass cannot be null");
        this.securityType = Objects.requireNonNull(builder.securityType, "securityType cannot be null");
        this.securityExpression = builder.securityExpression;
        this.requiredRoles = Collections.unmodifiableSet(new LinkedHashSet<>(builder.requiredRoles));
        this.requiredAuthorities = Collections.unmodifiableSet(new LinkedHashSet<>(builder.requiredAuthorities));
        this.securityLevel = builder.securityLevel != null ? builder.securityLevel : SecurityLevel.MEDIUM;
        this.hasSpELExpression = builder.hasSpELExpression;
        this.spellAnalysis = builder.spellAnalysis;
        this.vulnerabilities = Collections.unmodifiableSet(new LinkedHashSet<>(builder.vulnerabilities));
        this.sourceAnnotation = builder.sourceAnnotation;
        this.isClassLevel = builder.isClassLevel;
        this.isMethodLevel = builder.isMethodLevel;
        this.description = builder.description;
    }
    
    // Getters
    public String getMethodName() { return methodName; }
    public String getDeclaringClass() { return declaringClass; }
    public SecurityEntrypointType getSecurityType() { return securityType; }
    public String getSecurityExpression() { return securityExpression; }
    public Set<String> getRequiredRoles() { return requiredRoles; }
    public Set<String> getRequiredAuthorities() { return requiredAuthorities; }
    public SecurityLevel getSecurityLevel() { return securityLevel; }
    public boolean hasSpELExpression() { return hasSpELExpression; }
    public String getSpellAnalysis() { return spellAnalysis; }
    public Set<SecurityVulnerability> getVulnerabilities() { return vulnerabilities; }
    public AnnotationInfo getSourceAnnotation() { return sourceAnnotation; }
    public boolean isClassLevel() { return isClassLevel; }
    public boolean isMethodLevel() { return isMethodLevel; }
    public String getDescription() { return description; }
    
    /**
     * Gets the fully qualified method identifier.
     */
    public String getMethodIdentifier() {
        return declaringClass + "." + methodName;
    }
    
    /**
     * Checks if this entrypoint has any security vulnerabilities.
     */
    public boolean hasVulnerabilities() {
        return !vulnerabilities.isEmpty();
    }
    
    /**
     * Checks if this entrypoint has high-severity vulnerabilities.
     */
    public boolean hasHighSeverityVulnerabilities() {
        return vulnerabilities.stream().anyMatch(v -> v.getSeverity() == SecurityVulnerability.Severity.HIGH);
    }
    
    /**
     * Checks if this entrypoint uses complex SpEL expressions.
     */
    public boolean hasComplexSpEL() {
        return hasSpELExpression && securityExpression != null && 
               (securityExpression.contains("and") || securityExpression.contains("or") || 
                securityExpression.contains("?") || securityExpression.length() > 50);
    }
    
    /**
     * Checks if this entrypoint is potentially over-permissive.
     */
    public boolean isOverPermissive() {
        return securityType.isPermissive() || 
               (!securityType.isRestrictive() && requiredRoles.isEmpty() && requiredAuthorities.isEmpty() && !hasSpELExpression);
    }
    
    /**
     * Gets the security coverage score (0-100).
     */
    public int getSecurityCoverageScore() {
        int score = 0;
        
        // Base points for having security
        score += 30;
        
        // Points for specific roles/authorities
        if (!requiredRoles.isEmpty()) score += 20;
        if (!requiredAuthorities.isEmpty()) score += 20;
        
        // Points for SpEL expressions (can be good or bad)
        if (hasSpELExpression && !hasComplexSpEL()) score += 15;
        
        // Points for security level
        switch (securityLevel) {
            case HIGH: score += 15; break;
            case MEDIUM: score += 10; break;
            case LOW: score += 5; break;
        }
        
        // Deduct points for vulnerabilities
        score -= vulnerabilities.size() * 10;
        
        // Ensure score is between 0 and 100
        return Math.max(0, Math.min(100, score));
    }
    
    /**
     * Security level enumeration.
     */
    public enum SecurityLevel {
        LOW("Low Security", "Basic access control"),
        MEDIUM("Medium Security", "Standard role-based access control"),
        HIGH("High Security", "Complex authorization with SpEL"),
        CRITICAL("Critical Security", "Mission-critical access control");
        
        private final String displayName;
        private final String description;
        
        SecurityLevel(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SecurityEntrypointInfo that = (SecurityEntrypointInfo) obj;
        return Objects.equals(methodName, that.methodName) &&
               Objects.equals(declaringClass, that.declaringClass) &&
               Objects.equals(securityType, that.securityType);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(methodName, declaringClass, securityType);
    }
    
    @Override
    public String toString() {
        return "SecurityEntrypointInfo{" +
                "method='" + getMethodIdentifier() + '\'' +
                ", type=" + securityType +
                ", level=" + securityLevel +
                ", vulnerabilities=" + vulnerabilities.size() +
                ", score=" + getSecurityCoverageScore() +
                '}';
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String methodName;
        private String declaringClass;
        private SecurityEntrypointType securityType;
        private String securityExpression;
        private Set<String> requiredRoles = new LinkedHashSet<>();
        private Set<String> requiredAuthorities = new LinkedHashSet<>();
        private SecurityLevel securityLevel;
        private boolean hasSpELExpression = false;
        private String spellAnalysis;
        private Set<SecurityVulnerability> vulnerabilities = new LinkedHashSet<>();
        private AnnotationInfo sourceAnnotation;
        private boolean isClassLevel = false;
        private boolean isMethodLevel = true;
        private String description;
        
        public Builder methodName(String methodName) {
            this.methodName = methodName;
            return this;
        }
        
        public Builder declaringClass(String declaringClass) {
            this.declaringClass = declaringClass;
            return this;
        }
        
        public Builder securityType(SecurityEntrypointType securityType) {
            this.securityType = securityType;
            return this;
        }
        
        public Builder securityExpression(String securityExpression) {
            this.securityExpression = securityExpression;
            return this;
        }
        
        public Builder addRequiredRole(String role) {
            if (role != null && !role.trim().isEmpty()) {
                this.requiredRoles.add(role.trim());
            }
            return this;
        }
        
        public Builder requiredRoles(Set<String> roles) {
            this.requiredRoles = new LinkedHashSet<>(roles != null ? roles : Collections.emptySet());
            return this;
        }
        
        public Builder addRequiredAuthority(String authority) {
            if (authority != null && !authority.trim().isEmpty()) {
                this.requiredAuthorities.add(authority.trim());
            }
            return this;
        }
        
        public Builder requiredAuthorities(Set<String> authorities) {
            this.requiredAuthorities = new LinkedHashSet<>(authorities != null ? authorities : Collections.emptySet());
            return this;
        }
        
        public Builder securityLevel(SecurityLevel securityLevel) {
            this.securityLevel = securityLevel;
            return this;
        }
        
        public Builder hasSpELExpression(boolean hasSpELExpression) {
            this.hasSpELExpression = hasSpELExpression;
            return this;
        }
        
        public Builder spellAnalysis(String spellAnalysis) {
            this.spellAnalysis = spellAnalysis;
            return this;
        }
        
        public Builder addVulnerability(SecurityVulnerability vulnerability) {
            if (vulnerability != null) {
                this.vulnerabilities.add(vulnerability);
            }
            return this;
        }
        
        public Builder vulnerabilities(Set<SecurityVulnerability> vulnerabilities) {
            this.vulnerabilities = new LinkedHashSet<>(vulnerabilities != null ? vulnerabilities : Collections.emptySet());
            return this;
        }
        
        public Builder sourceAnnotation(AnnotationInfo sourceAnnotation) {
            this.sourceAnnotation = sourceAnnotation;
            return this;
        }
        
        public Builder isClassLevel(boolean isClassLevel) {
            this.isClassLevel = isClassLevel;
            return this;
        }
        
        public Builder isMethodLevel(boolean isMethodLevel) {
            this.isMethodLevel = isMethodLevel;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public SecurityEntrypointInfo build() {
            return new SecurityEntrypointInfo(this);
        }
    }
}