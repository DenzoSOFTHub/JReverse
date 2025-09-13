# Report 06: Mappa dei Controller REST e Relativi Metodi

## Descrizione
Catalogazione dettagliata di tutti i controller REST (@RestController, @Controller) con analisi completa dei metodi handler, mapping delle richieste, parametri, e configurazioni di sicurezza.

## Valore per l'Utente
**⭐⭐⭐⭐⭐** - Valore Massimo
- Inventario completo dell'API REST
- Documentazione automatica degli endpoint
- Analisi della consistenza dell'API design
- Identificazione di gap di sicurezza
- Base per testing automatizzato

## Complessità di Implementazione
**🟡 Media** - Analisi diretta di annotazioni Spring MVC

## Tempo di Realizzazione Stimato
**5-7 giorni** di sviluppo

## Sezioni del Report

### 1. Executive Summary
- Numero totale di controller
- Distribuzione endpoint per controller
- Copertura CRUD operations
- Pattern di naming analysis
- Security coverage overview

### 2. Controller Inventory
```
REST Controllers Overview
├── UserController (8 endpoints)
│   ├── Base Path: /api/v1/users
│   ├── Security: Role-based
│   └── CRUD: Complete
├── OrderController (12 endpoints)
│   ├── Base Path: /api/v1/orders  
│   ├── Security: JWT + Role
│   └── CRUD: Complete
└── AdminController (5 endpoints)
    ├── Base Path: /admin
    ├── Security: Admin only
    └── CRUD: Read/Update only
```

### 3. Detailed Controller Analysis

#### 3.1 Per ogni Controller:
```java
@RestController
@RequestMapping("/api/v1/users")
@PreAuthorize("hasRole('USER')")
@Validated
public class UserController {
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or @userService.isOwner(#id, authentication.name)")
    public ResponseEntity<UserDto> getUser(@PathVariable Long id) { ... }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserRequest request) { ... }
}
```

#### 3.2 Analisi per ogni Endpoint:
- **HTTP Method & Path**: Complete URL mapping
- **Handler Method**: Java method signature
- **Request Parameters**: Path vars, query params, body
- **Response Type**: Return type and status codes
- **Security**: Authentication/authorization requirements
- **Validation**: Input validation rules
- **Exception Handling**: Error response mapping

### 4. API Design Patterns Analysis

#### 4.1 RESTful Compliance:
- **Resource Identification**: /users/{id}
- **HTTP Method Usage**: GET, POST, PUT, DELETE
- **Status Code Consistency**: 200, 201, 404, etc.
- **Content Negotiation**: JSON, XML support

#### 4.2 Naming Conventions:
- Controller class naming patterns
- Endpoint URL patterns
- Method naming consistency
- Parameter naming standards

## Implementazione Javassist

### Identificazione Controller Classes

```java
public class RestControllerAnalyzer {
    
    private final ClassPool classPool;
    private final List<ControllerInfo> controllers = new ArrayList<>();
    
    public ControllerAnalysis analyzeRestControllers() {
        // 1. Trova tutti i controller
        findAllControllers();
        
        // 2. Analizza ogni controller in dettaglio
        for (ControllerInfo controller : controllers) {
            analyzeControllerDetails(controller);
        }
        
        // 3. Analizza pattern e consistenza
        analyzeApiDesignPatterns();
        
        return new ControllerAnalysis(controllers);
    }
    
    private void findAllControllers() {
        for (CtClass clazz : classPool.getAllClasses()) {
            if (isControllerClass(clazz)) {
                ControllerInfo controllerInfo = createControllerInfo(clazz);
                controllers.add(controllerInfo);
            }
        }
    }
    
    private boolean isControllerClass(CtClass clazz) {
        return clazz.hasAnnotation("org.springframework.web.bind.annotation.RestController") ||
               clazz.hasAnnotation("org.springframework.stereotype.Controller");
    }
    
    private ControllerInfo createControllerInfo(CtClass clazz) {
        ControllerInfo info = new ControllerInfo();
        info.setClassName(clazz.getName());
        info.setSimpleName(clazz.getSimpleName());
        info.setPackageName(clazz.getPackageName());
        
        // Determina il tipo di controller
        if (clazz.hasAnnotation("org.springframework.web.bind.annotation.RestController")) {
            info.setControllerType(ControllerType.REST_CONTROLLER);
        } else {
            info.setControllerType(ControllerType.CONTROLLER);
        }
        
        // Analizza @RequestMapping a livello di classe
        analyzeClassLevelMapping(clazz, info);
        
        // Analizza security annotations
        analyzeClassLevelSecurity(clazz, info);
        
        return info;
    }
}
```

