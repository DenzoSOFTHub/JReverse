# Report 48: Gestione delle Eccezioni

**Valore**: ⭐⭐⭐⭐ **Complessità**: 🟡 Media **Tempo**: 4-6 giorni
**Tags**: `#exception-handling` `#error-management` `#robustness` `#best-practices`

## Descrizione

Analizza la strategia di gestione delle eccezioni nell'applicazione, identificando pattern di error handling, eccezioni non gestite, strategie di recovery e conformità alle best practices.

## Sezioni del Report

### 1. Exception Handling Overview
- Gerarchia delle eccezioni custom
- Global exception handlers
- Strategie di error handling per layer
- Exception propagation patterns

### 2. Exception Analysis
- Eccezioni checked vs unchecked
- Eccezioni business vs technical
- Exception handling patterns utilizzati
- Coverage dell'error handling

### 3. Error Recovery Strategies
- Retry mechanisms
- Circuit breaker patterns
- Fallback strategies
- Error logging e monitoring

### 4. Best Practices Compliance
- Proper exception wrapping
- Meaningful error messages
- Resource cleanup (try-with-resources)
- Error handling testing

## Implementazione con Javassist

```java
public class ExceptionHandlingAnalyzer {
    
    private static final Set<String> GENERIC_EXCEPTION_TYPES = Set.of(
        "java.lang.Exception",
        "java.lang.RuntimeException", 
        "java.lang.Throwable"
    );
    
    private static final Set<String> BUSINESS_EXCEPTION_PATTERNS = Set.of(
        "BusinessException", "DomainException", "ValidationException",
        "AuthenticationException", "AuthorizationException", "ServiceException"
    );
    
    private static final Set<String> TECHNICAL_EXCEPTION_PATTERNS = Set.of(
        "DatabaseException", "NetworkException", "FileException", 
        "ConfigurationException", "IntegrationException", "SystemException"
    );
    
    private static final Map<String, Integer> EXCEPTION_SEVERITY_PENALTIES = Map.of(
        "GENERIC_CATCH", -15,           // Catch generico (Exception, RuntimeException)
        "EMPTY_CATCH", -25,             // Catch vuoto senza gestione
        "SWALLOWED_EXCEPTION", -20,     // Eccezione ignorata silenziosamente
        "IMPROPER_LOGGING", -10,        // Logging inadeguato
        "MISSING_FINALLY", -8,          // Mancanza di blocco finally per cleanup
        "THROW_IN_FINALLY", -30,        // Throw in blocco finally (maschera eccezioni)
        "NO_RECOVERY_LOGIC", -12,       // Mancanza di logica di recovery
        "POOR_ERROR_MESSAGE", -8,       // Messaggi di errore non informativi
        "EXCEPTION_AS_FLOW_CONTROL", -20, // Uso eccezioni per controllo flusso
        "RESOURCE_LEAK", -25,           // Mancato cleanup risorse
        "INTERRUPTED_EXCEPTION_MISHANDLING", -15 // Gestione scorretta InterruptedException
    );
    
    private static final Map<String, Integer> EXCEPTION_HANDLING_BONUSES = Map.of(
        "SPECIFIC_EXCEPTION_HANDLING", 5,  // Gestione eccezioni specifiche
        "PROPER_LOGGING", 8,               // Logging completo e strutturato
        "RECOVERY_STRATEGY", 12,           // Implementa strategie di recovery
        "TRY_WITH_RESOURCES", 10,          // Uso try-with-resources
        "CUSTOM_EXCEPTIONS", 8,            // Definisce eccezioni custom significative
        "EXCEPTION_CHAINING", 6,           // Catena correttamente le eccezioni
        "GRACEFUL_DEGRADATION", 15,        // Implementa graceful degradation
        "CIRCUIT_BREAKER", 12,             // Pattern circuit breaker
        "RETRY_MECHANISM", 10,             // Meccanismi di retry
        "MONITORING_INTEGRATION", 8,       // Integrazione con sistemi di monitoring
        "CORRELATION_ID_TRACKING", 6       // Tracciamento correlation ID
    );
    
    public ExceptionHandlingReport analyzeExceptionHandling(CtClass[] classes) {
        ExceptionHandlingReport report = new ExceptionHandlingReport();
        
        for (CtClass ctClass : classes) {
            analyzeExceptionHierarchy(ctClass, report);
            analyzeExceptionHandlers(ctClass, report);
            analyzeTryCatchBlocks(ctClass, report);
            analyzeThrowsDeclarations(ctClass, report);
            analyzeResourceManagement(ctClass, report);
            analyzeErrorRecoveryPatterns(ctClass, report);
        }
        
        evaluateExceptionHandlingPractices(report);
        calculateQualityScore(report);
        return report;
    }
    
    private void analyzeExceptionHierarchy(CtClass ctClass, ExceptionHandlingReport report) {
        try {
            // Verifica se è una classe di eccezione
            if (isExceptionClass(ctClass)) {
                CustomException customException = new CustomException();
                customException.setClassName(ctClass.getName());
                customException.setSuperClass(ctClass.getSuperclass().getName());
                customException.setChecked(isCheckedException(ctClass));
                
                // Analizza costruttori
                CtConstructor[] constructors = ctClass.getDeclaredConstructors();
                for (CtConstructor constructor : constructors) {
                    ExceptionConstructor exceptionConstructor = new ExceptionConstructor();
                    exceptionConstructor.setParameterTypes(getParameterTypeNames(constructor));
                    customException.addConstructor(exceptionConstructor);
                }
                
                // Analizza campi e metodi aggiuntivi
                analyzeExceptionFields(ctClass, customException);
                analyzeExceptionMethods(ctClass, customException);
                
                report.addCustomException(customException);
            }
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi exception hierarchy: " + e.getMessage());
        }
    }
    
    private void analyzeExceptionHandlers(CtClass ctClass, ExceptionHandlingReport report) {
        try {
            CtMethod[] methods = ctClass.getDeclaredMethods();
            
            for (CtMethod method : methods) {
                // Cerca @ExceptionHandler
                AnnotationsAttribute attr = (AnnotationsAttribute) 
                    method.getMethodInfo().getAttribute(AnnotationsAttribute.visibleTag);
                
                if (attr != null) {
                    for (Annotation annotation : attr.getAnnotations()) {
                        if (annotation.getTypeName().equals("org.springframework.web.bind.annotation.ExceptionHandler")) {
                            ExceptionHandler handler = new ExceptionHandler();
                            handler.setClassName(ctClass.getName());
                            handler.setMethodName(method.getName());
                            handler.setReturnType(method.getReturnType().getName());
                            
                            // Estrai i tipi di eccezione gestite
                            MemberValue value = annotation.getMemberValue("value");
                            if (value != null) {
                                handler.setHandledExceptions(extractExceptionTypes(value));
                            }
                            
                            analyzeHandlerLogic(method, handler);
                            report.addExceptionHandler(handler);
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi exception handlers: " + e.getMessage());
        }
    }
    
    private void analyzeTryCatchBlocks(CtClass ctClass, ExceptionHandlingReport report) {
        try {
            CtMethod[] methods = ctClass.getDeclaredMethods();
            
            for (CtMethod method : methods) {
                if (method.getMethodInfo().getCodeAttribute() != null) {
                    ExceptionTable exceptionTable = method.getMethodInfo()
                        .getCodeAttribute().getExceptionTable();
                    
                    if (exceptionTable != null && exceptionTable.size() > 0) {
                        MethodExceptionHandling methodHandling = new MethodExceptionHandling();
                        methodHandling.setClassName(ctClass.getName());
                        methodHandling.setMethodName(method.getName());
                        
                        for (int i = 0; i < exceptionTable.size(); i++) {
                            TryCatchBlock tryCatch = new TryCatchBlock();
                            tryCatch.setStartPc(exceptionTable.startPc(i));
                            tryCatch.setEndPc(exceptionTable.endPc(i));
                            tryCatch.setHandlerPc(exceptionTable.handlerPc(i));
                            
                            int catchType = exceptionTable.catchType(i);
                            if (catchType != 0) {
                                ConstPool constPool = method.getMethodInfo().getConstPool();
                                String exceptionType = constPool.getClassInfo(catchType);
                                tryCatch.setCaughtExceptionType(exceptionType);
                            }
                            
                            methodHandling.addTryCatchBlock(tryCatch);
                        }
                        
                        // Analizza la logica di gestione
                        analyzeCatchBlockLogic(method, methodHandling);
                        report.addMethodExceptionHandling(methodHandling);
                    }
                }
            }
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi try-catch blocks: " + e.getMessage());
        }
    }
    
    private void analyzeThrowsDeclarations(CtClass ctClass, ExceptionHandlingReport report) {
        try {
            CtMethod[] methods = ctClass.getDeclaredMethods();
            
            for (CtMethod method : methods) {
                CtClass[] exceptionTypes = method.getExceptionTypes();
                
                if (exceptionTypes.length > 0) {
                    MethodThrowsDeclaration throwsDecl = new MethodThrowsDeclaration();
                    throwsDecl.setClassName(ctClass.getName());
                    throwsDecl.setMethodName(method.getName());
                    
                    for (CtClass exceptionType : exceptionTypes) {
                        throwsDecl.addThrownException(exceptionType.getName());
                    }
                    
                    report.addMethodThrowsDeclaration(throwsDecl);
                }
            }
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi throws declarations: " + e.getMessage());
        }
    }
    
    private void analyzeCatchBlockLogic(CtMethod method, MethodExceptionHandling methodHandling) {
        try {
            String methodBody = getMethodBody(method);
            
            // Analizza pattern di gestione comuni
            if (methodBody.contains("logger.error") || methodBody.contains("log.error")) {
                methodHandling.setHasLogging(true);
            }
            
            if (methodBody.contains("throw new") || methodBody.contains("throw ")) {
                methodHandling.setRethrowsException(true);
            }
            
            if (methodBody.contains("return null") || methodBody.contains("return \"\"")) {
                methodHandling.setReturnsDefaultValue(true);
            }
            
            // Cerca pattern di recovery
            if (methodBody.contains("retry") || methodBody.contains("fallback")) {
                methodHandling.setHasRecoveryLogic(true);
            }
            
        } catch (Exception e) {
            methodHandling.addAnalysisError("Errore nell'analisi catch logic: " + e.getMessage());
        }
    }
    
    private void analyzeResourceManagement(CtClass ctClass, ExceptionHandlingReport report) {
        try {
            CtMethod[] methods = ctClass.getDeclaredMethods();
            
            for (CtMethod method : methods) {
                String methodBody = getMethodBody(method);
                
                ResourceManagementAnalysis resourceAnalysis = new ResourceManagementAnalysis();
                resourceAnalysis.setClassName(ctClass.getName());
                resourceAnalysis.setMethodName(method.getName());
                
                // Analizza try-with-resources
                if (methodBody.contains("try (")) {
                    resourceAnalysis.setUsesTryWithResources(true);
                    resourceAnalysis.addGoodPractice("TRY_WITH_RESOURCES");
                }
                
                // Cerca pattern di resource leak
                if (hasResourceLeakPotential(methodBody)) {
                    resourceAnalysis.setHasResourceLeakRisk(true);
                    resourceAnalysis.addQualityIssue("RESOURCE_LEAK", "ALTA", 
                        "Potenziale resource leak - risorse non chiuse correttamente");
                }
                
                // Analizza finally blocks
                if (methodBody.contains("finally")) {
                    resourceAnalysis.setHasFinallyBlock(true);
                    
                    // Verifica throw in finally
                    if (hasThrowInFinally(methodBody)) {
                        resourceAnalysis.addQualityIssue("THROW_IN_FINALLY", "CRITICA",
                            "Throw in blocco finally può mascherare eccezioni originali");
                    }
                }
                
                report.addResourceManagementAnalysis(resourceAnalysis);
            }
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi resource management: " + e.getMessage());
        }
    }
    
    private void analyzeErrorRecoveryPatterns(CtClass ctClass, ExceptionHandlingReport report) {
        try {
            CtMethod[] methods = ctClass.getDeclaredMethods();
            
            for (CtMethod method : methods) {
                String methodBody = getMethodBody(method);
                
                ErrorRecoveryAnalysis recoveryAnalysis = new ErrorRecoveryAnalysis();
                recoveryAnalysis.setClassName(ctClass.getName());
                recoveryAnalysis.setMethodName(method.getName());
                
                // Cerca pattern di retry
                if (methodBody.contains("retry") || methodBody.contains("attempt")) {
                    recoveryAnalysis.setHasRetryMechanism(true);
                    recoveryAnalysis.addGoodPractice("RETRY_MECHANISM");
                }
                
                // Cerca circuit breaker pattern
                if (methodBody.contains("circuitBreaker") || methodBody.contains("CircuitBreaker")) {
                    recoveryAnalysis.setHasCircuitBreaker(true);
                    recoveryAnalysis.addGoodPractice("CIRCUIT_BREAKER");
                }
                
                // Cerca fallback strategies
                if (methodBody.contains("fallback") || methodBody.contains("defaultValue")) {
                    recoveryAnalysis.setHasFallbackStrategy(true);
                    recoveryAnalysis.addGoodPractice("GRACEFUL_DEGRADATION");
                }
                
                // Cerca correlation ID tracking
                if (methodBody.contains("correlationId") || methodBody.contains("MDC")) {
                    recoveryAnalysis.setHasCorrelationTracking(true);
                    recoveryAnalysis.addGoodPractice("CORRELATION_ID_TRACKING");
                }
                
                // Verifica monitoring integration
                if (methodBody.contains("metrics") || methodBody.contains("monitor")) {
                    recoveryAnalysis.setHasMonitoringIntegration(true);
                    recoveryAnalysis.addGoodPractice("MONITORING_INTEGRATION");
                }
                
                report.addErrorRecoveryAnalysis(recoveryAnalysis);
            }
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi error recovery: " + e.getMessage());
        }
    }
    
    private void evaluateExceptionHandlingPractices(ExceptionHandlingReport report) {
        ExceptionHandlingEvaluation evaluation = new ExceptionHandlingEvaluation();
        
        // Valuta copertura exception handling
        int totalMethods = report.getMethodExceptionHandlings().size() + 
                          report.getMethodThrowsDeclarations().size();
        int methodsWithHandling = report.getMethodExceptionHandlings().size();
        
        if (totalMethods > 0) {
            double handlingCoverage = (double) methodsWithHandling / totalMethods;
            evaluation.setHandlingCoverage(handlingCoverage);
        }
        
        // Valuta qualità degli handlers
        long handlersWithLogging = report.getMethodExceptionHandlings().stream()
            .filter(MethodExceptionHandling::isHasLogging)
            .count();
        
        if (methodsWithHandling > 0) {
            evaluation.setLoggingRate((double) handlersWithLogging / methodsWithHandling);
        }
        
        // Analizza custom exceptions
        long businessExceptions = report.getCustomExceptions().stream()
            .filter(ex -> isBusinessException(ex.getClassName()))
            .count();
        long technicalExceptions = report.getCustomExceptions().stream()
            .filter(ex -> isTechnicalException(ex.getClassName()))
            .count();
        
        evaluation.setBusinessExceptionCount(businessExceptions);
        evaluation.setTechnicalExceptionCount(technicalExceptions);
        
        // Valuta recovery patterns
        long methodsWithRecovery = report.getErrorRecoveryAnalyses().stream()
            .filter(analysis -> analysis.isHasRetryMechanism() || 
                              analysis.isHasFallbackStrategy() ||
                              analysis.isHasCircuitBreaker())
            .count();
        
        if (totalMethods > 0) {
            evaluation.setRecoveryPatternUsage((double) methodsWithRecovery / totalMethods);
        }
        
        // Identifica anti-patterns e best practices
        identifyExceptionAntiPatterns(report, evaluation);
        identifyExceptionBestPractices(report, evaluation);
        
        report.setExceptionHandlingEvaluation(evaluation);
    }
    
    private void calculateQualityScore(ExceptionHandlingReport report) {
        int baseScore = 100;
        int totalPenalties = 0;
        int totalBonuses = 0;
        
        ExceptionHandlingEvaluation evaluation = report.getExceptionHandlingEvaluation();
        List<QualityIssue> qualityIssues = new ArrayList<>();
        
        // Calcola penalità per anti-patterns
        for (String antiPattern : evaluation.getAntiPatterns()) {
            int penalty = EXCEPTION_SEVERITY_PENALTIES.getOrDefault(antiPattern, -5);
            totalPenalties += Math.abs(penalty);
            
            String severity = determineSeverity(Math.abs(penalty));
            qualityIssues.add(new QualityIssue(
                antiPattern,
                severity, 
                getAntiPatternDescription(antiPattern),
                getAntiPatternRecommendation(antiPattern)
            ));
        }
        
        // Calcola bonus per best practices
        for (String bestPractice : evaluation.getBestPractices()) {
            int bonus = EXCEPTION_HANDLING_BONUSES.getOrDefault(bestPractice, 2);
            totalBonuses += bonus;
        }
        
        // Penalità per bassa copertura
        double handlingCoverage = evaluation.getHandlingCoverage();
        if (handlingCoverage < 0.3) {
            totalPenalties += 20;
            qualityIssues.add(new QualityIssue(
                "LOW_HANDLING_COVERAGE",
                "ALTA",
                String.format("Bassa copertura gestione eccezioni: %.1f%%", handlingCoverage * 100),
                "Aumentare la copertura di gestione delle eccezioni nei metodi critici"
            ));
        } else if (handlingCoverage < 0.6) {
            totalPenalties += 10;
            qualityIssues.add(new QualityIssue(
                "MEDIUM_HANDLING_COVERAGE",
                "MEDIA",
                String.format("Copertura gestione eccezioni migliorabile: %.1f%%", handlingCoverage * 100),
                "Considerare l'aggiunta di gestione eccezioni in più metodi"
            ));
        }
        
        // Bonus per alta copertura
        if (handlingCoverage > 0.8) {
            totalBonuses += 10;
        }
        
        // Penalità per scarso logging
        double loggingRate = evaluation.getLoggingRate();
        if (loggingRate < 0.5) {
            totalPenalties += 15;
            qualityIssues.add(new QualityIssue(
                "POOR_EXCEPTION_LOGGING",
                "ALTA",
                String.format("Tasso di logging delle eccezioni: %.1f%%", loggingRate * 100),
                "Implementare logging strutturato per tutte le eccezioni gestite"
            ));
        }
        
        // Bonus per recovery patterns
        double recoveryUsage = evaluation.getRecoveryPatternUsage();
        if (recoveryUsage > 0.3) {
            totalBonuses += 15;
        }
        
        // Calcola score finale
        int finalScore = Math.max(0, Math.min(100, baseScore - totalPenalties + totalBonuses));
        
        ExceptionHandlingQualityScore qualityScore = new ExceptionHandlingQualityScore();
        qualityScore.setOverallScore(finalScore);
        qualityScore.setHandlingCoverageScore((int) (handlingCoverage * 100));
        qualityScore.setLoggingQualityScore((int) (loggingRate * 100));
        qualityScore.setRecoveryPatternsScore((int) (recoveryUsage * 100));
        qualityScore.setCustomExceptionDesignScore(calculateCustomExceptionScore(report));
        qualityScore.setQualityLevel(determineQualityLevel(finalScore));
        qualityScore.setQualityIssues(qualityIssues);
        qualityScore.setTotalPenalties(totalPenalties);
        qualityScore.setTotalBonuses(totalBonuses);
        
        report.setQualityScore(qualityScore);
    }
    
    // Metodi helper per l'analisi
    private boolean hasResourceLeakPotential(String methodBody) {
        return (methodBody.contains("FileInputStream") || 
                methodBody.contains("Connection") ||
                methodBody.contains("Statement") ||
                methodBody.contains("InputStream") ||
                methodBody.contains("OutputStream")) &&
               !methodBody.contains("try (") &&
               !methodBody.contains("close()");
    }
    
    private boolean hasThrowInFinally(String methodBody) {
        String[] lines = methodBody.split("\n");
        boolean inFinally = false;
        
        for (String line : lines) {
            if (line.trim().startsWith("finally")) {
                inFinally = true;
            } else if (line.trim().equals("}") && inFinally) {
                inFinally = false;
            } else if (inFinally && line.contains("throw")) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isBusinessException(String className) {
        return BUSINESS_EXCEPTION_PATTERNS.stream()
            .anyMatch(pattern -> className.toLowerCase().contains(pattern.toLowerCase()));
    }
    
    private boolean isTechnicalException(String className) {
        return TECHNICAL_EXCEPTION_PATTERNS.stream()
            .anyMatch(pattern -> className.toLowerCase().contains(pattern.toLowerCase()));
    }
    
    private void identifyExceptionAntiPatterns(ExceptionHandlingReport report, 
                                             ExceptionHandlingEvaluation evaluation) {
        List<String> antiPatterns = new ArrayList<>();
        
        // Generic catch blocks
        long genericCatches = report.getMethodExceptionHandlings().stream()
            .flatMap(meh -> meh.getTryCatchBlocks().stream())
            .filter(tcb -> GENERIC_EXCEPTION_TYPES.contains(tcb.getCaughtExceptionType()))
            .count();
        
        if (genericCatches > 0) {
            antiPatterns.add("GENERIC_CATCH");
        }
        
        // Empty catch blocks
        long emptyCatches = report.getMethodExceptionHandlings().stream()
            .filter(meh -> !meh.isHasLogging() && !meh.isRethrowsException() && 
                          !meh.isReturnsDefaultValue() && !meh.isHasRecoveryLogic())
            .count();
        
        if (emptyCatches > 0) {
            antiPatterns.add("EMPTY_CATCH");
        }
        
        // Poor logging practices
        double loggingRate = evaluation.getLoggingRate();
        if (loggingRate < 0.5) {
            antiPatterns.add("IMPROPER_LOGGING");
        }
        
        // Resource management issues
        long resourceLeaks = report.getResourceManagementAnalyses().stream()
            .filter(rma -> rma.isHasResourceLeakRisk())
            .count();
        
        if (resourceLeaks > 0) {
            antiPatterns.add("RESOURCE_LEAK");
        }
        
        evaluation.setAntiPatterns(antiPatterns);
    }
    
    private void identifyExceptionBestPractices(ExceptionHandlingReport report, 
                                              ExceptionHandlingEvaluation evaluation) {
        List<String> bestPractices = new ArrayList<>();
        
        // Try-with-resources usage
        long tryWithResourcesUsage = report.getResourceManagementAnalyses().stream()
            .filter(rma -> rma.isUsesTryWithResources())
            .count();
        
        if (tryWithResourcesUsage > 0) {
            bestPractices.add("TRY_WITH_RESOURCES");
        }
        
        // Custom exceptions
        if (!report.getCustomExceptions().isEmpty()) {
            bestPractices.add("CUSTOM_EXCEPTIONS");
        }
        
        // Recovery strategies
        long recoveryStrategies = report.getErrorRecoveryAnalyses().stream()
            .filter(era -> era.isHasRetryMechanism() || era.isHasFallbackStrategy())
            .count();
        
        if (recoveryStrategies > 0) {
            bestPractices.add("RECOVERY_STRATEGY");
        }
        
        // Monitoring integration
        long monitoringIntegration = report.getErrorRecoveryAnalyses().stream()
            .filter(era -> era.isHasMonitoringIntegration())
            .count();
        
        if (monitoringIntegration > 0) {
            bestPractices.add("MONITORING_INTEGRATION");
        }
        
        evaluation.setBestPractices(bestPractices);
    }
    
    private int calculateCustomExceptionScore(ExceptionHandlingReport report) {
        int score = 50; // Base score
        
        long customExceptions = report.getCustomExceptions().size();
        long businessExceptions = report.getCustomExceptions().stream()
            .filter(ex -> isBusinessException(ex.getClassName()))
            .count();
        long technicalExceptions = report.getCustomExceptions().stream()
            .filter(ex -> isTechnicalException(ex.getClassName()))
            .count();
        
        // Bonus per presenza di custom exceptions
        if (customExceptions > 0) {
            score += Math.min(20, (int) customExceptions * 5);
        }
        
        // Bonus per separazione business/technical
        if (businessExceptions > 0 && technicalExceptions > 0) {
            score += 15;
        }
        
        // Verifica design quality delle custom exceptions
        for (CustomException customEx : report.getCustomExceptions()) {
            // Bonus per costruttori appropriati
            if (customEx.getConstructors().size() >= 3) { // message, cause, message+cause
                score += 5;
            }
            
            // Bonus per campi aggiuntivi significativi
            if (!customEx.getAdditionalFields().isEmpty()) {
                score += 3;
            }
        }
        
        return Math.min(100, score);
    }
    
    private String determineSeverity(int penalty) {
        if (penalty >= 25) return "CRITICA";
        if (penalty >= 15) return "ALTA";
        if (penalty >= 8) return "MEDIA";
        return "BASSA";
    }
    
    private String getAntiPatternDescription(String antiPattern) {
        return switch (antiPattern) {
            case "GENERIC_CATCH" -> "Uso di catch generici (Exception, RuntimeException, Throwable)";
            case "EMPTY_CATCH" -> "Blocchi catch vuoti che ignorano le eccezioni silenziosamente";
            case "SWALLOWED_EXCEPTION" -> "Eccezioni catturate ma non gestite appropriatamente";
            case "IMPROPER_LOGGING" -> "Logging inadeguato o assente nelle gestioni di errore";
            case "RESOURCE_LEAK" -> "Potenziali resource leak per mancato cleanup";
            case "THROW_IN_FINALLY" -> "Utilizzo di throw in blocchi finally";
            case "NO_RECOVERY_LOGIC" -> "Mancanza di logica di recovery o fallback";
            case "EXCEPTION_AS_FLOW_CONTROL" -> "Uso inappropriato di eccezioni per controllo flusso";
            case "LOW_HANDLING_COVERAGE" -> "Bassa copertura nella gestione delle eccezioni";
            default -> "Anti-pattern nella gestione delle eccezioni";
        };
    }
    
    private String getAntiPatternRecommendation(String antiPattern) {
        return switch (antiPattern) {
            case "GENERIC_CATCH" -> "Catturare eccezioni specifiche e gestire diversi scenari appropriatamente";
            case "EMPTY_CATCH" -> "Implementare gestione appropriata: logging, recovery, o re-throw";
            case "SWALLOWED_EXCEPTION" -> "Aggiungere logging strutturato e considerare strategie di recovery";
            case "IMPROPER_LOGGING" -> "Implementare logging completo con context, correlation ID e stack trace";
            case "RESOURCE_LEAK" -> "Utilizzare try-with-resources o implementare cleanup in finally";
            case "THROW_IN_FINALLY" -> "Evitare throw in finally; utilizzare logging o altri meccanismi";
            case "NO_RECOVERY_LOGIC" -> "Implementare strategie di fallback, retry, o graceful degradation";
            case "EXCEPTION_AS_FLOW_CONTROL" -> "Utilizzare controlli condizionali invece di eccezioni per flusso normale";
            case "LOW_HANDLING_COVERAGE" -> "Aumentare la copertura di gestione eccezioni nei metodi critici";
            default -> "Seguire le best practices per la gestione delle eccezioni";
        };
    }
    
    private String determineQualityLevel(int score) {
        if (score >= 90) return "ECCELLENTE";
        if (score >= 80) return "BUONO";
        if (score >= 70) return "DISCRETO";
        if (score >= 60) return "SUFFICIENTE";
        return "INSUFFICIENTE";
    }
    
    private boolean isExceptionClass(CtClass ctClass) {
        try {
            CtClass current = ctClass;
            while (current != null) {
                if ("java.lang.Throwable".equals(current.getName())) {
                    return true;
                }
                current = current.getSuperclass();
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}

public class ExceptionHandlingReport {
    private List<CustomException> customExceptions = new ArrayList<>();
    private List<ExceptionHandler> exceptionHandlers = new ArrayList<>();
    private List<MethodExceptionHandling> methodExceptionHandlings = new ArrayList<>();
    private List<MethodThrowsDeclaration> methodThrowsDeclarations = new ArrayList<>();
    private List<ResourceManagementAnalysis> resourceManagementAnalyses = new ArrayList<>();
    private List<ErrorRecoveryAnalysis> errorRecoveryAnalyses = new ArrayList<>();
    private ExceptionHandlingEvaluation exceptionHandlingEvaluation;
    private ExceptionHandlingQualityScore qualityScore;
    private List<String> errors = new ArrayList<>();
    
    public static class CustomException {
        private String className;
        private String superClass;
        private boolean checked;
        private List<ExceptionConstructor> constructors = new ArrayList<>();
        private List<String> additionalFields = new ArrayList<>();
        private List<String> additionalMethods = new ArrayList<>();
        
        // Getters and setters...
    }
    
    public static class ExceptionConstructor {
        private List<String> parameterTypes = new ArrayList<>();
        private boolean hasMessageParameter;
        private boolean hasCauseParameter;
        
        // Getters and setters...
    }
    
    public static class ExceptionHandler {
        private String className;
        private String methodName;
        private String returnType;
        private List<String> handledExceptions = new ArrayList<>();
        private boolean hasLogging;
        private boolean hasCustomLogic;
        private String responseStrategy;
        
        // Getters and setters...
    }
    
    public static class MethodExceptionHandling {
        private String className;
        private String methodName;
        private List<TryCatchBlock> tryCatchBlocks = new ArrayList<>();
        private boolean hasLogging;
        private boolean rethrowsException;
        private boolean returnsDefaultValue;
        private boolean hasRecoveryLogic;
        private List<String> analysisErrors = new ArrayList<>();
        
        // Getters and setters...
    }
    
    public static class TryCatchBlock {
        private int startPc;
        private int endPc;
        private int handlerPc;
        private String caughtExceptionType;
        
        // Getters and setters...
    }
    
    public static class MethodThrowsDeclaration {
        private String className;
        private String methodName;
        private List<String> thrownExceptions = new ArrayList<>();
        
        // Getters and setters...
    }
    
    public static class ResourceManagementAnalysis {
        private String className;
        private String methodName;
        private boolean usesTryWithResources;
        private boolean hasResourceLeakRisk;
        private boolean hasFinallyBlock;
        private List<String> goodPractices = new ArrayList<>();
        private List<QualityIssue> qualityIssues = new ArrayList<>();
        
        public void addGoodPractice(String practice) {
            this.goodPractices.add(practice);
        }
        
        public void addQualityIssue(String issueType, String severity, String description) {
            this.qualityIssues.add(new QualityIssue(issueType, severity, description, ""));
        }
        
        // Getters and setters...
    }
    
    public static class ErrorRecoveryAnalysis {
        private String className;
        private String methodName;
        private boolean hasRetryMechanism;
        private boolean hasCircuitBreaker;
        private boolean hasFallbackStrategy;
        private boolean hasCorrelationTracking;
        private boolean hasMonitoringIntegration;
        private List<String> goodPractices = new ArrayList<>();
        
        public void addGoodPractice(String practice) {
            this.goodPractices.add(practice);
        }
        
        // Getters and setters...
    }
    
    public static class ExceptionHandlingEvaluation {
        private double handlingCoverage;
        private double loggingRate;
        private double recoveryPatternUsage;
        private long businessExceptionCount;
        private long technicalExceptionCount;
        private List<String> antiPatterns = new ArrayList<>();
        private List<String> bestPractices = new ArrayList<>();
        private List<String> recommendations = new ArrayList<>();
        
        // Getters and setters...
    }
    
    public static class ExceptionHandlingQualityScore {
        private int overallScore;
        private int handlingCoverageScore;
        private int loggingQualityScore;
        private int recoveryPatternsScore;
        private int customExceptionDesignScore;
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
    
    // Add getter and setter methods for all collections
    public List<ResourceManagementAnalysis> getResourceManagementAnalyses() {
        return resourceManagementAnalyses;
    }
    
    public void addResourceManagementAnalysis(ResourceManagementAnalysis analysis) {
        this.resourceManagementAnalyses.add(analysis);
    }
    
    public List<ErrorRecoveryAnalysis> getErrorRecoveryAnalyses() {
        return errorRecoveryAnalyses;
    }
    
    public void addErrorRecoveryAnalysis(ErrorRecoveryAnalysis analysis) {
        this.errorRecoveryAnalyses.add(analysis);
    }
    
    // Additional getters and setters...
}
```

