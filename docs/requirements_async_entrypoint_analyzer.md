# Requisiti Tecnici: AsyncEntrypointAnalyzer

## Overview
Analyzer specializzato per l'identificazione e analisi di entry point asincroni (@Async) che rappresentano punti di ingresso per l'esecuzione parallela nel codebase Spring Boot.

## Valore Business
**⭐⭐⭐⭐⭐ - Valore Massimo**
- Identificazione completa dei flussi asincroni come entry points
- Analisi delle performance e scalabilità attraverso processing parallelo
- Detection di problemi di thread safety e race conditions
- Mappatura delle dipendenze tra async flows e entry points tradizionali

## Complessità di Implementazione
**🔴 Complessa** - Richiede analisi avanzata di concorrenza e thread safety

## Tempo di Realizzazione Stimato
**8-10 giorni** di sviluppo

## Struttura delle Classi

### Core Interface
```java
package it.denzosoft.jreverse.analyzer.async;

public interface AsyncEntrypointAnalyzer {
    boolean canAnalyze(JarContent jarContent);
    AsyncEntrypointAnalysisResult analyze(JarContent jarContent);
    List<AsyncEntrypointInfo> findAsyncEntrypoints(CtClass[] classes);
    AsyncFlowAnalysis analyzeAsyncFlows(List<AsyncEntrypointInfo> entrypoints);
}
```

### Implementazione Javassist
```java
package it.denzosoft.jreverse.analyzer.async;

public class JavassistAsyncEntrypointAnalyzer implements AsyncEntrypointAnalyzer {
    
    private static final String ASYNC_ANNOTATION = "org.springframework.scheduling.annotation.Async";
    private static final String ENABLE_ASYNC = "org.springframework.scheduling.annotation.EnableAsync";
    private static final Set<String> ASYNC_RETURN_TYPES = Set.of(
        "java.util.concurrent.Future",
        "java.util.concurrent.CompletableFuture",
        "org.springframework.util.concurrent.ListenableFuture",
        "java.util.concurrent.CompletionStage"
    );
    
    @Override
    public AsyncEntrypointAnalysisResult analyze(JarContent jarContent) {
        AsyncEntrypointAnalysisResult.Builder resultBuilder = AsyncEntrypointAnalysisResult.builder();
        
        try {
            CtClass[] classes = jarContent.getAllClasses();
            
            // Verifica configurazione async
            AsyncConfigurationAnalysis configAnalysis = analyzeAsyncConfiguration(classes);
            resultBuilder.configurationAnalysis(configAnalysis);
            
            // Trova tutti gli async entrypoints
            List<AsyncEntrypointInfo> entrypoints = findAsyncEntrypoints(classes);
            resultBuilder.asyncEntrypoints(entrypoints);
            
            // Analizza flussi async
            AsyncFlowAnalysis flowAnalysis = analyzeAsyncFlows(entrypoints);
            resultBuilder.flowAnalysis(flowAnalysis);
            
            // Identifica problemi di concorrenza
            List<ConcurrencyIssue> concurrencyIssues = identifyConcurrencyIssues(entrypoints, classes);
            resultBuilder.concurrencyIssues(concurrencyIssues);
            
            // Analizza integration con altri entrypoints
            EntrypointIntegrationAnalysis integrationAnalysis = analyzeEntrypointIntegration(entrypoints, classes);
            resultBuilder.integrationAnalysis(integrationAnalysis);
            
            // Calcola metriche di performance
            AsyncPerformanceMetrics performanceMetrics = calculatePerformanceMetrics(entrypoints);
            resultBuilder.performanceMetrics(performanceMetrics);
            
        } catch (Exception e) {
            resultBuilder.addError("Error analyzing async entrypoints: " + e.getMessage());
        }
        
        return resultBuilder.build();
    }
    
    @Override
    public List<AsyncEntrypointInfo> findAsyncEntrypoints(CtClass[] classes) {
        List<AsyncEntrypointInfo> entrypoints = new ArrayList<>();
        
        for (CtClass ctClass : classes) {
            try {
                // Analizza metodi @Async come entrypoints
                CtMethod[] methods = ctClass.getDeclaredMethods();
                
                for (CtMethod method : methods) {
                    if (method.hasAnnotation(ASYNC_ANNOTATION)) {
                        AsyncEntrypointInfo entrypoint = analyzeAsyncEntrypoint(method, ctClass);
                        if (isActualEntrypoint(entrypoint, classes)) {
                            entrypoints.add(entrypoint);
                        }
                    }
                }
                
                // Analizza @Async a livello di classe
                if (ctClass.hasAnnotation(ASYNC_ANNOTATION)) {
                    List<AsyncEntrypointInfo> classLevelEntrypoints = analyzeClassLevelAsyncEntrypoints(ctClass);
                    entrypoints.addAll(classLevelEntrypoints.stream()
                        .filter(ep -> isActualEntrypoint(ep, classes))
                        .collect(Collectors.toList()));
                }
                
            } catch (Exception e) {
                // Log error but continue processing
            }
        }
        
        return entrypoints;
    }
    
    private boolean isActualEntrypoint(AsyncEntrypointInfo asyncInfo, CtClass[] classes) {
        // Un metodo async è considerato entrypoint se:
        // 1. È pubblico e chiamato da REST controllers, schedulers, o event listeners
        // 2. È il punto di inizio di un flusso asincrono non nested
        // 3. Non è chiamato solo da altri metodi async (non è parte di una chain)
        
        return analyzeCallPattern(asyncInfo, classes).isIndependentEntrypoint();
    }
}
```

