# Report 50: Violazioni Architetturali

**Valore**: ⭐⭐⭐⭐⭐ **Complessità**: 🔴 Molto Complessa **Tempo**: 10-12 giorni
**Tags**: `#architectural-violations` `#design-principles` `#code-quality` `#technical-debt`

## Descrizione

Identifica violazioni dei principi architetturali e pattern di design, includendo violazioni SOLID, dipendenze circolari, layering violations, anti-patterns e deviazioni dall'architettura target definita per l'applicazione.

## Sezioni del Report

### 1. SOLID Principles Violations
- Single Responsibility Principle violations
- Open/Closed Principle violations  
- Liskov Substitution Principle violations
- Interface Segregation Principle violations
- Dependency Inversion Principle violations

### 2. Architectural Layer Violations
- Layering architecture compliance
- Dependency direction violations
- Cross-cutting concerns violations
- Modularity violations

### 3. Design Pattern Anti-Patterns
- God Object anti-pattern
- Spaghetti Code detection
- Copy-Paste Programming
- Magic Numbers/Strings
- Inappropriate Intimacy

### 4. Dependency Architecture Analysis
- Circular dependencies detection
- Package coupling analysis
- Dependency stability metrics
- Architecture decision records violations

## Implementazione con Javassist

```java
public class ArchitecturalViolationsAnalyzer {
    
    private final Map<String, ArchitecturalLayer> layerDefinitions;
    private final List<ArchitecturalRule> architecturalRules;
    
    public ArchitecturalViolationsReport analyzeViolations(CtClass[] classes) {
        ArchitecturalViolationsReport report = new ArchitecturalViolationsReport();
        
        // Costruisce grafo delle dipendenze
        DependencyGraph dependencyGraph = buildDependencyGraph(classes);
        
        for (CtClass ctClass : classes) {
            analyzeSolidViolations(ctClass, report);
            analyzeLayerViolations(ctClass, dependencyGraph, report);
            analyzeAntiPatterns(ctClass, report);
            analyzeCouplingViolations(ctClass, dependencyGraph, report);
        }
        
        analyzeCircularDependencies(dependencyGraph, report);
        evaluateArchitecturalHealth(report);
        return report;
    }
    
    private void analyzeSolidViolations(CtClass ctClass, ArchitecturalViolationsReport report) {
        try {
            // Single Responsibility Principle
            analyzeSRPViolations(ctClass, report);
            
            // Open/Closed Principle
            analyzeOCPViolations(ctClass, report);
            
            // Liskov Substitution Principle
            analyzeLSPViolations(ctClass, report);
            
            // Interface Segregation Principle
            analyzeISPViolations(ctClass, report);
            
            // Dependency Inversion Principle
            analyzeDIPViolations(ctClass, report);
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi SOLID violations: " + e.getMessage());
        }
    }
    
    private void analyzeSRPViolations(CtClass ctClass, ArchitecturalViolationsReport report) {
        try {
            SRPAnalysis srpAnalysis = new SRPAnalysis();
            srpAnalysis.setClassName(ctClass.getName());
            
            CtMethod[] methods = ctClass.getDeclaredMethods();
            CtField[] fields = ctClass.getDeclaredFields();
            
            // Analizza responsabilità multiple
            Set<String> responsibilities = identifyResponsibilities(ctClass);
            srpAnalysis.setIdentifiedResponsibilities(new ArrayList<>(responsibilities));
            
            if (responsibilities.size() > 1) {
                SolidViolation violation = new SolidViolation();
                violation.setPrinciple("Single Responsibility Principle");
                violation.setClassName(ctClass.getName());
                violation.setSeverity("HIGH");
                violation.setDescription("Class has " + responsibilities.size() + " responsibilities: " + 
                                       String.join(", ", responsibilities));
                violation.setRecommendation("Split class into smaller classes with single responsibility");
                
                report.addSolidViolation(violation);
            }
            
            // Analizza metriche di coesione
            double cohesionMetric = calculateClassCohesion(ctClass);
            srpAnalysis.setCohesionMetric(cohesionMetric);
            
            if (cohesionMetric < 0.3) { // Soglia bassa di coesione
                SolidViolation violation = new SolidViolation();
                violation.setPrinciple("Single Responsibility Principle");
                violation.setClassName(ctClass.getName());
                violation.setSeverity("MEDIUM");
                violation.setDescription("Low class cohesion: " + String.format("%.2f", cohesionMetric));
                violation.setRecommendation("Improve class cohesion by grouping related functionality");
                
                report.addSolidViolation(violation);
            }
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi SRP: " + e.getMessage());
        }
    }
    
    private void analyzeLayerViolations(CtClass ctClass, DependencyGraph graph, 
                                       ArchitecturalViolationsReport report) {
        try {
            String className = ctClass.getName();
            ArchitecturalLayer currentLayer = determineLayer(className);
            
            if (currentLayer == null) {
                LayerViolation violation = new LayerViolation();
                violation.setClassName(className);
                violation.setViolationType("UNDEFINED_LAYER");
                violation.setDescription("Class does not belong to any defined architectural layer");
                violation.setSeverity("MEDIUM");
                
                report.addLayerViolation(violation);
                return;
            }
            
            // Analizza dipendenze del layer
            Set<String> dependencies = graph.getDependencies(className);
            
            for (String dependency : dependencies) {
                ArchitecturalLayer dependencyLayer = determineLayer(dependency);
                
                if (dependencyLayer != null && !isAllowedDependency(currentLayer, dependencyLayer)) {
                    LayerViolation violation = new LayerViolation();
                    violation.setClassName(className);
                    violation.setDependentClass(dependency);
                    violation.setSourceLayer(currentLayer.getName());
                    violation.setTargetLayer(dependencyLayer.getName());
                    violation.setViolationType("INVALID_LAYER_DEPENDENCY");
                    violation.setDescription(String.format("Layer %s should not depend on layer %s", 
                                                         currentLayer.getName(), dependencyLayer.getName()));
                    violation.setSeverity("HIGH");
                    
                    report.addLayerViolation(violation);
                }
            }
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi layer violations: " + e.getMessage());
        }
    }
    
    private void analyzeAntiPatterns(CtClass ctClass, ArchitecturalViolationsReport report) {
        try {
            // God Object detection
            analyzeGodObject(ctClass, report);
            
            // Long Parameter List
            analyzeLongParameterList(ctClass, report);
            
            // Large Class detection
            analyzeLargeClass(ctClass, report);
            
            // Feature Envy
            analyzeFeatureEnvy(ctClass, report);
            
            // Data Class
            analyzeDataClass(ctClass, report);
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi anti-patterns: " + e.getMessage());
        }
    }
    
    private void analyzeGodObject(CtClass ctClass, ArchitecturalViolationsReport report) {
        try {
            CtMethod[] methods = ctClass.getDeclaredMethods();
            CtField[] fields = ctClass.getDeclaredFields();
            
            int totalMethods = methods.length;
            int totalFields = fields.length;
            int totalLinesOfCode = calculateLinesOfCode(ctClass);
            
            // Soglie per God Object
            boolean isGodObject = totalMethods > 50 || totalFields > 20 || totalLinesOfCode > 1000;
            
            if (isGodObject) {
                AntiPattern antiPattern = new AntiPattern();
                antiPattern.setPatternType("God Object");
                antiPattern.setClassName(ctClass.getName());
                antiPattern.setSeverity("HIGH");
                antiPattern.setDescription(String.format(
                    "Class has %d methods, %d fields, and %d lines of code", 
                    totalMethods, totalFields, totalLinesOfCode));
                antiPattern.setRecommendation("Break down into smaller, more focused classes");
                
                // Metriche dettagliate
                Map<String, Object> metrics = new HashMap<>();
                metrics.put("methods", totalMethods);
                metrics.put("fields", totalFields);
                metrics.put("linesOfCode", totalLinesOfCode);
                antiPattern.setMetrics(metrics);
                
                report.addAntiPattern(antiPattern);
            }
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi God Object: " + e.getMessage());
        }
    }
    
    private void analyzeCircularDependencies(DependencyGraph graph, ArchitecturalViolationsReport report) {
        try {
            List<List<String>> cycles = graph.findCircularDependencies();
            
            for (List<String> cycle : cycles) {
                CircularDependency circularDep = new CircularDependency();
                circularDep.setCyclePath(cycle);
                circularDep.setCycleLength(cycle.size());
                circularDep.setSeverity(determineCycleSeverity(cycle.size()));
                circularDep.setDescription("Circular dependency detected: " + String.join(" -> ", cycle));
                circularDep.setRecommendation(generateCycleBreakingRecommendation(cycle));
                
                report.addCircularDependency(circularDep);
            }
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi circular dependencies: " + e.getMessage());
        }
    }
    
    private Set<String> identifyResponsibilities(CtClass ctClass) {
        Set<String> responsibilities = new HashSet<>();
        
        try {
            CtMethod[] methods = ctClass.getDeclaredMethods();
            
            for (CtMethod method : methods) {
                String methodName = method.getName();
                String methodBody = getMethodBody(method);
                
                // Identifica responsabilità basate su pattern nei nomi dei metodi
                if (methodName.startsWith("save") || methodName.startsWith("persist") ||
                    methodName.startsWith("store") || methodBody.contains("repository")) {
                    responsibilities.add("Data Persistence");
                }
                
                if (methodName.startsWith("validate") || methodBody.contains("validation")) {
                    responsibilities.add("Validation");
                }
                
                if (methodName.startsWith("calculate") || methodName.startsWith("compute")) {
                    responsibilities.add("Calculation");
                }
                
                if (methodName.startsWith("format") || methodName.startsWith("convert") ||
                    methodName.startsWith("transform")) {
                    responsibilities.add("Data Transformation");
                }
                
                if (methodBody.contains("HttpClient") || methodBody.contains("RestTemplate") ||
                    methodBody.contains("WebClient")) {
                    responsibilities.add("External Communication");
                }
                
                if (methodBody.contains("logger") || methodBody.contains("log.")) {
                    responsibilities.add("Logging");
                }
                
                // Analisi più sofisticata basata su importi e dipendenze
                analyzeMethodResponsibilities(method, responsibilities);
            }
            
        } catch (Exception e) {
            // Log error but continue
        }
        
        return responsibilities;
    }
    
    private void evaluateArchitecturalHealth(ArchitecturalViolationsReport report) {
        ArchitecturalHealthMetrics metrics = new ArchitecturalHealthMetrics();
        
        // Calcola metriche di salute architettuale
        int totalViolations = report.getSolidViolations().size() + 
                             report.getLayerViolations().size() + 
                             report.getAntiPatterns().size() + 
                             report.getCircularDependencies().size();
        
        metrics.setTotalViolations(totalViolations);
        
        // Calcola indice di salute (0-100)
        double healthIndex = calculateArchitecturalHealthIndex(report);
        metrics.setHealthIndex(healthIndex);
        
        // Genera raccomandazioni prioritarie
        List<String> recommendations = generatePriorityRecommendations(report);
        metrics.setRecommendations(recommendations);
        
        report.setArchitecturalHealthMetrics(metrics);
    }
}

public class ArchitecturalViolationsReport {
    private List<SolidViolation> solidViolations = new ArrayList<>();
    private List<LayerViolation> layerViolations = new ArrayList<>();
    private List<AntiPattern> antiPatterns = new ArrayList<>();
    private List<CircularDependency> circularDependencies = new ArrayList<>();
    private ArchitecturalHealthMetrics architecturalHealthMetrics;
    private List<String> errors = new ArrayList<>();
    
    public static class SolidViolation {
        private String principle;
        private String className;
        private String severity;
        private String description;
        private String recommendation;
        private Map<String, Object> metrics = new HashMap<>();
    }
    
    public static class LayerViolation {
        private String className;
        private String dependentClass;
        private String sourceLayer;
        private String targetLayer;
        private String violationType;
        private String description;
        private String severity;
    }
    
    public static class AntiPattern {
        private String patternType;
        private String className;
        private String severity;
        private String description;
        private String recommendation;
        private Map<String, Object> metrics = new HashMap<>();
    }
    
    public static class ArchitecturalHealthMetrics {
        private int totalViolations;
        private double healthIndex;
        private Map<String, Integer> violationsByCategory = new HashMap<>();
        private List<String> recommendations = new ArrayList<>();
    }
}
```

