# Report 32: Lunghezza Media delle Classi e dei Metodi

**Valore**: ⭐⭐⭐ **Complessità**: 🟢 Semplice **Tempo**: 3-4 giorni
**Tags**: `#code-metrics` `#method-length` `#class-size`

## Descrizione

Questo report analizza le metriche di lunghezza delle classi e dei metodi nell'applicazione, identificando classi troppo grandi (God Classes), metodi troppo lunghi, e fornendo statistiche per valutare la manutenibilità e leggibilità del codice secondo le best practices.

## Obiettivi dell'Analisi

1. **Class Size Analysis**: Misurazione numero di metodi, campi e righe per classe
2. **Method Length Analysis**: Analisi lunghezza metodi in righe di codice
3. **God Class Detection**: Identificazione classi eccessivamente grandi
4. **Long Method Detection**: Rilevamento metodi troppo lunghi
5. **Maintainability Assessment**: Valutazione manutenibilità basata su metriche

## Implementazione con Javassist

```java
package com.jreverse.analyzer.metrics;

import javassist.*;
import javassist.bytecode.*;
import javassist.expr.*;
import java.util.*;
import java.util.stream.Collectors;
import java.io.IOException;

/**
 * Analyzer per metriche di lunghezza classi e metodi
 */
public class ClassMethodLengthAnalyzer {
    
    private ClassPool classPool;
    private Map<String, ClassMetrics> classMetrics;
    private Map<String, MethodMetrics> methodMetrics;
    private List<CodeIssue> codeIssues;
    private List<String> godClasses;
    private List<String> longMethods;
    
    // Soglie per identificare problemi
    private static final int MAX_CLASS_METHODS = 20;
    private static final int MAX_CLASS_FIELDS = 15;
    private static final int MAX_CLASS_LINES = 300;
    private static final int MAX_METHOD_LINES = 30;
    private static final int MAX_CONSTRUCTOR_LINES = 20;
    private static final int MAX_PARAMETER_COUNT = 5;
    
    // Soglie per God Classes
    private static final int GOD_CLASS_METHOD_THRESHOLD = 30;
    private static final int GOD_CLASS_FIELD_THRESHOLD = 20;
    private static final int GOD_CLASS_LINE_THRESHOLD = 500;
    
    public ClassMethodLengthAnalyzer() {
        this.classPool = ClassPool.getDefault();
        this.classMetrics = new HashMap<>();
        this.methodMetrics = new HashMap<>();
        this.codeIssues = new ArrayList<>();
        this.godClasses = new ArrayList<>();
        this.longMethods = new ArrayList<>();
    }
    
    /**
     * Analizza metriche di lunghezza nell'applicazione
     */
    public LengthAnalysisReport analyzeLengthMetrics(List<String> classNames) throws Exception {
        for (String className : classNames) {
            try {
                CtClass ctClass = classPool.get(className);
                analyzeClass(ctClass);
            } catch (Exception e) {
                System.err.println("Errore nell'analisi della classe " + className + ": " + e.getMessage());
            }
        }
        
        // Identifica problemi
        identifyGodClasses();
        identifyLongMethods();
        generateRecommendations();
        
        return generateReport();
    }
    
    /**
     * Analizza una singola classe
     */
    private void analyzeClass(CtClass ctClass) throws Exception {
        String className = ctClass.getName();
        
        // Skip classi di sistema e generate
        if (isSystemOrGeneratedClass(className)) {
            return;
        }
        
        ClassMetrics metrics = new ClassMetrics(className);
        
        // Analizza struttura classe
        analyzeClassStructure(ctClass, metrics);
        
        // Analizza metodi della classe
        analyzeClassMethods(ctClass, metrics);
        
        // Analizza campi della classe
        analyzeClassFields(ctClass, metrics);
        
        // Calcola metriche aggregate
        calculateClassMetrics(metrics);
        
        classMetrics.put(className, metrics);
    }
    
    /**
     * Verifica se è classe di sistema o generata
     */
    private boolean isSystemOrGeneratedClass(String className) {
        return className.startsWith("java.") ||
               className.startsWith("javax.") ||
               className.startsWith("org.springframework.") ||
               className.contains("$$") ||
               className.contains("_") ||
               className.endsWith("Test");
    }
    
    /**
     * Analizza struttura generale della classe
     */
    private void analyzeClassStructure(CtClass ctClass, ClassMetrics metrics) throws Exception {
        // Conta righe classe (approssimazione dal bytecode)
        metrics.estimatedLines = estimateClassLines(ctClass);
        
        // Verifica se è interfaccia, abstract, enum
        metrics.isInterface = ctClass.isInterface();
        metrics.isAbstract = Modifier.isAbstract(ctClass.getModifiers());
        metrics.isEnum = ctClass.isEnum();
        
        // Analizza inheritance
        CtClass superClass = ctClass.getSuperclass();
        if (superClass != null && !superClass.getName().equals("java.lang.Object")) {
            metrics.hasSuperClass = true;
            metrics.superClassName = superClass.getName();
        }
        
        // Conta interfacce implementate
        CtClass[] interfaces = ctClass.getInterfaces();
        metrics.implementedInterfacesCount = interfaces.length;
        
        // Analizza annotations
        metrics.annotationCount = countClassAnnotations(ctClass);
    }
    
    /**
     * Stima righe di codice della classe
     */
    private int estimateClassLines(CtClass ctClass) throws Exception {
        int estimatedLines = 0;
        
        // Base lines per dichiarazione classe
        estimatedLines += 5;
        
        // Linee per campi
        estimatedLines += ctClass.getDeclaredFields().length * 2;
        
        // Linee per metodi (stima dalla complessità bytecode)
        for (CtMethod method : ctClass.getDeclaredMethods()) {
            estimatedLines += estimateMethodLines(method);
        }
        
        // Linee per costruttori
        for (CtConstructor constructor : ctClass.getDeclaredConstructors()) {
            estimatedLines += estimateConstructorLines(constructor);
        }
        
        return estimatedLines;
    }
    
    /**
     * Stima righe di un metodo dal bytecode
     */
    private int estimateMethodLines(CtMethod method) throws Exception {
        try {
            CodeAttribute code = method.getMethodInfo().getCodeAttribute();
            if (code == null) {
                return 1; // Metodo astratto o nativo
            }
            
            int codeLength = code.getCode().length;
            
            // Stima approssimativa: 1 riga Java ~ 3-5 bytes bytecode
            int estimatedLines = Math.max(1, codeLength / 4);
            
            // Aggiustamenti basati su pattern
            estimatedLines += countMethodCallsInBytecode(method) / 2;
            estimatedLines += countBranchingInstructions(method);
            
            return Math.min(estimatedLines, 200); // Cap massimo ragionevole
            
        } catch (Exception e) {
            return 10; // Default ragionevole
        }
    }
    
    /**
     * Conta chiamate a metodi nel bytecode
     */
    private int countMethodCallsInBytecode(CtMethod method) throws Exception {
        final int[] count = {0};
        
        try {
            method.instrument(new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    count[0]++;
                }
            });
        } catch (Exception e) {
            // Ignora errori di strumentazione
        }
        
        return count[0];
    }
    
    /**
     * Conta istruzioni di branching nel bytecode
     */
    private int countBranchingInstructions(CtMethod method) throws Exception {
        try {
            CodeAttribute code = method.getMethodInfo().getCodeAttribute();
            if (code == null) return 0;
            
            byte[] bytecode = code.getCode();
            int branches = 0;
            
            for (int i = 0; i < bytecode.length; i++) {
                int opcode = bytecode[i] & 0xFF;
                
                // Conta istruzioni condizionali e loop
                if ((opcode >= 153 && opcode <= 166) || // if_icmp, if_acmp, etc.
                    (opcode >= 198 && opcode <= 199) || // ifnull, ifnonnull
                    (opcode >= 167 && opcode <= 168)) { // goto, jsr
                    branches++;
                }
            }
            
            return branches;
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * Stima righe di un costruttore
     */
    private int estimateConstructorLines(CtConstructor constructor) throws Exception {
        try {
            CodeAttribute code = constructor.getMethodInfo().getCodeAttribute();
            if (code == null) return 1;
            
            int codeLength = code.getCode().length;
            return Math.max(3, Math.min(codeLength / 4, 50));
        } catch (Exception e) {
            return 5; // Default per costruttore
        }
    }
    
    /**
     * Conta annotations della classe
     */
    private int countClassAnnotations(CtClass ctClass) {
        try {
            AnnotationsAttribute attr = (AnnotationsAttribute)
                ctClass.getClassFile().getAttribute(AnnotationsAttribute.visibleTag);
            return attr != null ? attr.numAnnotations() : 0;
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * Analizza metodi della classe
     */
    private void analyzeClassMethods(CtClass ctClass, ClassMetrics classMetrics) throws Exception {
        String className = ctClass.getName();
        
        for (CtMethod method : ctClass.getDeclaredMethods()) {
            MethodMetrics methodMetrics = analyzeMethod(className, method);
            
            classMetrics.methodCount++;
            classMetrics.totalMethodLines += methodMetrics.estimatedLines;
            classMetrics.methods.add(methodMetrics);
            
            // Aggiorna statistiche classe
            if (methodMetrics.estimatedLines > classMetrics.longestMethodLines) {
                classMetrics.longestMethodLines = methodMetrics.estimatedLines;
                classMetrics.longestMethodName = method.getName();
            }
            
            // Memorizza metriche metodo
            String methodId = className + "." + method.getName();
            this.methodMetrics.put(methodId, methodMetrics);
        }
    }
    
    /**
     * Analizza un singolo metodo
     */
    private MethodMetrics analyzeMethod(String className, CtMethod method) throws Exception {
        MethodMetrics metrics = new MethodMetrics();
        
        metrics.className = className;
        metrics.methodName = method.getName();
        metrics.methodId = className + "." + method.getName();
        
        // Analizza signature
        metrics.parameterCount = method.getParameterTypes().length;
        metrics.returnType = method.getReturnType().getName();
        
        // Analizza modifiers
        int modifiers = method.getModifiers();
        metrics.isPublic = Modifier.isPublic(modifiers);
        metrics.isPrivate = Modifier.isPrivate(modifiers);
        metrics.isProtected = Modifier.isProtected(modifiers);
        metrics.isStatic = Modifier.isStatic(modifiers);
        metrics.isAbstract = Modifier.isAbstract(modifiers);
        metrics.isFinal = Modifier.isFinal(modifiers);
        
        // Stima righe metodo
        metrics.estimatedLines = estimateMethodLines(method);
        
        // Analizza complessità
        metrics.cyclomaticComplexity = calculateCyclomaticComplexity(method);
        metrics.methodCallCount = countMethodCallsInBytecode(method);
        
        // Verifica annotations
        metrics.annotationCount = countMethodAnnotations(method);
        
        // Identifica pattern specifici
        identifyMethodPatterns(method, metrics);
        
        return metrics;
    }
    
    /**
     * Calcola complessità ciclomatica
     */
    private int calculateCyclomaticComplexity(CtMethod method) throws Exception {
        try {
            int complexity = 1; // Base complexity
            
            // Conta branch instructions nel bytecode
            CodeAttribute code = method.getMethodInfo().getCodeAttribute();
            if (code != null) {
                byte[] bytecode = code.getCode();
                
                for (int i = 0; i < bytecode.length; i++) {
                    int opcode = bytecode[i] & 0xFF;
                    
                    // Incrementa per ogni decisione point
                    if (isDecisionInstruction(opcode)) {
                        complexity++;
                    }
                }
            }
            
            return complexity;
        } catch (Exception e) {
            return 1; // Default minimo
        }
    }
    
    /**
     * Verifica se è istruzione di decisione
     */
    private boolean isDecisionInstruction(int opcode) {
        return (opcode >= 153 && opcode <= 166) || // if_icmp variants
               (opcode >= 198 && opcode <= 199) || // ifnull, ifnonnull
               opcode == 170 || opcode == 171;     // tableswitch, lookupswitch
    }
    
    /**
     * Conta annotations del metodo
     */
    private int countMethodAnnotations(CtMethod method) {
        try {
            AnnotationsAttribute attr = (AnnotationsAttribute)
                method.getMethodInfo().getAttribute(AnnotationsAttribute.visibleTag);
            return attr != null ? attr.numAnnotations() : 0;
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * Identifica pattern specifici del metodo
     */
    private void identifyMethodPatterns(CtMethod method, MethodMetrics metrics) throws Exception {
        String methodName = method.getName();
        
        // Getter/Setter detection
        if (methodName.startsWith("get") && metrics.parameterCount == 0) {
            metrics.isGetter = true;
        } else if (methodName.startsWith("set") && metrics.parameterCount == 1) {
            metrics.isSetter = true;
        }
        
        // Constructor detection
        if (methodName.equals("<init>")) {
            metrics.isConstructor = true;
        }
        
        // Override detection
        metrics.hasOverrideAnnotation = hasAnnotation(method, "java.lang.Override");
        
        // Main method detection
        if (methodName.equals("main") && metrics.isStatic && metrics.isPublic) {
            metrics.isMainMethod = true;
        }
    }
    
    /**
     * Verifica presenza annotation specifica
     */
    private boolean hasAnnotation(CtMethod method, String annotationName) {
        try {
            AnnotationsAttribute attr = (AnnotationsAttribute)
                method.getMethodInfo().getAttribute(AnnotationsAttribute.visibleTag);
            
            if (attr != null) {
                for (Annotation annotation : attr.getAnnotations()) {
                    if (annotation.getTypeName().equals(annotationName)) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Analizza campi della classe
     */
    private void analyzeClassFields(CtClass ctClass, ClassMetrics classMetrics) throws Exception {
        CtField[] fields = ctClass.getDeclaredFields();
        classMetrics.fieldCount = fields.length;
        
        for (CtField field : fields) {
            FieldMetrics fieldMetrics = analyzeField(field);
            classMetrics.fields.add(fieldMetrics);
            
            // Aggiorna statistiche classe
            if (fieldMetrics.isStatic) classMetrics.staticFieldCount++;
            if (fieldMetrics.isFinal) classMetrics.finalFieldCount++;
            if (fieldMetrics.isPublic) classMetrics.publicFieldCount++;
        }
    }
    
    /**
     * Analizza un singolo campo
     */
    private FieldMetrics analyzeField(CtField field) throws Exception {
        FieldMetrics metrics = new FieldMetrics();
        
        metrics.fieldName = field.getName();
        metrics.fieldType = field.getType().getName();
        
        int modifiers = field.getModifiers();
        metrics.isPublic = Modifier.isPublic(modifiers);
        metrics.isPrivate = Modifier.isPrivate(modifiers);
        metrics.isProtected = Modifier.isProtected(modifiers);
        metrics.isStatic = Modifier.isStatic(modifiers);
        metrics.isFinal = Modifier.isFinal(modifiers);
        metrics.isTransient = Modifier.isTransient(modifiers);
        metrics.isVolatile = Modifier.isVolatile(modifiers);
        
        return metrics;
    }
    
    /**
     * Calcola metriche aggregate della classe
     */
    private void calculateClassMetrics(ClassMetrics metrics) {
        // Calcola metriche derivate
        metrics.averageMethodLines = metrics.methodCount > 0 ? 
            (double) metrics.totalMethodLines / metrics.methodCount : 0.0;
        
        // Calcola indice di complessità classe
        metrics.complexityIndex = calculateClassComplexityIndex(metrics);
        
        // Calcola maintainability index
        metrics.maintainabilityIndex = calculateMaintainabilityIndex(metrics);
        
        // Verifica soglie problematiche
        checkClassThresholds(metrics);
    }
    
    /**
     * Calcola indice di complessità della classe
     */
    private double calculateClassComplexityIndex(ClassMetrics metrics) {
        double complexity = 0.0;
        
        // Fattori di complessità
        complexity += metrics.methodCount * 0.5;
        complexity += metrics.fieldCount * 0.3;
        complexity += metrics.estimatedLines * 0.01;
        complexity += metrics.methods.stream()
            .mapToInt(m -> m.cyclomaticComplexity)
            .sum() * 0.2;
        
        // Penalità per pattern problematici
        if (metrics.longestMethodLines > MAX_METHOD_LINES) complexity += 2.0;
        if (metrics.publicFieldCount > 5) complexity += 1.5;
        
        return complexity;
    }
    
    /**
     * Calcola indice di manutenibilità
     */
    private double calculateMaintainabilityIndex(ClassMetrics metrics) {
        double maintainability = 100.0;
        
        // Penalità per dimensioni eccessive
        if (metrics.methodCount > MAX_CLASS_METHODS) {
            maintainability -= (metrics.methodCount - MAX_CLASS_METHODS) * 2.0;
        }
        
        if (metrics.estimatedLines > MAX_CLASS_LINES) {
            maintainability -= (metrics.estimatedLines - MAX_CLASS_LINES) * 0.05;
        }
        
        // Penalità per complessità alta
        maintainability -= metrics.complexityIndex * 2.0;
        
        // Bonus per good practices
        double getterSetterRatio = (double) metrics.methods.stream()
            .mapToInt(m -> (m.isGetter || m.isSetter) ? 1 : 0).sum() / metrics.methodCount;
        
        if (getterSetterRatio < 0.3) maintainability += 5.0; // Non troppi getter/setter
        
        return Math.max(0.0, Math.min(100.0, maintainability));
    }
    
    /**
     * Verifica soglie problematiche
     */
    private void checkClassThresholds(ClassMetrics metrics) {
        String className = metrics.className;
        
        // Verifica God Class
        if (metrics.methodCount > GOD_CLASS_METHOD_THRESHOLD ||
            metrics.fieldCount > GOD_CLASS_FIELD_THRESHOLD ||
            metrics.estimatedLines > GOD_CLASS_LINE_THRESHOLD) {
            
            metrics.isGodClass = true;
        }
        
        // Verifica altre soglie
        if (metrics.methodCount > MAX_CLASS_METHODS) {
            codeIssues.add(new CodeIssue(
                IssueType.TOO_MANY_METHODS,
                "Classe con troppi metodi: " + metrics.methodCount,
                className,
                IssueSeverity.MEDIUM
            ));
        }
        
        if (metrics.fieldCount > MAX_CLASS_FIELDS) {
            codeIssues.add(new CodeIssue(
                IssueType.TOO_MANY_FIELDS,
                "Classe con troppi campi: " + metrics.fieldCount,
                className,
                IssueSeverity.MEDIUM
            ));
        }
        
        if (metrics.estimatedLines > MAX_CLASS_LINES) {
            codeIssues.add(new CodeIssue(
                IssueType.CLASS_TOO_LONG,
                "Classe troppo lunga: ~" + metrics.estimatedLines + " righe",
                className,
                IssueSeverity.HIGH
            ));
        }
    }
    
    /**
     * Identifica God Classes
     */
    private void identifyGodClasses() {
        for (ClassMetrics metrics : classMetrics.values()) {
            if (metrics.isGodClass) {
                godClasses.add(metrics.className);
                
                codeIssues.add(new CodeIssue(
                    IssueType.GOD_CLASS,
                    String.format("God Class rilevata: %d metodi, %d campi, ~%d righe",
                        metrics.methodCount, metrics.fieldCount, metrics.estimatedLines),
                    metrics.className,
                    IssueSeverity.CRITICAL
                ));
            }
        }
    }
    
    /**
     * Identifica metodi troppo lunghi
     */
    private void identifyLongMethods() {
        for (MethodMetrics metrics : methodMetrics.values()) {
            if (metrics.estimatedLines > MAX_METHOD_LINES) {
                longMethods.add(metrics.methodId);
                
                IssueSeverity severity = IssueSeverity.MEDIUM;
                if (metrics.estimatedLines > MAX_METHOD_LINES * 2) {
                    severity = IssueSeverity.HIGH;
                }
                
                codeIssues.add(new CodeIssue(
                    IssueType.METHOD_TOO_LONG,
                    "Metodo troppo lungo: ~" + metrics.estimatedLines + " righe",
                    metrics.methodId,
                    severity
                ));
            }
            
            // Verifica parametri eccessivi
            if (metrics.parameterCount > MAX_PARAMETER_COUNT) {
                codeIssues.add(new CodeIssue(
                    IssueType.TOO_MANY_PARAMETERS,
                    "Troppi parametri: " + metrics.parameterCount,
                    metrics.methodId,
                    IssueSeverity.MEDIUM
                ));
            }
        }
    }
    
    /**
     * Genera raccomandazioni
     */
    private void generateRecommendations() {
        // Le raccomandazioni vengono generate come parte dei CodeIssue
        // Qui possiamo aggiungere suggerimenti più generali
        
        double avgClassSize = classMetrics.values().stream()
            .mapToInt(m -> m.estimatedLines)
            .average().orElse(0.0);
        
        if (avgClassSize > MAX_CLASS_LINES * 0.8) {
            codeIssues.add(new CodeIssue(
                IssueType.GENERAL_RECOMMENDATION,
                "Dimensioni classi tendenzialmente elevate (media: " + 
                String.format("%.1f", avgClassSize) + " righe). Considera refactoring.",
                "GLOBAL",
                IssueSeverity.LOW
            ));
        }
    }
    
    /**
     * Genera report finale
     */
    private LengthAnalysisReport generateReport() {
        LengthAnalysisReport report = new LengthAnalysisReport();
        
        report.totalClasses = classMetrics.size();
        report.totalMethods = methodMetrics.size();
        report.classMetrics = new HashMap<>(classMetrics);
        report.methodMetrics = new HashMap<>(methodMetrics);
        report.codeIssues = new ArrayList<>(codeIssues);
        report.godClasses = new ArrayList<>(godClasses);
        report.longMethods = new ArrayList<>(longMethods);
        
        // Calcola statistiche aggregate
        report.averageClassLength = calculateAverageClassLength();
        report.averageMethodLength = calculateAverageMethodLength();
        report.averageMethodsPerClass = calculateAverageMethodsPerClass();
        report.averageFieldsPerClass = calculateAverageFieldsPerClass();
        
        // Distribuzione dimensioni
        report.classSizeDistribution = calculateClassSizeDistribution();
        report.methodSizeDistribution = calculateMethodSizeDistribution();
        
        // Metriche qualità
        report.overallMaintainabilityIndex = calculateOverallMaintainabilityIndex();
        report.codeQualityScore = calculateCodeQualityScore();
        
        return report;
    }
    
    private double calculateAverageClassLength() {
        return classMetrics.values().stream()
            .mapToInt(m -> m.estimatedLines)
            .average().orElse(0.0);
    }
    
    private double calculateAverageMethodLength() {
        return methodMetrics.values().stream()
            .mapToInt(m -> m.estimatedLines)
            .average().orElse(0.0);
    }
    
    private double calculateAverageMethodsPerClass() {
        return classMetrics.values().stream()
            .mapToInt(m -> m.methodCount)
            .average().orElse(0.0);
    }
    
    private double calculateAverageFieldsPerClass() {
        return classMetrics.values().stream()
            .mapToInt(m -> m.fieldCount)
            .average().orElse(0.0);
    }
    
    private Map<String, Integer> calculateClassSizeDistribution() {
        Map<String, Integer> distribution = new HashMap<>();
        distribution.put("SMALL", 0);
        distribution.put("MEDIUM", 0);
        distribution.put("LARGE", 0);
        distribution.put("VERY_LARGE", 0);
        
        for (ClassMetrics metrics : classMetrics.values()) {
            if (metrics.estimatedLines <= 100) {
                distribution.merge("SMALL", 1, Integer::sum);
            } else if (metrics.estimatedLines <= 300) {
                distribution.merge("MEDIUM", 1, Integer::sum);
            } else if (metrics.estimatedLines <= 500) {
                distribution.merge("LARGE", 1, Integer::sum);
            } else {
                distribution.merge("VERY_LARGE", 1, Integer::sum);
            }
        }
        
        return distribution;
    }
    
    private Map<String, Integer> calculateMethodSizeDistribution() {
        Map<String, Integer> distribution = new HashMap<>();
        distribution.put("SHORT", 0);
        distribution.put("MEDIUM", 0);
        distribution.put("LONG", 0);
        distribution.put("VERY_LONG", 0);
        
        for (MethodMetrics metrics : methodMetrics.values()) {
            if (metrics.estimatedLines <= 10) {
                distribution.merge("SHORT", 1, Integer::sum);
            } else if (metrics.estimatedLines <= 30) {
                distribution.merge("MEDIUM", 1, Integer::sum);
            } else if (metrics.estimatedLines <= 60) {
                distribution.merge("LONG", 1, Integer::sum);
            } else {
                distribution.merge("VERY_LONG", 1, Integer::sum);
            }
        }
        
        return distribution;
    }
    
    private double calculateOverallMaintainabilityIndex() {
        return classMetrics.values().stream()
            .mapToDouble(m -> m.maintainabilityIndex)
            .average().orElse(0.0);
    }
    
    private int calculateCodeQualityScore() {
        int baseScore = 100;
        
        // Penalità per issues
        for (CodeIssue issue : codeIssues) {
            switch (issue.severity) {
                case CRITICAL: baseScore -= 15; break;
                case HIGH: baseScore -= 10; break;
                case MEDIUM: baseScore -= 5; break;
                case LOW: baseScore -= 2; break;
            }
        }
        
        // Penalità per metriche scadenti
        if (calculateAverageClassLength() > MAX_CLASS_LINES) baseScore -= 20;
        if (calculateAverageMethodLength() > MAX_METHOD_LINES) baseScore -= 15;
        
        // Bonus per good practices
        double maintainability = calculateOverallMaintainabilityIndex();
        if (maintainability > 80) baseScore += 10;
        else if (maintainability < 50) baseScore -= 15;
        
        return Math.max(0, Math.min(100, baseScore));
    }
    
    // Classi di supporto
    public static class ClassMetrics {
        public String className;
        public int methodCount;
        public int fieldCount;
        public int estimatedLines;
        public int staticFieldCount;
        public int finalFieldCount;
        public int publicFieldCount;
        public int annotationCount;
        public int implementedInterfacesCount;
        public int totalMethodLines;
        public int longestMethodLines;
        public String longestMethodName;
        public double averageMethodLines;
        public double complexityIndex;
        public double maintainabilityIndex;
        
        public boolean isInterface;
        public boolean isAbstract;
        public boolean isEnum;
        public boolean hasSuperClass;
        public String superClassName;
        public boolean isGodClass;
        
        public List<MethodMetrics> methods = new ArrayList<>();
        public List<FieldMetrics> fields = new ArrayList<>();
        
        public ClassMetrics(String className) {
            this.className = className;
        }
    }
    
    public static class MethodMetrics {
        public String className;
        public String methodName;
        public String methodId;
        public int estimatedLines;
        public int parameterCount;
        public String returnType;
        public int cyclomaticComplexity;
        public int methodCallCount;
        public int annotationCount;
        
        public boolean isPublic;
        public boolean isPrivate;
        public boolean isProtected;
        public boolean isStatic;
        public boolean isAbstract;
        public boolean isFinal;
        public boolean isGetter;
        public boolean isSetter;
        public boolean isConstructor;
        public boolean isMainMethod;
        public boolean hasOverrideAnnotation;
    }
    
    public static class FieldMetrics {
        public String fieldName;
        public String fieldType;
        
        public boolean isPublic;
        public boolean isPrivate;
        public boolean isProtected;
        public boolean isStatic;
        public boolean isFinal;
        public boolean isTransient;
        public boolean isVolatile;
    }
    
    public static class CodeIssue {
        public IssueType type;
        public String description;
        public String location;
        public IssueSeverity severity;
        
        public CodeIssue(IssueType type, String description, String location, IssueSeverity severity) {
            this.type = type;
            this.description = description;
            this.location = location;
            this.severity = severity;
        }
    }
    
    public enum IssueType {
        GOD_CLASS, CLASS_TOO_LONG, TOO_MANY_METHODS, TOO_MANY_FIELDS,
        METHOD_TOO_LONG, TOO_MANY_PARAMETERS, GENERAL_RECOMMENDATION
    }
    
    public enum IssueSeverity {
        LOW, MEDIUM, HIGH, CRITICAL
    }
    
    public static class LengthAnalysisReport {
        public int totalClasses;
        public int totalMethods;
        public Map<String, ClassMetrics> classMetrics;
        public Map<String, MethodMetrics> methodMetrics;
        public List<CodeIssue> codeIssues;
        public List<String> godClasses;
        public List<String> longMethods;
        
        public double averageClassLength;
        public double averageMethodLength;
        public double averageMethodsPerClass;
        public double averageFieldsPerClass;
        
        public Map<String, Integer> classSizeDistribution;
        public Map<String, Integer> methodSizeDistribution;
        
        public double overallMaintainabilityIndex;
        public int codeQualityScore;
    }
}
```

