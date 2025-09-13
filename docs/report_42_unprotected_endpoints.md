# Report 42: Endpoint Non Protetti o Pubblici

**Valore**: ⭐⭐⭐⭐⭐ **Complessità**: 🟡 Media **Tempo**: 5-6 giorni
**Tags**: `#security-gaps` `#unprotected-endpoints` `#vulnerability-assessment`

## Descrizione

Analizza tutti gli endpoint REST dell'applicazione per identificare quelli accessibili pubblicamente senza autenticazione o autorizzazione, mappando potenziali vulnerabilità di sicurezza e accessi non autorizzati.

## Sezioni del Report

### 1. Unprotected Endpoints Analysis
- REST endpoint senza security annotations
- Endpoint pubblici per design vs oversight
- Missing authentication/authorization
- Sensitive data exposure risk

### 2. Security Configuration Assessment  
- Spring Security configuration coverage
- Method-level security analysis
- Role-based access control gaps
- CORS configuration vulnerabilities

### 3. Risk Classification
- High risk: Admin/management endpoints
- Medium risk: Business logic endpoints  
- Low risk: Public information endpoints
- Data sensitivity exposure assessment

### 4. Protection Recommendations
- Required security annotations
- Access control implementation
- Authentication mechanism suggestions
- Authorization strategy recommendations

## Implementazione con Javassist

