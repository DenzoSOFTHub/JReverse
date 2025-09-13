# Requisiti Tecnici: SchedulingEntrypointAnalyzer

## Overview
Analyzer specializzato per l'identificazione e analisi di entry point basati su scheduling (@Scheduled, @Schedules) nel codebase Spring Boot.

## Valore Business
**⭐⭐⭐⭐ - Alto Valore**
- Identificazione di tutti i task schedulati automatici
- Analisi delle frequenze di esecuzione e sovrapposizioni
- Detection di performance bottlenecks nei task ricorrenti
- Visibilità completa sui processi batch e maintenance

## Complessità di Implementazione
**🟡 Media** - Pattern consolidati con analisi di annotazioni specifiche

## Tempo di Realizzazione Stimato
**5-7 giorni** di sviluppo

## Struttura delle Classi

### Core Interface
```java
package it.denzosoft.jreverse.analyzer.scheduling;

public interface SchedulingEntrypointAnalyzer {
    boolean canAnalyze(JarContent jarContent);
    SchedulingAnalysisResult analyze(JarContent jarContent);
    List<SchedulingEntrypointInfo> findSchedulingEntrypoints(CtClass[] classes);
}
```

### Implementazione Javassist
```java
package it.denzosoft.jreverse.analyzer.scheduling;

public class JavassistSchedulingEntrypointAnalyzer implements SchedulingEntrypointAnalyzer {
    
    private static final String SCHEDULED_ANNOTATION = "org.springframework.scheduling.annotation.Scheduled";
    private static final String SCHEDULES_ANNOTATION = "org.springframework.scheduling.annotation.Schedules";
    private static final String ENABLE_SCHEDULING = "org.springframework.scheduling.annotation.EnableScheduling";
    
    @Override
    public SchedulingAnalysisResult analyze(JarContent jarContent) {
        SchedulingAnalysisResult.Builder resultBuilder = SchedulingAnalysisResult.builder();
        
        try {
            CtClass[] classes = jarContent.getAllClasses();
            
            // Verifica se scheduling è abilitato
            boolean schedulingEnabled = isSchedulingEnabled(classes);
            resultBuilder.schedulingEnabled(schedulingEnabled);
            
            if (!schedulingEnabled) {
                resultBuilder.addWarning("Scheduling not enabled - @EnableScheduling not found");
            }
            
            // Trova tutti gli entrypoint scheduling
            List<SchedulingEntrypointInfo> entrypoints = findSchedulingEntrypoints(classes);
            resultBuilder.schedulingEntrypoints(entrypoints);
            
            // Analizza configurazioni di scheduling
            List<SchedulingConfigInfo> configs = analyzeSchedulingConfigurations(classes);
            resultBuilder.schedulingConfigurations(configs);
            
            // Identifica potenziali problemi
            List<SchedulingIssue> issues = identifySchedulingIssues(entrypoints);
            resultBuilder.schedulingIssues(issues);
            
            // Calcola statistiche
            SchedulingStatistics stats = calculateSchedulingStatistics(entrypoints, configs);
            resultBuilder.statistics(stats);
            
        } catch (Exception e) {
            resultBuilder.addError("Error analyzing scheduling: " + e.getMessage());
        }
        
        return resultBuilder.build();
    }
    
    @Override
    public List<SchedulingEntrypointInfo> findSchedulingEntrypoints(CtClass[] classes) {
        List<SchedulingEntrypointInfo> entrypoints = new ArrayList<>();
        
        for (CtClass ctClass : classes) {
            try {
                CtMethod[] methods = ctClass.getDeclaredMethods();
                
                for (CtMethod method : methods) {
                    // Analizza @Scheduled
                    if (method.hasAnnotation(SCHEDULED_ANNOTATION)) {
                        SchedulingEntrypointInfo entrypoint = analyzeScheduledMethod(method, ctClass);
                        entrypoints.add(entrypoint);
                    }
                    
                    // Analizza @Schedules (contenitore per multipli @Scheduled)
                    if (method.hasAnnotation(SCHEDULES_ANNOTATION)) {
                        List<SchedulingEntrypointInfo> multipleEntrypoints = analyzeSchedulesMethod(method, ctClass);
                        entrypoints.addAll(multipleEntrypoints);
                    }
                }
                
            } catch (Exception e) {
                // Log error but continue processing
            }
        }
        
        return entrypoints;
    }
}
```

## Modelli di Dati

