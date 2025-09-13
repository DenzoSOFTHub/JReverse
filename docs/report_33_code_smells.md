# Report 33: Code Smells

**Valore**: ⭐⭐⭐⭐ **Complessità**: 🔴 Complessa **Tempo**: 10-12 giorni
**Tags**: `#code-smells` `#anti-patterns` `#code-quality` `#refactoring`

## Descrizione

Identifica e analizza code smells nel codebase utilizzando pattern recognition avanzato, classificando anti-patterns per severità, suggerendo strategie di refactoring e valutando l'impatto sulla maintainability del codice.

## Sezioni del Report

### 1. Code Smell Classification
- Long Method detection e analysis
- Large Class identification
- Duplicate Code detection
- Dead Code identification
- Feature Envy detection
- God Class analysis

### 2. Anti-Pattern Analysis
- Spaghetti Code detection
- Copy-Paste Programming
- Magic Number identification
- Long Parameter List
- Switch Statement Abuse
- Data Clumping

### 3. Refactoring Recommendations
- Extract Method opportunities
- Extract Class suggestions
- Move Method recommendations
- Replace Magic Number with Named Constant
- Introduce Parameter Object
- Replace Conditional with Polymorphism

### 4. Quality Impact Assessment
- Maintainability score calculation
- Technical debt quantification
- Refactoring effort estimation
- Priority-based remediation roadmap

## Implementazione con Javassist