```java
public class UnprotectedEndpointsAnalyzer {
    
    private static final Set<String> SECURITY_ANNOTATIONS = Set.of(
        "org.springframework.security.access.prepost.PreAuthorize",
        "org.springframework.security.access.prepost.PostAuthorize", 
        "org.springframework.security.access.annotation.Secured",
        "javax.annotation.security.RolesAllowed",
        "javax.annotation.security.PermitAll",
        "javax.annotation.security.DenyAll"
    );
    
    public UnprotectedEndpointsReport analyzeUnprotectedEndpoints(CtClass[] classes) {
        UnprotectedEndpointsReport report = new UnprotectedEndpointsReport();
        
        for (CtClass ctClass : classes) {
            if (isControllerClass(ctClass)) {
                analyzeControllerEndpoints(ctClass, report);
            }
        }
        
        analyzeSecurityConfiguration(report);
        classifyEndpointRisks(report);
        
        return report;
    }
    
    private void analyzeControllerEndpoints(CtClass ctClass, UnprotectedEndpointsReport report) {
        try {
            ControllerSecurityInfo controllerInfo = new ControllerSecurityInfo();
            controllerInfo.setClassName(ctClass.getName());
            
            // Class-level security annotations
            SecurityConfiguration classSecurityConfig = analyzeClassSecurity(ctClass);
            controllerInfo.setClassLevelSecurity(classSecurityConfig);
            
            CtMethod[] methods = ctClass.getDeclaredMethods();
            
            for (CtMethod method : methods) {
                if (isEndpointMethod(method)) {
                    EndpointSecurityInfo endpointInfo = analyzeEndpointSecurity(method, classSecurityConfig);
                    controllerInfo.addEndpoint(endpointInfo);
                    
                    // Check if endpoint is unprotected
                    if (!endpointInfo.isProtected()) {
                        UnprotectedEndpoint unprotected = new UnprotectedEndpoint();
                        unprotected.setClassName(ctClass.getName());
                        unprotected.setMethodName(method.getName());
                        unprotected.setEndpointInfo(endpointInfo);
                        unprotected.setRiskLevel(assessEndpointRisk(endpointInfo));
                        
                        report.addUnprotectedEndpoint(unprotected);
                    }
                }
            }
            
            report.addControllerInfo(controllerInfo);
            
        } catch (Exception e) {
            report.addError("Error analyzing controller: " + e.getMessage());
        }
    }
    
    private boolean isControllerClass(CtClass ctClass) {
        return ctClass.hasAnnotation("org.springframework.stereotype.Controller") ||
               ctClass.hasAnnotation("org.springframework.web.bind.annotation.RestController");
    }
    
    private boolean isEndpointMethod(CtMethod method) {
        return method.hasAnnotation("org.springframework.web.bind.annotation.RequestMapping") ||
               method.hasAnnotation("org.springframework.web.bind.annotation.GetMapping") ||
               method.hasAnnotation("org.springframework.web.bind.annotation.PostMapping") ||
               method.hasAnnotation("org.springframework.web.bind.annotation.PutMapping") ||
               method.hasAnnotation("org.springframework.web.bind.annotation.DeleteMapping") ||
               method.hasAnnotation("org.springframework.web.bind.annotation.PatchMapping");
    }
    
    private SecurityConfiguration analyzeClassSecurity(CtClass ctClass) {
        SecurityConfiguration config = new SecurityConfiguration();
        
        // Check for class-level security annotations
        for (String securityAnnotation : SECURITY_ANNOTATIONS) {
            if (ctClass.hasAnnotation(securityAnnotation)) {
                config.setHasClassLevelSecurity(true);
                config.setSecurityAnnotation(securityAnnotation);
                
                try {
                    Annotation annotation = ctClass.getAnnotation(securityAnnotation);
                    config.setSecurityExpression(extractSecurityExpression(annotation));
                } catch (ClassNotFoundException e) {
                    config.addError("Error reading security annotation: " + e.getMessage());
                }
                break;
            }
        }
        
        return config;
    }
    
    private EndpointSecurityInfo analyzeEndpointSecurity(CtMethod method, SecurityConfiguration classConfig) {
        EndpointSecurityInfo info = new EndpointSecurityInfo();
        info.setMethodName(method.getName());
        
        try {
            // Extract endpoint mapping information
            info.setHttpMethods(extractHttpMethods(method));
            info.setPaths(extractEndpointPaths(method));
            
            // Method-level security analysis
            SecurityConfiguration methodSecurity = analyzeMethodSecurity(method);
            info.setMethodLevelSecurity(methodSecurity);
            
            // Determine overall protection status
            boolean isProtected = classConfig.hasClassLevelSecurity() || methodSecurity.hasMethodLevelSecurity();
            info.setProtected(isProtected);
            
            // If not protected, analyze why
            if (!isProtected) {
                analyzeUnprotectedReasons(method, info);
            }
            
            // Analyze parameter security
            analyzeParameterSecurity(method, info);
            
            // Check for sensitive operations
            analyzeSensitiveOperations(method, info);
            
        } catch (Exception e) {
            info.addError("Error analyzing endpoint security: " + e.getMessage());
        }
        
        return info;
    }
    
    private SecurityConfiguration analyzeMethodSecurity(CtMethod method) {
        SecurityConfiguration config = new SecurityConfiguration();
        
        for (String securityAnnotation : SECURITY_ANNOTATIONS) {
            if (method.hasAnnotation(securityAnnotation)) {
                config.setHasMethodLevelSecurity(true);
                config.setSecurityAnnotation(securityAnnotation);
                
                try {
                    Annotation annotation = method.getAnnotation(securityAnnotation);
                    config.setSecurityExpression(extractSecurityExpression(annotation));
                } catch (ClassNotFoundException e) {
                    config.addError("Error reading method security annotation: " + e.getMessage());
                }
                break;
            }
        }
        
        return config;
    }
    
    private void analyzeUnprotectedReasons(CtMethod method, EndpointSecurityInfo info) {
        List<String> reasons = new ArrayList<>();
        
        // Check if it might be intentionally public
        if (method.getName().toLowerCase().contains("public") || 
            method.getName().toLowerCase().contains("open") ||
            method.getName().toLowerCase().contains("health")) {
            reasons.add("Potentially intended as public endpoint");
        }
        
        // Check for actuator endpoints
        try {
            CtClass declaringClass = method.getDeclaringClass();
            if (declaringClass.getName().contains("actuator")) {
                reasons.add("Actuator endpoint - review if should be protected");
            }
        } catch (Exception e) {
            // Ignore
        }
        
        // Check HTTP methods - GET might be more acceptable as public
        Set<String> httpMethods = info.getHttpMethods();
        if (httpMethods.contains("POST") || httpMethods.contains("PUT") || 
            httpMethods.contains("DELETE")) {
            reasons.add("Modifying HTTP method without protection");
        }
        
        if (reasons.isEmpty()) {
            reasons.add("No security annotations found");
        }
        
        info.setUnprotectedReasons(reasons);
    }
    
    private void analyzeParameterSecurity(CtMethod method, EndpointSecurityInfo info) {
        try {
            CtClass[] paramTypes = method.getParameterTypes();
            Annotation[][] paramAnnotations = method.getParameterAnnotations();
            
            for (int i = 0; i < paramTypes.length; i++) {
                ParameterSecurityInfo paramInfo = new ParameterSecurityInfo();
                paramInfo.setParameterType(paramTypes[i].getName());
                paramInfo.setParameterIndex(i);
                
                // Check for validation annotations
                for (Annotation annotation : paramAnnotations[i]) {
                    String annotationType = annotation.getTypeName();
                    
                    if (annotationType.startsWith("javax.validation") || 
                        annotationType.startsWith("org.hibernate.validator")) {
                        paramInfo.setHasValidation(true);
                        paramInfo.addValidationAnnotation(annotationType);
                    }
                    
                    // Check for @PathVariable, @RequestParam security
                    if ("org.springframework.web.bind.annotation.PathVariable".equals(annotationType) ||
                        "org.springframework.web.bind.annotation.RequestParam".equals(annotationType)) {
                        paramInfo.setUserControllable(true);
                    }
                }
                
                info.addParameterInfo(paramInfo);
            }
            
        } catch (NotFoundException e) {
            info.addError("Error analyzing parameters: " + e.getMessage());
        }
    }
    
    private void analyzeSensitiveOperations(CtMethod method, EndpointSecurityInfo info) {
        try {
            method.instrument(new ExprEditor() {
                @Override
                public void edit(MethodCall call) throws CannotCompileException {
                    String className = call.getClassName();
                    String methodName = call.getMethodName();
                    
                    // Database operations
                    if (isDatabaseOperation(className, methodName)) {
                        info.addSensitiveOperation("Database access: " + className + "." + methodName);
                    }
                    
                    // File system operations
                    if (isFileSystemOperation(className, methodName)) {
                        info.addSensitiveOperation("File system access: " + className + "." + methodName);
                    }
                    
                    // External service calls
                    if (isExternalServiceCall(className, methodName)) {
                        info.addSensitiveOperation("External service call: " + className + "." + methodName);
                    }
                    
                    // Admin operations
                    if (isAdminOperation(className, methodName)) {
                        info.addSensitiveOperation("Administrative operation: " + className + "." + methodName);
                        info.setHighRiskOperation(true);
                    }
                }
            });
            
        } catch (CannotCompileException e) {
            info.addError("Error analyzing sensitive operations: " + e.getMessage());
        }
    }
    
    private RiskLevel assessEndpointRisk(EndpointSecurityInfo endpointInfo) {
        // High risk criteria
        if (endpointInfo.isHighRiskOperation()) {
            return RiskLevel.CRITICAL;
        }
        
        if (endpointInfo.getHttpMethods().contains("DELETE")) {
            return RiskLevel.HIGH;
        }
        
        if (endpointInfo.getHttpMethods().contains("POST") || 
            endpointInfo.getHttpMethods().contains("PUT")) {
            return RiskLevel.HIGH;
        }
        
        if (!endpointInfo.getSensitiveOperations().isEmpty()) {
            return RiskLevel.HIGH;
        }
        
        // Medium risk criteria
        if (endpointInfo.getPaths().stream().anyMatch(path -> 
            path.contains("admin") || path.contains("management") || path.contains("config"))) {
            return RiskLevel.MEDIUM;
        }
        
        // Check for user-controllable parameters without validation
        boolean hasUnvalidatedUserInput = endpointInfo.getParameterInfos().stream()
            .anyMatch(param -> param.isUserControllable() && !param.hasValidation());
        
        if (hasUnvalidatedUserInput) {
            return RiskLevel.MEDIUM;
        }
        
        // Low risk for GET endpoints with minimal sensitive operations
        if (endpointInfo.getHttpMethods().size() == 1 && 
            endpointInfo.getHttpMethods().contains("GET")) {
            return RiskLevel.LOW;
        }
        
        return RiskLevel.MEDIUM;
    }
    
    private boolean isDatabaseOperation(String className, String methodName) {
        return className.contains("Repository") || 
               className.contains("EntityManager") ||
               className.contains("JdbcTemplate") ||
               methodName.startsWith("save") || 
               methodName.startsWith("delete") || 
               methodName.startsWith("update");
    }
    
    private boolean isFileSystemOperation(String className, String methodName) {
        return className.contains("File") || 
               className.contains("Path") ||
               methodName.contains("read") || 
               methodName.contains("write") || 
               methodName.contains("delete");
    }
    
    private boolean isExternalServiceCall(String className, String methodName) {
        return className.contains("RestTemplate") || 
               className.contains("WebClient") ||
               className.contains("HttpClient") ||
               className.contains("Service") && methodName.contains("call");
    }
    
    private boolean isAdminOperation(String className, String methodName) {
        return className.toLowerCase().contains("admin") || 
               methodName.toLowerCase().contains("admin") ||
               methodName.toLowerCase().contains("manage") ||
               methodName.toLowerCase().contains("config");
    }
}

public class UnprotectedEndpointsReport {
    private List<UnprotectedEndpoint> unprotectedEndpoints = new ArrayList<>();
    private List<ControllerSecurityInfo> controllerInfos = new ArrayList<>();
    private SecurityStatistics securityStatistics;
    private List<String> errors = new ArrayList<>();
    
    public static class UnprotectedEndpoint {
        private String className;
        private String methodName;
        private EndpointSecurityInfo endpointInfo;
        private RiskLevel riskLevel;
        private List<String> recommendations = new ArrayList<>();
    }
    
    public static class EndpointSecurityInfo {
        private String methodName;
        private Set<String> httpMethods = new HashSet<>();
        private Set<String> paths = new HashSet<>();
        private boolean isProtected;
        private SecurityConfiguration methodLevelSecurity;
        private List<String> unprotectedReasons = new ArrayList<>();
        private List<ParameterSecurityInfo> parameterInfos = new ArrayList<>();
        private List<String> sensitiveOperations = new ArrayList<>();
        private boolean highRiskOperation;
        private List<String> errors = new ArrayList<>();
    }
    
    public enum RiskLevel {
        CRITICAL, HIGH, MEDIUM, LOW
    }
}
```

