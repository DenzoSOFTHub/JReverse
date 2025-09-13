# Report 19: Mapping delle Eccezioni Gestite

**Valore**: ⭐⭐⭐⭐ **Complessità**: 🟡 Media **Tempo**: 5-6 giorni
**Tags**: `#exception-handling` `#error-management` `#controller-advice`

## Descrizione

Analizza la gestione delle eccezioni nell'applicazione Spring, mappando @ControllerAdvice, @ExceptionHandler, try-catch patterns e strategie di error handling per identificare potenziali punti di fallimento e migliorare la robustezza dell'applicazione.

## Sezioni del Report

### 1. Exception Handler Analysis
- @ControllerAdvice e @RestControllerAdvice classes
- @ExceptionHandler method mappings
- Global vs local exception handling
- Exception hierarchy coverage

### 2. Try-Catch Pattern Analysis  
- Try-catch blocks distribution
- Exception types caught
- Error recovery strategies
- Resource cleanup patterns

### 3. Custom Exception Analysis
- Custom exception classes definition
- Exception inheritance hierarchy
- Business logic exception patterns
- Error code/message standardization

### 4. Error Response Patterns
- HTTP status code mapping
- Error response structure consistency
- Localization and internationalization
- Logging and monitoring integration

## Implementazione Javassist Completa