### Analisi Request Mapping

```java
private void analyzeClassLevelMapping(CtClass clazz, ControllerInfo info) {
    if (clazz.hasAnnotation("org.springframework.web.bind.annotation.RequestMapping")) {
        try {
            Annotation requestMapping = clazz.getAnnotation("RequestMapping");
            
            // Base paths
            String[] paths = getAnnotationArrayValue(requestMapping, "value");
            if (paths.length == 0) {
                paths = getAnnotationArrayValue(requestMapping, "path");
            }
            info.setBasePaths(Arrays.asList(paths));
            
            // HTTP methods (se specificati a livello di classe)
            String[] methods = getAnnotationArrayValue(requestMapping, "method");
            if (methods.length > 0) {
                info.setDefaultHttpMethods(Arrays.asList(methods));
            }
            
            // Consumes/Produces
            String[] consumes = getAnnotationArrayValue(requestMapping, "consumes");
            String[] produces = getAnnotationArrayValue(requestMapping, "produces");
            
            info.setDefaultConsumes(Arrays.asList(consumes));
            info.setDefaultProduces(Arrays.asList(produces));
            
        } catch (ClassNotFoundException e) {
            logger.warn("Error analyzing @RequestMapping for {}", clazz.getName(), e);
        }
    }
}

private void analyzeControllerDetails(ControllerInfo controllerInfo) {
    try {
        CtClass clazz = classPool.get(controllerInfo.getClassName());
        
        // Analizza tutti i metodi handler
        for (CtMethod method : clazz.getDeclaredMethods()) {
            if (isHandlerMethod(method)) {
                EndpointInfo endpoint = analyzeHandlerMethod(method, controllerInfo);
                controllerInfo.addEndpoint(endpoint);
            }
        }
        
        // Calcola statistiche per il controller
        calculateControllerMetrics(controllerInfo);
        
    } catch (NotFoundException e) {
        logger.error("Controller class not found: {}", controllerInfo.getClassName(), e);
    }
}

private boolean isHandlerMethod(CtMethod method) {
    return method.hasAnnotation("org.springframework.web.bind.annotation.RequestMapping") ||
           method.hasAnnotation("org.springframework.web.bind.annotation.GetMapping") ||
           method.hasAnnotation("org.springframework.web.bind.annotation.PostMapping") ||
           method.hasAnnotation("org.springframework.web.bind.annotation.PutMapping") ||
           method.hasAnnotation("org.springframework.web.bind.annotation.DeleteMapping") ||
           method.hasAnnotation("org.springframework.web.bind.annotation.PatchMapping");
}
```

### Analisi Handler Methods

