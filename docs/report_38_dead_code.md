# Report 38: Identificazione di Dead Code

**Valore**: ⭐⭐⭐⭐ **Complessità**: 🔴 Complessa **Tempo**: 8-10 giorni
**Tags**: `#dead-code` `#unused-methods` `#code-cleanup`

## Descrizione

Identifica codice morto nell'applicazione attraverso analisi statica delle dipendenze, individuando classi, metodi, campi e import non utilizzati per facilitare cleanup del codebase e riduzione del technical debt.

## Sezioni del Report

### 1. Dead Code Overview
- Percentuale di codice morto nel progetto
- Distribuzione per tipologia (classes, methods, fields)
- Impact su codebase size e maintainability
- Trend nel tempo

### 2. Unused Classes Analysis
- Classi completamente non referenziate
- Classi utilizzate solo per reflection/configuration
- Utility classes non utilizzate
- Legacy classes obsolete

### 3. Unused Methods & Fields
- Metodi privati non chiamati
- Metodi pubblici non utilizzati esternamente
- Campi non accessati
- Getter/setter non utilizzati

### 4. Code Cleanup Recommendations
- Safe removal candidates
- Potential false positives (reflection, annotations)
- Cleanup priority e impact assessment
- Automated cleanup strategies

## Implementazione con Javassist

