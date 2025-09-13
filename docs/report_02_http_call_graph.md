# Report 02: Call Graph delle Richieste HTTP

## Descrizione
Tracciamento completo del flusso di esecuzione per ogni richiesta HTTP, dalla ricezione nel controller fino all'accesso al database, incluse tutte le chiamate intermedie ai service layer, repository, e componenti esterni.

## Valore per l'Utente
**⭐⭐⭐⭐⭐** - Valore Massimo
- Comprensione del flusso applicativo end-to-end
- Identificazione di bottleneck e punti critici
- Analisi delle dipendenze transitive
- Ottimizzazione delle performance
- Debug facilitato per problemi complessi
- Analisi dell'impatto delle modifiche

## Complessità di Implementazione
**🔴 Molto Complessa** - Richiede analisi statica avanzata e tracciamento delle chiamate

## Tempo di Realizzazione Stimato
**15-20 giorni** di sviluppo (including complex call graph algorithms)

## Sezioni del Report

### 1. Executive Summary
- Numero totale di call graph tracciati
- Profondità media delle chiamate
- Componenti più utilizzati (hotspots)
- Flussi critici identificati
- Potenziali problemi architetturali

### 2. Request Flow Overview
```
GET /api/v1/users/{id}
├── UserController.getUserById()
│   ├── UserService.findById()
│   │   ├── UserRepository.findById()
│   │   │   └── JPA Query: SELECT u FROM User u WHERE u.id = :id
│   │   ├── CacheManager.get("user:" + id)
│   │   └── AuditService.logAccess()
│   │       └── AuditRepository.save()
│   ├── UserMapper.toDto()
│   └── ValidationService.validateAccess()
└── Response: UserDto
```

### 3. Detailed Call Graphs

#### 3.1 Per ogni endpoint HTTP:
- **Request Entry Point**: Controller method
- **Call Sequence**: Sequenza cronologica delle chiamate
- **Service Layer Interactions**: Servizi coinvolti
- **Data Access Layer**: Repository e query eseguite
- **External Dependencies**: Chiamate a servizi esterni
- **Cross-Cutting Concerns**: Logging, security, caching, transactions

#### 3.2 Call Graph Metrics:
- **Depth**: Profondità massima della chiamata
- **Breadth**: Numero di componenti coinvolti
- **Cyclomatic Complexity**: Complessità del flusso
- **External Calls**: Chiamate a sistemi esterni
- **Database Queries**: Numero e tipo di query

### 4. Component Interaction Analysis
- **Service Dependencies**: Matrice di dipendenze tra servizi
- **Repository Usage**: Frequenza di utilizzo dei repository
- **Cross-Service Communication**: Comunicazione tra bounded context
- **Circular Dependencies**: Dipendenze circolari identificate

### 5. Performance Analysis
- **Hot Paths**: Percorsi più frequentemente eseguiti
- **Heavy Operations**: Operazioni computazionalmente costose
- **Database Access Patterns**: Pattern di accesso al database
- **Caching Effectiveness**: Analisi dell'efficacia della cache

### 6. Architecture Quality Indicators
- **Layer Separation**: Rispetto della separazione dei layer
- **Single Responsibility**: Aderenza al principio SRP
- **Dependency Direction**: Direzione corretta delle dipendenze
- **Coupling Analysis**: Livello di accoppiamento tra componenti

## Implementazione Javassist

### Analisi dei Controller Endpoints

```java
public class CallGraphAnalyzer {
    
    private final ClassPool classPool;
    private final Map<String, CallGraphNode> callGraphs = new HashMap<>();
    
    public void analyzeHttpCallGraphs() {
        // 1. Identifica tutti i controller endpoints
        List<ControllerMethod> endpoints = findControllerEndpoints();
        
        // 2. Per ogni endpoint, costruisci il call graph
        for (ControllerMethod endpoint : endpoints) {
            CallGraphNode rootNode = buildCallGraph(endpoint);
            callGraphs.put(endpoint.getSignature(), rootNode);
        }
    }
    
    private List<ControllerMethod> findControllerEndpoints() {
        List<ControllerMethod> endpoints = new ArrayList<>();
        
        for (CtClass clazz : classPool.getAllClasses()) {
            if (isController(clazz)) {
                for (CtMethod method : clazz.getDeclaredMethods()) {
                    if (isEndpointMethod(method)) {
                        endpoints.add(new ControllerMethod(clazz, method));
                    }
                }
            }
        }
        
        return endpoints;
    }
}
```