## Modelli di Dati

### AsyncEntrypointInfo
```java
package it.denzosoft.jreverse.analyzer.async;

public class AsyncEntrypointInfo {
    
    // Identificazione
    private final String className;
    private final String methodName;
    private final String methodSignature;
    private final AsyncEntrypointType entrypointType;
    
    // Configurazione async
    private final String executorName;
    private final String returnType;
    private final boolean hasAppropriateReturnType;
    private final AsyncExecutionMode executionMode;
    
    // Analisi del flusso
    private final List<String> triggeredBy;        // cosa scatena questo entrypoint
    private final List<String> triggersAsync;     // altri async methods chiamati
    private final List<String> databaseOperations;
    private final List<String> externalServiceCalls;
    private final int estimatedExecutionTime;
    private final AsyncComplexityLevel complexityLevel;
    
    // Thread safety analysis
    private final List<SharedStateAccess> sharedStateAccesses;
    private final boolean hasThreadSafetyIssues;
    private final List<String> concurrencyRisks;
    
    // Performance characteristics  
    private final boolean isIOBound;
    private final boolean isCPUBound;
    private final ResourceUsagePattern resourceUsage;
    
    // Integration analysis
    private final boolean callsOtherEntrypoints;
    private final List<String> relatedEntrypoints;
    private final EntrypointChainType chainType;
    
    public enum AsyncEntrypointType {
        REST_INITIATED("REST-Initiated Async"),
        SCHEDULED_INITIATED("Scheduler-Initiated Async"),
        EVENT_INITIATED("Event-Initiated Async"),
        SERVICE_INITIATED("Service-Initiated Async"),
        MANUAL_INITIATED("Manually-Initiated Async");
        
        private final String displayName;
        
        AsyncEntrypointType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() { return displayName; }
    }
    
    public enum AsyncExecutionMode {
        FIRE_AND_FORGET("Fire and Forget"),
        FUTURE_BASED("Future-based"),
        CALLBACK_BASED("Callback-based"),
        STREAMING("Streaming");
        
        private final String displayName;
        
        AsyncExecutionMode(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() { return displayName; }
    }
    
    public enum AsyncComplexityLevel {
        SIMPLE(1, "Simple async operation"),
        MODERATE(2, "Moderate complexity with some dependencies"),
        COMPLEX(3, "Complex with multiple dependencies and state"),
        VERY_COMPLEX(4, "Very complex with high coordination needs");
        
        private final int level;
        private final String description;
        
        AsyncComplexityLevel(int level, String description) {
            this.level = level;
            this.description = description;
        }
        
        public int getLevel() { return level; }
        public String getDescription() { return description; }
    }
}
```