```java
public class DeadCodeAnalyzer {
    
    private final Set<String> referencedClasses = new HashSet<>();
    private final Set<String> referencedMethods = new HashSet<>();
    private final Set<String> referencedFields = new HashSet<>();
    
    private static final Set<String> FRAMEWORK_ANNOTATIONS = Set.of(
        "org.springframework.stereotype.Component",
        "org.springframework.stereotype.Service", 
        "org.springframework.stereotype.Repository",
        "org.springframework.stereotype.Controller",
        "org.springframework.web.bind.annotation.RestController",
        "javax.persistence.Entity",
        "org.springframework.boot.autoconfigure.SpringBootApplication"
    );
    
    private static final Map<String, Integer> DEAD_CODE_SEVERITY_PENALTIES = Map.of(
        "HIGH_DEAD_CODE_RATIO", -30,        // > 30% codice morto
        "DEAD_PUBLIC_API", -25,             // API pubbliche non utilizzate
        "DEAD_COMPLEX_CODE", -20,           // Codice complesso ma morto
        "LARGE_DEAD_CLASSES", -18,          // Classi grandi completamente morte
        "DEAD_TEST_CODE", -15,              // Test code non utilizzato
        "ORPHANED_DEPENDENCIES", -12,       // Dipendenze orfane
        "DEAD_CONFIGURATION", -10,          // Configurazioni non utilizzate
        "UNUSED_IMPORTS", -5,               // Import non utilizzati (meno critico)
        "DEAD_UTILITY_CLASSES", -8,         // Utility classes non utilizzate
        "LEGACY_CODE_DEBT", -15             // Legacy code che accumula debt
    );
    
    private static final Map<String, Integer> CODE_CLEANLINESS_BONUSES = Map.of(
        "MINIMAL_DEAD_CODE", 20,            // < 5% codice morto
        "CLEAN_PUBLIC_API", 15,             // API pubbliche tutte utilizzate
        "AUTOMATED_CLEANUP", 12,            // Cleanup automatizzato implementato
        "PROPER_ENCAPSULATION", 10,         // Buona encapsulation riduce dead code
        "ACTIVE_MAINTENANCE", 8,            // Segni di manutenzione attiva
        "LEAN_DEPENDENCIES", 10,            // Dipendenze snelle
        "CLEAN_TEST_SUITE", 8,              // Test suite pulita
        "DOCUMENTATION_COVERAGE", 5,        // Codice documentato (meno likely dead)
        "RECENT_ACTIVITY", 6,               // Attività recente nel codebase
        "CONSISTENT_CODING_STANDARDS", 4    // Standard consistenti
    );
    
    public DeadCodeReport analyzeDeadCode(CtClass[] classes) {
        DeadCodeReport report = new DeadCodeReport();
        
        // Prima passata: raccoglie tutte le referenze
        collectAllReferences(classes);
        
        // Seconda passata: identifica codice non referenziato
        for (CtClass ctClass : classes) {
            analyzeClassUsage(ctClass, report);
        }
        
        // Analizza configuration e annotation usage
        analyzeConfigurationUsage(classes, report);
        
        // Genera raccomandazioni di cleanup
        generateCleanupRecommendations(report);
        
        // Calcola metriche di qualità
        evaluateCodeCleanliness(report);
        calculateDeadCodeQualityScore(report);
        
        return report;
    }
    
    private void collectAllReferences(CtClass[] classes) {
        for (CtClass ctClass : classes) {
            try {
                collectClassReferences(ctClass);
            } catch (Exception e) {
                // Log error but continue
            }
        }
    }
    
    private void collectClassReferences(CtClass ctClass) {
        try {
            // Riferimenti da superclass e interfacce
            if (ctClass.getSuperclass() != null) {
                referencedClasses.add(ctClass.getSuperclass().getName());
            }
            
            for (CtClass interfaceClass : ctClass.getInterfaces()) {
                referencedClasses.add(interfaceClass.getName());
            }
            
            // Riferimenti da campi
            for (CtField field : ctClass.getDeclaredFields()) {
                referencedClasses.add(field.getType().getName());
                analyzeFieldAnnotations(field);
            }
            
            // Riferimenti da metodi
            for (CtMethod method : ctClass.getDeclaredMethods()) {
                collectMethodReferences(method);
            }
            
            // Riferimenti da constructors
            for (CtConstructor constructor : ctClass.getDeclaredConstructors()) {
                collectConstructorReferences(constructor);
            }
            
            // Analizza annotazioni della classe
            analyzeClassAnnotations(ctClass);
            
        } catch (Exception e) {
            // Log and continue
        }
    }
    
    private void collectMethodReferences(CtMethod method) {
        try {
            // Return type e parameter types
            referencedClasses.add(method.getReturnType().getName());
            for (CtClass paramType : method.getParameterTypes()) {
                referencedClasses.add(paramType.getName());
            }
            
            // Exception types
            for (CtClass exceptionType : method.getExceptionTypes()) {
                referencedClasses.add(exceptionType.getName());
            }
            
            // Method body references
            method.instrument(new ExprEditor() {
                @Override
                public void edit(MethodCall call) throws CannotCompileException {
                    referencedMethods.add(call.getClassName() + "." + call.getMethodName());
                    referencedClasses.add(call.getClassName());
                }
                
                @Override
                public void edit(FieldAccess access) throws CannotCompileException {
                    referencedFields.add(access.getClassName() + "." + access.getFieldName());
                    referencedClasses.add(access.getClassName());
                }
                
                @Override
                public void edit(NewExpr newExpr) throws CannotCompileException {
                    referencedClasses.add(newExpr.getClassName());
                }
                
                @Override
                public void edit(Cast cast) throws CannotCompileException {
                    try {
                        referencedClasses.add(cast.getType().getName());
                    } catch (NotFoundException e) {
                        // Ignore
                    }
                }
                
                @Override
                public void edit(Instanceof instanceofExpr) throws CannotCompileException {
                    try {
                        referencedClasses.add(instanceofExpr.getType().getName());
                    } catch (NotFoundException e) {
                        // Ignore
                    }
                }
            });
            
            // Analizza annotazioni del metodo
            analyzeMethodAnnotations(method);
            
        } catch (Exception e) {
            // Log and continue
        }
    }
    
    private void analyzeClassUsage(CtClass ctClass, DeadCodeReport report) {
        try {
            String className = ctClass.getName();
            
            // Skip system classes, test classes, and configuration classes
            if (isSystemClass(className) || isTestClass(className) || isConfigurationClass(ctClass)) {
                return;
            }
            
            ClassUsageInfo classUsage = new ClassUsageInfo();
            classUsage.setClassName(className);
            classUsage.setPackageName(ctClass.getPackageName());
            classUsage.setReferenced(referencedClasses.contains(className));
            
            // Analizza se è entry point (main method, controller, etc.)
            classUsage.setEntryPoint(isEntryPoint(ctClass));
            
            // Analizza se utilizzata via reflection/annotations
            classUsage.setReflectionUsage(isUsedViaReflection(ctClass));
            
            // Se non referenziata e non entry point, potrebbe essere dead code
            if (!classUsage.isReferenced() && !classUsage.isEntryPoint() && !classUsage.isReflectionUsage()) {
                DeadCodeItem deadClass = new DeadCodeItem();
                deadClass.setType(DeadCodeType.CLASS);
                deadClass.setName(className);
                deadClass.setSeverity(DeadCodeSeverity.HIGH);
                deadClass.setDescription("Class not referenced anywhere in the codebase");
                deadClass.setSafeToRemove(isSafeToRemove(ctClass));
                
                report.addDeadCodeItem(deadClass);
            }
            
            // Analizza metodi della classe
            analyzeMethodUsage(ctClass, classUsage, report);
            
            // Analizza campi della classe
            analyzeFieldUsage(ctClass, classUsage, report);
            
            report.addClassUsageInfo(classUsage);
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi class usage: " + e.getMessage());
        }
    }
    
    private void analyzeMethodUsage(CtClass ctClass, ClassUsageInfo classUsage, DeadCodeReport report) {
        try {
            CtMethod[] methods = ctClass.getDeclaredMethods();
            
            for (CtMethod method : methods) {
                String methodKey = ctClass.getName() + "." + method.getName();
                
                // Skip special methods
                if (isSpecialMethod(method)) {
                    continue;
                }
                
                MethodUsageInfo methodUsage = new MethodUsageInfo();
                methodUsage.setMethodName(method.getName());
                methodUsage.setMethodSignature(method.getSignature());
                methodUsage.setReferenced(referencedMethods.contains(methodKey));
                methodUsage.setPublicAccess(Modifier.isPublic(method.getModifiers()));
                methodUsage.setAnnotated(hasAnnotations(method));
                
                // Se metodo privato non referenziato, è dead code
                if (!methodUsage.isReferenced() && !Modifier.isPublic(method.getModifiers()) && !methodUsage.isAnnotated()) {
                    DeadCodeItem deadMethod = new DeadCodeItem();
                    deadMethod.setType(DeadCodeType.METHOD);
                    deadMethod.setName(methodKey);
                    deadMethod.setSeverity(DeadCodeSeverity.MEDIUM);
                    deadMethod.setDescription("Private method not called anywhere");
                    deadMethod.setSafeToRemove(true);
                    
                    report.addDeadCodeItem(deadMethod);
                }
                
                classUsage.addMethodUsage(methodUsage);
            }
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi method usage: " + e.getMessage());
        }
    }
    
    private void analyzeFieldUsage(CtClass ctClass, ClassUsageInfo classUsage, DeadCodeReport report) {
        try {
            CtField[] fields = ctClass.getDeclaredFields();
            
            for (CtField field : fields) {
                String fieldKey = ctClass.getName() + "." + field.getName();
                
                FieldUsageInfo fieldUsage = new FieldUsageInfo();
                fieldUsage.setFieldName(field.getName());
                fieldUsage.setReferenced(referencedFields.contains(fieldKey));
                fieldUsage.setPublicAccess(Modifier.isPublic(field.getModifiers()));
                fieldUsage.setAnnotated(hasFieldAnnotations(field));
                fieldUsage.setConstant(Modifier.isStatic(field.getModifiers()) && 
                                     Modifier.isFinal(field.getModifiers()));
                
                // Se campo privato non referenziato, è dead code
                if (!fieldUsage.isReferenced() && !fieldUsage.isPublicAccess() && 
                    !fieldUsage.isAnnotated() && !fieldUsage.isConstant()) {
                    DeadCodeItem deadField = new DeadCodeItem();
                    deadField.setType(DeadCodeType.FIELD);
                    deadField.setName(fieldKey);
                    deadField.setSeverity(DeadCodeSeverity.LOW);
                    deadField.setDescription("Private field not accessed anywhere");
                    deadField.setSafeToRemove(true);
                    
                    report.addDeadCodeItem(deadField);
                }
                
                classUsage.addFieldUsage(fieldUsage);
            }
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi field usage: " + e.getMessage());
        }
    }
    
    private boolean isEntryPoint(CtClass ctClass) {
        try {
            // Main method
            try {
                CtMethod mainMethod = ctClass.getDeclaredMethod("main", 
                    new CtClass[]{ctClass.getClassPool().get("java.lang.String[]")});
                if (Modifier.isPublic(mainMethod.getModifiers()) && Modifier.isStatic(mainMethod.getModifiers())) {
                    return true;
                }
            } catch (NotFoundException e) {
                // No main method
            }
            
            // Spring components
            return ctClass.hasAnnotation("org.springframework.stereotype.Controller") ||
                   ctClass.hasAnnotation("org.springframework.web.bind.annotation.RestController") ||
                   ctClass.hasAnnotation("org.springframework.stereotype.Service") ||
                   ctClass.hasAnnotation("org.springframework.stereotype.Repository") ||
                   ctClass.hasAnnotation("org.springframework.stereotype.Component") ||
                   ctClass.hasAnnotation("org.springframework.context.annotation.Configuration");
                   
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean isUsedViaReflection(CtClass ctClass) {
        // Heuristics per identificare usage via reflection
        try {
            // JPA entities
            if (ctClass.hasAnnotation("javax.persistence.Entity") ||
                ctClass.hasAnnotation("jakarta.persistence.Entity")) {
                return true;
            }
            
            // DTO/Model classes con constructor di default
            CtConstructor[] constructors = ctClass.getConstructors();
            for (CtConstructor constructor : constructors) {
                if (constructor.getParameterTypes().length == 0) {
                    return true; // Potrebbero essere istanziate via reflection
                }
            }
            
            return false;
            
        } catch (Exception e) {
            return false;
        }
    }
    
    private void evaluateCodeCleanliness(DeadCodeReport report) {
        CodeCleanlinessEvaluation evaluation = new CodeCleanlinessEvaluation();
        
        // Calcola statistiche dead code
        long totalElements = report.getClassUsageInfos().size();
        long deadElements = report.getDeadCodeItems().size();
        
        if (totalElements > 0) {
            double deadCodeRatio = (double) deadElements / totalElements;
            evaluation.setDeadCodeRatio(deadCodeRatio);
        }
        
        // Analizza tipologie di dead code
        long deadClasses = report.getDeadCodeItems().stream()
            .filter(item -> "CLASS".equals(item.getType()))
            .count();
        
        long deadMethods = report.getDeadCodeItems().stream()
            .filter(item -> "METHOD".equals(item.getType()))
            .count();
            
        long deadFields = report.getDeadCodeItems().stream()
            .filter(item -> "FIELD".equals(item.getType()))
            .count();
        
        evaluation.setDeadClassesCount(deadClasses);
        evaluation.setDeadMethodsCount(deadMethods);
        evaluation.setDeadFieldsCount(deadFields);
        
        // Valuta public API dead code (più grave)
        long deadPublicAPI = report.getDeadCodeItems().stream()
            .filter(item -> item.getVisibility() != null && item.getVisibility().equals("public"))
            .count();
        evaluation.setDeadPublicAPICount(deadPublicAPI);
        
        // Valuta complessità del dead code
        int totalDeadCodeComplexity = report.getDeadCodeItems().stream()
            .mapToInt(item -> item.getComplexityScore() != null ? item.getComplexityScore() : 0)
            .sum();
        evaluation.setDeadCodeComplexity(totalDeadCodeComplexity);
        
        // Valuta cleanup potential
        long safeCleanupCandidates = report.getCleanupRecommendations().stream()
            .filter(rec -> "SAFE".equals(rec.getRisk()))
            .count();
        evaluation.setSafeCleanupCandidates(safeCleanupCandidates);
        
        report.setCodeCleanlinessEvaluation(evaluation);
    }
    
    private void calculateDeadCodeQualityScore(DeadCodeReport report) {
        int baseScore = 100;
        int totalPenalties = 0;
        int totalBonuses = 0;
        
        CodeCleanlinessEvaluation evaluation = report.getCodeCleanlinessEvaluation();
        List<QualityIssue> qualityIssues = new ArrayList<>();
        
        // Penalità per alta percentuale dead code
        double deadCodeRatio = evaluation.getDeadCodeRatio();
        if (deadCodeRatio > 0.3) {
            totalPenalties += 30;
            qualityIssues.add(new QualityIssue(
                "HIGH_DEAD_CODE_RATIO",
                "ALTA",
                String.format("Alto rapporto codice morto: %.1f%%", deadCodeRatio * 100),
                "Implementare cleanup sistematico del dead code"
            ));
        } else if (deadCodeRatio > 0.15) {
            totalPenalties += 15;
            qualityIssues.add(new QualityIssue(
                "MODERATE_DEAD_CODE_RATIO",
                "MEDIA",
                String.format("Rapporto codice morto migliorabile: %.1f%%", deadCodeRatio * 100),
                "Pianificare cleanup graduale del dead code"
            ));
        }
        
        // Penalità per dead public API (più grave)
        if (evaluation.getDeadPublicAPICount() > 0) {
            totalPenalties += 25;
            qualityIssues.add(new QualityIssue(
                "DEAD_PUBLIC_API",
                "ALTA",
                String.format("%d elementi di API pubblica non utilizzati", evaluation.getDeadPublicAPICount()),
                "Rimuovere o deprecare API pubbliche non utilizzate"
            ));
        }
        
        // Penalità per dead code complesso
        if (evaluation.getDeadCodeComplexity() > 100) {
            totalPenalties += 20;
            qualityIssues.add(new QualityIssue(
                "DEAD_COMPLEX_CODE", 
                "ALTA",
                String.format("Codice complesso non utilizzato (complexity: %d)", evaluation.getDeadCodeComplexity()),
                "Prioritizzare rimozione del codice complesso inutilizzato"
            ));
        }
        
        // Penalità per classi grandi morte
        if (evaluation.getDeadClassesCount() > 5) {
            totalPenalties += 18;
            qualityIssues.add(new QualityIssue(
                "LARGE_DEAD_CLASSES",
                "MEDIA",
                String.format("%d classi completamente inutilizzate", evaluation.getDeadClassesCount()),
                "Rimuovere classi inutilizzate per ridurre codebase size"
            ));
        }
        
        // Bonus per minimal dead code
        if (deadCodeRatio < 0.05) {
            totalBonuses += 20;
        } else if (deadCodeRatio < 0.1) {
            totalBonuses += 10;
        }
        
        // Bonus per clean public API
        if (evaluation.getDeadPublicAPICount() == 0) {
            totalBonuses += 15;
        }
        
        // Bonus per alto cleanup potential
        double cleanupRatio = evaluation.getSafeCleanupCandidates() / (double) Math.max(1, evaluation.getDeadCodeRatio() * 100);
        if (cleanupRatio > 0.8) {
            totalBonuses += 12;
        }
        
        // Calcola score finale
        int finalScore = Math.max(0, Math.min(100, baseScore - totalPenalties + totalBonuses));
        
        DeadCodeQualityScore qualityScore = new DeadCodeQualityScore();
        qualityScore.setOverallScore(finalScore);
        qualityScore.setCodeCleanlinessScore((int) ((1 - deadCodeRatio) * 100));
        qualityScore.setPublicAPICleanlinessScore(evaluation.getDeadPublicAPICount() == 0 ? 100 : 
            Math.max(0, 100 - (int) evaluation.getDeadPublicAPICount() * 10));
        qualityScore.setCleanupPotentialScore((int) (cleanupRatio * 100));
        qualityScore.setMaintenanceScore(calculateMaintenanceScore(evaluation));
        qualityScore.setQualityLevel(determineQualityLevel(finalScore));
        qualityScore.setQualityIssues(qualityIssues);
        qualityScore.setTotalPenalties(totalPenalties);
        qualityScore.setTotalBonuses(totalBonuses);
        
        report.setQualityScore(qualityScore);
    }
    
    // Helper methods
    private int calculateMaintenanceScore(CodeCleanlinessEvaluation evaluation) {
        int score = 100;
        
        // Penalità basate su maintenance overhead
        score -= (int) (evaluation.getDeadCodeRatio() * 30);
        score -= Math.min(20, (int) evaluation.getDeadPublicAPICount() * 5);
        score -= Math.min(15, evaluation.getDeadCodeComplexity() / 20);
        
        return Math.max(0, score);
    }
    
    private String determineQualityLevel(int score) {
        if (score >= 90) return "ECCELLENTE";
        if (score >= 80) return "BUONO";
        if (score >= 70) return "DISCRETO"; 
        if (score >= 60) return "SUFFICIENTE";
        return "INSUFFICIENTE";
    }
}

public class DeadCodeReport {
    private List<DeadCodeItem> deadCodeItems = new ArrayList<>();
    private List<ClassUsageInfo> classUsageInfos = new ArrayList<>();
    private DeadCodeStatistics statistics;
    private List<CleanupRecommendation> cleanupRecommendations = new ArrayList<>();
    private CodeCleanlinessEvaluation codeCleanlinessEvaluation;
    private DeadCodeQualityScore qualityScore;
    private List<String> errors = new ArrayList<>();
    
    public static class DeadCodeItem {
        private DeadCodeType type;
        private String name;
        private DeadCodeSeverity severity;
        private String description;
        private boolean safeToRemove;
        private String reason;
    }
    
    public static class ClassUsageInfo {
        private String className;
        private String packageName;
        private boolean referenced;
        private boolean entryPoint;
        private boolean reflectionUsage;
        private List<MethodUsageInfo> methodUsages = new ArrayList<>();
        private List<FieldUsageInfo> fieldUsages = new ArrayList<>();
    }
    
    public enum DeadCodeType {
        CLASS, METHOD, FIELD, IMPORT
    }
    
    public enum DeadCodeSeverity {
        HIGH, MEDIUM, LOW
    }
    
    public static class CodeCleanlinessEvaluation {
        private double deadCodeRatio;
        private long deadClassesCount;
        private long deadMethodsCount;
        private long deadFieldsCount;
        private long deadPublicAPICount;
        private int deadCodeComplexity;
        private long safeCleanupCandidates;
        
        // Getters and setters...
    }
    
    public static class DeadCodeQualityScore {
        private int overallScore;
        private int codeCleanlinessScore;
        private int publicAPICleanlinessScore;
        private int cleanupPotentialScore;
        private int maintenanceScore;
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
    public void setCodeCleanlinessEvaluation(CodeCleanlinessEvaluation evaluation) {
        this.codeCleanlinessEvaluation = evaluation;
    }
    
    public CodeCleanlinessEvaluation getCodeCleanlinessEvaluation() {
        return this.codeCleanlinessEvaluation;
    }
}
```