```java
private EndpointInfo analyzeHandlerMethod(CtMethod method, ControllerInfo controllerInfo) {
    EndpointInfo endpoint = new EndpointInfo();
    endpoint.setMethodName(method.getName());
    endpoint.setControllerClass(controllerInfo.getClassName());
    
    try {
        // Analizza return type
        CtClass returnType = method.getReturnType();
        endpoint.setReturnType(analyzeReturnType(returnType));
        
        // Analizza parametri
        endpoint.setParameters(analyzeMethodParameters(method));
        
        // Analizza mapping annotations
        analyzeMappingAnnotations(method, endpoint, controllerInfo);
        
        // Analizza security
        analyzeEndpointSecurity(method, endpoint);
        
        // Analizza validation
        analyzeValidationAnnotations(method, endpoint);
        
        // Analizza exception handling
        analyzeExceptionHandling(method, endpoint);
        
    } catch (NotFoundException e) {
        logger.warn("Error analyzing handler method: {}", method.getLongName(), e);
    }
    
    return endpoint;
}

private void analyzeMappingAnnotations(CtMethod method, EndpointInfo endpoint, ControllerInfo controllerInfo) {
    // @GetMapping
    if (method.hasAnnotation("org.springframework.web.bind.annotation.GetMapping")) {
        analyzeMappingAnnotation(method, "GetMapping", HttpMethod.GET, endpoint, controllerInfo);
    }
    // @PostMapping  
    else if (method.hasAnnotation("org.springframework.web.bind.annotation.PostMapping")) {
        analyzeMappingAnnotation(method, "PostMapping", HttpMethod.POST, endpoint, controllerInfo);
    }
    // @PutMapping
    else if (method.hasAnnotation("org.springframework.web.bind.annotation.PutMapping")) {
        analyzeMappingAnnotation(method, "PutMapping", HttpMethod.PUT, endpoint, controllerInfo);
    }
    // @DeleteMapping
    else if (method.hasAnnotation("org.springframework.web.bind.annotation.DeleteMapping")) {
        analyzeMappingAnnotation(method, "DeleteMapping", HttpMethod.DELETE, endpoint, controllerInfo);
    }
    // @RequestMapping (generic)
    else if (method.hasAnnotation("org.springframework.web.bind.annotation.RequestMapping")) {
        analyzeRequestMapping(method, endpoint, controllerInfo);
    }
}

private void analyzeMappingAnnotation(CtMethod method, String annotationName, HttpMethod httpMethod, 
                                     EndpointInfo endpoint, ControllerInfo controllerInfo) {
    try {
        Annotation annotation = method.getAnnotation("org.springframework.web.bind.annotation." + annotationName);
        
        // Paths
        String[] paths = getAnnotationArrayValue(annotation, "value");
        if (paths.length == 0) {
            paths = getAnnotationArrayValue(annotation, "path");
        }
        
        // Combina base paths del controller con method paths
        List<String> fullPaths = combineBasePaths(controllerInfo.getBasePaths(), Arrays.asList(paths));
        endpoint.setPaths(fullPaths);
        endpoint.setHttpMethod(httpMethod);
        
        // Consumes/Produces
        String[] consumes = getAnnotationArrayValue(annotation, "consumes");
        String[] produces = getAnnotationArrayValue(annotation, "produces");
        
        endpoint.setConsumes(Arrays.asList(consumes));
        endpoint.setProduces(Arrays.asList(produces));
        
        // Headers
        String[] headers = getAnnotationArrayValue(annotation, "headers");
        endpoint.setRequiredHeaders(Arrays.asList(headers));
        
        // Params
        String[] params = getAnnotationArrayValue(annotation, "params");
        endpoint.setRequiredParams(Arrays.asList(params));
        
    } catch (ClassNotFoundException e) {
        logger.warn("Error analyzing {} annotation", annotationName, e);
    }
}
```

### Analisi Parameters

```java
private List<ParameterInfo> analyzeMethodParameters(CtMethod method) throws NotFoundException {
    List<ParameterInfo> parameters = new ArrayList<>();
    
    CtClass[] paramTypes = method.getParameterTypes();
    Annotation[][] paramAnnotations = method.getParameterAnnotations();
    
    for (int i = 0; i < paramTypes.length; i++) {
        ParameterInfo param = new ParameterInfo();
        param.setType(paramTypes[i].getName());
        param.setPosition(i);
        
        // Analizza annotazioni del parametro
        for (Annotation annotation : paramAnnotations[i]) {
            analyzeParameterAnnotation(annotation, param);
        }
        
        // Se non ha annotazioni specifiche, potrebbe essere request body
        if (param.getSource() == null) {
            param.setSource(ParameterSource.REQUEST_BODY);
        }
        
        parameters.add(param);
    }
    
    return parameters;
}

private void analyzeParameterAnnotation(Annotation annotation, ParameterInfo param) {
    String annotationType = annotation.getTypeName();
    
    switch (annotationType) {
        case "org.springframework.web.bind.annotation.PathVariable":
            param.setSource(ParameterSource.PATH_VARIABLE);
            param.setName(getAnnotationValue(annotation, "value"));
            param.setRequired(getAnnotationBooleanValue(annotation, "required", true));
            break;
            
        case "org.springframework.web.bind.annotation.RequestParam":
            param.setSource(ParameterSource.REQUEST_PARAM);
            param.setName(getAnnotationValue(annotation, "value"));
            param.setRequired(getAnnotationBooleanValue(annotation, "required", true));
            param.setDefaultValue(getAnnotationValue(annotation, "defaultValue"));
            break;
            
        case "org.springframework.web.bind.annotation.RequestHeader":
            param.setSource(ParameterSource.REQUEST_HEADER);
            param.setName(getAnnotationValue(annotation, "value"));
            param.setRequired(getAnnotationBooleanValue(annotation, "required", true));
            break;
            
        case "org.springframework.web.bind.annotation.RequestBody":
            param.setSource(ParameterSource.REQUEST_BODY);
            param.setRequired(getAnnotationBooleanValue(annotation, "required", true));
            break;
            
        case "org.springframework.web.bind.annotation.CookieValue":
            param.setSource(ParameterSource.COOKIE);
            param.setName(getAnnotationValue(annotation, "value"));
            break;
            
        case "javax.validation.Valid":
        case "org.springframework.validation.annotation.Validated":
            param.setValidated(true);
            break;
    }
}
```