## Esempio di Output HTML

```html
<!DOCTYPE html>
<html>
<head>
    <title>Class and Method Length Analysis Report</title>
    <style>
        .metrics-container { margin: 20px 0; padding: 15px; border-left: 4px solid #28a745; }
        .god-class { border-left-color: #dc3545; background-color: #ffe6e6; }
        .large-class { border-left-color: #ffc107; background-color: #fff3cd; }
        .long-method { background-color: #f8d7da; margin: 10px 0; padding: 8px; }
        .size-chart { display: inline-block; width: 30%; margin: 10px; }
        .metric-value { font-size: 2em; font-weight: bold; color: #007bff; }
    </style>
</head>
<body>
    <h1>📏 Report: Lunghezza Media delle Classi e dei Metodi</h1>
    
    <div class="summary">
        <h2>📊 Metriche Generali</h2>
        <div style="display: flex; justify-content: space-around;">
            <div class="size-chart">
                <h3>Classi Analizzate</h3>
                <div class="metric-value">156</div>
            </div>
            <div class="size-chart">
                <h3>Metodi Analizzati</h3>
                <div class="metric-value">1,247</div>
            </div>
            <div class="size-chart">
                <h3>Score Qualità</h3>
                <div class="metric-value">78/100</div>
            </div>
        </div>
        
        <div class="averages">
            <h3>📈 Metriche Medie</h3>
            <ul>
                <li><strong>Lunghezza Media Classi:</strong> 187 righe</li>
                <li><strong>Lunghezza Media Metodi:</strong> 12 righe</li>
                <li><strong>Metodi per Classe:</strong> 8.2</li>
                <li><strong>Campi per Classe:</strong> 5.8</li>
                <li><strong>Indice Manutenibilità:</strong> 74.5/100</li>
            </ul>
        </div>
    </div>
    
    <div class="critical-issues">
        <h2>🔴 God Classes Rilevate</h2>
        
        <div class="metrics-container god-class">
            <h3>UserManagerService</h3>
            <div class="class-details">
                <p><strong>Righe Stimate:</strong> 847</p>
                <p><strong>Metodi:</strong> 34</p>
                <p><strong>Campi:</strong> 18</p>
                <p><strong>Problema:</strong> Classe monolitica con troppe responsabilità</p>
            </div>
            <div class="recommendations">
                <h4>🔧 Raccomandazioni</h4>
                <ul>
                    <li>Applica Single Responsibility Principle</li>
                    <li>Estrai UserAuthenticationService</li>
                    <li>Estrai UserProfileService</li>
                    <li>Usa pattern Strategy per operazioni diverse</li>
                </ul>
            </div>
        </div>
        
        <div class="metrics-container god-class">
            <h3>OrderProcessingController</h3>
            <div class="class-details">
                <p><strong>Righe Stimate:</strong> 623</p>
                <p><strong>Metodi:</strong> 28</p>
                <p><strong>Campi:</strong> 15</p>
            </div>
        </div>
    </div>
    
    <div class="size-distributions">
        <h2>📊 Distribuzione Dimensioni</h2>
        
        <div style="display: flex; justify-content: space-between;">
            <div class="size-chart">
                <h3>Dimensioni Classi</h3>
                <ul>
                    <li>🟢 <strong>Piccole (&lt;100):</strong> 45 classi</li>
                    <li>🟡 <strong>Medie (100-300):</strong> 89 classi</li>
                    <li>🟠 <strong>Grandi (300-500):</strong> 18 classi</li>
                    <li>🔴 <strong>Molto Grandi (&gt;500):</strong> 4 classi</li>
                </ul>
            </div>
            
            <div class="size-chart">
                <h3>Dimensioni Metodi</h3>
                <ul>
                    <li>🟢 <strong>Corti (&lt;10):</strong> 834 metodi</li>
                    <li>🟡 <strong>Medi (10-30):</strong> 356 metodi</li>
                    <li>🟠 <strong>Lunghi (30-60):</strong> 45 metodi</li>
                    <li>🔴 <strong>Molto Lunghi (&gt;60):</strong> 12 metodi</li>
                </ul>
            </div>
        </div>
    </div>
    
    <div class="long-methods">
        <h2>⚠️ Metodi Troppo Lunghi</h2>
        
        <div class="long-method">
            <h4>OrderService.processComplexOrder()</h4>
            <p><strong>Righe Stimate:</strong> 78 | <strong>Complessità Ciclomatica:</strong> 15</p>
            <p><strong>Suggerimento:</strong> Estrai submethods per validation, calculation, notification</p>
        </div>
        
        <div class="long-method">
            <h4>ReportGenerator.generateMonthlyReport()</h4>
            <p><strong>Righe Stimate:</strong> 65 | <strong>Parametri:</strong> 8</p>
            <p><strong>Suggerimenti:</strong> Usa pattern Builder per parametri, estrai formatting logic</p>
        </div>
    </div>
    
    <div class="quality-analysis">
        <h2>🎯 Analisi Qualità Codice</h2>
        <div class="quality-metrics">
            <h3>Distribuzione Issues</h3>
            <ul>
                <li>🔴 <strong>Critici:</strong> 4 (God Classes)</li>
                <li>🟠 <strong>Alti:</strong> 12 (Classi troppo lunghe)</li>
                <li>🟡 <strong>Medi:</strong> 28 (Metodi lunghi, troppi campi)</li>
                <li>🔵 <strong>Bassi:</strong> 15 (Raccomandazioni generali)</li>
            </ul>
        </div>
        
        <div class="maintainability">
            <h3>📈 Trend Manutenibilità</h3>
            <p><strong>Indice Complessivo:</strong> 74.5/100</p>
            <p><strong>Classi Altamente Manutenibili (&gt;80):</strong> 89 (57%)</p>
            <p><strong>Classi Difficili da Manutenere (&lt;50):</strong> 12 (8%)</p>
        </div>
    </div>
</body>
</html>
```