```java
public class ExceptionMappingAnalyzer {
    
    public ExceptionMappingReport analyzeExceptionMapping(CtClass[] classes) {
        ExceptionMappingReport report = new ExceptionMappingReport();
        
        for (CtClass ctClass : classes) {
            analyzeExceptionHandling(ctClass, report);
        }
        
        analyzeExceptionHierarchy(report);
        identifyExceptionIssues(report);
        
        return report;
    }
    
    private void analyzeExceptionHandling(CtClass ctClass, ExceptionMappingReport report) {
        try {
            // Analizza @ControllerAdvice classes
            if (ctClass.hasAnnotation("org.springframework.web.bind.annotation.ControllerAdvice") ||
                ctClass.hasAnnotation("org.springframework.web.bind.annotation.RestControllerAdvice")) {
                analyzeControllerAdvice(ctClass, report);
            }
            
            // Analizza controller classes per local exception handling
            if (isControllerClass(ctClass)) {
                analyzeControllerExceptionHandling(ctClass, report);
            }
            
            // Analizza custom exception classes
            if (isExceptionClass(ctClass)) {
                analyzeCustomException(ctClass, report);
            }
            
            // Analizza try-catch patterns in business logic
            analyzeTryCatchPatterns(ctClass, report);
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi exception handling: " + e.getMessage());
        }
    }
    
    private void analyzeControllerAdvice(CtClass ctClass, ExceptionMappingReport report) {
        try {
            ControllerAdviceInfo adviceInfo = new ControllerAdviceInfo();
            adviceInfo.setClassName(ctClass.getName());
            
            // Determina scope del ControllerAdvice
            if (ctClass.hasAnnotation("org.springframework.web.bind.annotation.ControllerAdvice")) {
                Annotation annotation = ctClass.getAnnotation("org.springframework.web.bind.annotation.ControllerAdvice");
                String[] basePackages = extractStringArrayAnnotationValue(annotation, "basePackages");
                Class<?>[] assignableTypes = extractClassArrayAnnotationValue(annotation, "assignableTypes");
                
                adviceInfo.setBasePackages(basePackages);
                adviceInfo.setAssignableTypes(assignableTypes);
                adviceInfo.setGlobal(basePackages.length == 0 && assignableTypes.length == 0);
            }
            
            CtMethod[] methods = ctClass.getDeclaredMethods();
            
            for (CtMethod method : methods) {
                if (method.hasAnnotation("org.springframework.web.bind.annotation.ExceptionHandler")) {
                    analyzeExceptionHandlerMethod(method, adviceInfo, report);
                }
            }
            
            // Verifica coverage delle eccezioni
            if (adviceInfo.getExceptionHandlers().isEmpty()) {
                ExceptionIssue issue = new ExceptionIssue();
                issue.setType(IssueType.EMPTY_CONTROLLER_ADVICE);
                issue.setClassName(ctClass.getName());
                issue.setSeverity(Severity.MEDIUM);
                issue.setDescription("@ControllerAdvice without @ExceptionHandler methods");
                issue.setRecommendation("Add @ExceptionHandler methods or remove unused @ControllerAdvice");
                
                report.addExceptionIssue(issue);
            }
            
            report.addControllerAdvice(adviceInfo);
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi ControllerAdvice: " + e.getMessage());
        }
    }
    
    private void analyzeExceptionHandlerMethod(CtMethod method, ControllerAdviceInfo adviceInfo, ExceptionMappingReport report) {
        try {
            ExceptionHandlerInfo handlerInfo = new ExceptionHandlerInfo();
            handlerInfo.setMethodName(method.getName());
            handlerInfo.setMethodSignature(method.getSignature());
            
            // Estrai exception types da @ExceptionHandler
            Annotation exceptionHandler = method.getAnnotation("org.springframework.web.bind.annotation.ExceptionHandler");
            Class<?>[] exceptionTypes = extractClassArrayAnnotationValue(exceptionHandler, "value");
            handlerInfo.setHandledExceptions(exceptionTypes);
            
            // Analizza response status
            if (method.hasAnnotation("org.springframework.web.bind.annotation.ResponseStatus")) {
                Annotation responseStatus = method.getAnnotation("org.springframework.web.bind.annotation.ResponseStatus");
                String statusCode = extractAnnotationValue(responseStatus, "value");
                handlerInfo.setResponseStatus(statusCode);
            }
            
            // Analizza return type per response structure
            String returnType = method.getReturnType().getName();
            handlerInfo.setResponseType(returnType);
            
            // Verifica se il metodo gestisce logging
            boolean hasLogging = hasLoggingCalls(method);
            handlerInfo.setHasLogging(hasLogging);
            
            if (!hasLogging) {
                ExceptionIssue issue = new ExceptionIssue();
                issue.setType(IssueType.NO_LOGGING_IN_EXCEPTION_HANDLER);
                issue.setClassName(method.getDeclaringClass().getName());
                issue.setMethodName(method.getName());
                issue.setSeverity(Severity.MEDIUM);
                issue.setDescription("Exception handler without logging");
                issue.setRecommendation("Add appropriate logging for monitoring and debugging");
                
                report.addExceptionIssue(issue);
            }
            
            // Verifica gestione generica (Exception.class)
            for (Class<?> exceptionType : exceptionTypes) {
                if ("java.lang.Exception".equals(exceptionType.getName())) {
                    ExceptionIssue issue = new ExceptionIssue();
                    issue.setType(IssueType.GENERIC_EXCEPTION_HANDLER);
                    issue.setClassName(method.getDeclaringClass().getName());
                    issue.setMethodName(method.getName());
                    issue.setSeverity(Severity.LOW);
                    issue.setDescription("Generic Exception handler may mask specific errors");
                    issue.setRecommendation("Handle specific exception types when possible");
                    
                    report.addExceptionIssue(issue);
                }
            }
            
            adviceInfo.addExceptionHandler(handlerInfo);
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi ExceptionHandler method: " + e.getMessage());
        }
    }
    
    private void analyzeTryCatchPatterns(CtClass ctClass, ExceptionMappingReport report) {
        try {
            CtMethod[] methods = ctClass.getDeclaredMethods();
            
            for (CtMethod method : methods) {
                method.instrument(new ExprEditor() {
                    @Override
                    public void edit(Handler handler) throws CannotCompileException {
                        try {
                            TryCatchInfo tryCatchInfo = new TryCatchInfo();
                            tryCatchInfo.setClassName(ctClass.getName());
                            tryCatchInfo.setMethodName(method.getName());
                            
                            CtClass exceptionType = handler.getType();
                            if (exceptionType != null) {
                                tryCatchInfo.setCaughtExceptionType(exceptionType.getName());
                                
                                // Verifica se è un catch generico
                                if ("java.lang.Exception".equals(exceptionType.getName()) || 
                                    "java.lang.Throwable".equals(exceptionType.getName())) {
                                    
                                    ExceptionIssue issue = new ExceptionIssue();
                                    issue.setType(IssueType.GENERIC_CATCH_BLOCK);
                                    issue.setClassName(ctClass.getName());
                                    issue.setMethodName(method.getName());
                                    issue.setSeverity(Severity.MEDIUM);
                                    issue.setDescription("Generic catch block may hide specific errors");
                                    issue.setRecommendation("Catch specific exception types when possible");
                                    
                                    report.addExceptionIssue(issue);
                                }
                            }
                            
                            // Analizza il contenuto del catch block
                            String handlerCode = handler.toString();
                            boolean hasLogging = handlerCode.contains("log") || handlerCode.contains("Log");
                            boolean hasRethrow = handlerCode.contains("throw");
                            boolean isEmpty = handlerCode.trim().length() < 50; // Euristica per catch vuoti
                            
                            tryCatchInfo.setHasLogging(hasLogging);
                            tryCatchInfo.setHasRethrow(hasRethrow);
                            tryCatchInfo.setIsEmpty(isEmpty);
                            
                            if (isEmpty) {
                                ExceptionIssue issue = new ExceptionIssue();
                                issue.setType(IssueType.EMPTY_CATCH_BLOCK);
                                issue.setClassName(ctClass.getName());
                                issue.setMethodName(method.getName());
                                issue.setSeverity(Severity.HIGH);
                                issue.setDescription("Empty catch block swallows exceptions");
                                issue.setRecommendation("Add appropriate exception handling or logging");
                                
                                report.addExceptionIssue(issue);
                            }
                            
                            report.addTryCatchInfo(tryCatchInfo);
                            
                        } catch (Exception e) {
                            report.addError("Errore nell'analisi try-catch: " + e.getMessage());
                        }
                    }
                });
            }
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi try-catch patterns: " + e.getMessage());
        }
    }
    
    private void analyzeCustomException(CtClass ctClass, ExceptionMappingReport report) {
        try {
            CustomExceptionInfo exceptionInfo = new CustomExceptionInfo();
            exceptionInfo.setClassName(ctClass.getName());
            
            // Analizza hierarchy
            CtClass superClass = ctClass.getSuperclass();
            if (superClass != null) {
                exceptionInfo.setParentExceptionType(superClass.getName());
                
                // Verifica se estende appropriatamente RuntimeException o Exception
                boolean extendsRuntimeException = isSubclassOf(ctClass, "java.lang.RuntimeException");
                boolean extendsCheckedException = isSubclassOf(ctClass, "java.lang.Exception") && !extendsRuntimeException;
                
                exceptionInfo.setRuntimeException(extendsRuntimeException);
                exceptionInfo.setCheckedException(extendsCheckedException);
            }
            
            // Analizza constructors
            CtConstructor[] constructors = ctClass.getDeclaredConstructors();
            boolean hasMessageConstructor = false;
            boolean hasCauseConstructor = false;
            
            for (CtConstructor constructor : constructors) {
                CtClass[] paramTypes = constructor.getParameterTypes();
                
                if (paramTypes.length == 1 && "java.lang.String".equals(paramTypes[0].getName())) {
                    hasMessageConstructor = true;
                }
                
                if (paramTypes.length == 2 && "java.lang.String".equals(paramTypes[0].getName()) && 
                    "java.lang.Throwable".equals(paramTypes[1].getName())) {
                    hasCauseConstructor = true;
                }
            }
            
            exceptionInfo.setHasMessageConstructor(hasMessageConstructor);
            exceptionInfo.setHasCauseConstructor(hasCauseConstructor);
            
            // Verifica completezza constructors
            if (!hasMessageConstructor || !hasCauseConstructor) {
                ExceptionIssue issue = new ExceptionIssue();
                issue.setType(IssueType.INCOMPLETE_EXCEPTION_CONSTRUCTORS);
                issue.setClassName(ctClass.getName());
                issue.setSeverity(Severity.LOW);
                issue.setDescription("Custom exception missing standard constructors");
                issue.setRecommendation("Add constructors for message and cause parameters");
                
                report.addExceptionIssue(issue);
            }
            
            report.addCustomException(exceptionInfo);
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi custom exception: " + e.getMessage());
        }
    }
}

public class ExceptionMappingReport {
    private List<ControllerAdviceInfo> controllerAdvices = new ArrayList<>();
    private List<ExceptionHandlerInfo> exceptionHandlers = new ArrayList<>();
    private List<TryCatchInfo> tryCatchInfos = new ArrayList<>();
    private List<CustomExceptionInfo> customExceptions = new ArrayList<>();
    private List<ExceptionIssue> exceptionIssues = new ArrayList<>();
    private ExceptionStatistics statistics;
    private List<String> errors = new ArrayList<>();
    
    public static class ControllerAdviceInfo {
        private String className;
        private String[] basePackages;
        private Class<?>[] assignableTypes;
        private boolean isGlobal;
        private List<ExceptionHandlerInfo> exceptionHandlers = new ArrayList<>();
    }
    
    public static class ExceptionHandlerInfo {
        private String methodName;
        private String methodSignature;
        private Class<?>[] handledExceptions;
        private String responseStatus;
        private String responseType;
        private boolean hasLogging;
    }
    
    public static class TryCatchInfo {
        private String className;
        private String methodName;
        private String caughtExceptionType;
        private boolean hasLogging;
        private boolean hasRethrow;
        private boolean isEmpty;
    }
    
    public static class ExceptionIssue {
        private IssueType type;
        private String className;
        private String methodName;
        private Severity severity;
        private String description;
        private String recommendation;
    }
    
    public enum IssueType {
        EMPTY_CONTROLLER_ADVICE,
        NO_LOGGING_IN_EXCEPTION_HANDLER,
        GENERIC_EXCEPTION_HANDLER,
        GENERIC_CATCH_BLOCK,
        EMPTY_CATCH_BLOCK,
        INCOMPLETE_EXCEPTION_CONSTRUCTORS,
        MISSING_GLOBAL_EXCEPTION_HANDLER,
        INCONSISTENT_ERROR_RESPONSES
    }
    
    public enum Severity {
        CRITICAL, HIGH, MEDIUM, LOW
    }
}
```