## Raccolta Dati

### 1. Reference Analysis
- Costruzione grafo completo delle dipendenze
- Tracking di tutte le referenze dirette e indirette
- Analisi di usage via reflection e annotations
- Identificazione entry points

### 2. Dead Code Detection
- Classi completamente non referenziate
- Metodi privati non chiamati
- Campi non accessati
- Import statements non utilizzati

### 3. Safety Assessment
- Valutazione sicurezza rimozione
- Identificazione false positive (reflection usage)
- Analisi impact della rimozione
- Configuration e annotation dependencies

### 4. Cleanup Strategy
- Prioritizzazione rimozioni per impact
- Automated cleanup opportunities  
- Manual review requirements
- Regression testing recommendations

## Metriche di Qualità del Codice

### Algoritmo di Calcolo (0-100)

```java
public int calculateDeadCodeQualityScore(DeadCodeReport result) {
    double score = 100.0;
    
    // Penalizzazioni per dead code presente
    score -= result.getDeadClasses() * 15;                    // -15 per classe morta
    score -= result.getDeadMethods() * 5;                     // -5 per metodo morto
    score -= result.getDeadFields() * 2;                      // -2 per campo morto
    score -= result.getUnusedImports() * 1;                   // -1 per import non usato
    score -= result.getLargeDeadCodePercentage() * 20;        // -20 se >10% dead code
    score -= result.getComplexDeadCodePatterns() * 10;        // -10 per pattern complessi
    
    // Bonus per codebase pulito
    score += result.getWellMaintainedPackages() * 2;          // +2 per package senza dead code
    score += result.getProperUsagePatterns() * 1;             // +1 per pattern usage corretti
    
    return Math.max(0, Math.min(100, (int) score));
}
```