### AsyncFlowAnalysis
```java
package it.denzosoft.jreverse.analyzer.async;

public class AsyncFlowAnalysis {
    
    private final Map<String, List<String>> asyncCallChains;
    private final List<AsyncFlowPattern> identifiedPatterns;
    private final List<AsyncBottleneck> bottlenecks;
    private final AsyncFlowMetrics metrics;
    
    // Analisi dei pattern di flusso async
    public static class AsyncFlowPattern {
        private final PatternType type;
        private final List<String> involvedMethods;
        private final String description;
        private final PatternComplexity complexity;
        
        public enum PatternType {
            SCATTER_GATHER("Scatter-Gather Pattern"),
            PIPELINE("Pipeline Pattern"),
            FAN_OUT("Fan-Out Pattern"),
            AGGREGATOR("Aggregator Pattern"),
            CIRCUIT_BREAKER("Circuit Breaker Pattern");
            
            private final String displayName;
            
            PatternType(String displayName) {
                this.displayName = displayName;
            }
        }
    }
    
    // Identificazione dei colli di bottiglia
    public static class AsyncBottleneck {
        private final String location;
        private final BottleneckType type;
        private final String cause;
        private final String recommendation;
        private final int impactScore;
        
        public enum BottleneckType {
            SHARED_RESOURCE("Shared Resource Contention"),
            EXECUTOR_SATURATION("Thread Pool Saturation"),
            SYNCHRONIZATION_POINT("Synchronization Bottleneck"),
            IO_WAIT("I/O Wait Bottleneck");
            
            private final String displayName;
            
            BottleneckType(String displayName) {
                this.displayName = displayName;
            }
        }
    }
}
```

### ConcurrencyIssue
```java
package it.denzosoft.jreverse.analyzer.async;

public class ConcurrencyIssue {
    
    private final ConcurrencyIssueType type;
    private final String className;
    private final String methodName;
    private final ConcurrencyRiskLevel riskLevel;
    private final String description;
    private final String recommendation;
    private final List<String> involvedResources;
    
    public enum ConcurrencyIssueType {
        RACE_CONDITION("Race Condition Risk"),
        SHARED_MUTABLE_STATE("Shared Mutable State Access"),
        DEADLOCK_POTENTIAL("Deadlock Potential"),
        LIVELOCK_RISK("Livelock Risk"),
        STARVATION_RISK("Resource Starvation Risk"),
        IMPROPER_SYNCHRONIZATION("Improper Synchronization"),
        ASYNC_EXCEPTION_PROPAGATION("Async Exception Propagation Issue");
        
        private final String displayName;
        
        ConcurrencyIssueType(String displayName) {
            this.displayName = displayName;
        }
    }
    
    public enum ConcurrencyRiskLevel {
        CRITICAL("Critical - High probability of production issues"),
        HIGH("High - Likely to cause problems under load"),
        MEDIUM("Medium - May cause occasional issues"),
        LOW("Low - Potential issue in edge cases");
        
        private final String description;
        
        ConcurrencyRiskLevel(String description) {
            this.description = description;
        }
    }
}
```

## Algoritmi di Analisi Avanzata

### 1. Async Entrypoint Classification
```java
private AsyncEntrypointType classifyAsyncEntrypoint(AsyncEntrypointInfo asyncInfo, CtClass[] classes) {
    // Analizza chi chiama questo metodo async per classificarlo
    CallPatternAnalysis callPattern = analyzeCallPattern(asyncInfo, classes);
    
    if (callPattern.isCalledByRestController()) {
        return AsyncEntrypointType.REST_INITIATED;
    } else if (callPattern.isCalledByScheduledTask()) {
        return AsyncEntrypointType.SCHEDULED_INITIATED;
    } else if (callPattern.isCalledByEventListener()) {
        return AsyncEntrypointType.EVENT_INITIATED;
    } else if (callPattern.isCalledByServiceLayer()) {
        return AsyncEntrypointType.SERVICE_INITIATED;
    } else {
        return AsyncEntrypointType.MANUAL_INITIATED;
    }
}
```

