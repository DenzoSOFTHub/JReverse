# Report 13: Dipendenze tra Package

**Valore**: ⭐⭐⭐⭐ **Complessità**: 🟡 Media **Tempo**: 6-8 giorni
**Tags**: `#package-dependencies` `#coupling-analysis` `#architecture-quality` `#medium-complexity`

## Descrizione

Analizza le dipendenze tra package per identificare accoppiamento, dipendenze circolari e violazioni architetturali. Fornisce una visione completa della struttura modulare dell'applicazione e identifica opportunità di miglioramento architetturale.

## Sezioni del Report

### 1. Package Dependency Overview
- Mappa delle dipendenze tra package
- Metriche di accoppiamento (afferent/efferent coupling)
- Stabilità dei package (I = Ce / (Ca + Ce))
- Dipendenze circolari tra package

### 2. Coupling Analysis
- Afferent Coupling (Ca) - package che dipendono da questo
- Efferent Coupling (Ce) - package da cui questo dipende
- Instability Index per package
- Distance from Main Sequence

### 3. Architectural Violations
- Violazioni del principio di dipendenza aciclica
- Layer violations (dependency direction)
- Package con responsabilità multiple
- Anti-pattern architetturali

### 4. Package Structure Quality
- Coesione interna dei package
- Principi di responsabilità singola per package
- Modularità e separazione delle responsabilità

## Implementazione con Javassist