### Costruzione del Call Graph

```java
private CallGraphNode buildCallGraph(ControllerMethod endpoint) {
    CallGraphNode rootNode = new CallGraphNode(endpoint);
    Set<String> visited = new HashSet<>();
    
    analyzeMethodCalls(endpoint.getMethod(), rootNode, visited, 0);
    
    return rootNode;
}

private void analyzeMethodCalls(CtMethod method, CallGraphNode parentNode, 
                               Set<String> visited, int depth) {
    
    if (depth > MAX_DEPTH || visited.contains(method.getLongName())) {
        return; // Evita cicli infiniti
    }
    
    visited.add(method.getLongName());
    
    try {
        // Analisi del bytecode per trovare le chiamate
        method.instrument(new ExprEditor() {
            @Override
            public void edit(MethodCall methodCall) throws CannotCompileException {
                processMethodCall(methodCall, parentNode, visited, depth);
            }
            
            @Override
            public void edit(FieldAccess fieldAccess) throws CannotCompileException {
                processFieldAccess(fieldAccess, parentNode);
            }
        });
    } catch (CannotCompileException e) {
        // Log error and continue
        logger.warn("Cannot analyze method: " + method.getLongName(), e);
    }
    
    visited.remove(method.getLongName());
}

private void processMethodCall(MethodCall methodCall, CallGraphNode parentNode, 
                              Set<String> visited, int depth) {
    try {
        CtMethod targetMethod = methodCall.getMethod();
        CtClass targetClass = targetMethod.getDeclaringClass();
        
        // Crea nodo per la chiamata
        CallGraphNode childNode = new CallGraphNode(targetClass, targetMethod);
        childNode.setCallLocation(methodCall.getLineNumber());
        childNode.setCallType(determineCallType(targetClass, targetMethod));
        
        parentNode.addChild(childNode);
        
        // Continua l'analisi ricorsivamente
        if (shouldAnalyzeDeeper(targetClass)) {
            analyzeMethodCalls(targetMethod, childNode, visited, depth + 1);
        }
        
    } catch (NotFoundException e) {
        // External method call - create external node
        createExternalCallNode(methodCall, parentNode);
    }
}
```

### Classificazione dei Tipi di Chiamata

```java
private CallType determineCallType(CtClass targetClass, CtMethod targetMethod) {
    
    // Database access
    if (isRepositoryClass(targetClass)) {
        return CallType.DATABASE_ACCESS;
    }
    
    // Service layer
    if (hasAnnotation(targetClass, "org.springframework.stereotype.Service")) {
        return CallType.SERVICE_CALL;
    }
    
    // External service call
    if (isRestTemplateCall(targetMethod) || isWebClientCall(targetMethod)) {
        return CallType.EXTERNAL_HTTP_CALL;
    }
    
    // Messaging
    if (isMessageProducerCall(targetMethod)) {
        return CallType.MESSAGE_PUBLISH;
    }
    
    // Caching
    if (hasCachingAnnotations(targetMethod)) {
        return CallType.CACHE_OPERATION;
    }
    
    // Transaction boundary
    if (hasAnnotation(targetMethod, "org.springframework.transaction.annotation.Transactional")) {
        return CallType.TRANSACTIONAL_CALL;
    }
    
    return CallType.BUSINESS_LOGIC;
}
```

### Analisi delle Query Database

