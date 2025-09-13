# Report 43: Annotazioni di Sicurezza

**Valore**: ⭐⭐⭐⭐ **Complessità**: 🟡 Media **Tempo**: 4-5 giorni
**Tags**: `#security-annotations` `#authorization` `#method-security` `#preauthorize`

## Descrizione

Analizza l'utilizzo delle annotazioni di sicurezza Spring Security nell'applicazione, identificando pattern di autorizzazione, controlli di accesso method-level, e verifica la copertura e correttezza delle protezioni implementate.

## Sezioni del Report

### 1. Security Annotations Overview
- Distribuzione delle annotazioni di sicurezza utilizzate
- Coverage della sicurezza method-level
- Pattern di autorizzazione comuni
- Metodi non protetti in classi sensibili

### 2. Method-Level Security Analysis
- @PreAuthorize e @PostAuthorize usage
- @Secured annotations
- @RolesAllowed implementation
- Expression-based security

### 3. Class-Level Security
- @EnableGlobalMethodSecurity configuration
- @EnableMethodSecurity settings
- Security proxy configuration
- AOP aspects for security

### 4. Authorization Patterns
- Role-based access control (RBAC) patterns
- Permission-based access control
- Custom security expressions
- Context-aware authorization

## Implementazione con Javassist

```java
public class SecurityAnnotationsAnalyzer {
    
    public SecurityAnnotationsReport analyzeSecurityAnnotations(CtClass[] classes) {
        SecurityAnnotationsReport report = new SecurityAnnotationsReport();
        
        for (CtClass ctClass : classes) {
            analyzeClassSecurityAnnotations(ctClass, report);
            analyzeMethodSecurityAnnotations(ctClass, report);
        }
        
        analyzeSecurityConfiguration(report);
        calculateSecurityCoverage(report);
        
        return report;
    }
    
    private void analyzeClassSecurityAnnotations(CtClass ctClass, SecurityAnnotationsReport report) {
        try {
            ClassSecurityInfo classInfo = new ClassSecurityInfo();
            classInfo.setClassName(ctClass.getName());
            
            // Analizza annotazioni a livello di classe
            if (ctClass.hasAnnotation("org.springframework.security.access.prepost.PreAuthorize")) {
                ClassLevelSecurity security = extractPreAuthorize(ctClass);
                classInfo.addClassLevelSecurity(security);
            }
            
            if (ctClass.hasAnnotation("javax.annotation.security.RolesAllowed")) {
                ClassLevelSecurity security = extractRolesAllowed(ctClass);
                classInfo.addClassLevelSecurity(security);
            }
            
            if (ctClass.hasAnnotation("org.springframework.security.access.annotation.Secured")) {
                ClassLevelSecurity security = extractSecured(ctClass);
                classInfo.addClassLevelSecurity(security);
            }
            
            // Verifica se è un controller o service senza protezioni
            if (isSecuritySensitiveClass(ctClass) && !hasClassLevelSecurity(ctClass)) {
                SecurityViolation violation = new SecurityViolation();
                violation.setType(ViolationType.UNPROTECTED_SENSITIVE_CLASS);
                violation.setClassName(ctClass.getName());
                violation.setSeverity(Severity.HIGH);
                violation.setDescription("Security-sensitive class without protection");
                
                report.addSecurityViolation(violation);
            }
            
            report.addClassSecurityInfo(classInfo);
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi class security: " + e.getMessage());
        }
    }
    
    private void analyzeMethodSecurityAnnotations(CtClass ctClass, SecurityAnnotationsReport report) {
        try {
            CtMethod[] methods = ctClass.getDeclaredMethods();
            
            for (CtMethod method : methods) {
                MethodSecurityInfo methodInfo = new MethodSecurityInfo();
                methodInfo.setClassName(ctClass.getName());
                methodInfo.setMethodName(method.getName());
                methodInfo.setMethodSignature(method.getSignature());
                
                // @PreAuthorize
                if (method.hasAnnotation("org.springframework.security.access.prepost.PreAuthorize")) {
                    PreAuthorizeInfo preAuth = extractMethodPreAuthorize(method);
                    methodInfo.setPreAuthorize(preAuth);
                    
                    // Analizza complessità dell'espressione
                    analyzeSecurityExpression(preAuth.getExpression(), methodInfo);
                }
                
                // @PostAuthorize
                if (method.hasAnnotation("org.springframework.security.access.prepost.PostAuthorize")) {
                    PostAuthorizeInfo postAuth = extractPostAuthorize(method);
                    methodInfo.setPostAuthorize(postAuth);
                }
                
                // @Secured
                if (method.hasAnnotation("org.springframework.security.access.annotation.Secured")) {
                    SecuredInfo secured = extractMethodSecured(method);
                    methodInfo.setSecured(secured);
                }
                
                // @RolesAllowed
                if (method.hasAnnotation("javax.annotation.security.RolesAllowed")) {
                    RolesAllowedInfo roles = extractMethodRolesAllowed(method);
                    methodInfo.setRolesAllowed(roles);
                }
                
                // Verifica metodi pubblici senza protezione
                if (isPublicMethod(method) && !hasMethodSecurity(method)) {
                    if (isSecuritySensitiveMethod(method)) {
                        SecurityViolation violation = new SecurityViolation();
                        violation.setType(ViolationType.UNPROTECTED_SENSITIVE_METHOD);
                        violation.setClassName(ctClass.getName());
                        violation.setMethodName(method.getName());
                        violation.setSeverity(Severity.MEDIUM);
                        violation.setDescription("Security-sensitive method without protection");
                        
                        report.addSecurityViolation(violation);
                    }
                }
                
                report.addMethodSecurityInfo(methodInfo);
            }
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi method security: " + e.getMessage());
        }
    }
    
    private void analyzeSecurityExpression(String expression, MethodSecurityInfo methodInfo) {
        SecurityExpressionAnalysis analysis = new SecurityExpressionAnalysis();
        analysis.setExpression(expression);
        
        // Analizza complessità dell'espressione
        int complexity = calculateExpressionComplexity(expression);
        analysis.setComplexity(complexity);
        
        // Identifica pattern comuni
        if (expression.contains("hasRole(")) {
            analysis.addPattern("ROLE_BASED");
        }
        if (expression.contains("hasAuthority(")) {
            analysis.addPattern("AUTHORITY_BASED");
        }
        if (expression.contains("authentication.")) {
            analysis.addPattern("AUTHENTICATION_BASED");
        }
        if (expression.contains("#")) {
            analysis.addPattern("PARAMETER_BASED");
        }
        
        // Verifica best practices
        if (expression.length() > 100) {
            analysis.addIssue("COMPLEX_EXPRESSION");
        }
        if (expression.contains("||") && expression.split("\\|\\|").length > 3) {
            analysis.addIssue("TOO_MANY_OR_CONDITIONS");
        }
        
        methodInfo.setExpressionAnalysis(analysis);
    }
    
    private boolean isSecuritySensitiveClass(CtClass ctClass) {
        // Controller, Service, Repository sono classi sensibili
        return ctClass.hasAnnotation("org.springframework.stereotype.Controller") ||
               ctClass.hasAnnotation("org.springframework.web.bind.annotation.RestController") ||
               ctClass.hasAnnotation("org.springframework.stereotype.Service") ||
               ctClass.hasAnnotation("org.springframework.stereotype.Repository");
    }
    
    private boolean isSecuritySensitiveMethod(CtMethod method) {
        try {
            // Metodi con @RequestMapping o simili sono sensibili
            return method.hasAnnotation("org.springframework.web.bind.annotation.RequestMapping") ||
                   method.hasAnnotation("org.springframework.web.bind.annotation.GetMapping") ||
                   method.hasAnnotation("org.springframework.web.bind.annotation.PostMapping") ||
                   method.hasAnnotation("org.springframework.web.bind.annotation.PutMapping") ||
                   method.hasAnnotation("org.springframework.web.bind.annotation.DeleteMapping") ||
                   method.getName().startsWith("delete") ||
                   method.getName().startsWith("update") ||
                   method.getName().startsWith("create");
        } catch (Exception e) {
            return false;
        }
    }
    
    private void calculateSecurityCoverage(SecurityAnnotationsReport report) {
        SecurityCoverageMetrics metrics = new SecurityCoverageMetrics();
        
        int totalSensitiveMethods = 0;
        int protectedMethods = 0;
        int totalSensitiveClasses = 0;
        int protectedClasses = 0;
        
        for (ClassSecurityInfo classInfo : report.getClassSecurityInfos()) {
            if (classInfo.isSecuritySensitive()) {
                totalSensitiveClasses++;
                if (classInfo.hasClassLevelSecurity()) {
                    protectedClasses++;
                }
            }
        }
        
        for (MethodSecurityInfo methodInfo : report.getMethodSecurityInfos()) {
            if (methodInfo.isSecuritySensitive()) {
                totalSensitiveMethods++;
                if (methodInfo.hasSecurityAnnotation()) {
                    protectedMethods++;
                }
            }
        }
        
        if (totalSensitiveClasses > 0) {
            metrics.setClassCoverage((double) protectedClasses / totalSensitiveClasses * 100);
        }
        
        if (totalSensitiveMethods > 0) {
            metrics.setMethodCoverage((double) protectedMethods / totalSensitiveMethods * 100);
        }
        
        report.setSecurityCoverageMetrics(metrics);
    }
}

public class SecurityAnnotationsReport {
    private List<ClassSecurityInfo> classSecurityInfos = new ArrayList<>();
    private List<MethodSecurityInfo> methodSecurityInfos = new ArrayList<>();
    private List<SecurityViolation> securityViolations = new ArrayList<>();
    private SecurityCoverageMetrics securityCoverageMetrics;
    private List<String> errors = new ArrayList<>();
    
    public static class MethodSecurityInfo {
        private String className;
        private String methodName;
        private String methodSignature;
        private PreAuthorizeInfo preAuthorize;
        private PostAuthorizeInfo postAuthorize;
        private SecuredInfo secured;
        private RolesAllowedInfo rolesAllowed;
        private SecurityExpressionAnalysis expressionAnalysis;
        private boolean securitySensitive;
    }
    
    public static class SecurityViolation {
        private ViolationType type;
        private String className;
        private String methodName;
        private Severity severity;
        private String description;
        private String recommendation;
    }
    
    public static class SecurityCoverageMetrics {
        private double classCoverage;
        private double methodCoverage;
        private int totalSecurityAnnotations;
        private Map<String, Integer> annotationDistribution = new HashMap<>();
    }
    
    public enum ViolationType {
        UNPROTECTED_SENSITIVE_CLASS,
        UNPROTECTED_SENSITIVE_METHOD,
        COMPLEX_SECURITY_EXPRESSION,
        INCONSISTENT_SECURITY_PATTERN,
        DEPRECATED_SECURITY_ANNOTATION
    }
    
    public enum Severity {
        CRITICAL, HIGH, MEDIUM, LOW
    }
}
```