### 2. Thread Safety Analysis
```java
private List<ConcurrencyIssue> analyzeThreadSafety(AsyncEntrypointInfo asyncInfo, CtClass declaringClass) {
    List<ConcurrencyIssue> issues = new ArrayList<>();
    
    try {
        CtMethod method = findMethod(declaringClass, asyncInfo.getMethodName());
        
        method.instrument(new ExprEditor() {
            @Override
            public void edit(FieldAccess access) throws CannotCompileException {
                CtField field = access.getField();
                
                // Verifica accesso a campi non thread-safe
                if (!isThreadSafe(field)) {
                    if (access.isWriter()) {
                        ConcurrencyIssue issue = ConcurrencyIssue.builder()
                            .type(ConcurrencyIssueType.SHARED_MUTABLE_STATE)
                            .className(asyncInfo.getClassName())
                            .methodName(asyncInfo.getMethodName())
                            .riskLevel(ConcurrencyRiskLevel.HIGH)
                            .description("Write access to non-thread-safe field: " + field.getName())
                            .recommendation("Use thread-safe alternatives or proper synchronization")
                            .addInvolvedResource(field.getName())
                            .build();
                        issues.add(issue);
                    }
                }
            }
            
            @Override
            public void edit(MethodCall call) throws CannotCompileException {
                // Verifica chiamate a metodi non thread-safe
                if (isNonThreadSafeOperation(call)) {
                    ConcurrencyIssue issue = ConcurrencyIssue.builder()
                        .type(ConcurrencyIssueType.RACE_CONDITION)
                        .className(asyncInfo.getClassName())
                        .methodName(asyncInfo.getMethodName())
                        .riskLevel(ConcurrencyRiskLevel.MEDIUM)
                        .description("Call to non-thread-safe method: " + call.getMethodName())
                        .recommendation("Use thread-safe alternatives or add synchronization")
                        .build();
                    issues.add(issue);
                }
            }
        });
        
    } catch (Exception e) {
        // Handle error
    }
    
    return issues;
}
```

### 3. Async Flow Pattern Recognition
```java
private List<AsyncFlowPattern> identifyAsyncFlowPatterns(List<AsyncEntrypointInfo> entrypoints) {
    List<AsyncFlowPattern> patterns = new ArrayList<>();
    
    // Scatter-Gather Pattern Detection
    patterns.addAll(detectScatterGatherPatterns(entrypoints));
    
    // Pipeline Pattern Detection
    patterns.addAll(detectPipelinePatterns(entrypoints));
    
    // Fan-Out Pattern Detection
    patterns.addAll(detectFanOutPatterns(entrypoints));
    
    return patterns;
}

private List<AsyncFlowPattern> detectScatterGatherPatterns(List<AsyncEntrypointInfo> entrypoints) {
    List<AsyncFlowPattern> patterns = new ArrayList<>();
    
    for (AsyncEntrypointInfo entrypoint : entrypoints) {
        if (entrypoint.getTriggersAsync().size() > 2 && 
            entrypoint.getReturnType().contains("CompletableFuture")) {
            
            AsyncFlowPattern pattern = AsyncFlowPattern.builder()
                .type(AsyncFlowPattern.PatternType.SCATTER_GATHER)
                .involvedMethods(buildMethodList(entrypoint))
                .description("Scatter-gather pattern: distributes work and aggregates results")
                .complexity(determinePatternComplexity(entrypoint))
                .build();
            patterns.add(pattern);
        }
    }
    
    return patterns;
}
```

## Integrazione con Entry Point Ecosystem

