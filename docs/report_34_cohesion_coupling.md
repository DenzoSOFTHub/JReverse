# Report 34: Analisi di Coesione e Accoppiamento

**Valore**: ⭐⭐⭐⭐ **Complessità**: 🔴 Complessa **Tempo**: 8-10 giorni
**Tags**: `#cohesion` `#coupling` `#architecture-metrics` `#design-quality`

## Descrizione

Questo report analizza la coesione interna delle classi e l'accoppiamento tra classi/moduli per valutare la qualità dell'architettura, identificando classi con bassa coesione, alto accoppiamento e fornendo metriche per migliorare il design secondo i principi SOLID.

## Obiettivi dell'Analisi

1. **Cohesion Analysis**: Misurazione coesione interna delle classi (LCOM metrics)
2. **Coupling Analysis**: Analisi accoppiamento tra classi (CBO, Ce, Ca)
3. **Instability Metrics**: Calcolo instabilità e astrattezza moduli
4. **Design Quality**: Valutazione qualità design secondo principi OOP
5. **Architecture Assessment**: Identificazione violazioni principi architetturali

## Implementazione con Javassist

```java
package com.jreverse.analyzer.cohesion;

import javassist.*;
import javassist.bytecode.*;
import javassist.expr.*;
import java.util.*;
import java.util.stream.Collectors;
import java.io.IOException;

/**
 * Analyzer per metriche di coesione e accoppiamento
 */
public class CohesionCouplingAnalyzer {
    
    private ClassPool classPool;
    private Map<String, ClassCohesionMetrics> cohesionMetrics;
    private Map<String, ClassCouplingMetrics> couplingMetrics;
    private Map<String, Set<String>> classDependencies;
    private Map<String, Set<String>> classClients;
    private List<DesignIssue> designIssues;
    private Map<String, Double> instabilityScores;
    private Map<String, Double> abstractnessScores;
    
    // Soglie per identificare problemi
    private static final double HIGH_COUPLING_THRESHOLD = 10.0;
    private static final double LOW_COHESION_THRESHOLD = 0.3;
    private static final double HIGH_INSTABILITY_THRESHOLD = 0.8;
    private static final double LOW_ABSTRACTNESS_THRESHOLD = 0.2;
    
    public CohesionCouplingAnalyzer() {
        this.classPool = ClassPool.getDefault();
        this.cohesionMetrics = new HashMap<>();
        this.couplingMetrics = new HashMap<>();
        this.classDependencies = new HashMap<>();
        this.classClients = new HashMap<>();
        this.designIssues = new ArrayList<>();
        this.instabilityScores = new HashMap<>();
        this.abstractnessScores = new HashMap<>();
    }
    
    /**
     * Analizza coesione e accoppiamento nell'applicazione
     */
    public CohesionCouplingReport analyzeCohesionCoupling(List<String> classNames) throws Exception {
        // Fase 1: Prima passata - raccogli informazioni base
        for (String className : classNames) {
            try {
                CtClass ctClass = classPool.get(className);
                if (isAnalyzableClass(ctClass)) {
                    collectBasicClassInfo(ctClass);
                }
            } catch (Exception e) {
                System.err.println("Errore nella prima passata: " + className + ": " + e.getMessage());
            }
        }
        
        // Fase 2: Seconda passata - analizza coesione
        for (String className : classNames) {
            try {
                CtClass ctClass = classPool.get(className);
                if (isAnalyzableClass(ctClass)) {
                    analyzeCohesion(ctClass);
                }
            } catch (Exception e) {
                System.err.println("Errore nell'analisi coesione: " + className + ": " + e.getMessage());
            }
        }
        
        // Fase 3: Terza passata - analizza accoppiamento
        for (String className : classNames) {
            try {
                CtClass ctClass = classPool.get(className);
                if (isAnalyzableClass(ctClass)) {
                    analyzeCoupling(ctClass);
                }
            } catch (Exception e) {
                System.err.println("Errore nell'analisi accoppiamento: " + className + ": " + e.getMessage());
            }
        }
        
        // Fase 4: Calcola metriche derivate
        calculateDerivedMetrics();
        
        // Fase 5: Identifica problemi di design
        identifyDesignIssues();
        
        return generateReport();
    }
    
    /**
     * Verifica se la classe è analizzabile
     */
    private boolean isAnalyzableClass(CtClass ctClass) throws Exception {
        String className = ctClass.getName();
        return !className.startsWith("java.") &&
               !className.startsWith("javax.") &&
               !className.startsWith("org.springframework.") &&
               !className.contains("$$") &&
               !className.endsWith("Test");
    }
    
    /**
     * Raccoglie informazioni base della classe
     */
    private void collectBasicClassInfo(CtClass ctClass) throws Exception {
        String className = ctClass.getName();
        
        // Inizializza strutture dati
        cohesionMetrics.put(className, new ClassCohesionMetrics(className));
        couplingMetrics.put(className, new ClassCouplingMetrics(className));
        classDependencies.put(className, new HashSet<>());
        classClients.put(className, new HashSet<>());
        
        // Calcola abstractness
        calculateAbstractness(ctClass);
    }
    
    /**
     * Calcola abstractness della classe
     */
    private void calculateAbstractness(CtClass ctClass) throws Exception {
        String className = ctClass.getName();
        
        int totalMethods = ctClass.getDeclaredMethods().length;
        int abstractMethods = 0;
        
        if (ctClass.isInterface()) {
            abstractnessScores.put(className, 1.0);
            return;
        }
        
        for (CtMethod method : ctClass.getDeclaredMethods()) {
            if (Modifier.isAbstract(method.getModifiers())) {
                abstractMethods++;
            }
        }
        
        double abstractness = totalMethods > 0 ? (double) abstractMethods / totalMethods : 0.0;
        abstractnessScores.put(className, abstractness);
    }
    
    /**
     * Analizza coesione della classe
     */
    private void analyzeCohesion(CtClass ctClass) throws Exception {
        String className = ctClass.getName();
        ClassCohesionMetrics metrics = cohesionMetrics.get(className);
        
        if (metrics == null) return;
        
        // Raccoglie informazioni su campi e metodi
        Set<String> fields = collectClassFields(ctClass);
        Map<String, Set<String>> methodFieldUsage = collectMethodFieldUsage(ctClass);
        
        metrics.totalFields = fields.size();
        metrics.totalMethods = methodFieldUsage.size();
        
        // Calcola LCOM (Lack of Cohesion of Methods) - versione 1
        metrics.lcom1 = calculateLCOM1(methodFieldUsage, fields);
        
        // Calcola LCOM2 (versione più sofisticata)
        metrics.lcom2 = calculateLCOM2(methodFieldUsage);
        
        // Calcola Tight Class Cohesion (TCC)
        metrics.tcc = calculateTCC(ctClass, methodFieldUsage);
        
        // Calcola Cohesion Among Methods (CAM)
        metrics.cam = calculateCAM(ctClass);
        
        // Valuta coesione complessiva
        metrics.overallCohesion = calculateOverallCohesion(metrics);
        
        // Identifica metodi con bassa coesione
        identifyLowCohesionMethods(ctClass, methodFieldUsage, metrics);
    }
    
    /**
     * Raccoglie campi della classe
     */
    private Set<String> collectClassFields(CtClass ctClass) throws Exception {
        Set<String> fields = new HashSet<>();
        
        for (CtField field : ctClass.getDeclaredFields()) {
            // Esclude campi statici e sintetici
            if (!Modifier.isStatic(field.getModifiers()) && 
                !field.getName().contains("$")) {
                fields.add(field.getName());
            }
        }
        
        return fields;
    }
    
    /**
     * Raccoglie utilizzo dei campi da parte dei metodi
     */
    private Map<String, Set<String>> collectMethodFieldUsage(CtClass ctClass) throws Exception {
        Map<String, Set<String>> methodFieldUsage = new HashMap<>();
        
        for (CtMethod method : ctClass.getDeclaredMethods()) {
            if (Modifier.isStatic(method.getModifiers()) || 
                method.getName().startsWith("get") || 
                method.getName().startsWith("set")) {
                continue; // Skip getters, setters e metodi statici
            }
            
            Set<String> usedFields = new HashSet<>();
            
            try {
                method.instrument(new ExprEditor() {
                    @Override
                    public void edit(FieldAccess f) throws CannotCompileException {
                        try {
                            if (f.getClassName().equals(ctClass.getName())) {
                                usedFields.add(f.getFieldName());
                            }
                        } catch (Exception e) {
                            // Continua analisi
                        }
                    }
                });
            } catch (Exception e) {
                // Metodo potrebbe essere astratto o nativo
            }
            
            methodFieldUsage.put(method.getName(), usedFields);
        }
        
        return methodFieldUsage;
    }
    
    /**
     * Calcola LCOM1 (Lack of Cohesion of Methods)
     */
    private double calculateLCOM1(Map<String, Set<String>> methodFieldUsage, Set<String> allFields) {
        if (methodFieldUsage.isEmpty() || allFields.isEmpty()) return 0.0;
        
        int pairsWithoutSharedFields = 0;
        int pairsWithSharedFields = 0;
        
        List<String> methods = new ArrayList<>(methodFieldUsage.keySet());
        
        for (int i = 0; i < methods.size(); i++) {
            for (int j = i + 1; j < methods.size(); j++) {
                Set<String> fields1 = methodFieldUsage.get(methods.get(i));
                Set<String> fields2 = methodFieldUsage.get(methods.get(j));
                
                Set<String> intersection = new HashSet<>(fields1);
                intersection.retainAll(fields2);
                
                if (intersection.isEmpty()) {
                    pairsWithoutSharedFields++;
                } else {
                    pairsWithSharedFields++;
                }
            }
        }
        
        // LCOM1 = max(0, P - Q) dove P = coppie senza campi condivisi, Q = coppie con campi condivisi
        return Math.max(0, pairsWithoutSharedFields - pairsWithSharedFields);
    }
    
    /**
     * Calcola LCOM2 (versione normalizzata)
     */
    private double calculateLCOM2(Map<String, Set<String>> methodFieldUsage) {
        if (methodFieldUsage.isEmpty()) return 0.0;
        
        List<String> methods = new ArrayList<>(methodFieldUsage.keySet());
        int totalPairs = methods.size() * (methods.size() - 1) / 2;
        
        if (totalPairs == 0) return 0.0;
        
        int connectedPairs = 0;
        
        for (int i = 0; i < methods.size(); i++) {
            for (int j = i + 1; j < methods.size(); j++) {
                Set<String> fields1 = methodFieldUsage.get(methods.get(i));
                Set<String> fields2 = methodFieldUsage.get(methods.get(j));
                
                Set<String> intersection = new HashSet<>(fields1);
                intersection.retainAll(fields2);
                
                if (!intersection.isEmpty()) {
                    connectedPairs++;
                }
            }
        }
        
        // LCOM2 = 1 - (connectedPairs / totalPairs)
        return 1.0 - ((double) connectedPairs / totalPairs);
    }
    
    /**
     * Calcola Tight Class Cohesion (TCC)
     */
    private double calculateTCC(CtClass ctClass, Map<String, Set<String>> methodFieldUsage) throws Exception {
        // TCC misura le connessioni dirette tra metodi tramite uso di campi comuni
        int directConnections = 0;
        int totalPossibleConnections = 0;
        
        List<String> methods = new ArrayList<>(methodFieldUsage.keySet());
        
        for (int i = 0; i < methods.size(); i++) {
            for (int j = i + 1; j < methods.size(); j++) {
                totalPossibleConnections++;
                
                Set<String> fields1 = methodFieldUsage.get(methods.get(i));
                Set<String> fields2 = methodFieldUsage.get(methods.get(j));
                
                Set<String> intersection = new HashSet<>(fields1);
                intersection.retainAll(fields2);
                
                if (!intersection.isEmpty()) {
                    directConnections++;
                }
            }
        }
        
        return totalPossibleConnections > 0 ? (double) directConnections / totalPossibleConnections : 0.0;
    }
    
    /**
     * Calcola Cohesion Among Methods (CAM)
     */
    private double calculateCAM(CtClass ctClass) throws Exception {
        // CAM basato su similarità tra metodi in termini di tipi di parametri e chiamate
        CtMethod[] methods = ctClass.getDeclaredMethods();
        
        if (methods.length <= 1) return 1.0;
        
        double totalSimilarity = 0.0;
        int comparisons = 0;
        
        for (int i = 0; i < methods.length; i++) {
            for (int j = i + 1; j < methods.length; j++) {
                double similarity = calculateMethodSimilarity(methods[i], methods[j]);
                totalSimilarity += similarity;
                comparisons++;
            }
        }
        
        return comparisons > 0 ? totalSimilarity / comparisons : 0.0;
    }
    
    /**
     * Calcola similarità tra due metodi
     */
    private double calculateMethodSimilarity(CtMethod method1, CtMethod method2) throws Exception {
        double similarity = 0.0;
        
        // Similarità basata sui tipi di parametri
        CtClass[] params1 = method1.getParameterTypes();
        CtClass[] params2 = method2.getParameterTypes();
        
        Set<String> paramTypes1 = Arrays.stream(params1)
            .map(CtClass::getName).collect(Collectors.toSet());
        Set<String> paramTypes2 = Arrays.stream(params2)
            .map(CtClass::getName).collect(Collectors.toSet());
        
        Set<String> intersection = new HashSet<>(paramTypes1);
        intersection.retainAll(paramTypes2);
        
        Set<String> union = new HashSet<>(paramTypes1);
        union.addAll(paramTypes2);
        
        if (!union.isEmpty()) {
            similarity += 0.5 * (double) intersection.size() / union.size();
        }
        
        // Similarità basata sul tipo di return
        try {
            if (method1.getReturnType().getName().equals(method2.getReturnType().getName())) {
                similarity += 0.3;
            }
        } catch (Exception e) {
            // Ignore
        }
        
        // Similarità basata sui modifier
        if ((method1.getModifiers() & 0x7) == (method2.getModifiers() & 0x7)) {
            similarity += 0.2;
        }
        
        return Math.min(1.0, similarity);
    }
    
    /**
     * Calcola coesione complessiva
     */
    private double calculateOverallCohesion(ClassCohesionMetrics metrics) {
        // Combina diverse metriche di coesione
        double cohesion = 0.0;
        int weights = 0;
        
        if (metrics.lcom2 >= 0) {
            cohesion += (1.0 - metrics.lcom2) * 0.4; // Inverti LCOM2
            weights += 4;
        }
        
        if (metrics.tcc >= 0) {
            cohesion += metrics.tcc * 0.4;
            weights += 4;
        }
        
        if (metrics.cam >= 0) {
            cohesion += metrics.cam * 0.2;
            weights += 2;
        }
        
        return weights > 0 ? cohesion * 10 / weights : 0.0;
    }
    
    /**
     * Identifica metodi con bassa coesione
     */
    private void identifyLowCohesionMethods(CtClass ctClass, 
                                          Map<String, Set<String>> methodFieldUsage,
                                          ClassCohesionMetrics metrics) {
        
        for (Map.Entry<String, Set<String>> entry : methodFieldUsage.entrySet()) {
            String methodName = entry.getKey();
            Set<String> usedFields = entry.getValue();
            
            // Metodo che usa pochi campi può indicare bassa coesione
            double fieldUsageRatio = metrics.totalFields > 0 ? 
                (double) usedFields.size() / metrics.totalFields : 0.0;
            
            if (fieldUsageRatio < 0.2 && usedFields.size() > 0) {
                metrics.lowCohesionMethods.add(methodName);
            }
        }
    }
    
    /**
     * Analizza accoppiamento della classe
     */
    private void analyzeCoupling(CtClass ctClass) throws Exception {
        String className = ctClass.getName();
        ClassCouplingMetrics metrics = couplingMetrics.get(className);
        
        if (metrics == null) return;
        
        // Analizza dipendenze in uscita (Efferent Coupling - Ce)
        Set<String> efferentDependencies = analyzeEfferentCoupling(ctClass);
        metrics.efferentCoupling = efferentDependencies.size();
        classDependencies.put(className, efferentDependencies);
        
        // Registra dipendenze inverse per calcolare Ce successivamente
        for (String dependency : efferentDependencies) {
            classClients.computeIfAbsent(dependency, k -> new HashSet<>()).add(className);
        }
        
        // Calcola CBO (Coupling Between Objects)
        metrics.couplingBetweenObjects = calculateCBO(ctClass, efferentDependencies);
        
        // Analizza accoppiamento dei dati
        metrics.dataCoupling = analyzeDataCoupling(ctClass);
        
        // Analizza accoppiamento di controllo
        metrics.controlCoupling = analyzeControlCoupling(ctClass);
    }
    
    /**
     * Analizza accoppiamento in uscita (efferent)
     */
    private Set<String> analyzeEfferentCoupling(CtClass ctClass) throws Exception {
        Set<String> dependencies = new HashSet<>();
        String className = ctClass.getName();
        
        // Analizza campi
        for (CtField field : ctClass.getDeclaredFields()) {
            String fieldType = field.getType().getName();
            if (isApplicationClass(fieldType) && !fieldType.equals(className)) {
                dependencies.add(fieldType);
            }
        }
        
        // Analizza metodi
        for (CtMethod method : ctClass.getDeclaredMethods()) {
            // Parametri
            for (CtClass paramType : method.getParameterTypes()) {
                String paramTypeName = paramType.getName();
                if (isApplicationClass(paramTypeName) && !paramTypeName.equals(className)) {
                    dependencies.add(paramTypeName);
                }
            }
            
            // Return type
            try {
                String returnType = method.getReturnType().getName();
                if (isApplicationClass(returnType) && !returnType.equals(className)) {
                    dependencies.add(returnType);
                }
            } catch (Exception e) {
                // Ignore
            }
            
            // Chiamate a metodi
            analyzeDependenciesInMethod(method, dependencies, className);
        }
        
        // Analizza costruttori
        for (CtConstructor constructor : ctClass.getDeclaredConstructors()) {
            for (CtClass paramType : constructor.getParameterTypes()) {
                String paramTypeName = paramType.getName();
                if (isApplicationClass(paramTypeName) && !paramTypeName.equals(className)) {
                    dependencies.add(paramTypeName);
                }
            }
            analyzeDependenciesInConstructor(constructor, dependencies, className);
        }
        
        return dependencies;
    }
    
    /**
     * Analizza dipendenze in un metodo
     */
    private void analyzeDependenciesInMethod(CtMethod method, Set<String> dependencies, String className) {
        try {
            method.instrument(new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    try {
                        String calledClass = m.getClassName();
                        if (isApplicationClass(calledClass) && !calledClass.equals(className)) {
                            dependencies.add(calledClass);
                        }
                    } catch (Exception e) {
                        // Continue analysis
                    }
                }
                
                @Override
                public void edit(NewExpr e) throws CannotCompileException {
                    try {
                        String createdClass = e.getClassName();
                        if (isApplicationClass(createdClass) && !createdClass.equals(className)) {
                            dependencies.add(createdClass);
                        }
                    } catch (Exception ex) {
                        // Continue analysis
                    }
                }
                
                @Override
                public void edit(Cast c) throws CannotCompileException {
                    try {
                        String castType = c.getType().getName();
                        if (isApplicationClass(castType) && !castType.equals(className)) {
                            dependencies.add(castType);
                        }
                    } catch (Exception ex) {
                        // Continue analysis
                    }
                }
            });
        } catch (Exception e) {
            // Metodo potrebbe essere astratto
        }
    }
    
    /**
     * Analizza dipendenze in un costruttore
     */
    private void analyzeDependenciesInConstructor(CtConstructor constructor, 
                                                Set<String> dependencies, String className) {
        try {
            constructor.instrument(new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    try {
                        String calledClass = m.getClassName();
                        if (isApplicationClass(calledClass) && !calledClass.equals(className)) {
                            dependencies.add(calledClass);
                        }
                    } catch (Exception e) {
                        // Continue analysis
                    }
                }
                
                @Override
                public void edit(NewExpr e) throws CannotCompileException {
                    try {
                        String createdClass = e.getClassName();
                        if (isApplicationClass(createdClass) && !createdClass.equals(className)) {
                            dependencies.add(createdClass);
                        }
                    } catch (Exception ex) {
                        // Continue analysis
                    }
                }
            });
        } catch (Exception e) {
            // Continue analysis
        }
    }
    
    /**
     * Verifica se è classe dell'applicazione
     */
    private boolean isApplicationClass(String className) {
        return !className.startsWith("java.") &&
               !className.startsWith("javax.") &&
               !className.startsWith("org.springframework.") &&
               !className.contains("$") &&
               !className.equals("void") &&
               !isPrimitiveType(className);
    }
    
    /**
     * Verifica se è tipo primitivo
     */
    private boolean isPrimitiveType(String typeName) {
        return Arrays.asList("boolean", "byte", "char", "short", "int", 
                           "long", "float", "double").contains(typeName);
    }
    
    /**
     * Calcola CBO (Coupling Between Objects)
     */
    private double calculateCBO(CtClass ctClass, Set<String> efferentDependencies) throws Exception {
        // CBO è il numero di classi a cui questa classe è accoppiata
        return efferentDependencies.size();
    }
    
    /**
     * Analizza accoppiamento dei dati
     */
    private double analyzeDataCoupling(CtClass ctClass) throws Exception {
        int dataCouplingCount = 0;
        
        // Conta parametri di tipo non primitivo
        for (CtMethod method : ctClass.getDeclaredMethods()) {
            for (CtClass paramType : method.getParameterTypes()) {
                if (isApplicationClass(paramType.getName())) {
                    dataCouplingCount++;
                }
            }
        }
        
        return dataCouplingCount;
    }
    
    /**
     * Analizza accoppiamento di controllo
     */
    private double analyzeControlCoupling(CtClass ctClass) throws Exception {
        int controlCouplingCount = 0;
        
        // Semplificato: conta metodi che ricevono boolean/enum come parametri di controllo
        for (CtMethod method : ctClass.getDeclaredMethods()) {
            for (CtClass paramType : method.getParameterTypes()) {
                String typeName = paramType.getName();
                if (typeName.equals("boolean") || typeName.equals("java.lang.Boolean")) {
                    controlCouplingCount++;
                }
                try {
                    if (paramType.isEnum()) {
                        controlCouplingCount++;
                    }
                } catch (Exception e) {
                    // Continue
                }
            }
        }
        
        return controlCouplingCount;
    }
    
    /**
     * Calcola metriche derivate
     */
    private void calculateDerivedMetrics() {
        // Calcola Afferent Coupling (Ca) per ogni classe
        for (String className : couplingMetrics.keySet()) {
            ClassCouplingMetrics metrics = couplingMetrics.get(className);
            Set<String> clients = classClients.getOrDefault(className, new HashSet<>());
            metrics.afferentCoupling = clients.size();
            
            // Calcola Instability (I = Ce / (Ce + Ca))
            double ce = metrics.efferentCoupling;
            double ca = metrics.afferentCoupling;
            double instability = (ce + ca) > 0 ? ce / (ce + ca) : 0.0;
            
            instabilityScores.put(className, instability);
            metrics.instability = instability;
            
            // Calcola Distance from Main Sequence (D = |A + I - 1|)
            double abstractness = abstractnessScores.getOrDefault(className, 0.0);
            double distance = Math.abs(abstractness + instability - 1.0);
            metrics.distanceFromMainSequence = distance;
        }
    }
    
    /**
     * Identifica problemi di design
     */
    private void identifyDesignIssues() {
        for (String className : cohesionMetrics.keySet()) {
            ClassCohesionMetrics cohesion = cohesionMetrics.get(className);
            ClassCouplingMetrics coupling = couplingMetrics.get(className);
            
            // Bassa coesione
            if (cohesion.overallCohesion < LOW_COHESION_THRESHOLD) {
                designIssues.add(new DesignIssue(
                    DesignIssueType.LOW_COHESION,
                    "Bassa coesione interna: " + String.format("%.2f", cohesion.overallCohesion),
                    className,
                    IssueSeverity.MEDIUM
                ));
            }
            
            // Alto accoppiamento
            if (coupling.couplingBetweenObjects > HIGH_COUPLING_THRESHOLD) {
                designIssues.add(new DesignIssue(
                    DesignIssueType.HIGH_COUPLING,
                    "Alto accoppiamento: " + coupling.couplingBetweenObjects + " dipendenze",
                    className,
                    IssueSeverity.HIGH
                ));
            }
            
            // Alta instabilità
            if (coupling.instability > HIGH_INSTABILITY_THRESHOLD) {
                designIssues.add(new DesignIssue(
                    DesignIssueType.HIGH_INSTABILITY,
                    "Alta instabilità: " + String.format("%.2f", coupling.instability),
                    className,
                    IssueSeverity.MEDIUM
                ));
            }
            
            // Violazione Main Sequence
            if (coupling.distanceFromMainSequence > 0.7) {
                designIssues.add(new DesignIssue(
                    DesignIssueType.MAIN_SEQUENCE_VIOLATION,
                    "Lontano dalla Main Sequence: " + String.format("%.2f", coupling.distanceFromMainSequence),
                    className,
                    IssueSeverity.MEDIUM
                ));
            }
            
            // Zone of Pain (alta stabilità, bassa astrattezza)
            double abstractness = abstractnessScores.getOrDefault(className, 0.0);
            if (coupling.instability < 0.2 && abstractness < LOW_ABSTRACTNESS_THRESHOLD) {
                designIssues.add(new DesignIssue(
                    DesignIssueType.ZONE_OF_PAIN,
                    "Zone of Pain: classe concreta molto stabile",
                    className,
                    IssueSeverity.HIGH
                ));
            }
            
            // Zone of Uselessness (bassa stabilità, alta astrattezza)
            if (coupling.instability > 0.8 && abstractness > 0.8) {
                designIssues.add(new DesignIssue(
                    DesignIssueType.ZONE_OF_USELESSNESS,
                    "Zone of Uselessness: classe astratta molto instabile",
                    className,
                    IssueSeverity.LOW
                ));
            }
        }
    }
    
    /**
     * Genera report finale
     */
    private CohesionCouplingReport generateReport() {
        CohesionCouplingReport report = new CohesionCouplingReport();
        
        report.totalClasses = cohesionMetrics.size();
        report.cohesionMetrics = new HashMap<>(cohesionMetrics);
        report.couplingMetrics = new HashMap<>(couplingMetrics);
        report.designIssues = new ArrayList<>(designIssues);
        report.instabilityScores = new HashMap<>(instabilityScores);
        report.abstractnessScores = new HashMap<>(abstractnessScores);
        
        // Calcola statistiche aggregate
        report.averageCohesion = calculateAverageCohesion();
        report.averageCoupling = calculateAverageCoupling();
        report.averageInstability = calculateAverageInstability();
        report.averageAbstractness = calculateAverageAbstractness();
        
        // Distribuzione problemi
        report.issuesByType = groupIssuesByType();
        report.issuesBySeverity = groupIssuesBySeverity();
        
        // Score qualità design
        report.designQualityScore = calculateDesignQualityScore();
        
        return report;
    }
    
    private double calculateAverageCohesion() {
        return cohesionMetrics.values().stream()
            .mapToDouble(m -> m.overallCohesion)
            .average().orElse(0.0);
    }
    
    private double calculateAverageCoupling() {
        return couplingMetrics.values().stream()
            .mapToDouble(m -> m.couplingBetweenObjects)
            .average().orElse(0.0);
    }
    
    private double calculateAverageInstability() {
        return instabilityScores.values().stream()
            .mapToDouble(Double::doubleValue)
            .average().orElse(0.0);
    }
    
    private double calculateAverageAbstractness() {
        return abstractnessScores.values().stream()
            .mapToDouble(Double::doubleValue)
            .average().orElse(0.0);
    }
    
    private Map<DesignIssueType, Integer> groupIssuesByType() {
        Map<DesignIssueType, Integer> grouped = new HashMap<>();
        for (DesignIssue issue : designIssues) {
            grouped.merge(issue.type, 1, Integer::sum);
        }
        return grouped;
    }
    
    private Map<IssueSeverity, Integer> groupIssuesBySeverity() {
        Map<IssueSeverity, Integer> grouped = new HashMap<>();
        for (DesignIssue issue : designIssues) {
            grouped.merge(issue.severity, 1, Integer::sum);
        }
        return grouped;
    }
    
    private int calculateDesignQualityScore() {
        int baseScore = 100;
        
        // Penalità per issues
        for (DesignIssue issue : designIssues) {
            switch (issue.severity) {
                case CRITICAL: baseScore -= 20; break;
                case HIGH: baseScore -= 15; break;
                case MEDIUM: baseScore -= 8; break;
                case LOW: baseScore -= 3; break;
            }
        }
        
        // Penalità per metriche scadenti
        if (calculateAverageCohesion() < 0.5) baseScore -= 20;
        if (calculateAverageCoupling() > 15) baseScore -= 15;
        
        // Bonus per good design
        if (calculateAverageCohesion() > 0.8) baseScore += 10;
        if (calculateAverageCoupling() < 5) baseScore += 5;
        
        return Math.max(0, Math.min(100, baseScore));
    }
    
    // Classi di supporto
    public static class ClassCohesionMetrics {
        public String className;
        public int totalFields;
        public int totalMethods;
        public double lcom1;
        public double lcom2;
        public double tcc;
        public double cam;
        public double overallCohesion;
        public List<String> lowCohesionMethods = new ArrayList<>();
        
        public ClassCohesionMetrics(String className) {
            this.className = className;
        }
    }
    
    public static class ClassCouplingMetrics {
        public String className;
        public double efferentCoupling; // Ce - outgoing dependencies
        public double afferentCoupling; // Ca - incoming dependencies
        public double couplingBetweenObjects; // CBO
        public double instability; // I = Ce / (Ce + Ca)
        public double distanceFromMainSequence; // D = |A + I - 1|
        public double dataCoupling;
        public double controlCoupling;
        
        public ClassCouplingMetrics(String className) {
            this.className = className;
        }
    }
    
    public static class DesignIssue {
        public DesignIssueType type;
        public String description;
        public String className;
        public IssueSeverity severity;
        
        public DesignIssue(DesignIssueType type, String description, String className, IssueSeverity severity) {
            this.type = type;
            this.description = description;
            this.className = className;
            this.severity = severity;
        }
    }
    
    public enum DesignIssueType {
        LOW_COHESION, HIGH_COUPLING, HIGH_INSTABILITY, MAIN_SEQUENCE_VIOLATION,
        ZONE_OF_PAIN, ZONE_OF_USELESSNESS
    }
    
    public enum IssueSeverity {
        LOW, MEDIUM, HIGH, CRITICAL
    }
    
    public static class CohesionCouplingReport {
        public int totalClasses;
        public Map<String, ClassCohesionMetrics> cohesionMetrics;
        public Map<String, ClassCouplingMetrics> couplingMetrics;
        public List<DesignIssue> designIssues;
        public Map<String, Double> instabilityScores;
        public Map<String, Double> abstractnessScores;
        
        public double averageCohesion;
        public double averageCoupling;
        public double averageInstability;
        public double averageAbstractness;
        
        public Map<DesignIssueType, Integer> issuesByType;
        public Map<IssueSeverity, Integer> issuesBySeverity;
        
        public int designQualityScore;
    }
}
```