## Raccolta Dati

### 1. Method-Level Security
- Annotazioni @PreAuthorize, @PostAuthorize, @Secured, @RolesAllowed
- Espressioni di sicurezza e loro complessità
- Parametri utilizzati nelle espressioni di sicurezza

### 2. Class-Level Security
- Annotazioni di sicurezza a livello di classe
- Configurazioni @EnableGlobalMethodSecurity
- Proxy configuration e AOP aspects

### 3. Security Coverage
- Percentuale di metodi sensibili protetti
- Classi controller/service senza protezioni
- Pattern di autorizzazione utilizzati

### 4. Security Expression Analysis
- Complessità delle espressioni SpEL
- Pattern di accesso role-based vs permission-based
- Custom security expressions

## Metriche di Qualità del Codice

### Algoritmo di Calcolo (0-100)

```java
public int calculateSecurityAnnotationsQualityScore(SecurityAnnotationsReport result) {
    double score = 100.0;
    
    // Penalizzazioni per problemi di sicurezza
    score -= result.getUnprotectedSensitiveClasses() * 20;    // -20 per classe sensibile non protetta
    score -= result.getUnprotectedSensitiveMethods() * 15;    // -15 per metodo sensibile non protetto
    score -= result.getComplexSecurityExpressions() * 10;     // -10 per espressioni troppo complesse
    score -= result.getInconsistentPatterns() * 8;            // -8 per pattern inconsistenti
    score -= result.getDeprecatedAnnotations() * 12;          // -12 per annotazioni deprecate
    score -= result.getOverlyPermissiveRoles() * 6;           // -6 per ruoli troppo permissivi
    score -= result.getMissingPostAuthorize() * 4;            // -4 per mancanza @PostAuthorize su dati sensibili
    
    // Bonus per best practices
    score += result.getProperExpressionBasedSecurity() * 3;   // +3 per espressioni ben strutturate
    score += result.getConsistentSecurityPatterns() * 2;      // +2 per pattern consistenti
    score += result.getGoodSecurityCoverage() * 1;            // +1 per buona copertura
    
    return Math.max(0, Math.min(100, (int) score));
}
```