## Metriche di Qualità del Codice

### Algoritmo di Calcolo (0-100)

```java
public int calculateExceptionHandlingQualityScore(ExceptionMappingReport result) {
    double score = 100.0;
    
    // Penalizzazioni per problemi exception handling critici
    score -= result.getEmptyCatchBlocks() * 20;                   // -20 per catch block vuoti
    score -= result.getGenericCatchBlocks() * 12;                 // -12 per catch generici (Exception)
    score -= result.getExceptionHandlersWithoutLogging() * 10;    // -10 per handler senza logging
    score -= result.getGenericExceptionHandlers() * 8;            // -8 per @ExceptionHandler generici
    score -= result.getEmptyControllerAdvices() * 15;             // -15 per @ControllerAdvice vuoti
    score -= result.getIncompleteCustomExceptions() * 5;          // -5 per custom exception incomplete
    score -= result.getInconsistentErrorResponses() * 7;          // -7 per response inconsistenti
    
    // Bonus per buone pratiche exception handling
    score += result.getProperExceptionHandlers() * 3;             // +3 per exception handler appropriati
    score += result.getCustomExceptionsWithLogging() * 2;         // +2 per custom exception con logging
    score += result.getConsistentErrorResponseStructure() * 2;    // +2 per struttura error response consistente
    score += result.getSpecificExceptionHandling() * 1;           // +1 per gestione specifica (non generica)
    
    return Math.max(0, Math.min(100, (int) score));
}
```