## Raccolta Dati

### 1. Exception Hierarchy
- Classi di eccezione custom definite
- Gerarchia delle eccezioni (checked/unchecked)
- Costruttori e metodi delle eccezioni

### 2. Exception Handlers
- Metodi annotati con `@ExceptionHandler`
- Global exception handlers (`@ControllerAdvice`)
- Tipi di eccezione gestiti

### 3. Try-Catch Analysis
- Blocchi try-catch nel codice
- Tipi di eccezione catturate
- Logica di gestione implementata

### 4. Exception Propagation
- Dichiarazioni throws sui metodi
- Catena di propagazione delle eccezioni
- Pattern di wrapping delle eccezioni

## Metriche di Qualità del Codice

Il sistema di scoring valuta la qualità della gestione delle eccezioni con un algoritmo multi-fattoriale che analizza copertura, pratiche implementate e anti-pattern presenti.

### Algoritmo di Scoring (0-100)

```java
baseScore = 100

// Penalità principali (-5 a -30 punti)
GENERIC_CATCH: -15           // Catch generico (Exception, RuntimeException)  
EMPTY_CATCH: -25             // Catch vuoto senza gestione
SWALLOWED_EXCEPTION: -20     // Eccezione ignorata silenziosamente  
THROW_IN_FINALLY: -30        // Throw in blocco finally (maschera eccezioni)
RESOURCE_LEAK: -25           // Mancato cleanup risorse
EXCEPTION_AS_FLOW_CONTROL: -20  // Uso eccezioni per controllo flusso

// Bonus principali (+5 a +15 punti)  
TRY_WITH_RESOURCES: +10      // Uso try-with-resources
GRACEFUL_DEGRADATION: +15    // Implementa graceful degradation
CIRCUIT_BREAKER: +12         // Pattern circuit breaker
RECOVERY_STRATEGY: +12       // Implementa strategie di recovery
RETRY_MECHANISM: +10         // Meccanismi di retry

// Penalità per coverage
if (handlingCoverage < 30%) penaltyPoints += 20
if (loggingRate < 50%) penaltyPoints += 15

// Bonus per coverage
if (handlingCoverage > 80%) bonusPoints += 10
if (recoveryUsage > 30%) bonusPoints += 15

finalScore = max(0, min(100, baseScore - totalPenalties + totalBonuses))
```