## Esempio di Output HTML

```html
<!DOCTYPE html>
<html>
<head>
    <title>Cohesion and Coupling Analysis Report</title>
    <style>
        .class-container { margin: 20px 0; padding: 15px; border-left: 4px solid #28a745; }
        .low-cohesion { border-left-color: #ffc107; background-color: #fff3cd; }
        .high-coupling { border-left-color: #dc3545; background-color: #ffe6e6; }
        .zone-pain { border-left-color: #dc3545; background-color: #ffe6e6; }
        .metrics-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; margin: 20px 0; }
        .metric-card { padding: 15px; background: #f8f9fa; border-radius: 5px; text-align: center; }
        .score { font-size: 2em; font-weight: bold; color: #007bff; }
    </style>
</head>
<body>
    <h1>🔗 Report: Analisi di Coesione e Accoppiamento</h1>
    
    <div class="summary">
        <h2>📊 Metriche Generali</h2>
        <div class="metrics-grid">
            <div class="metric-card">
                <h3>Classi Analizzate</h3>
                <div class="score">142</div>
            </div>
            <div class="metric-card">
                <h3>Score Design Quality</h3>
                <div class="score">73/100</div>
            </div>
            <div class="metric-card">
                <h3>Issues Rilevati</h3>
                <div class="score">28</div>
            </div>
        </div>
        
        <div class="averages">
            <h3>📈 Metriche Medie</h3>
            <ul>
                <li><strong>Coesione Media:</strong> 0.67</li>
                <li><strong>Accoppiamento Medio:</strong> 8.3</li>
                <li><strong>Instabilità Media:</strong> 0.45</li>
                <li><strong>Astrattezza Media:</strong> 0.21</li>
            </ul>
        </div>
    </div>
    
    <div class="critical-issues">
        <h2>🔴 Zone of Pain Rilevate</h2>
        
        <div class="class-container zone-pain">
            <h3>DatabaseManager</h3>
            <div class="metrics-details">
                <p><strong>Problema:</strong> Classe concreta molto stabile</p>
                <p><strong>Instabilità:</strong> 0.15 (molto bassa)</p>
                <p><strong>Astrattezza:</strong> 0.05 (molto bassa)</p>
                <p><strong>Distanza Main Sequence:</strong> 0.80</p>
            </div>
            <div class="recommendations">
                <h4>🔧 Raccomandazioni</h4>
                <ul>
                    <li>Introduci interfacce per aumentare astrattezza</li>
                    <li>Applica Dependency Inversion Principle</li>
                    <li>Considera pattern Strategy per variazioni</li>
                </ul>
            </div>
        </div>
    </div>
    
    <div class="cohesion-issues">
        <h2>⚠️ Problemi di Coesione</h2>
        
        <div class="class-container low-cohesion">
            <h3>UserService</h3>
            <div class="cohesion-metrics">
                <p><strong>Coesione Complessiva:</strong> 0.28</p>
                <p><strong>LCOM2:</strong> 0.75 (alta mancanza coesione)</p>
                <p><strong>TCC:</strong> 0.15 (bassa coesione)</p>
                <p><strong>Metodi Problematici:</strong> calculateDiscount, sendEmail, logActivity</p>
            </div>
            <div class="suggestions">
                <h4>💡 Suggerimenti</h4>
                <ul>
                    <li>Estrai UserNotificationService per sendEmail</li>
                    <li>Estrai DiscountCalculator per calculateDiscount</li>
                    <li>Estrai AuditService per logActivity</li>
                </ul>
            </div>
        </div>
    </div>
    
    <div class="coupling-issues">
        <h2>🔗 Problemi di Accoppiamento</h2>
        
        <div class="class-container high-coupling">
            <h3>OrderController</h3>
            <div class="coupling-metrics">
                <p><strong>CBO (Coupling Between Objects):</strong> 15</p>
                <p><strong>Efferent Coupling (Ce):</strong> 12</p>
                <p><strong>Afferent Coupling (Ca):</strong> 8</p>
                <p><strong>Instabilità:</strong> 0.60</p>
            </div>
            <div class="dependencies">
                <h4>🔄 Dipendenze Principali</h4>
                <p>UserService, ProductService, PaymentService, EmailService, 
                   LogService, ValidationService...</p>
            </div>
        </div>
    </div>
    
    <div class="design-analysis">
        <h2>📊 Analisi Design Quality</h2>
        
        <div class="issues-distribution">
            <h3>Distribuzione per Tipo</h3>
            <ul>
                <li>🟡 <strong>Bassa Coesione:</strong> 8 classi</li>
                <li>🔴 <strong>Alto Accoppiamento:</strong> 5 classi</li>
                <li>🟠 <strong>Alta Instabilità:</strong> 7 classi</li>
                <li>🔴 <strong>Zone of Pain:</strong> 3 classi</li>
                <li>🔵 <strong>Zone of Uselessness:</strong> 2 classi</li>
            </ul>
        </div>
        
        <div class="severity-distribution">
            <h3>Distribuzione per Severità</h3>
            <ul>
                <li>🔴 <strong>Alti:</strong> 8 issues</li>
                <li>🟡 <strong>Medi:</strong> 15 issues</li>
                <li>🔵 <strong>Bassi:</strong> 5 issues</li>
            </ul>
        </div>
    </div>
    
    <div class="main-sequence-plot">
        <h2>📈 Distance from Main Sequence</h2>
        <p><strong>Classi sulla Main Sequence (D &lt; 0.3):</strong> 89 classi (63%)</p>
        <p><strong>Classi in Zone of Pain (I &lt; 0.2, A &lt; 0.2):</strong> 3 classi</p>
        <p><strong>Classi in Zone of Uselessness (I &gt; 0.8, A &gt; 0.8):</strong> 2 classi</p>
    </div>
</body>
</html>
```

