# Report 46: Validazione Input

**Valore**: ⭐⭐⭐⭐⭐ **Complessità**: 🔴 Complessa **Tempo**: 7-9 giorni
**Tags**: `#input-validation` `#security` `#data-sanitization` `#injection-prevention`

## Descrizione

Analizza tutti i punti di ingresso dell'applicazione per identificare meccanismi di validazione degli input, potenziali vulnerabilità di injection e pratiche di sanitizzazione dei dati.

## Sezioni del Report

### 1. Input Validation Overview
- Controller endpoints con validazione input
- Meccanismi di validazione utilizzati
- Pattern di validazione comuni
- Campi senza validazione

### 2. Validation Mechanisms Analysis
- Annotazioni JSR-303/JSR-380 (Bean Validation)
- Validazioni custom
- Validazioni lato client vs server
- Messaggi di errore di validazione

### 3. Security Vulnerabilities
- Potenziali SQL injection points
- XSS vulnerabilities
- Command injection risks
- Path traversal vulnerabilities

### 4. Data Sanitization
- Sanitizzazione dell'output
- Encoding dei dati
- Escape sequences
- Content Security Policy

## Implementazione con Javassist

```java
public class InputValidationAnalyzer {
    
    private static final Set<String> VALIDATION_ANNOTATIONS = Set.of(
        "javax.validation.constraints.NotNull",
        "javax.validation.constraints.NotEmpty", 
        "javax.validation.constraints.NotBlank",
        "javax.validation.constraints.Size",
        "javax.validation.constraints.Min",
        "javax.validation.constraints.Max",
        "javax.validation.constraints.Pattern",
        "javax.validation.constraints.Email",
        "javax.validation.Valid",
        "org.springframework.validation.annotation.Validated"
    );
    
    private static final Set<String> INJECTION_RISK_PATTERNS = Set.of(
        "SELECT.*FROM", "INSERT.*INTO", "UPDATE.*SET", "DELETE.*FROM",
        "<script", "</script>", "javascript:", "on[a-z]+\\s*=",
        "../", "..\\\\", "/etc/passwd", "cmd.exe", "System.getProperty"
    );
    
    private static final Map<String, Integer> VALIDATION_SEVERITY_PENALTIES = Map.of(
        "UNVALIDATED_INPUT", -40,           // Input senza validazione
        "SQL_INJECTION_RISK", -50,          // Rischio SQL injection
        "XSS_VULNERABILITY", -45,           // Rischio XSS
        "COMMAND_INJECTION_RISK", -40,      // Rischio command injection
        "PATH_TRAVERSAL_RISK", -35,         // Rischio path traversal
        "WEAK_VALIDATION", -25,             // Validazione insufficiente
        "MISSING_SANITIZATION", -30,        // Mancanza sanitizzazione output
        "CLIENT_SIDE_VALIDATION_ONLY", -20, // Solo validazione client-side
        "GENERIC_ERROR_MESSAGES", -15,      // Messaggi di errore generici
        "INCONSISTENT_VALIDATION", -18,     // Validazione inconsistente
        "MISSING_CSRF_PROTECTION", -25      // Mancanza protezione CSRF
    );
    
    private static final Map<String, Integer> VALIDATION_SECURITY_BONUSES = Map.of(
        "COMPREHENSIVE_VALIDATION", 20,     // Validazione completa su tutti gli input
        "STRONG_INPUT_SANITIZATION", 18,    // Sanitizzazione robusta degli input
        "OUTPUT_ENCODING", 15,              // Encoding appropriato dell'output  
        "PARAMETERIZED_QUERIES", 25,        // Uso di prepared statements
        "WHITELIST_VALIDATION", 20,         // Validazione basata su whitelist
        "CONTENT_SECURITY_POLICY", 12,      // Implementazione CSP
        "CSRF_PROTECTION", 15,              // Protezione CSRF implementata
        "FILE_UPLOAD_SECURITY", 15,         // Validazione sicura file upload
        "RATE_LIMITING", 10,                // Rate limiting implementato
        "CUSTOM_VALIDATION_LOGIC", 8,       // Logiche di validazione custom
        "SERVER_SIDE_VALIDATION", 12        // Validazione server-side robusta
    );
    
    public InputValidationReport analyzeInputValidation(CtClass[] classes) {
        InputValidationReport report = new InputValidationReport();
        
        for (CtClass ctClass : classes) {
            analyzeControllerValidation(ctClass, report);
            analyzeEntityValidation(ctClass, report);
            analyzeCustomValidation(ctClass, report);
            analyzeSanitizationMethods(ctClass, report);
            analyzeSecurityVulnerabilities(ctClass, report);
            analyzeCSRFProtection(ctClass, report);
        }
        
        evaluateValidationCoverage(report);
        calculateInputValidationQualityScore(report);
        return report;
    }
    
    private void analyzeControllerValidation(CtClass ctClass, InputValidationReport report) {
        try {
            // Verifica se è un controller
            if (!isController(ctClass)) return;
            
            CtMethod[] methods = ctClass.getDeclaredMethods();
            
            for (CtMethod method : methods) {
                if (isRequestMappedMethod(method)) {
                    analyzeEndpointValidation(method, report);
                }
            }
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi controller validation: " + e.getMessage());
        }
    }
    
    private void analyzeEndpointValidation(CtMethod method, InputValidationReport report) {
        try {
            ParameterAnnotationsAttribute paramAttrs = 
                (ParameterAnnotationsAttribute) method.getMethodInfo()
                    .getAttribute(ParameterAnnotationsAttribute.visibleTag);
            
            if (paramAttrs != null) {
                Annotation[][] annotations = paramAttrs.getAnnotations();
                CtClass[] paramTypes = method.getParameterTypes();
                
                for (int i = 0; i < annotations.length; i++) {
                    EndpointParameter param = new EndpointParameter();
                    param.setType(paramTypes[i].getName());
                    param.setMethodName(method.getName());
                    param.setClassName(method.getDeclaringClass().getName());
                    
                    boolean hasValidation = false;
                    
                    for (Annotation annotation : annotations[i]) {
                        String annotationType = annotation.getTypeName();
                        
                        if (isValidationAnnotation(annotationType)) {
                            param.addValidationAnnotation(annotationType);
                            hasValidation = true;
                        }
                        
                        if (annotationType.equals("javax.validation.Valid")) {
                            param.setHasValidAnnotation(true);
                            hasValidation = true;
                        }
                    }
                    
                    param.setValidated(hasValidation);
                    report.addEndpointParameter(param);
                    
                    if (!hasValidation) {
                        report.addUnvalidatedParameter(param);
                    }
                }
            }
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi endpoint: " + e.getMessage());
        }
    }
    
    private void analyzeEntityValidation(CtClass ctClass, InputValidationReport report) {
        try {
            if (!isEntity(ctClass)) return;
            
            CtField[] fields = ctClass.getDeclaredFields();
            
            for (CtField field : fields) {
                EntityField entityField = new EntityField();
                entityField.setFieldName(field.getName());
                entityField.setFieldType(field.getType().getName());
                entityField.setClassName(ctClass.getName());
                
                AnnotationsAttribute attr = (AnnotationsAttribute) 
                    field.getFieldInfo().getAttribute(AnnotationsAttribute.visibleTag);
                
                if (attr != null) {
                    for (Annotation annotation : attr.getAnnotations()) {
                        String annotationType = annotation.getTypeName();
                        
                        if (isValidationAnnotation(annotationType)) {
                            entityField.addValidationAnnotation(annotationType);
                            extractValidationConstraints(annotation, entityField);
                        }
                    }
                }
                
                report.addEntityField(entityField);
            }
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi entity validation: " + e.getMessage());
        }
    }
    
    private void analyzeCustomValidation(CtClass ctClass, InputValidationReport report) {
        try {
            CtMethod[] methods = ctClass.getDeclaredMethods();
            
            for (CtMethod method : methods) {
                String methodBody = getMethodBody(method);
                
                // Cerca pattern di validazione custom
                if (containsValidationPatterns(methodBody)) {
                    CustomValidation validation = new CustomValidation();
                    validation.setMethodName(method.getName());
                    validation.setClassName(ctClass.getName());
                    validation.setValidationLogic(extractValidationLogic(methodBody));
                    
                    report.addCustomValidation(validation);
                }
            }
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi custom validation: " + e.getMessage());
        }
    }
    
    private void analyzeSanitizationMethods(CtClass ctClass, InputValidationReport report) {
        try {
            CtMethod[] methods = ctClass.getDeclaredMethods();
            
            for (CtMethod method : methods) {
                String methodBody = getMethodBody(method);
                
                // Cerca metodi di sanitizzazione
                if (containsSanitizationPatterns(methodBody)) {
                    SanitizationMethod sanitization = new SanitizationMethod();
                    sanitization.setMethodName(method.getName());
                    sanitization.setClassName(ctClass.getName());
                    sanitization.setSanitizationType(determineSanitizationType(methodBody));
                    
                    report.addSanitizationMethod(sanitization);
                }
            }
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi sanitization: " + e.getMessage());
        }
    }
    
    private void analyzeSecurityVulnerabilities(CtClass ctClass, InputValidationReport report) {
        try {
            CtMethod[] methods = ctClass.getDeclaredMethods();
            
            for (CtMethod method : methods) {
                String methodBody = getMethodBody(method);
                
                SecurityVulnerabilityAnalysis vulnAnalysis = new SecurityVulnerabilityAnalysis();
                vulnAnalysis.setClassName(ctClass.getName());
                vulnAnalysis.setMethodName(method.getName());
                
                // Analizza rischi SQL injection
                if (containsSQLInjectionRisk(methodBody)) {
                    vulnAnalysis.addVulnerability("SQL_INJECTION_RISK", "CRITICA", 
                        "Potenziale SQL injection in " + method.getName(),
                        "Utilizzare prepared statements e parametri legati");
                }
                
                // Analizza rischi XSS
                if (containsXSSRisk(methodBody)) {
                    vulnAnalysis.addVulnerability("XSS_VULNERABILITY", "CRITICA",
                        "Potenziale XSS vulnerability in " + method.getName(), 
                        "Implementare output encoding e Content Security Policy");
                }
                
                // Analizza rischi command injection
                if (containsCommandInjectionRisk(methodBody)) {
                    vulnAnalysis.addVulnerability("COMMAND_INJECTION_RISK", "ALTA",
                        "Potenziale command injection in " + method.getName(),
                        "Validare e sanitizzare input prima dell'esecuzione comandi");
                }
                
                // Analizza rischi path traversal
                if (containsPathTraversalRisk(methodBody)) {
                    vulnAnalysis.addVulnerability("PATH_TRAVERSAL_RISK", "ALTA",
                        "Potenziale path traversal in " + method.getName(),
                        "Validare e normalizzare path prima dell'accesso file");
                }
                
                if (!vulnAnalysis.getVulnerabilities().isEmpty()) {
                    report.addSecurityVulnerabilityAnalysis(vulnAnalysis);
                }
            }
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi vulnerabilità: " + e.getMessage());
        }
    }
    
    private void analyzeCSRFProtection(CtClass ctClass, InputValidationReport report) {
        try {
            // Cerca configurazioni CSRF
            CtMethod[] methods = ctClass.getDeclaredMethods();
            
            for (CtMethod method : methods) {
                AnnotationsAttribute attr = (AnnotationsAttribute) 
                    method.getMethodInfo().getAttribute(AnnotationsAttribute.visibleTag);
                
                if (attr != null) {
                    for (Annotation annotation : attr.getAnnotations()) {
                        String annotationType = annotation.getTypeName();
                        
                        if (annotationType.contains("PostMapping") ||
                            annotationType.contains("PutMapping") ||
                            annotationType.contains("DeleteMapping")) {
                            
                            CSRFProtectionAnalysis csrfAnalysis = new CSRFProtectionAnalysis();
                            csrfAnalysis.setClassName(ctClass.getName());
                            csrfAnalysis.setMethodName(method.getName());
                            csrfAnalysis.setHttpMethod(extractHttpMethod(annotationType));
                            
                            // Verifica presenza protezione CSRF
                            if (!hasCSRFProtection(method, ctClass)) {
                                csrfAnalysis.setHasCSRFProtection(false);
                                csrfAnalysis.addSecurityIssue("MISSING_CSRF_PROTECTION", 
                                    "Metodo di modifica senza protezione CSRF");
                            } else {
                                csrfAnalysis.setHasCSRFProtection(true);
                            }
                            
                            report.addCSRFProtectionAnalysis(csrfAnalysis);
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi CSRF protection: " + e.getMessage());
        }
    }
    
    private void evaluateValidationCoverage(InputValidationReport report) {
        ValidationCoverageEvaluation evaluation = new ValidationCoverageEvaluation();
        
        // Calcola coverage parametri controller
        int totalEndpointParams = report.getEndpointParameters().size();
        int validatedParams = (int) report.getEndpointParameters().stream()
            .filter(EndpointParameter::isValidated)
            .count();
        
        if (totalEndpointParams > 0) {
            double coverage = (double) validatedParams / totalEndpointParams;
            evaluation.setEndpointValidationCoverage(coverage);
        }
        
        // Calcola coverage campi entity
        int totalEntityFields = report.getEntityFields().size();
        int validatedEntityFields = (int) report.getEntityFields().stream()
            .filter(field -> !field.getValidationAnnotations().isEmpty())
            .count();
        
        if (totalEntityFields > 0) {
            double coverage = (double) validatedEntityFields / totalEntityFields;
            evaluation.setEntityValidationCoverage(coverage);
        }
        
        // Valuta presenza custom validation
        evaluation.setHasCustomValidation(!report.getCustomValidations().isEmpty());
        evaluation.setHasSanitization(!report.getSanitizationMethods().isEmpty());
        
        // Conta vulnerabilità
        long criticalVulns = report.getSecurityVulnerabilityAnalyses().stream()
            .flatMap(analysis -> analysis.getVulnerabilities().stream())
            .filter(vuln -> "CRITICA".equals(vuln.getSeverity()))
            .count();
        
        evaluation.setCriticalVulnerabilityCount(criticalVulns);
        
        report.setValidationCoverageEvaluation(evaluation);
    }
    
    private void calculateInputValidationQualityScore(InputValidationReport report) {
        int baseScore = 100;
        int totalPenalties = 0;
        int totalBonuses = 0;
        
        ValidationCoverageEvaluation evaluation = report.getValidationCoverageEvaluation();
        List<QualityIssue> qualityIssues = new ArrayList<>();
        
        // Penalità per bassa copertura validazione
        double endpointCoverage = evaluation.getEndpointValidationCoverage();
        if (endpointCoverage < 0.5) {
            totalPenalties += 40;
            qualityIssues.add(new QualityIssue(
                "UNVALIDATED_INPUT",
                "CRITICA",
                String.format("Bassa copertura validazione endpoint: %.1f%%", endpointCoverage * 100),
                "Implementare validazione su tutti i parametri di input"
            ));
        } else if (endpointCoverage < 0.8) {
            totalPenalties += 20;
            qualityIssues.add(new QualityIssue(
                "WEAK_VALIDATION",
                "ALTA", 
                String.format("Copertura validazione migliorabile: %.1f%%", endpointCoverage * 100),
                "Aumentare la copertura di validazione sui parametri critici"
            ));
        }
        
        // Penalità per vulnerabilità critiche
        if (evaluation.getCriticalVulnerabilityCount() > 0) {
            int penalty = Math.min(50, (int) evaluation.getCriticalVulnerabilityCount() * 25);
            totalPenalties += penalty;
            qualityIssues.add(new QualityIssue(
                "CRITICAL_VULNERABILITIES",
                "CRITICA",
                String.format("%d vulnerabilità critiche rilevate", evaluation.getCriticalVulnerabilityCount()),
                "Risolvere immediatamente tutte le vulnerabilità critiche"
            ));
        }
        
        // Penalità per mancanza sanitizzazione
        if (!evaluation.isHasSanitization()) {
            totalPenalties += 30;
            qualityIssues.add(new QualityIssue(
                "MISSING_SANITIZATION",
                "ALTA",
                "Mancanza di metodi di sanitizzazione dell'output",
                "Implementare sanitizzazione appropriata per tutti gli output"
            ));
        }
        
        // Bonus per alta copertura
        if (endpointCoverage > 0.9) {
            totalBonuses += 20;
        }
        
        // Bonus per custom validation
        if (evaluation.isHasCustomValidation()) {
            totalBonuses += 8;
        }
        
        // Bonus per sanitizzazione
        if (evaluation.isHasSanitization()) {
            totalBonuses += 18;
        }
        
        // Calcola score finale
        int finalScore = Math.max(0, Math.min(100, baseScore - totalPenalties + totalBonuses));
        
        InputValidationQualityScore qualityScore = new InputValidationQualityScore();
        qualityScore.setOverallScore(finalScore);
        qualityScore.setValidationCoverageScore((int) (endpointCoverage * 100));
        qualityScore.setSecurityScore(calculateSecurityScore(report));
        qualityScore.setSanitizationScore(evaluation.isHasSanitization() ? 100 : 0);
        qualityScore.setCustomValidationScore(evaluation.isHasCustomValidation() ? 80 : 40);
        qualityScore.setQualityLevel(determineQualityLevel(finalScore));
        qualityScore.setQualityIssues(qualityIssues);
        qualityScore.setTotalPenalties(totalPenalties);
        qualityScore.setTotalBonuses(totalBonuses);
        
        report.setQualityScore(qualityScore);
    }
    
    // Helper methods
    private boolean containsSQLInjectionRisk(String methodBody) {
        return methodBody.contains("SELECT") && methodBody.contains("+") &&
               !methodBody.contains("PreparedStatement") && !methodBody.contains("?");
    }
    
    private boolean containsXSSRisk(String methodBody) {
        return (methodBody.contains("innerHTML") || methodBody.contains("document.write")) &&
               !methodBody.contains("escapeHtml") && !methodBody.contains("encode");
    }
    
    private boolean containsCommandInjectionRisk(String methodBody) {
        return (methodBody.contains("Runtime.exec") || methodBody.contains("ProcessBuilder")) &&
               methodBody.contains("+") && !methodBody.contains("validate");
    }
    
    private boolean containsPathTraversalRisk(String methodBody) {
        return methodBody.contains("../") || (methodBody.contains("File(") && 
               methodBody.contains("+") && !methodBody.contains("normalize"));
    }
    
    private boolean hasCSRFProtection(CtMethod method, CtClass ctClass) {
        try {
            // Cerca annotazioni CSRF o configurazioni Spring Security
            String methodBody = getMethodBody(method);
            return methodBody.contains("@CsrfToken") || 
                   methodBody.contains("csrf().disable()") == false;
        } catch (Exception e) {
            return false;
        }
    }
    
    private int calculateSecurityScore(InputValidationReport report) {
        int score = 100;
        
        // Penalità per vulnerabilità
        for (SecurityVulnerabilityAnalysis analysis : report.getSecurityVulnerabilityAnalyses()) {
            for (SecurityVulnerability vuln : analysis.getVulnerabilities()) {
                switch (vuln.getSeverity()) {
                    case "CRITICA" -> score -= 25;
                    case "ALTA" -> score -= 15;
                    case "MEDIA" -> score -= 8;
                    case "BASSA" -> score -= 3;
                }
            }
        }
        
        return Math.max(0, score);
    }
    
    private String determineQualityLevel(int score) {
        if (score >= 95) return "ECCELLENTE";
        if (score >= 85) return "BUONO";
        if (score >= 70) return "DISCRETO";
        if (score >= 60) return "SUFFICIENTE";
        return "INSUFFICIENTE";
    }
    
    private boolean isValidationAnnotation(String annotationType) {
        return annotationType.startsWith("javax.validation.constraints.") ||
               annotationType.startsWith("org.hibernate.validator.constraints.") ||
               annotationType.equals("javax.validation.Valid");
    }
}

public class InputValidationReport {
    private List<EndpointParameter> endpointParameters = new ArrayList<>();
    private List<EndpointParameter> unvalidatedParameters = new ArrayList<>();
    private List<EntityField> entityFields = new ArrayList<>();
    private List<CustomValidation> customValidations = new ArrayList<>();
    private List<SanitizationMethod> sanitizationMethods = new ArrayList<>();
    private List<SecurityRisk> securityRisks = new ArrayList<>();
    private List<SecurityVulnerabilityAnalysis> securityVulnerabilityAnalyses = new ArrayList<>();
    private List<CSRFProtectionAnalysis> csrfProtectionAnalyses = new ArrayList<>();
    private ValidationCoverageEvaluation validationCoverageEvaluation;
    private InputValidationQualityScore qualityScore;
    private List<String> errors = new ArrayList<>();
    
    public static class EndpointParameter {
        private String type;
        private String methodName;
        private String className;
        private List<String> validationAnnotations = new ArrayList<>();
        private boolean hasValidAnnotation;
        private boolean validated;
    }
    
    public static class EntityField {
        private String fieldName;
        private String fieldType;
        private String className;
        private List<String> validationAnnotations = new ArrayList<>();
        private Map<String, String> validationConstraints = new HashMap<>();
    }
    
    public static class SecurityRisk {
        private String riskType;
        private String severity;
        private String location;
        private String description;
        private String mitigation;
    }
    
    public static class SecurityVulnerabilityAnalysis {
        private String className;
        private String methodName;
        private List<SecurityVulnerability> vulnerabilities = new ArrayList<>();
        
        public void addVulnerability(String type, String severity, String description, String recommendation) {
            this.vulnerabilities.add(new SecurityVulnerability(type, severity, description, recommendation));
        }
        
        // Getters and setters...
    }
    
    public static class SecurityVulnerability {
        private String type;
        private String severity;
        private String description;
        private String recommendation;
        
        public SecurityVulnerability(String type, String severity, String description, String recommendation) {
            this.type = type;
            this.severity = severity;
            this.description = description;
            this.recommendation = recommendation;
        }
        
        // Getters and setters...
    }
    
    public static class CSRFProtectionAnalysis {
        private String className;
        private String methodName;
        private String httpMethod;
        private boolean hasCSRFProtection;
        private List<String> securityIssues = new ArrayList<>();
        
        public void addSecurityIssue(String issueType, String description) {
            this.securityIssues.add(issueType + ": " + description);
        }
        
        // Getters and setters...
    }
    
    public static class ValidationCoverageEvaluation {
        private double endpointValidationCoverage;
        private double entityValidationCoverage;
        private boolean hasCustomValidation;
        private boolean hasSanitization;
        private long criticalVulnerabilityCount;
        
        // Getters and setters...
    }
    
    public static class InputValidationQualityScore {
        private int overallScore;
        private int validationCoverageScore;
        private int securityScore;
        private int sanitizationScore;
        private int customValidationScore;
        private String qualityLevel;
        private List<QualityIssue> qualityIssues = new ArrayList<>();
        private int totalPenalties;
        private int totalBonuses;
        
        // Getters and setters...
    }
    
    public static class QualityIssue {
        private String issueType;
        private String severity;
        private String description;
        private String recommendation;
        
        public QualityIssue(String issueType, String severity, String description, String recommendation) {
            this.issueType = issueType;
            this.severity = severity;
            this.description = description;
            this.recommendation = recommendation;
        }
        
        // Getters and setters...
    }
    
    // Getter and setter methods
    public void addSecurityVulnerabilityAnalysis(SecurityVulnerabilityAnalysis analysis) {
        this.securityVulnerabilityAnalyses.add(analysis);
    }
    
    public void addCSRFProtectionAnalysis(CSRFProtectionAnalysis analysis) {
        this.csrfProtectionAnalyses.add(analysis);
    }
}
```