```java
private void analyzeRepositoryMethods(CallGraphNode repositoryNode) {
    CtMethod method = repositoryNode.getMethod();
    CtClass repoClass = repositoryNode.getDeclaringClass();
    
    // JPA Repository method analysis
    if (isJpaRepository(repoClass)) {
        QueryInfo queryInfo = analyzeJpaMethod(method);
        repositoryNode.setQueryInfo(queryInfo);
    }
    
    // Custom query analysis
    if (method.hasAnnotation("org.springframework.data.jpa.repository.Query")) {
        String jpql = getAnnotationValue(method, "Query", "value");
        repositoryNode.setCustomQuery(jpql);
    }
    
    // Native query analysis
    if (method.hasAnnotation("org.springframework.data.jpa.repository.Query")) {
        Annotation queryAnnotation = method.getAnnotation("Query");
        boolean isNative = getAnnotationBooleanValue(queryAnnotation, "nativeQuery");
        if (isNative) {
            repositoryNode.setNativeQuery(true);
        }
    }
}

private QueryInfo analyzeJpaMethod(CtMethod method) {
    String methodName = method.getName();
    QueryInfo info = new QueryInfo();
    
    // Spring Data JPA method name parsing
    if (methodName.startsWith("findBy")) {
        info.setOperationType(QueryType.SELECT);
        info.setQueryMethod(QueryMethod.DERIVED);
        info.setPredicate(methodName.substring(6)); // Remove "findBy"
    } else if (methodName.startsWith("deleteBy")) {
        info.setOperationType(QueryType.DELETE);
        info.setQueryMethod(QueryMethod.DERIVED);
    } else if (methodName.startsWith("countBy")) {
        info.setOperationType(QueryType.COUNT);
        info.setQueryMethod(QueryMethod.DERIVED);
    }
    
    // Analyze method parameters for query parameters
    info.setParameters(analyzeQueryParameters(method));
    
    return info;
}
```

### Analisi delle Performance

```java
private PerformanceMetrics calculatePerformanceMetrics(CallGraphNode rootNode) {
    PerformanceMetrics metrics = new PerformanceMetrics();
    
    // Calculate depth and breadth
    metrics.setMaxDepth(calculateMaxDepth(rootNode));
    metrics.setBreadth(calculateBreadth(rootNode));
    
    // Count different types of operations
    CallTypeCounter counter = new CallTypeCounter();
    countCallTypes(rootNode, counter);
    
    metrics.setDatabaseCalls(counter.getDatabaseCalls());
    metrics.setExternalCalls(counter.getExternalCalls());
    metrics.setServiceCalls(counter.getServiceCalls());
    
    // Identify potential performance issues
    List<PerformanceIssue> issues = identifyPerformanceIssues(rootNode);
    metrics.setPerformanceIssues(issues);
    
    return metrics;
}

private List<PerformanceIssue> identifyPerformanceIssues(CallGraphNode node) {
    List<PerformanceIssue> issues = new ArrayList<>();
    
    // N+1 query detection
    if (detectNPlusOnePattern(node)) {
        issues.add(new PerformanceIssue(
            PerformanceIssueType.N_PLUS_ONE_QUERY, 
            "Potential N+1 query pattern detected",
            node.getLocation()
        ));
    }
    
    // Excessive database calls
    int dbCalls = countDatabaseCalls(node);
    if (dbCalls > EXCESSIVE_DB_CALLS_THRESHOLD) {
        issues.add(new PerformanceIssue(
            PerformanceIssueType.EXCESSIVE_DB_CALLS,
            String.format("Excessive database calls: %d", dbCalls),
            node.getLocation()
        ));
    }
    
    // Missing transaction boundaries
    if (hasDatabaseCallsWithoutTransaction(node)) {
        issues.add(new PerformanceIssue(
            PerformanceIssueType.MISSING_TRANSACTION,
            "Database calls without transaction boundary",
            node.getLocation()
        ));
    }
    
    return issues;
}
```

### Generazione del Report Visuale