## Metriche di Qualità del Codice

### Algoritmo di Scoring (0-100)

```java
public class CohesionCouplingQualityScorer {
    
    public int calculateQualityScore(CohesionCouplingReport report) {
        int baseScore = 100;
        
        // Penalità per issues per severità
        Map<IssueSeverity, Integer> issues = report.issuesBySeverity;
        baseScore -= issues.getOrDefault(IssueSeverity.CRITICAL, 0) * 25;
        baseScore -= issues.getOrDefault(IssueSeverity.HIGH, 0) * 15;
        baseScore -= issues.getOrDefault(IssueSeverity.MEDIUM, 0) * 8;
        baseScore -= issues.getOrDefault(IssueSeverity.LOW, 0) * 3;
        
        // Penalità per metriche scadenti
        if (report.averageCohesion < 0.4) baseScore -= 25;
        else if (report.averageCohesion < 0.6) baseScore -= 15;
        else if (report.averageCohesion < 0.7) baseScore -= 5;
        
        if (report.averageCoupling > 15) baseScore -= 20;
        else if (report.averageCoupling > 10) baseScore -= 10;
        else if (report.averageCoupling > 7) baseScore -= 5;
        
        // Penalità per Zone of Pain
        int zonePainCount = report.issuesByType.getOrDefault(DesignIssueType.ZONE_OF_PAIN, 0);
        baseScore -= zonePainCount * 15;
        
        // Bonus per good design
        if (report.averageCohesion > 0.8) baseScore += 10;
        if (report.averageCoupling < 5) baseScore += 8;
        if (report.averageInstability > 0.3 && report.averageInstability < 0.7) baseScore += 5; // Good balance
        
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
| **Coesione Media** | >0.8 | 0.6-0.8 | 0.4-0.6 | <0.4 |
| **Accoppiamento Medio** | <5 | 5-8 | 8-12 | >12 |
| **Zone of Pain** | 0 | 0-1 | 2-3 | >3 |
| **Instabilità Media** | 0.3-0.7 | 0.2-0.8 | 0.1-0.9 | <0.1 o >0.9 |

### Segnalazioni per Gravità

#### 🔴 CRITICA
- **Zone of Pain**: Classi concrete molto stabili difficili da modificare
- **God Classes**: Classi con accoppiamento >20 e coesione <0.2
- **Circular Dependencies**: Dipendenze circolari tra classi
- **Violazioni Architetturali Gravi**: Violazioni massive Main Sequence

#### 🟠 ALTA
- **Alto Accoppiamento**: CBO >12 senza giustificazione architetturale
- **Bassa Coesione**: LCOM >0.8 con molti metodi disconnessi
- **Instabilità Eccessiva**: Classi core con instabilità >0.8
- **Dipendenze Inappropriate**: Accoppiamento con classi di livello inferiore

#### 🟡 MEDIA
- **Coesione Migliorabile**: LCOM 0.5-0.8 con pattern refactoring
- **Accoppiamento Moderato**: CBO 8-12 con dipendenze riducibili
- **Main Sequence Deviation**: Distanza 0.3-0.7 dalla sequenza principale
- **Interfacce Mancanti**: Classi concrete usate direttamente

#### 🔵 BASSA
- **Zone of Uselessness**: Astrazioni poco utilizzate
- **Opportunity di Refactoring**: Leggeri miglioramenti possibili
- **Documentazione Design**: Rationale architetturale mancante
- **Pattern Suggestions**: Applicazione pattern design migliorativi

### Valore di Business

- **Manutenibilità Architettura**: Design coeso facilita modifiche future
- **Riduzione Costi Sviluppo**: Basso accoppiamento riduce impatti modifiche
- **Scalabilità Sistema**: Architettura modulare supporta crescita
- **Quality Gate**: Metriche design prevengono degrado qualità
- **Team Velocity**: Codice ben strutturato accelera sviluppo features