## Metriche di Qualità del Codice

### Algoritmo di Scoring (0-100)

```java
public class LengthQualityScorer {
    
    public int calculateQualityScore(LengthAnalysisReport report) {
        int baseScore = 100;
        
        // Penalità per God Classes
        baseScore -= report.godClasses.size() * 20;
        
        // Penalità per dimensioni eccessive
        if (report.averageClassLength > 300) baseScore -= 15;
        else if (report.averageClassLength > 200) baseScore -= 8;
        
        if (report.averageMethodLength > 50) baseScore -= 20;
        else if (report.averageMethodLength > 30) baseScore -= 10;
        
        // Penalità per issues per severità
        for (CodeIssue issue : report.codeIssues) {
            switch (issue.severity) {
                case CRITICAL: baseScore -= 15; break;
                case HIGH: baseScore -= 10; break;
                case MEDIUM: baseScore -= 5; break;
                case LOW: baseScore -= 2; break;
            }
        }
        
        // Penalità per distribuzione scadente
        int veryLargeClasses = report.classSizeDistribution.getOrDefault("VERY_LARGE", 0);
        int totalClasses = report.totalClasses;
        if (veryLargeClasses > totalClasses * 0.05) baseScore -= 15; // >5% very large
        
        // Bonus per good practices
        if (report.overallMaintainabilityIndex > 80) baseScore += 10;
        if (report.averageMethodsPerClass <= 15) baseScore += 5;
        
        return Math.max(0, Math.min(100, baseScore));
    }
    
    public String getQualityLevel(int score) {
        if (score >= 90) return "🟢 ECCELLENTE";
        if (score >= 75) return "🟡 BUONO";
        if (score >= 60) return "🟠 SUFFICIENTE";
        return "🔴 CRITICO";
    }
}
```