## Raccolta Dati

### 1. Endpoint Mapping Analysis
- REST controller classes identification
- HTTP method mappings (@GetMapping, @PostMapping, etc.)
- URL path patterns and parameters
- Request/Response body analysis

### 2. Security Annotation Detection
- Method-level security (@PreAuthorize, @Secured, etc.)
- Class-level security configurations
- Role-based access control patterns
- Custom security annotations

### 3. Risk Assessment
- Sensitive operation detection in endpoint methods
- Parameter validation analysis
- HTTP method risk classification
- Data exposure potential evaluation

### 4. Protection Gap Analysis
- Missing authentication requirements
- Insufficient authorization checks
- Public access by design vs oversight
- Security configuration inheritance

## Metriche di Qualità del Codice

### Algoritmo di Calcolo (0-100)

```java
public int calculateEndpointSecurityScore(UnprotectedEndpointsReport result) {
    double score = 100.0;
    
    // Penalizzazioni per endpoint non protetti critici
    score -= result.getCriticalUnprotectedEndpoints() * 30;     // -30 per endpoint critici non protetti
    score -= result.getHighRiskUnprotectedEndpoints() * 20;     // -20 per endpoint high-risk non protetti  
    score -= result.getMediumRiskUnprotectedEndpoints() * 10;   // -10 per endpoint medium-risk non protetti
    score -= result.getDeleteEndpointsUnprotected() * 25;       // -25 per DELETE endpoint non protetti
    score -= result.getPostPutEndpointsUnprotected() * 15;      // -15 per POST/PUT endpoint non protetti
    score -= result.getAdminEndpointsUnprotected() * 35;        // -35 per endpoint admin non protetti
    score -= result.getUnvalidatedUserInput() * 12;             // -12 per input utente non validati
    score -= result.getSensitiveDataExposure() * 18;            // -18 per potenziale esposizione dati sensibili
    
    // Bonus per buone pratiche sicurezza
    score += result.getMethodLevelSecurityRatio() * 10;         // +10 per alta % method-level security
    score += result.getValidatedParametersRatio() * 5;          // +5 per parametri validati
    score += result.getProperRoleBasedAccess() * 3;             // +3 per RBAC appropriato
    score += result.getSecurityAnnotationConsistency() * 2;     // +2 per consistenza annotazioni
    
    return Math.max(0, Math.min(100, (int) score));
}
```