### Soglie di Valutazione

| Punteggio | Livello | Descrizione |
|-----------|---------|-------------|
| 90-100 | 🟢 **ECCELLENTE** | Gestione eccezioni esemplare con pattern avanzati |
| 80-89  | 🔵 **BUONO** | Buone pratiche implementate, miglioramenti minori |
| 70-79  | 🟡 **DISCRETO** | Gestione adeguata con alcune lacune |
| 60-69  | 🟠 **SUFFICIENTE** | Gestione base presente, necessari miglioramenti |
| 0-59   | 🔴 **INSUFFICIENTE** | Gravi carenze nella gestione degli errori |

### Categorie di Problemi per Gravità

#### 🔴 CRITICA (25+ punti penalità)
- **Empty Catch Blocks** (-25): Blocchi catch vuoti che ignorano silenziosamente le eccezioni
- **Throw in Finally** (-30): Utilizzo di throw in blocchi finally che maschera eccezioni originali
- **Resource Leaks** (-25): Mancato cleanup di risorse critiche (connessioni, file, stream)

#### 🟠 ALTA (15-24 punti penalità)
- **Generic Exception Catch** (-15): Uso di catch generici (Exception, RuntimeException, Throwable)
- **Swallowed Exceptions** (-20): Eccezioni catturate ma non gestite appropriatamente
- **Exception Flow Control** (-20): Uso inappropriato di eccezioni per controllo del flusso
- **Poor Exception Logging** (-15): Logging inadeguato delle eccezioni gestite

