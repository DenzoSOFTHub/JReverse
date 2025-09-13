# Requisiti Tecnici: SecurityEntrypointAnalyzer (Completamento)

## Overview
Analyzer specializzato per l'identificazione completa e l'analisi di entry point protetti da annotazioni di sicurezza (@PreAuthorize, @PostAuthorize, @Secured, @RolesAllowed) che rappresentano punti di controllo per l'accesso alle funzionalità applicative.

## Valore Business
**⭐⭐⭐⭐⭐ - Valore Massimo**
- Mappatura completa della superficie di attacco applicativa
- Identificazione di gap e vulnerabilità nella configurazione di sicurezza
- Analisi della matrice di autorizzazione e controlli di accesso
- Audit trail per compliance e security assessment

## Complessità di Implementazione
**🔴 Complessa** - Gestione di multiple tecnologie di security e pattern complessi di autorizzazione

## Tempo di Realizzazione Stimato
**8-10 giorni** di sviluppo

## Estensione della Copertura Attuale

### Situazione Attuale Analizzata
Il sistema attualmente gestisce solo @PreAuthorize in forma limitata. Necessaria estensione completa per:
- @PostAuthorize con analisi del return value filtering
- @Secured con supporto per role hierarchy
- @RolesAllowed (JSR-250) con inheritance analysis
- @DenyAll e @PermitAll per completezza
- Custom security annotations

## Struttura delle Classi

### Core Interface Estesa
```java
package it.denzosoft.jreverse.analyzer.security;

public interface SecurityEntrypointAnalyzer {
    boolean canAnalyze(JarContent jarContent);
    SecurityEntrypointAnalysisResult analyze(JarContent jarContent);
    List<SecurityEntrypointInfo> findSecurityEntrypoints(CtClass[] classes);
    SecurityMatrixAnalysis buildSecurityMatrix(List<SecurityEntrypointInfo> entrypoints);
    VulnerabilityAnalysis analyzeSecurityVulnerabilities(List<SecurityEntrypointInfo> entrypoints);
}
```

