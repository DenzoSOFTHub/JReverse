# Report 01: Mappa degli Endpoint REST

## Descrizione
Analisi completa di tutti gli endpoint REST esposti dall'applicazione Spring Boot, inclusi path, metodi HTTP, parametri di input, tipi di ritorno, headers e annotazioni di sicurezza.

## Valore per l'Utente
**⭐⭐⭐⭐⭐** - Valore Massimo
- Comprensione completa dell'API esposta
- Identificazione di endpoint non documentati
- Analisi di sicurezza degli endpoint
- Base per generazione automatica di documentazione API
- Individuazione di inconsistenze nell'API design

## Complessità di Implementazione
**🔴 Complessa** - Richiede analisi profonda delle annotazioni Spring MVC/WebFlux

## Tempo di Realizzazione Stimato
**8-12 giorni** di sviluppo (including testing)

## Sezioni del Report

### 1. Executive Summary
- Numero totale di endpoint REST
- Distribuzione per metodo HTTP (GET, POST, PUT, DELETE, etc.)
- Endpoint pubblici vs protetti
- Endpoint deprecated o experimental

### 2. REST Controllers Overview
```
Controller: UserController
├── Base Path: /api/v1/users
├── Security: @PreAuthorize("hasRole('USER')")
├── Endpoints: 7
└── Documentation Status: ✓ Swagger
```

### 3. Detailed Endpoints Catalog

#### 3.1 Per ogni endpoint:
- **HTTP Method & Path**: `GET /api/v1/users/{id}`
- **Controller Method**: `UserController.getUserById()`
- **Parameters**:
  - Path Variables: `id` (Long, @PathVariable)
  - Query Parameters: `include` (String[], @RequestParam, optional)
  - Request Body: N/A
  - Headers: `Accept` (String, @RequestHeader, optional)
- **Response Type**: `UserDto`
- **Status Codes**: 200 (OK), 404 (Not Found), 403 (Forbidden)
- **Security Annotations**: `@PreAuthorize("hasRole('USER')")`
- **Validation**: `@Valid`, `@NotNull`
- **Documentation**: Swagger/OpenAPI annotations
- **Caching**: `@Cacheable("users")`

### 4. Security Analysis
- **Public Endpoints**: Elenco endpoint accessibili senza autenticazione
- **Protected Endpoints**: Mapping ruoli/permessi richiesti
- **Security Gaps**: Endpoint potenzialmente non protetti
- **CORS Configuration**: Configurazione Cross-Origin Resource Sharing

### 5. API Versioning Strategy
- Pattern di versioning utilizzato (URL path, headers, params)
- Versioni attive simultaneamente
- Endpoint deprecated per versione

### 6. Content Type Analysis
- Media types supportati (JSON, XML, form-data)
- Custom media types
- Content negotiation strategy

## Implementazione Javassist

### Classi Target da Analizzare

```java
// 1. Identificazione dei Controller
CtClass[] controllers = classPool.getClasses().stream()
    .filter(clazz -> clazz.hasAnnotation("org.springframework.web.bind.annotation.RestController") ||
                     clazz.hasAnnotation("org.springframework.stereotype.Controller"))
    .toArray(CtClass[]::new);

// 2. Estrazione @RequestMapping a livello di classe
for (CtClass controller : controllers) {
    Annotation requestMapping = controller.getAnnotation("RequestMapping");
    String[] basePaths = extractBasePaths(requestMapping);
    
    // 3. Analisi dei metodi endpoint
    for (CtMethod method : controller.getDeclaredMethods()) {
        analyzeEndpointMethod(method, basePaths);
    }
}
```

### Estrazione Dettagli Endpoint

```java
private EndpointInfo analyzeEndpointMethod(CtMethod method, String[] basePaths) {
    EndpointInfo info = new EndpointInfo();
    
    // HTTP Methods
    info.setHttpMethods(extractHttpMethods(method));
    
    // Paths
    info.setPaths(combinePaths(basePaths, extractMethodPaths(method)));
    
    // Parameters
    info.setParameters(analyzeParameters(method));
    
    // Response Type
    info.setReturnType(analyzeReturnType(method));
    
    // Security
    info.setSecurityAnnotations(extractSecurityAnnotations(method));
    
    // Validation
    info.setValidationRules(extractValidationAnnotations(method));
    
    return info;
}

private List<ParameterInfo> analyzeParameters(CtMethod method) {
    List<ParameterInfo> parameters = new ArrayList<>();
    
    CtClass[] paramTypes = method.getParameterTypes();
    Annotation[][] paramAnnotations = method.getParameterAnnotations();
    
    for (int i = 0; i < paramTypes.length; i++) {
        ParameterInfo param = new ParameterInfo();
        param.setType(paramTypes[i].getName());
        
        // Analizza annotazioni parametro
        for (Annotation annotation : paramAnnotations[i]) {
            switch (annotation.getTypeName()) {
                case "org.springframework.web.bind.annotation.PathVariable":
                    param.setSource(ParameterSource.PATH);
                    param.setName(getAnnotationValue(annotation, "value"));
                    break;
                case "org.springframework.web.bind.annotation.RequestParam":
                    param.setSource(ParameterSource.QUERY);
                    param.setRequired(getAnnotationBooleanValue(annotation, "required"));
                    break;
                case "org.springframework.web.bind.annotation.RequestBody":
                    param.setSource(ParameterSource.BODY);
                    break;
                case "org.springframework.web.bind.annotation.RequestHeader":
                    param.setSource(ParameterSource.HEADER);
                    break;
            }
        }
        
        parameters.add(param);
    }
    
    return parameters;
}
```