### Soglie di Valutazione
- **0-40**: 🔴 CRITICO - Gravi lacune nella sicurezza method-level
- **41-60**: 🟡 SUFFICIENTE - Sicurezza parziale ma con gap significativi
- **61-80**: 🟢 BUONO - Buona copertura di sicurezza con alcuni miglioramenti
- **81-100**: ⭐ ECCELLENTE - Sicurezza method-level robusta e completa

## Segnalazioni per Gravità

### 🔴 GRAVITÀ CRITICA (Score Impact: -15 to -20)
1. **Classi sensibili non protette**
   - Descrizione: Controller o Service senza annotazioni di sicurezza
   - Rischio: Accesso non autorizzato a funzionalità business-critical
   - Soluzione: Aggiungere @PreAuthorize o @Secured a livello classe/metodo

2. **Metodi sensibili non protetti**
   - Descrizione: Metodi CRUD o endpoint REST senza controlli di accesso
   - Rischio: Operazioni critiche eseguibili da utenti non autorizzati
   - Soluzione: Implementare method-level security appropriata

### 🟠 GRAVITÀ ALTA (Score Impact: -10 to -12)
3. **Annotazioni di sicurezza deprecate**
   - Descrizione: Uso di annotazioni obsolete o non raccomandate
   - Rischio: Compatibilità futura e supporto ridotto
   - Soluzione: Migrare a @PreAuthorize/@PostAuthorize moderne