### Implementazione Javassist Completa
```java
package it.denzosoft.jreverse.analyzer.security;

public class JavassistSecurityEntrypointAnalyzer implements SecurityEntrypointAnalyzer {
    
    private static final Map<String, SecurityAnnotationType> SECURITY_ANNOTATIONS = Map.of(
        "org.springframework.security.access.prepost.PreAuthorize", SecurityAnnotationType.PRE_AUTHORIZE,
        "org.springframework.security.access.prepost.PostAuthorize", SecurityAnnotationType.POST_AUTHORIZE,
        "org.springframework.security.access.annotation.Secured", SecurityAnnotationType.SECURED,
        "javax.annotation.security.RolesAllowed", SecurityAnnotationType.ROLES_ALLOWED,
        "javax.annotation.security.DenyAll", SecurityAnnotationType.DENY_ALL,
        "javax.annotation.security.PermitAll", SecurityAnnotationType.PERMIT_ALL,
        "org.springframework.security.access.prepost.PreFilter", SecurityAnnotationType.PRE_FILTER,
        "org.springframework.security.access.prepost.PostFilter", SecurityAnnotationType.POST_FILTER
    );
    
    @Override
    public SecurityEntrypointAnalysisResult analyze(JarContent jarContent) {
        SecurityEntrypointAnalysisResult.Builder resultBuilder = SecurityEntrypointAnalysisResult.builder();
        
        try {
            CtClass[] classes = jarContent.getAllClasses();
            
            // Analizza configurazione Spring Security
            SpringSecurityConfigAnalysis configAnalysis = analyzeSpringSecurityConfig(classes);
            resultBuilder.configurationAnalysis(configAnalysis);
            
            // Trova tutti i security entrypoints
            List<SecurityEntrypointInfo> entrypoints = findSecurityEntrypoints(classes);
            resultBuilder.securityEntrypoints(entrypoints);
            
            // Costruisce matrice di sicurezza completa
            SecurityMatrixAnalysis matrixAnalysis = buildSecurityMatrix(entrypoints);
            resultBuilder.matrixAnalysis(matrixAnalysis);
            
            // Analizza vulnerabilità di sicurezza
            VulnerabilityAnalysis vulnerabilityAnalysis = analyzeSecurityVulnerabilities(entrypoints);
            resultBuilder.vulnerabilityAnalysis(vulnerabilityAnalysis);
            
            // Analizza role hierarchy e permission inheritance
            RoleHierarchyAnalysis roleAnalysis = analyzeRoleHierarchy(classes, entrypoints);
            resultBuilder.roleHierarchyAnalysis(roleAnalysis);
            
            // Identifica pattern di sicurezza applicati
            List<SecurityPattern> securityPatterns = identifySecurityPatterns(entrypoints, classes);
            resultBuilder.securityPatterns(securityPatterns);
            
            // Calcola security coverage metrics
            SecurityCoverageMetrics coverageMetrics = calculateSecurityCoverage(entrypoints, classes);
            resultBuilder.coverageMetrics(coverageMetrics);
            
            // Analizza integration con altri entrypoints
            SecurityIntegrationAnalysis integrationAnalysis = analyzeSecurityIntegration(entrypoints, classes);
            resultBuilder.integrationAnalysis(integrationAnalysis);
            
        } catch (Exception e) {
            resultBuilder.addError("Error analyzing security entrypoints: " + e.getMessage());
        }
        
        return resultBuilder.build();
    }
    
    @Override
    public List<SecurityEntrypointInfo> findSecurityEntrypoints(CtClass[] classes) {
        List<SecurityEntrypointInfo> entrypoints = new ArrayList<>();
        
        for (CtClass ctClass : classes) {
            try {
                // Analizza security a livello di classe
                ClassSecurityContext classSecurityContext = analyzeClassSecurityContext(ctClass);
                
                CtMethod[] methods = ctClass.getDeclaredMethods();
                
                for (CtMethod method : methods) {
                    SecurityEntrypointInfo entrypoint = analyzeMethodSecurityEntrypoint(
                        method, ctClass, classSecurityContext
                    );
                    
                    if (entrypoint.hasSecurityConstraints()) {
                        entrypoints.add(entrypoint);
                    }
                }
                
                // Analizza security inheritance patterns
                List<SecurityEntrypointInfo> inheritedEntrypoints = analyzeSecurityInheritance(ctClass, classSecurityContext);
                entrypoints.addAll(inheritedEntrypoints);
                
            } catch (Exception e) {
                // Log error but continue processing
            }
        }
        
        return entrypoints;
    }
    
    private SecurityEntrypointInfo analyzeMethodSecurityEntrypoint(
            CtMethod method, CtClass ctClass, ClassSecurityContext classContext) {
        
        SecurityEntrypointInfo.Builder builder = SecurityEntrypointInfo.builder()
            .className(ctClass.getName())
            .methodName(method.getName())
            .methodSignature(method.getSignature())
            .classSecurityContext(classContext);
        
        try {
            // Analizza tutte le security annotations presenti
            Map<SecurityAnnotationType, SecurityConstraint> constraints = new HashMap<>();
            
            for (Map.Entry<String, SecurityAnnotationType> entry : SECURITY_ANNOTATIONS.entrySet()) {
                String annotationName = entry.getKey();
                SecurityAnnotationType annotationType = entry.getValue();
                
                if (method.hasAnnotation(annotationName)) {
                    Annotation annotation = method.getAnnotation(annotationName);
                    SecurityConstraint constraint = parseSecurityConstraint(annotation, annotationType);
                    constraints.put(annotationType, constraint);
                }
            }
            
            builder.securityConstraints(constraints);
            
            // Analizza security context del metodo
            MethodSecurityContext methodContext = analyzeMethodSecurityContext(method, constraints);
            builder.methodSecurityContext(methodContext);
            
            // Determina effective security policy
            EffectiveSecurityPolicy effectivePolicy = determineEffectiveSecurityPolicy(
                classContext, methodContext, constraints
            );
            builder.effectiveSecurityPolicy(effectivePolicy);
            
            // Analizza parametri per security implications
            SecurityParameterAnalysis parameterAnalysis = analyzeSecurityParameters(method, constraints);
            builder.parameterAnalysis(parameterAnalysis);
            
            // Analizza return value security filtering
            ReturnValueSecurityAnalysis returnAnalysis = analyzeReturnValueSecurity(method, constraints);
            builder.returnValueAnalysis(returnAnalysis);
            
            // Verifica expression security (SpEL)
            SpELSecurityAnalysis spelAnalysis = analyzeSpELSecurity(constraints);
            builder.spelAnalysis(spelAnalysis);
            
        } catch (Exception e) {
            builder.addError("Error analyzing method security: " + e.getMessage());
        }
        
        return builder.build();
    }
}
```