### Soglie di Valutazione

| Metrica | 🟢 Eccellente | 🟡 Buono | 🟠 Sufficiente | 🔴 Critico |
|---------|---------------|----------|----------------|-------------|
| **Score Complessivo** | 90-100 | 75-89 | 60-74 | 0-59 |
| **Lunghezza Media Classi** | <150 | 150-250 | 250-400 | >400 |
| **Lunghezza Media Metodi** | <15 | 15-25 | 25-40 | >40 |
| **God Classes** | 0 | 0 | 1-2 | >2 |
| **Indice Manutenibilità** | >80 | 65-80 | 50-64 | <50 |

### Segnalazioni per Gravità

#### 🔴 CRITICA
- **God Classes**: Classi >500 righe, >30 metodi, >20 campi
- **Metodi Giganti**: Metodi >100 righe con alta complessità ciclomatica
- **Indice Manutenibilità <30**: Codice estremamente difficile da manutenere
- **Classi Monolitiche**: Singola classe >1000 righe

#### 🟠 ALTA
- **Classi Molto Lunghe**: >400 righe senza essere God Classes
- **Metodi Molto Lunghi**: >60 righe o >15 parametri
- **Troppe Responsabilità**: >25 metodi pubblici in una classe
- **Complessità Strutturale**: Classi con >20 campi o >15 dipendenze

#### 🟡 MEDIA
- **Classi Lunghe**: 250-400 righe
- **Metodi Lunghi**: 30-60 righe
- **Troppe Proprietà**: >15 campi in classe non-entity
- **Parametri Eccessivi**: Metodi con >5 parametri

#### 🔵 BASSA
- **Convenzioni Naming**: Classi/metodi con nomi non espressivi
- **Mancanza Documentazione**: Metodi complessi senza Javadoc
- **Getter/Setter Eccessivi**: Classi con >60% getter/setter
- **Classi Utility Grosse**: Utility classes con >150 righe

### Valore di Business

- **Manutenibilità Codice**: Codice più piccolo e focalizzato è più facile da manutenere
- **Velocity di Sviluppo**: Metodi/classi piccoli accelerano feature development
- **Riduzione Bug**: Codice più semplice ha meno probabilità di bug
- **Onboarding Team**: Nuovo sviluppatori capiscono codice più facilmente
- **Technical Debt**: Prevenzione accumulo debito tecnico da refactoring costosi