## Raccolta Dati

### 1. SOLID Violations
- Analisi Single Responsibility Principle tramite analisi delle responsabilità
- Open/Closed violations tramite analisi di modificabilità
- Liskov Substitution tramite analisi dell'ereditarietà
- Interface Segregation tramite analisi delle interfacce
- Dependency Inversion tramite analisi delle dipendenze

### 2. Architectural Layers
- Mapping di classi a layer architetturali
- Analisi delle dipendenze inter-layer
- Violazioni delle regole di layering

### 3. Anti-Pattern Detection
- God Object detection tramite metriche di dimensioni
- Spaghetti Code tramite analisi della complessità
- Feature Envy tramite analisi dell'uso di metodi esterni

### 4. Dependency Analysis
- Costruzione del grafo delle dipendenze
- Detection di cicli nelle dipendenze
- Analisi della stabilità delle dipendenze

## Metriche di Qualità del Codice

### Algoritmo di Calcolo (0-100)

```java
public int calculateArchitecturalQualityScore(ArchitecturalViolationsReport result) {
    double score = 100.0;
    
    // Penalizzazioni per violazioni architetturali critiche
    score -= result.getSolidViolations().size() * 12;         // -12 per violazione SOLID
    score -= result.getLayerViolations().size() * 20;         // -20 per violazione layering
    score -= result.getGodObjects() * 25;                     // -25 per God Object
    score -= result.getCircularDependencies().size() * 18;    // -18 per dipendenza circolare
    score -= result.getSpaghettiCodeClasses() * 15;           // -15 per Spaghetti Code
    score -= result.getFeatureEnvyViolations() * 8;           // -8 per Feature Envy
    score -= result.getDataClasses() * 6;                     // -6 per Data Class
    score -= result.getLongParameterLists() * 4;              // -4 per Long Parameter List
    
    // Bonus per buona architettura
    score += result.getWellDesignedClasses() * 2;             // +2 per classi ben progettate
    score += result.getProperLayeringSeparation() * 3;        // +3 per layering corretto
    score += result.getGoodCohesionClasses() * 1;             // +1 per buona coesione
    
    return Math.max(0, Math.min(100, (int) score));
}
```