### SchedulingEntrypointInfo
```java
package it.denzosoft.jreverse.analyzer.scheduling;

public class SchedulingEntrypointInfo {
    
    // Identificazione metodo
    private final String className;
    private final String methodName;
    private final String methodSignature;
    
    // Configurazione scheduling
    private final SchedulingType schedulingType;
    private final String cronExpression;
    private final long fixedDelay;
    private final long fixedRate;
    private final long initialDelay;
    private final String timeZone;
    
    // Analisi contenuto
    private final boolean isAsync;
    private final int estimatedComplexity;
    private final boolean hasExceptionHandling;
    private final boolean hasTransactionSupport;
    private final List<String> databaseOperations;
    private final List<String> externalServiceCalls;
    
    // Metadati aggiuntivi
    private final boolean hasConditionalExecution;
    private final String condition;
    private final Set<String> profiles;
    
    public enum SchedulingType {
        CRON("Cron Expression"),
        FIXED_RATE("Fixed Rate"),
        FIXED_DELAY("Fixed Delay"),
        INITIAL_DELAY("Initial Delay");
        
        private final String displayName;
        
        SchedulingType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() { return displayName; }
    }
}
```

### SchedulingAnalysisResult
```java
package it.denzosoft.jreverse.analyzer.scheduling;

public class SchedulingAnalysisResult {
    
    private final boolean schedulingEnabled;
    private final List<SchedulingEntrypointInfo> schedulingEntrypoints;
    private final List<SchedulingConfigInfo> schedulingConfigurations;
    private final List<SchedulingIssue> schedulingIssues;
    private final SchedulingStatistics statistics;
    private final List<String> warnings;
    private final List<String> errors;
    
    // Builder pattern implementation
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private boolean schedulingEnabled = false;
        private List<SchedulingEntrypointInfo> schedulingEntrypoints = new ArrayList<>();
        private List<SchedulingConfigInfo> schedulingConfigurations = new ArrayList<>();
        private List<SchedulingIssue> schedulingIssues = new ArrayList<>();
        private SchedulingStatistics statistics;
        private List<String> warnings = new ArrayList<>();
        private List<String> errors = new ArrayList<>();
        
        public Builder schedulingEnabled(boolean enabled) {
            this.schedulingEnabled = enabled;
            return this;
        }
        
        public Builder schedulingEntrypoints(List<SchedulingEntrypointInfo> entrypoints) {
            this.schedulingEntrypoints = new ArrayList<>(entrypoints);
            return this;
        }
        
        public Builder addSchedulingEntrypoint(SchedulingEntrypointInfo entrypoint) {
            this.schedulingEntrypoints.add(entrypoint);
            return this;
        }
        
        public Builder addWarning(String warning) {
            this.warnings.add(warning);
            return this;
        }
        
        public Builder addError(String error) {
            this.errors.add(error);
            return this;
        }
        
        public SchedulingAnalysisResult build() {
            return new SchedulingAnalysisResult(this);
        }
    }
}
```

## Algoritmi di Analisi

### 1. Detection di Overlapping Tasks
```java
private List<SchedulingIssue> detectOverlappingTasks(List<SchedulingEntrypointInfo> entrypoints) {
    List<SchedulingIssue> issues = new ArrayList<>();
    
    for (SchedulingEntrypointInfo entrypoint : entrypoints) {
        if (entrypoint.getSchedulingType() == SchedulingType.FIXED_RATE) {
            if (!entrypoint.isAsync() && entrypoint.getEstimatedComplexity() > COMPLEX_THRESHOLD) {
                SchedulingIssue issue = SchedulingIssue.builder()
                    .type(SchedulingIssueType.POTENTIAL_OVERLAP)
                    .className(entrypoint.getClassName())
                    .methodName(entrypoint.getMethodName())
                    .severity(IssueSeverity.HIGH)
                    .description("Fixed rate scheduling with complex synchronous task may cause overlaps")
                    .recommendation("Consider using fixedDelay or make task async with @Async")
                    .build();
                issues.add(issue);
            }
        }
    }
    
    return issues;
}
```