### Soglie di Valutazione
- **0-40**: 🔴 CRITICO - Significativo accumulo di dead code che impatta maintainability
- **41-60**: 🟡 SUFFICIENTE - Presenza moderata di dead code da pulire
- **61-80**: 🟢 BUONO - Codebase relativamente pulito con minor cleanup needed
- **81-100**: ⭐ ECCELLENTE - Codebase ben mantenuto senza dead code significativo

## Segnalazioni per Gravità

### 🔴 GRAVITÀ CRITICA (Score Impact: -15 to -20)
1. **Classi completamente morte**
   - Descrizione: Classi non referenziate da nessuna parte del codebase
   - Rischio: Codebase bloat, confusione per sviluppatori, maintenance overhead
   - Soluzione: Rimozione sicura dopo verifica di non-usage

2. **Alta percentuale dead code (>10%)**
   - Descrizione: Percentuale significativa di codice non utilizzato
   - Rischio: Technical debt elevato, difficoltà navigazione codebase
   - Soluzione: Cleanup sistematico e processo per prevenire accumulo

### 🟠 GRAVITÀ ALTA (Score Impact: -5 to -10)
3. **Pattern dead code complessi**
   - Descrizione: Gerarchie di classi o moduli interi non utilizzati
   - Rischio: Architettura confusa, over-engineering evidente
   - Soluzione: Refactoring architetturale, semplificazione design