```java
public class PackageDependencyAnalyzer {
    
    public PackageDependencyReport analyzePackageDependencies(CtClass[] classes) {
        PackageDependencyReport report = new PackageDependencyReport();
        
        // Costruisce mappa delle dipendenze tra package
        Map<String, PackageInfo> packageMap = buildPackageMap(classes);
        
        for (CtClass ctClass : classes) {
            analyzeClassDependencies(ctClass, packageMap, report);
        }
        
        calculateCouplingMetrics(packageMap, report);
        detectCircularDependencies(packageMap, report);
        identifyArchitecturalViolations(packageMap, report);
        
        return report;
    }
    
    private Map<String, PackageInfo> buildPackageMap(CtClass[] classes) {
        Map<String, PackageInfo> packageMap = new HashMap<>();
        
        for (CtClass ctClass : classes) {
            String packageName = ctClass.getPackageName();
            
            PackageInfo packageInfo = packageMap.computeIfAbsent(packageName, 
                k -> new PackageInfo(k));
            packageInfo.addClass(ctClass.getName());
        }
        
        return packageMap;
    }
    
    private void analyzeClassDependencies(CtClass ctClass, 
                                         Map<String, PackageInfo> packageMap,
                                         PackageDependencyReport report) {
        try {
            String sourcePackage = ctClass.getPackageName();
            PackageInfo sourcePackageInfo = packageMap.get(sourcePackage);
            
            Set<String> referencedClasses = findReferencedClasses(ctClass);
            
            for (String referencedClass : referencedClasses) {
                String targetPackage = extractPackageName(referencedClass);
                
                if (!sourcePackage.equals(targetPackage) && packageMap.containsKey(targetPackage)) {
                    // Dipendenza tra package diversi
                    PackageDependency dependency = new PackageDependency(
                        sourcePackage, targetPackage, ctClass.getName(), referencedClass);
                    
                    sourcePackageInfo.addOutgoingDependency(dependency);
                    packageMap.get(targetPackage).addIncomingDependency(dependency);
                    
                    report.addPackageDependency(dependency);
                }
            }
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi dependencies: " + e.getMessage());
        }
    }
    
    private Set<String> findReferencedClasses(CtClass ctClass) {
        Set<String> referencedClasses = new HashSet<>();
        
        try {
            // Analizza superclass e interfacce
            if (ctClass.getSuperclass() != null) {
                referencedClasses.add(ctClass.getSuperclass().getName());
            }
            
            for (CtClass interfaceClass : ctClass.getInterfaces()) {
                referencedClasses.add(interfaceClass.getName());
            }
            
            // Analizza campi
            for (CtField field : ctClass.getDeclaredFields()) {
                referencedClasses.add(field.getType().getName());
                
                // Analizza annotazioni sui campi
                analyzeAnnotationDependencies(field.getAvailableAnnotations(), referencedClasses);
            }
            
            // Analizza metodi
            for (CtMethod method : ctClass.getDeclaredMethods()) {
                // Return type
                referencedClasses.add(method.getReturnType().getName());
                
                // Parameter types
                for (CtClass paramType : method.getParameterTypes()) {
                    referencedClasses.add(paramType.getName());
                }
                
                // Exception types
                for (CtClass exceptionType : method.getExceptionTypes()) {
                    referencedClasses.add(exceptionType.getName());
                }
                
                // Analizza il body del metodo per altre dipendenze
                analyzeMethodBodyDependencies(method, referencedClasses);
                
                // Annotazioni sui metodi
                analyzeAnnotationDependencies(method.getAvailableAnnotations(), referencedClasses);
            }
            
            // Analizza annotazioni sulla classe
            analyzeAnnotationDependencies(ctClass.getAvailableAnnotations(), referencedClasses);
            
        } catch (Exception e) {
            // Log error but continue
        }
        
        return referencedClasses;
    }
    
    private void analyzeMethodBodyDependencies(CtMethod method, Set<String> referencedClasses) {
        try {
            method.instrument(new ExprEditor() {
                @Override
                public void edit(MethodCall call) throws CannotCompileException {
                    referencedClasses.add(call.getClassName());
                }
                
                @Override
                public void edit(FieldAccess access) throws CannotCompileException {
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
            });
        } catch (Exception e) {
            // Log error but continue
        }
    }
    
    private void calculateCouplingMetrics(Map<String, PackageInfo> packageMap, 
                                        PackageDependencyReport report) {
        for (PackageInfo packageInfo : packageMap.values()) {
            CouplingMetrics metrics = new CouplingMetrics();
            
            // Afferent Coupling (Ca) - package che dipendono da questo
            int afferentCoupling = packageInfo.getIncomingDependencies().size();
            metrics.setAfferentCoupling(afferentCoupling);
            
            // Efferent Coupling (Ce) - package da cui questo dipende
            int efferentCoupling = packageInfo.getOutgoingDependencies().size();
            metrics.setEfferentCoupling(efferentCoupling);
            
            // Instability (I = Ce / (Ca + Ce))
            double instability = 0.0;
            if (afferentCoupling + efferentCoupling > 0) {
                instability = (double) efferentCoupling / (afferentCoupling + efferentCoupling);
            }
            metrics.setInstability(instability);
            
            // Abstractness - analizza le classi astratte/interfacce nel package
            double abstractness = calculateAbstractness(packageInfo);
            metrics.setAbstractness(abstractness);
            
            // Distance from Main Sequence |A + I - 1|
            double distance = Math.abs(abstractness + instability - 1);
            metrics.setDistanceFromMainSequence(distance);
            
            packageInfo.setCouplingMetrics(metrics);
            report.addPackageMetrics(packageInfo.getPackageName(), metrics);
        }
    }
    
    private void detectCircularDependencies(Map<String, PackageInfo> packageMap, 
                                          PackageDependencyReport report) {
        // Implementa algoritmo DFS per trovare cicli
        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();
        
        for (String packageName : packageMap.keySet()) {
            if (!visited.contains(packageName)) {
                List<String> cycle = findPackageCycle(packageName, packageMap, 
                                                    visited, recursionStack, new ArrayList<>());
                if (!cycle.isEmpty()) {
                    CircularPackageDependency circularDep = new CircularPackageDependency(cycle);
                    report.addCircularDependency(circularDep);
                }
            }
        }
    }
    
    private List<String> findPackageCycle(String currentPackage, 
                                        Map<String, PackageInfo> packageMap,
                                        Set<String> visited, 
                                        Set<String> recursionStack, 
                                        List<String> path) {
        visited.add(currentPackage);
        recursionStack.add(currentPackage);
        path.add(currentPackage);
        
        PackageInfo packageInfo = packageMap.get(currentPackage);
        if (packageInfo != null) {
            for (PackageDependency dependency : packageInfo.getOutgoingDependencies()) {
                String targetPackage = dependency.getTargetPackage();
                
                if (recursionStack.contains(targetPackage)) {
                    // Trovato ciclo
                    List<String> cycle = new ArrayList<>();
                    int cycleStart = path.indexOf(targetPackage);
                    cycle.addAll(path.subList(cycleStart, path.size()));
                    cycle.add(targetPackage); // Chiudi il ciclo
                    return cycle;
                }
                
                if (!visited.contains(targetPackage)) {
                    List<String> cycle = findPackageCycle(targetPackage, packageMap, 
                                                        visited, recursionStack, new ArrayList<>(path));
                    if (!cycle.isEmpty()) {
                        return cycle;
                    }
                }
            }
        }
        
        recursionStack.remove(currentPackage);
        return new ArrayList<>(); // Nessun ciclo trovato
    }
}

public class PackageDependencyReport {
    private List<PackageDependency> packageDependencies = new ArrayList<>();
    private Map<String, CouplingMetrics> packageMetrics = new HashMap<>();
    private List<CircularPackageDependency> circularDependencies = new ArrayList<>();
    private List<ArchitecturalViolation> architecturalViolations = new ArrayList<>();
    private List<String> errors = new ArrayList<>();
    
    public static class PackageDependency {
        private String sourcePackage;
        private String targetPackage;
        private String sourceClass;
        private String targetClass;
        private DependencyType type;
        private int strength; // Numero di dipendenze tra i package
    }
    
    public static class CouplingMetrics {
        private int afferentCoupling; // Ca
        private int efferentCoupling; // Ce
        private double instability;   // I
        private double abstractness;  // A
        private double distanceFromMainSequence; // |A + I - 1|
    }
    
    public static class CircularPackageDependency {
        private List<String> cyclePath;
        private int cycleLength;
        private String severity;
        private String recommendation;
    }
}
```