## Modelli di Dati Estesi

### SecurityEntrypointInfo
```java
package it.denzosoft.jreverse.analyzer.security;

public class SecurityEntrypointInfo {
    
    // Identificazione
    private final String className;
    private final String methodName;
    private final String methodSignature;
    private final boolean isEntrypoint;
    
    // Security contexts
    private final ClassSecurityContext classSecurityContext;
    private final MethodSecurityContext methodSecurityContext;
    private final EffectiveSecurityPolicy effectiveSecurityPolicy;
    
    // Security constraints per tipo
    private final Map<SecurityAnnotationType, SecurityConstraint> securityConstraints;
    private final SecurityConstraintComplexity complexityLevel;
    
    // Analisi parametri e return value
    private final SecurityParameterAnalysis parameterAnalysis;
    private final ReturnValueSecurityAnalysis returnValueAnalysis;
    
    // SpEL security analysis
    private final SpELSecurityAnalysis spelAnalysis;
    
    // Integration analysis
    private final List<String> relatedSecurityEntrypoints;
    private final SecurityChainType securityChainType;
    
    // Risk assessment
    private final List<SecurityRisk> identifiedRisks;
    private final SecurityRiskLevel overallRiskLevel;
    
    public enum SecurityAnnotationType {
        PRE_AUTHORIZE("@PreAuthorize", "Method-level pre-invocation authorization"),
        POST_AUTHORIZE("@PostAuthorize", "Method-level post-invocation authorization"),
        SECURED("@Secured", "Role-based method security"),
        ROLES_ALLOWED("@RolesAllowed", "JSR-250 role-based security"),
        DENY_ALL("@DenyAll", "Deny all access"),
        PERMIT_ALL("@PermitAll", "Permit all access"),
        PRE_FILTER("@PreFilter", "Pre-invocation collection filtering"),
        POST_FILTER("@PostFilter", "Post-invocation collection filtering");
        
        private final String displayName;
        private final String description;
        
        SecurityAnnotationType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    public enum SecurityConstraintComplexity {
        SIMPLE(1, "Simple role or permission check"),
        MODERATE(2, "Multiple roles or basic expressions"),
        COMPLEX(3, "Complex SpEL expressions with context"),
        VERY_COMPLEX(4, "Highly complex expressions with multiple dependencies");
        
        private final int level;
        private final String description;
        
        SecurityConstraintComplexity(int level, String description) {
            this.level = level;
            this.description = description;
        }
        
        public int getLevel() { return level; }
        public String getDescription() { return description; }
    }
    
    public enum SecurityChainType {
        STANDALONE("Standalone Security Check"),
        CHAINED("Part of Security Chain"),
        NESTED("Nested Security Context"),
        INHERITED("Inherited Security Policy");
        
        private final String displayName;
        
        SecurityChainType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() { return displayName; }
    }
}
```

### SecurityConstraint
```java
package it.denzosoft.jreverse.analyzer.security;

public class SecurityConstraint {
    
    private final SecurityAnnotationType type;
    private final String rawExpression;
    private final List<String> requiredRoles;
    private final List<String> requiredPermissions;
    private final SpELExpression spelExpression;
    private final ConstraintEvaluationMode evaluationMode;
    
    // SpEL expression analysis
    public static class SpELExpression {
        private final String expression;
        private final List<String> referencedVariables;
        private final List<String> referencedMethods;
        private final boolean hasComplexLogic;
        private final SpELComplexityLevel complexityLevel;
        private final List<SpELSecurityRisk> securityRisks;
        
        // ... constructor and getters
        
        public enum SpELComplexityLevel {
            SIMPLE, MODERATE, COMPLEX, VERY_COMPLEX
        }
        
        public enum SpELSecurityRisk {
            INJECTION_RISK("SpEL Injection Risk"),
            EXCESSIVE_PERMISSIONS("Overly Permissive Expression"),
            LOGIC_FLAW("Logical Flaw in Expression"),
            PERFORMANCE_IMPACT("Performance Impact");
            
            private final String description;
            
            SpELSecurityRisk(String description) {
                this.description = description;
            }
        }
    }
    
    public enum ConstraintEvaluationMode {
        PRE_INVOCATION("Evaluated before method execution"),
        POST_INVOCATION("Evaluated after method execution"),
        FILTER_BASED("Applied as collection filter"),
        STATIC_ROLES("Static role-based evaluation");
        
        private final String description;
        
        ConstraintEvaluationMode(String description) {
            this.description = description;
        }
    }
}
```