### Soglie di Valutazione
- **0-40**: 🔴 CRITICO - Gestione eccezioni inadeguata, rischio stabilità applicazione
- **41-60**: 🟡 SUFFICIENTE - Exception handling di base ma con lacune significative
- **61-80**: 🟢 BUONO - Gestione errori strutturata con alcuni miglioramenti necessari
- **81-100**: ⭐ ECCELLENTE - Exception handling robusto e best practices implementate

## Segnalazioni per Gravità

### 🔴 GRAVITÀ CRITICA (Score Impact: -15 to -20)
1. **Catch blocks vuoti**
   - Descrizione: Blocchi catch che non gestiscono l'eccezione catturata
   - Rischio: Errori silenti, problemi nascosti, debugging difficile
   - Soluzione: Aggiungere logging appropriato o handling specifico

2. **@ControllerAdvice vuoti**
   - Descrizione: Classi @ControllerAdvice senza @ExceptionHandler methods
   - Rischio: Configuration inutile, aspettative non soddisfatte
   - Soluzione: Aggiungere handler o rimuovere annotation non utilizzate

### 🟠 GRAVITÀ ALTA (Score Impact: -10 to -12)  
3. **Catch blocks generici**
   - Descrizione: Catch di Exception o Throwable invece di tipi specifici
   - Rischio: Mascheramento errori specifici, handling inappropriato
   - Soluzione: Catturare tipi di eccezione specifici quando possibile

4. **Exception handlers senza logging**
   - Descrizione: @ExceptionHandler methods senza logging degli errori
   - Rischio: Perdita informazioni per monitoring e debugging
   - Soluzione: Implementare logging appropriato per tutti gli exception handler

### 🟡 GRAVITÀ MEDIA (Score Impact: -7 to -8)
5. **@ExceptionHandler generici**
   - Descrizione: Handler che gestiscono Exception.class genericamente
   - Rischio: Response inappropriata per errori specifici
   - Soluzione: Creare handler specifici per tipi di errore diversi

6. **Error response inconsistenti**
   - Descrizione: Strutture di risposta error diverse tra handler
   - Rischio: Client confusion, difficile error handling lato client
   - Soluzione: Standardizzare struttura error response

### 🔵 GRAVITÀ BASSA (Score Impact: -5)
7. **Custom exceptions incomplete**
   - Descrizione: Custom exception senza constructors standard (message, cause)
   - Rischio: Utilizzo limitato, difficile debugging
   - Soluzione: Aggiungere constructors standard per message e causa

## Metriche di Valore

- **Application Stability**: Riduce crash e comportamenti inaspettati
- **Debugging Efficiency**: Facilita identificazione e risoluzione problemi  
- **User Experience**: Fornisce error message significativi agli utenti
- **Monitoring Capability**: Enable proper error tracking e alerting

## Tags per Classificazione
`#exception-handling` `#error-management` `#controller-advice` `#robustness` `#high-value` `#medium-complexity`