```java
public class CodeSmellAnalyzer {
    
    private final ClassPool classPool;
    private final Map<String, CodeSmellDetector> detectors = new HashMap<>();
    
    public CodeSmellAnalyzer(ClassPool classPool) {
        this.classPool = classPool;
        initializeDetectors();
    }
    
    private void initializeDetectors() {
        detectors.put("LONG_METHOD", new LongMethodDetector());
        detectors.put("LARGE_CLASS", new LargeClassDetector());
        detectors.put("DUPLICATE_CODE", new DuplicateCodeDetector());
        detectors.put("DEAD_CODE", new DeadCodeDetector());
        detectors.put("FEATURE_ENVY", new FeatureEnvyDetector());
        detectors.put("GOD_CLASS", new GodClassDetector());
        detectors.put("MAGIC_NUMBERS", new MagicNumberDetector());
        detectors.put("LONG_PARAMETER_LIST", new LongParameterListDetector());
        detectors.put("SWITCH_STATEMENT_ABUSE", new SwitchStatementAbuseDetector());
        detectors.put("DATA_CLUMPING", new DataClumpingDetector());
    }
    
    public CodeSmellReport analyzeCodeSmells(CtClass[] classes) {
        CodeSmellReport report = new CodeSmellReport();
        
        // 1. Esegui tutti i detector su tutte le classi
        for (CtClass ctClass : classes) {
            analyzeClassForCodeSmells(ctClass, report);
        }
        
        // 2. Analizza relazioni cross-class per code smells
        analyzeCrossClassCodeSmells(classes, report);
        
        // 3. Calcola metriche qualità aggregate
        calculateQualityMetrics(report);
        
        // 4. Genera raccomandazioni refactoring
        generateRefactoringRecommendations(report);
        
        return report;
    }
    
    private void analyzeClassForCodeSmells(CtClass ctClass, CodeSmellReport report) {
        ClassSmellAnalysis classAnalysis = new ClassSmellAnalysis();
        classAnalysis.setClassName(ctClass.getName());
        
        try {
            // Analizza class-level smells
            analyzeClassLevelSmells(ctClass, classAnalysis);
            
            // Analizza method-level smells
            CtMethod[] methods = ctClass.getDeclaredMethods();
            for (CtMethod method : methods) {
                analyzeMethodSmells(method, classAnalysis);
            }
            
            // Analizza field-level smells
            CtField[] fields = ctClass.getDeclaredFields();
            for (CtField field : fields) {
                analyzeFieldSmells(field, classAnalysis);
            }
            
        } catch (Exception e) {
            classAnalysis.addError("Error analyzing class: " + e.getMessage());
        }
        
        report.addClassAnalysis(classAnalysis);
    }
    
    private void analyzeClassLevelSmells(CtClass ctClass, ClassSmellAnalysis analysis) {
        // Large Class detection
        LargeClassDetector largeClassDetector = (LargeClassDetector) detectors.get("LARGE_CLASS");
        if (largeClassDetector.isLargeClass(ctClass)) {
            CodeSmell smell = new CodeSmell();
            smell.setType(CodeSmellType.LARGE_CLASS);
            smell.setSeverity(calculateLargeClassSeverity(ctClass));
            smell.setLocation(ctClass.getName());
            smell.setDescription(generateLargeClassDescription(ctClass));
            smell.setRefactoringRecommendation("Consider extracting responsibilities into separate classes");
            analysis.addCodeSmell(smell);
        }
        
        // God Class detection
        GodClassDetector godClassDetector = (GodClassDetector) detectors.get("GOD_CLASS");
        if (godClassDetector.isGodClass(ctClass)) {
            CodeSmell smell = new CodeSmell();
            smell.setType(CodeSmellType.GOD_CLASS);
            smell.setSeverity(SmellSeverity.CRITICAL);
            smell.setLocation(ctClass.getName());
            smell.setDescription("Class has too many responsibilities and high coupling");
            smell.setRefactoringRecommendation("Apply Single Responsibility Principle - extract classes");
            analysis.addCodeSmell(smell);
        }
        
        // Data Class detection
        if (isDataClass(ctClass)) {
            CodeSmell smell = new CodeSmell();
            smell.setType(CodeSmellType.DATA_CLASS);
            smell.setSeverity(SmellSeverity.MEDIUM);
            smell.setLocation(ctClass.getName());
            smell.setDescription("Class only contains fields and getters/setters");
            smell.setRefactoringRecommendation("Add business logic or consider if class is necessary");
            analysis.addCodeSmell(smell);
        }
    }
    
    private void analyzeMethodSmells(CtMethod method, ClassSmellAnalysis analysis) {
        try {
            String methodName = method.getName();
            String className = method.getDeclaringClass().getName();
            String fullMethodName = className + "." + methodName;
            
            // Long Method detection
            LongMethodDetector longMethodDetector = (LongMethodDetector) detectors.get("LONG_METHOD");
            if (longMethodDetector.isLongMethod(method)) {
                CodeSmell smell = new CodeSmell();
                smell.setType(CodeSmellType.LONG_METHOD);
                smell.setSeverity(calculateLongMethodSeverity(method));
                smell.setLocation(fullMethodName);
                smell.setDescription(generateLongMethodDescription(method));
                smell.setRefactoringRecommendation("Extract smaller methods to improve readability");
                analysis.addCodeSmell(smell);
            }
            
            // Long Parameter List detection
            CtClass[] paramTypes = method.getParameterTypes();
            if (paramTypes.length > 5) {
                CodeSmell smell = new CodeSmell();
                smell.setType(CodeSmellType.LONG_PARAMETER_LIST);
                smell.setSeverity(paramTypes.length > 8 ? SmellSeverity.HIGH : SmellSeverity.MEDIUM);
                smell.setLocation(fullMethodName);
                smell.setDescription(String.format("Method has %d parameters", paramTypes.length));
                smell.setRefactoringRecommendation("Introduce Parameter Object or Builder pattern");
                analysis.addCodeSmell(smell);
            }
            
            // Magic Number detection
            MagicNumberDetector magicDetector = (MagicNumberDetector) detectors.get("MAGIC_NUMBERS");
            List<MagicNumber> magicNumbers = magicDetector.findMagicNumbers(method);
            for (MagicNumber magicNumber : magicNumbers) {
                CodeSmell smell = new CodeSmell();
                smell.setType(CodeSmellType.MAGIC_NUMBER);
                smell.setSeverity(SmellSeverity.LOW);
                smell.setLocation(fullMethodName + ":" + magicNumber.getLineNumber());
                smell.setDescription("Magic number: " + magicNumber.getValue());
                smell.setRefactoringRecommendation("Replace with named constant");
                analysis.addCodeSmell(smell);
            }
            
            // Switch Statement Abuse detection
            if (hasSwitchStatementAbuse(method)) {
                CodeSmell smell = new CodeSmell();
                smell.setType(CodeSmellType.SWITCH_STATEMENT_ABUSE);
                smell.setSeverity(SmellSeverity.HIGH);
                smell.setLocation(fullMethodName);
                smell.setDescription("Complex switch statement should be replaced with polymorphism");
                smell.setRefactoringRecommendation("Replace conditional with polymorphism");
                analysis.addCodeSmell(smell);
            }
            
            // Feature Envy detection
            FeatureEnvyDetector envyDetector = (FeatureEnvyDetector) detectors.get("FEATURE_ENVY");
            String enviedClass = envyDetector.detectFeatureEnvy(method);
            if (enviedClass != null) {
                CodeSmell smell = new CodeSmell();
                smell.setType(CodeSmellType.FEATURE_ENVY);
                smell.setSeverity(SmellSeverity.MEDIUM);
                smell.setLocation(fullMethodName);
                smell.setDescription("Method is more interested in class: " + enviedClass);
                smell.setRefactoringRecommendation("Move method to " + enviedClass);
                analysis.addCodeSmell(smell);
            }
            
            // Complex Conditional detection
            int conditionalComplexity = calculateConditionalComplexity(method);
            if (conditionalComplexity > 4) {
                CodeSmell smell = new CodeSmell();
                smell.setType(CodeSmellType.COMPLEX_CONDITIONAL);
                smell.setSeverity(conditionalComplexity > 8 ? SmellSeverity.HIGH : SmellSeverity.MEDIUM);
                smell.setLocation(fullMethodName);
                smell.setDescription("Complex conditional logic with " + conditionalComplexity + " branches");
                smell.setRefactoringRecommendation("Extract condition methods or use Strategy pattern");
                analysis.addCodeSmell(smell);
            }
            
        } catch (NotFoundException e) {
            analysis.addError("Error analyzing method " + methodName + ": " + e.getMessage());
        }
    }
    
    private void analyzeFieldSmells(CtField field, ClassSmellAnalysis analysis) {
        try {
            String fieldName = field.getName();
            String className = field.getDeclaringClass().getName();
            String fullFieldName = className + "." + fieldName;
            
            // Public Field detection
            if (Modifier.isPublic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers())) {
                CodeSmell smell = new CodeSmell();
                smell.setType(CodeSmellType.PUBLIC_FIELD);
                smell.setSeverity(SmellSeverity.MEDIUM);
                smell.setLocation(fullFieldName);
                smell.setDescription("Non-final public field breaks encapsulation");
                smell.setRefactoringRecommendation("Make field private and provide accessor methods");
                analysis.addCodeSmell(smell);
            }
            
            // Unused Field detection (simplified)
            if (!isFieldUsed(field)) {
                CodeSmell smell = new CodeSmell();
                smell.setType(CodeSmellType.UNUSED_FIELD);
                smell.setSeverity(SmellSeverity.LOW);
                smell.setLocation(fullFieldName);
                smell.setDescription("Field appears to be unused");
                smell.setRefactoringRecommendation("Remove unused field");
                analysis.addCodeSmell(smell);
            }
            
        } catch (Exception e) {
            analysis.addError("Error analyzing field " + fieldName + ": " + e.getMessage());
        }
    }
    
    private void analyzeCrossClassCodeSmells(CtClass[] classes, CodeSmellReport report) {
        // Duplicate Code detection across classes
        DuplicateCodeDetector duplicateDetector = (DuplicateCodeDetector) detectors.get("DUPLICATE_CODE");
        List<DuplicateCodeInstance> duplicates = duplicateDetector.findDuplicates(classes);
        
        for (DuplicateCodeInstance duplicate : duplicates) {
            CodeSmell smell = new CodeSmell();
            smell.setType(CodeSmellType.DUPLICATE_CODE);
            smell.setSeverity(calculateDuplicateSeverity(duplicate));
            smell.setLocation(String.join(", ", duplicate.getLocations()));
            smell.setDescription(String.format("Duplicate code block of %d lines", duplicate.getLineCount()));
            smell.setRefactoringRecommendation("Extract common code into shared method or utility class");
            
            // Add to the first class analysis
            String firstClassName = duplicate.getLocations().get(0).split("\\.")[0];
            ClassSmellAnalysis firstClassAnalysis = report.getClassAnalyses().stream()
                .filter(analysis -> analysis.getClassName().equals(firstClassName))
                .findFirst()
                .orElse(null);
            
            if (firstClassAnalysis != null) {
                firstClassAnalysis.addCodeSmell(smell);
            }
        }
        
        // Data Clumping detection
        DataClumpingDetector clumpingDetector = (DataClumpingDetector) detectors.get("DATA_CLUMPING");
        List<DataClump> dataClumps = clumpingDetector.findDataClumps(classes);
        
        for (DataClump clump : dataClumps) {
            CodeSmell smell = new CodeSmell();
            smell.setType(CodeSmellType.DATA_CLUMPING);
            smell.setSeverity(SmellSeverity.MEDIUM);
            smell.setLocation(String.join(", ", clump.getAffectedMethods()));
            smell.setDescription("Parameters " + clump.getClumpedParameters() + " appear together frequently");
            smell.setRefactoringRecommendation("Introduce Parameter Object for clumped data");
            
            // Add to relevant class analyses
            for (String methodLocation : clump.getAffectedMethods()) {
                String className = methodLocation.split("\\.")[0];
                ClassSmellAnalysis classAnalysis = report.getClassAnalyses().stream()
                    .filter(analysis -> analysis.getClassName().equals(className))
                    .findFirst()
                    .orElse(null);
                
                if (classAnalysis != null) {
                    classAnalysis.addCodeSmell(smell);
                }
            }
        }
    }
    
    private void calculateQualityMetrics(CodeSmellReport report) {
        QualityMetrics metrics = new QualityMetrics();
        
        // Count smells by type and severity
        Map<CodeSmellType, Integer> smellTypeCounts = new HashMap<>();
        Map<SmellSeverity, Integer> severityCounts = new HashMap<>();
        
        int totalSmells = 0;
        int totalClasses = report.getClassAnalyses().size();
        
        for (ClassSmellAnalysis classAnalysis : report.getClassAnalyses()) {
            for (CodeSmell smell : classAnalysis.getCodeSmells()) {
                totalSmells++;
                smellTypeCounts.merge(smell.getType(), 1, Integer::sum);
                severityCounts.merge(smell.getSeverity(), 1, Integer::sum);
            }
        }
        
        metrics.setTotalCodeSmells(totalSmells);
        metrics.setTotalClasses(totalClasses);
        metrics.setSmellDensity((double) totalSmells / totalClasses);
        metrics.setSmellTypeDistribution(smellTypeCounts);
        metrics.setSeverityDistribution(severityCounts);
        
        // Calculate maintainability index (0-100)
        int maintainabilityIndex = calculateMaintainabilityIndex(severityCounts, totalSmells);
        metrics.setMaintainabilityIndex(maintainabilityIndex);
        
        // Calculate technical debt estimate
        double technicalDebt = calculateTechnicalDebt(report);
        metrics.setTechnicalDebtHours(technicalDebt);
        
        report.setQualityMetrics(metrics);
    }
    
    private int calculateMaintainabilityIndex(Map<SmellSeverity, Integer> severityCounts, int totalSmells) {
        if (totalSmells == 0) return 100;
        
        int criticalPenalty = severityCounts.getOrDefault(SmellSeverity.CRITICAL, 0) * 20;
        int highPenalty = severityCounts.getOrDefault(SmellSeverity.HIGH, 0) * 10;
        int mediumPenalty = severityCounts.getOrDefault(SmellSeverity.MEDIUM, 0) * 5;
        int lowPenalty = severityCounts.getOrDefault(SmellSeverity.LOW, 0) * 2;
        
        int totalPenalty = criticalPenalty + highPenalty + mediumPenalty + lowPenalty;
        
        return Math.max(0, Math.min(100, 100 - totalPenalty));
    }
    
    private double calculateTechnicalDebt(CodeSmellReport report) {
        double totalHours = 0.0;
        
        for (ClassSmellAnalysis classAnalysis : report.getClassAnalyses()) {
            for (CodeSmell smell : classAnalysis.getCodeSmells()) {
                switch (smell.getSeverity()) {
                    case CRITICAL:
                        totalHours += 8.0; // 1 day
                        break;
                    case HIGH:
                        totalHours += 4.0; // 0.5 day
                        break;
                    case MEDIUM:
                        totalHours += 2.0; // 0.25 day
                        break;
                    case LOW:
                        totalHours += 0.5; // 1 hour
                        break;
                }
            }
        }
        
        return totalHours;
    }
    
    private void generateRefactoringRecommendations(CodeSmellReport report) {
        RefactoringStrategy strategy = new RefactoringStrategy();
        
        // Group smells by refactoring strategy
        Map<String, List<CodeSmell>> strategiesByType = new HashMap<>();
        
        for (ClassSmellAnalysis classAnalysis : report.getClassAnalyses()) {
            for (CodeSmell smell : classAnalysis.getCodeSmells()) {
                String strategyType = determineRefactoringStrategy(smell.getType());
                strategiesByType.computeIfAbsent(strategyType, k -> new ArrayList<>()).add(smell);
            }
        }
        
        // Create refactoring recommendations
        List<RefactoringRecommendation> recommendations = new ArrayList<>();
        
        for (Map.Entry<String, List<CodeSmell>> entry : strategiesByType.entrySet()) {
            RefactoringRecommendation recommendation = new RefactoringRecommendation();
            recommendation.setStrategy(entry.getKey());
            recommendation.setAffectedSmells(entry.getValue());
            recommendation.setPriority(calculateRefactoringPriority(entry.getValue()));
            recommendation.setEstimatedEffort(calculateRefactoringEffort(entry.getValue()));
            recommendation.setDescription(generateRefactoringDescription(entry.getKey(), entry.getValue()));
            
            recommendations.add(recommendation);
        }
        
        // Sort by priority
        recommendations.sort((a, b) -> b.getPriority().compareTo(a.getPriority()));
        
        strategy.setRecommendations(recommendations);
        report.setRefactoringStrategy(strategy);
    }
    
    // Helper methods for various detections
    private boolean isDataClass(CtClass ctClass) {
        try {
            CtMethod[] methods = ctClass.getDeclaredMethods();
            CtField[] fields = ctClass.getDeclaredFields();
            
            if (fields.length == 0) return false;
            
            int getterSetterCount = 0;
            for (CtMethod method : methods) {
                if (isGetterOrSetter(method)) {
                    getterSetterCount++;
                }
            }
            
            // If most methods are just getters/setters, it's likely a data class
            return getterSetterCount > (methods.length * 0.8);
            
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean isGetterOrSetter(CtMethod method) {
        String methodName = method.getName();
        return (methodName.startsWith("get") && methodName.length() > 3) ||
               (methodName.startsWith("set") && methodName.length() > 3) ||
               (methodName.startsWith("is") && methodName.length() > 2);
    }
    
    private int calculateConditionalComplexity(CtMethod method) {
        try {
            AtomicInteger complexity = new AtomicInteger(0);
            
            method.instrument(new ExprEditor() {
                @Override
                public void edit(MethodCall call) throws CannotCompileException {
                    // Count if/switch statements
                }
            });
            
            return complexity.get();
        } catch (CannotCompileException e) {
            return 0;
        }
    }
}

public class CodeSmellReport {
    private List<ClassSmellAnalysis> classAnalyses = new ArrayList<>();
    private QualityMetrics qualityMetrics;
    private RefactoringStrategy refactoringStrategy;
    private List<String> errors = new ArrayList<>();
    
    public static class ClassSmellAnalysis {
        private String className;
        private List<CodeSmell> codeSmells = new ArrayList<>();
        private List<String> errors = new ArrayList<>();
    }
    
    public static class CodeSmell {
        private CodeSmellType type;
        private SmellSeverity severity;
        private String location;
        private String description;
        private String refactoringRecommendation;
        private double estimatedEffort; // in hours
    }
    
    public enum CodeSmellType {
        LONG_METHOD, LARGE_CLASS, DUPLICATE_CODE, DEAD_CODE, FEATURE_ENVY,
        GOD_CLASS, MAGIC_NUMBER, LONG_PARAMETER_LIST, SWITCH_STATEMENT_ABUSE,
        DATA_CLUMPING, DATA_CLASS, PUBLIC_FIELD, UNUSED_FIELD, COMPLEX_CONDITIONAL
    }
    
    public enum SmellSeverity {
        CRITICAL, HIGH, MEDIUM, LOW
    }
}
```