### 🟡 GRAVITÀ MEDIA (Score Impact: -2 to -5)  
4. **Metodi privati non utilizzati**
   - Descrizione: Metodi interni che non sono più chiamati
   - Rischio: Codice di maintenance non necessario
   - Soluzione: Rimozione diretta, sono safe-to-remove

5. **Legacy utility methods**
   - Descrizione: Metodi utility non più utilizzati
   - Rischio: API confusion, alternatives migliori disponibili
   - Soluzione: Deprecation seguita da rimozione

### 🔵 GRAVITÀ BASSA (Score Impact: -1 to -2)
6. **Campi non utilizzati**
   - Descrizione: Variabili di istanza non accessate
   - Rischio: Memory footprint minimale, confusion minima
   - Soluzione: Cleanup quando si modifica la classe

7. **Import non utilizzati**
   - Descrizione: Import statement per classi non utilizzate  
   - Rischio: IDE clutter, compile time marginalmente impattato
   - Soluzione: Automated cleanup tramite IDE

## Metriche di Qualità del Codice

Il sistema di scoring valuta la pulizia del codebase attraverso l'analisi del dead code, con focus su maintainability, technical debt reduction e developer productivity.

### Algoritmo di Scoring (0-100)

```java
baseScore = 100

// Penalità principali (-5 a -30 punti)
HIGH_DEAD_CODE_RATIO: -30        // > 30% codice morto
DEAD_PUBLIC_API: -25             // API pubbliche non utilizzate
DEAD_COMPLEX_CODE: -20           // Codice complesso ma morto
LARGE_DEAD_CLASSES: -18          // Classi grandi completamente morte
DEAD_TEST_CODE: -15              // Test code non utilizzato
LEGACY_CODE_DEBT: -15            // Legacy code che accumula debt
ORPHANED_DEPENDENCIES: -12       // Dipendenze orfane
DEAD_CONFIGURATION: -10          // Configurazioni non utilizzate
DEAD_UTILITY_CLASSES: -8         // Utility classes non utilizzate
UNUSED_IMPORTS: -5               // Import non utilizzati

// Bonus principali (+4 a +20 punti)
MINIMAL_DEAD_CODE: +20           // < 5% codice morto
CLEAN_PUBLIC_API: +15            // API pubbliche tutte utilizzate
AUTOMATED_CLEANUP: +12           // Cleanup automatizzato implementato
PROPER_ENCAPSULATION: +10        // Buona encapsulation riduce dead code
LEAN_DEPENDENCIES: +10           // Dipendenze snelle
ACTIVE_MAINTENANCE: +8           // Segni di manutenzione attiva
CLEAN_TEST_SUITE: +8             // Test suite pulita

// Penalità per dead code ratio
if (deadCodeRatio > 30%) penaltyPoints += 30
if (deadCodeRatio > 15%) penaltyPoints += 15
if (deadPublicAPI > 0) penaltyPoints += 25
if (deadComplexity > 100) penaltyPoints += 20

// Bonus per cleanliness
if (deadCodeRatio < 5%) bonusPoints += 20
if (deadPublicAPI == 0) bonusPoints += 15
if (cleanupPotential > 80%) bonusPoints += 12

finalScore = max(0, min(100, baseScore - totalPenalties + totalBonuses))
```