## Raccolta Dati

### 1. Endpoint Parameters
- Parametri di controller con/senza validazione
- Tipi di annotazioni di validazione utilizzate
- Parametri @RequestBody, @RequestParam, @PathVariable

### 2. Entity Validation
- Annotazioni JSR-303 su campi entity
- Vincoli di validazione (lunghezza, pattern, range)
- Validazioni cross-field

### 3. Custom Validation Logic
- Metodi di validazione personalizzati
- Logiche di business validation
- Validazioni condizionali

### 4. Security Analysis
- Input non validati che potrebbero essere vulnerabili
- Pattern pericolosi nel codice
- Metodi di sanitizzazione utilizzati

## Metriche di Qualità del Codice

Il sistema di scoring valuta la sicurezza dell'input validation con focus critico su injection prevention, data sanitization e coverage di validazione.

### Algoritmo di Scoring (0-100)

```java
baseScore = 100

// Penalità principali (-15 a -50 punti)
SQL_INJECTION_RISK: -50           // Rischio SQL injection
XSS_VULNERABILITY: -45            // Rischio XSS
UNVALIDATED_INPUT: -40            // Input senza validazione
COMMAND_INJECTION_RISK: -40       // Rischio command injection
PATH_TRAVERSAL_RISK: -35          // Rischio path traversal
MISSING_SANITIZATION: -30         // Mancanza sanitizzazione output
MISSING_CSRF_PROTECTION: -25      // Mancanza protezione CSRF
WEAK_VALIDATION: -25              // Validazione insufficiente
CLIENT_SIDE_VALIDATION_ONLY: -20  // Solo validazione client-side

// Bonus principali (+8 a +25 punti)
PARAMETERIZED_QUERIES: +25        // Uso di prepared statements
WHITELIST_VALIDATION: +20         // Validazione basata su whitelist
COMPREHENSIVE_VALIDATION: +20     // Validazione completa su tutti input
STRONG_INPUT_SANITIZATION: +18    // Sanitizzazione robusta degli input
OUTPUT_ENCODING: +15              // Encoding appropriato dell'output
CSRF_PROTECTION: +15              // Protezione CSRF implementata
FILE_UPLOAD_SECURITY: +15         // Validazione sicura file upload

// Penalità per coverage
if (validationCoverage < 50%) penaltyPoints += 40
if (validationCoverage < 80%) penaltyPoints += 20
if (criticalVulnerabilities > 0) penaltyPoints += 25 per vulnerability

// Bonus per coverage e sanitization
if (validationCoverage > 90%) bonusPoints += 20
if (hasSanitization) bonusPoints += 18
if (hasCustomValidation) bonusPoints += 8

finalScore = max(0, min(100, baseScore - totalPenalties + totalBonuses))
```