### Soglie di Valutazione
- **0-40**: 🔴 CRITICO - Gravi lacune sicurezza con endpoint critici esposti
- **41-60**: 🟡 SUFFICIENTE - Sicurezza di base ma con gap significativi
- **61-80**: 🟢 BUONO - Buona protezione endpoint con minor miglioramenti
- **81-100**: ⭐ ECCELLENTE - Protezione endpoint completa e robusta

## Segnalazioni per Gravità

### 🔴 GRAVITÀ CRITICA (Score Impact: -25 to -35)
1. **Endpoint amministrativi non protetti**
   - Descrizione: Endpoint /admin, /manage, /config accessibili senza autenticazione
   - Rischio: Compromissione sistema, accesso controlli amministrativi
   - Soluzione: Aggiungere @PreAuthorize("hasRole('ADMIN')") o equivalente

2. **DELETE endpoint non protetti**
   - Descrizione: Endpoint di cancellazione accessibili pubblicamente
   - Rischio: Perdita dati, sabotaggio, cancellazioni non autorizzate
   - Soluzione: Implementare autenticazione e autorizzazione appropriata

3. **Endpoint critici con operazioni sensibili**
   - Descrizione: Endpoint che accedono DB/filesystem senza protezione
   - Rischio: Data breach, modifiche non autorizzate, privilege escalation
   - Soluzione: Implementare security layer con role-based access control