### Soglie di Valutazione

| Punteggio | Livello | Descrizione |
|-----------|---------|-------------|
| 90-100 | 🟢 **ECCELLENTE** | Codebase estremamente pulito con minimal dead code |
| 80-89  | 🔵 **BUONO** | Dead code sotto controllo, cleanup occasionale necessario |
| 70-79  | 🟡 **DISCRETO** | Dead code presente ma gestibile, cleanup raccomandato |
| 60-69  | 🟠 **SUFFICIENTE** | Dead code significativo, cleanup pianificato necessario |
| 0-59   | 🔴 **INSUFFICIENTE** | Eccesso di dead code impatta maintainability |

### Categorie di Problemi per Gravità

#### 🔴 CRITICA (25+ punti penalità)
- **High Dead Code Ratio** (-30): > 30% del codebase è dead code
- **Dead Public API** (-25): API pubbliche non utilizzate che impattano backward compatibility
- **Dead Complex Code** (-20): Codice complesso inutilizzato che aumenta maintenance burden

#### 🟠 ALTA (15-24 punti penalità)
- **Large Dead Classes** (-18): Classi complete inutilizzate che occupano spazio significativo
- **Dead Test Code** (-15): Test non utilizzati che riducono confidence nella test suite
- **Legacy Code Debt** (-15): Accumulo di codice legacy non utilizzato