### REST Controller Integration
```java
private EntrypointIntegrationAnalysis analyzeRestControllerIntegration(List<AsyncEntrypointInfo> asyncEntrypoints, CtClass[] classes) {
    EntrypointIntegrationAnalysis.Builder builder = EntrypointIntegrationAnalysis.builder();
    
    // Trova REST controllers che chiamano metodi async
    List<RestAsyncIntegration> integrations = new ArrayList<>();
    
    for (CtClass ctClass : classes) {
        if (isRestController(ctClass)) {
            List<AsyncEntrypointInfo> calledAsyncMethods = findAsyncMethodsCalled(ctClass, asyncEntrypoints);
            
            if (!calledAsyncMethods.isEmpty()) {
                RestAsyncIntegration integration = RestAsyncIntegration.builder()
                    .restControllerClass(ctClass.getName())
                    .asyncEntrypoints(calledAsyncMethods)
                    .integrationPattern(determineIntegrationPattern(ctClass, calledAsyncMethods))
                    .build();
                integrations.add(integration);
            }
        }
    }
    
    return builder.restIntegrations(integrations).build();
}
```

## Test Strategy Avanzata

### Concurrency Testing
```java
@Test
public void testSharedStateDetection() {
    // Given
    CtClass classWithSharedState = createClassWithSharedMutableState();
    
    // When
    List<ConcurrencyIssue> issues = analyzer.identifyConcurrencyIssues(
        Arrays.asList(createAsyncEntrypointInfo()), 
        new CtClass[]{classWithSharedState}
    );
    
    // Then
    assertThat(issues).hasSize(1);
    assertThat(issues.get(0).getType()).isEqualTo(ConcurrencyIssueType.SHARED_MUTABLE_STATE);
    assertThat(issues.get(0).getRiskLevel()).isEqualTo(ConcurrencyRiskLevel.HIGH);
}

@Test
public void testAsyncFlowPatternRecognition() {
    // Given
    List<AsyncEntrypointInfo> scatterGatherEntrypoints = createScatterGatherPattern();
    
    // When
    AsyncFlowAnalysis analysis = analyzer.analyzeAsyncFlows(scatterGatherEntrypoints);
    
    // Then
    assertThat(analysis.getIdentifiedPatterns()).hasSize(1);
    assertThat(analysis.getIdentifiedPatterns().get(0).getType())
        .isEqualTo(AsyncFlowPattern.PatternType.SCATTER_GATHER);
}
```

## Metriche di Qualità

### Algoritmo di Scoring Avanzato (0-100)
```java
public int calculateAsyncEntrypointQualityScore(AsyncEntrypointAnalysisResult result) {
    double score = 100.0;
    
    // Penalizzazioni critiche per problemi di concorrenza
    score -= result.getCriticalConcurrencyIssues() * 30;      // -30 per problemi critici
    score -= result.getHighRiskConcurrencyIssues() * 20;      // -20 per problemi high risk
    score -= result.getRaceConditionRisks() * 18;             // -18 per race condition risks
    score -= result.getDeadlockPotentials() * 25;             // -25 per deadlock potentials
    
    // Penalizzazioni per design issues
    score -= result.getInappropriateReturnTypes() * 12;       // -12 per return type inappropriati
    score -= result.getAsyncWithoutExceptionHandling() * 10;  // -10 per async senza exception handling
    score -= result.getComplexAsyncChains() * 8;              // -8 per catene async troppo complesse
    
    // Bonus per buone pratiche
    score += result.getWellDesignedAsyncPatterns() * 5;       // +5 per pattern async ben progettati
    score += result.getProperThreadSafetyMeasures() * 4;     // +4 per misure di thread safety appropriate
    score += result.getOptimalExecutorConfiguration() * 3;    // +3 per configurazione executor ottimale
    score += result.getAsyncWithProperReturnTypes() * 2;     // +2 per return type appropriati
    
    return Math.max(0, Math.min(100, (int) score));
}
```

## Considerazioni Architetturali

### Performance Optimization
- Analisi lazy delle call chains per evitare overhead
- Caching delle analisi di thread safety
- Parallelizzazione dell'analisi stessa dove possibile

### Scalability
- Support per progetti con migliaia di metodi async
- Configurabilità delle soglie di complessità
- Reporting incrementale per grandi codebase

### Integration Points
- Plugin per custom async annotations
- Support per reactive programming patterns (WebFlux)
- Integration con APM tools per metriche runtime