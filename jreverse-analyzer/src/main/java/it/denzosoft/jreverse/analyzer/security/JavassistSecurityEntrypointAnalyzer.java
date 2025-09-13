package it.denzosoft.jreverse.analyzer.security;

import it.denzosoft.jreverse.core.model.AnnotationInfo;
import it.denzosoft.jreverse.core.model.ClassInfo;
import it.denzosoft.jreverse.core.model.JarContent;
import it.denzosoft.jreverse.core.model.MethodInfo;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Javassist-based implementation of SecurityEntrypointAnalyzer.
 * Analyzes security annotations and identifies potential vulnerabilities.
 */
public class JavassistSecurityEntrypointAnalyzer implements SecurityEntrypointAnalyzer {
    
    private static final Set<String> SECURITY_ANNOTATIONS = new HashSet<>();
    private static final Map<String, SecurityEntrypointType> ANNOTATION_TYPE_MAP = new HashMap<>();
    
    static {
        SECURITY_ANNOTATIONS.add("org.springframework.security.access.prepost.PreAuthorize");
        SECURITY_ANNOTATIONS.add("org.springframework.security.access.prepost.PostAuthorize");
        SECURITY_ANNOTATIONS.add("org.springframework.security.access.annotation.Secured");
        SECURITY_ANNOTATIONS.add("javax.annotation.security.RolesAllowed");
        SECURITY_ANNOTATIONS.add("javax.annotation.security.DenyAll");
        SECURITY_ANNOTATIONS.add("javax.annotation.security.PermitAll");
        SECURITY_ANNOTATIONS.add("javax.annotation.security.RunAs");
        
        ANNOTATION_TYPE_MAP.put("org.springframework.security.access.prepost.PreAuthorize", SecurityEntrypointType.PRE_AUTHORIZE);
        ANNOTATION_TYPE_MAP.put("org.springframework.security.access.prepost.PostAuthorize", SecurityEntrypointType.POST_AUTHORIZE);
        ANNOTATION_TYPE_MAP.put("org.springframework.security.access.annotation.Secured", SecurityEntrypointType.SECURED);
        ANNOTATION_TYPE_MAP.put("javax.annotation.security.RolesAllowed", SecurityEntrypointType.ROLES_ALLOWED);
        ANNOTATION_TYPE_MAP.put("javax.annotation.security.DenyAll", SecurityEntrypointType.DENY_ALL);
        ANNOTATION_TYPE_MAP.put("javax.annotation.security.PermitAll", SecurityEntrypointType.PERMIT_ALL);
        ANNOTATION_TYPE_MAP.put("javax.annotation.security.RunAs", SecurityEntrypointType.RUN_AS);
    }
    
    @Override
    public SecurityAnalysisResult analyze(JarContent jarContent) {
        long startTime = System.currentTimeMillis();
        List<SecurityEntrypointInfo> allEntrypoints = new ArrayList<>();
        
        for (ClassInfo classInfo : jarContent.getClasses()) {
            if (hasSecurityAnnotations(classInfo)) {
                SecurityAnalysisResult classResult = analyzeClass(classInfo);
                allEntrypoints.addAll(classResult.getSecurityEntrypoints());
            }
        }
        
        long analysisTime = System.currentTimeMillis() - startTime;
        String fileName = jarContent.getLocation() != null ? jarContent.getLocation().getFileName() : "";
        return new SecurityAnalysisResult(allEntrypoints, analysisTime, fileName);
    }
    
    @Override
    public SecurityAnalysisResult analyzeClass(ClassInfo classInfo) {
        List<SecurityEntrypointInfo> entrypoints = new ArrayList<>();
        
        // Analyze class-level security annotations
        List<AnnotationInfo> classAnnotations = getSecurityAnnotations(new ArrayList<>(classInfo.getAnnotations()));
        for (AnnotationInfo annotation : classAnnotations) {
            SecurityEntrypointInfo entrypoint = analyzeSecurityAnnotation(
                annotation, classInfo.getFullyQualifiedName(), "<class-level>", true, false
            );
            if (entrypoint != null) {
                entrypoints.add(entrypoint);
            }
        }
        
        // Analyze method-level security annotations
        for (MethodInfo method : classInfo.getMethods()) {
            List<AnnotationInfo> methodAnnotations = getSecurityAnnotations(new ArrayList<>(method.getAnnotations()));
            for (AnnotationInfo annotation : methodAnnotations) {
                SecurityEntrypointInfo entrypoint = analyzeSecurityAnnotation(
                    annotation, classInfo.getFullyQualifiedName(), method.getName(), false, true
                );
                if (entrypoint != null) {
                    entrypoints.add(entrypoint);
                }
            }
        }
        
        return new SecurityAnalysisResult(entrypoints, 0L, "");
    }
    