### Soglie di Valutazione

| Punteggio | Livello | Descrizione |
|-----------|---------|-------------|
| 95-100 | 🟢 **ECCELLENTE** | Input validation enterprise-grade con tutte le protezioni |
| 85-94  | 🔵 **BUONO** | Validazione robusta con protezioni avanzate implementate |
| 70-84  | 🟡 **DISCRETO** | Validazione adeguata con alcune lacune di sicurezza |
| 60-69  | 🟠 **SUFFICIENTE** | Validazione base con vulnerabilità significative |
| 0-59   | 🔴 **INSUFFICIENTE** | Validazione inadeguata con vulnerabilità critiche |

### Categorie di Problemi per Gravità

#### 🔴 CRITICA (40+ punti penalità)
- **SQL Injection Risk** (-50): Input non sanitizzato utilizzato in query SQL dinamiche
- **XSS Vulnerability** (-45): Output non encodato che permette script injection
- **Unvalidated Input** (-40): Parametri critici senza validazione alcuna
- **Command Injection Risk** (-40): Input utilizzato in esecuzione comandi sistema

#### 🟠 ALTA (25-39 punti penalità)
- **Path Traversal Risk** (-35): Input file path non validato per directory traversal
- **Missing Sanitization** (-30): Mancanza di sanitizzazione output in punti critici
- **Missing CSRF Protection** (-25): Endpoint di modifica senza protezione CSRF
- **Weak Validation** (-25): Validazione presente ma insufficiente o bypassabile