4. **Espressioni di sicurezza troppo complesse**
   - Descrizione: Espressioni SpEL lunghe e articolate difficili da mantenere
   - Rischio: Errori nella logica di autorizzazione, difficile debugging
   - Soluzione: Semplificare o delegare a custom security evaluators

### 🟡 GRAVITÀ MEDIA (Score Impact: -4 to -8)
5. **Pattern di sicurezza inconsistenti**
   - Descrizione: Mix di approcci di sicurezza senza coerenza
   - Rischio: Confusione, errori di configurazione
   - Soluzione: Standardizzare su un approccio uniforme

6. **Ruoli troppo permissivi**
   - Descrizione: Uso eccessivo di ruoli generici o amministratori
   - Rischio: Principio least privilege violato
   - Soluzione: Definire ruoli granulari e specifici

### 🔵 GRAVITÀ BASSA (Score Impact: -1 to -4)
7. **Mancanza @PostAuthorize per dati sensibili**
   - Descrizione: Controlli solo pre-execution senza post-processing
   - Rischio: Data leakage se oggetti contengono dati non autorizzati
   - Soluzione: Aggiungere @PostAuthorize per filtraggio risultati

## Metriche di Valore

- **Security Coverage**: Percentuale di metodi/classi sensibili protetti
- **Authorization Consistency**: Coerenza nei pattern di autorizzazione
- **Expression Complexity**: Complessità media delle espressioni di sicurezza
- **Security Pattern Distribution**: Distribuzione dei pattern utilizzati

## Classificazione

**Categoria**: Security & Robustness
**Priorità**: Alta - La sicurezza method-level è fondamentale per proteggere le business operations
**Stakeholder**: Security team, Development team, Compliance team