## Raccolta Dati

### 1. Method-Level Analysis
- Lunghezza metodi (linee di codice)
- Complessità ciclomatica per metodo
- Numero parametri per metodo
- Identificazione magic numbers

### 2. Class-Level Analysis
- Dimensione classi (fields, methods, LOC)
- Responsabilità multiple detection
- Coupling e cohesion metrics
- Data class pattern detection

### 3. Code Structure Analysis
- Duplicate code blocks identification
- Switch statement complexity analysis
- Conditional logic complexity
- Dead code detection

### 4. Cross-Class Analysis
- Feature envy detection
- Data clumping identification
- God class relationships
- Architectural smell detection

## Metriche di Qualità del Codice

### Algoritmo di Calcolo (0-100)

```java
public int calculateCodeSmellQualityScore(CodeSmellReport result) {
    double score = 100.0;
    
    // Penalizzazioni per code smells per severità
    score -= result.getCriticalCodeSmells() * 15;             // -15 per ogni smell critico
    score -= result.getHighSeverityCodeSmells() * 8;          // -8 per ogni smell high severity
    score -= result.getMediumSeverityCodeSmells() * 4;        // -4 per ogni smell medium severity
    score -= result.getLowSeverityCodeSmells() * 1;           // -1 per ogni smell low severity
    
    // Penalizzazioni specifiche per tipi problematici
    score -= result.getGodClasses() * 20;                     // -20 per ogni God Class
    score -= result.getLongMethods() * 6;                     // -6 per ogni Long Method
    score -= result.getLargeClasses() * 10;                   // -10 per ogni Large Class
    score -= result.getDuplicateCodeInstances() * 12;         // -12 per ogni istanza duplicate code
    score -= result.getFeatureEnvyMethods() * 5;              // -5 per ogni Feature Envy method
    score -= result.getSwitchStatementAbuses() * 8;           // -8 per ogni Switch Statement Abuse
    score -= result.getDataClumps() * 3;                      // -3 per ogni Data Clump
    
    // Bonus per buone pratiche (assenza di smell)
    score += result.getWellStructuredClasses() * 1;           // +1 per classi ben strutturate
    score += result.getCleanMethods() * 0.5;                  // +0.5 per metodi puliti
    score += result.getProperEncapsulation() * 0.5;           // +0.5 per encapsulation appropriata
    
    return Math.max(0, Math.min(100, (int) score));
}
```