## Raccolta Dati

### 1. Package Dependencies
- Dipendenze dirette tra package tramite import e usage
- Forza delle dipendenze (numero di classi coinvolte)
- Tipo di dipendenze (inheritance, composition, usage)

### 2. Coupling Metrics
- Afferent Coupling: package che dipendono da questo package
- Efferent Coupling: package da cui questo package dipende
- Instability Index: misura la stabilità del package
- Abstractness: percentuale di classi astratte/interfacce

### 3. Circular Dependencies
- Cicli di dipendenze tra package
- Lunghezza e complessità dei cicli
- Impatto sulla modularità

### 4. Architectural Analysis
- Violazioni dei principi di layered architecture
- Package con responsabilità multiple
- Opportunità di refactoring

## Metriche di Valore

- **Maintainability**: Identifica package difficili da modificare
- **Testability**: Evidenzia dipendenze che complicano il testing
- **Modularity**: Valuta la qualità della struttura modulare
- **Refactoring Opportunities**: Suggerisce miglioramenti architetturali

## Metriche di Qualità del Codice

### Algoritmo di Calcolo (0-100)

```java
public int calculatePackageDependencyQualityScore(PackageDependencyReport result) {
    double score = 100.0;
    
    // Penalizzazioni per problemi architetturali
    score -= result.getCircularDependencies() * 25;           // -25 per dipendenza circolare
    score -= result.getHighCouplingPackages() * 15;           // -15 per package ad alto accoppiamento
    score -= result.getLayerViolations() * 20;                // -20 per violazione layer architecture
    score -= result.getUnstablePackages() * 12;               // -12 per package instabili
    score -= result.getGodPackages() * 10;                    // -10 per package con troppe responsabilità
    score -= result.getOrphanPackages() * 8;                  // -8 per package isolati
    score -= result.getInconsistentNaming() * 5;              // -5 per naming inconsistente
    
    // Bonus per buona architettura
    score += result.getWellLayeredPackages() * 3;             // +3 per layering corretto
    score += result.getLowCouplingPackages() * 2;             // +2 per basso accoppiamento
    score += result.getStablePackages() * 1;                  // +1 per package stabili
    
    return Math.max(0, Math.min(100, (int) score));
}
```

### Soglie di Valutazione
- **0-40**: 🔴 CRITICO - Architettura con gravi problemi strutturali
- **41-60**: 🟡 SUFFICIENTE - Struttura accettabile ma necessita refactoring
- **61-80**: 🟢 BUONO - Architettura ben organizzata con margini di miglioramento
- **81-100**: ⭐ ECCELLENTE - Architettura modulare ottimale e ben strutturata

## Segnalazioni per Gravità

### 🔴 GRAVITÀ CRITICA (Score Impact: -20 to -25)
1. **Dipendenze circolari tra package**
   - Descrizione: Package che si referenziano reciprocamente creando cicli
   - Rischio: Impossibilità di compilazione incrementale, tight coupling
   - Soluzione: Refactoring per spezzare i cicli, introdurre interfacce

2. **Violazioni layer architecture**
   - Descrizione: Package di layer superiori che dipendono da layer inferiori
   - Rischio: Inversione delle dipendenze, architettura compromessa
   - Soluzione: Rispettare dependency inversion principle

### 🟠 GRAVITÀ ALTA (Score Impact: -12 to -15)
3. **Package ad alto accoppiamento**
   - Descrizione: Package con troppi incoming/outgoing dependencies
   - Rischio: Difficoltà di modifica, effetto domino nei cambiamenti
   - Soluzione: Spezzare responsabilità, ridurre dipendenze

4. **Package instabili**
   - Descrizione: Alto instability index (I = Ce / (Ca + Ce))
   - Rischio: Frequenti cambiamenti propagati ad altri package
   - Soluzione: Stabilizzare interfacce, ridurre efferent coupling

### 🟡 GRAVITÀ MEDIA (Score Impact: -8 to -10)
5. **God package (troppe responsabilità)**
   - Descrizione: Package che contengono classi con responsabilità diverse
   - Rischio: Violazione Single Responsibility Principle
   - Soluzione: Suddividere in package più specifici

6. **Package orfani**
   - Descrizione: Package utilizzati da nessuno o che non utilizzano altri
   - Rischio: Dead code, manutenzione inutile
   - Soluzione: Rimuovere se non utilizzati o integrare meglio

### 🔵 GRAVITÀ BASSA (Score Impact: -1 to -5)
7. **Naming inconsistente**
   - Descrizione: Convenzioni di naming non uniformi tra package
   - Rischio: Confusione, difficoltà di navigazione del codice
   - Soluzione: Standardizzare naming conventions

## Classificazione

**Categoria**: Architecture & Dependencies
**Priorità**: Alta - La struttura dei package influenza l'intera architettura
**Stakeholder**: Architecture team, Development team, Technical leads