### Analisi Security e Validation

```java
private void analyzeEndpointSecurity(CtMethod method, EndpointInfo endpoint) {
    SecurityInfo security = new SecurityInfo();
    
    // @PreAuthorize
    if (method.hasAnnotation("org.springframework.security.access.prepost.PreAuthorize")) {
        try {
            Annotation preAuth = method.getAnnotation("PreAuthorize");
            String expression = getAnnotationValue(preAuth, "value");
            security.setPreAuthorizeExpression(expression);
            security.setSecurityLevel(SecurityLevel.METHOD_LEVEL);
        } catch (ClassNotFoundException e) {
            logger.warn("Error analyzing @PreAuthorize", e);
        }
    }
    
    // @Secured
    if (method.hasAnnotation("org.springframework.security.access.annotation.Secured")) {
        try {
            Annotation secured = method.getAnnotation("Secured");
            String[] roles = getAnnotationArrayValue(secured, "value");
            security.setRequiredRoles(Arrays.asList(roles));
            security.setSecurityLevel(SecurityLevel.ROLE_BASED);
        } catch (ClassNotFoundException e) {
            logger.warn("Error analyzing @Secured", e);
        }
    }
    
    // Se non ha security a livello di metodo, eredita dal controller
    if (security.getSecurityLevel() == SecurityLevel.NONE) {
        security = inheritClassLevelSecurity(method.getDeclaringClass());
    }
    
    endpoint.setSecurityInfo(security);
}

private void analyzeValidationAnnotations(CtMethod method, EndpointInfo endpoint) {
    ValidationInfo validation = new ValidationInfo();
    
    // Cerca @Validated sul metodo
    if (method.hasAnnotation("org.springframework.validation.annotation.Validated")) {
        validation.setMethodValidationEnabled(true);
    }
    
    // Analizza i parametri per validation annotations
    try {
        CtClass[] paramTypes = method.getParameterTypes();
        Annotation[][] paramAnnotations = method.getParameterAnnotations();
        
        for (int i = 0; i < paramTypes.length; i++) {
            for (Annotation annotation : paramAnnotations[i]) {
                if (isValidationAnnotation(annotation.getTypeName())) {
                    validation.addValidatedParameter(i, annotation.getTypeName());
                }
            }
        }
        
    } catch (NotFoundException e) {
        logger.warn("Error analyzing validation annotations", e);
    }
    
    endpoint.setValidationInfo(validation);
}

private boolean isValidationAnnotation(String annotationType) {
    return annotationType.startsWith("javax.validation.constraints.") ||
           annotationType.startsWith("org.hibernate.validator.constraints.") ||
           annotationType.equals("javax.validation.Valid") ||
           annotationType.equals("org.springframework.validation.annotation.Validated");
}
```

### API Design Pattern Analysis