### Soglie di Valutazione
- **0-40**: 🔴 CRITICO - Codice con gravi problemi di quality e maintainability
- **41-60**: 🟡 SUFFICIENTE - Codice funzionale ma con molti code smells
- **61-80**: 🟢 BUONO - Codice di buona qualità con minor code smells
- **81-100**: ⭐ ECCELLENTE - Codice pulito con excellent maintainability

## Segnalazioni per Gravità

### 🔴 GRAVITÀ CRITICA (Score Impact: -15 to -20)
1. **God Class (classe con troppe responsabilità)**
   - Descrizione: Classe con più di 1000 linee o più di 20 responsabilità diverse
   - Rischio: Difficile maintenance, testing complesso, violazione SRP
   - Soluzione: Applicare Single Responsibility Principle, estrarre classi separate

2. **Duplicate Code esteso (>50 linee)**
   - Descrizione: Blocchi di codice duplicato significativi attraverso multiple classi
   - Rischio: Maintenance nightmare, bug propagation, inconsistenza
   - Soluzione: Extract Method, creare utility classes, applicare DRY principle

### 🟠 GRAVITÀ ALTA (Score Impact: -8 to -12)
3. **Large Class (classe troppo grande)**
   - Descrizione: Classe con più di 500 linee di codice o più di 15 metodi pubblici
   - Rischio: Difficile comprensione, violazione cohesion, testing difficile
   - Soluzione: Extract Class, separare responsabilità, applicare cohesion principles