#### 🟡 MEDIA (8-14 punti penalità)
- **No Recovery Logic** (-12): Mancanza di logica di recovery o fallback
- **Missing Finally Cleanup** (-8): Mancanza di blocco finally per cleanup risorse
- **Poor Error Messages** (-8): Messaggi di errore non informativi o mancanti

#### 🔵 BASSA (< 8 punti penalità)
- **Improper Exception Chaining** (-6): Catena di eccezioni non implementata correttamente
- **Missing Context Information** (-5): Mancanza di informazioni contestuali negli errori

### Esempio Output HTML

```html
<div class="exception-handling-quality-score">
    <h3>📊 Exception Handling Quality Score: 73/100 (DISCRETO)</h3>
    
    <div class="score-breakdown">
        <div class="metric">
            <span class="label">Handling Coverage:</span>
            <div class="bar"><div class="fill" style="width: 65%"></div></div>
            <span class="value">65%</span>
        </div>
        
        <div class="metric">
            <span class="label">Logging Quality:</span>
            <div class="bar"><div class="fill" style="width: 78%"></div></div>
            <span class="value">78%</span>
        </div>
        
        <div class="metric">
            <span class="label">Recovery Patterns:</span>
            <div class="bar"><div class="fill" style="width: 45%"></div></div>
            <span class="value">45%</span>
        </div>
        
        <div class="metric">
            <span class="label">Custom Exception Design:</span>
            <div class="bar"><div class="fill" style="width: 82%"></div></div>
            <span class="value">82%</span>
        </div>
    </div>

    <div class="issues-summary">
        <h4>🔍 Problemi Identificati</h4>
        <div class="issue critical">
            🔴 <strong>RESOURCE_LEAK</strong>: 3 istanze di potenziali resource leak
            <br>→ <em>Utilizzare try-with-resources per tutte le risorse AutoCloseable</em>
        </div>
        
        <div class="issue high">
            🟠 <strong>GENERIC_CATCH</strong>: 8 blocchi catch generici
            <br>→ <em>Catturare eccezioni specifiche per gestione appropriata</em>
        </div>
        
        <div class="issue medium">
            🟡 <strong>NO_RECOVERY_LOGIC</strong>: 12 metodi senza logica di recovery  
            <br>→ <em>Implementare strategie di fallback per operazioni critiche</em>
        </div>
    </div>
    
    <div class="best-practices">
        <h4>✅ Best Practices Identificate</h4>
        <ul>
            <li>✅ Try-with-resources utilizzato in 23 metodi</li>
            <li>✅ Custom exceptions ben progettate (8 classi)</li>
            <li>✅ Separazione business/technical exceptions</li>
            <li>✅ Correlation ID tracking implementato</li>
        </ul>
    </div>
</div>
```