```java
private ApiDesignAnalysis analyzeApiDesignPatterns() {
    ApiDesignAnalysis analysis = new ApiDesignAnalysis();
    
    // 1. Analyze RESTful compliance
    RestfulComplianceScore restScore = calculateRestfulCompliance();
    analysis.setRestfulCompliance(restScore);
    
    // 2. Analyze naming consistency
    NamingConsistencyScore namingScore = analyzeNamingConsistency();
    analysis.setNamingConsistency(namingScore);
    
    // 3. Analyze HTTP status code usage
    StatusCodeUsage statusUsage = analyzeStatusCodePatterns();
    analysis.setStatusCodeUsage(statusUsage);
    
    // 4. Analyze security patterns
    SecurityPatternAnalysis securityPatterns = analyzeSecurityPatterns();
    analysis.setSecurityPatterns(securityPatterns);
    
    return analysis;
}

private RestfulComplianceScore calculateRestfulCompliance() {
    RestfulComplianceScore score = new RestfulComplianceScore();
    int totalEndpoints = 0;
    int restfulEndpoints = 0;
    
    for (ControllerInfo controller : controllers) {
        for (EndpointInfo endpoint : controller.getEndpoints()) {
            totalEndpoints++;
            
            if (isRestfulEndpoint(endpoint)) {
                restfulEndpoints++;
            } else {
                score.addViolation(new RestViolation(endpoint, detectRestViolationType(endpoint)));
            }
        }
    }
    
    score.setComplianceRatio((double) restfulEndpoints / totalEndpoints);
    return score;
}

private boolean isRestfulEndpoint(EndpointInfo endpoint) {
    // Verifica pattern RESTful:
    // 1. Resource-based URLs
    // 2. Appropriate HTTP methods
    // 3. Consistent response codes
    // 4. Proper content negotiation
    
    return hasResourceBasedUrl(endpoint) &&
           hasAppropriateHttpMethod(endpoint) &&
           hasConsistentResponseHandling(endpoint);
}
```

## Struttura Output Report

```json
{
  "summary": {
    "totalControllers": 12,
    "totalEndpoints": 87,
    "controllerTypes": {
      "restController": 10,
      "controller": 2
    },
    "httpMethodDistribution": {
      "GET": 35,
      "POST": 23,
      "PUT": 15,
      "DELETE": 12,
      "PATCH": 2
    },
    "securityCoverage": {
      "secured": 75,
      "public": 12
    }
  },
  "controllers": [
    {
      "className": "com.example.UserController",
      "type": "REST_CONTROLLER",
      "basePath": "/api/v1/users",
      "endpointCount": 8,
      "securityLevel": "ROLE_BASED",
      "endpoints": [
        {
          "path": "/api/v1/users/{id}",
          "httpMethod": "GET",
          "handlerMethod": "getUser",
          "parameters": [
            {
              "name": "id",
              "type": "Long",
              "source": "PATH_VARIABLE",
              "required": true
            }
          ],
          "returnType": {
            "type": "ResponseEntity<UserDto>",
            "statusCodes": [200, 404, 403]
          },
          "security": {
            "preAuthorize": "hasRole('USER')",
            "level": "METHOD_LEVEL"
          },
          "validation": {
            "enabled": false
          }
        }
      ]
    }
  ],
  "apiDesignAnalysis": {
    "restfulCompliance": {
      "score": 0.85,
      "violations": [
        {
          "endpoint": "/users/search",
          "issue": "Non-RESTful action endpoint",
          "suggestion": "Use GET /users?search=term"
        }
      ]
    },
    "namingConsistency": {
      "score": 0.92,
      "issues": ["Mixed camelCase/snake_case in parameters"]
    },
    "securityPatterns": {
      "hasGlobalSecurity": true,
      "methodLevelSecurity": 45,
      "publicEndpoints": 12,
      "securityGaps": []
    }
  }
}
```

## Metriche di Qualità del Codice

### Algoritmo di Calcolo (0-100)

```java
public int calculateRestControllerQualityScore(RestControllerReport result) {
    double score = 100.0;
    
    // Penalizzazioni per problemi REST API design critici
    score -= result.getUnprotectedEndpoints() * 18;           // -18 per endpoint pubblici non intenzionali
    score -= result.getNonRestfulEndpoints() * 12;            // -12 per endpoint non RESTful
    score -= result.getMissingValidationEndpoints() * 8;      // -8 per endpoint senza validazione input
    score -= result.getInconsistentNaming() * 6;              // -6 per naming inconsistente
    score -= result.getImproperlHttpMethods() * 10;           // -10 per uso scorretto HTTP methods
    score -= result.getMissingErrorHandling() * 15;           // -15 per mancanza error handling appropriato
    score -= result.getOverComplexEndpoints() * 5;            // -5 per endpoint troppo complessi
    score -= result.getMissingContentNegotiation() * 4;       // -4 per mancanza content negotiation
    
    // Bonus per buone pratiche REST API
    score += result.getProperRestfulDesign() * 3;             // +3 per design RESTful appropriato
    score += result.getComprehensiveSecurity() * 2;           // +2 per sicurezza endpoint completa
    score += result.getConsistentApiDesign() * 2;             // +2 per design API consistente
    score += result.getProperValidation() * 1;                // +1 per validazione input appropriata
    
    return Math.max(0, Math.min(100, (int) score));
}
```