    private boolean hasSecurityAnnotations(ClassInfo classInfo) {
        // Check class-level annotations
        if (classInfo.getAnnotations().stream().anyMatch(this::isSecurityAnnotation)) {
            return true;
        }
        
        // Check method-level annotations
        return classInfo.getMethods().stream()
            .anyMatch(method -> method.getAnnotations().stream().anyMatch(this::isSecurityAnnotation));
    }
    
    private boolean isSecurityAnnotation(AnnotationInfo annotation) {
        return SECURITY_ANNOTATIONS.contains(annotation.getType());
    }
    
    private List<AnnotationInfo> getSecurityAnnotations(List<AnnotationInfo> annotations) {
        return annotations.stream()
            .filter(this::isSecurityAnnotation)
            .collect(Collectors.toList());
    }
    
    private SecurityEntrypointInfo analyzeSecurityAnnotation(AnnotationInfo annotation, 
                                                           String className, 
                                                           String methodName, 
                                                           boolean isClassLevel, 
                                                           boolean isMethodLevel) {
        
        SecurityEntrypointType type = ANNOTATION_TYPE_MAP.get(annotation.getType());
        if (type == null) {
            return null;
        }
        
        SecurityEntrypointInfo.Builder builder = SecurityEntrypointInfo.builder()
            .methodName(methodName)
            .declaringClass(className)
            .securityType(type)
            .sourceAnnotation(annotation)
            .isClassLevel(isClassLevel)
            .isMethodLevel(isMethodLevel);
        
        // Extract security expression and analyze
        String expression = extractSecurityExpression(annotation);
        if (expression != null && !expression.trim().isEmpty()) {
            builder.securityExpression(expression)
                   .hasSpELExpression(isSpELExpression(expression))
                   .spellAnalysis(analyzeSpELExpression(expression));
        }
        
        // Extract roles and authorities
        Set<String> roles = extractRoles(annotation, type);
        Set<String> authorities = extractAuthorities(annotation, type);
        builder.requiredRoles(roles)
               .requiredAuthorities(authorities);
        
        // Determine security level
        SecurityEntrypointInfo.SecurityLevel level = determineSecurityLevel(type, expression, roles, authorities);
        builder.securityLevel(level);
        
        // Analyze vulnerabilities
        Set<SecurityVulnerability> vulnerabilities = analyzeVulnerabilities(type, expression, roles, authorities, className, methodName);
        builder.vulnerabilities(vulnerabilities);
        
        return builder.build();
    }
    
    private String extractSecurityExpression(AnnotationInfo annotation) {
        if (annotation.getAttributes() == null || annotation.getAttributes().isEmpty()) {
            return null;
        }
        
        // For @PreAuthorize and @PostAuthorize, the expression is typically in "value" parameter
        Object value = annotation.getAttributes().get("value");
        if (value instanceof String) {
            return (String) value;
        }
        
        return null;
    }
    
    private boolean isSpELExpression(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            return false;
        }
        