### Analisi Security Annotations

```java
private SecurityInfo extractSecurityAnnotations(CtMethod method) {
    SecurityInfo security = new SecurityInfo();
    
    // Method-level security
    if (method.hasAnnotation("org.springframework.security.access.prepost.PreAuthorize")) {
        Annotation preAuth = method.getAnnotation("PreAuthorize");
        security.setPreAuthorizeExpression(getAnnotationValue(preAuth, "value"));
    }
    
    // Class-level security
    CtClass declaringClass = method.getDeclaringClass();
    if (declaringClass.hasAnnotation("PreAuthorize")) {
        // Class-level security inherited
    }
    
    // Role-based annotations
    if (method.hasAnnotation("org.springframework.security.access.annotation.Secured")) {
        Annotation secured = method.getAnnotation("Secured");
        security.setRequiredRoles(getAnnotationArrayValue(secured, "value"));
    }
    
    return security;
}
```

### Analisi Response Types

```java
private ResponseInfo analyzeReturnType(CtMethod method) {
    ResponseInfo response = new ResponseInfo();
    
    CtClass returnType = method.getReturnType();
    
    // ResponseEntity analysis
    if (returnType.getName().equals("org.springframework.http.ResponseEntity")) {
        // Extract generic type
        String signature = method.getGenericSignature();
        String actualType = extractGenericType(signature);
        response.setBodyType(actualType);
        response.setHasCustomStatusCode(true);
    } else {
        response.setBodyType(returnType.getName());
        response.setDefaultStatusCode(200);
    }
    
    // @ResponseStatus annotation
    if (method.hasAnnotation("org.springframework.web.bind.annotation.ResponseStatus")) {
        Annotation responseStatus = method.getAnnotation("ResponseStatus");
        response.setCustomStatusCode(getAnnotationValue(responseStatus, "code"));
    }
    
    return response;
}
```

### Pattern Recognition per API Design

```java
private ApiDesignPatterns analyzeApiDesignPatterns(List<EndpointInfo> endpoints) {
    ApiDesignPatterns patterns = new ApiDesignPatterns();
    
    // REST Maturity Analysis
    patterns.setRestMaturityLevel(calculateRestMaturityLevel(endpoints));
    
    // Naming Convention Analysis
    patterns.setNamingConventions(analyzeNamingPatterns(endpoints));
    
    // HTTP Method Usage
    patterns.setHttpMethodUsage(analyzeHttpMethodDistribution(endpoints));
    
    // Response Format Consistency
    patterns.setResponseFormatConsistency(analyzeResponseFormats(endpoints));
    
    return patterns;
}
```

## Struttura Output Report

```json
{
  "summary": {
    "totalEndpoints": 45,
    "httpMethodDistribution": {
      "GET": 20,
      "POST": 12,
      "PUT": 8,
      "DELETE": 5
    },
    "securityCoverage": {
      "protected": 38,
      "public": 7
    },
    "apiVersions": ["v1", "v2"]
  },
  "controllers": [
    {
      "className": "com.example.UserController",
      "basePath": "/api/v1/users",
      "endpoints": [
        {
          "httpMethod": "GET",
          "path": "/api/v1/users/{id}",
          "methodName": "getUserById",
          "parameters": [
            {
              "name": "id",
              "type": "Long",
              "source": "PATH_VARIABLE",
              "required": true,
              "validation": ["@NotNull", "@Min(1)"]
            }
          ],
          "responseType": "UserDto",
          "statusCodes": [200, 404, 403],
          "security": {
            "preAuthorize": "hasRole('USER')",
            "roles": ["USER"]
          },
          "documentation": {
            "hasSwagger": true,
            "summary": "Get user by ID"
          }
        }
      ]
    }
  ],
  "securityAnalysis": {
    "publicEndpoints": ["/health", "/info"],
    "protectedEndpoints": [...],
    "securityGaps": []
  },
  "apiDesignPatterns": {
    "restMaturityLevel": 3,
    "consistencyScore": 0.85,
    "issues": ["Inconsistent error response format"]
  }
}
```