### 🟠 GRAVITÀ ALTA (Score Impact: -15 to -20)
4. **POST/PUT endpoint non protetti**
   - Descrizione: Endpoint di creazione/modifica senza autenticazione
   - Rischio: Creazione contenuto non autorizzato, modifiche dati
   - Soluzione: Aggiungere autenticazione user e validazione autorizzazioni

5. **High-risk operation endpoint pubblici**
   - Descrizione: Endpoint con operazioni business-critical accessibili pubblicamente
   - Rischio: Operazioni non autorizzate, bypass business logic
   - Soluzione: Implementare authentication e fine-grained authorization

### 🟡 GRAVITÀ MEDIA (Score Impact: -10 to -12)
6. **Input utente non validato**
   - Descrizione: Parametri @PathVariable/@RequestParam senza validazione
   - Rischio: Injection attacks, malformed data processing
   - Soluzione: Aggiungere validation annotations (@Valid, @NotNull, etc.)

7. **Medium-risk endpoint non protetti**
   - Descrizione: Endpoint business con protezione inadeguata
   - Rischio: Accesso non autorizzato a funzionalità business
   - Soluzione: Review e implementare appropriate security measures

### 🔵 GRAVITÀ BASSA (Score Impact: -1 to -5)
8. **GET endpoint informativi non protetti**
   - Descrizione: Endpoint read-only per informazioni pubbliche
   - Rischio: Information disclosure minimo per dati non sensibili
   - Soluzione: Review se informazioni dovrebbero rimanere pubbliche

## Metriche di Valore

- **Security Posture**: Migliora postura sicurezza identificando gap protezione
- **Risk Mitigation**: Riduce rischio accessi non autorizzati e data breach
- **Compliance**: Facilita compliance con security standard e regulations
- **Attack Surface**: Riduce superficie attacco proteggendo endpoint critici

## Classificazione

**Categoria**: Security & Robustness  
**Priorità**: Critica - Protezione endpoint è fondamentale per sicurezza applicazione
**Stakeholder**: Security team, Development team, DevSecOps

## Tags per Classificazione

`#security-gaps` `#unprotected-endpoints` `#vulnerability-assessment` `#access-control` `#authentication` `#authorization` `#risk-assessment` `#critical-security`