        // Simple heuristics to detect SpEL expressions
        return expression.contains("#") || 
               expression.contains("hasRole") || 
               expression.contains("hasAuthority") || 
               expression.contains("authentication") ||
               expression.contains("principal");
    }
    
    private String analyzeSpELExpression(String expression) {
        if (!isSpELExpression(expression)) {
            return "Not a SpEL expression";
        }
        
        // Basic SpEL analysis
        List<String> analysis = new ArrayList<>();
        
        if (expression.contains("#")) {
            analysis.add("Uses SpEL variables");
        }
        if (expression.contains("hasRole")) {
            analysis.add("Role-based authorization");
        }
        if (expression.contains("hasAuthority")) {
            analysis.add("Authority-based authorization");
        }
        if (expression.contains("authentication")) {
            analysis.add("Authentication context access");
        }
        if (expression.contains("principal")) {
            analysis.add("Principal access");
        }
        if (expression.contains("and") || expression.contains("or")) {
            analysis.add("Complex logical expressions");
        }
        
        return analysis.isEmpty() ? "Basic SpEL expression" : String.join(", ", analysis);
    }
    
    private Set<String> extractRoles(AnnotationInfo annotation, SecurityEntrypointType type) {
        Set<String> roles = new HashSet<>();
        
        if (type == SecurityEntrypointType.ROLES_ALLOWED || type == SecurityEntrypointType.SECURED) {
            Object value = annotation.getAttributes().get("value");
            if (value instanceof String[]) {
                Collections.addAll(roles, (String[]) value);
            } else if (value instanceof String) {
                roles.add((String) value);
            }
        }
        
        return roles;
    }
    
    private Set<String> extractAuthorities(AnnotationInfo annotation, SecurityEntrypointType type) {
        Set<String> authorities = new HashSet<>();
        
        // For now, treat roles as authorities for SECURED annotation
        if (type == SecurityEntrypointType.SECURED) {
            authorities.addAll(extractRoles(annotation, type));
        }
        
        return authorities;
    }
    
    private SecurityEntrypointInfo.SecurityLevel determineSecurityLevel(SecurityEntrypointType type, 
                                                                       String expression, 
                                                                       Set<String> roles, 
                                                                       Set<String> authorities) {
        
        if (type == SecurityEntrypointType.DENY_ALL) {
            return SecurityEntrypointInfo.SecurityLevel.CRITICAL;
        }
        
        if (type == SecurityEntrypointType.PERMIT_ALL) {
            return SecurityEntrypointInfo.SecurityLevel.LOW;
        }
        
        if (type.supportsSpEL() && expression != null && !expression.trim().isEmpty()) {
            if (expression.length() > 100 || expression.contains("and") || expression.contains("or")) {
                return SecurityEntrypointInfo.SecurityLevel.HIGH;
            }
            return SecurityEntrypointInfo.SecurityLevel.MEDIUM;
        }
        
        if (!roles.isEmpty() || !authorities.isEmpty()) {
            return SecurityEntrypointInfo.SecurityLevel.MEDIUM;
        }
        
        return SecurityEntrypointInfo.SecurityLevel.LOW;
    }
    
    private Set<SecurityVulnerability> analyzeVulnerabilities(SecurityEntrypointType type, 
                                                             String expression, 
                                                             Set<String> roles, 
                                                             Set<String> authorities, 
                                                             String className, 
                                                             String methodName) {
        
        Set<SecurityVulnerability> vulnerabilities = new HashSet<>();
        String location = className + "." + methodName;
        
        // Check for over-permissive access
        if (type.isPermissive()) {
            vulnerabilities.add(new SecurityVulnerability(
                SecurityVulnerability.VulnerabilityType.WEAK_AUTHORIZATION,
                SecurityVulnerability.Severity.HIGH,
                "Method allows unrestricted access with @PermitAll",
                location,
                "Review if unrestricted access is necessary, consider adding role-based restrictions"
            ));
        }
        
        // Check for missing authorization
        if (roles.isEmpty() && authorities.isEmpty() && (expression == null || expression.trim().isEmpty()) 
            && !type.isRestrictive() && !type.isPermissive()) {
            vulnerabilities.add(new SecurityVulnerability(
                SecurityVulnerability.VulnerabilityType.MISSING_AUTHORIZATION,
                SecurityVulnerability.Severity.MEDIUM,
                "Security annotation present but no specific authorization criteria defined",
                location,
                "Define specific roles, authorities, or SpEL expressions for authorization"
            ));
        }
        
        // Check for complex SpEL expressions
        if (expression != null && expression.length() > 150) {
            vulnerabilities.add(new SecurityVulnerability(
                SecurityVulnerability.VulnerabilityType.COMPLEX_SPELL,
                SecurityVulnerability.Severity.MEDIUM,
                "SpEL expression is very complex and may be difficult to maintain",
                location,
                "Consider breaking down complex expressions into simpler, more maintainable parts"
            ));
        }
        
        // Check for potential SpEL injection
        if (expression != null && (expression.contains("#request") || expression.contains("#param"))) {
            vulnerabilities.add(new SecurityVulnerability(
                SecurityVulnerability.VulnerabilityType.SPELL_INJECTION,
                SecurityVulnerability.Severity.HIGH,
                "SpEL expression may be vulnerable to injection attacks through request parameters",
                location,
                "Avoid using user-controllable input directly in SpEL expressions, validate and sanitize inputs"
            ));
        }
        
        // Check for hardcoded roles
        if (roles.size() > 3) {
            vulnerabilities.add(new SecurityVulnerability(
                SecurityVulnerability.VulnerabilityType.HARDCODED_ROLES,
                SecurityVulnerability.Severity.LOW,
                "Large number of hardcoded roles may indicate inflexible authorization design",
                location,
                "Consider using role hierarchies or external configuration for role management"
            ));
        }
        
        return vulnerabilities;
    }
}