### SecurityMatrixAnalysis
```java
package it.denzosoft.jreverse.analyzer.security;

public class SecurityMatrixAnalysis {
    
    private final Map<String, Set<String>> roleToMethodsMatrix;
    private final Map<String, Set<String>> methodToRolesMatrix;
    private final Map<String, SecurityCoverageInfo> coverageByPackage;
    private final List<SecurityGap> identifiedGaps;
    private final SecurityMatrixMetrics metrics;
    
    // Gap analysis
    public static class SecurityGap {
        private final SecurityGapType type;
        private final String location;
        private final String description;
        private final GapSeverity severity;
        private final String recommendation;
        
        public enum SecurityGapType {
            UNPROTECTED_ENDPOINT("Unprotected Endpoint"),
            OVERPRIVILEGED_ACCESS("Overprivileged Access"),
            INCONSISTENT_PROTECTION("Inconsistent Protection Level"),
            MISSING_AUTHORIZATION("Missing Authorization Check"),
            WEAK_CONSTRAINT("Weak Security Constraint");
            
            private final String displayName;
            
            SecurityGapType(String displayName) {
                this.displayName = displayName;
            }
        }
        
        public enum GapSeverity {
            CRITICAL, HIGH, MEDIUM, LOW
        }
    }
    
    // Coverage information per package/class
    public static class SecurityCoverageInfo {
        private final String packageName;
        private final int totalMethods;
        private final int securedMethods;
        private final double coveragePercentage;
        private final List<String> unprotectedMethods;
        private final SecurityCoverageLevel coverageLevel;
        
        public enum SecurityCoverageLevel {
            EXCELLENT(90, 100, "Excellent security coverage"),
            GOOD(70, 89, "Good security coverage"),
            MODERATE(50, 69, "Moderate security coverage"),
            POOR(25, 49, "Poor security coverage"),
            CRITICAL(0, 24, "Critical lack of security coverage");
            
            private final int minPercentage;
            private final int maxPercentage;
            private final String description;
            
            SecurityCoverageLevel(int minPercentage, int maxPercentage, String description) {
                this.minPercentage = minPercentage;
                this.maxPercentage = maxPercentage;
                this.description = description;
            }
        }
    }
}
```

## Algoritmi di Analisi Avanzata

### 1. SpEL Security Analysis
```java
private SpELSecurityAnalysis analyzeSpELSecurity(Map<SecurityAnnotationType, SecurityConstraint> constraints) {
    SpELSecurityAnalysis.Builder builder = SpELSecurityAnalysis.builder();
    
    for (SecurityConstraint constraint : constraints.values()) {
        if (constraint.getSpelExpression() != null) {
            SpELExpression expression = constraint.getSpelExpression();
            
            // Analyze for injection risks
            if (hasInjectionRisk(expression)) {
                builder.addSecurityRisk(SpELSecurityRisk.INJECTION_RISK);
            }
            
            // Analyze expression complexity
            SpELComplexityLevel complexity = assessSpELComplexity(expression);
            builder.addComplexityAssessment(expression.getExpression(), complexity);
            
            // Check for common security anti-patterns
            List<SpELAntiPattern> antiPatterns = detectSpELAntiPatterns(expression);
            builder.antiPatterns(antiPatterns);
            
            // Performance impact analysis
            if (hasPerformanceImpact(expression)) {
                builder.addPerformanceWarning(expression.getExpression());
            }
        }
    }
    
    return builder.build();
}

private boolean hasInjectionRisk(SpELExpression expression) {
    String expr = expression.getExpression();
    
    // Check for dynamic expression construction patterns that could lead to injection
    return expr.contains("#{") && 
           (expr.contains("T(") || 
            expr.contains("@") || 
            expr.contains("new ") ||
            containsDynamicEvaluation(expr));
}

private List<SpELAntiPattern> detectSpELAntiPatterns(SpELExpression expression) {
    List<SpELAntiPattern> antiPatterns = new ArrayList<>();
    String expr = expression.getExpression();
    
    // Anti-pattern: Overly permissive expressions
    if (expr.contains("permitAll") && expr.contains("||")) {
        antiPatterns.add(SpELAntiPattern.OVERLY_PERMISSIVE);
    }
    
    // Anti-pattern: Hard-coded credentials
    if (containsHardcodedCredentials(expr)) {
        antiPatterns.add(SpELAntiPattern.HARDCODED_CREDENTIALS);
    }
    
    // Anti-pattern: Complex nested conditions
    if (countNestedConditions(expr) > 3) {
        antiPatterns.add(SpELAntiPattern.OVERLY_COMPLEX);
    }
    
    return antiPatterns;
}
```