## Metriche di Qualità del Codice

### Algoritmo di Calcolo (0-100)

```java
public int calculateRESTEndpointQualityScore(EndpointAnalysisResult result) {
    double score = 100.0;
    
    // Penalizzazioni per problemi critici
    score -= result.getUnprotectedEndpoints() * 15;           // -15 per endpoint non protetto
    score -= result.getMissingValidationEndpoints() * 10;     // -10 per endpoint senza validazione
    score -= result.getInconsistentNaming() * 8;              // -8 per naming inconsistente
    score -= result.getMissingDocumentation() * 5;            // -5 per endpoint non documentato
    score -= result.getUnusedParameters() * 3;                // -3 per parametri non utilizzati
    score -= result.getImproperHTTPMethods() * 12;            // -12 per metodi HTTP inappropriati
    score -= result.getMissingErrorHandling() * 7;            // -7 per gestione errori mancante
    
    // Bonus per best practices
    score += result.getProperlySecuredEndpoints() * 2;        // +2 per endpoint correttamente protetti
    score += result.getWellDocumentedEndpoints() * 1;         // +1 per endpoint ben documentati
    score += result.getConsistentResponseFormat() * 3;        // +3 per formato risposta consistente
    
    return Math.max(0, Math.min(100, (int) score));
}
```

### Soglie di Valutazione
- **0-40**: 🔴 CRITICO - API con gravi problemi di sicurezza o design
- **41-60**: 🟡 SUFFICIENTE - API funzionale ma necessita miglioramenti
- **61-80**: 🟢 BUONO - API ben strutturata con alcuni aspetti da ottimizzare
- **81-100**: ⭐ ECCELLENTE - API di alta qualità che segue le best practices

## Segnalazioni per Gravità

### 🔴 GRAVITÀ CRITICA (Score Impact: -12 to -15)
1. **Endpoint non protetti esposti pubblicamente**
   - Descrizione: Endpoint sensibili senza autenticazione/autorizzazione
   - Rischio: Accesso non autorizzato ai dati
   - Soluzione: Aggiungere `@PreAuthorize` o `@Secured`

2. **Metodi HTTP inappropriati**
   - Descrizione: Uso di GET per operazioni di modifica dati
   - Rischio: Violazione semantica HTTP, problemi di caching
   - Soluzione: Utilizzare POST/PUT/DELETE per operazioni di scrittura

### 🟠 GRAVITÀ ALTA (Score Impact: -7 to -10)
3. **Mancanza di validazione input**
   - Descrizione: Parametri non validati con `@Valid` o constraint annotations
   - Rischio: Dati inconsistenti, potenziali vulnerabilità
   - Soluzione: Implementare Bean Validation (JSR-303)

4. **Gestione errori inadeguata**
   - Descrizione: Mancanza di `@ExceptionHandler` o error handling
   - Rischio: Leak di informazioni sensitive negli errori
   - Soluzione: Implementare global exception handler

### 🟡 GRAVITÀ MEDIA (Score Impact: -3 to -8)
5. **Naming inconsistente**
   - Descrizione: Convenzioni di naming non uniformi tra endpoint
   - Rischio: Confusione per sviluppatori e consumatori API
   - Soluzione: Definire e seguire naming conventions

6. **Documentazione mancante**
   - Descrizione: Endpoint senza `@Operation` o documentazione Swagger
   - Rischio: Difficoltà nell'utilizzo dell'API
   - Soluzione: Aggiungere annotazioni OpenAPI

### 🔵 GRAVITÀ BASSA (Score Impact: -1 to -3)
7. **Parametri non utilizzati**
   - Descrizione: Parametri dichiarati ma non utilizzati nella logica
   - Rischio: Confusione e manutenzione difficoltosa
   - Soluzione: Rimuovere parametri inutilizzati

## Metriche e KPI

- **API Coverage**: Percentuale di endpoint documentati
- **Security Score**: Percentuale di endpoint protetti appropriatamente
- **REST Compliance**: Aderenza ai principi REST
- **Consistency Score**: Coerenza nel design dell'API
- **Performance Risk**: Endpoint con potenziali problemi di performance

## Tags per Classificazione

`#rest-api` `#endpoints` `#security` `#spring-mvc` `#documentation` `#api-design` `#high-value` `#complex-implementation` `#essential`