### Soglie di Valutazione
- **0-40**: 🔴 CRITICO - Architettura gravemente compromessa, refactoring urgente
- **41-60**: 🟡 SUFFICIENTE - Problemi architetturali significativi da risolvere
- **61-80**: 🟢 BUONO - Architettura solida con alcuni aspetti da migliorare
- **81-100**: ⭐ ECCELLENTE - Architettura di alta qualità che rispetta i principi di design

## Segnalazioni per Gravità

### 🔴 GRAVITÀ CRITICA (Score Impact: -18 to -25)
1. **God Object Anti-Pattern**
   - Descrizione: Classi con troppe responsabilità, metodi e campi (>50 metodi, >20 campi)
   - Rischio: Difficoltà manutenzione, testing, comprensibilità del codice
   - Soluzione: Spezzare in classi più piccole con responsabilità specifiche

2. **Violazioni Layer Architecture**
   - Descrizione: Dipendenze che violano la direzione dei layer (es: Domain → Infrastructure)
   - Rischio: Architettura compromessa, difficoltà testing e deployment
   - Soluzione: Applicare Dependency Inversion Principle, introdurre interfacce

3. **Dipendenze Circolari**
   - Descrizione: Cicli di dipendenze tra classi o package
   - Rischio: Compilazione problematica, tight coupling, difficoltà refactoring
   - Soluzione: Spezzare i cicli introducendo interfacce o mediatori