### 2. Security Coverage Analysis
```java
private SecurityCoverageMetrics calculateSecurityCoverage(
        List<SecurityEntrypointInfo> entrypoints, CtClass[] classes) {
    
    SecurityCoverageMetrics.Builder builder = SecurityCoverageMetrics.builder();
    
    // Analyze coverage by package
    Map<String, PackageCoverageInfo> packageCoverage = new HashMap<>();
    
    for (CtClass ctClass : classes) {
        if (isBusinessLogicClass(ctClass)) {
            String packageName = ctClass.getPackageName();
            PackageCoverageInfo coverage = packageCoverage.computeIfAbsent(
                packageName, k -> new PackageCoverageInfo(k)
            );
            
            coverage.addTotalMethods(countPublicMethods(ctClass));
            
            // Count secured methods
            int securedMethods = countSecuredMethods(ctClass, entrypoints);
            coverage.addSecuredMethods(securedMethods);
        }
    }
    
    // Calculate overall metrics
    int totalMethods = packageCoverage.values().stream()
        .mapToInt(PackageCoverageInfo::getTotalMethods)
        .sum();
    
    int totalSecuredMethods = packageCoverage.values().stream()
        .mapToInt(PackageCoverageInfo::getSecuredMethods)
        .sum();
    
    double overallCoverage = (double) totalSecuredMethods / totalMethods * 100;
    
    builder.overallCoverage(overallCoverage)
           .packageCoverage(packageCoverage)
           .totalMethods(totalMethods)
           .securedMethods(totalSecuredMethods);
    
    return builder.build();
}
```

### 3. Vulnerability Detection
```java
@Override
public VulnerabilityAnalysis analyzeSecurityVulnerabilities(List<SecurityEntrypointInfo> entrypoints) {
    VulnerabilityAnalysis.Builder builder = VulnerabilityAnalysis.builder();
    
    for (SecurityEntrypointInfo entrypoint : entrypoints) {
        // Check for common security vulnerabilities
        List<SecurityVulnerability> vulnerabilities = new ArrayList<>();
        
        // 1. Broken Access Control
        if (hasBrokenAccessControl(entrypoint)) {
            vulnerabilities.add(createVulnerability(
                VulnerabilityType.BROKEN_ACCESS_CONTROL,
                entrypoint,
                "Inconsistent or missing access controls"
            ));
        }
        
        // 2. Injection vulnerabilities in SpEL
        if (hasSpELInjectionVulnerability(entrypoint)) {
            vulnerabilities.add(createVulnerability(
                VulnerabilityType.INJECTION,
                entrypoint,
                "Potential SpEL injection vulnerability"
            ));
        }
        
        // 3. Privilege escalation risks
        if (hasPrivilegeEscalationRisk(entrypoint)) {
            vulnerabilities.add(createVulnerability(
                VulnerabilityType.PRIVILEGE_ESCALATION,
                entrypoint,
                "Method allows privilege escalation"
            ));
        }
        
        // 4. Information disclosure
        if (hasInformationDisclosureRisk(entrypoint)) {
            vulnerabilities.add(createVulnerability(
                VulnerabilityType.INFORMATION_DISCLOSURE,
                entrypoint,
                "Method may expose sensitive information"
            ));
        }
        
        builder.addVulnerabilities(entrypoint.getClassName() + "." + entrypoint.getMethodName(), 
                                  vulnerabilities);
    }
    
    return builder.build();
}
```