#### 🟡 MEDIA (8-14 punti penalità)
- **Orphaned Dependencies** (-12): Dipendenze non più utilizzate nel progetto
- **Dead Configuration** (-10): File di configurazione o proprietà non utilizzate
- **Dead Utility Classes** (-8): Utility classes che nessuno usa

#### 🔵 BASSA (< 8 punti penalità)
- **Unused Imports** (-5): Import statements non utilizzati
- **Minor Dead Code** (-3): Piccole porzioni di codice morto isolate

### Esempio Output HTML

```html
<div class="dead-code-quality-score">
    <h3>🧹 Dead Code Cleanliness Score: 74/100 (DISCRETO)</h3>
    
    <div class="score-breakdown">
        <div class="metric">
            <span class="label">Code Cleanliness:</span>
            <div class="bar"><div class="fill" style="width: 78%"></div></div>
            <span class="value">78%</span>
        </div>
        
        <div class="metric">
            <span class="label">Public API Cleanliness:</span>
            <div class="bar"><div class="fill" style="width: 65%"></div></div>
            <span class="value">65%</span>
        </div>
        
        <div class="metric">
            <span class="label">Cleanup Potential:</span>
            <div class="bar"><div class="fill" style="width: 85%"></div></div>
            <span class="value">85%</span>
        </div>
        
        <div class="metric">
            <span class="label">Maintenance Score:</span>
            <div class="bar"><div class="fill" style="width: 70%"></div></div>
            <span class="value">70%</span>
        </div>
    </div>

    <div class="dead-code-summary">
        <h4>📊 Dead Code Summary</h4>
        <div class="summary-grid">
            <div class="summary-item">
                <span class="label">Total Elements:</span>
                <span class="value">1,247</span>
            </div>
            
            <div class="summary-item critical">
                <span class="label">Dead Code:</span>
                <span class="value">203 (16.3%)</span>
            </div>
            
            <div class="summary-item">
                <span class="label">Dead Classes:</span>
                <span class="value">12</span>
            </div>
            
            <div class="summary-item">
                <span class="label">Dead Methods:</span>
                <span class="value">156</span>
            </div>
            
            <div class="summary-item">
                <span class="label">Dead Fields:</span>
                <span class="value">35</span>
            </div>
            
            <div class="summary-item warning">
                <span class="label">Dead Public API:</span>
                <span class="value">8</span>
            </div>
        </div>
    </div>
    
    <div class="cleanup-recommendations">
        <h4>🎯 Cleanup Recommendations</h4>
        <div class="rec-item safe">
            🟢 <strong>Safe Removal</strong>: 145 elementi possono essere rimossi senza rischi
        </div>
        
        <div class="rec-item caution">
            🟡 <strong>Caution Required</strong>: 42 elementi richiedono verifica per reflection/annotation usage
        </div>
        
        <div class="rec-item manual">
            🔴 <strong>Manual Review</strong>: 16 elementi richiedono review manuale per possibili side effects
        </div>
    </div>
    
    <div class="maintenance-impact">
        <h4>⚙️ Maintenance Impact</h4>
        <div class="impact-metric">
            <span class="label">Potential Size Reduction:</span>
            <span class="value">~18% codebase size</span>
        </div>
        
        <div class="impact-metric">
            <span class="label">Compilation Time Improvement:</span>
            <span class="value">~12% faster builds</span>
        </div>
        
        <div class="impact-metric">
            <span class="label">Developer Navigation:</span>
            <span class="value">Significantly improved</span>
        </div>
    </div>
</div>
```