### 🟠 GRAVITÀ ALTA (Score Impact: -12 to -15)
4. **Violazioni Principi SOLID**
   - Descrizione: Classi che violano Single Responsibility, Open/Closed, etc.
   - Rischio: Codice difficile da estendere, modificare e testare
   - Soluzione: Refactoring per rispettare i principi SOLID

5. **Spaghetti Code Pattern**
   - Descrizione: Codice con alta complessità ciclomatica e flusso di controllo confuso
   - Rischio: Bug difficili da trovare, manutenzione costosa
   - Soluzione: Semplificare logica, estrarre metodi, ridurre complessità

### 🟡 GRAVITÀ MEDIA (Score Impact: -6 to -8)
6. **Feature Envy**
   - Descrizione: Classi che utilizzano più metodi/dati di altre classi che proprie
   - Rischio: Accoppiamento inappropriato, responsabilità mal distribuite
   - Soluzione: Spostare metodi nella classe appropriata

7. **Data Class Anti-Pattern**
   - Descrizione: Classi che contengono solo dati senza comportamenti
   - Rischio: Logica business distribuita inappropriatamente
   - Soluzione: Aggiungere comportamenti alla classe o valutare se è appropriata

### 🔵 GRAVITÀ BASSA (Score Impact: -1 to -4)
8. **Long Parameter List**
   - Descrizione: Metodi con troppi parametri (>5-6)
   - Rischio: Difficoltà nell'uso dell'API, errori nei parametri
   - Soluzione: Introdurre Parameter Object o Builder pattern

## Metriche di Valore

- **Maintainability**: Identifica problemi che rendono difficile la manutenzione
- **Technical Debt**: Quantifica il debito tecnico architetturale
- **Code Quality**: Misura l'aderenza ai principi di design
- **System Evolution**: Valuta la capacità del sistema di evolvere

## Classificazione

**Categoria**: Architecture & Dependencies
**Priorità**: Critica - Le violazioni architetturali impattano l'intera struttura del sistema
**Stakeholder**: Architecture team, Development team, Technical leads