## Integrazione con Report Esistenti

### Report da Aggiornare

1. **Report 01 (REST Endpoints Map)**: Aggiungere colonna per security annotations
2. **Report 06 (REST Controllers Map)**: Includere security matrix per controller
3. **Report 41 (Spring Security Config)**: Estendere con method-level security analysis
4. **Report 43 (Annotazioni Sicurezza)**: Upgrade completo con nuova analisi

### Nuovi Report da Creare

- **Report 53: Security Entrypoint Matrix** - Matrice completa role/method
- **Report 54: Security Vulnerability Assessment** - Assessment vulnerabilità
- **Report 55: SpEL Security Analysis** - Analisi dedicata espressioni SpEL

## Test Strategy Completa

### Security Testing
```java
@Test
public void testComplexSpELExpressionAnalysis() {
    // Given
    SecurityConstraint constraint = createComplexSpELConstraint();
    
    // When
    SpELSecurityAnalysis analysis = analyzer.analyzeSpELSecurity(Map.of(
        SecurityAnnotationType.PRE_AUTHORIZE, constraint
    ));
    
    // Then
    assertThat(analysis.getSecurityRisks()).contains(SpELSecurityRisk.INJECTION_RISK);
    assertThat(analysis.getComplexityLevel()).isEqualTo(SpELComplexityLevel.VERY_COMPLEX);
}

@Test
public void testSecurityCoverageCalculation() {
    // Given
    List<SecurityEntrypointInfo> entrypoints = createMixedSecurityEntrypoints();
    CtClass[] classes = createTestClasses();
    
    // When
    SecurityCoverageMetrics metrics = analyzer.calculateSecurityCoverage(entrypoints, classes);
    
    // Then
    assertThat(metrics.getOverallCoverage()).isBetween(70.0, 80.0);
    assertThat(metrics.getPackageCoverage()).hasSize(3);
}
```

## Metriche di Qualità

### Algoritmo di Scoring Avanzato (0-100)
```java
public int calculateSecurityQualityScore(SecurityEntrypointAnalysisResult result) {
    double score = 100.0;
    
    // Penalizzazioni critiche per vulnerabilità
    score -= result.getCriticalVulnerabilities() * 30;        // -30 per vulnerabilità critiche
    score -= result.getHighVulnerabilities() * 20;           // -20 per vulnerabilità high
    score -= result.getSpELInjectionRisks() * 25;            // -25 per SpEL injection risks
    score -= result.getBrokenAccessControls() * 18;          // -18 per broken access control
    
    // Penalizzazioni per coverage insufficiente
    double coverageScore = result.getCoverageMetrics().getOverallCoverage();
    if (coverageScore < 80) {
        score -= (80 - coverageScore) * 0.5;                 // -0.5 per ogni % sotto 80%
    }
    
    // Penalizzazioni per anti-patterns
    score -= result.getSpELAntiPatterns() * 8;               // -8 per SpEL anti-pattern
    score -= result.getInconsistentSecurityPolicies() * 10;  // -10 per policy inconsistenti
    score -= result.getOverprivilegedMethods() * 6;          // -6 per metodi overprivileged
    
    // Bonus per best practices
    score += result.getComprehensiveSecurityPolicies() * 5;  // +5 per policy complete
    score += result.getProperSpELUsage() * 3;                // +3 per uso corretto SpEL
    score += result.getConsistentSecurityPatterns() * 4;     // +4 per pattern consistenti
    
    return Math.max(0, Math.min(100, (int) score));
}
```

## Considerazioni Architetturali

### Integration con Spring Security
- Support per custom security configurations
- Analysis di method security configuration
- Integration con role hierarchy definitions

### Extensibility
- Plugin per custom security annotations
- Support per OAuth2 e JWT analysis
- Extension per security frameworks alternativi

### Performance e Scalability
- Lazy evaluation delle security matrices
- Caching delle policy evaluation
- Parallel analysis per large codebases