```java
private void generateVisualCallGraph(CallGraphNode rootNode, String endpoint) {
    StringBuilder mermaidGraph = new StringBuilder();
    mermaidGraph.append("graph TD\n");
    
    Map<String, String> nodeIds = new HashMap<>();
    AtomicInteger nodeCounter = new AtomicInteger(0);
    
    generateMermaidNodes(rootNode, mermaidGraph, nodeIds, nodeCounter);
    
    // Save to file
    String filename = sanitizeFilename(endpoint) + "_callgraph.mmd";
    saveToFile(filename, mermaidGraph.toString());
}

private void generateMermaidNodes(CallGraphNode node, StringBuilder graph, 
                                 Map<String, String> nodeIds, AtomicInteger counter) {
    String nodeId = getOrCreateNodeId(node, nodeIds, counter);
    
    // Add node definition with styling based on type
    String nodeLabel = formatNodeLabel(node);
    String nodeStyle = getNodeStyle(node.getCallType());
    
    graph.append(String.format("  %s[%s]%s\n", nodeId, nodeLabel, nodeStyle));
    
    // Process children
    for (CallGraphNode child : node.getChildren()) {
        String childId = getOrCreateNodeId(child, nodeIds, counter);
        graph.append(String.format("  %s --> %s\n", nodeId, childId));
        
        generateMermaidNodes(child, graph, nodeIds, counter);
    }
}
```

## Struttura Output Report

```json
{
  "summary": {
    "totalEndpoints": 45,
    "averageCallDepth": 4.2,
    "maxCallDepth": 8,
    "totalComponents": 156,
    "hotspotComponents": [
      {"name": "UserService", "usage": 23},
      {"name": "AuthService", "usage": 18}
    ]
  },
  "callGraphs": [
    {
      "endpoint": "GET /api/v1/users/{id}",
      "rootMethod": "UserController.getUserById",
      "metrics": {
        "depth": 5,
        "breadth": 12,
        "databaseCalls": 3,
        "externalCalls": 1,
        "serviceCalls": 4
      },
      "callGraph": {
        "method": "getUserById",
        "class": "UserController",
        "callType": "CONTROLLER",
        "children": [
          {
            "method": "findById",
            "class": "UserService",
            "callType": "SERVICE_CALL",
            "lineNumber": 45,
            "children": [
              {
                "method": "findById",
                "class": "UserRepository",
                "callType": "DATABASE_ACCESS",
                "queryInfo": {
                  "type": "SELECT",
                  "entity": "User",
                  "jpql": "SELECT u FROM User u WHERE u.id = :id"
                }
              }
            ]
          }
        ]
      },
      "performanceIssues": [],
      "visualGraph": "user_get_callgraph.mmd"
    }
  ],
  "componentInteractions": {
    "serviceMatrix": {
      "UserService -> AuthService": 12,
      "UserService -> NotificationService": 5
    },
    "repositoryUsage": {
      "UserRepository": {"calls": 45, "endpoints": 8},
      "OrderRepository": {"calls": 23, "endpoints": 5}
    }
  },
  "architectureMetrics": {
    "layerViolations": 2,
    "circularDependencies": 0,
    "couplingScore": 0.73,
    "cohesionScore": 0.85
  }
}
```

## Metriche di Qualità del Codice

### Algoritmo di Calcolo (0-100)

```java
public int calculateCallGraphQualityScore(CallGraphReport result) {
    double score = 100.0;
    
    // Penalizzazioni per problemi di architettura e performance
    score -= result.getNPlusOnePatterns() * 25;               // -25 per pattern N+1 query
    score -= result.getCircularDependencies() * 20;           // -20 per dipendenza circolare
    score -= result.getExcessiveCallDepth() * 15;             // -15 per profondità eccessiva (>8)
    score -= result.getLayerViolations() * 18;                // -18 per violazioni layer architecture
    score -= result.getExcessiveDbCalls() * 12;               // -12 per troppe chiamate DB
    score -= result.getUnhandledExceptions() * 10;            // -10 per eccezioni non gestite
    score -= result.getHighCouplingEndpoints() * 8;           // -8 per endpoint ad alto accoppiamento
    score -= result.getMissingTransactionBoundaries() * 6;    // -6 per boundary transazionali mancanti
    
    // Bonus per buona architettura
    score += result.getWellStructuredFlows() * 2;             // +2 per flussi ben strutturati
    score += result.getProperLayerSeparation() * 3;           // +3 per corretta separazione layer
    score += result.getOptimizedQueryPatterns() * 1;          // +1 per pattern query ottimizzati
    
    return Math.max(0, Math.min(100, (int) score));
}
```