### Soglie di Valutazione
- **0-40**: 🔴 CRITICO - API REST con gravi problemi di design e sicurezza
- **41-60**: 🟡 SUFFICIENTE - API funzionale ma non segue best practices
- **61-80**: 🟢 BUONO - API ben progettata con minor miglioramenti
- **81-100**: ⭐ ECCELLENTE - API REST ottimale con design pattern appropriati

## Segnalazioni per Gravità

### 🔴 GRAVITÀ CRITICA (Score Impact: -15 to -18)
1. **Endpoint pubblici non protetti**
   - Descrizione: Endpoint REST accessibili senza autenticazione appropriata
   - Rischio: Accesso non autorizzato ai dati, violazioni sicurezza
   - Soluzione: Implementare @PreAuthorize, @Secured o security configuration

2. **Mancanza error handling completo**
   - Descrizione: Endpoint senza gestione appropriata errori e exception
   - Rischio: Information disclosure, poor user experience, debugging difficile
   - Soluzione: Implementare @ExceptionHandler, ResponseStatusException, global exception handling

### 🟠 GRAVITÀ ALTA (Score Impact: -10 to -12)
3. **Design non-RESTful**
   - Descrizione: Endpoint che non seguono principi REST (azioni in URL, HTTP methods inappropriati)
   - Rischio: API inconsistente, difficile integrazione, maintenance problematico
   - Soluzione: Ristrutturare endpoint per seguire resource-based design, HTTP methods appropriati

4. **Uso improprio HTTP methods**
   - Descrizione: GET per operazioni che modificano stato, POST per operazioni idempotent
   - Rischio: Caching issues, semantic incorretta, integration problems
   - Soluzione: Usare GET per read, POST per create, PUT per update, DELETE per remove

### 🟡 GRAVITÀ MEDIA (Score Impact: -6 to -8)
5. **Validazione input mancante**
   - Descrizione: Endpoint che non validano parametri input o request body
   - Rischio: Data inconsistency, security vulnerabilities, error propagation
   - Soluzione: Aggiungere @Valid, @Validated, constraint annotations

6. **Naming conventions inconsistenti**
   - Descrizione: Mix di camelCase/snake_case, naming pattern inconsistenti
   - Rischio: Developer confusion, integration difficulties
   - Soluzione: Standardizzare naming conventions per parametri, endpoints, response

### 🔵 GRAVITÀ BASSA (Score Impact: -4 to -5)
7. **Content negotiation mancante**
   - Descrizione: Endpoint senza supporto per accept headers o content types
   - Rischio: Limited API flexibility, poor client integration
   - Soluzione: Aggiungere @RequestMapping(produces), content type handling

8. **Endpoint over-complex**
   - Descrizione: Handler methods con troppi parametri o logica complessa
   - Rischio: Difficult testing, maintenance issues, code smells
   - Soluzione: Extract business logic to service layer, simplify parameter handling

## Metriche di Valore

- **API Usability**: Migliora usabilità API attraverso design consistente e RESTful
- **Developer Experience**: Facilita integration per client developers
- **Security Posture**: Garantisce protezione appropriata degli endpoint
- **Maintainability**: Semplifica maintenance attraverso pattern consistenti

## Classificazione

**Categoria**: Entrypoint & Main Flows
**Priorità**: Alta - REST Controllers definiscono l'interfaccia pubblica dell'applicazione
**Stakeholder**: Frontend developers, API consumers, Security team

## Tags per Classificazione

`#rest-controllers` `#spring-mvc` `#api-endpoints` `#security-analysis` `#api-design` `#documentation` `#high-value` `#medium-complexity`