### Metriche Business Value

#### 🎯 System Reliability Impact
- **Reduced Error Propagation**: Contenimento errori attraverso gestione appropriata
- **Improved Fault Tolerance**: Capacità sistema di operare in presenza di errori
- **Better Error Visibility**: Monitoring e debugging migliorati attraverso logging strutturato
- **Reduced MTTR**: Mean Time To Recovery ridotto grazie a error handling chiaro

#### 💰 Operational Efficiency 
- **Decreased Support Tickets**: Errori gestiti gracefully riducono richieste utente
- **Faster Issue Resolution**: Stack trace e context information accelerano debugging
- **Reduced Downtime**: Pattern di recovery prevengono cascading failures
- **Improved Monitoring**: Integration con sistemi di observability per proactive response

#### 👥 Developer Productivity
- **Code Maintainability**: Exception handling consistente facilita manutenzione
- **Debugging Efficiency**: Error context ricco riduce tempo investigazione
- **Testing Reliability**: Gestione errori prevedibile migliora test stability
- **Knowledge Transfer**: Pattern consistenti facilitano onboarding nuovi sviluppatori

### Raccomandazioni Prioritarie

1. **Eliminate Empty Catch Blocks**: Implementare gestione appropriata in tutti i catch vuoti
2. **Implement Resource Management**: Utilizzare try-with-resources per tutte le risorse AutoCloseable  
3. **Add Recovery Strategies**: Implementare fallback logic per operazioni business-critical
4. **Improve Exception Logging**: Aggiungere structured logging con correlation IDs
5. **Design Custom Exceptions**: Creare gerarchia eccezioni domain-specific significative

## Metriche di Valore

- **Robustezza**: Misura la capacità di gestire errori gracefully
- **Maintainability**: Facilita debugging e troubleshooting
- **User Experience**: Migliora la gestione di errori lato utente
- **System Reliability**: Aumenta la stabilità dell'applicazione

## Classificazione

**Categoria**: Code Quality & Performance
**Priorità**: Alta - La gestione corretta delle eccezioni è fondamentale per la robustezza
**Stakeholder**: Development team, QA team, Operations team