### Soglie di Valutazione
- **0-40**: 🔴 CRITICO - Architettura con gravi problemi di design e performance
- **41-60**: 🟡 SUFFICIENTE - Flussi funzionali ma con significative inefficienze
- **61-80**: 🟢 BUONO - Architettura ben organizzata con alcuni miglioramenti possibili
- **81-100**: ⭐ ECCELLENTE - Call graph ottimizzato e ben strutturato

## Segnalazioni per Gravità

### 🔴 GRAVITÀ CRITICA (Score Impact: -18 to -25)
1. **Pattern N+1 Query**
   - Descrizione: Loop che eseguono query database per ogni iterazione
   - Rischio: Performance drasticamente degradate con dati in crescita
   - Soluzione: Utilizzare JOIN, batch loading, o @EntityGraph

2. **Dipendenze Circolari**
   - Descrizione: Cicli di chiamate tra componenti che creano dipendenze circolari
   - Rischio: Impossibilità di testing isolato, tight coupling
   - Soluzione: Refactoring architetturale, introduzione interfacce

3. **Violazioni Layer Architecture**
   - Descrizione: Chiamate dirette che bypassano layer intermedi (Controller->Repository)
   - Rischio: Logica business distribuita, difficoltà manutenzione
   - Soluzione: Rispettare layered architecture, centralizzare logica nei Service

### 🟠 GRAVITÀ ALTA (Score Impact: -10 to -15)
4. **Profondità Eccessiva Call Graph (>8 livelli)**
   - Descrizione: Catene di chiamate troppo profonde
   - Rischio: Difficoltà debugging, stack overflow potential, performance degradate
   - Soluzione: Semplificare flussi, ridurre delegazione eccessiva

5. **Chiamate Database Eccessive**
   - Descrizione: Endpoint che eseguono troppe query separate (>20)
   - Rischio: Latenza elevata, connection pool exhaustion
   - Soluzione: Query optimization, aggregation, caching

### 🟡 GRAVITÀ MEDIA (Score Impact: -6 to -8)
6. **Alto Accoppiamento Endpoint**
   - Descrizione: Endpoint che dipendono da molti servizi/componenti
   - Rischio: Difficoltà testing, modifiche a cascata
   - Soluzione: Applicare principi SOLID, ridurre dipendenze

7. **Boundary Transazionali Mancanti**
   - Descrizione: Flussi che modificano dati senza gestione transazioni
   - Rischio: Data inconsistency, problemi concorrenza
   - Soluzione: Definire appropriate transaction boundaries

### 🔵 GRAVITÀ BASSA (Score Impact: -1 to -6)
8. **Eccezioni Non Gestite nel Flusso**
   - Descrizione: Potential exception paths non gestiti appropriatamente
   - Rischio: Errori non controllati, user experience degradata
   - Soluzione: Implementare exception handling completo

## Metriche e KPI

- **Call Graph Coverage**: Percentuale di endpoint tracciati
- **Average Depth**: Profondità media dei call graph
- **Complexity Score**: Complessità media dei flussi
- **Performance Risk Score**: Rischio di performance basato sui pattern
- **Architecture Quality**: Qualità architettturale basata su dipendenze

## Tags per Classificazione

`#call-graph` `#performance` `#architecture` `#tracing` `#dependencies` `#complex-analysis` `#high-value` `#bottleneck-detection` `#essential`