#### 🟡 MEDIA (15-24 punti penalità)
- **Client-Side Only Validation** (-20): Validazione solo lato client senza server-side
- **Inconsistent Validation** (-18): Pattern di validazione inconsistenti
- **Generic Error Messages** (-15): Messaggi di errore che rivelano informazioni sistema

#### 🔵 BASSA (< 15 punti penalità)
- **Suboptimal Validation Patterns** (-10): Pattern di validazione non ottimali
- **Missing Input Normalization** (-8): Mancanza normalizzazione input

### Esempio Output HTML

```html
<div class="input-validation-quality-score">
    <h3>🛡️ Input Validation Security Score: 72/100 (DISCRETO)</h3>
    
    <div class="score-breakdown">
        <div class="metric">
            <span class="label">Validation Coverage:</span>
            <div class="bar"><div class="fill" style="width: 78%"></div></div>
            <span class="value">78%</span>
        </div>
        
        <div class="metric">
            <span class="label">Security Score:</span>
            <div class="bar security-medium"><div class="fill" style="width: 65%"></div></div>
            <span class="value">65%</span>
        </div>
        
        <div class="metric">
            <span class="label">Sanitization Score:</span>
            <div class="bar security-high"><div class="fill" style="width: 85%"></div></div>
            <span class="value">85%</span>
        </div>
        
        <div class="metric">
            <span class="label">Custom Validation:</span>
            <div class="bar"><div class="fill" style="width: 80%"></div></div>
            <span class="value">80%</span>
        </div>
    </div>

    <div class="critical-vulnerabilities">
        <h4>🚨 Vulnerabilità Critiche</h4>
        <div class="vuln critical">
            🔴 <strong>SQL_INJECTION_RISK</strong>: UserController.searchUsers()
            <br>→ <em>Utilizzare prepared statements invece di concatenazione stringhe</em>
        </div>
        
        <div class="vuln high">
            🟠 <strong>XSS_VULNERABILITY</strong>: CommentController.displayComment()
            <br>→ <em>Implementare output encoding per tutti i dati utente</em>
        </div>
        
        <div class="vuln medium">
            🟡 <strong>MISSING_CSRF_PROTECTION</strong>: 3 endpoint POST senza protezione
            <br>→ <em>Abilitare CSRF protection in Spring Security configuration</em>
        </div>
    </div>
    
    <div class="validation-coverage">
        <h4>📊 Validation Coverage</h4>
        <div class="coverage-item">
            <span class="label">Controller Endpoints:</span>
            <div class="coverage-bar">
                <div class="fill" style="width: 78%"></div>
                <span class="percentage">78%</span>
            </div>
        </div>
        
        <div class="coverage-item">
            <span class="label">Entity Fields:</span>
            <div class="coverage-bar">
                <div class="fill" style="width: 85%"></div>
                <span class="percentage">85%</span>
            </div>
        </div>
    </div>
    
    <div class="security-features">
        <h4>🔒 Security Features</h4>
        <ul>
            <li>✅ Bean Validation (JSR-303) implementato</li>
            <li>✅ Custom validation logic presente</li>
            <li>✅ Input sanitization parzialmente implementata</li>
            <li>⚠️ Output encoding: implementazione parziale</li>
            <li>❌ CSRF protection: non configurata</li>
            <li>❌ Content Security Policy: non implementata</li>
        </ul>
    </div>
</div>
```