### Metriche Business Value

#### 🧹 Technical Debt Reduction
- **Codebase Size Optimization**: Riduzione dimensioni codebase attraverso rimozione dead code
- **Maintenance Overhead**: Riduzione effort di manutenzione e refactoring
- **Code Comprehension**: Miglioramento comprensibilità codebase per nuovi sviluppatori
- **Build Performance**: Improvement compilation times e deployment speed

#### 💡 Developer Productivity
- **Navigation Efficiency**: Riduzione clutter nell'IDE e miglioramento code navigation
- **Code Search**: Risultati di ricerca più pertinenti senza false positives
- **Debugging Experience**: Focus su codice attivo elimina confusione durante debug
- **Refactoring Safety**: Maggiore confidenza nel refactoring senza dead code interference

#### 📊 Quality Metrics Impact
- **Code Coverage**: Metriche più accurate senza dead code skewing
- **Complexity Metrics**: Riduzione artificial complexity da codice inutilizzato
- **Dependency Analysis**: Grafo dipendenze più pulito e comprensibile
- **Static Analysis**: Tool di analisi statica più efficaci su codebase pulito

#### 🔄 Continuous Improvement
- **Automated Cleanup**: Integrazione cleanup automatico nel CI/CD pipeline
- **Trend Analysis**: Monitoring evoluzione dead code nel tempo
- **Preventive Measures**: Identificazione pattern che generano dead code
- **Team Awareness**: Educazione team su pratiche che riducono dead code

### Raccomandazioni Prioritarie

1. **Remove Dead Public APIs**: Priorità massima per API pubbliche non utilizzate
2. **Cleanup Complex Dead Code**: Rimuovere codice complesso morto per ridurre maintenance burden
3. **Automate Import Cleanup**: Implementare automated cleanup per import non utilizzati
4. **Establish Cleanup Process**: Creare processo sistematico di dead code identification e removal
5. **Monitor Dead Code Growth**: Implementare monitoring per prevenire accumulo futuro

## Metriche di Valore

- **Codebase Cleanliness**: Riduce technical debt e maintenance overhead
- **Developer Productivity**: Facilita navigazione e comprensione del codice
- **Build Performance**: Riduce compilation time e artifact size
- **Code Quality**: Migliora overall code quality metrics

## Classificazione

**Categoria**: Code Quality & Performance
**Priorità**: Media - Important per long-term maintainability
**Stakeholder**: Development team, Technical leads, Code reviewers