### 2. Cron Expression Validation
```java
private List<SchedulingIssue> validateCronExpressions(List<SchedulingEntrypointInfo> entrypoints) {
    List<SchedulingIssue> issues = new ArrayList<>();
    
    for (SchedulingEntrypointInfo entrypoint : entrypoints) {
        if (entrypoint.getSchedulingType() == SchedulingType.CRON) {
            String cronExpression = entrypoint.getCronExpression();
            
            if (!isValidCronExpression(cronExpression)) {
                SchedulingIssue issue = SchedulingIssue.builder()
                    .type(SchedulingIssueType.INVALID_CRON_EXPRESSION)
                    .className(entrypoint.getClassName())
                    .methodName(entrypoint.getMethodName())
                    .severity(IssueSeverity.CRITICAL)
                    .description("Invalid cron expression: " + cronExpression)
                    .recommendation("Fix cron expression syntax")
                    .build();
                issues.add(issue);
            }
            
            if (isTooFrequentExecution(cronExpression)) {
                SchedulingIssue issue = SchedulingIssue.builder()
                    .type(SchedulingIssueType.TOO_FREQUENT_EXECUTION)
                    .className(entrypoint.getClassName())
                    .methodName(entrypoint.getMethodName())
                    .severity(IssueSeverity.MEDIUM)
                    .description("Very frequent cron execution may impact performance")
                    .recommendation("Review execution frequency necessity")
                    .build();
                issues.add(issue);
            }
        }
    }
    
    return issues;
}
```

## Test Strategy

### Unit Tests
```java
@Test
public void testScheduledMethodDetection() {
    // Given
    CtClass scheduledClass = createMockClassWithScheduledMethod();
    
    // When
    List<SchedulingEntrypointInfo> entrypoints = analyzer.findSchedulingEntrypoints(new CtClass[]{scheduledClass});
    
    // Then
    assertThat(entrypoints).hasSize(1);
    assertThat(entrypoints.get(0).getSchedulingType()).isEqualTo(SchedulingType.FIXED_RATE);
    assertThat(entrypoints.get(0).getFixedRate()).isEqualTo(5000);
}

@Test
public void testSchedulingNotEnabledWarning() {
    // Given
    CtClass[] classesWithoutEnableScheduling = createClassesWithoutEnableScheduling();
    
    // When
    SchedulingAnalysisResult result = analyzer.analyze(createJarContent(classesWithoutEnableScheduling));
    
    // Then
    assertThat(result.isSchedulingEnabled()).isFalse();
    assertThat(result.getWarnings()).contains("Scheduling not enabled - @EnableScheduling not found");
}
```

## Integrazione con Report Esistenti

### Report da Aggiornare
1. **Report 10 (Sequenze Chiamate Asincrone)**: Aggiungere sezione dedicata ai scheduled tasks
2. **Report 37 (Punti Critici Performance)**: Includere analisi performance dei task schedulati
3. **Report 02 (Call Graph HTTP)**: Estendere per includere flussi iniziati da scheduled tasks

### Nuovi Report da Creare
- **Report 51: Scheduled Tasks Analysis** - Report dedicato all'analisi completa dei task schedulati
- **Report 52: Batch Processing Flows** - Analisi dei flussi batch e maintenance

## Metriche di Qualità

### Algoritmo di Scoring (0-100)
```java
public int calculateSchedulingQualityScore(SchedulingAnalysisResult result) {
    double score = 100.0;
    
    // Penalizzazioni
    score -= result.getInvalidCronExpressions() * 25;     // -25 per cron expression invalide
    score -= result.getPotentialOverlaps() * 20;          // -20 per potenziali overlap
    score -= result.getTooFrequentExecutions() * 15;      // -15 per esecuzioni troppo frequenti
    score -= result.getTasksWithoutExceptionHandling() * 12; // -12 per task senza exception handling
    score -= result.getComplexSynchronousTasks() * 10;    // -10 per task sincroni complessi
    
    // Bonus
    if (result.isSchedulingEnabled()) score += 10;        // +10 se scheduling è abilitato
    score += result.getAsyncScheduledTasks() * 3;         // +3 per task async appropriati
    score += result.getTasksWithTransactionSupport() * 2; // +2 per task con supporto transazionale
    
    return Math.max(0, Math.min(100, (int) score));
}
```

## Considerazioni Architetturali

### Performance
- Caching delle annotazioni per evitare riparse multiple
- Lazy loading delle configurazioni complesse
- Ottimizzazione delle espressioni regolari per cron parsing

### Estensibilità
- Support per custom scheduling annotations
- Plugin architecture per validatori esterni
- Configurabilità delle soglie di warning

### Error Handling
- Graceful degradation per cron expression malformate
- Recovery da errori di parsing delle annotazioni
- Logging dettagliato per troubleshooting