### Metriche Business Value

#### 🛡️ Security Impact
- **Injection Prevention**: Prevenzione di SQL, XSS, command injection attacks
- **Data Integrity**: Protezione integrità dati attraverso validazione robusta
- **Compliance Achievement**: Conformità a OWASP Top 10 e standard di sicurezza
- **Attack Surface Reduction**: Riduzione superficie di attacco attraverso input sanitization

#### 🔍 Risk Assessment
- **Vulnerability Detection**: Identificazione automatica di pattern vulnerabili
- **Input Security Analysis**: Analisi completa di tutti i punti di ingresso
- **Sanitization Coverage**: Valutazione copertura sanitizzazione output
- **CSRF Protection Status**: Verifica protezione Cross-Site Request Forgery

#### 💰 Operational Benefits
- **Security Incident Prevention**: Prevenzione proattiva di security breach
- **Audit Readiness**: Preparazione per audit di sicurezza applicativa
- **Penetration Test Preparation**: Identificazione precoce di vulnerabilità
- **Regulatory Compliance**: Supporto compliance PCI-DSS, GDPR, HIPAA

#### 👥 Development Impact
- **Secure Coding Practices**: Promozione best practices di sviluppo sicuro
- **Security Awareness**: Educazione team su common vulnerabilities
- **Code Review Enhancement**: Miglioramento process di code review security
- **Testing Integration**: Integrazione security testing nel CI/CD pipeline

### Raccomandazioni Prioritarie

1. **Eliminate SQL Injection Risks**: Sostituire query dinamiche con prepared statements
2. **Implement Output Encoding**: Encodare tutti gli output per prevenire XSS
3. **Enable CSRF Protection**: Configurare protezione CSRF per tutti endpoint di modifica
4. **Comprehensive Input Validation**: Implementare validazione su tutti i parametri di input
5. **Content Security Policy**: Implementare CSP header per additional XSS protection

## Metriche di Valore

- **Sicurezza**: Identifica vulnerabilità di injection e XSS
- **Conformità**: Verifica aderenza alle best practices di sicurezza
- **Qualità**: Misura la robustezza della validazione input
- **Risk Mitigation**: Valuta l'esposizione ai rischi di sicurezza

## Classificazione

**Categoria**: Security & Robustness
**Priorità**: Critica - La validazione input è fondamentale per la sicurezza applicativa
**Stakeholder**: Security team, Development team, QA team