4. **Switch Statement Abuse**
   - Descrizione: Switch statement complessi con molti case o logica business
   - Rischio: Violazione Open/Closed Principle, difficile extension
   - Soluzione: Replace Conditional with Polymorphism, Strategy pattern

### 🟡 GRAVITÀ MEDIA (Score Impact: -4 to -6)
5. **Long Method (metodo troppo lungo)**
   - Descrizione: Metodi con più di 30 linee di codice o alta complessità ciclomatica
   - Rischio: Difficile comprensione, testing complesso, poor reusability
   - Soluzione: Extract Method, scomporre in metodi più piccoli

6. **Feature Envy (method che usa troppo un'altra classe)**
   - Descrizione: Metodo che accede frequentemente ai dati di un'altra classe
   - Rischio: Poor encapsulation, tight coupling, inappropriate responsibilities
   - Soluzione: Move Method verso la classe appropriata

### 🔵 GRAVITÀ BASSA (Score Impact: -1 to -3)
7. **Magic Numbers**
   - Descrizione: Uso di numeri hardcoded senza spiegazione nel codice
   - Rischio: Poor readability, difficile maintenance, unclear intent
   - Soluzione: Replace Magic Number with Named Constant

8. **Data Clumping**
   - Descrizione: Stesso gruppo di parametri utilizzato in multiple method signatures
   - Rischio: Parameter list pollution, missed abstraction opportunities
   - Soluzione: Introduce Parameter Object per raggruppare dati correlati

## Metriche di Valore

- **Code Maintainability**: Migliora drasticamente maintainability riducendo complexity
- **Development Velocity**: Accelera development rimuovendo impedimenti code quality
- **Bug Reduction**: Riduce bug density attraverso cleaner code structure
- **Onboarding Speed**: Facilita comprensione codice per nuovi team members

## Classificazione

**Categoria**: Code Quality & Performance
**Priorità**: Alta - Code smells impattano significativamente maintainability
**Stakeholder**: Development team, Technical leads, Quality assurance

## Tags per Classificazione

`#code-smells` `#anti-patterns` `#code-quality` `#refactoring` `#maintainability` `#technical-debt` `#clean-code` `#software-quality`